import { NgFor } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  AcademicClass,
  AcademicClassInput,
  AcademicPeriod,
  AcademicSeries,
  AcademicShift,
} from '../../models/academic.model';

export interface AcademicClassDialogData {
  turma?: AcademicClass;
  periods: AcademicPeriod[];
  series: AcademicSeries[];
  shifts: AcademicShift[];
}

@Component({
  selector: 'app-academic-class-dialog',
  standalone: true,
  imports: [
    NgFor,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './academic-class-dialog.component.html',
  styleUrls: ['./academic-class-dialog.component.scss'],
})
export class AcademicClassDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AcademicClassDialogComponent>);
  protected readonly data = inject<AcademicClassDialogData>(MAT_DIALOG_DATA);

  protected readonly selectedPeriodId = signal<string | null>(
    this.data.turma?.periodoLetivoId ?? null,
  );
  protected readonly periodSearchTerm = signal(this.getPeriodName(this.data.turma?.periodoLetivoId));

  protected readonly form = this.fb.nonNullable.group({
    nome: [this.data.turma?.nome ?? '', [Validators.required, Validators.maxLength(80)]],
    capacidade: [this.data.turma?.capacidade ?? 30, [Validators.required, Validators.min(1)]],
    periodoLetivoNome: [this.getPeriodName(this.data.turma?.periodoLetivoId), [Validators.required]],
    serieId: [this.data.turma?.serieId ?? '', [Validators.required]],
    turno: [this.data.turma?.turno ?? '', [Validators.required]],
    status: [this.data.turma?.status ?? 'ATIVA', [Validators.required]],
  });

  protected readonly filteredPeriods = computed(() => {
    const term = this.periodSearchTerm().toLowerCase().trim();
    return this.data.periods
      .filter((period) => period.nome.toLowerCase().includes(term))
      .slice(0, 10);
  });

  protected get title(): string {
    return this.data.turma ? 'Editar turma' : 'Nova turma';
  }

  protected onPeriodInput(value: string): void {
    this.periodSearchTerm.set(value);
    this.selectedPeriodId.set(null);
    this.form.controls.periodoLetivoNome.setValue(value, { emitEvent: false });
  }

  protected onPeriodOptionSelected(id: string): void {
    const period = this.data.periods.find((item) => item.id === id);
    this.selectedPeriodId.set(id);
    this.periodSearchTerm.set(period?.nome ?? '');
    this.form.controls.periodoLetivoNome.setValue(period?.nome ?? '', { emitEvent: false });
  }

  protected save(): void {
    if (this.form.invalid || !this.selectedPeriodId()) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: AcademicClassInput = {
      codigo: this.buildClassCode(raw.nome, raw.serieId, raw.turno),
      nome: raw.nome.trim(),
      capacidade: raw.capacidade,
      periodoLetivoId: this.selectedPeriodId()!,
      serieId: raw.serieId,
      turno: raw.turno,
      status: raw.status,
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private buildClassCode(nome: string, serieId: string, turno: string): string {
    const serie = this.data.series.find((item) => item.id === serieId)?.ordem.toString() ?? '';
    return [serie, turno, nome]
      .filter(Boolean)
      .map((part) => part.trim().toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, ''))
      .map((part) => part.replace(/[^A-Z0-9]+/g, '-').replace(/^-+|-+$/g, ''))
      .filter(Boolean)
      .join('-')
      .slice(0, 20);
  }

  private getPeriodName(periodId?: string): string {
    if (!periodId) {
      return '';
    }
    return this.data.periods.find((period) => period.id === periodId)?.nome ?? '';
  }
}
