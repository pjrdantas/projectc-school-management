import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { AcademicClassInput } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';
import { AcademicClassDialogComponent } from './academic-class-dialog.component';

@Component({
  selector: 'app-academic-classes',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatPaginatorModule,
  ],
  templateUrl: './academic-classes.component.html',
  styleUrls: ['./academic-classes.component.scss'],
})
export class AcademicClassesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);
  private readonly dialog = inject(MatDialog);

  protected readonly classes$ = this.academicService.classes$;
  protected readonly classes = toSignal(this.classes$, {
    initialValue: this.academicService.listClasses(),
  });
  protected readonly periods$ = this.academicService.periods$;
  protected readonly periods = toSignal(this.periods$, {
    initialValue: this.academicService.listPeriods(),
  });
  protected readonly series$ = this.academicService.series$;
  protected readonly series = toSignal(this.series$, {
    initialValue: this.academicService.listSeries(),
  });
  protected readonly shifts$ = this.academicService.shifts$;
  protected readonly shifts = toSignal(this.shifts$, {
    initialValue: this.academicService.listShifts(),
  });

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });

  protected readonly isLoading = signal(false);
  protected readonly sortedClasses = computed(() =>
    [...this.classes()].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
  );
  protected readonly total = computed(() => this.sortedClasses().length);
  protected readonly pagedClasses = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedClasses().slice(start, start + this.pageSize());
  });
  protected readonly classSearchTerm = signal('');
  protected readonly pendingClassId = signal<string | null>(null);
  protected readonly filteredClasses = computed(() =>
    this.classes()
      .filter((turma) => {
        const term = this.classSearchTerm().toLowerCase().trim();
        return turma.nome.toLowerCase().includes(term) || turma.codigo.toLowerCase().includes(term);
      })
      .slice(0, 10),
  );

  ngOnInit(): void {
    this.academicService.syncFromApi().subscribe();
  }

  protected openCreateDialog(): void {
    this.openClassDialog();
  }

  protected openEditDialog(id: string): void {
    const turma = this.classes().find((item) => item.id === id);
    if (!turma) {
      this.snackBar.open('Turma não encontrada na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.openClassDialog(id);
  }

  protected onSearchByName(): void {
    const found = this.pendingClassId()
      ? this.classes().find((c) => c.id === this.pendingClassId())
      : null;
    if (!found) {
      this.snackBar.open('Selecione uma turma pelo nome para consultar.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.openEditDialog(found.id);
  }

  protected onClassSearchInput(value: string): void {
    this.classSearchTerm.set(value);
    this.pendingClassId.set(null);
    this.searchForm.controls.nome.setValue(value, { emitEvent: false });
  }
  protected onClassOptionSelected(id: string): void {
    this.pendingClassId.set(id);
    const turma = this.classes().find((item) => item.id === id);
    this.searchForm.controls.nome.setValue(turma?.nome ?? '', { emitEvent: false });
  }

  protected onEdit(id: string): void {
    this.openEditDialog(id);
  }
  protected onDelete(id: string): void {
    this.academicService.deleteClassLocally(id);
  }
  protected clearForm(): void {
    this.classSearchTerm.set('');
    this.pendingClassId.set(null);
    this.searchForm.reset();
  }

  protected getPeriodName(periodId: string): string {
    return this.periods().find((p) => p.id === periodId)?.nome ?? periodId;
  }

  protected getSerieName(serieId: string, serieNome?: string): string {
    return serieNome || this.series().find((serie) => serie.id === serieId)?.nome || serieId;
  }

  protected getShiftName(code?: string): string {
    if (!code) {
      return '-';
    }

    return this.shifts().find((turno) => turno.codigo === code)?.descricao ?? code;
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  private openClassDialog(id?: string): void {
    const turma = id ? this.classes().find((item) => item.id === id) : undefined;

    this.dialog
      .open(AcademicClassDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        data: {
          turma,
          periods: this.periods(),
          series: this.series(),
          shifts: this.shifts(),
        },
      })
      .afterClosed()
      .subscribe((payload?: AcademicClassInput) => {
        if (!payload) {
          return;
        }

        this.saveClass(payload, id);
      });
  }

  private saveClass(payload: AcademicClassInput, id?: string): void {
    this.isLoading.set(true);
    const request$ = id
      ? this.academicService.updateClass(id, payload)
      : this.academicService.createClass(payload);

    request$.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open(id ? 'Turma atualizada com sucesso.' : 'Turma cadastrada com sucesso.', 'Fechar', {
          duration: 3000,
        });
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, id ? 'Não foi possível atualizar turma.' : 'Não foi possível cadastrar turma.'),
          'Fechar',
          { duration: 4500 },
        );
      },
    });
  }

}
