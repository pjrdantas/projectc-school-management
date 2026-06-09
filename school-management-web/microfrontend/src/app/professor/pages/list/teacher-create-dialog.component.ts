import { NgFor, NgIf } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ProfessorFuncionarioElegivel, ProfessorInput } from '../../models/teacher.model';
import { TeachersService } from '../../services/teachers.service';

@Component({
  selector: 'app-teacher-create-dialog',
  standalone: true,
  imports: [
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './teacher-create-dialog.component.html',
  styleUrls: ['./teacher-create-dialog.component.scss'],
})
export class TeacherCreateDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogRef = inject(MatDialogRef<TeacherCreateDialogComponent>);
  private readonly teachersService = inject(TeachersService);

  protected readonly funcionariosElegiveis = signal<ProfessorFuncionarioElegivel[]>([]);
  protected readonly isLoadingFuncionarios = signal(true);

  protected readonly statusOptions = [
    { value: 'true', label: 'Ativo' },
    { value: 'false', label: 'Inativo' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    funcionarioId: ['', [Validators.required]],
    registroProfissional: ['', [Validators.maxLength(80)]],
    formacao: ['', [Validators.maxLength(150)]],
    ativo: ['true', [Validators.required]],
  });

  constructor() {
    this.teachersService
      .listarFuncionariosElegiveis()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: funcionarios => {
          this.funcionariosElegiveis.set(funcionarios);
          this.isLoadingFuncionarios.set(false);
        },
        error: () => {
          this.funcionariosElegiveis.set([]);
          this.isLoadingFuncionarios.set(false);
        },
      });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: ProfessorInput = {
      funcionarioId: raw.funcionarioId.trim(),
      registroProfissional: raw.registroProfissional.trim() || null,
      formacao: raw.formacao.trim() || null,
      ativo: raw.ativo === 'true',
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
