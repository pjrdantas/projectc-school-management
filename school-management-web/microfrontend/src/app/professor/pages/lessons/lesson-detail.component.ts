import { DatePipe, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Lesson } from '../../models/lesson.model';
import { LessonsService } from '../../services/lessons.service';

@Component({
  selector: 'app-lesson-detail',
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
  templateUrl: './lesson-detail.component.html',
  styleUrls: ['./lesson-detail.component.scss'],
})
export class LessonDetailComponent implements OnInit {
  private readonly lessonsService = inject(LessonsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly lesson = signal<Lesson | null>(null);
  protected readonly isLoading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.snackBar.open('Aula não informada.', 'Fechar', { duration: 3000 });
      this.back();
      return;
    }

    this.loadLesson(id);
  }

  protected back(): void {
    this.router.navigate(['/lessons']);
  }

  private loadLesson(id: string): void {
    this.isLoading.set(true);
    this.lessonsService.buscarPorId(id).subscribe({
      next: lesson => {
        this.lesson.set(lesson);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar aula.'),
          'Fechar',
          { duration: 4000 },
        );
        this.back();
      },
    });
  }
}
