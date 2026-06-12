import { NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PlanningAiVersionInput } from '../../models/planning.model';

export interface PlanningAiVersionDialogData {
  conteudoAtual?: string | null;
}

@Component({
  selector: 'app-planning-ai-version-dialog',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './planning-ai-version-dialog.component.html',
  styleUrls: ['./planning-ai-version-dialog.component.scss'],
})
export class PlanningAiVersionDialogComponent {
  private readonly data = inject<PlanningAiVersionDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<PlanningAiVersionDialogComponent>);
  private readonly fb = inject(FormBuilder);

  protected readonly form = this.fb.nonNullable.group({
    conteudo: [this.data.conteudoAtual ?? '', [Validators.required]],
    motivoAlteracao: [''],
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: PlanningAiVersionInput = {
      conteudo: raw.conteudo.trim(),
      motivoAlteracao: raw.motivoAlteracao.trim() || null,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
