import { NgFor, NgIf } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AcademicService } from '../../../academic/services/academic.service';
import { Student } from '../../../students/models/student.model';
import { StudentsService } from '../../../students/services/students.service';
import { ApiErrorResponse, Enrollment } from '../../models/enrollment.model';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-enrollment-new',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './enrollment-new.component.html',
  styleUrls: ['./enrollment-new.component.scss'],
})
export class EnrollmentNewComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly studentsService = inject(StudentsService);
  private readonly academicService = inject(AcademicService);

  protected readonly isLoading = signal(false);
  protected readonly enrollments = signal<Enrollment[]>([]);
  protected readonly students = signal<Student[]>(this.studentsService.list());
  protected readonly statuses = ['ATIVA', 'TRANCADA', 'CANCELADA', 'CONCLUIDA'];

  protected readonly form = this.fb.nonNullable.group({
    alunoNome: ['', [Validators.required]],
    turmaId: ['', [Validators.required]],
    periodoLetivoId: ['', [Validators.required]],
    status: ['ATIVA', [Validators.required]],
  });

  protected readonly editingId = signal<string | null>(null);
  protected readonly selectedAlunoId = signal<string | null>(null);

  protected readonly filteredStudents = computed(() =>
    this.students()
      .filter(s =>
        s.nomeCompleto
          .toLowerCase()
          .includes(this.form.controls.alunoNome.value.toLowerCase().trim()),
      )
      .slice(0, 10),
  );

  constructor() {
    this.studentsService.syncFromApi().subscribe({ next: list => this.students.set(list) });
    this.enrollmentService.search({}).subscribe({ next: list => this.enrollments.set(list) });
  }

  protected onCreateOrUpdate(): void {
    if (this.form.invalid || !this.selectedAlunoId()) {
      this.snackBar.open('Selecione aluno, turma e período letivo.', 'Fechar', {
        duration: 3500,
      });
      return;
    }

    const raw = this.form.getRawValue();
    const enrollment: Enrollment = {
      id: this.editingId() ?? `local-${crypto.randomUUID()}`,
      alunoId: this.selectedAlunoId(),
      turmaId: raw.turmaId,
      periodoLetivoId: raw.periodoLetivoId,
      status: raw.status,
      createdAt: new Date().toISOString(),
    } as Enrollment;

    if (this.editingId()) {
      this.enrollments.set(
        this.enrollments().map(item => (item.id === enrollment.id ? enrollment : item)),
      );
      this.clear();
      return;
    }

    this.isLoading.set(true);
    this.enrollmentService
      .create({
        alunoId: enrollment.alunoId,
        turmaId: enrollment.turmaId,
        periodoLetivoId: enrollment.periodoLetivoId,
      })
      .subscribe({
        next: response => {
          this.isLoading.set(false);
          this.enrollments.set([{ ...response, status: raw.status }, ...this.enrollments()]);
          this.clear();
        },
        error: (error: { error?: ApiErrorResponse }) => {
          this.isLoading.set(false);
          this.snackBar.open(error.error?.message ?? 'Erro ao criar matrícula.', 'Fechar', {
            duration: 4500,
          });
        },
      });
  }

  protected selectAluno(id: string, nome: string): void {
    this.selectedAlunoId.set(id);
    this.form.controls.alunoNome.setValue(nome);
  }

  protected onAlunoInput(): void {
    this.selectedAlunoId.set(null);
  }

  protected onEdit(item: Enrollment): void {
    this.editingId.set(item.id);
    const aluno = this.students().find(s => s.id === item.alunoId);
    this.selectedAlunoId.set(item.alunoId);
    this.form.patchValue({
      alunoNome: aluno?.nomeCompleto ?? '',
      turmaId: item.turmaId,
      periodoLetivoId: item.periodoLetivoId,
      status: item.status,
    });
  }

  protected onDelete(id: string): void {
    this.enrollments.set(this.enrollments().filter(i => i.id !== id));
  }

  protected clear(): void {
    this.form.reset({ alunoNome: '', turmaId: '', periodoLetivoId: '', status: 'ATIVA' });
    this.editingId.set(null);
    this.selectedAlunoId.set(null);
  }

  protected nomeAluno(id: string): string {
    return this.students().find(s => s.id === id)?.nomeCompleto ?? 'Aluno não encontrado';
  }
  protected nomeTurma(id: string): string {
    return this.academicService.listClasses().find(c => c.id === id)?.nome ?? 'Turma não encontrada';
  }
  protected nomePeriodo(id: string): string {
    return this.academicService.listPeriods().find(p => p.id === id)?.nome ?? 'Período não encontrado';
  }

  protected get turmas() {
    return this.academicService.listClasses();
  }

  protected get periodos() {
    return this.academicService.listPeriods();
  }
}
