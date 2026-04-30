import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiErrorResponse } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';

@Component({
  selector: 'app-academic-periods',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, MatAutocompleteModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatPaginatorModule],
  templateUrl: './academic-periods.component.html',
  styleUrls: ['./academic-periods.component.scss'],
})
export class AcademicPeriodsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);

  protected readonly periods$ = this.academicService.periods$;
  protected readonly periods = toSignal(this.periods$, { initialValue: this.academicService.listPeriods() });

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);

  protected readonly form = this.fb.nonNullable.group({ nome: ['', [Validators.required, Validators.maxLength(30)]], dataInicio: ['', [Validators.required]], dataFim: ['', [Validators.required]] });
  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });

  protected readonly isLoading = signal(false);
  protected readonly editingPeriodId = signal<string | null>(null);
  protected readonly periodSearchTerm = signal('');
  protected readonly pendingPeriodId = signal<string | null>(null);
  protected readonly filteredPeriods = computed(() => {
    const term = this.periodSearchTerm().trim().toLowerCase();
    if (!term) return [];
    return this.periods().filter(period => period.nome.toLowerCase().includes(term));
  });

  protected readonly sortedPeriods = computed(() => [...this.periods()].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')));
  protected readonly total = computed(() => this.sortedPeriods().length);
  protected readonly pagedPeriods = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.sortedPeriods().slice(start, start + this.pageSize());
  });

  ngOnInit(): void { this.academicService.hydrateSeedData().subscribe(); }

  protected onCreateOrUpdate(): void { /* unchanged */
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload = this.form.getRawValue(); const editingId = this.editingPeriodId();
    if (editingId) { this.academicService.updatePeriodLocally(editingId, payload); this.clearForm(); this.snackBar.open('Período letivo atualizado na lista.', 'Fechar', { duration: 3000 }); return; }
    this.isLoading.set(true);
    this.academicService.createPeriod(payload).subscribe({ next: () => { this.isLoading.set(false); this.form.reset(); this.snackBar.open('Período letivo cadastrado com sucesso.', 'Fechar', { duration: 3000 }); }, error: (error: { error?: ApiErrorResponse }) => { this.isLoading.set(false); this.snackBar.open(error.error?.message ?? 'Não foi possível cadastrar período letivo.', 'Fechar', { duration: 4000 }); } });
  }
  protected onSearchByName(): void { if (this.searchForm.invalid) { this.searchForm.markAllAsTouched(); return; }
    const found = this.pendingPeriodId() ? this.periods().find(item => item.id === this.pendingPeriodId()) : null;
    if (!found) { this.snackBar.open('Selecione um período letivo pelo nome para consultar.', 'Fechar', { duration: 3000 }); return; }
    this.form.patchValue({ nome: found.nome, dataInicio: found.dataInicio, dataFim: found.dataFim }); this.editingPeriodId.set(found.id);
  }
  protected onPeriodSearchInput(value: string): void { this.periodSearchTerm.set(value); this.pendingPeriodId.set(null); this.searchForm.controls.nome.setValue(value, { emitEvent: false }); }
  protected onPeriodOptionSelected(id: string): void { this.pendingPeriodId.set(id); }
  protected displayPeriod = (id: string | null): string => !id ? '' : this.periods().find(item => item.id === id)?.nome ?? '';
  protected onDelete(id: string): void { this.academicService.deletePeriodLocally(id); if (this.editingPeriodId() === id) this.clearForm(); }
  protected onEdit(id: string): void { const period = this.periods().find(item => item.id === id); if (!period) return; this.form.patchValue({ nome: period.nome, dataInicio: period.dataInicio, dataFim: period.dataFim }); this.editingPeriodId.set(period.id); }
  protected clearForm(): void { this.form.reset(); this.searchForm.reset(); this.periodSearchTerm.set(''); this.pendingPeriodId.set(null); this.editingPeriodId.set(null); }
  protected onPageChange(event: PageEvent): void { this.pageSize.set(event.pageSize); this.pageIndex.set(event.pageIndex); }
}
