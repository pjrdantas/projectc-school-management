import { DatePipe, NgFor, NgIf } from '@angular/common';
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
import { Assessment, AssessmentInput } from '../../models/assessment.model';
import { ProfessorAllocation } from '../../models/teacher.model';
import { AssessmentsService } from '../../services/assessments.service';
import { TeachersService } from '../../services/teachers.service';
import { AssessmentCreateDialogComponent, AssessmentCreateDialogData } from './assessment-create-dialog.component';

interface AssessmentFilters {
  professorId: string;
  turmaId: string;
  disciplinaId: string;
  tipoAvaliacao: string;
}

@Component({
  selector: 'app-assessments-list',
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
    MatPaginatorModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './assessments-list.component.html',
  styleUrls: ['./assessments-list.component.scss'],
})
export class AssessmentsListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly assessmentsService = inject(AssessmentsService);
  private readonly teachersService = inject(TeachersService);

  protected readonly assessments = signal<Assessment[]>([]);
  protected readonly allocations = signal<ProfessorAllocation[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingAllocations = signal(false);
  protected readonly filters = signal<AssessmentFilters>({
    professorId: '',
    turmaId: '',
    disciplinaId: '',
    tipoAvaliacao: '',
  });
  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly filterForm = this.fb.nonNullable.group({
    professorId: [''],
    turmaId: [''],
    disciplinaId: [''],
    tipoAvaliacao: [''],
  });

  protected readonly professorOptions = computed(() =>
    this.uniqueOptions(this.assessments(), 'professorId', 'professorNome'),
  );
  protected readonly classOptions = computed(() =>
    this.uniqueOptions(this.assessments(), 'turmaId', 'turmaNome'),
  );
  protected readonly disciplineOptions = computed(() =>
    this.uniqueOptions(this.assessments(), 'disciplinaId', 'disciplinaNome'),
  );
  protected readonly typeOptions = computed(() =>
    [...new Set(this.assessments().map(item => item.tipoAvaliacao))]
      .map(value => ({ value, label: this.assessmentTypeLabel(value) }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR')),
  );
  protected readonly filteredAssessments = computed(() => {
    const filters = this.filters();
    return this.assessments()
      .filter(item => !filters.professorId || item.professorId === filters.professorId)
      .filter(item => !filters.turmaId || item.turmaId === filters.turmaId)
      .filter(item => !filters.disciplinaId || item.disciplinaId === filters.disciplinaId)
      .filter(item => !filters.tipoAvaliacao || item.tipoAvaliacao === filters.tipoAvaliacao)
      .sort((left, right) => {
        const leftDate = left.dataAplicacao ?? '';
        const rightDate = right.dataAplicacao ?? '';
        const dateOrder = rightDate.localeCompare(leftDate);
        if (dateOrder !== 0) {
          return dateOrder;
        }
        return left.titulo.localeCompare(right.titulo, 'pt-BR');
      });
  });
  protected readonly pagedAssessments = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredAssessments().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.filterForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => {
        this.filters.set({
          professorId: value.professorId ?? '',
          turmaId: value.turmaId ?? '',
          disciplinaId: value.disciplinaId ?? '',
          tipoAvaliacao: value.tipoAvaliacao ?? '',
        });
        this.pageIndex.set(0);
      });

    this.loadAssessments();
    this.loadAllocations();
  }

  protected openCreateDialog(): void {
    const dialogRef = this.dialog.open<AssessmentCreateDialogComponent, AssessmentCreateDialogData, AssessmentInput>(
      AssessmentCreateDialogComponent,
      {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
        data: {
          allocations: this.allocations(),
        },
      },
    );

    dialogRef.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }

      this.assessmentsService.criar(payload).subscribe({
        next: () => {
          this.snackBar.open('Avaliação cadastrada com sucesso.', 'Fechar', { duration: 3000 });
          this.loadAssessments();
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível cadastrar avaliação.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  protected onDetails(id: string): void {
    this.router.navigate(['/assessments', id]);
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
      tipoAvaliacao: '',
    });
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

  private loadAssessments(): void {
    this.isLoading.set(true);
    this.assessmentsService.listar().subscribe({
      next: assessments => {
        this.assessments.set(assessments);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar avaliações.'),
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
    assessments: Assessment[],
    idKey: T,
    labelKey: T extends 'professorId'
      ? 'professorNome'
      : T extends 'turmaId'
        ? 'turmaNome'
        : 'disciplinaNome',
  ): Array<{ id: string; label: string }> {
    const options = new Map<string, string>();
    assessments.forEach(item => options.set(item[idKey], item[labelKey]));
    return [...options.entries()]
      .map(([id, label]) => ({ id, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR'));
  }
}
