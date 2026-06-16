import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { getApiErrorMessage } from '../../../core/http/api-error';
import {
  BimonthlyPlanning,
  BimonthlyPlanningAssessmentInput,
  BimonthlyPlanningLessonInput,
  PedagogicalContentLibraryItem,
  PlanningAiContent,
  PlanningAiContentVersion,
  PlanningAiInteraction,
  PlanningAiVersionInput,
} from '../../models';
import { PlanningService } from '../../services';
import {
  PlanningAiVersionDialogComponent,
  PlanningAiVersionDialogData,
} from './planning-ai-version-dialog.component';
import { PlanningAssessmentDialogComponent } from './planning-assessment-dialog.component';
import { PlanningLessonDialogComponent } from './planning-lesson-dialog.component';

@Component({
  selector: 'app-planning-detail',
  standalone: true,
  imports: [
    DatePipe,
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './planning-detail.component.html',
  styleUrls: ['./planning-detail.component.scss'],
})
export class PlanningDetailComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);
  private readonly planningService = inject(PlanningService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly planning = signal<BimonthlyPlanning | null>(null);
  protected readonly aiContents = signal<PlanningAiContent[]>([]);
  protected readonly aiInteractions = signal<PlanningAiInteraction[]>([]);
  protected readonly aiVersions = signal<PlanningAiContentVersion[]>([]);
  protected readonly libraryItems = signal<PedagogicalContentLibraryItem[]>([]);
  protected readonly selectedAiContentId = signal<string | null>(null);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingAi = signal(false);
  protected readonly isLoadingLibrary = signal(false);
  protected readonly isGeneratingAi = signal(false);
  protected readonly isSavingAi = signal(false);
  protected readonly isSavingStatus = signal(false);
  protected readonly statusOptions = [
    { value: 'RASCUNHO', label: 'Rascunho' },
    { value: 'EM_ANALISE', label: 'Em análise' },
    { value: 'APROVADO', label: 'Aprovado' },
    { value: 'REPROVADO', label: 'Reprovado' },
  ];
  protected readonly aiTypeOptions = [
    { value: 'PLANO_BIMESTRAL', label: 'Plano bimestral' },
    { value: 'PLANO_AULA', label: 'Plano de aula' },
    { value: 'ATIVIDADE', label: 'Atividade' },
    { value: 'PROVA', label: 'Prova' },
    { value: 'QUESTOES', label: 'Questões' },
    { value: 'RESUMO', label: 'Resumo' },
    { value: 'MATERIAL_APOIO', label: 'Material de apoio' },
    { value: 'RUBRICA', label: 'Rubrica' },
  ];

  protected readonly statusForm = this.fb.nonNullable.group({
    status: ['RASCUNHO'],
  });
  protected readonly aiForm = this.fb.nonNullable.group({
    promptProfessor: ['', [Validators.required]],
    tipoConteudo: ['PLANO_BIMESTRAL', [Validators.required]],
    titulo: [''],
    reutilizavel: ['true'],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.snackBar.open('Planejamento não informado.', 'Fechar', { duration: 3000 });
      this.back();
      return;
    }

    this.loadPlanning(id);
  }

  protected back(): void {
    this.router.navigate(['/planning']);
  }

  protected saveStatus(): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    this.isSavingStatus.set(true);
    this.planningService.alterarStatus(data.id, this.statusForm.getRawValue().status).subscribe({
      next: planning => {
        this.planning.set(planning);
        this.statusForm.patchValue({ status: planning.status || 'RASCUNHO' }, { emitEvent: false });
        this.isSavingStatus.set(false);
        this.snackBar.open('Status atualizado.', 'Fechar', { duration: 3000 });
      },
      error: (error: unknown) => {
        this.isSavingStatus.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível alterar status.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected openLessonDialog(): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    const dialogRef = this.dialog.open<PlanningLessonDialogComponent, undefined, BimonthlyPlanningLessonInput>(
      PlanningLessonDialogComponent,
      {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }

      this.planningService.adicionarAulaPrevista(data.id, payload).subscribe({
        next: () => {
          this.snackBar.open('Aula prevista adicionada.', 'Fechar', { duration: 3000 });
          this.loadPlanning(data.id);
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível adicionar aula prevista.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  protected openAssessmentDialog(): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    const dialogRef = this.dialog.open<PlanningAssessmentDialogComponent, undefined, BimonthlyPlanningAssessmentInput>(
      PlanningAssessmentDialogComponent,
      {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }

      this.planningService.adicionarAvaliacaoPrevista(data.id, payload).subscribe({
        next: () => {
          this.snackBar.open('Avaliação prevista adicionada.', 'Fechar', { duration: 3000 });
          this.loadPlanning(data.id);
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível adicionar avaliação prevista.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  protected generateAiContent(): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    if (this.aiForm.invalid) {
      this.aiForm.markAllAsTouched();
      return;
    }

    const raw = this.aiForm.getRawValue();
    this.isGeneratingAi.set(true);
    this.planningService
      .gerarConteudoIA(data.id, {
        promptProfessor: raw.promptProfessor.trim(),
        tipoConteudo: raw.tipoConteudo,
        titulo: raw.titulo.trim() || null,
        reutilizavel: raw.reutilizavel === 'true',
      })
      .subscribe({
        next: content => {
          this.isGeneratingAi.set(false);
          this.selectedAiContentId.set(content.id);
          this.aiForm.patchValue({ promptProfessor: '', titulo: '' }, { emitEvent: false });
          this.snackBar.open('Conteúdo gerado.', 'Fechar', { duration: 3000 });
          this.loadAiData(data.id, content.id);
        },
        error: (error: unknown) => {
          this.isGeneratingAi.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível gerar conteúdo.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected selectAiContent(content: PlanningAiContent): void {
    this.selectedAiContentId.set(content.id);
    this.loadAiVersions(content.id);
  }

  protected isSelectedAiContent(content: PlanningAiContent): boolean {
    return this.selectedAiContentId() === content.id;
  }

  protected openAiVersionDialog(content: PlanningAiContent): void {
    const dialogRef = this.dialog.open<
      PlanningAiVersionDialogComponent,
      PlanningAiVersionDialogData,
      PlanningAiVersionInput
    >(PlanningAiVersionDialogComponent, {
      width: '820px',
      maxWidth: '95vw',
      disableClose: true,
      data: {
        conteudoAtual: content.conteudo,
      },
    });

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }

      this.isSavingAi.set(true);
      this.planningService.criarVersaoIA(content.id, payload).subscribe({
        next: () => {
          this.isSavingAi.set(false);
          this.snackBar.open('Versão criada.', 'Fechar', { duration: 3000 });
          this.reloadAiAfterMutation(content.id);
        },
        error: (error: unknown) => {
          this.isSavingAi.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível criar versão.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  protected approveAiVersion(content: PlanningAiContent, version: PlanningAiContentVersion, publish: boolean): void {
    this.isSavingAi.set(true);
    this.planningService
      .aprovarVersaoIA(content.id, {
        numeroVersao: version.numeroVersao,
        publicarBiblioteca: publish,
      })
      .subscribe({
        next: () => {
          this.isSavingAi.set(false);
          this.snackBar.open(
            publish ? 'Versão aprovada e publicada.' : 'Versão aprovada.',
            'Fechar',
            { duration: 3000 },
          );
          this.reloadAiAfterMutation(content.id);
        },
        error: (error: unknown) => {
          this.isSavingAi.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível aprovar versão.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected publishAiContent(content: PlanningAiContent): void {
    this.isSavingAi.set(true);
    this.planningService.publicarBibliotecaIA(content.id).subscribe({
      next: () => {
        this.isSavingAi.set(false);
        this.snackBar.open('Conteúdo publicado na biblioteca.', 'Fechar', { duration: 3000 });
        const data = this.planning();
        if (data) {
          this.loadPlanningLibrary(data);
        }
      },
      error: (error: unknown) => {
        this.isSavingAi.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível publicar conteúdo.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected reuseLibraryContent(item: PedagogicalContentLibraryItem): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    this.isSavingAi.set(true);
    this.planningService
      .atualizar(data.id, {
        professorTurmaDisciplinaId: data.professorTurmaDisciplinaId,
        periodoAvaliativoId: data.periodoAvaliativoId ?? null,
        titulo: data.titulo,
        temaPrincipal: data.temaPrincipal,
        descricaoInicial: data.descricaoInicial,
        objetivoGeral: data.objetivoGeral ?? null,
        observacaoProfessor: data.observacaoProfessor ?? null,
        conteudoFinalAprovado: item.conteudo,
        reutilizavel: data.reutilizavel,
        criadoComAuxilioIA: true,
      })
      .subscribe({
        next: planning => {
          this.isSavingAi.set(false);
          this.planning.set(planning);
          this.snackBar.open('Conteúdo reaproveitado no planejamento.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.isSavingAi.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível reaproveitar conteúdo.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected applyAiContentToPlanning(content: PlanningAiContent): void {
    const data = this.planning();
    if (!data || !content.aprovadoPeloProfessor) {
      return;
    }

    this.isSavingAi.set(true);
    this.planningService
      .atualizar(data.id, {
        professorTurmaDisciplinaId: data.professorTurmaDisciplinaId,
        periodoAvaliativoId: data.periodoAvaliativoId ?? null,
        titulo: data.titulo,
        temaPrincipal: data.temaPrincipal,
        descricaoInicial: data.descricaoInicial,
        objetivoGeral: data.objetivoGeral ?? null,
        observacaoProfessor: data.observacaoProfessor ?? null,
        conteudoFinalAprovado: content.conteudo,
        reutilizavel: data.reutilizavel,
        criadoComAuxilioIA: true,
      })
      .subscribe({
        next: planning => {
          this.isSavingAi.set(false);
          this.planning.set(planning);
          this.snackBar.open('Conteúdo aplicado ao planejamento.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.isSavingAi.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível aplicar conteúdo ao planejamento.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected statusLabel(status?: string | null, description?: string | null): string {
    return description || this.statusOptions.find(option => option.value === status)?.label || status || 'Sem status';
  }

  protected assessmentTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      PROVA: 'Prova',
      TRABALHO: 'Trabalho',
      ATIVIDADE: 'Atividade',
      SEMINARIO: 'Seminário',
      RECUPERACAO: 'Recuperação',
    };
    return labels[type] ?? type;
  }

  protected aiTypeLabel(type?: string | null, description?: string | null): string {
    return description || this.aiTypeOptions.find(option => option.value === type)?.label || type || 'Sem tipo';
  }

  protected contentStatusLabel(status?: string | null, description?: string | null): string {
    return description || status || 'Sem status';
  }

  protected selectedAiContent(): PlanningAiContent | null {
    const id = this.selectedAiContentId();
    return this.aiContents().find(content => content.id === id) ?? null;
  }

  private loadPlanning(id: string): void {
    this.isLoading.set(true);
    this.planningService.buscarPorId(id).subscribe({
      next: planning => {
        this.planning.set(planning);
        this.statusForm.patchValue({ status: planning.status || 'RASCUNHO' }, { emitEvent: false });
        this.isLoading.set(false);
        this.loadAiData(planning.id);
        this.loadPlanningLibrary(planning);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar planejamento.'),
          'Fechar',
          { duration: 4000 },
        );
        this.back();
      },
    });
  }

  private loadAiData(planningId: string, preferredContentId?: string): void {
    this.isLoadingAi.set(true);
    forkJoin({
      contents: this.planningService.listarConteudosIA(planningId),
      interactions: this.planningService.listarInteracoesIA(planningId),
    }).subscribe({
      next: ({ contents, interactions }) => {
        this.aiContents.set(contents);
        this.aiInteractions.set(interactions);
        const selectedId = preferredContentId || this.selectedAiContentId() || contents[0]?.id || null;
        this.selectedAiContentId.set(selectedId);
        this.isLoadingAi.set(false);

        if (selectedId) {
          this.loadAiVersions(selectedId);
        } else {
          this.aiVersions.set([]);
        }
      },
      error: (error: unknown) => {
        this.isLoadingAi.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar conteúdos de IA.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private loadAiVersions(contentId: string): void {
    this.planningService.listarVersoesIA(contentId).subscribe({
      next: versions => this.aiVersions.set(versions),
      error: (error: unknown) => {
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar versões.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private loadPlanningLibrary(planning: BimonthlyPlanning): void {
    this.isLoadingLibrary.set(true);
    this.planningService
      .listarBiblioteca({
        disciplinaId: planning.disciplinaId,
        tema: planning.temaPrincipal,
      })
      .subscribe({
        next: items => {
          this.libraryItems.set(items);
          this.isLoadingLibrary.set(false);
        },
        error: (error: unknown) => {
          this.isLoadingLibrary.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível carregar biblioteca pedagógica.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  private reloadAiAfterMutation(contentId: string): void {
    const data = this.planning();
    if (!data) {
      return;
    }

    this.loadAiData(data.id, contentId);
    this.loadPlanningLibrary(data);
  }
}
