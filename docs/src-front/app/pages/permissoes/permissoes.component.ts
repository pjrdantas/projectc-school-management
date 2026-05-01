import { Component, OnInit, ViewChild, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, BehaviorSubject, of } from 'rxjs'; // Adicionado of
import { switchMap, tap } from 'rxjs/operators';

import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { PermissaoService } from '../../services/permissao.service';
import { Permissao } from '../../models/permissao.model';
import { PermissaoDialogComponent } from './dialog/permissao-dialog';
import { ConfirmationDialogComponent } from '../../shared/dialog-confirmacao/confirmation-dialog.component';
import { ConfirmationDialogData } from '../../models/confirmation-dialog.model';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';
import { AuthService } from '../../core/auth/auth.service'; // Adicionado
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-permissoes',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDialogModule,
    MatSortModule,
    MatPaginatorModule,
    MatSnackBarModule,
    MatTooltipModule,
    HasPermissionDirective,
    MatDividerModule
  ],
  templateUrl: './permissoes.component.html',
  styleUrls: ['./permissoes.component.scss']
})
export class PermissoesComponent implements OnInit {
  private service = inject(PermissaoService);
  private authService = inject(AuthService); // Adicionado
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  hasAnyReadPermission = true; // Adicionado
  dataSource = new MatTableDataSource<Permissao>([]);
  displayedColumns: string[] = ['id', 'nmPermissao', 'actions'];

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
      if (!this.dataSource.sort.active) {
        this.setDefaultSort(sort);
      }
    }
  }

  @ViewChild(MatPaginator) set matPaginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  permissoes$!: Observable<Permissao[]>;
  private refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.permissoes$ = this.refresh$.pipe(
      switchMap(() => {
        // Verifica se tem a permissão para listar
        this.hasAnyReadPermission = !!this.authService.hasPermission('READ_ALL');

        if (this.hasAnyReadPermission) {
          return this.service.listAll();
        }
        return of([]); // Retorna vazio se não tiver permissão
      }),
      tap(data => {
        this.dataSource.data = data;
        this.cdr.detectChanges();
      })
    );
  }

  private setDefaultSort(sort: MatSort) {
    setTimeout(() => {
      sort.active = 'id';
      sort.direction = 'asc';
      sort.sortChange.emit();
      this.cdr.detectChanges();
    });
  }

  loadAll(): void {
    this.refresh$.next();
  }

  openDialogCreate(): void {
    const dialogRef = this.dialog.open(PermissaoDialogComponent, {
      width: '500px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.create(result).subscribe({
          next: () => {
            this.loadAll();
            this.snackBar.open('Permissão criada com sucesso!', 'Fechar', { duration: 5000 });
          },
          error: () => this.snackBar.open('Erro ao criar permissão.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' })
        });
      }
    });
  }

  openDialogEdit(p: Permissao): void {
    const dialogRef = this.dialog.open(PermissaoDialogComponent, {
      width: '500px',
      data: p,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && p.id) {
        this.service.update(p.id, result).subscribe({
          next: () => {
            this.loadAll();
            this.snackBar.open('Permissão atualizada com sucesso!', 'Fechar', { duration: 5000 });
          },
          error: () => this.snackBar.open('Erro ao atualizar permissão.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' })
        });
      }
    });
  }

  delete(id: number): void {
    const dialogData: ConfirmationDialogData = {
      message: 'Tem certeza que deseja excluir esta permissão?',
      confirmButtonText: 'Excluir',
      confirmButtonColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, { data: dialogData });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.service.delete(id).subscribe({
          next: () => {
            this.loadAll();
            this.snackBar.open('Permissão excluída com sucesso!', 'Fechar', { duration: 5000 });
          },
          error: () => this.snackBar.open('Erro ao excluir. Verifique permissões.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' })
        });
      }
    });
  }
}
