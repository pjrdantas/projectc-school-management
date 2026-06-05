import { NgFor } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Disciplina, DisciplinaInput } from '../../models/student-records.model';

export interface DisciplineDialogData {
  disciplina?: Disciplina;
}

@Component({
  selector: 'app-discipline-dialog',
  standalone: true,
  imports: [
    NgFor,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './discipline-dialog.component.html',
  styleUrls: ['./discipline-dialog.component.scss'],
})
export class DisciplineDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DisciplineDialogComponent>);
  protected readonly data = inject<DisciplineDialogData>(MAT_DIALOG_DATA);

  protected readonly statusOptions = [
    { value: 'ATIVA', label: 'Ativa' },
    { value: 'INATIVA', label: 'Inativa' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    cargaHoraria: this.fb.control<number | null>(null, [Validators.min(0)]),
    status: ['ATIVA', [Validators.required]],
  });

  protected get title(): string {
    return this.data.disciplina ? 'Editar disciplina' : 'Nova disciplina';
  }

  ngOnInit(): void {
    const disciplina = this.data.disciplina;
    this.form.patchValue({
      nome: disciplina?.nome ?? '',
      cargaHoraria: disciplina?.cargaHoraria ?? null,
      status: this.normalizeStatus(disciplina?.status),
    });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: DisciplinaInput = {
      nome: raw.nome.trim(),
      cargaHoraria: raw.cargaHoraria ?? undefined,
      status: this.normalizeStatus(raw.status),
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
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
}
