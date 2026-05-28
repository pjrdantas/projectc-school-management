import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Disciplina } from '../../models/student-records.model';
import { StudentRecordsService } from '../../services/student-records.service';

@Component({
  selector: 'app-disciplines',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './disciplines.component.html',
  styleUrls: ['./disciplines.component.scss'],
})
export class DisciplinesComponent implements OnInit {
  private readonly recordsService = inject(StudentRecordsService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly disciplinas = signal<Disciplina[]>([]);
  protected editingId = '';
  protected form = {
    nome: '',
    cargaHoraria: null as number | null,
    status: 'ATIVA',
  };

  ngOnInit(): void {
    this.carregar();
  }

  protected carregar(): void {
    this.recordsService.listarDisciplinas().subscribe({
      next: (disciplinas) =>
        this.disciplinas.set(
          [...disciplinas].sort((a, b) =>
            a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' }),
          ),
        ),
      error: (error: unknown) => {
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar disciplinas.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected salvar(): void {
    const nome = this.form.nome.trim();
    if (!nome) {
      this.snackBar.open('Nome da disciplina é obrigatório.', 'Fechar', { duration: 3000 });
      return;
    }

    const payload = {
      nome,
      cargaHoraria: this.form.cargaHoraria ?? undefined,
      status: this.form.status || 'ATIVA',
    };

    const request$ = this.editingId
      ? this.recordsService.atualizarDisciplina(this.editingId, payload)
      : this.recordsService.criarDisciplina(payload);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          this.editingId ? 'Disciplina atualizada.' : 'Disciplina cadastrada.',
          'Fechar',
          { duration: 2500 },
        );
        this.limpar();
        this.carregar();
      },
      error: (error: unknown) => {
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível salvar disciplina.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }

  protected editar(disciplina: Disciplina): void {
    this.editingId = disciplina.id;
    this.form = {
      nome: disciplina.nome,
      cargaHoraria: disciplina.cargaHoraria ?? null,
      status: disciplina.status || 'ATIVA',
    };
  }

  protected limpar(): void {
    this.editingId = '';
    this.form = {
      nome: '',
      cargaHoraria: null,
      status: 'ATIVA',
    };
  }
}
