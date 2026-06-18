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
import { Lesson, LessonInput } from '../../models/lesson.model';
import { ProfessorAllocation } from '../../models/teacher.model';
import { LessonsService } from '../../services/lessons.service';
import { TeachersService } from '../../services/teachers.service';
import { LessonCreateDialogComponent, LessonCreateDialogData } from './lesson-create-dialog.component';

interface LessonFilters {
  professorId: string;
  turmaId: string;
  disciplinaId: string;
  dataAula: string;
}

@Component({
  selector: 'app-lessons-list',
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
  templateUrl: './lessons-list.component.html',
  styleUrls: ['./lessons-list.component.scss'],
})
export class LessonsListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);
  private readonly lessonsService = inject(LessonsService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly teachersService = inject(TeachersService);

  protected readonly lessons = signal<Lesson[]>([]);
  protected readonly allocations = signal<ProfessorAllocation[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingAllocations = signal(false);
  protected readonly filters = signal<LessonFilters>({
    professorId: '',
    turmaId: '',
    disciplinaId: '',
    dataAula: '',
  });
  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly filterForm = this.fb.nonNullable.group({
    professorId: [''],
    turmaId: [''],
    disciplinaId: [''],
    dataAula: [''],
  });

  protected readonly professorOptions = computed(() =>
    this.uniqueOptions(this.lessons(), 'professorId', 'professorNome'),
  );
  protected readonly classOptions = computed(() =>
    this.uniqueOptions(this.lessons(), 'turmaId', 'turmaNome'),
  );
  protected readonly disciplineOptions = computed(() =>
    this.uniqueOptions(this.lessons(), 'disciplinaId', 'disciplinaNome'),
  );
  protected readonly filteredLessons = computed(() => {
    const filters = this.filters();
    return this.lessons()
      .filter(lesson => !filters.professorId || lesson.professorId === filters.professorId)
      .filter(lesson => !filters.turmaId || lesson.turmaId === filters.turmaId)
      .filter(lesson => !filters.disciplinaId || lesson.disciplinaId === filters.disciplinaId)
      .filter(lesson => !filters.dataAula || lesson.dataAula === filters.dataAula)
      .sort((left, right) => {
        const dateOrder = right.dataAula.localeCompare(left.dataAula);
        if (dateOrder !== 0) {
          return dateOrder;
        }
        return `${left.turmaNome} ${left.disciplinaNome}`.localeCompare(
          `${right.turmaNome} ${right.disciplinaNome}`,
          'pt-BR',
        );
      });
  });
  protected readonly pagedLessons = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredLessons().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.filterForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(value => {
        this.filters.set({
          professorId: value.professorId ?? '',
          turmaId: value.turmaId ?? '',
          disciplinaId: value.disciplinaId ?? '',
          dataAula: value.dataAula ?? '',
        });
        this.pageIndex.set(0);
      });

    this.loadLessons();
    this.loadAllocations();
  }

  protected openCreateDialog(): void {
    const dialogRef = this.dialog.open<LessonCreateDialogComponent, LessonCreateDialogData, LessonInput>(
      LessonCreateDialogComponent,
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

      this.lessonsService.criar(payload).subscribe({
        next: () => {
          this.snackBar.open('Aula cadastrada com sucesso.', 'Fechar', { duration: 3000 });
          this.loadLessons();
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível cadastrar aula.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected onDetails(id: string): void {
    this.router.navigate(['/lessons', id]);
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      professorId: '',
      turmaId: '',
      disciplinaId: '',
      dataAula: '',
    });
  }

  private loadLessons(): void {
    this.isLoading.set(true);
    this.lessonsService.listar().subscribe({
      next: lessons => {
        this.lessons.set(lessons);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar aulas.'),
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
    lessons: Lesson[],
    idKey: T,
    labelKey: T extends 'professorId'
      ? 'professorNome'
      : T extends 'turmaId'
        ? 'turmaNome'
        : 'disciplinaNome',
  ): Array<{ id: string; label: string }> {
    const options = new Map<string, string>();
    lessons.forEach(lesson => options.set(lesson[idKey], lesson[labelKey]));
    return [...options.entries()]
      .map(([id, label]) => ({ id, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'pt-BR'));
  }
}
