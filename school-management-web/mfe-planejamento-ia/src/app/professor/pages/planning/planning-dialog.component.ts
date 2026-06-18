import { NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BimonthlyPlanning, BimonthlyPlanningInput, ProfessorAllocation } from '../../models';

export interface PlanningDialogData {
  allocations: ProfessorAllocation[];
  planning?: BimonthlyPlanning;
}

@Component({
  selector: 'app-planning-dialog',
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
  templateUrl: './planning-dialog.component.html',
  styleUrls: ['./planning-dialog.component.scss'],
})
export class PlanningDialogComponent {
  private readonly data = inject<PlanningDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<PlanningDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly isEdit = Boolean(this.data.planning);
  protected readonly allocations = [...this.data.allocations].sort((left, right) =>
    `${left.professorNome} ${left.turmaNome} ${left.disciplinaNome}`.localeCompare(
      `${right.professorNome} ${right.turmaNome} ${right.disciplinaNome}`,
      'pt-BR',
    ),
  );

  protected readonly form = this.fb.nonNullable.group({
    professorTurmaDisciplinaId: [this.data.planning?.professorTurmaDisciplinaId ?? '', [Validators.required]],
    titulo: [this.data.planning?.titulo ?? '', [Validators.required, Validators.maxLength(180)]],
    temaPrincipal: [this.data.planning?.temaPrincipal ?? '', [Validators.required, Validators.maxLength(180)]],
    descricaoInicial: [this.data.planning?.descricaoInicial ?? '', [Validators.required]],
    objetivoGeral: [this.data.planning?.objetivoGeral ?? ''],
    observacaoProfessor: [this.data.planning?.observacaoProfessor ?? ''],
    conteudoFinalAprovado: [this.data.planning?.conteudoFinalAprovado ?? ''],
    reutilizavel: [String(this.data.planning?.reutilizavel ?? false)],
    criadoComAuxilioIA: [String(this.data.planning?.criadoComAuxilioIA ?? false)],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: BimonthlyPlanningInput = {
      professorTurmaDisciplinaId: raw.professorTurmaDisciplinaId,
      periodoAvaliativoId: this.data.planning?.periodoAvaliativoId ?? null,
      titulo: raw.titulo.trim(),
      temaPrincipal: raw.temaPrincipal.trim(),
      descricaoInicial: raw.descricaoInicial.trim(),
      objetivoGeral: raw.objetivoGeral.trim() || null,
      observacaoProfessor: raw.observacaoProfessor.trim() || null,
      conteudoFinalAprovado: raw.conteudoFinalAprovado.trim() || null,
      reutilizavel: raw.reutilizavel === 'true',
      criadoComAuxilioIA: raw.criadoComAuxilioIA === 'true',
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
