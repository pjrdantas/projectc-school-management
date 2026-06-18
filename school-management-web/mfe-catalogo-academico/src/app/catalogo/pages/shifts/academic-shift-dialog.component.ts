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
import { AcademicShift, AcademicShiftInput } from '../../models/academic.model';

export interface AcademicShiftDialogData {
  turno?: AcademicShift;
}

@Component({
  selector: 'app-academic-shift-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './academic-shift-dialog.component.html',
  styleUrls: ['./academic-shift-dialog.component.scss'],
})
export class AcademicShiftDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AcademicShiftDialogComponent>);
  protected readonly data = inject<AcademicShiftDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    codigo: [this.data.turno?.codigo ?? '', [Validators.required, Validators.maxLength(40)]],
    descricao: [this.data.turno?.descricao ?? '', [Validators.required, Validators.maxLength(120)]],
  });

  protected get title(): string {
    return this.data.turno ? 'Editar turno' : 'Novo turno';
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: AcademicShiftInput = {
      codigo: raw.codigo.trim(),
      descricao: raw.descricao.trim(),
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
