import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Responsible } from '../../models/responsible.model';
import { ResponsiblesService } from '../../services/responsibles.service';

@Component({
  selector: 'app-responsibles-detail',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterLink, MatSnackBarModule],
  templateUrl: './responsibles-detail.component.html',
  styleUrls: ['./responsibles-detail.component.scss'],
})
export class ResponsiblesDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly responsiblesService = inject(ResponsiblesService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly responsible = signal<Responsible | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/responsibles']);
      return;
    }

    this.responsiblesService.fetchByIdFromApi(id).subscribe({
      next: (responsible) => this.responsible.set(responsible),
      error: () => {
        this.snackBar.open('Responsável não encontrado.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/responsibles']);
      },
    });
  }

  protected formatCpf(cpf: string): string {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  protected formatCep(cep?: string): string {
    if (!cep) return 'Não informado';
    const digits = cep.replace(/\D/g, '').slice(0, 8);
    if (digits.length !== 8) return cep;
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
  }

  protected enderecoCompleto(item: Responsible): string {
    const partes = [
      item.logradouro,
      item.numero ? `nº ${item.numero}` : undefined,
      item.complemento,
      item.bairro,
      item.cidade && item.uf ? `${item.cidade}/${item.uf}` : item.cidade || item.uf,
    ].filter(Boolean);

    return partes.length ? partes.join(', ') : 'Não informado';
  }
}
