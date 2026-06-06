import { NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ProfessorAllocation } from '../../models/teacher.model';
import { LessonInput } from '../../models/lesson.model';

export interface LessonCreateDialogData {
  allocations: ProfessorAllocation[];
}

@Component({
  selector: 'app-lesson-create-dialog',
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
  templateUrl: './lesson-create-dialog.component.html',
  styleUrls: ['./lesson-create-dialog.component.scss'],
})
export class LessonCreateDialogComponent {
  private readonly data = inject<LessonCreateDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<LessonCreateDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly allocations = [...this.data.allocations].sort((left, right) =>
    `${left.professorNome} ${left.turmaNome} ${left.disciplinaNome}`.localeCompare(
      `${right.professorNome} ${right.turmaNome} ${right.disciplinaNome}`,
      'pt-BR',
    ),
  );

  protected readonly statusOptions = [
    { value: 'true', label: 'Realizada' },
    { value: 'false', label: 'Planejada' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    professorTurmaDisciplinaId: ['', [Validators.required]],
    dataAula: ['', [Validators.required]],
    horarioInicio: [''],
    horarioFim: [''],
    conteudoMinistrado: [''],
    observacao: ['', [Validators.maxLength(4000)]],
    realizada: ['false', [Validators.required]],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: LessonInput = {
      professorTurmaDisciplinaId: raw.professorTurmaDisciplinaId,
      dataAula: raw.dataAula,
      horarioInicio: raw.horarioInicio || null,
      horarioFim: raw.horarioFim || null,
      conteudoMinistrado: raw.conteudoMinistrado.trim() || null,
      observacao: raw.observacao.trim() || null,
      realizada: raw.realizada === 'true',
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
