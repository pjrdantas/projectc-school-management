import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiErrorResponse } from '../../models/academic.model';
import { AcademicService } from '../../services/academic.service';

@Component({
  selector: 'app-academic-classes',
  standalone: true,
  imports: [NgIf, NgFor, AsyncPipe, ReactiveFormsModule, MatAutocompleteModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  templateUrl: './academic-classes.component.html',
  styleUrls: ['./academic-classes.component.scss'],
})
export class AcademicClassesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  private readonly academicService = inject(AcademicService);

  protected readonly classes$ = this.academicService.classes$;
  protected readonly classes = toSignal(this.classes$, { initialValue: this.academicService.listClasses() });
  protected readonly periods$ = this.academicService.periods$;
  protected readonly periods = toSignal(this.periods$, { initialValue: this.academicService.listPeriods() });

  protected readonly form = this.fb.nonNullable.group({ codigo: ['', [Validators.required, Validators.maxLength(20)]], nome: ['', [Validators.required, Validators.maxLength(80)]], capacidade: [30, [Validators.required, Validators.min(1)]], periodoNome: ['', [Validators.required]] });
  protected readonly searchForm = this.fb.nonNullable.group({ nome: ['', [Validators.required]] });

  protected readonly isLoading = signal(false);
  protected readonly total = computed(() => this.classes().length);
  protected readonly editingClassId = signal<string | null>(null);
  protected readonly selectedPeriodId = signal<string | null>(null);
  protected readonly periodSearchTerm = signal('');
  protected readonly classSearchTerm = signal('');
  protected readonly pendingClassId = signal<string | null>(null);
  protected readonly filteredPeriods = computed(() => this.periods().filter(p => p.nome.toLowerCase().includes(this.periodSearchTerm().toLowerCase().trim())).slice(0, 10));
  protected readonly filteredClasses = computed(() => this.classes().filter(c => c.nome.toLowerCase().includes(this.classSearchTerm().toLowerCase().trim())).slice(0, 10));

  protected onCreateOrUpdate(): void {
    if (this.form.invalid || !this.selectedPeriodId()) {
      this.form.markAllAsTouched();
      this.snackBar.open('Selecione o período letivo no autocomplete.', 'Fechar', { duration: 3000 });
      return;
    }

    const raw = this.form.getRawValue();
    const payload = { codigo: raw.codigo, nome: raw.nome, capacidade: raw.capacidade, periodoLetivoId: this.selectedPeriodId()! };

    if (this.editingClassId()) {
      this.academicService.updateClassLocally(this.editingClassId()!, payload);
      this.clearForm();
      this.snackBar.open('Turma atualizada na lista.', 'Fechar', { duration: 2500 });
      return;
    }

    this.isLoading.set(true);
    this.academicService.createClass(payload).subscribe({
      next: () => { this.isLoading.set(false); this.clearForm(); this.form.controls.capacidade.setValue(30); this.snackBar.open('Turma cadastrada com sucesso.', 'Fechar', { duration: 3000 }); },
      error: (error: { error?: ApiErrorResponse }) => { this.isLoading.set(false); this.snackBar.open(error.error?.message ?? 'Não foi possível cadastrar turma.', 'Fechar', { duration: 4000 }); },
    });
  }

  protected onSearchByName(): void {
    const found = this.pendingClassId() ? this.classes().find(c => c.id === this.pendingClassId()) : null;
    if (!found) { this.snackBar.open('Selecione uma turma pelo nome para consultar.', 'Fechar', { duration: 3000 }); return; }
    const period = this.periods().find(p => p.id === found.periodoLetivoId);
    this.form.patchValue({ codigo: found.codigo, nome: found.nome, capacidade: found.capacidade, periodoNome: period?.nome ?? '' });
    this.selectedPeriodId.set(found.periodoLetivoId);
    this.editingClassId.set(found.id);
  }

  protected onClassSearchInput(value: string): void { this.classSearchTerm.set(value); this.pendingClassId.set(null); this.searchForm.controls.nome.setValue(value, { emitEvent: false }); }
  protected onClassOptionSelected(id: string): void { this.pendingClassId.set(id); }
  protected onPeriodInput(value: string): void { this.periodSearchTerm.set(value); this.selectedPeriodId.set(null); this.form.controls.periodoNome.setValue(value, { emitEvent: false }); }
  protected onPeriodOptionSelected(id: string): void { const period = this.periods().find(p => p.id === id); this.selectedPeriodId.set(id); this.form.controls.periodoNome.setValue(period?.nome ?? '', { emitEvent: false }); }

  protected onEdit(id: string): void { this.pendingClassId.set(id); this.onSearchByName(); }
  protected onDelete(id: string): void { this.academicService.deleteClassLocally(id); if (this.editingClassId() === id) this.clearForm(); }
  protected clearForm(): void { this.form.reset({ codigo: '', nome: '', capacidade: 30, periodoNome: '' }); this.selectedPeriodId.set(null); this.editingClassId.set(null); this.periodSearchTerm.set(''); this.classSearchTerm.set(''); this.pendingClassId.set(null); this.searchForm.reset(); }
  protected getPeriodName(periodId: string): string { return this.periods().find(p => p.id === periodId)?.nome ?? periodId; }
}
