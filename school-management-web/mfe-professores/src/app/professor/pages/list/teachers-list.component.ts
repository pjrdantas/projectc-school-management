import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
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
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Professor, ProfessorInput } from '../../models/teacher.model';
import { TeachersService } from '../../services/teachers.service';
import { TeacherCreateDialogComponent } from './teacher-create-dialog.component';

@Component({
  selector: 'app-teachers-list',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
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
  templateUrl: './teachers-list.component.html',
  styleUrls: ['./teachers-list.component.scss'],
})
export class TeachersListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly teachersService = inject(TeachersService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  protected readonly professores = signal<Professor[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);
  protected readonly searchForm = this.fb.nonNullable.group({ termo: [''] });
  protected readonly searchTerm = signal('');

  protected readonly filteredTeachers = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    return [...this.professores()]
      .filter((professor) => {
        if (!term) return true;
        return [
          professor.nomeCompleto,
          professor.registroProfissional ?? '',
          professor.formacao ?? '',
        ].some((value) => value.toLowerCase().includes(term));
      })
      .sort((a, b) => a.nomeCompleto.localeCompare(b.nomeCompleto, 'pt-BR', { sensitivity: 'base' }));
  });
  protected readonly total = computed(() => this.filteredTeachers().length);
  protected readonly pagedTeachers = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredTeachers().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.carregar();
  }

  protected openCreateDialog(): void {
    this.dialog
      .open(TeacherCreateDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
      })
      .afterClosed()
      .subscribe((payload?: ProfessorInput) => {
        if (payload) {
          this.criar(payload);
        }
      });
  }

  protected onSearchInput(value: string): void {
    this.searchTerm.set(value);
    this.pageIndex.set(0);
  }

  protected clearSearch(): void {
    this.searchForm.reset();
    this.searchTerm.set('');
    this.pageIndex.set(0);
  }

  protected onDetails(id: string): void {
    this.router.navigate(['/teachers', id]);
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  private carregar(): void {
    this.isLoading.set(true);
    this.teachersService.listar().subscribe({
      next: (professores) => {
        this.isLoading.set(false);
        this.professores.set(professores);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar professores.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private criar(payload: ProfessorInput): void {
    this.isLoading.set(true);
    this.teachersService.criar(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearSearch();
        this.snackBar.open('Professor cadastrado.', 'Fechar', { duration: 2500 });
        this.carregar();
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível cadastrar professor.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }
}
