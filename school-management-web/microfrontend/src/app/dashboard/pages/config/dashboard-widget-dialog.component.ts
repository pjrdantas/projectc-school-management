import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { DashboardWidget, DashboardWidgetInput } from '../../models/dashboard-config.model';
import { DashboardPublicoCodigo } from '../../models/dashboard.model';
import {
  DASHBOARD_WIDGET_CATALOG,
  DashboardWidgetCatalogItem,
} from '../../models/dashboard-widget-catalog';

export interface DashboardWidgetDialogData {
  widget?: DashboardWidget;
  dashboardId: string;
  publicoCodigo?: string | null;
}

@Component({
  selector: 'app-dashboard-widget-dialog',
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
  templateUrl: './dashboard-widget-dialog.component.html',
  styleUrls: ['./dashboard-widget-dialog.component.scss'],
})
export class DashboardWidgetDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DashboardWidgetDialogComponent>);
  protected readonly data = inject<DashboardWidgetDialogData>(MAT_DIALOG_DATA);
  protected readonly statusOptions = [
    { value: 'true', label: 'Ativo' },
    { value: 'false', label: 'Inativo' },
  ];
  protected readonly typeOptions = ['CARD', 'LISTA', 'GRAFICO', 'ALERTA', 'TABELA'];
  protected readonly officialWidgetOptions = this.getOfficialWidgetOptions();

  protected readonly form = this.fb.nonNullable.group({
    modeloOficial: [''],
    codigo: ['', [Validators.required, Validators.maxLength(80)]],
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    descricao: [''],
    tipoWidget: ['CARD', [Validators.required, Validators.maxLength(40)]],
    ordem: [0, [Validators.required, Validators.min(0)]],
    queryReferencia: ['', [Validators.maxLength(150)]],
    ativo: ['true', [Validators.required]],
  });

  protected get title(): string {
    return this.data.widget ? 'Editar widget' : 'Novo widget';
  }

  ngOnInit(): void {
    const widget = this.data.widget;
    this.form.patchValue({
      codigo: widget?.codigo ?? '',
      titulo: widget?.titulo ?? '',
      descricao: widget?.descricao ?? '',
      tipoWidget: widget?.tipoWidget ?? 'CARD',
      ordem: widget?.ordem ?? 0,
      queryReferencia: widget?.queryReferencia ?? '',
      ativo: String(widget?.ativo ?? true),
    });

    const officialWidget = this.officialWidgetOptions.find(item => item.codigo === widget?.codigo);
    if (officialWidget) {
      this.form.controls.modeloOficial.setValue(officialWidget.codigo);
    }
  }

  protected applyOfficialWidget(codigo: string): void {
    const option = this.officialWidgetOptions.find(item => item.codigo === codigo);
    if (!option) {
      return;
    }

    this.form.patchValue({
      modeloOficial: option.codigo,
      codigo: option.codigo,
      titulo: option.titulo,
      descricao: option.descricao,
      tipoWidget: option.tipoWidget,
      ordem: option.ordem,
      queryReferencia: option.queryReferencia,
      ativo: 'true',
    });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: DashboardWidgetInput = {
      dashboardId: this.data.dashboardId,
      codigo: raw.codigo.trim().toUpperCase(),
      titulo: raw.titulo.trim(),
      descricao: raw.descricao.trim() || null,
      tipoWidget: raw.tipoWidget.trim().toUpperCase(),
      ordem: raw.ordem ?? 0,
      queryReferencia: raw.queryReferencia.trim() || null,
      ativo: raw.ativo === 'true',
    };
    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private getOfficialWidgetOptions(): DashboardWidgetCatalogItem[] {
    const publicoCodigo = this.normalizePublicoCodigo(this.data.publicoCodigo);

    return DASHBOARD_WIDGET_CATALOG
      .filter(item => item.publicos.includes('GERAL') || item.publicos.includes(publicoCodigo))
      .sort((left, right) => left.ordem - right.ordem || left.titulo.localeCompare(right.titulo, 'pt-BR'));
  }

  private normalizePublicoCodigo(value?: string | null): DashboardPublicoCodigo {
    const normalized = value?.trim().toUpperCase();
    if (normalized === 'SECRETARIA' || normalized === 'DIRETOR' || normalized === 'PROFESSOR') {
      return normalized;
    }

    return 'ACADEMICO';
  }
}
