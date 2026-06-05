import { NgFor, NgIf } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
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
import { AcademicShift, AcademicShiftInput } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';
import { AcademicShiftDialogComponent } from './academic-shift-dialog.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-academic-shifts',
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
  templateUrl: './academic-shifts.component.html',
  styleUrls: ['./academic-shifts.component.scss'],
})
export class AcademicShiftsComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);
  private readonly dialog = inject(MatDialog);

  protected readonly shifts$ = this.academicService.shifts$;
  protected readonly shifts = signal<AcademicShift[]>(this.academicService.listShifts());
  private shiftsSubscription?: Subscription;

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly searchForm = this.fb.nonNullable.group({ descricao: ['', [Validators.required]] });
  protected readonly isLoading = signal(false);
  protected readonly sortedShifts = computed(() =>
    [...this.shifts()].sort((a, b) => a.codigo.localeCompare(b.codigo, 'pt-BR')),
  );
  protected readonly total = computed(() => this.sortedShifts().length);
  protected readonly pagedShifts = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedShifts().slice(start, start + this.pageSize());
  });
  protected readonly shiftSearchTerm = signal('');
  protected readonly pendingShiftId = signal<string | null>(null);
  protected readonly filteredShifts = computed(() => {
    const term = this.shiftSearchTerm().toLowerCase().trim();

    return this.shifts()
      .filter((turno) =>
        turno.descricao.toLowerCase().includes(term) || turno.codigo.toLowerCase().includes(term),
      )
      .slice(0, 10);
  });

  ngOnInit(): void {
    this.shiftsSubscription = this.shifts$.subscribe((shifts) => this.shifts.set(shifts));
    this.academicService.syncFromApi().subscribe();
  }

  ngOnDestroy(): void {
    this.shiftsSubscription?.unsubscribe();
  }

  protected openCreateDialog(): void {
    this.openShiftDialog();
  }

  protected openEditDialog(id: string): void {
    const turno = this.shifts().find((item) => item.id === id);
    if (!turno) {
      this.snackBar.open('Turno não encontrado na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.openShiftDialog(id);
  }

  protected onSearchByDescription(): void {
    const found = this.pendingShiftId()
      ? this.shifts().find((turno) => turno.id === this.pendingShiftId())
      : null;
    if (!found) {
      this.snackBar.open('Selecione um turno para consultar.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.openEditDialog(found.id);
  }

  protected onShiftSearchInput(value: string): void {
    this.shiftSearchTerm.set(value);
    this.pendingShiftId.set(null);
    this.searchForm.controls.descricao.setValue(value, { emitEvent: false });
  }

  protected onShiftOptionSelected(id: string): void {
    this.pendingShiftId.set(id);
    const turno = this.shifts().find((item) => item.id === id);
    this.searchForm.controls.descricao.setValue(turno?.descricao ?? '', { emitEvent: false });
  }

  protected onEdit(id: string): void {
    this.openEditDialog(id);
  }

  protected onDelete(id: string): void {
    this.academicService.deleteShiftLocally(id);
  }

  protected clearForm(): void {
    this.shiftSearchTerm.set('');
    this.pendingShiftId.set(null);
    this.searchForm.reset();
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  private openShiftDialog(id?: string): void {
    const turno = id ? this.shifts().find((item) => item.id === id) : undefined;

    this.dialog
      .open(AcademicShiftDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
        data: { turno },
      })
      .afterClosed()
      .subscribe((payload?: AcademicShiftInput) => {
        if (!payload) {
          return;
        }

        this.saveShift(payload, id);
      });
  }

  private saveShift(payload: AcademicShiftInput, id?: string): void {
    this.isLoading.set(true);
    const request$ = id
      ? this.academicService.updateShift(id, payload)
      : this.academicService.createShift(payload);

    request$.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open(id ? 'Turno atualizado com sucesso.' : 'Turno cadastrado com sucesso.', 'Fechar', {
          duration: 3000,
        });
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, id ? 'Não foi possível atualizar turno.' : 'Não foi possível cadastrar turno.'),
          'Fechar',
          { duration: 4500 },
        );
      },
    });
  }
}
