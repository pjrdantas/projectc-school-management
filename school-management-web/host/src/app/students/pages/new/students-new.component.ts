import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';
import { StudentInput } from '../../models/student.model';
import { StudentsService } from '../../services/students.service';
import { validarCPF } from '../../utils/cpf-validator';
import { Responsible } from '../../../responsibles/models/responsible.model';
import { ResponsiblesService } from '../../../responsibles/services/responsibles.service';

@Component({
  selector: 'app-students-new',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './students-new.component.html',
  styleUrls: ['./students-new.component.scss'],
})
export class StudentsNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly studentsService = inject(StudentsService);
  private readonly responsiblesService = inject(ResponsiblesService);

  protected readonly studentId = signal<string | null>(null);
  protected readonly isEditing = computed(() => !!this.studentId());
  protected readonly responsibles = signal<Responsible[]>([]);
  protected readonly selectedResponsibleIds = signal<string[]>([]);
  protected readonly responsibleSearchTerm = signal('');
  protected readonly pendingResponsibleId = signal<string | null>(null);
  protected readonly filteredResponsibles = computed(() => {
    const termRaw = this.responsibleSearchTerm().trim().toLowerCase();
    const termDigits = this.onlyDigits(this.responsibleSearchTerm());
    const selectedIds = new Set(this.selectedResponsibleIds());

    return this.responsibles().filter(responsible => {
      if (selectedIds.has(responsible.id)) {
        return false;
      }

      if (!termRaw && !termDigits) {
        return true;
      }

      const nome = responsible.nomeCompleto.toLowerCase();
      const cpf = this.onlyDigits(responsible.cpf);
      return nome.includes(termRaw) || cpf.includes(termDigits);
    });
  });
  protected readonly searchedResponsible = computed(() => this.filteredResponsibles()[0] ?? null);
  protected readonly selectedResponsibles = computed(() => {
    const selectedIds = new Set(this.selectedResponsibleIds());
    return this.responsibles().filter(item => selectedIds.has(item.id));
  });

  protected readonly alunoForm = this.fb.nonNullable.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: ['', [Validators.required, this.cpfValidator()]],
    dataNascimento: ['', [Validators.required, this.dataBrValidator()]],
    email: ['', [Validators.required, this.emailConsistenteValidator()]],
    telefone: ['', [this.telefoneValidator()]],
  });

  ngOnInit(): void {
    this.responsiblesService.syncFromApi().subscribe({
      next: responsaveis => this.responsibles.set(responsaveis),
      error: () => {
        this.snackBar.open('Não foi possível carregar responsáveis para vínculo.', 'Fechar', {
          duration: 3000,
        });
      },
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.studentsService.fetchByIdFromApi(id).subscribe({
      next: student => {
        this.studentId.set(id);
        this.alunoForm.patchValue({
          nomeCompleto: student.nomeCompleto,
          cpf: this.formatCpf(student.cpf),
          dataNascimento: this.isoToBr(student.dataNascimento),
          email: student.email,
          telefone: student.telefone ?? '',
        });
        this.carregarResponsaveisVinculados(id);
        this.applyCpfDuplicadoValidation();
      },
      error: () => {
        this.snackBar.open('Aluno não encontrado.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/students']);
      },
    });
    this.applyCpfDuplicadoValidation();
  }

  protected onSubmit(): void {
    if (this.alunoForm.invalid) {
      this.alunoForm.markAllAsTouched();
      return;
    }

    const formValue = this.alunoForm.getRawValue();
    const payload: StudentInput = {
      nomeCompleto: formValue.nomeCompleto.trim(),
      cpf: this.onlyDigits(formValue.cpf),
      dataNascimento: this.brToIso(formValue.dataNascimento),
      email: formValue.email.trim().toLowerCase(),
      telefone: formValue.telefone?.trim() || undefined,
    };

    const id = this.studentId();
    if (this.studentsService.isCpfInUse(payload.cpf, id ?? undefined)) {
      this.alunoForm.controls.cpf.setErrors({
        ...this.alunoForm.controls.cpf.errors,
        cpfDuplicado: true,
      });
      this.alunoForm.controls.cpf.markAsTouched();
      this.snackBar.open('Já existe um aluno com este CPF.', 'Fechar', { duration: 3000 });
      return;
    }

    const request$ = id
      ? this.studentsService.updateOnApi(id, payload)
      : this.studentsService.createOnApi(payload);

    request$
      .pipe(switchMap(student => this.sincronizarResponsaveis(student.id)))
      .subscribe({
        next: () => {
          const mensagem = id
            ? 'Aluno atualizado com sucesso.'
            : 'Aluno cadastrado com sucesso.';
          this.snackBar.open(mensagem, 'Fechar', { duration: 3000 });
          this.router.navigate(['/students']);
        },
        error: (error: { status?: number }) => {
          const message = this.mapApiErrorMessage(error?.status);
          this.snackBar.open(message, 'Fechar', { duration: 4000 });
        },
      });
  }

  protected onCancel(): void {
    this.router.navigate(['/students']);
  }

  protected onCpfInput(): void {
    const cpf = this.alunoForm.controls.cpf.value;
    this.alunoForm.controls.cpf.setValue(this.formatCpf(cpf), { emitEvent: false });
    this.applyCpfDuplicadoValidation();
  }

  protected onDateInput(): void {
    const value = this.alunoForm.controls.dataNascimento.value;
    this.alunoForm.controls.dataNascimento.setValue(this.formatDateBr(value), {
      emitEvent: false,
    });
  }

  protected onTelefoneInput(): void {
    const telefone = this.alunoForm.controls.telefone.value;
    this.alunoForm.controls.telefone.setValue(this.formatTelefone(telefone), { emitEvent: false });
  }

  protected formatCpf(cpf: string): string {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  protected onResponsibleSearchInput(value: string): void {
    this.responsibleSearchTerm.set(value);
    this.pendingResponsibleId.set(null);
  }

  protected onResponsibleOptionSelected(idResponsible: string): void {
    this.pendingResponsibleId.set(idResponsible);
  }

  protected addResponsibleFromSearch(): void {
    const selectedFromAutocomplete = this.pendingResponsibleId()
      ? this.responsibles().find(item => item.id === this.pendingResponsibleId())
      : null;
    const found = selectedFromAutocomplete ?? this.searchedResponsible();
    if (!found) {
      this.snackBar.open('Nenhum responsável encontrado para o termo informado.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    const selectedIds = this.selectedResponsibleIds();
    if (selectedIds.includes(found.id)) {
      this.snackBar.open('Este responsável já foi selecionado.', 'Fechar', {
        duration: 2500,
      });
      return;
    }

    this.selectedResponsibleIds.set([...selectedIds, found.id]);
    this.responsibleSearchTerm.set('');
    this.pendingResponsibleId.set(null);
  }

  protected removeResponsible(idResponsible: string): void {
    const updatedIds = this.selectedResponsibleIds().filter(id => id !== idResponsible);
    this.selectedResponsibleIds.set(updatedIds);
  }

  protected displayResponsible = (idResponsible: string | null): string => {
    if (!idResponsible) {
      return '';
    }

    const responsible = this.responsibles().find(item => item.id === idResponsible);
    return responsible ? `${responsible.nomeCompleto} - ${this.formatCpf(responsible.cpf)}` : '';
  };

  private carregarResponsaveisVinculados(idAluno: string): void {
    this.responsiblesService.listarResponsaveisPorAluno(idAluno).subscribe({
      next: responsaveis => {
        const ids = responsaveis.map(item => item.id);
        this.selectedResponsibleIds.set(ids);
      },
      error: () => {
        this.snackBar.open('Não foi possível carregar responsáveis já vinculados.', 'Fechar', {
          duration: 3000,
        });
      },
    });
  }

  private sincronizarResponsaveis(idAluno: string): Observable<void> {
    const selectedIds = [...new Set(this.selectedResponsibleIds())];

    return this.responsiblesService.listarResponsaveisPorAluno(idAluno).pipe(
      switchMap(vinculosAtuais => {
        const atuaisIds = vinculosAtuais.map(item => item.id);
        const toAdd = selectedIds.filter(id => !atuaisIds.includes(id));
        const toRemove = atuaisIds.filter(id => !selectedIds.includes(id));

        const requests: Observable<unknown>[] = [
          ...toAdd.map(idResponsavel =>
            this.responsiblesService.vincularAlunoResponsavel(idAluno, idResponsavel),
          ),
          ...toRemove.map(idResponsavel =>
            this.responsiblesService.desvincularAlunoResponsavel(idAluno, idResponsavel),
          ),
        ];

        if (requests.length === 0) {
          return of(void 0);
        }

        return forkJoin(requests).pipe(map(() => void 0));
      }),
    );
  }

  private applyCpfDuplicadoValidation(): void {
    const cpfControl = this.alunoForm.controls.cpf;
    const cpf = this.onlyDigits(cpfControl.value);

    if (cpf.length < 11) {
      if (cpfControl.hasError('cpfDuplicado')) {
        const { cpfDuplicado, ...rest } = cpfControl.errors ?? {};
        cpfControl.setErrors(Object.keys(rest).length ? rest : null);
      }
      return;
    }

    const duplicado = this.studentsService.isCpfInUse(cpf, this.studentId() ?? undefined);
    if (duplicado) {
      cpfControl.setErrors({ ...cpfControl.errors, cpfDuplicado: true });
      return;
    }

    if (cpfControl.hasError('cpfDuplicado')) {
      const { cpfDuplicado, ...rest } = cpfControl.errors ?? {};
      cpfControl.setErrors(Object.keys(rest).length ? rest : null);
    }
  }

  private cpfValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      return validarCPF(String(control.value)) ? null : { cpfInvalido: true };
    };
  }

  private emailConsistenteValidator(): ValidatorFn {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+$/;
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      return emailRegex.test(String(control.value).trim()) ? null : { emailInvalido: true };
    };
  }

  private telefoneValidator(): ValidatorFn {
    const phoneRegex = /^$|^\(\d{2}\)\s\d{4,5}-\d{4}$/;
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      return phoneRegex.test(String(control.value)) ? null : { telefoneInvalido: true };
    };
  }

  private dataBrValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }

      const value = String(control.value);
      const validPattern = /^\d{2}\/\d{2}\/\d{4}$/.test(value);
      if (!validPattern) {
        return { dataInvalida: true };
      }

      const [dd, mm, yyyy] = value.split('/').map(Number);
      const date = new Date(yyyy, mm - 1, dd);
      const validDate =
        date.getFullYear() === yyyy && date.getMonth() === mm - 1 && date.getDate() === dd;

      return validDate ? null : { dataInvalida: true };
    };
  }

  private mapApiErrorMessage(status?: number): string {
    if (status === 401) {
      return 'Sessão inválida. Faça login novamente.';
    }

    if (status === 409) {
      return 'Já existe aluno cadastrado com este CPF.';
    }

    if (status === 400) {
      return 'Dados inválidos. Revise os campos obrigatórios.';
    }

    return 'Erro ao cadastrar aluno no backend.';
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private formatDateBr(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 8);
    if (digits.length <= 2) return digits;
    if (digits.length <= 4) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
  }

  private formatTelefone(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 11);
    if (digits.length <= 2) {
      return digits.length ? `(${digits}` : '';
    }

    if (digits.length <= 6) {
      return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
    }

    const isNineDigits = digits.length > 10;
    const prefixEnd = isNineDigits ? 7 : 6;
    return `(${digits.slice(0, 2)}) ${digits.slice(2, prefixEnd)}-${digits.slice(prefixEnd)}`;
  }

  private brToIso(value: string): string {
    const [dd, mm, yyyy] = value.split('/');
    return `${yyyy}-${mm}-${dd}`;
  }

  private isoToBr(value: string): string {
    const [yyyy, mm, dd] = value.split('-');
    return `${dd}/${mm}/${yyyy}`;
  }
}
