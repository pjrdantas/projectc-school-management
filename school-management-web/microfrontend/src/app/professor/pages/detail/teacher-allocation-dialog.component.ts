import { NgFor, NgIf } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AcademicClass, AcademicClassDiscipline } from '../../../catalogo/models/academic.model';
import { AcademicService } from '../../../catalogo/services/academic.service';
import { ProfessorAllocation, ProfessorAllocationInput } from '../../models/teacher.model';
import { TeachersService } from '../../services/teachers.service';

@Component({
  selector: 'app-teacher-allocation-dialog',
  standalone: true,
  imports: [
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './teacher-allocation-dialog.component.html',
  styleUrls: ['./teacher-allocation-dialog.component.scss'],
})
export class TeacherAllocationDialogComponent {
  private readonly academicService = inject(AcademicService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogRef = inject(MatDialogRef<TeacherAllocationDialogComponent>);
  private readonly fb = inject(FormBuilder);
  private readonly teachersService = inject(TeachersService);

  protected readonly classes = signal<AcademicClass[]>([]);
  protected readonly disciplines = signal<AcademicClassDiscipline[]>([]);
  protected readonly teachersInClass = signal<ProfessorAllocation[]>([]);
  protected readonly isLoadingClasses = signal(true);
  protected readonly isLoadingDisciplines = signal(false);
  protected readonly isLoadingTeachersInClass = signal(false);

  protected readonly statusOptions = [
    { value: 'true', label: 'Ativo' },
    { value: 'false', label: 'Inativo' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    turmaId: ['', [Validators.required]],
    turmaDisciplinaId: ['', [Validators.required]],
    dataInicio: [''],
    dataFim: [''],
    ativo: ['true', [Validators.required]],
  });

  constructor() {
    this.loadClasses();

    this.form.controls.turmaId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(turmaId => this.onClassSelected(turmaId));
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload: ProfessorAllocationInput = {
      turmaDisciplinaId: raw.turmaDisciplinaId,
      dataInicio: raw.dataInicio || null,
      dataFim: raw.dataFim || null,
      ativo: raw.ativo === 'true',
    };

    this.dialogRef.close(payload);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private loadClasses(): void {
    const cachedClasses = this.academicService.listClasses();
    if (cachedClasses.length > 0) {
      this.classes.set(this.sortedClasses(cachedClasses));
      this.isLoadingClasses.set(false);
      return;
    }

    this.academicService
      .syncFromApi()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.classes.set(this.sortedClasses(this.academicService.listClasses()));
          this.isLoadingClasses.set(false);
        },
        error: () => {
          this.classes.set([]);
          this.isLoadingClasses.set(false);
        },
      });
  }

  private onClassSelected(turmaId: string): void {
    this.form.controls.turmaDisciplinaId.setValue('');
    this.disciplines.set([]);
    this.teachersInClass.set([]);

    if (!turmaId) {
      return;
    }

    this.isLoadingDisciplines.set(true);
    this.isLoadingTeachersInClass.set(true);

    this.academicService
      .listClassDisciplines(turmaId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: disciplines => {
          this.disciplines.set(
            [...disciplines].sort((left, right) =>
              left.disciplinaNome.localeCompare(right.disciplinaNome, 'pt-BR'),
            ),
          );
          this.isLoadingDisciplines.set(false);
        },
        error: () => {
          this.disciplines.set([]);
          this.isLoadingDisciplines.set(false);
        },
      });

    this.teachersService
      .listarProfessoresPorTurma(turmaId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: allocations => {
          this.teachersInClass.set(allocations);
          this.isLoadingTeachersInClass.set(false);
        },
        error: () => {
          this.teachersInClass.set([]);
          this.isLoadingTeachersInClass.set(false);
        },
      });
  }

  private sortedClasses(classes: AcademicClass[]): AcademicClass[] {
    return [...classes].sort((left, right) => left.nome.localeCompare(right.nome, 'pt-BR'));
  }
}
