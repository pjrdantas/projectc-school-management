import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
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

  protected goToNewStudent(): void {
    this.router.navigate(['/students/new']);
  }

  protected remover(id: string): void {
    const removed = this.studentsService.remove(id);
    if (removed) {
      this.snackBar.open('Aluno removido com sucesso.', 'Fechar', { duration: 3000 });
    }
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
