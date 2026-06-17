import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AcademicClass, AcademicPeriod } from '../../../catalogo/models/academic.model';
import { AcademicService } from '../../../catalogo/services/academic.service';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { MessageDialogComponent } from '../../../compartilhado/dialogs/message-dialog/message-dialog.component';
import { Student } from '../../../aluno/models/student.model';
import { StudentsService } from '../../../aluno/services/students.service';
import { Enrollment, EnrollmentCatalogItem, EnrollmentFilter } from '../../models/enrollment.model';
import { EnrollmentService } from '../../services/enrollment.service';

@Component({
  selector: 'app-enrollment-new',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './enrollment-new.component.html',
  styleUrls: ['./enrollment-new.component.scss'],
})
export class EnrollmentNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly studentsService = inject(StudentsService);
  private readonly academicService = inject(AcademicService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly isLoading = signal(false);
  protected readonly isCreating = signal(false);
  protected readonly deletingEnrollmentId = signal<string | null>(null);
  protected readonly updatingEnrollmentStatusId = signal<string | null>(null);
  protected readonly enrollments = signal<Enrollment[]>([]);
  protected readonly activeEnrollmentSnapshot = signal<Enrollment[]>([]);
  protected readonly enrollmentSnapshot = signal<Enrollment[]>([]);
  protected readonly hasConsulted = signal(false);
  protected readonly students = signal<Student[]>([]);
  protected readonly periods = signal<AcademicPeriod[]>([]);
  protected readonly classes = signal<AcademicClass[]>([]);
  private readonly fallbackStatusOptions: EnrollmentCatalogItem[] = [
    { id: 'SOLICITADA', codigo: 'SOLICITADA', descricao: 'Matrícula solicitada' },
    { id: 'EM_ANDAMENTO', codigo: 'EM_ANDAMENTO', descricao: 'Matrícula em andamento' },
    {
      id: 'AGUARDANDO_DOCUMENTOS',
      codigo: 'AGUARDANDO_DOCUMENTOS',
      descricao: 'Aguardando documentos',
    },
    {
      id: 'AGUARDANDO_HISTORICO_ESCOLAR',
      codigo: 'AGUARDANDO_HISTORICO_ESCOLAR',
      descricao: 'Aguardando histórico escolar',
    },
    { id: 'EFETIVADA', codigo: 'EFETIVADA', descricao: 'Matrícula efetivada' },
    { id: 'CANCELADA', codigo: 'CANCELADA', descricao: 'Matrícula cancelada' },
    { id: 'CONCLUIDA', codigo: 'CONCLUIDA', descricao: 'Matrícula concluída' },
    { id: 'INDEFERIDA', codigo: 'INDEFERIDA', descricao: 'Matrícula indeferida' },
  ];
  protected readonly statusCatalog = signal<EnrollmentCatalogItem[]>([]);
  protected readonly statusOptions = computed(() =>
    this.statusCatalog().length ? this.statusCatalog() : this.fallbackStatusOptions,
  );
  protected readonly pageSizeOptions = [5, 10, 20];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);
  protected readonly searchTerm = signal('');
  protected readonly studentSearchTerm = signal('');
  protected readonly filterStudentSearchTerm = signal('');

  protected readonly form = this.fb.nonNullable.group({
    alunoNome: ['', [Validators.required]],
    turmaId: ['', [Validators.required]],
  });

  protected readonly filterForm = this.fb.nonNullable.group({
    alunoNome: [''],
    turmaId: [''],
    status: [''],
  });

  protected readonly selectedAlunoId = signal<string | null>(null);
  protected readonly currentPeriod = computed(() => this.findCurrentPeriod());
  protected readonly currentClasses = computed(() => {
    const period = this.currentPeriod();
    if (!period) {
      return [];
    }

    return this.classes().filter((item) => item.periodoLetivoId === period.id);
  });
  protected readonly unavailableStudentIds = computed(
    () =>
      new Set(
        this.enrollmentSnapshot()
          .filter((item) => this.isActiveEnrollmentStatus(item.status))
          .map((item) => item.alunoId),
      ),
  );

  protected readonly filteredStudents = computed(() => {
    const term = this.studentSearchTerm();
    return this.filterStudents(term, true);
  });

  protected readonly filteredFilterStudents = computed(() => {
    const term = this.filterStudentSearchTerm();
    return this.filterStudents(term, false);
  });

  protected readonly filteredEnrollments = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const list = this.sortEnrollments(
      this.enrollments().filter((item) => this.matchesFilterStudentName(item)),
    );
    if (!term) return list;

    return list.filter((item) => {
      const aluno = this.nomeAluno(item.alunoId).toLowerCase();
      const turma = this.nomeTurma(item.turmaId).toLowerCase();
      const periodo = this.nomePeriodo(item.periodoLetivoId).toLowerCase();
      return (
        aluno.includes(term) ||
        turma.includes(term) ||
        periodo.includes(term) ||
        item.status.toLowerCase().includes(term)
      );
    });
  });

  protected readonly totalEnrollments = computed(() => this.filteredEnrollments().length);
  protected readonly activeEnrollments = computed(
    () => this.enrollments().filter((item) => this.isActiveEnrollmentStatus(item.status)).length,
  );
  protected readonly distinctStudents = computed(
    () => new Set(this.enrollments().map((item) => item.alunoId)).size,
  );
  protected readonly studentsWithMultipleActiveEnrollments = computed(() => {
    const counts = new Map<string, number>();
    this.enrollments()
      .filter((item) => this.isActiveEnrollmentStatus(item.status))
      .forEach((item) => counts.set(item.alunoId, (counts.get(item.alunoId) ?? 0) + 1));

    return [...counts.entries()]
      .filter(([, count]) => count > 1)
      .map(([alunoId, count]) => ({ alunoId, count }));
  });
  protected readonly distinctClasses = computed(
    () => new Set(this.enrollments().map((item) => item.turmaId)).size,
  );
  protected readonly pagedEnrollments = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredEnrollments().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.loadReferenceData();
  }

  protected onCreate(): void {
    const period = this.currentPeriod();
    if (!period) {
      this.snackBar.open('Não existe período letivo vigente para a data atual.', 'Fechar', {
        duration: 4500,
      });
      return;
    }

    if (this.form.invalid || !this.selectedAlunoId()) {
      this.form.markAllAsTouched();
      this.snackBar.open('Selecione aluno e turma.', 'Fechar', {
        duration: 3500,
      });
      return;
    }

    if (this.unavailableStudentIds().has(this.selectedAlunoId()!)) {
      this.snackBar.open('Aluno já possui matrícula no período.', 'Fechar', { duration: 4000 });
      return;
    }

    const raw = this.form.getRawValue();
    if (this.isClassFull(raw.turmaId)) {
      this.snackBar.open('Turma sem vagas disponíveis.', 'Fechar', { duration: 4000 });
      return;
    }

    const alunoId = this.selectedAlunoId()!;
    const turmaId = raw.turmaId;
    const dialogRef = this.dialog.open(MessageDialogComponent, {
      width: '640px',
      maxWidth: 'calc(100vw - 32px)',
      disableClose: true,
      data: {
        title: 'Confirmar matrícula',
        message: 'Confirme os dados antes de criar a matrícula.',
        details: [
          `Aluno: ${this.nomeAluno(alunoId)}`,
          `Turma: ${this.nomeTurma(turmaId)}`,
          `Período: ${this.currentPeriodLabel()}`,
        ],
        confirmLabel: 'Confirmar matrícula',
        cancelLabel: 'Cancelar',
        icon: 'how_to_reg',
        tone: 'warning',
      },
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.criarMatricula(alunoId, turmaId, period.id);
    });
  }

  private criarMatricula(alunoId: string, turmaId: string, periodoLetivoId: string): void {
    this.isCreating.set(true);
    this.enrollmentService
      .create({
        alunoId,
        turmaId,
        periodoLetivoId,
      })
      .subscribe({
        next: () => {
          this.isCreating.set(false);
          this.form.reset({ alunoNome: '', turmaId: '' });
          this.selectedAlunoId.set(null);
          this.studentSearchTerm.set('');
          this.snackBar.open('Matrícula criada com sucesso.', 'Fechar', { duration: 3000 });
          this.hasConsulted.set(true);
          this.loadAvailability();
          this.loadEnrollments();
        },
        error: (error: unknown) => {
          this.isCreating.set(false);
          this.snackBar.open(getApiErrorMessage(error, 'Erro ao criar matrícula.'), 'Fechar', {
            duration: 4500,
          });
        },
      });
  }

  protected selectAluno(id: string, nome: string): void {
    if (this.unavailableStudentIds().has(id)) {
      this.selectedAlunoId.set(null);
      this.form.patchValue({ alunoNome: '', turmaId: '' });
      this.studentSearchTerm.set('');
      this.snackBar.open('Aluno já possui matrícula ativa no período atual.', 'Fechar', {
        duration: 4000,
      });
      return;
    }

    this.selectedAlunoId.set(id);
    this.form.controls.alunoNome.setValue(nome);
    this.studentSearchTerm.set(nome);
  }

  protected selectFilterAluno(_id: string, nome: string): void {
    this.filterForm.controls.alunoNome.setValue(nome);
    this.filterStudentSearchTerm.set(nome);
  }

  protected onAlunoInput(value: string): void {
    this.selectedAlunoId.set(null);
    this.studentSearchTerm.set(value);
    this.form.controls.turmaId.setValue('');
  }

  protected goToStudentRegistration(): void {
    this.router.navigate(['/students/new'], {
      queryParams: { returnTo: 'enrollment' },
    });
  }

  protected onFilterAlunoInput(value: string): void {
    this.filterStudentSearchTerm.set(value);
  }

  protected applyFilters(): void {
    this.pageIndex.set(0);
    this.hasConsulted.set(true);
    this.loadEnrollments();
  }

  protected clearFilters(): void {
    this.filterForm.reset({ alunoNome: '', turmaId: '', status: '' });
    this.filterStudentSearchTerm.set('');
    this.searchTerm.set('');
    this.pageIndex.set(0);
    this.hasConsulted.set(false);
    this.enrollments.set([]);
  }

  protected onSearchTermChange(value: string): void {
    this.searchTerm.set(value);
    this.pageIndex.set(0);
  }

  protected clearSearch(): void {
    this.searchTerm.set('');
    this.pageIndex.set(0);
  }

  protected refreshEnrollments(): void {
    this.hasConsulted.set(true);
    this.loadEnrollments();
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  protected deleteEnrollment(enrollment: Enrollment): void {
    const aluno = this.nomeAluno(enrollment.alunoId);
    const dialogRef = this.dialog.open(MessageDialogComponent, {
      width: '640px',
      maxWidth: 'calc(100vw - 32px)',
      disableClose: true,
      data: {
        title: 'Confirmar exclusão',
        message: `Deseja excluir a matrícula de ${aluno}?`,
        details: [
          `Turma: ${this.nomeTurma(enrollment.turmaId)}`,
          `Período: ${this.nomePeriodo(enrollment.periodoLetivoId)}`,
          'Depois disso será possível excluir o cadastro do aluno, se ele não tiver outros vínculos.',
        ],
        confirmLabel: 'Excluir matrícula',
        cancelLabel: 'Cancelar',
        icon: 'delete',
        tone: 'danger',
      },
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.excluirMatricula(enrollment);
    });
  }

  private excluirMatricula(enrollment: Enrollment): void {
    this.deletingEnrollmentId.set(enrollment.id);
    this.enrollmentService.delete(enrollment.id).subscribe({
      next: () => {
        this.enrollments.update((current) => current.filter((item) => item.id !== enrollment.id));
        this.activeEnrollmentSnapshot.update((current) =>
          current.filter((item) => item.id !== enrollment.id),
        );
        this.enrollmentSnapshot.update((current) =>
          current.filter((item) => item.id !== enrollment.id),
        );
        this.deletingEnrollmentId.set(null);
        this.adjustPageAfterRemoval();
        this.snackBar.open('Matrícula excluída com sucesso.', 'Fechar', { duration: 3000 });
      },
      error: (error: unknown) => {
        this.deletingEnrollmentId.set(null);
        this.snackBar.open(getApiErrorMessage(error, 'Erro ao excluir matrícula.'), 'Fechar', {
          duration: 4500,
        });
      },
    });
  }

  protected changeStatus(enrollment: Enrollment, status: string): void {
    if (enrollment.status === status) {
      return;
    }

    this.updatingEnrollmentStatusId.set(enrollment.id);
    this.enrollmentService.updateStatus(enrollment.id, status).subscribe({
      next: (updated) => {
        this.replaceEnrollment(updated);
        this.updatingEnrollmentStatusId.set(null);
        this.loadAvailability();
        this.snackBar.open('Status da matrícula atualizado.', 'Fechar', { duration: 3000 });
      },
      error: (error: unknown) => {
        this.updatingEnrollmentStatusId.set(null);
        this.snackBar.open(getApiErrorMessage(error, 'Erro ao atualizar status.'), 'Fechar', {
          duration: 4500,
        });
      },
    });
  }

  protected nomeAluno(id: string): string {
    return this.students().find((s) => s.id === id)?.nomeCompleto ?? 'Aluno não encontrado';
  }

  protected nomeTurma(id: string): string {
    return this.classes().find((c) => c.id === id)?.nome ?? 'Turma não encontrada';
  }

  protected nomePeriodo(id: string): string {
    return this.periods().find((p) => p.id === id)?.nome ?? 'Período não encontrado';
  }

  protected activeEnrollmentsForClass(turmaId: string): number {
    return this.activeEnrollmentSnapshot().filter((item) =>
      item.turmaId === turmaId && this.isActiveEnrollmentStatus(item.status),
    ).length;
  }

  protected availableSlots(turmaId: string): number {
    const turma = this.classes().find((item) => item.id === turmaId);
    if (!turma) {
      return 0;
    }

    return Math.max(turma.capacidade - this.activeEnrollmentsForClass(turmaId), 0);
  }

  protected isClassFull(turmaId: string): boolean {
    return this.availableSlots(turmaId) <= 0;
  }

  protected formatDate(value: string): string {
    if (!value) return '-';
    const [date] = value.split('T');
    const [yyyy, mm, dd] = date.split('-');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy}` : value;
  }

  protected currentPeriodLabel(): string {
    const period = this.currentPeriod();
    if (!period) {
      return 'Nenhum período vigente para hoje';
    }

    return `${period.nome} (${this.formatDate(period.dataInicio)} a ${this.formatDate(period.dataFim)})`;
  }

  private loadReferenceData(): void {
    this.studentsService.syncFromApi().subscribe({
      next: (list) => {
        this.students.set(list);
        this.selectReturnedStudentIfNeeded(list);
      },
      error: () =>
        this.snackBar.open('Não foi possível carregar alunos.', 'Fechar', { duration: 4000 }),
    });

    this.academicService.syncFromApi().subscribe({
      next: () => {
        this.periods.set(this.academicService.listPeriods());
        this.classes.set(this.academicService.listClasses());
        this.loadAvailability();
      },
      error: () => {
        this.snackBar.open('Não foi possível carregar períodos, séries e turmas.', 'Fechar', {
          duration: 4000,
        });
      },
    });

    this.enrollmentService.listStatuses().subscribe({
      next: (list) => this.statusCatalog.set(list),
      error: () =>
        this.snackBar.open('Não foi possível carregar status de matrícula.', 'Fechar', {
          duration: 4000,
        }),
    });
  }

  protected loadEnrollments(): void {
    const period = this.currentPeriod();
    if (!period) {
      this.enrollments.set([]);
      this.isLoading.set(false);
      return;
    }

    const raw = this.filterForm.getRawValue();
    const filter: EnrollmentFilter = {
      turmaId: raw.turmaId || undefined,
      periodoLetivoId: period.id,
      status: raw.status || undefined,
    };

    this.isLoading.set(true);
    this.enrollmentService.search(filter).subscribe({
      next: (list) => {
        this.enrollments.set(list);
        this.isLoading.set(false);
        this.adjustPageAfterRemoval();
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(getApiErrorMessage(error, 'Erro ao consultar matrículas.'), 'Fechar', {
          duration: 4500,
        });
      },
    });
  }

  private loadAvailability(): void {
    const period = this.currentPeriod();
    if (!period) {
      this.activeEnrollmentSnapshot.set([]);
      return;
    }

    this.enrollmentService
      .search({
        periodoLetivoId: period.id,
        status: 'EFETIVADA',
      })
      .subscribe({
        next: (list) => this.activeEnrollmentSnapshot.set(list),
        error: () =>
          this.snackBar.open('Não foi possível carregar matrículas ativas.', 'Fechar', {
            duration: 4000,
          }),
      });

    this.enrollmentService
      .search({
        periodoLetivoId: period.id,
      })
      .subscribe({
        next: (list) => {
          this.enrollmentSnapshot.set(list);
          this.selectReturnedStudentIfNeeded(this.students());
        },
      });
  }

  private filterStudents(term: string, onlyAvailable: boolean): Student[] {
    const normalizedTerm = this.normalizeText(term);
    const unavailableIds = this.unavailableStudentIds();
    const enrolledIds = new Set(
      this.enrollmentSnapshot()
        .filter((item) => this.isActiveEnrollmentStatus(item.status))
        .map((item) => item.alunoId),
    );
    const source = onlyAvailable
      ? this.students().filter((student) => !unavailableIds.has(student.id))
      : this.students().filter((student) => enrolledIds.has(student.id));

    if (!normalizedTerm) {
      return source.slice(0, 10);
    }

    return source
      .filter((student) => this.normalizeText(student.nomeCompleto).includes(normalizedTerm))
      .slice(0, 20);
  }

  private findCurrentPeriod(): AcademicPeriod | null {
    const today = this.todayIsoDate();
    return (
      this.periods()
        .filter((period) => period.dataInicio <= today && period.dataFim >= today)
        .sort((a, b) => a.dataInicio.localeCompare(b.dataInicio))[0] ?? null
    );
  }

  private todayIsoDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private adjustPageAfterRemoval(): void {
    const total = this.totalEnrollments();
    const maxPageIndex = Math.max(Math.ceil(total / this.pageSize()) - 1, 0);
    if (this.pageIndex() > maxPageIndex) {
      this.pageIndex.set(maxPageIndex);
    }
  }

  private replaceEnrollment(updated: Enrollment): void {
    this.enrollments.update((current) =>
      current.map((item) => (item.id === updated.id ? updated : item)),
    );
    this.enrollmentSnapshot.update((current) =>
      current.map((item) => (item.id === updated.id ? updated : item)),
    );
    this.activeEnrollmentSnapshot.update((current) => {
      const withoutUpdated = current.filter((item) => item.id !== updated.id);
      return this.isActiveEnrollmentStatus(updated.status) ? [...withoutUpdated, updated] : withoutUpdated;
    });
  }

  private sortEnrollments(list: Enrollment[]): Enrollment[] {
    return [...list].sort((a, b) => {
      const turma = this.nomeTurma(a.turmaId).localeCompare(this.nomeTurma(b.turmaId), 'pt-BR');
      if (turma !== 0) {
        return turma;
      }

      return this.nomeAluno(a.alunoId).localeCompare(this.nomeAluno(b.alunoId), 'pt-BR');
    });
  }

  private matchesFilterStudentName(enrollment: Enrollment): boolean {
    const raw = this.filterForm.getRawValue();
    const term = this.normalizeText(raw.alunoNome);
    if (!term) {
      return true;
    }

    return this.normalizeText(this.nomeAluno(enrollment.alunoId)).includes(term);
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  private isActiveEnrollmentStatus(status: string): boolean {
    return status === 'EFETIVADA';
  }

  private selectReturnedStudentIfNeeded(students: Student[]): void {
    const studentId = this.route.snapshot.queryParamMap.get('studentId');
    if (!studentId || this.selectedAlunoId() === studentId) {
      return;
    }

    const student = students.find((item) => item.id === studentId);
    if (!student) {
      return;
    }

    if (this.unavailableStudentIds().has(student.id)) {
      this.snackBar.open('Aluno já possui matrícula ativa no período atual.', 'Fechar', {
        duration: 4000,
      });
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { studentId: null },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
      return;
    }

    this.selectedAlunoId.set(student.id);
    this.form.patchValue({ alunoNome: student.nomeCompleto, turmaId: '' });
    this.studentSearchTerm.set(student.nomeCompleto);
    this.hasConsulted.set(true);
    this.snackBar.open('Aluno cadastrado. Selecione a turma para criar a matrícula.', 'Fechar', {
      duration: 4500,
    });
  }
}
