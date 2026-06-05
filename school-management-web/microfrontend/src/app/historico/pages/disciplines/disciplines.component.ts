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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Disciplina, DisciplinaInput } from '../../models/student-records.model';
import { StudentRecordsService } from '../../services/student-records.service';
import { DisciplineDialogComponent } from './discipline-dialog.component';

@Component({
  selector: 'app-disciplines',
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
    MatSnackBarModule,
  ],
  templateUrl: './disciplines.component.html',
  styleUrls: ['./disciplines.component.scss'],
})
export class DisciplinesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly recordsService = inject(StudentRecordsService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  protected readonly disciplinas = signal<Disciplina[]>([]);
  protected readonly isLoading = signal(false);

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });
  protected readonly disciplineSearchTerm = signal('');
  protected readonly pendingDisciplineId = signal<string | null>(null);

  protected readonly sortedDisciplines = computed(() =>
    [...this.disciplinas()].sort((a, b) =>
      a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }),
    ),
  );
  protected readonly total = computed(() => this.sortedDisciplines().length);
  protected readonly pagedDisciplines = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedDisciplines().slice(start, start + this.pageSize());
  });
  protected readonly filteredDisciplines = computed(() =>
    this.disciplinas()
      .filter((disciplina) =>
        disciplina.nome.toLowerCase().includes(this.disciplineSearchTerm().toLowerCase().trim()),
      )
      .slice(0, 10),
  );

  ngOnInit(): void {
    this.carregar();
  }

  protected openCreateDialog(): void {
    this.openDisciplineDialog();
  }

  protected openEditDialog(id: string): void {
    const disciplina = this.disciplinas().find((item) => item.id === id);
    if (!disciplina) {
      this.snackBar.open('Disciplina não encontrada na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.openDisciplineDialog(id);
  }

  protected onSearchByName(): void {
    const found = this.pendingDisciplineId()
      ? this.disciplinas().find((disciplina) => disciplina.id === this.pendingDisciplineId())
      : null;
    if (!found) {
      this.snackBar.open('Selecione uma disciplina pelo nome para consultar.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.openEditDialog(found.id);
  }

  protected onDisciplineSearchInput(value: string): void {
    this.disciplineSearchTerm.set(value);
    this.pendingDisciplineId.set(null);
    this.searchForm.controls.nome.setValue(value, { emitEvent: false });
  }

  protected onDisciplineOptionSelected(id: string): void {
    this.pendingDisciplineId.set(id);
    const disciplina = this.disciplinas().find((item) => item.id === id);
    this.searchForm.controls.nome.setValue(disciplina?.nome ?? '', { emitEvent: false });
  }

  protected onEdit(id: string): void {
    this.openEditDialog(id);
  }

  protected onDelete(id: string): void {
    this.isLoading.set(true);
    this.recordsService.excluirDisciplina(id).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open('Disciplina excluída.', 'Fechar', { duration: 2500 });
        this.carregar();
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível excluir disciplina.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected clearForm(): void {
    this.disciplineSearchTerm.set('');
    this.pendingDisciplineId.set(null);
    this.searchForm.reset();
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  private carregar(): void {
    this.isLoading.set(true);
    this.recordsService.listarDisciplinas().subscribe({
      next: (disciplinas) => {
        this.isLoading.set(false);
        this.disciplinas.set(disciplinas);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar disciplinas.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private openDisciplineDialog(id?: string): void {
    const disciplina = id ? this.disciplinas().find((item) => item.id === id) : undefined;

    this.dialog
      .open(DisciplineDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
        data: { disciplina },
      })
      .afterClosed()
      .subscribe((payload?: DisciplinaInput) => {
        if (!payload) {
          return;
        }

        this.saveDiscipline(payload, id);
      });
  }

  private saveDiscipline(payload: DisciplinaInput, id?: string): void {
    this.isLoading.set(true);
    const request$ = id
      ? this.recordsService.atualizarDisciplina(id, payload)
      : this.recordsService.criarDisciplina(payload);

    request$.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open(id ? 'Disciplina atualizada.' : 'Disciplina cadastrada.', 'Fechar', {
          duration: 2500,
        });
        this.carregar();
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível salvar disciplina.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }
}
