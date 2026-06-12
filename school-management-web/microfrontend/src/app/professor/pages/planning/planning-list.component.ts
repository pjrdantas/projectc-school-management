import { NgFor, NgIf } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin, map } from 'rxjs';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { BimonthlyPlanning, BimonthlyPlanningInput } from '../../models/planning.model';
import { ProfessorAllocation } from '../../models/teacher.model';
import { PlanningService } from '../../services/planning.service';
import { TeachersService } from '../../services/teachers.service';
import { PlanningDialogComponent, PlanningDialogData } from './planning-dialog.component';

interface PlanningFilters {
  professorId: string;
  turmaId: string;
  disciplinaId: string;
  periodoAvaliativoId: string;
  status: string;
}

@Component({
  selector: 'app-planning-list',
  standalone: true,
  imports: [
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './planning-list.component.html',
  styleUrls: ['./planning-list.component.scss'],
})
export class PlanningListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);
  private readonly planningService = inject(PlanningService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly teachersService = inject(TeachersService);

  protected readonly plannings = signal<BimonthlyPlanning[]>([]);
  protected readonly allocations = signal<ProfessorAllocation[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingAllocations = signal(false);
  protected readonly filters = signal<PlanningFilters>({
    professorId: '',
    turmaId: '',
    disciplinaId: '',
    periodoAvaliativoId: '',
    status: '',
  });
  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly filterForm = this.fb.nonNullable.group({
    professorId: [''],
    turmaId: [''],
    disciplinaId: [''],
    periodoAvaliativoId: [''],
    status: [''],
  });

  protected readonly professorOptions = computed(() =>
    this.uniqueOptions(this.plannings(), 'professorId', 'professorNome'),
  );
  protected readonly classOptions = computed(() =>
    this.uniqueOptions(this.plannings(), 'turmaId', 'turmaNome'),
  );
  protected readonly disciplineOptions = computed(() =>
    this.uniqueOptions(this.plannings(), 'disciplinaId', 'disciplinaNome'),
  );
  protected readonly periodOptions = computed(() => {
    const options = new Map<string, string>();
    this.plannings().forEach(item => {
      if (item.periodoAvaliativoId) {
        options.set(item.periodoAvaliativoId, item.periodoAvaliativoNome || 'Período avaliativo');
      }
    });
    return [...options.entries()]
      .map(([id, label]) => ({ id, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR'));
  });
  protected readonly statusOptions = computed(() => {
    const options = new Map<string, string>();
    this.plannings().forEach(item => {
      if (item.status) {
        options.set(item.status, this.statusLabel(item.status, item.statusDescricao));
      }
    });
    return [...options.entries()]
      .map(([value, label]) => ({ value, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR'));
  });
  protected readonly filteredPlannings = computed(() => {
    const filters = this.filters();
    return this.plannings()
      .filter(item => !filters.professorId || item.professorId === filters.professorId)
      .filter(item => !filters.turmaId || item.turmaId === filters.turmaId)
      .filter(item => !filters.disciplinaId || item.disciplinaId === filters.disciplinaId)
      .filter(item => !filters.periodoAvaliativoId || item.periodoAvaliativoId === filters.periodoAvaliativoId)
      .filter(item => !filters.status || item.status === filters.status)
      .sort((left, right) => {
        const createdOrder = (right.createdAt ?? '').localeCompare(left.createdAt ?? '');
        if (createdOrder !== 0) {
          return createdOrder;
        }
        return left.titulo.localeCompare(right.titulo, 'pt-BR');
      });
  });
  protected readonly pagedPlannings = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredPlannings().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.filterForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => {
        this.filters.set({
          professorId: value.professorId ?? '',
          turmaId: value.turmaId ?? '',
          disciplinaId: value.disciplinaId ?? '',
          periodoAvaliativoId: value.periodoAvaliativoId ?? '',
          status: value.status ?? '',
        });
        this.pageIndex.set(0);
      });

    this.loadPlannings();
    this.loadAllocations();
  }

  protected openCreateDialog(): void {
    this.openDialog();
  }

  protected openEditDialog(planning: BimonthlyPlanning, event: Event): void {
    event.stopPropagation();
    this.openDialog(planning);
  }

  protected onDetails(id: string): void {
    this.router.navigate(['/planning', id]);
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      professorId: '',
      turmaId: '',
      disciplinaId: '',
      periodoAvaliativoId: '',
      status: '',
    });
  }

  protected statusLabel(status?: string | null, description?: string | null): string {
    const labels: Record<string, string> = {
      RASCUNHO: 'Rascunho',
      EM_ANALISE: 'Em análise',
      APROVADO: 'Aprovado',
      REPROVADO: 'Reprovado',
    };
    return description || labels[status ?? ''] || status || 'Sem status';
  }

  private openDialog(planning?: BimonthlyPlanning): void {
    const dialogRef = this.dialog.open<PlanningDialogComponent, PlanningDialogData, BimonthlyPlanningInput>(
      PlanningDialogComponent,
      {
        width: '800px',
        maxWidth: '95vw',
        disableClose: true,
        data: {
          allocations: this.allocations(),
          planning,
        },
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }

      const request = planning
        ? this.planningService.atualizar(planning.id, payload)
        : this.planningService.criar(payload);

      request.subscribe({
        next: () => {
          this.snackBar.open(
            planning ? 'Planejamento atualizado com sucesso.' : 'Planejamento cadastrado com sucesso.',
            'Fechar',
            { duration: 3000 },
          );
          this.loadPlannings();
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível salvar planejamento.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  private loadPlannings(): void {
    this.isLoading.set(true);
    this.planningService.listar().subscribe({
      next: plannings => {
        this.plannings.set(plannings);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar planejamentos.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private loadAllocations(): void {
    this.isLoadingAllocations.set(true);
    this.teachersService.listar().subscribe({
      next: professors => {
        if (professors.length === 0) {
          this.allocations.set([]);
          this.isLoadingAllocations.set(false);
          return;
        }

        forkJoin(professors.map(professor => this.teachersService.listarAlocacoes(professor.id)))
          .pipe(map(results => results.flat().filter(allocation => allocation.ativo)))
          .subscribe({
            next: allocations => {
              this.allocations.set(allocations);
              this.isLoadingAllocations.set(false);
            },
            error: () => {
              this.allocations.set([]);
              this.isLoadingAllocations.set(false);
            },
          });
      },
      error: () => {
        this.allocations.set([]);
        this.isLoadingAllocations.set(false);
      },
    });
  }

  private uniqueOptions<T extends 'professorId' | 'turmaId' | 'disciplinaId'>(
    plannings: BimonthlyPlanning[],
    idKey: T,
    labelKey: T extends 'professorId'
      ? 'professorNome'
      : T extends 'turmaId'
        ? 'turmaNome'
        : 'disciplinaNome',
  ): Array<{ id: string; label: string }> {
    const options = new Map<string, string>();
    plannings.forEach(item => options.set(item[idKey], item[labelKey]));
    return [...options.entries()]
      .map(([id, label]) => ({ id, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR'));
  }
}
