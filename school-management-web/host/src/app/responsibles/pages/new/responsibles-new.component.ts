import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
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

  protected readonly form = this.fb.nonNullable.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: ['', [Validators.required, this.cpfValidator()]],
    email: ['', [Validators.email]],
    telefone: ['', [this.telefoneValidator()]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.responsiblesService.fetchByIdFromApi(id).subscribe({
      next: responsible => {
        this.responsibleId.set(id);
        this.form.patchValue({
          nomeCompleto: responsible.nomeCompleto,
          cpf: this.formatCpf(responsible.cpf),
          email: responsible.email ?? '',
          telefone: responsible.telefone ?? '',
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
      email: raw.email?.trim() || undefined,
      telefone: raw.telefone?.trim() || undefined,
    };

    const id = this.responsibleId();
    if (id) {
      this.responsiblesService.updateOnApi(id, payload).subscribe({
        next: () => {
          this.snackBar.open('Responsável atualizado com sucesso.', 'Fechar', { duration: 3000 });
          this.router.navigate(['/responsibles']);
        },
        error: () => this.snackBar.open('Erro ao atualizar responsável.', 'Fechar', { duration: 4000 }),
      });
      return;
    }

    this.responsiblesService.createOnApi(payload).subscribe({
      next: () => {
        this.snackBar.open('Responsável cadastrado com sucesso.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/responsibles']);
      },
      error: () => this.snackBar.open('Erro ao cadastrar responsável.', 'Fechar', { duration: 4000 }),
    });
  }

  protected onCancel(): void {
    this.router.navigate(['/responsibles']);
  }

  protected onCpfInput(): void {
    this.form.controls.cpf.setValue(this.formatCpf(this.form.controls.cpf.value), { emitEvent: false });
  }

  protected onTelefoneInput(): void {
    this.form.controls.telefone.setValue(this.formatTelefone(this.form.controls.telefone.value), { emitEvent: false });
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
    if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }
}
