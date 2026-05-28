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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { DocumentsPanelComponent } from '../../../shared/documents/documents-panel.component';
import { validarCPF } from '../../../students/utils/cpf-validator';
import { ResponsibleInput } from '../../models/responsible.model';
import { ResponsiblesService } from '../../services/responsibles.service';

@Component({
  selector: 'app-responsibles-new',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    DocumentsPanelComponent,
  ],
  templateUrl: './responsibles-new.component.html',
  styleUrls: ['./responsibles-new.component.scss'],
})
export class ResponsiblesNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly responsiblesService = inject(ResponsiblesService);

  protected readonly responsibleId = signal<string | null>(null);
  protected readonly isEditing = computed(() => !!this.responsibleId());
  protected readonly buscandoCep = signal(false);
  protected readonly documentosObrigatoriosResponsavel = ['RG', 'CPF', 'COMPROVANTE_RESIDENCIA'];
  protected readonly documentosDisponiveisResponsavel = [
    'RG',
    'CPF',
    'COMPROVANTE_RESIDENCIA',
    'LAUDO',
    'OUTROS',
  ];

  protected readonly form = this.fb.nonNullable.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: ['', [Validators.required, this.cpfValidator()]],
    rg: ['', [Validators.maxLength(20)]],
    email: ['', [Validators.email]],
    telefone: ['', [this.telefoneValidator()]],
    cep: ['', [this.cepValidator()]],
    logradouro: ['', [Validators.maxLength(150)]],
    numero: ['', [Validators.maxLength(20)]],
    complemento: ['', [Validators.maxLength(100)]],
    bairro: ['', [Validators.maxLength(100)]],
    cidade: ['', [Validators.maxLength(100)]],
    uf: ['', [Validators.maxLength(2)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.responsiblesService.fetchByIdFromApi(id).subscribe({
      next: (responsible) => {
        this.responsibleId.set(id);
        this.form.patchValue({
          nomeCompleto: responsible.nomeCompleto,
          cpf: this.formatCpf(responsible.cpf),
          rg: responsible.rg ?? '',
          email: responsible.email ?? '',
          telefone: responsible.telefone ?? '',
          cep: responsible.cep ? this.formatCep(responsible.cep) : '',
          logradouro: responsible.logradouro ?? '',
          numero: responsible.numero ?? '',
          complemento: responsible.complemento ?? '',
          bairro: responsible.bairro ?? '',
          cidade: responsible.cidade ?? '',
          uf: responsible.uf ?? '',
        });
      },
      error: () => {
        this.snackBar.open('Responsável não encontrado.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/responsibles']);
      },
    });
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: ResponsibleInput = {
      nomeCompleto: raw.nomeCompleto.trim(),
      cpf: this.onlyDigits(raw.cpf),
      rg: this.optional(raw.rg),
      email: raw.email?.trim() || undefined,
      telefone: raw.telefone?.trim() || undefined,
      cep: this.optional(this.onlyDigits(raw.cep)),
      logradouro: this.optional(raw.logradouro),
      numero: this.optional(raw.numero),
      complemento: this.optional(raw.complemento),
      bairro: this.optional(raw.bairro),
      cidade: this.optional(raw.cidade),
      uf: this.optional(raw.uf)?.toUpperCase(),
    };

    if (payload.cep && !payload.numero) {
      this.form.controls.numero.setErrors({
        ...this.form.controls.numero.errors,
        numeroObrigatorio: true,
      });
      this.form.controls.numero.markAsTouched();
      this.snackBar.open('Número é obrigatório quando CEP é informado.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    const id = this.responsibleId();
    if (id) {
      this.responsiblesService.updateOnApi(id, payload).subscribe({
        next: () => {
          this.snackBar.open('Responsável atualizado com sucesso.', 'Fechar', { duration: 3000 });
          this.router.navigate(['/responsibles']);
        },
        error: (error: unknown) =>
          this.snackBar.open(
            getApiErrorMessage(error, 'Erro ao atualizar responsável.'),
            'Fechar',
            { duration: 4000 },
          ),
      });
      return;
    }

    this.responsiblesService.createOnApi(payload).subscribe({
      next: (responsible) => {
        this.snackBar.open('Responsável cadastrado com sucesso.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/responsibles', responsible.id, 'edit']);
      },
      error: (error: unknown) =>
        this.snackBar.open(
          getApiErrorMessage(error, 'Erro ao cadastrar responsável.'),
          'Fechar',
          { duration: 4000 },
        ),
    });
  }

  protected onCancel(): void {
    this.router.navigate(['/responsibles']);
  }

  protected onCpfInput(): void {
    this.form.controls.cpf.setValue(this.formatCpf(this.form.controls.cpf.value), {
      emitEvent: false,
    });
  }

  protected onTelefoneInput(): void {
    this.form.controls.telefone.setValue(this.formatTelefone(this.form.controls.telefone.value), {
      emitEvent: false,
    });
  }

  protected onCepInput(): void {
    this.form.controls.cep.setValue(this.formatCep(this.form.controls.cep.value), {
      emitEvent: false,
    });
  }

  protected onUfInput(): void {
    const control = this.form.controls.uf;
    control.setValue(control.value.toUpperCase().slice(0, 2), { emitEvent: false });
  }

  protected buscarCep(): void {
    const cep = this.onlyDigits(this.form.controls.cep.value);
    if (cep.length !== 8) {
      this.form.controls.cep.setErrors({
        ...this.form.controls.cep.errors,
        cepInvalido: true,
      });
      this.form.controls.cep.markAsTouched();
      return;
    }

    this.buscandoCep.set(true);
    this.responsiblesService.consultarCep(cep).subscribe({
      next: (endereco) => {
        this.form.patchValue({
          cep: this.formatCep(endereco.cep),
          logradouro: endereco.logradouro ?? '',
          bairro: endereco.bairro ?? '',
          cidade: endereco.cidade ?? '',
          uf: endereco.uf ?? '',
          complemento: this.form.controls.complemento.value || endereco.complemento || '',
        });
        this.buscandoCep.set(false);
      },
      error: (error: unknown) => {
        this.buscandoCep.set(false);
        this.snackBar.open(getApiErrorMessage(error, 'Não foi possível consultar o CEP.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  private cpfValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return validarCPF(String(control.value)) ? null : { cpfInvalido: true };
    };
  }

  private telefoneValidator(): ValidatorFn {
    const phoneRegex = /^$|^\(\d{2}\)\s\d{4,5}-\d{4}$/;
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return phoneRegex.test(String(control.value)) ? null : { telefoneInvalido: true };
    };
  }

  private cepValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return this.onlyDigits(String(control.value)).length === 8 ? null : { cepInvalido: true };
    };
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private formatCpf(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 11);
    return digits
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  private formatTelefone(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 11);
    if (digits.length <= 2) return digits;
    if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
    if (digits.length <= 10)
      return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }

  private formatCep(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 8);
    if (digits.length <= 5) return digits;
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
  }

  private optional(value: string): string | undefined {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  }
}
