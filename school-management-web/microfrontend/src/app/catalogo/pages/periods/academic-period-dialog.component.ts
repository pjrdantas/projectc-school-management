import { NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AcademicPeriod, AcademicPeriodInput } from '../../models/academic.model';

export interface AcademicPeriodDialogData {
  period?: AcademicPeriod;
}

@Component({
  selector: 'app-academic-period-dialog',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './academic-period-dialog.component.html',
  styleUrls: ['./academic-period-dialog.component.scss'],
})
export class AcademicPeriodDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AcademicPeriodDialogComponent>);
  protected readonly data = inject<AcademicPeriodDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    nome: [this.data.period?.nome ?? '', [Validators.required, Validators.maxLength(30)]],
    dataInicio: [this.isoToBr(this.data.period?.dataInicio ?? ''), [Validators.required, this.dataBrValidator()]],
    dataFim: [this.isoToBr(this.data.period?.dataFim ?? ''), [Validators.required, this.dataBrValidator()]],
  });

  protected get title(): string {
    return this.data.period ? 'Editar período letivo' : 'Novo período letivo';
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: AcademicPeriodInput = {
      nome: raw.nome.trim(),
      dataInicio: this.brToIso(raw.dataInicio),
      dataFim: this.brToIso(raw.dataFim),
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected onDateInput(controlName: 'dataInicio' | 'dataFim'): void {
    const control = this.form.controls[controlName];
    control.setValue(this.formatDateBr(control.value), { emitEvent: false });
  }

  private dataBrValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }

      const value = String(control.value);
      const validPattern = /^\d{2}\/\d{2}\/\d{4}$/.test(value);
      if (!validPattern) {
        return { dataInvalida: true };
      }

      const [dd, mm, yyyy] = value.split('/').map(Number);
      const date = new Date(yyyy, mm - 1, dd);
      const validDate =
        date.getFullYear() === yyyy && date.getMonth() === mm - 1 && date.getDate() === dd;

      return validDate ? null : { dataInvalida: true };
    };
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private formatDateBr(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 8);
    if (digits.length <= 2) return digits;
    if (digits.length <= 4) return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
  }

  private brToIso(value: string): string {
    const [dd, mm, yyyy] = value.split('/');
    return `${yyyy}-${mm}-${dd}`;
  }

  private isoToBr(value: string): string {
    if (!value) return '';
    if (value.includes('/')) return value;

    const [yyyy, mm, dd] = value.split('-');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy}` : value;
  }
}
