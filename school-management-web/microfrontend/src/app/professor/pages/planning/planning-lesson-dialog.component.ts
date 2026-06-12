import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BimonthlyPlanningLessonInput } from '../../models/planning.model';

@Component({
  selector: 'app-planning-lesson-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './planning-lesson-dialog.component.html',
  styleUrls: ['./planning-lesson-dialog.component.scss'],
})
export class PlanningLessonDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<PlanningLessonDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly form = this.fb.nonNullable.group({
    numeroAula: [1, [Validators.required, Validators.min(1)]],
    temaAula: ['', [Validators.required, Validators.maxLength(180)]],
    objetivoAula: [''],
    conteudoPrevisto: [''],
    metodologia: [''],
    recursos: [''],
    atividadePrevista: [''],
    observacao: [''],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: BimonthlyPlanningLessonInput = {
      numeroAula: Number(raw.numeroAula),
      temaAula: raw.temaAula.trim(),
      objetivoAula: raw.objetivoAula.trim() || null,
      conteudoPrevisto: raw.conteudoPrevisto.trim() || null,
      metodologia: raw.metodologia.trim() || null,
      recursos: raw.recursos.trim() || null,
      atividadePrevista: raw.atividadePrevista.trim() || null,
      observacao: raw.observacao.trim() || null,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
