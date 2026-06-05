import { NgFor } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
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
  ],
  templateUrl: './academic-class-dialog.component.html',
  styleUrls: ['./academic-class-dialog.component.scss'],
})
export class AcademicClassDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AcademicClassDialogComponent>);
  protected readonly data = inject<AcademicClassDialogData>(MAT_DIALOG_DATA);
  protected readonly periodOptions = this.resolvePeriodOptions();
  protected readonly seriesOptions = this.resolveSeriesOptions();
  protected readonly shiftOptions = this.resolveShiftOptions();
  protected readonly statusOptions = [
    { value: 'ATIVA', label: 'Ativa' },
    { value: 'INATIVA', label: 'Inativa' },
  ];

  protected readonly selectedPeriodId = signal<string | null>(
    this.data.turma?.periodoLetivoId ?? null,
  );
  protected readonly periodSearchTerm = signal(this.getPeriodName(this.data.turma?.periodoLetivoId));

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(80)]],
    capacidade: [30, [Validators.required, Validators.min(1)]],
    periodoLetivoNome: ['', [Validators.required]],
    serieId: ['', [Validators.required]],
    turno: ['', [Validators.required]],
    status: ['ATIVA', [Validators.required]],
  });

  protected readonly filteredPeriods = computed(() => {
    const term = this.periodSearchTerm().toLowerCase().trim();
    return this.periodOptions
      .filter((period) => period.nome.toLowerCase().includes(term))
      .slice(0, 10);
  });

  protected get title(): string {
    return this.data.turma ? 'Editar turma' : 'Nova turma';
  }

  ngOnInit(): void {
    const turma = this.data.turma;
    const periodName = this.getPeriodName(turma?.periodoLetivoId);
    const periodId = turma?.periodoLetivoId ?? null;

    this.selectedPeriodId.set(periodId);
    this.periodSearchTerm.set(periodName);
    this.form.patchValue({
      nome: turma?.nome ?? '',
      capacidade: turma?.capacidade ?? 30,
      periodoLetivoNome: periodName,
      serieId: this.resolveSeriesId(turma),
      turno: this.resolveShiftCode(turma?.turno),
      status: this.normalizeStatus(turma?.status),
    });
  }

  protected onPeriodInput(value: string): void {
    this.periodSearchTerm.set(value);
    this.selectedPeriodId.set(null);
    this.form.controls.periodoLetivoNome.setValue(value, { emitEvent: false });
  }

  protected onPeriodOptionSelected(id: string): void {
    const period = this.periodOptions.find((item) => item.id === id);
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
      status: this.normalizeStatus(raw.status),
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private buildClassCode(nome: string, serieId: string, turno: string): string {
    const serie = this.seriesOptions.find((item) => item.id === serieId)?.ordem.toString() ?? '';
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
    return this.periodOptions.find((period) => period.id === periodId)?.nome ?? periodId;
  }

  private resolvePeriodOptions(): AcademicPeriod[] {
    const periods = [...(this.data.periods ?? [])];
    const periodId = this.data.turma?.periodoLetivoId;
    if (periodId && !periods.some((period) => period.id === periodId)) {
      periods.push({
        id: periodId,
        nome: periodId,
        ano: 0,
        dataInicio: '',
        dataFim: '',
        ativo: true,
        createdAt: '',
      });
    }
    return periods.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  }

  private resolveSeriesOptions(): AcademicSeries[] {
    const series = [...(this.data.series ?? [])];
    const turma = this.data.turma;
    if (turma?.serieId && !series.some((serie) => serie.id === turma.serieId)) {
      series.push({
        id: turma.serieId,
        nome: turma.serieNome || turma.serieId,
        ordem: 0,
        nivelEnsino: '',
        createdAt: '',
      });
    }
    return series.sort((a, b) => a.ordem - b.ordem || a.nome.localeCompare(b.nome, 'pt-BR'));
  }

  private resolveShiftOptions(): AcademicShift[] {
    const shifts = [...(this.data.shifts ?? [])];
    const turno = this.data.turma?.turno;
    if (turno && !shifts.some((shift) => this.normalizeText(shift.codigo) === this.normalizeText(turno))) {
      shifts.push({
        id: turno,
        codigo: turno,
        descricao: turno,
      });
    }
    return shifts.sort((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR'));
  }

  private resolveSeriesId(turma?: AcademicClass): string {
    if (!turma) {
      return '';
    }

    if (this.seriesOptions.some((serie) => serie.id === turma.serieId)) {
      return turma.serieId;
    }

    const normalizedSerieName = this.normalizeText(turma.serieNome);
    return this.seriesOptions.find((serie) => this.normalizeText(serie.nome) === normalizedSerieName)?.id ?? '';
  }

  private resolveShiftCode(turno?: string): string {
    if (!turno) {
      return '';
    }

    const normalizedShift = this.normalizeText(turno);
    return this.shiftOptions.find((option) =>
      this.normalizeText(option.codigo) === normalizedShift ||
      this.normalizeText(option.descricao) === normalizedShift
    )?.codigo ?? turno;
  }

  private normalizeStatus(status?: string): string {
    const normalizedStatus = (status || 'ATIVA').trim().toUpperCase();
    if (normalizedStatus === 'ATIVO' || normalizedStatus === 'TRUE') {
      return 'ATIVA';
    }
    if (normalizedStatus === 'INATIVO' || normalizedStatus === 'FALSE') {
      return 'INATIVA';
    }
    return this.statusOptions.some((option) => option.value === normalizedStatus) ? normalizedStatus : 'ATIVA';
  }

  private normalizeText(value?: string): string {
    return (value ?? '')
      .trim()
      .toUpperCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}
