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
import { AcademicSeries, AcademicSeriesInput } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';
import { AcademicSeriesDialogComponent } from './academic-series-dialog.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-academic-series',
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
  templateUrl: './academic-series.component.html',
  styleUrls: ['./academic-series.component.scss'],
})
export class AcademicSeriesComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);
  private readonly dialog = inject(MatDialog);

  protected readonly series$ = this.academicService.series$;
  protected readonly series = signal<AcademicSeries[]>(this.academicService.listSeries());
  private seriesSubscription?: Subscription;

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });
  protected readonly isLoading = signal(false);
  protected readonly sortedSeries = computed(() =>
    [...this.series()].sort((a, b) => a.ordem - b.ordem || a.nome.localeCompare(b.nome, 'pt-BR')),
  );
  protected readonly total = computed(() => this.sortedSeries().length);
  protected readonly pagedSeries = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedSeries().slice(start, start + this.pageSize());
  });
  protected readonly seriesSearchTerm = signal('');
  protected readonly pendingSeriesId = signal<string | null>(null);
  protected readonly filteredSeries = computed(() =>
    this.series()
      .filter((serie) => serie.nome.toLowerCase().includes(this.seriesSearchTerm().toLowerCase().trim()))
      .slice(0, 10),
  );

  ngOnInit(): void {
    this.seriesSubscription = this.series$.subscribe((series) => this.series.set(series));
    this.academicService.syncFromApi().subscribe();
  }

  ngOnDestroy(): void {
    this.seriesSubscription?.unsubscribe();
  }

  protected openCreateDialog(): void {
    this.openSeriesDialog();
  }

  protected openEditDialog(id: string): void {
    const serie = this.series().find((item) => item.id === id);
    if (!serie) {
      this.snackBar.open('Série não encontrada na lista.', 'Fechar', { duration: 3000 });
      return;
    }

    this.openSeriesDialog(id);
  }

  protected onSearchByName(): void {
    const found = this.pendingSeriesId()
      ? this.series().find((serie) => serie.id === this.pendingSeriesId())
      : null;
    if (!found) {
      this.snackBar.open('Selecione uma série pelo nome para consultar.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.openEditDialog(found.id);
  }

  protected onSeriesSearchInput(value: string): void {
    this.seriesSearchTerm.set(value);
    this.pendingSeriesId.set(null);
    this.searchForm.controls.nome.setValue(value, { emitEvent: false });
  }

  protected onSeriesOptionSelected(id: string): void {
    this.pendingSeriesId.set(id);
  }

  protected onEdit(id: string): void {
    this.openEditDialog(id);
  }

  protected onDelete(id: string): void {
    this.academicService.deleteSeriesLocally(id);
  }

  protected clearForm(): void {
    this.seriesSearchTerm.set('');
    this.pendingSeriesId.set(null);
    this.searchForm.reset();
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  private openSeriesDialog(id?: string): void {
    const serie = id ? this.series().find((item) => item.id === id) : undefined;

    this.dialog
      .open(AcademicSeriesDialogComponent, {
        width: '760px',
        maxWidth: '95vw',
        disableClose: true,
        data: { serie },
      })
      .afterClosed()
      .subscribe((payload?: AcademicSeriesInput) => {
        if (!payload) {
          return;
        }

        this.saveSeries(payload, id);
      });
  }

  private saveSeries(payload: AcademicSeriesInput, id?: string): void {
    this.isLoading.set(true);
    const request$ = id
      ? this.academicService.updateSeries(id, payload)
      : this.academicService.createSeries(payload);

    request$.subscribe({
      next: () => {
        this.isLoading.set(false);
        this.clearForm();
        this.snackBar.open(id ? 'Série atualizada com sucesso.' : 'Série cadastrada com sucesso.', 'Fechar', {
          duration: 3000,
        });
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, id ? 'Não foi possível atualizar série.' : 'Não foi possível cadastrar série.'),
          'Fechar',
          { duration: 4500 },
        );
      },
    });
  }
}
