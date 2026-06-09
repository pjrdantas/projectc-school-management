import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
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
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Enrollment } from '../../../matricula/models/enrollment.model';
import { EnrollmentService } from '../../../matricula/services/enrollment.service';
import { StudentFrequency, TeacherFrequency } from '../../models/frequency.model';
import { Lesson } from '../../models/lesson.model';
import { LessonsService } from '../../services/lessons.service';

interface StudentFrequencyDraft {
  situacao: string;
  justificativa: string;
}

interface StudentAttendanceRow {
  enrollment: Enrollment;
  studentName: string;
  frequency?: StudentFrequency;
  draft: StudentFrequencyDraft;
}

@Component({
  selector: 'app-lesson-detail',
  standalone: true,
  imports: [
    DatePipe,
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './lesson-detail.component.html',
  styleUrls: ['./lesson-detail.component.scss'],
})
export class LessonDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly lessonsService = inject(LessonsService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly studentsService = inject(StudentsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly lesson = signal<Lesson | null>(null);
  protected readonly teacherFrequencies = signal<TeacherFrequency[]>([]);
  protected readonly studentFrequencies = signal<StudentFrequency[]>([]);
  protected readonly enrollments = signal<Enrollment[]>([]);
  protected readonly students = signal<Student[]>([]);
  protected readonly studentDrafts = signal<Record<string, StudentFrequencyDraft>>({});
  protected readonly isLoading = signal(false);
  protected readonly isSavingTeacherFrequency = signal(false);
  protected readonly savingStudentEnrollmentId = signal<string | null>(null);

  protected readonly statusOptions = [
    { value: 'PRESENTE', label: 'Presente' },
    { value: 'FALTA', label: 'Falta' },
    { value: 'FALTA_JUSTIFICADA', label: 'Falta justificada' },
  ];

  protected readonly teacherFrequencyForm = this.fb.nonNullable.group({
    presente: ['true'],
    justificativa: [''],
  });

  protected readonly teacherFrequency = computed(() => this.teacherFrequencies()[0] ?? null);
  protected readonly studentRows = computed<StudentAttendanceRow[]>(() => {
    const frequencies = new Map(this.studentFrequencies().map(item => [item.matriculaId, item]));
    return this.eligibleEnrollments()
      .map(enrollment => ({
        enrollment,
        studentName: this.studentName(enrollment.alunoId),
        frequency: frequencies.get(enrollment.id),
        draft: this.studentDrafts()[enrollment.id] ?? { situacao: 'PRESENTE', justificativa: '' },
      }))
      .sort((left, right) => left.studentName.localeCompare(right.studentName, 'pt-BR'));
  });

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

  protected saveTeacherFrequency(): void {
    const data = this.lesson();
    if (!data || this.teacherFrequency()) {
      return;
    }

    const raw = this.teacherFrequencyForm.getRawValue();
    this.isSavingTeacherFrequency.set(true);
    this.lessonsService
      .registrarFrequenciaProfessor(data.id, {
        presente: raw.presente === 'true',
        justificativa: raw.justificativa.trim() || undefined,
      })
      .subscribe({
        next: frequency => {
          this.teacherFrequencies.set([frequency]);
          this.isSavingTeacherFrequency.set(false);
          this.snackBar.open('Frequência do professor registrada.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.isSavingTeacherFrequency.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível registrar frequência do professor.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected saveStudentFrequency(row: StudentAttendanceRow): void {
    const data = this.lesson();
    if (!data || row.frequency) {
      return;
    }

    this.savingStudentEnrollmentId.set(row.enrollment.id);
    this.lessonsService
      .registrarFrequenciaAluno(data.id, {
        matriculaId: row.enrollment.id,
        situacao: row.draft.situacao,
        justificativa: row.draft.justificativa.trim() || undefined,
      })
      .subscribe({
        next: frequency => {
          this.studentFrequencies.update(items => [...items, frequency]);
          this.savingStudentEnrollmentId.set(null);
          this.snackBar.open('Frequência do aluno registrada.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.savingStudentEnrollmentId.set(null);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível registrar frequência do aluno.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected updateStudentDraft(enrollmentId: string, changes: Partial<StudentFrequencyDraft>): void {
    this.studentDrafts.update(current => ({
      ...current,
      [enrollmentId]: {
        situacao: current[enrollmentId]?.situacao ?? 'PRESENTE',
        justificativa: current[enrollmentId]?.justificativa ?? '',
        ...changes,
      },
    }));
  }

  protected frequencyStatusLabel(status: string): string {
    return this.statusOptions.find(option => option.value === status)?.label ?? status;
  }

  private loadLesson(id: string): void {
    this.isLoading.set(true);
    this.lessonsService.buscarPorId(id).subscribe({
      next: lesson => {
        this.lesson.set(lesson);
        this.loadLessonContext(lesson);
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

  private loadLessonContext(lesson: Lesson): void {
    forkJoin({
      teacherFrequencies: this.lessonsService.listarFrequenciaProfessor(lesson.id),
      studentFrequencies: this.lessonsService.listarFrequenciasAlunos(lesson.id),
      enrollments: this.enrollmentService.search({ turmaId: lesson.turmaId }),
      students: this.studentsService.syncFromApi(),
    }).subscribe({
      next: ({ teacherFrequencies, studentFrequencies, enrollments, students }) => {
        this.teacherFrequencies.set(teacherFrequencies);
        this.studentFrequencies.set(studentFrequencies);
        this.enrollments.set(enrollments);
        this.students.set(students);
        this.studentDrafts.set(
          this.eligibleEnrollments().reduce<Record<string, StudentFrequencyDraft>>((drafts, enrollment) => {
            drafts[enrollment.id] = { situacao: 'PRESENTE', justificativa: '' };
            return drafts;
          }, {}),
        );
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar frequências da aula.'),
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
