import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  DashboardConfiguracao,
  DashboardConfiguracaoInput,
  DashboardPublico,
} from '../../models';

export interface DashboardConfigDialogData {
  dashboard?: DashboardConfiguracao;
  publicos: DashboardPublico[];
  publicoDashboardId?: string;
}

@Component({
  selector: 'app-dashboard-config-dialog',
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
  templateUrl: './dashboard-config-dialog.component.html',
  styleUrls: ['./dashboard-config-dialog.component.scss'],
})
export class DashboardConfigDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DashboardConfigDialogComponent>);
  protected readonly data = inject<DashboardConfigDialogData>(MAT_DIALOG_DATA);
  protected readonly statusOptions = [
    { value: 'true', label: 'Ativo' },
    { value: 'false', label: 'Inativo' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    publicoDashboardId: ['', [Validators.required]],
    codigo: ['', [Validators.required, Validators.maxLength(80)]],
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    descricao: [''],
    ativo: ['true', [Validators.required]],
  });

  protected get title(): string {
    return this.data.dashboard ? 'Editar dashboard' : 'Novo dashboard';
  }

  ngOnInit(): void {
    const dashboard = this.data.dashboard;
    this.form.patchValue({
      publicoDashboardId: dashboard?.publicoDashboardId ?? this.data.publicoDashboardId ?? '',
      codigo: dashboard?.codigo ?? '',
      nome: dashboard?.nome ?? '',
      descricao: dashboard?.descricao ?? '',
      ativo: String(dashboard?.ativo ?? true),
    });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: DashboardConfiguracaoInput = {
      publicoDashboardId: raw.publicoDashboardId,
      codigo: raw.codigo.trim().toUpperCase(),
      nome: raw.nome.trim(),
      descricao: raw.descricao.trim() || null,
      ativo: raw.ativo === 'true',
    };
    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
