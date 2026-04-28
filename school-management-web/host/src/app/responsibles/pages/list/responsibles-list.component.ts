import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ResponsiblesService } from '../../services/responsibles.service';

@Component({
  selector: 'app-responsibles-list',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink,
    AsyncPipe,
    NgIf,
    NgFor,
  ],
  templateUrl: './responsibles-list.component.html',
  styleUrls: ['./responsibles-list.component.scss'],
})
export class ResponsiblesListComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly responsiblesService = inject(ResponsiblesService);

  protected readonly responsibles$ = this.responsiblesService.responsibles$;
  private readonly responsiblesSignal = toSignal(this.responsiblesService.responsibles$, {
    initialValue: this.responsiblesService.list(),
  });

  protected readonly pageSizeOptions = [5];
  protected readonly pageSize = signal(5);
  protected readonly pageIndex = signal(0);
  protected readonly searchTerm = signal('');

  protected readonly filteredResponsibles = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const items = this.responsiblesSignal();
    return !term
      ? items
      : items.filter(item => item.nomeCompleto.toLowerCase().includes(term));
  });

  protected readonly totalResponsibles = computed(() => this.filteredResponsibles().length);

  protected readonly pagedResponsibles = computed(() => {
    const items = this.filteredResponsibles();
    const start = this.pageIndex() * this.pageSize();
    return items.slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    this.responsiblesService.syncFromApi().subscribe({
      error: () => this.snackBar.open('Não foi possível carregar responsáveis.', 'Fechar', { duration: 4000 }),
    });
  }

  protected goToNew(): void {
    this.router.navigate(['/responsibles/new']);
  }

  protected onSearchTermChange(value: string): void {
    this.searchTerm.set(value);
    this.pageIndex.set(0);
    this.responsiblesService.syncFromApi(value || undefined).subscribe({
      error: () => this.snackBar.open('Não foi possível buscar responsáveis.', 'Fechar', { duration: 4000 }),
    });
  }

  protected remover(id: string): void {
    this.responsiblesService.removeOnApi(id).subscribe({
      next: () => this.snackBar.open('Responsável removido com sucesso.', 'Fechar', { duration: 3000 }),
      error: () => this.snackBar.open('Erro ao excluir responsável.', 'Fechar', { duration: 4000 }),
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.pageIndex.set(event.pageIndex);
  }

  protected formatCpf(cpf: string): string {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }
}
