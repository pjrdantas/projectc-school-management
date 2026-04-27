import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { StudentsService } from '../../services/students.service';

@Component({
  selector: 'app-students-list',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink,
    AsyncPipe,
    NgIf,
    NgFor,
  ],
  templateUrl: './students-list.component.html',
  styleUrls: ['./students-list.component.scss'],
})
export class StudentsListComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly studentsService = inject(StudentsService);

  protected readonly students$ = this.studentsService.students$;

  private readonly studentsSignal = toSignal(this.studentsService.students$, {
    initialValue: this.studentsService.list(),
  });

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);
  protected readonly searchTerm = signal('');

  protected readonly filteredStudents = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const students = this.studentsSignal();
    const filtered = !term
      ? students
      : students.filter(student => student.nomeCompleto.toLowerCase().includes(term));

    return [...filtered].sort((a, b) =>
      a.nomeCompleto.localeCompare(b.nomeCompleto, 'pt-BR', { sensitivity: 'base' }),
    );
  });

  protected readonly totalStudents = computed(() => this.filteredStudents().length);

  protected readonly pagedStudents = computed(() => {
    const students = this.filteredStudents();
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    return students.slice(start, end);
  });

  ngOnInit(): void {
    this.studentsService.syncFromApi().subscribe({
      error: () => {
        this.snackBar.open('Não foi possível carregar alunos do backend.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected goToNewStudent(): void {
    this.router.navigate(['/students/new']);
  }

  protected onSearchTermChange(value: string): void {
    const term = value.trim();
    this.searchTerm.set(value);
    this.pageIndex.set(0);

    this.studentsService.syncFromApi(term || undefined).subscribe({
      error: () => {
        this.snackBar.open('Não foi possível buscar alunos no backend.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected clearSearch(): void {
    this.searchTerm.set('');
    this.pageIndex.set(0);
    this.studentsService.syncFromApi().subscribe({
      error: () => {
        this.snackBar.open('Não foi possível recarregar alunos no backend.', 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected remover(id: string): void {
    this.studentsService.removeOnApi(id).subscribe({
      next: () => {
        this.snackBar.open('Aluno removido com sucesso.', 'Fechar', { duration: 3000 });

        const currentPageStart = this.pageIndex() * this.pageSize();
        const total = this.totalStudents();
        if (total > 0 && currentPageStart >= total) {
          this.pageIndex.set(Math.max(Math.ceil(total / this.pageSize()) - 1, 0));
        }
      },
      error: () => {
        this.snackBar.open('Erro ao excluir aluno no backend.', 'Fechar', { duration: 4000 });
      },
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  protected formatCpf(cpf: string): string {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  protected formatData(value: string): string {
    const [yyyy, mm, dd] = value.split('-');
    return `${dd}/${mm}/${yyyy}`;
  }
}
