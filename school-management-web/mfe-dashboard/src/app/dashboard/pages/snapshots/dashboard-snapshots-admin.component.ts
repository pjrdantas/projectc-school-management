import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Professor } from '../../../compartilhado/professor/teacher.model';
import { TeachersService } from '../../../compartilhado/professor/teachers.service';
import {
  DashboardIndicadorHistorico,
  DashboardIndicadorHistoricoPonto,
  DashboardIndicadorSnapshot,
  DashboardPublico,
} from '../../models';
import { DashboardConfigService, DashboardSnapshotService } from '../../services';

@Component({
  selector: 'app-dashboard-snapshots-admin',
  standalone: true,
  imports: [
    DatePipe,
    NgClass,
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './dashboard-snapshots-admin.component.html',
  styleUrls: ['./dashboard-snapshots-admin.component.scss'],
})
export class DashboardSnapshotsAdminComponent implements OnInit {
  private readonly configService = inject(DashboardConfigService);
  private readonly fb = inject(FormBuilder);
  private readonly snapshotService = inject(DashboardSnapshotService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly teachersService = inject(TeachersService);

  protected readonly publicos = signal<DashboardPublico[]>([]);
  protected readonly professores = signal<Professor[]>([]);
  protected readonly snapshots = signal<DashboardIndicadorSnapshot[]>([]);
  protected readonly historicos = signal<DashboardIndicadorHistorico[]>([]);
  protected readonly isLoadingPublicos = signal(false);
  protected readonly isLoadingProfessores = signal(false);
  protected readonly isLoadingSnapshots = signal(false);
  protected readonly isLoadingHistorico = signal(false);
  protected readonly isGeneratingSnapshots = signal(false);

  protected readonly filterForm = this.fb.nonNullable.group({
    publicoCodigo: [''],
    professorId: [''],
    referenciaData: [''],
    dataInicio: [''],
    dataFim: [''],
  });

  protected readonly orderedSnapshots = computed(() =>
    [...this.snapshots()].sort((left, right) => {
      const dateOrder = right.referenciaData.localeCompare(left.referenciaData);
      if (dateOrder !== 0) {
        return dateOrder;
      }

      return left.codigoIndicador.localeCompare(right.codigoIndicador, 'pt-BR');
    }),
  );
  protected readonly numericSnapshotsCount = computed(() =>
    this.snapshots().filter(item => item.valorNumeric !== null && item.valorNumeric !== undefined).length,
  );
  protected readonly textSnapshotsCount = computed(() =>
    this.snapshots().filter(item => item.valorTexto).length,
  );
  protected readonly referenciaResumo = computed(() => {
    const dates = [...new Set(this.snapshots().map(item => item.referenciaData))];
    return dates.length === 1 ? dates[0] : `${dates.length} datas`;
  });
  protected readonly orderedHistoricos = computed(() =>
    [...this.historicos()].sort((left, right) =>
      this.codigoIndicadorHistoricoVisivel(left).localeCompare(this.codigoIndicadorHistoricoVisivel(right), 'pt-BR'),
    ),
  );
  protected canGenerateSnapshots(): boolean {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    const professorId = this.filterForm.controls.professorId.value;
    return !!publicoCodigo
      && (publicoCodigo !== 'PROFESSOR' || !!professorId)
      && !this.isLoadingPublicos()
      && !this.isLoadingProfessores()
      && !this.isLoadingSnapshots()
      && !this.isGeneratingSnapshots();
  }
  protected canConsultarHistorico(): boolean {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    const professorId = this.filterForm.controls.professorId.value;
    return !!publicoCodigo
      && (publicoCodigo !== 'PROFESSOR' || !!professorId)
      && !this.isLoadingPublicos()
      && !this.isLoadingProfessores()
      && !this.isLoadingHistorico();
  }

  ngOnInit(): void {
    this.loadPublicos();
    this.loadProfessores();
  }

  protected consultar(): void {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    if (!publicoCodigo) {
      this.snackBar.open('Selecione um público antes de consultar snapshots.', 'Fechar', { duration: 3000 });
      return;
    }

    this.loadSnapshots(publicoCodigo, this.filterForm.controls.referenciaData.value);
    if (this.canConsultarHistorico()) {
      this.loadHistorico();
    } else {
      this.historicos.set([]);
    }
  }

  protected limparData(): void {
    this.filterForm.controls.referenciaData.setValue('');
    this.consultar();
  }

  protected gerarSnapshot(): void {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    const professorId = this.filterForm.controls.professorId.value;
    const referenciaData = this.filterForm.controls.referenciaData.value;
    if (!publicoCodigo) {
      this.snackBar.open('Selecione um público antes de gerar snapshots.', 'Fechar', { duration: 3000 });
      return;
    }

    if (publicoCodigo === 'PROFESSOR' && !professorId) {
      this.snackBar.open('Selecione um professor antes de gerar snapshots do professor.', 'Fechar', { duration: 4000 });
      return;
    }

    const referenciaTexto = referenciaData || 'hoje';
    const alvoTexto = publicoCodigo === 'PROFESSOR'
      ? this.professores().find(professor => professor.id === professorId)?.nomeCompleto ?? 'professor selecionado'
      : publicoCodigo;

    if (!confirm(`Gerar snapshots para ${alvoTexto} com referência ${referenciaTexto}?`)) {
      return;
    }

    this.isGeneratingSnapshots.set(true);
    const request = publicoCodigo === 'PROFESSOR'
      ? this.snapshotService.gerarProfessor(professorId, referenciaData)
      : this.snapshotService.gerarPorPublicoCodigo(publicoCodigo, referenciaData);

    request.subscribe({
      next: generated => {
        this.isGeneratingSnapshots.set(false);
        this.snapshots.set(generated);
        this.snackBar.open(`${generated.length} snapshot(s) gerado(s).`, 'Fechar', { duration: 3000 });
        this.loadSnapshots(publicoCodigo, referenciaData);
        if (this.canConsultarHistorico()) {
          this.loadHistorico();
        }
      },
      error: error => {
        this.isGeneratingSnapshots.set(false);
        this.showError(error, 'Não foi possível gerar snapshots de dashboard.');
      },
    });
  }

  protected valorPrincipal(snapshot: DashboardIndicadorSnapshot): string {
    if (snapshot.valorNumeric !== null && snapshot.valorNumeric !== undefined) {
      return String(snapshot.valorNumeric);
    }

    return snapshot.valorTexto || '-';
  }

  protected codigoIndicadorVisivel(snapshot: DashboardIndicadorSnapshot): string {
    if (snapshot.publicoCodigo === 'PROFESSOR') {
      return this.removeProfessorPrefix(snapshot.codigoIndicador);
    }

    return snapshot.codigoIndicador;
  }

  protected codigoIndicadorHistoricoVisivel(historico: DashboardIndicadorHistorico): string {
    if (historico.publicoCodigo === 'PROFESSOR') {
      return this.removeProfessorPrefix(historico.codigoIndicador);
    }

    return historico.codigoIndicador;
  }

  protected variacaoClass(historico: DashboardIndicadorHistorico): string {
    const value = Number(historico.variacaoPercentual ?? 0);
    if (value > 0) {
      return 'positive';
    }
    if (value < 0) {
      return 'negative';
    }
    return 'neutral';
  }

  protected formatPercent(value?: number | string | null): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }

    return `${Number(value).toFixed(1)}%`;
  }

  protected formatValue(value?: number | string | null): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }

    return String(value);
  }

  protected pontoValor(ponto: DashboardIndicadorHistoricoPonto): string {
    if (ponto.valorNumeric !== null && ponto.valorNumeric !== undefined) {
      return String(ponto.valorNumeric);
    }

    return ponto.valorTexto || '-';
  }

  private loadPublicos(): void {
    this.isLoadingPublicos.set(true);
    this.configService.listarPublicos().subscribe({
      next: publicos => {
        const ordered = [...publicos].sort((left, right) => left.descricao.localeCompare(right.descricao, 'pt-BR'));
        this.publicos.set(ordered);
        this.isLoadingPublicos.set(false);

        const firstPublico = ordered[0];
        if (firstPublico) {
          this.filterForm.controls.publicoCodigo.setValue(firstPublico.codigo);
          this.loadSnapshots(firstPublico.codigo, '');
        }
      },
      error: error => {
        this.isLoadingPublicos.set(false);
        this.showError(error, 'Não foi possível carregar públicos de dashboard.');
      },
    });
  }

  private loadProfessores(): void {
    this.isLoadingProfessores.set(true);
    this.teachersService.listar().subscribe({
      next: professores => {
        this.professores.set(
          [...professores]
            .filter(professor => professor.ativo)
            .sort((left, right) => left.nomeCompleto.localeCompare(right.nomeCompleto, 'pt-BR')),
        );
        this.isLoadingProfessores.set(false);
      },
      error: error => {
        this.professores.set([]);
        this.isLoadingProfessores.set(false);
        this.showError(error, 'Não foi possível carregar professores.');
      },
    });
  }

  private loadSnapshots(publicoCodigo: string, referenciaData: string): void {
    this.isLoadingSnapshots.set(true);
    this.snapshotService.listarPorPublicoCodigo(publicoCodigo, referenciaData).subscribe({
      next: snapshots => {
        this.snapshots.set(this.filterProfessorSnapshots(snapshots));
        this.isLoadingSnapshots.set(false);
      },
      error: error => {
        this.snapshots.set([]);
        this.isLoadingSnapshots.set(false);
        this.showError(error, 'Não foi possível carregar snapshots de dashboard.');
      },
    });
  }

  protected loadHistorico(): void {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    if (!publicoCodigo) {
      this.snackBar.open('Selecione um público antes de consultar o histórico.', 'Fechar', { duration: 3000 });
      return;
    }

    if (publicoCodigo === 'PROFESSOR' && !this.filterForm.controls.professorId.value) {
      this.snackBar.open('Selecione um professor para consultar o histórico do professor.', 'Fechar', { duration: 4000 });
      return;
    }

    this.isLoadingHistorico.set(true);
    this.snapshotService.consultarHistorico(publicoCodigo, {
      dataInicio: this.filterForm.controls.dataInicio.value,
      dataFim: this.filterForm.controls.dataFim.value,
      professorId: publicoCodigo === 'PROFESSOR' ? this.filterForm.controls.professorId.value : null,
    }).subscribe({
      next: historicos => {
        this.historicos.set(historicos);
        this.isLoadingHistorico.set(false);
      },
      error: error => {
        this.historicos.set([]);
        this.isLoadingHistorico.set(false);
        this.showError(error, 'Não foi possível carregar histórico de snapshots.');
      },
    });
  }

  private filterProfessorSnapshots(snapshots: DashboardIndicadorSnapshot[]): DashboardIndicadorSnapshot[] {
    const publicoCodigo = this.filterForm.controls.publicoCodigo.value;
    const professorId = this.filterForm.controls.professorId.value;
    if (publicoCodigo !== 'PROFESSOR' || !professorId) {
      return snapshots;
    }

    return snapshots.filter(snapshot => snapshot.valorTexto === professorId);
  }

  private removeProfessorPrefix(codigoIndicador: string): string {
    return codigoIndicador.replace(/^PROFESSOR_[A-F0-9]{32}_/i, '');
  }

  private showError(error: unknown, fallbackMessage: string): void {
    this.snackBar.open(getApiErrorMessage(error, fallbackMessage), 'Fechar', { duration: 4500 });
  }
}
