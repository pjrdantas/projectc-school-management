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
import { AcademicPeriod, AcademicPeriodInput } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';
import { Subscription } from 'rxjs';
import { AcademicPeriodDialogComponent } from './academic-period-dialog.component';

@Component({
  selector: 'app-academic-periods',
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
  templateUrl: './academic-periods.component.html',
  styleUrls: ['./academic-periods.component.scss'],
})
export class AcademicPeriodsComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);
  private readonly dialog = inject(MatDialog);

  protected readonly periods$ = this.academicService.periods$;
  protected readonly periods = signal<AcademicPeriod[]>(this.academicService.listPeriods());
  private periodsSubscription?: Subscription;

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });

  protected readonly isLoading = signal(false);
  protected readonly periodSearchTerm = signal('');
  protected readonly pendingPeriodId = signal<string | null>(null);
  protected readonly filteredPeriods = computed(() => {
    const term = this.periodSearchTerm().trim().toLowerCase();
    if (!term) return [];
    return this.periods().filter((period) => period.nome.toLowerCase().includes(term));
  });

  protected readonly sortedPeriods = computed(() =>
    [...this.periods()].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
  );
  protected readonly total = computed(() => this.sortedPeriods().length);
  protected readonly pagedPeriods = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedPeriods().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.periodsSubscription = this.periods$.subscribe((periods) => this.periods.set(periods));
    this.academicService.syncFromApi().subscribe();
  }

  ngOnDestroy(): void {
    this.periodsSubscription?.unsubscribe();
  }

  protected openCreateDialog(): void {
    this.openPeriodDialog();
  }

  protected onSearchByName(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    const found = this.pendingPeriodId()
      ? this.periods().find((item) => item.id === this.pendingPeriodId())
      : null;
    if (!found) {
      this.snackBar.open('Selecione um período letivo pelo nome para consultar.', 'Fechar', {
        duration: 3000,
      });
      return;
    }
    this.openEditDialog(found.id);
  }

  protected onPeriodSearchInput(value: string): void {
    this.periodSearchTerm.set(value);
    this.pendingPeriodId.set(null);
    this.searchForm.controls.nome.setValue(value, { emitEvent: false });
  }
  protected onPeriodOptionSelected(id: string): void {
    this.pendingPeriodId.set(id);
  }
  protected displayPeriod = (id: string | null): string =>
    !id ? '' : (this.periods().find((item) => item.id === id)?.nome ?? '');
  protected onDelete(id: string): void {
    this.academicService.deletePeriodLocally(id);
  }
  protected onEdit(id: string): void {
    this.openEditDialog(id);
  }
  protected clearForm(): void {
    this.searchForm.reset();
    this.periodSearchTerm.set('');
    this.pendingPeriodId.set(null);
  }
  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  protected formatDate(value: string): string {
    return this.isoToBr(value);
  }

  private openEditDialog(id: string): void {
    const period = this.periods().find((item) => item.id === id);
    if (!period) {
      this.snackBar.open('Período letivo não encontrado na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.openPeriodDialog(id);
  }

  private openPeriodDialog(id?: string): void {
    const period = id ? this.periods().find((item) => item.id === id) : undefined;

    this.dialog
      .open(AcademicPeriodDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
        data: { period },
      })
      .afterClosed()
      .subscribe((payload?: AcademicPeriodInput) => {
        if (!payload) {
          return;
        }

        this.savePeriod(payload, id);
      });
  }

  private savePeriod(payload: AcademicPeriodInput, id?: string): void {
    if (id) {
      this.academicService.updatePeriodLocally(id, payload);
      this.clearForm();
      this.snackBar.open('Período letivo atualizado na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.isLoading.set(true);
    this.academicService.createPeriod(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open('Período letivo cadastrado com sucesso.', 'Fechar', { duration: 3000 });
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível cadastrar período letivo.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  private brToIso(value: string): string {
    const [dd, mm, yyyy] = value.split('/');
    return `${yyyy}-${mm}-${dd}`;
  }

  private isoToBr(value: string): string {
    if (!value) return '';
    if (value.includes('/')) return value;

    const [yyyy, mm, dd] = value.split('-');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy}` : value;
  }
}
