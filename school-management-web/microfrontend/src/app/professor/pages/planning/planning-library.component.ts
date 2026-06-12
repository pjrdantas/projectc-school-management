import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { PedagogicalContentLibraryItem } from '../../models/planning.model';
import { PlanningService } from '../../services/planning.service';

@Component({
  selector: 'app-planning-library',
  standalone: true,
  imports: [
    NgFor,
    NgIf,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './planning-library.component.html',
  styleUrls: ['./planning-library.component.scss'],
})
export class PlanningLibraryComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly planningService = inject(PlanningService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly items = signal<PedagogicalContentLibraryItem[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly typeOptions = [
    { value: '', label: 'Todos' },
    { value: 'PLANO_BIMESTRAL', label: 'Plano bimestral' },
    { value: 'PLANO_AULA', label: 'Plano de aula' },
    { value: 'ATIVIDADE', label: 'Atividade' },
    { value: 'PROVA', label: 'Prova' },
    { value: 'QUESTOES', label: 'Questões' },
    { value: 'RESUMO', label: 'Resumo' },
    { value: 'MATERIAL_APOIO', label: 'Material de apoio' },
    { value: 'RUBRICA', label: 'Rubrica' },
  ];

  protected readonly filterForm = this.fb.nonNullable.group({
    tema: [''],
    tipoConteudo: [''],
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    const raw = this.filterForm.getRawValue();
    this.isLoading.set(true);
    this.planningService
      .listarBiblioteca({
        tema: raw.tema.trim() || undefined,
        tipoConteudo: raw.tipoConteudo || undefined,
      })
      .subscribe({
        next: items => {
          this.items.set(items);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.isLoading.set(false);
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível carregar biblioteca pedagógica.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
  }

  protected clearFilters(): void {
    this.filterForm.reset({ tema: '', tipoConteudo: '' });
    this.load();
  }

  protected typeLabel(item: PedagogicalContentLibraryItem): string {
    return item.tipoConteudoDescricao || item.tipoConteudo || 'Tipo não informado';
  }
}
