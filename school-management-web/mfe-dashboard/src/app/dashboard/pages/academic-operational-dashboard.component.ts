import { DecimalPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../core/http/api-error';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  DashboardFrontendWidget,
  DashboardFrontendResponse,
  DashboardPublicoCodigo,
  DashboardResumo,
  DashboardUsuarioConfiguracao,
} from '../models';
import { DashboardService } from '../services';

interface DashboardMetric {
  label: string;
  value: number;
  icon: string;
  tone: 'primary' | 'warning' | 'success' | 'neutral';
  widgetKey?: DashboardWidgetKey;
  defaultOrder?: number;
}

interface ChartPoint {
  label: string;
  value: number;
  color: string;
  percentual: number;
  tooltip: string;
}

interface PieSlice extends ChartPoint {
  path: string;
}

interface LineChartPoint extends ChartPoint {
  x: number;
  y: number;
}

interface DashboardPersonalizacaoItem {
  widget: DashboardFrontendWidget;
  config?: DashboardUsuarioConfiguracao;
  visivel: boolean;
  ordem: number;
}

type DashboardWidgetKey =
  | 'metric-total-matriculas'
  | 'metric-solicitadas'
  | 'metric-em-andamento'
  | 'metric-documentos'
  | 'metric-transferencias'
  | 'metric-pendentes'
  | 'metric-alunos-ativos'
  | 'metric-turmas-ativas'
  | 'metric-concluidas'
  | 'metric-rematricula'
  | 'chart-status'
  | 'chart-vagas'
  | 'chart-tendencia'
  | 'list-status'
  | 'list-vagas'
  | 'panel-documentos'
  | 'panel-resultado'
  | 'sector-academico'
  | 'sector-administrativo'
  | 'sector-pedagogico'
  | 'panel-alertas';

@Component({
  selector: 'app-academic-operational-dashboard',
  standalone: true,
  imports: [
    DecimalPipe,
    NgFor,
    NgIf,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './academic-operational-dashboard.component.html',
  styleUrls: ['./academic-operational-dashboard.component.scss'],
})
export class AcademicOperationalDashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly shellContext = inject(ShellContextService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly widgetAliases: Record<DashboardWidgetKey, string[]> = {
    'metric-total-matriculas': ['TOTAL MATRICULAS', 'MATRICULAS TOTAL'],
    'metric-solicitadas': ['MATRICULAS SOLICITADAS', 'SOLICITADAS'],
    'metric-em-andamento': ['MATRICULAS EM ANDAMENTO', 'EM ANDAMENTO'],
    'metric-documentos': ['DOCUMENTOS PENDENTES', 'AGUARDANDO DOCUMENTOS', 'COM DOCUMENTOS PENDENTES'],
    'metric-transferencias': ['TRANSFERENCIAS'],
    'metric-pendentes': ['MATRICULAS PENDENTES', 'PENDENTES'],
    'metric-alunos-ativos': ['ALUNOS ATIVOS'],
    'metric-turmas-ativas': ['TURMAS ATIVAS'],
    'metric-concluidas': ['MATRICULAS CONCLUIDAS', 'CONCLUIDAS'],
    'metric-rematricula': ['APTAS REMATRICULA', 'REMATRICULA'],
    'chart-status': ['GRAFICO STATUS', 'PIZZA STATUS', 'MATRICULAS POR STATUS', 'STATUS MATRICULAS'],
    'chart-vagas': ['GRAFICO VAGAS', 'COLUNAS VAGAS', 'VAGAS DISPONIVEIS'],
    'chart-tendencia': ['GRAFICO TENDENCIA', 'TENDENCIA OPERACIONAL', 'HISTORICO', 'LINHA'],
    'list-status': ['LISTA STATUS', 'STATUS DAS MATRICULAS', 'DISTRIBUICAO POR STATUS'],
    'list-vagas': ['LISTA VAGAS', 'TURMAS COM VAGAS'],
    'panel-documentos': ['DOCUMENTOS E HISTORICO', 'BOLETINS', 'HISTORICOS INTERNOS'],
    'panel-resultado': ['RESULTADO ACADEMICO', 'APROVADOS', 'REPROVADOS'],
    'sector-academico': ['SETOR ACADEMICO', 'ACADEMICO'],
    'sector-administrativo': ['SETOR ADMINISTRATIVO', 'ADMINISTRATIVO'],
    'sector-pedagogico': ['SETOR PEDAGOGICO', 'PEDAGOGICO'],
    'panel-alertas': ['ALERTAS'],
  };

  protected readonly isLoading = signal(false);
  protected readonly dashboard = signal<DashboardFrontendResponse | null>(null);
  protected readonly professorDashboardPendente = signal(false);
  protected readonly isSavingPreference = signal(false);

  protected readonly publicoCodigo = computed(() => this.dashboard()?.publicoCodigo ?? this.resolvePublicoCodigo());
  protected readonly titulo = computed(() => {
    switch (this.publicoCodigo()) {
      case 'SECRETARIA':
        return 'Dashboard da secretaria';
      case 'DIRETOR':
        return 'Dashboard da direção';
      case 'PROFESSOR':
        return 'Dashboard do professor';
      default:
        return 'Dashboard acadêmica';
    }
  });
  protected readonly subtitulo = computed(() => {
    switch (this.publicoCodigo()) {
      case 'SECRETARIA':
        return 'Rotina administrativa, matrículas e pendências';
      case 'DIRETOR':
        return 'Visão executiva por áreas da operação escolar';
      case 'PROFESSOR':
        return 'Atividades docentes e turmas vinculadas';
      default:
        return 'Operação acadêmica em tempo real';
    }
  });
  protected readonly resumo = computed<DashboardResumo | null>(
    () => this.dashboard()?.resumo ?? null,
  );
  protected readonly dashboardConfigurado = computed(() =>
    (this.dashboard()?.dashboards ?? []).find(item => item.ativo) ?? null,
  );
  protected readonly personalizacaoItems = computed<DashboardPersonalizacaoItem[]>(() => {
    const dashboard = this.dashboardConfigurado();
    if (!dashboard) {
      return [];
    }

    const configByWidgetId = new Map(
      (this.dashboard()?.configuracoesUsuario ?? []).map(config => [config.dashboardWidgetId, config]),
    );

    return dashboard.widgets
      .filter(widget => widget.ativo)
      .map(widget => {
        const config = configByWidgetId.get(widget.id);
        return {
          widget,
          config,
          visivel: config?.visivel ?? true,
          ordem: config?.ordem ?? widget.ordem ?? 0,
        };
      })
      .sort((left, right) => left.ordem - right.ordem || left.widget.titulo.localeCompare(right.widget.titulo, 'pt-BR'));
  });
  protected readonly metrics = computed<DashboardMetric[]>(() => {
    const resumo = this.resumo();
    if (!resumo) {
      return [];
    }

    switch (this.publicoCodigo()) {
      case 'SECRETARIA':
        return [
          this.metric('Solicitadas', resumo.matriculasSolicitadas, 'assignment', 'primary', 'metric-solicitadas', 1),
          this.metric('Em andamento', resumo.matriculasEmAndamento, 'hourglass_top', 'neutral', 'metric-em-andamento', 2),
          this.metric('Documentos pendentes', resumo.matriculasComDocumentosPendentes, 'pending_actions', 'warning', 'metric-documentos', 3),
          this.metric('Transferências', resumo.transferencias, 'sync_alt', 'success', 'metric-transferencias', 4),
        ];
      case 'DIRETOR':
        return [
          this.metric('Matrículas', resumo.totalMatriculas, 'assignment', 'primary', 'metric-total-matriculas', 1),
          this.metric('Pendentes', resumo.matriculasPendentes, 'pending_actions', 'warning', 'metric-pendentes', 2),
          this.metric('Alunos ativos', resumo.alunosAtivos, 'groups', 'success', 'metric-alunos-ativos', 3),
          this.metric('Turmas ativas', resumo.turmasAtivas, 'class', 'neutral', 'metric-turmas-ativas', 4),
        ];
      default:
        return [
          this.metric('Matrículas', resumo.totalMatriculas, 'assignment', 'primary', 'metric-total-matriculas', 1),
          this.metric('Aguardando documentos', resumo.matriculasAguardandoDocumentos, 'pending_actions', 'warning', 'metric-documentos', 2),
          this.metric('Concluídas', resumo.matriculasConcluidas, 'task_alt', 'success', 'metric-concluidas', 3),
          this.metric('Aptas para rematrícula', resumo.matriculasAptasRematricula, 'autorenew', 'neutral', 'metric-rematricula', 4),
        ];
    }
  });
  protected readonly metricTiles = computed(() =>
    this.metrics()
      .filter(metric => !metric.widgetKey || this.widgetVisivel(metric.widgetKey))
      .sort((left, right) =>
        this.widgetOrdem(left.widgetKey, left.defaultOrder ?? 0) -
        this.widgetOrdem(right.widgetKey, right.defaultOrder ?? 0),
      ),
  );
  protected readonly maxStatusTotal = computed(() =>
    Math.max(...(this.resumo()?.matriculasPorStatus ?? []).map(item => item.total), 1),
  );
  protected readonly maxVagasDisponiveis = computed(() =>
    Math.max(...(this.resumo()?.turmasComVagas ?? []).map(item => item.vagasDisponiveis), 1),
  );
  protected readonly statusChart = computed<ChartPoint[]>(() =>
    this.withPercentual(
      (this.resumo()?.matriculasPorStatus ?? []).map((item, index) => ({
        label: item.status,
        value: item.total,
        color: this.chartColors[index % this.chartColors.length],
      })),
    ),
  );
  protected readonly vagasChart = computed<ChartPoint[]>(() =>
    this.withPercentual(
      (this.resumo()?.turmasComVagas ?? []).slice(0, 6).map((item, index) => ({
        label: item.turmaNome,
        value: item.vagasDisponiveis,
        color: this.chartColors[(index + 2) % this.chartColors.length],
      })),
    ),
  );
  protected readonly historicoChart = computed<ChartPoint[]>(() => {
    const historico = this.dashboard()?.historico ?? [];
    const principal = historico.find(item => item.pontos?.length)?.pontos ?? [];
    if (principal.length > 0) {
      return this.withPercentual(
        principal.slice(-8).map((ponto, index) => ({
          label: ponto.referenciaData,
          value: Number(ponto.valorNumeric ?? 0),
          color: this.chartColors[index % this.chartColors.length],
        })),
      );
    }

    return this.statusChart().slice(0, 8);
  });
  protected readonly pieSlices = computed(() => this.buildPieSlices(this.statusChart()));
  protected readonly lineChartPoints = computed(() => this.buildLineChartPoints(this.historicoChart()));
  protected readonly linePoints = computed(() => this.buildLinePoints(this.historicoChart()));
  protected readonly lineChartContext = computed(() => {
    const historico = this.dashboard()?.historico ?? [];
    return historico.some(item => item.pontos?.length) ? 'período' : 'status';
  });
  protected readonly maxVagasChartValue = computed(() =>
    Math.max(...this.vagasChart().map(item => item.value), 1),
  );
  private readonly chartColors = [
    '#2f6fed',
    '#16a34a',
    '#f59e0b',
    '#ef4444',
    '#0891b2',
    '#7c3aed',
    '#64748b',
    '#db2777',
  ];
  protected readonly setorAcademico = computed<DashboardMetric[]>(() => {
    const resumo = this.resumo();
    if (!resumo) return [];

    return [
      this.metric('Concluídas', resumo.matriculasConcluidas, 'task_alt', 'success'),
      this.metric('Efetivadas', resumo.matriculasEfetivadas, 'how_to_reg', 'primary'),
      this.metric('Boletins', resumo.boletinsFechados, 'fact_check', 'neutral'),
      this.metric('Reprovados', resumo.alunosReprovados, 'report', 'warning'),
    ];
  });
  protected readonly setorAdministrativo = computed<DashboardMetric[]>(() => {
    const resumo = this.resumo();
    if (!resumo) return [];

    return [
      this.metric('Documentos pendentes', resumo.matriculasComDocumentosPendentes, 'description', 'warning'),
      this.metric('Histórico pendente', resumo.matriculasAguardandoHistoricoEscolar, 'history_edu', 'warning'),
      this.metric('Transferências', resumo.transferencias, 'sync_alt', 'neutral'),
      this.metric('Exclusões pendentes', resumo.solicitacoesExclusaoPendentes, 'delete_sweep', 'warning'),
    ];
  });
  protected readonly setorPedagogico = computed<DashboardMetric[]>(() => {
    const resumo = this.resumo();
    if (!resumo) return [];

    return [
      this.metric('Professores alocados', resumo.professoresAlocados, 'co_present', 'primary'),
      this.metric('Aulas realizadas', resumo.aulasRealizadas, 'event_available', 'success'),
      this.metric('Avaliações', resumo.avaliacoesRegistradas, 'edit_note', 'neutral'),
      this.metric('Notas pendentes', resumo.avaliacoesComNotasPendentes, 'rule', 'warning'),
    ];
  });
  protected readonly diretorSetoresVisiveis = computed(() =>
    this.widgetVisivel('sector-academico') ||
    this.widgetVisivel('sector-administrativo') ||
    this.widgetVisivel('sector-pedagogico'),
  );

  ngOnInit(): void {
    this.carregar();
  }

  protected carregar(): void {
    const publicoCodigo = this.resolvePublicoCodigo();
    this.professorDashboardPendente.set(false);
    this.dashboard.set(null);

    if (publicoCodigo === 'PROFESSOR') {
      const professorId = this.shellContext.getUsuario()?.professorId ?? null;
      if (!professorId) {
        this.professorDashboardPendente.set(true);
        return;
      }

      this.carregarDashboard(publicoCodigo, professorId);
      return;
    }

    this.carregarDashboard(publicoCodigo);
  }

  private carregarDashboard(publicoCodigo: DashboardPublicoCodigo, professorId?: string | null): void {
    this.isLoading.set(true);
    this.dashboardService.consultar(publicoCodigo, professorId).subscribe({
      next: dashboard => {
        this.dashboard.set(dashboard);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar o dashboard operacional.'),
          'Fechar',
          { duration: 4500 },
        );
      },
    });
  }

  protected statusPercentual(total: number): number {
    return Math.round((total / this.maxStatusTotal()) * 100);
  }

  protected vagasPercentual(vagasDisponiveis: number): number {
    return Math.round((vagasDisponiveis / this.maxVagasDisponiveis()) * 100);
  }

  protected barHeight(value: number): number {
    return Math.max(Math.round((value / this.maxVagasChartValue()) * 100), value > 0 ? 8 : 0);
  }

  protected value(value?: number | null): number {
    return value ?? 0;
  }

  protected widgetVisivel(key: DashboardWidgetKey): boolean {
    return this.findPreferenceFor(key)?.visivel ?? true;
  }

  protected widgetOrdem(key: DashboardWidgetKey | undefined, defaultOrder: number): number {
    if (!key) {
      return defaultOrder;
    }

    return this.findPreferenceFor(key)?.ordem ?? defaultOrder;
  }

  protected alterarVisibilidade(item: DashboardPersonalizacaoItem, event: Event): void {
    const checked = (event.target as HTMLInputElement | null)?.checked ?? false;
    this.salvarPreferencia(item, checked, item.ordem);
  }

  protected alterarOrdem(item: DashboardPersonalizacaoItem, event: Event): void {
    const value = Number((event.target as HTMLInputElement | null)?.value ?? item.ordem);
    this.salvarPreferencia(item, item.visivel, Number.isFinite(value) && value >= 0 ? value : item.ordem);
  }

  protected restaurarPreferencia(item: DashboardPersonalizacaoItem): void {
    const usuarioId = this.shellContext.getUsuario()?.usuarioId;
    if (!usuarioId) {
      this.snackBar.open('Usuário não identificado para restaurar preferência.', 'Fechar', { duration: 3500 });
      return;
    }

    this.isSavingPreference.set(true);
    this.dashboardService.excluirConfiguracaoWidget(usuarioId, item.widget.id).subscribe({
      next: () => {
        this.isSavingPreference.set(false);
        this.removerConfiguracaoSalva(item.widget.id);
        this.snackBar.open('Preferência restaurada.', 'Fechar', { duration: 2500 });
      },
      error: (error: unknown) => {
        this.isSavingPreference.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível restaurar preferência.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected formatPercentual(percentual: number): string {
    return `${percentual.toLocaleString('pt-BR', {
      minimumFractionDigits: 1,
      maximumFractionDigits: 1,
    })}%`;
  }

  private metric(
    label: string,
    value: number | undefined | null,
    icon: string,
    tone: DashboardMetric['tone'],
    widgetKey?: DashboardWidgetKey,
    defaultOrder?: number,
  ): DashboardMetric {
    return {
      label,
      value: value ?? 0,
      icon,
      tone,
      widgetKey,
      defaultOrder,
    };
  }

  private findPreferenceFor(key: DashboardWidgetKey): DashboardPersonalizacaoItem | undefined {
    return this.personalizacaoItems()
      .filter(item => this.widgetMatches(item.widget, key))
      .sort((left, right) => left.ordem - right.ordem)[0];
  }

  private widgetMatches(widget: DashboardFrontendWidget, key: DashboardWidgetKey): boolean {
    const text = this.normalizeWidgetText([
      widget.codigo,
      widget.titulo,
      widget.descricao,
      widget.tipoWidget,
      widget.queryReferencia,
    ].filter(Boolean).join(' '));

    return this.widgetAliases[key].some(alias => text.includes(alias));
  }

  private normalizeWidgetText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-zA-Z0-9]+/g, ' ')
      .trim()
      .toUpperCase();
  }

  private salvarPreferencia(item: DashboardPersonalizacaoItem, visivel: boolean, ordem: number): void {
    const usuarioId = this.shellContext.getUsuario()?.usuarioId;
    if (!usuarioId) {
      this.snackBar.open('Usuário não identificado para salvar preferência.', 'Fechar', { duration: 3500 });
      return;
    }

    this.isSavingPreference.set(true);
    this.dashboardService.salvarConfiguracaoWidget(usuarioId, item.widget.id, {
      visivel,
      ordem,
      configuracaoJson: item.config?.configuracaoJson ?? null,
    }).subscribe({
      next: saved => {
        this.isSavingPreference.set(false);
        this.aplicarConfiguracaoSalva(saved);
        this.snackBar.open('Preferência salva.', 'Fechar', { duration: 2200 });
      },
      error: (error: unknown) => {
        this.isSavingPreference.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível salvar preferência.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private aplicarConfiguracaoSalva(config: DashboardUsuarioConfiguracao): void {
    const current = this.dashboard();
    if (!current) {
      return;
    }

    this.dashboard.set({
      ...current,
      configuracoesUsuario: [
        ...current.configuracoesUsuario.filter(item => item.dashboardWidgetId !== config.dashboardWidgetId),
        config,
      ],
    });
  }

  private removerConfiguracaoSalva(widgetId: string): void {
    const current = this.dashboard();
    if (!current) {
      return;
    }

    this.dashboard.set({
      ...current,
      configuracoesUsuario: current.configuracoesUsuario.filter(item => item.dashboardWidgetId !== widgetId),
    });
  }

  private resolvePublicoCodigo(): DashboardPublicoCodigo {
    const perfis = this.shellContext
      .getUsuario()
      ?.perfis.map(perfil => perfil.trim().toUpperCase()) ?? [];

    if (perfis.includes('ADMIN')) return 'ACADEMICO';
    if (perfis.includes('DIRETOR')) return 'DIRETOR';
    if (perfis.includes('SECRETARIA')) return 'SECRETARIA';
    if (perfis.includes('PROFESSOR')) return 'PROFESSOR';

    return 'ACADEMICO';
  }

  private withPercentual(
    points: Array<Omit<ChartPoint, 'percentual' | 'tooltip'>>,
  ): ChartPoint[] {
    const total = points.reduce((sum, point) => sum + point.value, 0);

    return points.map(point => {
      const percentual = total > 0 ? (point.value / total) * 100 : 0;

      return {
        ...point,
        percentual,
        tooltip: this.formatPercentual(percentual),
      };
    });
  }

  private buildPieSlices(points: ChartPoint[]): PieSlice[] {
    const total = points.reduce((sum, point) => sum + point.value, 0);
    if (total <= 0) {
      return [];
    }

    let startAngle = -90;

    return points.map(point => {
      const endAngle = startAngle + (point.value / total) * 360;
      const path = this.describeArc(68, 68, 58, startAngle, endAngle);
      startAngle = endAngle;

      return {
        ...point,
        path,
      };
    });
  }

  private describeArc(cx: number, cy: number, radius: number, startAngle: number, endAngle: number): string {
    const start = this.polarToCartesian(cx, cy, radius, endAngle);
    const end = this.polarToCartesian(cx, cy, radius, startAngle);
    const angle = endAngle - startAngle;

    if (angle >= 359.99) {
      const middle = this.polarToCartesian(cx, cy, radius, startAngle + 180);
      return `M ${cx} ${cy} L ${end.x} ${end.y} A ${radius} ${radius} 0 1 1 ${middle.x} ${middle.y} A ${radius} ${radius} 0 1 1 ${end.x} ${end.y} Z`;
    }

    const largeArcFlag = angle <= 180 ? '0' : '1';
    return `M ${cx} ${cy} L ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArcFlag} 0 ${end.x} ${end.y} Z`;
  }

  private polarToCartesian(cx: number, cy: number, radius: number, angleInDegrees: number): { x: number; y: number } {
    const angleInRadians = (angleInDegrees * Math.PI) / 180;

    return {
      x: cx + radius * Math.cos(angleInRadians),
      y: cy + radius * Math.sin(angleInRadians),
    };
  }

  private buildLinePoints(points: ChartPoint[]): string {
    return this.buildLineChartPoints(points)
      .map(point => `${point.x},${point.y}`)
      .join(' ');
  }

  private buildLineChartPoints(points: ChartPoint[]): LineChartPoint[] {
    if (points.length === 0) {
      return [];
    }

    const maxValue = Math.max(...points.map(point => point.value), 1);
    const step = points.length === 1 ? 240 : 240 / (points.length - 1);

    return points.map((point, index) => ({
      ...point,
      x: 20 + index * step,
      y: 110 - (point.value / maxValue) * 82,
    }));
  }
}
