import { NgFor } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BimonthlyPlanningAssessmentInput } from '../../models';

@Component({
  selector: 'app-planning-assessment-dialog',
  standalone: true,
  imports: [NgFor, ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './planning-assessment-dialog.component.html',
  styleUrls: ['./planning-assessment-dialog.component.scss'],
})
export class PlanningAssessmentDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<PlanningAssessmentDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly typeOptions = [
    { value: 'PROVA', label: 'Prova' },
    { value: 'TRABALHO', label: 'Trabalho' },
    { value: 'ATIVIDADE', label: 'Atividade' },
    { value: 'SEMINARIO', label: 'Seminário' },
    { value: 'RECUPERACAO', label: 'Recuperação' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(180)]],
    descricao: [''],
    dataPrevista: [''],
    peso: [1, [Validators.required, Validators.min(0.01)]],
    valorMaximo: [10, [Validators.min(0.01)]],
    tipoAvaliacao: ['PROVA', [Validators.required]],
    conteudoCobrado: [''],
    orientacaoAplicacao: [''],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: BimonthlyPlanningAssessmentInput = {
      titulo: raw.titulo.trim(),
      descricao: raw.descricao.trim() || null,
      dataPrevista: raw.dataPrevista || null,
      peso: Number(raw.peso),
      valorMaximo: raw.valorMaximo ? Number(raw.valorMaximo) : null,
      tipoAvaliacao: raw.tipoAvaliacao,
      conteudoCobrado: raw.conteudoCobrado.trim() || null,
      orientacaoAplicacao: raw.orientacaoAplicacao.trim() || null,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
