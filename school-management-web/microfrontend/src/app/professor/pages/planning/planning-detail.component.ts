import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import {
  BimonthlyPlanning,
  BimonthlyPlanningAssessmentInput,
  BimonthlyPlanningLessonInput,
} from '../../models/planning.model';
import { PlanningService } from '../../services/planning.service';
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
  protected readonly isLoading = signal(false);
  protected readonly isSavingStatus = signal(false);
  protected readonly statusOptions = [
    { value: 'RASCUNHO', label: 'Rascunho' },
    { value: 'EM_ANALISE', label: 'Em análise' },
    { value: 'APROVADO', label: 'Aprovado' },
    { value: 'REPROVADO', label: 'Reprovado' },
  ];

  protected readonly statusForm = this.fb.nonNullable.group({
    status: ['RASCUNHO'],
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

  private loadPlanning(id: string): void {
    this.isLoading.set(true);
    this.planningService.buscarPorId(id).subscribe({
      next: planning => {
        this.planning.set(planning);
        this.statusForm.patchValue({ status: planning.status || 'RASCUNHO' }, { emitEvent: false });
        this.isLoading.set(false);
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
}
