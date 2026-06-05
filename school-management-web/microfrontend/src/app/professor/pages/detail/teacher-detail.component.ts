import { DatePipe, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Professor } from '../../models/teacher.model';
import { TeachersService } from '../../services/teachers.service';

@Component({
  selector: 'app-teacher-detail',
  standalone: true,
  imports: [
    DatePipe,
    NgIf,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './teacher-detail.component.html',
  styleUrls: ['./teacher-detail.component.scss'],
})
export class TeacherDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly teachersService = inject(TeachersService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly professor = signal<Professor | null>(null);
  protected readonly isLoading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.snackBar.open('Professor não informado.', 'Fechar', { duration: 3000 });
      this.back();
      return;
    }

    this.carregar(id);
  }

  protected back(): void {
    this.router.navigate(['/teachers']);
  }

  private carregar(id: string): void {
    this.isLoading.set(true);
    this.teachersService.buscarPorId(id).subscribe({
      next: (professor) => {
        this.isLoading.set(false);
        this.professor.set(professor);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar professor.'),
          'Fechar',
          { duration: 4000 },
        );
        this.back();
      },
    });
  }
}
