import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Student } from '../../models/student.model';
import { StudentsService } from '../../services/students.service';

@Component({
  selector: 'app-students-detail',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterLink, MatSnackBarModule],
  templateUrl: './students-detail.component.html',
  styleUrls: ['./students-detail.component.scss'],
})
export class StudentsDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly studentsService = inject(StudentsService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly student = signal<Student | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/students']);
      return;
    }

    this.studentsService.fetchByIdFromApi(id).subscribe({
      next: student => this.student.set(student),
      error: () => {
        this.snackBar.open('Aluno não encontrado.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/students']);
      },
    });
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
