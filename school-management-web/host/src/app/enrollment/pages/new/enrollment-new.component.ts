import { NgFor, NgIf } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AcademicService } from '../../../academic/services/academic.service';
import { Student } from '../../../students/models/student.model';
import { StudentsService } from '../../../students/services/students.service';
import { ApiErrorResponse, Enrollment } from '../../models/enrollment.model';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-enrollment-new',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, MatAutocompleteModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule],
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

  protected readonly form = this.fb.nonNullable.group({ alunoNome: ['', [Validators.required]], turmaNome: ['', [Validators.required]], periodoNome: ['', [Validators.required]] });
  protected readonly editingId = signal<string | null>(null);
  protected readonly selectedAlunoId = signal<string | null>(null);
  protected readonly selectedTurmaId = signal<string | null>(null);
  protected readonly selectedPeriodoId = signal<string | null>(null);

  protected readonly filteredStudents = computed(() => this.students().filter(s => s.nomeCompleto.toLowerCase().includes(this.form.controls.alunoNome.value.toLowerCase().trim())).slice(0, 10));
  protected readonly filteredClasses = computed(() => this.academicService.listClasses().filter(c => c.nome.toLowerCase().includes(this.form.controls.turmaNome.value.toLowerCase().trim())).slice(0, 10));
  protected readonly filteredPeriods = computed(() => this.academicService.listPeriods().filter(p => p.nome.toLowerCase().includes(this.form.controls.periodoNome.value.toLowerCase().trim())).slice(0, 10));

  constructor() {
    this.studentsService.syncFromApi().subscribe({ next: list => this.students.set(list) });
    this.enrollmentService.search({}).subscribe({ next: list => this.enrollments.set(list) });
  }

  protected onCreateOrUpdate(): void {
    if (this.form.invalid || !this.selectedAlunoId() || !this.selectedTurmaId() || !this.selectedPeriodoId()) {
      this.snackBar.open('Selecione aluno, turma e período letivo no autocomplete.', 'Fechar', { duration: 3500 });
      return;
    }

    const enrollment: Enrollment = {
      id: this.editingId() ?? `local-${crypto.randomUUID()}`,
      alunoId: this.selectedAlunoId()!,
      turmaId: this.selectedTurmaId()!,
      periodoLetivoId: this.selectedPeriodoId()!,
      status: 'ATIVA',
      createdAt: new Date().toISOString(),
    };

    if (this.editingId()) {
      this.enrollments.set(this.enrollments().map(item => (item.id === enrollment.id ? enrollment : item)));
      this.clear();
      return;
    }

    this.isLoading.set(true);
    this.enrollmentService.create({ alunoId: enrollment.alunoId, turmaId: enrollment.turmaId, periodoLetivoId: enrollment.periodoLetivoId }).subscribe({
      next: response => {
        this.isLoading.set(false);
        this.enrollments.set([response, ...this.enrollments()]);
        this.clear();
      },
      error: (error: { error?: ApiErrorResponse }) => {
        this.isLoading.set(false);
        this.snackBar.open(error.error?.message ?? 'Erro ao criar matrícula.', 'Fechar', { duration: 4500 });
      },
    });
  }

  protected selectAluno(id: string, nome: string): void { this.selectedAlunoId.set(id); this.form.controls.alunoNome.setValue(nome); }
  protected selectTurma(id: string, nome: string): void { this.selectedTurmaId.set(id); this.form.controls.turmaNome.setValue(nome); }
  protected selectPeriodo(id: string, nome: string): void { this.selectedPeriodoId.set(id); this.form.controls.periodoNome.setValue(nome); }
  protected onAlunoInput(): void { this.selectedAlunoId.set(null); }
  protected onTurmaInput(): void { this.selectedTurmaId.set(null); }
  protected onPeriodoInput(): void { this.selectedPeriodoId.set(null); }

  protected onEdit(item: Enrollment): void {
    this.editingId.set(item.id);
    const aluno = this.students().find(s => s.id === item.alunoId);
    const turma = this.academicService.listClasses().find(c => c.id === item.turmaId);
    const periodo = this.academicService.listPeriods().find(p => p.id === item.periodoLetivoId);
    this.selectedAlunoId.set(item.alunoId); this.selectedTurmaId.set(item.turmaId); this.selectedPeriodoId.set(item.periodoLetivoId);
    this.form.patchValue({ alunoNome: aluno?.nomeCompleto ?? '', turmaNome: turma?.nome ?? '', periodoNome: periodo?.nome ?? '' });
  }
  protected onDelete(id: string): void { this.enrollments.set(this.enrollments().filter(i => i.id !== id)); }
  protected clear(): void { this.form.reset(); this.editingId.set(null); this.selectedAlunoId.set(null); this.selectedTurmaId.set(null); this.selectedPeriodoId.set(null); }

  protected nomeAluno(id: string): string { return this.students().find(s => s.id === id)?.nomeCompleto ?? id; }
  protected nomeTurma(id: string): string { return this.academicService.listClasses().find(c => c.id === id)?.nome ?? id; }
  protected nomePeriodo(id: string): string { return this.academicService.listPeriods().find(p => p.id === id)?.nome ?? id; }
}
