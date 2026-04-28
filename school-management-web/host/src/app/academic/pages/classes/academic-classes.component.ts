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
  selector: 'app-academic-classes',
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
  templateUrl: './academic-classes.component.html',
  styleUrls: ['./academic-classes.component.scss'],
})
export class AcademicClassesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);

  protected readonly classes$ = this.academicService.classes$;
  protected readonly classes = toSignal(this.classes$, {
    initialValue: this.academicService.listClasses(),
  });

  protected readonly form = this.fb.nonNullable.group({
    codigo: ['', [Validators.required, Validators.maxLength(20)]],
    nome: ['', [Validators.required, Validators.maxLength(80)]],
    capacidade: [30, [Validators.required, Validators.min(1)]],
    periodoLetivoId: ['', [Validators.required]],
  });

  protected readonly searchForm = this.fb.nonNullable.group({
    id: ['', [Validators.required]],
  });

  protected readonly isLoading = signal(false);
  protected readonly total = computed(() => this.classes().length);

  protected onCreate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue();
    this.isLoading.set(true);

    this.academicService.createClass(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.form.reset({ codigo: '', nome: '', capacidade: 30, periodoLetivoId: '' });
        this.snackBar.open('Turma cadastrada com sucesso.', 'Fechar', { duration: 3000 });
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(error.error?.message ?? 'Não foi possível cadastrar turma.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected onSearchById(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.academicService.fetchClassById(this.searchForm.controls.id.value.trim()).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.snackBar.open('Turma localizada e atualizada na lista.', 'Fechar', {
          duration: 2500,
        });
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(error.error?.message ?? 'Turma não encontrada.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }
}
