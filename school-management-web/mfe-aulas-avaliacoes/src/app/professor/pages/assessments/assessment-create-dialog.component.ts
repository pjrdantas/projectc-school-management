import { NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AssessmentInput } from '../../models/assessment.model';
import { ProfessorAllocation } from '../../models/teacher.model';

export interface AssessmentCreateDialogData {
  allocations: ProfessorAllocation[];
}

@Component({
  selector: 'app-assessment-create-dialog',
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
  templateUrl: './assessment-create-dialog.component.html',
  styleUrls: ['./assessment-create-dialog.component.scss'],
})
export class AssessmentCreateDialogComponent {
  private readonly data = inject<AssessmentCreateDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AssessmentCreateDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly allocations = [...this.data.allocations].sort((left, right) =>
    `${left.professorNome} ${left.turmaNome} ${left.disciplinaNome}`.localeCompare(
      `${right.professorNome} ${right.turmaNome} ${right.disciplinaNome}`,
      'pt-BR',
    ),
  );

  protected readonly assessmentTypes = [
    { value: 'PROVA', label: 'Prova' },
    { value: 'TRABALHO', label: 'Trabalho' },
    { value: 'ATIVIDADE', label: 'Atividade' },
    { value: 'SEMINARIO', label: 'Seminário' },
    { value: 'RECUPERACAO', label: 'Recuperação' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    professorTurmaDisciplinaId: ['', [Validators.required]],
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    descricao: [''],
    dataAplicacao: [''],
    valorMaximo: [10, [Validators.required, Validators.min(0.01)]],
    peso: [1, [Validators.required, Validators.min(0.01)]],
    tipoAvaliacao: ['PROVA', [Validators.required]],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: AssessmentInput = {
      professorTurmaDisciplinaId: raw.professorTurmaDisciplinaId,
      titulo: raw.titulo.trim(),
      descricao: raw.descricao.trim() || null,
      dataAplicacao: raw.dataAplicacao || null,
      valorMaximo: Number(raw.valorMaximo),
      peso: Number(raw.peso),
      tipoAvaliacao: raw.tipoAvaliacao,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
