import { NgFor, NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiErrorResponse, Enrollment } from '../../models/enrollment.model';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-enrollment-search',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatSelectModule,
  ],
  templateUrl: './enrollment-search.component.html',
  styleUrls: ['./enrollment-search.component.scss'],
})
export class EnrollmentSearchComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly enrollmentService = inject(EnrollmentService);

  protected readonly form = this.fb.nonNullable.group({
    alunoId: [''],
    turmaId: [''],
    periodoLetivoId: [''],
    status: [''],
  });

  protected readonly statuses = ['ATIVA', 'CANCELADA', 'TRANCADA', 'CONCLUIDA'];

  protected readonly isLoading = signal(false);
  protected readonly hasSearched = signal(false);
  protected readonly result = signal<Enrollment[]>([]);

  protected onSearch(): void {
    this.isLoading.set(true);
    this.hasSearched.set(true);

    const raw = this.form.getRawValue();
    const filter = {
      alunoId: raw.alunoId.trim() || undefined,
      turmaId: raw.turmaId.trim() || undefined,
      periodoLetivoId: raw.periodoLetivoId.trim() || undefined,
      status: raw.status.trim() || undefined,
    };

    this.enrollmentService.search(filter).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.result.set(response);
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.result.set([]);
        this.snackBar.open(error.error?.message ?? 'Erro ao consultar matrículas.', 'Fechar', {
          duration: 4500,
        });
      },
    });
  }

  protected clear(): void {
    this.form.reset({ alunoId: '', turmaId: '', periodoLetivoId: '', status: '' });
    this.result.set([]);
    this.hasSearched.set(false);
  }
}
