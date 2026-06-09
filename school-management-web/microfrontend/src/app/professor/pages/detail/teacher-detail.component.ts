import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../../core/http/api-error';
import { Professor, ProfessorAllocation, ProfessorAllocationInput } from '../../models/teacher.model';
import { TeachersService } from '../../services/teachers.service';
import { TeacherAllocationDialogComponent } from './teacher-allocation-dialog.component';

@Component({
  selector: 'app-teacher-detail',
  standalone: true,
  imports: [
    DatePipe,
    NgFor,
    NgIf,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './teacher-detail.component.html',
  styleUrls: ['./teacher-detail.component.scss'],
})
export class TeacherDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly teachersService = inject(TeachersService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly professor = signal<Professor | null>(null);
  protected readonly allocations = signal<ProfessorAllocation[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isLoadingAllocations = signal(false);

  private professorId = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.snackBar.open('Professor não informado.', 'Fechar', { duration: 3000 });
      this.back();
      return;
    }

    this.professorId = id;
    this.carregar(id);
    this.carregarAlocacoes(id);
  }

  protected back(): void {
    this.router.navigate(['/teachers']);
  }

  protected openAllocationDialog(): void {
    if (!this.professorId) {
      return;
    }

    const dialogRef = this.dialog.open(TeacherAllocationDialogComponent, {
      width: '720px',
      maxWidth: '95vw',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((payload?: ProfessorAllocationInput) => {
      if (!payload) {
        return;
      }

      this.teachersService.vincularTurmaDisciplina(this.professorId, payload).subscribe({
        next: () => {
          this.snackBar.open('Vínculo cadastrado com sucesso.', 'Fechar', { duration: 3000 });
          this.carregarAlocacoes(this.professorId);
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível cadastrar vínculo.'),
            'Fechar',
            { duration: 4000 },
          );
        },
      });
    });
  }

  private carregar(id: string): void {
    this.isLoading.set(true);
    this.teachersService.buscarPorId(id).subscribe({
      next: (professor) => {
        this.isLoading.set(false);
        this.professor.set(professor);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar professor.'),
          'Fechar',
          { duration: 4000 },
        );
        this.back();
      },
    });
  }

  private carregarAlocacoes(id: string): void {
    this.isLoadingAllocations.set(true);
    this.teachersService.listarAlocacoes(id).subscribe({
      next: (allocations) => {
        this.allocations.set(
          [...allocations].sort((left, right) =>
            `${left.turmaNome} ${left.disciplinaNome}`.localeCompare(
              `${right.turmaNome} ${right.disciplinaNome}`,
              'pt-BR',
            ),
          ),
        );
        this.isLoadingAllocations.set(false);
      },
      error: (error: unknown) => {
        this.isLoadingAllocations.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar vínculos do professor.'),
          'Fechar',
          { duration: 4000 },
        );
      },
    });
  }
}
