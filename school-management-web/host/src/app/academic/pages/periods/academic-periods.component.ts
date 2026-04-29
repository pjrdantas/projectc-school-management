import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiErrorResponse } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';

@Component({
  selector: 'app-academic-periods',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    AsyncPipe,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './academic-periods.component.html',
  styleUrls: ['./academic-periods.component.scss'],
})
export class AcademicPeriodsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);

  protected readonly periods$ = this.academicService.periods$;
  protected readonly periods = toSignal(this.periods$, {
    initialValue: this.academicService.listPeriods(),
  });

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(30)]],
    dataInicio: ['', [Validators.required]],
    dataFim: ['', [Validators.required]],
  });

  protected readonly searchForm = this.fb.nonNullable.group({
    id: ['', [Validators.required]],
  });

  protected readonly isLoading = signal(false);
  protected readonly total = computed(() => this.periods().length);

  protected onCreate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue();
    this.isLoading.set(true);

    this.academicService.createPeriod(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.form.reset();
        this.snackBar.open('Período letivo cadastrado com sucesso.', 'Fechar', { duration: 3000 });
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(
          error.error?.message ?? 'Não foi possível cadastrar período letivo.',
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected onSearchById(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.academicService.fetchPeriodById(this.searchForm.controls.id.value.trim()).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackBar.open('Período letivo localizado e atualizado na lista.', 'Fechar', {
          duration: 2500,
        });
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(error.error?.message ?? 'Período letivo não encontrado.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }
}
