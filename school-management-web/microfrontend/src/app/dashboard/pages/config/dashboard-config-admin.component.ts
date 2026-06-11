import { NgClass, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { getApiErrorMessage } from '../../../core/http/api-error';
import {
  DashboardConfiguracao,
  DashboardConfiguracaoInput,
  DashboardPublico,
  DashboardPublicoInput,
  DashboardWidget,
  DashboardWidgetInput,
} from '../../models/dashboard-config.model';
import { DashboardPublicoCodigo } from '../../models/dashboard.model';
import {
  DASHBOARD_WIDGET_CATALOG,
  DashboardWidgetCatalogItem,
} from '../../models/dashboard-widget-catalog';
import { DashboardConfigService } from '../../services/dashboard-config.service';
import {
  DashboardConfigDialogComponent,
  DashboardConfigDialogData,
} from './dashboard-config-dialog.component';
import {
  DashboardPublicoDialogComponent,
  DashboardPublicoDialogData,
} from './dashboard-publico-dialog.component';
import {
  DashboardWidgetDialogComponent,
  DashboardWidgetDialogData,
} from './dashboard-widget-dialog.component';

interface DashboardOfficialWidgetReview {
  catalogItem: DashboardWidgetCatalogItem;
  existingWidget?: DashboardWidget;
  desatualizado: boolean;
}

@Component({
  selector: 'app-dashboard-config-admin',
  standalone: true,
  imports: [
    NgClass,
    NgFor,
    NgIf,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './dashboard-config-admin.component.html',
  styleUrls: ['./dashboard-config-admin.component.scss'],
})
export class DashboardConfigAdminComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly service = inject(DashboardConfigService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly publicos = signal<DashboardPublico[]>([]);
  protected readonly dashboards = signal<DashboardConfiguracao[]>([]);
  protected readonly widgets = signal<DashboardWidget[]>([]);
  protected readonly selectedPublicoId = signal<string | null>(null);
  protected readonly selectedDashboardId = signal<string | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingDashboards = signal(false);
  protected readonly isLoadingWidgets = signal(false);
  protected readonly isProvisioningWidgets = signal(false);
  protected readonly isUpdatingOfficialWidgets = signal(false);

  protected readonly selectedPublico = computed(() =>
    this.publicos().find(item => item.id === this.selectedPublicoId()) ?? null,
  );
  protected readonly selectedDashboard = computed(() =>
    this.dashboards().find(item => item.id === this.selectedDashboardId()) ?? null,
  );
  protected readonly orderedDashboards = computed(() =>
    [...this.dashboards()].sort((left, right) => left.nome.localeCompare(right.nome, 'pt-BR')),
  );
  protected readonly orderedWidgets = computed(() =>
    [...this.widgets()].sort((left, right) => left.ordem - right.ordem || left.titulo.localeCompare(right.titulo, 'pt-BR')),
  );
  protected readonly officialWidgetsForPublico = computed(() => {
    const publicoCodigo = this.normalizePublicoCodigo(this.selectedPublico()?.codigo);

    return DASHBOARD_WIDGET_CATALOG
      .filter(item => item.publicos.includes('GERAL') || item.publicos.includes(publicoCodigo))
      .sort((left, right) => left.ordem - right.ordem || left.titulo.localeCompare(right.titulo, 'pt-BR'));
  });
  protected readonly missingOfficialWidgets = computed(() => {
    const existingCodes = new Set(this.widgets().map(widget => widget.codigo.trim().toUpperCase()));

    return this.officialWidgetsForPublico()
      .filter(item => !existingCodes.has(item.codigo));
  });
  protected readonly officialWidgetReview = computed<DashboardOfficialWidgetReview[]>(() => {
    const existingByCode = new Map(
      this.widgets().map(widget => [widget.codigo.trim().toUpperCase(), widget]),
    );

    return this.officialWidgetsForPublico().map(catalogItem => {
      const existingWidget = existingByCode.get(catalogItem.codigo);
      return {
        catalogItem,
        existingWidget,
        desatualizado: existingWidget ? this.isOfficialWidgetOutdated(existingWidget, catalogItem) : false,
      };
    });
  });
  protected readonly outdatedOfficialWidgets = computed(() =>
    this.officialWidgetReview()
      .filter(item => item.existingWidget && item.desatualizado),
  );

  ngOnInit(): void {
    this.loadPublicos();
  }

  protected selectPublico(publico: DashboardPublico): void {
    this.selectedPublicoId.set(publico.id);
    this.selectedDashboardId.set(null);
    this.widgets.set([]);
    this.loadDashboards(publico.id);
  }

  protected selectDashboard(dashboard: DashboardConfiguracao): void {
    this.selectedDashboardId.set(dashboard.id);
    this.loadWidgets(dashboard.id);
  }

  protected openPublicoDialog(publico?: DashboardPublico): void {
    const dialogRef = this.dialog.open<DashboardPublicoDialogComponent, DashboardPublicoDialogData, DashboardPublicoInput>(
      DashboardPublicoDialogComponent,
      {
        width: '680px',
        maxWidth: '95vw',
        disableClose: true,
        data: { publico },
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) return;

      const request = publico
        ? this.service.atualizarPublico(publico.id, payload)
        : this.service.criarPublico(payload);

      request.subscribe({
        next: saved => {
          this.snackBar.open('Público salvo com sucesso.', 'Fechar', { duration: 3000 });
          this.loadPublicos(saved.id);
        },
        error: error => this.showError(error, 'Não foi possível salvar público.'),
      });
    });
  }

  protected openDashboardDialog(dashboard?: DashboardConfiguracao): void {
    const publicoId = dashboard?.publicoDashboardId ?? this.selectedPublicoId();
    const dialogRef = this.dialog.open<DashboardConfigDialogComponent, DashboardConfigDialogData, DashboardConfiguracaoInput>(
      DashboardConfigDialogComponent,
      {
        width: '780px',
        maxWidth: '95vw',
        disableClose: true,
        data: {
          dashboard,
          publicos: this.publicos(),
          publicoDashboardId: publicoId ?? undefined,
        },
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) return;

      const request = dashboard
        ? this.service.atualizarDashboard(dashboard.id, payload)
        : this.service.criarDashboard(payload);

      request.subscribe({
        next: saved => {
          this.snackBar.open('Dashboard salvo com sucesso.', 'Fechar', { duration: 3000 });
          this.selectedPublicoId.set(saved.publicoDashboardId);
          this.loadDashboards(saved.publicoDashboardId, saved.id);
        },
        error: error => this.showError(error, 'Não foi possível salvar dashboard.'),
      });
    });
  }

  protected openWidgetDialog(widget?: DashboardWidget): void {
    const dashboardId = widget?.dashboardId ?? this.selectedDashboardId();
    if (!dashboardId) {
      this.snackBar.open('Selecione um dashboard antes de criar widgets.', 'Fechar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open<DashboardWidgetDialogComponent, DashboardWidgetDialogData, DashboardWidgetInput>(
      DashboardWidgetDialogComponent,
      {
        width: '780px',
        maxWidth: '95vw',
        disableClose: true,
        data: {
          widget,
          dashboardId,
          publicoCodigo: this.selectedPublico()?.codigo,
        },
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) return;

      const request = widget
        ? this.service.atualizarWidget(widget.id, payload)
        : this.service.criarWidget(payload);

      request.subscribe({
        next: saved => {
          this.snackBar.open('Widget salvo com sucesso.', 'Fechar', { duration: 3000 });
          this.loadWidgets(saved.dashboardId);
        },
        error: error => this.showError(error, 'Não foi possível salvar widget.'),
      });
    });
  }

  protected deletePublico(publico: DashboardPublico): void {
    if (!confirm(`Remover o público ${publico.descricao}?`)) return;

    this.service.excluirPublico(publico.id).subscribe({
      next: () => {
        this.snackBar.open('Público removido.', 'Fechar', { duration: 3000 });
        this.selectedPublicoId.set(null);
        this.selectedDashboardId.set(null);
        this.dashboards.set([]);
        this.widgets.set([]);
        this.loadPublicos();
      },
      error: error => this.showError(error, 'Não foi possível remover público.'),
    });
  }

  protected deleteDashboard(dashboard: DashboardConfiguracao): void {
    if (!confirm(`Remover o dashboard ${dashboard.nome}?`)) return;

    this.service.excluirDashboard(dashboard.id).subscribe({
      next: () => {
        this.snackBar.open('Dashboard removido.', 'Fechar', { duration: 3000 });
        this.selectedDashboardId.set(null);
        this.widgets.set([]);
        this.loadDashboards(this.selectedPublicoId());
      },
      error: error => this.showError(error, 'Não foi possível remover dashboard.'),
    });
  }

  protected deleteWidget(widget: DashboardWidget): void {
    if (!confirm(`Remover o widget ${widget.titulo}?`)) return;

    this.service.excluirWidget(widget.id).subscribe({
      next: () => {
        this.snackBar.open('Widget removido.', 'Fechar', { duration: 3000 });
        this.loadWidgets(this.selectedDashboardId());
      },
      error: error => this.showError(error, 'Não foi possível remover widget.'),
    });
  }

  protected provisionarWidgetsPadrao(): void {
    const dashboardId = this.selectedDashboardId();
    const missingWidgets = this.missingOfficialWidgets();
    if (!dashboardId || missingWidgets.length === 0 || this.isProvisioningWidgets()) {
      return;
    }

    this.isProvisioningWidgets.set(true);
    forkJoin(missingWidgets.map(widget => this.service.criarWidget(this.toWidgetInput(dashboardId, widget))))
      .subscribe({
        next: created => {
          this.isProvisioningWidgets.set(false);
          this.snackBar.open(`${created.length} widget(s) padrão provisionado(s).`, 'Fechar', { duration: 3000 });
          this.loadWidgets(dashboardId);
        },
        error: error => {
          this.isProvisioningWidgets.set(false);
          this.showError(error, 'Não foi possível provisionar widgets padrão.');
        },
      });
  }

  protected atualizarWidgetsOficiais(): void {
    const dashboardId = this.selectedDashboardId();
    const outdatedWidgets = this.outdatedOfficialWidgets();
    if (!dashboardId || outdatedWidgets.length === 0 || this.isUpdatingOfficialWidgets()) {
      return;
    }

    if (!confirm(`Atualizar metadados de ${outdatedWidgets.length} widget(s) oficial(is)?`)) {
      return;
    }

    this.isUpdatingOfficialWidgets.set(true);
    forkJoin(outdatedWidgets.map(item => {
      const existingWidget = item.existingWidget as DashboardWidget;
      return this.service.atualizarWidget(
        existingWidget.id,
        this.toWidgetInput(dashboardId, item.catalogItem, existingWidget.ativo),
      );
    })).subscribe({
      next: updated => {
        this.isUpdatingOfficialWidgets.set(false);
        this.snackBar.open(`${updated.length} widget(s) oficial(is) atualizado(s).`, 'Fechar', { duration: 3000 });
        this.loadWidgets(dashboardId);
      },
      error: error => {
        this.isUpdatingOfficialWidgets.set(false);
        this.showError(error, 'Não foi possível atualizar widgets oficiais.');
      },
    });
  }

  protected statusLabel(active: boolean): string {
    return active ? 'Ativo' : 'Inativo';
  }

  private loadPublicos(selectId?: string): void {
    this.isLoading.set(true);
    this.service.listarPublicos().subscribe({
      next: publicos => {
        this.publicos.set(publicos.sort((left, right) => left.descricao.localeCompare(right.descricao, 'pt-BR')));
        this.isLoading.set(false);

        const selectedId = selectId ?? this.selectedPublicoId() ?? publicos[0]?.id ?? null;
        const selected = publicos.find(item => item.id === selectedId);
        if (selected) {
          this.selectPublico(selected);
        }
      },
      error: error => {
        this.isLoading.set(false);
        this.showError(error, 'Não foi possível carregar públicos de dashboard.');
      },
    });
  }

  private loadDashboards(publicoId?: string | null, selectDashboardId?: string): void {
    this.isLoadingDashboards.set(true);
    this.service.listarDashboards(publicoId).subscribe({
      next: dashboards => {
        this.dashboards.set(dashboards);
        this.isLoadingDashboards.set(false);

        const selected = dashboards.find(item => item.id === selectDashboardId)
          ?? dashboards.find(item => item.id === this.selectedDashboardId())
          ?? dashboards[0]
          ?? null;

        if (selected) {
          this.selectDashboard(selected);
        } else {
          this.selectedDashboardId.set(null);
          this.widgets.set([]);
        }
      },
      error: error => {
        this.isLoadingDashboards.set(false);
        this.showError(error, 'Não foi possível carregar dashboards.');
      },
    });
  }

  private loadWidgets(dashboardId?: string | null): void {
    if (!dashboardId) {
      this.widgets.set([]);
      return;
    }

    this.isLoadingWidgets.set(true);
    this.service.listarWidgets(dashboardId).subscribe({
      next: widgets => {
        this.widgets.set(widgets);
        this.isLoadingWidgets.set(false);
      },
      error: error => {
        this.isLoadingWidgets.set(false);
        this.showError(error, 'Não foi possível carregar widgets.');
      },
    });
  }

  private toWidgetInput(dashboardId: string, widget: DashboardWidgetCatalogItem, ativo = true): DashboardWidgetInput {
    return {
      dashboardId,
      codigo: widget.codigo,
      titulo: widget.titulo,
      descricao: widget.descricao,
      tipoWidget: widget.tipoWidget,
      ordem: widget.ordem,
      queryReferencia: widget.queryReferencia,
      ativo,
    };
  }

  private isOfficialWidgetOutdated(widget: DashboardWidget, catalogItem: DashboardWidgetCatalogItem): boolean {
    return widget.titulo !== catalogItem.titulo
      || (widget.descricao ?? '') !== catalogItem.descricao
      || widget.tipoWidget !== catalogItem.tipoWidget
      || widget.ordem !== catalogItem.ordem
      || (widget.queryReferencia ?? '') !== catalogItem.queryReferencia;
  }

  private normalizePublicoCodigo(value?: string | null): DashboardPublicoCodigo {
    const normalized = value?.trim().toUpperCase();
    if (normalized === 'SECRETARIA' || normalized === 'DIRETOR' || normalized === 'PROFESSOR') {
      return normalized;
    }

    return 'ACADEMICO';
  }

  private showError(error: unknown, fallbackMessage: string): void {
    this.snackBar.open(getApiErrorMessage(error, fallbackMessage), 'Fechar', { duration: 4500 });
  }
}
