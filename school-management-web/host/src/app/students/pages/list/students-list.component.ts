import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
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
    RouterLink,
    AsyncPipe,
    NgIf,
    NgFor,
  ],
  templateUrl: './students-list.component.html',
  styleUrls: ['./students-list.component.scss'],
})
export class StudentsListComponent {
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly studentsService = inject(StudentsService);

  protected readonly students$ = this.studentsService.students$;

  private readonly studentsSignal = toSignal(this.studentsService.students$, {
    initialValue: this.studentsService.list(),
  });

  protected readonly pageSizeOptions = [5, 10, 20];
  protected readonly pageSize = signal(this.pageSizeOptions[0]);
  protected readonly pageIndex = signal(0);

  protected readonly totalStudents = computed(() => this.studentsSignal().length);

  protected readonly pagedStudents = computed(() => {
    const students = this.studentsSignal();
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    return students.slice(start, end);
  });

  protected goToNewStudent(): void {
    this.router.navigate(['/students/new']);
  }

  protected remover(id: string): void {
    const removed = this.studentsService.remove(id);
    if (removed) {
      this.snackBar.open('Aluno removido com sucesso.', 'Fechar', { duration: 3000 });

      const currentPageStart = this.pageIndex() * this.pageSize();
      const total = this.totalStudents();
      if (total > 0 && currentPageStart >= total) {
        this.pageIndex.set(Math.max(Math.ceil(total / this.pageSize()) - 1, 0));
      }
    }
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
