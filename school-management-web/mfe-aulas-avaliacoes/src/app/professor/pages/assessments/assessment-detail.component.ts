import { DatePipe, DecimalPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { Student } from '../../../aluno/models/student.model';
import { StudentsService } from '../../../aluno/services/students.service';
import { Enrollment } from '../../../compartilhado/enrollment/enrollment.model';
import { EnrollmentService } from '../../../compartilhado/enrollment/enrollment.service';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Assessment, StudentGrade } from '../../models/assessment.model';
import { AssessmentsService } from '../../services/assessments.service';

interface GradeDraft {
  nota: string;
  observacao: string;
}

interface GradeRow {
  enrollment: Enrollment;
  studentName: string;
  grade?: StudentGrade;
  draft: GradeDraft;
}

@Component({
  selector: 'app-assessment-detail',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    NgFor,
    NgIf,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './assessment-detail.component.html',
  styleUrls: ['./assessment-detail.component.scss'],
})
export class AssessmentDetailComponent implements OnInit {
  private readonly assessmentsService = inject(AssessmentsService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly studentsService = inject(StudentsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly assessment = signal<Assessment | null>(null);
  protected readonly grades = signal<StudentGrade[]>([]);
  protected readonly enrollments = signal<Enrollment[]>([]);
  protected readonly students = signal<Student[]>([]);
  protected readonly gradeDrafts = signal<Record<string, GradeDraft>>({});
  protected readonly isLoading = signal(false);
  protected readonly savingEnrollmentId = signal<string | null>(null);

  protected readonly gradeRows = computed<GradeRow[]>(() => {
    const grades = new Map(this.grades().map(item => [item.matriculaId, item]));
    return this.eligibleEnrollments()
      .map(enrollment => ({
        enrollment,
        studentName: this.studentName(enrollment.alunoId),
        grade: grades.get(enrollment.id),
        draft: this.gradeDrafts()[enrollment.id] ?? { nota: '', observacao: '' },
      }))
      .sort((left, right) => left.studentName.localeCompare(right.studentName, 'pt-BR'));
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.snackBar.open('Avaliação não informada.', 'Fechar', { duration: 3000 });
      this.back();
      return;
    }

    this.loadAssessment(id);
  }

  protected back(): void {
    this.router.navigate(['/assessments']);
  }

  protected saveGrade(row: GradeRow): void {
    const assessment = this.assessment();
    if (!assessment || row.grade) {
      return;
    }

    const nota = Number(row.draft.nota);
    if (Number.isNaN(nota) || nota < 0 || nota > Number(assessment.valorMaximo)) {
      this.snackBar.open('Informe uma nota válida dentro do valor máximo.', 'Fechar', { duration: 3500 });
      return;
    }

    this.savingEnrollmentId.set(row.enrollment.id);
    this.assessmentsService
      .lancarNota(assessment.id, {
        matriculaId: row.enrollment.id,
        nota,
        observacao: row.draft.observacao.trim() || null,
      })
      .subscribe({
        next: grade => {
          this.grades.update(items => [...items, grade]);
          this.savingEnrollmentId.set(null);
          this.snackBar.open('Nota lançada com sucesso.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.savingEnrollmentId.set(null);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível lançar nota.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected updateGradeDraft(enrollmentId: string, changes: Partial<GradeDraft>): void {
    this.gradeDrafts.update(current => ({
      ...current,
      [enrollmentId]: {
        nota: current[enrollmentId]?.nota ?? '',
        observacao: current[enrollmentId]?.observacao ?? '',
        ...changes,
      },
    }));
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

  private loadAssessment(id: string): void {
    this.isLoading.set(true);
    this.assessmentsService.buscarPorId(id).subscribe({
      next: assessment => {
        this.assessment.set(assessment);
        this.loadAssessmentContext(assessment);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar avaliação.'),
          'Fechar',
          { duration: 4000 },
        );
        this.back();
      },
    });
  }

  private loadAssessmentContext(assessment: Assessment): void {
    forkJoin({
      grades: this.assessmentsService.listarNotas(assessment.id),
      enrollments: this.enrollmentService.search({ turmaId: assessment.turmaId }),
      students: this.studentsService.syncFromApi(),
    }).subscribe({
      next: ({ grades, enrollments, students }) => {
        this.grades.set(grades);
        this.enrollments.set(enrollments);
        this.students.set(students);
        this.gradeDrafts.set(
          this.eligibleEnrollments().reduce<Record<string, GradeDraft>>((drafts, enrollment) => {
            drafts[enrollment.id] = { nota: '', observacao: '' };
            return drafts;
          }, {}),
        );
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar notas da avaliação.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private eligibleEnrollments(): Enrollment[] {
    const ignoredStatuses = new Set(['CANCELADA', 'INDEFERIDA', 'TRANSFERIDO', 'TRANSFERIDA', 'CONCLUIDA']);
    return this.enrollments().filter(item => !ignoredStatuses.has(item.status));
  }

  private studentName(studentId: string): string {
    return this.students().find(student => student.id === studentId)?.nomeCompleto ?? 'Aluno não encontrado';
  }
}
