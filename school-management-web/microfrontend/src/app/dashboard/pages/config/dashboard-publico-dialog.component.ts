import { NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { DashboardPublico, DashboardPublicoInput } from '../../models/dashboard-config.model';

export interface DashboardPublicoDialogData {
  publico?: DashboardPublico;
}

@Component({
  selector: 'app-dashboard-publico-dialog',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './dashboard-publico-dialog.component.html',
  styleUrls: ['./dashboard-publico-dialog.component.scss'],
})
export class DashboardPublicoDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DashboardPublicoDialogComponent>);
  protected readonly data = inject<DashboardPublicoDialogData>(MAT_DIALOG_DATA);

  protected readonly form = this.fb.nonNullable.group({
    codigo: ['', [Validators.required, Validators.maxLength(40)]],
    descricao: ['', [Validators.required, Validators.maxLength(120)]],
  });

  protected get title(): string {
    return this.data.publico ? 'Editar público' : 'Novo público';
  }

  ngOnInit(): void {
    const publico = this.data.publico;
    this.form.patchValue({
      codigo: publico?.codigo ?? '',
      descricao: publico?.descricao ?? '',
    });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: DashboardPublicoInput = {
      codigo: raw.codigo.trim().toUpperCase(),
      descricao: raw.descricao.trim(),
    };
    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
