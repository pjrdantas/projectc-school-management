import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Student } from '../../models/student.model';
import { StudentsService } from '../../services/students.service';
import { Responsible } from '../../../responsibles/models/responsible.model';
import { ResponsiblesService } from '../../../responsibles/services/responsibles.service';

@Component({
  selector: 'app-students-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatIconModule,
    RouterLink,
    MatSnackBarModule,
  ],
  templateUrl: './students-detail.component.html',
  styleUrls: ['./students-detail.component.scss'],
})
export class StudentsDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly studentsService = inject(StudentsService);
  private readonly responsiblesService = inject(ResponsiblesService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly student = signal<Student | null>(null);
  protected readonly responsibles = signal<Responsible[]>([]);
  protected readonly vinculos = signal<Responsible[]>([]);
  protected selectedResponsibleId = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/students']);
      return;
    }

    this.studentsService.fetchByIdFromApi(id).subscribe({
      next: student => {
        this.student.set(student);
        this.carregarResponsaveis(student.id);
      },
      error: () => {
        this.snackBar.open('Aluno não encontrado.', 'Fechar', { duration: 3000 });
        this.router.navigate(['/students']);
      },
    });

    this.responsiblesService.syncFromApi().subscribe({
      next: responsibles => this.responsibles.set(responsibles),
      error: () => {
        this.snackBar.open('Não foi possível carregar responsáveis.', 'Fechar', { duration: 3000 });
      },
    });
  }

  protected vincularResponsavel(): void {
    const aluno = this.student();
    if (!aluno || !this.selectedResponsibleId) return;

    this.responsiblesService.vincularAlunoResponsavel(aluno.id, this.selectedResponsibleId).subscribe({
      next: () => {
        this.snackBar.open('Responsável vinculado com sucesso.', 'Fechar', { duration: 3000 });
        this.selectedResponsibleId = '';
        this.carregarResponsaveis(aluno.id);
      },
      error: () => {
        this.snackBar.open('Erro ao vincular responsável.', 'Fechar', { duration: 4000 });
      },
    });
  }

  protected desvincularResponsavel(idResponsavel: string): void {
    const aluno = this.student();
    if (!aluno) return;

    this.responsiblesService.desvincularAlunoResponsavel(aluno.id, idResponsavel).subscribe({
      next: () => {
        this.snackBar.open('Vínculo removido com sucesso.', 'Fechar', { duration: 3000 });
        this.carregarResponsaveis(aluno.id);
      },
      error: () => {
        this.snackBar.open('Erro ao remover vínculo.', 'Fechar', { duration: 4000 });
      },
    });
  }

  protected formatCpf(cpf: string): string {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  protected formatData(value: string): string {
    const [yyyy, mm, dd] = value.split('-');
    return `${dd}/${mm}/${yyyy}`;
  }

  private carregarResponsaveis(idAluno: string): void {
    this.responsiblesService.listarResponsaveisPorAluno(idAluno).subscribe({
      next: responsaveis => this.vinculos.set(responsaveis),
      error: () => {
        this.snackBar.open('Não foi possível carregar vínculos do aluno.', 'Fechar', { duration: 3000 });
      },
    });
  }
}
