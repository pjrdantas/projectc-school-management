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
import { StudentInput } from '../../models/student.model';
import { StudentsService } from '../../services/students.service';
import { validarCPF } from '../../utils/cpf-validator';

@Component({
  selector: 'app-students-new',
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

  protected readonly studentId = signal<string | null>(null);
  protected readonly isEditing = computed(() => !!this.studentId());

  protected readonly alunoForm = this.fb.nonNullable.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: ['', [Validators.required, this.cpfValidator()]],
    dataNascimento: ['', [Validators.required, this.dataBrValidator()]],
    email: ['', [Validators.required, this.emailConsistenteValidator()]],
    telefone: ['', [this.telefoneValidator()]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    const student = this.studentsService.getById(id);
    if (!student) {
      this.snackBar.open('Aluno não encontrado.', 'Fechar', { duration: 3000 });
      this.router.navigate(['/students']);
      return;
    }

    this.studentId.set(id);
    this.alunoForm.patchValue({
      nomeCompleto: student.nomeCompleto,
      cpf: this.formatCpf(student.cpf),
      dataNascimento: this.isoToBr(student.dataNascimento),
      email: student.email,
      telefone: student.telefone ?? '',
    });
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
    if (id) {
      this.studentsService.update(id, payload);
      this.snackBar.open('Aluno atualizado com sucesso.', 'Fechar', { duration: 3000 });
    } else {
      this.studentsService.create(payload);
      this.snackBar.open('Aluno cadastrado com sucesso.', 'Fechar', { duration: 3000 });
    }

    this.router.navigate(['/students']);
  }

  protected onCancel(): void {
    this.router.navigate(['/students']);
  }

  protected onCpfInput(): void {
    const cpf = this.alunoForm.controls.cpf.value;
    this.alunoForm.controls.cpf.setValue(this.formatCpf(cpf), { emitEvent: false });
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
    const phoneRegex = /^\(\d{2}\)\s\d{4,5}-\d{4}$/;
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
