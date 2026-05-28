import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AcademicSeries, AcademicSeriesInput } from '../../models/academic.model';

export interface AcademicSeriesDialogData {
  serie?: AcademicSeries;
}

@Component({
  selector: 'app-academic-series-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './academic-series-dialog.component.html',
  styleUrls: ['./academic-series-dialog.component.scss'],
})
export class AcademicSeriesDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AcademicSeriesDialogComponent>);
  protected readonly data = inject<AcademicSeriesDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    nome: [this.data.serie?.nome ?? '', [Validators.required, Validators.maxLength(120)]],
    ordem: [this.data.serie?.ordem ?? 1, [Validators.required, Validators.min(0)]],
    nivelEnsino: [this.data.serie?.nivelEnsino ?? '', [Validators.maxLength(80)]],
  });

  protected get title(): string {
    return this.data.serie ? 'Editar série' : 'Nova série';
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: AcademicSeriesInput = {
      nome: raw.nome.trim(),
      ordem: raw.ordem,
      nivelEnsino: raw.nivelEnsino.trim() || undefined,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
