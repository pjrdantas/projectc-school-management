import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiErrorResponse } from '../../models/enrollment.model';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-enrollment-new',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './enrollment-new.component.html',
  styleUrls: ['./enrollment-new.component.scss'],
})
export class EnrollmentNewComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly enrollmentService = inject(EnrollmentService);

  protected readonly isLoading = signal(false);
  protected readonly createdStatus = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    alunoId: ['', [Validators.required]],
    turmaId: ['', [Validators.required]],
    periodoLetivoId: ['', [Validators.required]],
  });

  protected onCreate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.createdStatus.set(null);

    this.enrollmentService.create(this.form.getRawValue()).subscribe({
      next: response => {
        this.isLoading.set(false);
        this.createdStatus.set(response.status);
        this.snackBar.open('Matrícula criada com sucesso.', 'Fechar', { duration: 3000 });
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(error.error?.message ?? 'Erro ao criar matrícula.', 'Fechar', {
          duration: 4500,
        });
      },
    });
  }
}
