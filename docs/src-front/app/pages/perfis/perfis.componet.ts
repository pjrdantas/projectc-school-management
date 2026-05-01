import { Component, OnInit, ViewChild, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, BehaviorSubject, of } from 'rxjs';
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

import { PerfilService } from '../../services/perfil.service';
import { PerfilResponse } from '../../models/perfil-response.model';
import { ConfirmationDialogComponent } from '../../shared/dialog-confirmacao/confirmation-dialog.component';
import { ConfirmationDialogData } from '../../models/confirmation-dialog.model';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';

import { PerfilDialogEditComponent } from './dialog/edit-isert/perfil-dialog-edit';
import { PerfilDialogReadComponent } from './dialog/read/perfil-dialog-read';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../core/auth/auth.service'; // Adicionado

@Component({
  selector: 'app-perfis',
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
  templateUrl: './perfis.component.html',
  styleUrls: ['./perfis.component.scss']
})
export class PerfisComponent implements OnInit {
  private service = inject(PerfilService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  hasAnyReadPermission = true; // Adicionado
  dataSource = new MatTableDataSource<PerfilResponse>([]);
  displayedColumns: string[] = ['id', 'nmPerfil',  'actions'];

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

  perfis$!: Observable<PerfilResponse[]>;
  private refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.perfis$ = this.refresh$.pipe(
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
  const dialogRef = this.dialog.open(PerfilDialogEditComponent, {
    width: '700px',
    disableClose: true
  });

  dialogRef.afterClosed().subscribe(result => {
    if (!result) return;

    const dialogData: ConfirmationDialogData = {
      message: 'Deseja salvar o novo perfil?',
      confirmButtonText: 'Salvar',
      confirmButtonColor: 'primary'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.service.create(result).subscribe({
            next: () => {
              this.loadAll();
              this.snackBar.open(
                'Perfil criado com sucesso!',
                'Fechar',
                { duration: 5000 }
              );
            },
            error: () => {
              this.snackBar.open(
                'Erro ao criar perfil.',
                'Fechar',
                { duration: 8000, panelClass: 'error-snackbar' }
              );
            }
          });
        }
      });
  });
}


  openDialogEdit(perfil: PerfilResponse): void {
  const dialogRef = this.dialog.open(PerfilDialogEditComponent, {
    width: '700px',
    data: perfil,
    disableClose: true
  });

  dialogRef.afterClosed().subscribe(result => {
    if (!result || !perfil.id) return;

    const dialogData: ConfirmationDialogData = {
      message: 'Deseja salvar as alterações do perfil?',
      confirmButtonText: 'Salvar',
      confirmButtonColor: 'primary'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.service.update(perfil.id!, result).subscribe({
            next: () => {
              this.loadAll();
              this.snackBar.open(
                'Perfil atualizado com sucesso!',
                'Fechar',
                { duration: 5000 }
              );
            },
            error: () => {
              this.snackBar.open(
                'Erro ao atualizar perfil.',
                'Fechar',
                { duration: 8000, panelClass: 'error-snackbar' }
              );
            }
          });
        }
      });
  });
}


  openDialogDetails(perfil: PerfilResponse): void {
    this.dialog.open(PerfilDialogReadComponent, {
      width: '600px',
      data: perfil
    });
  }

  delete(id: number): void {
    const dialogData: ConfirmationDialogData = {
      message: 'Deseja excluir este perfil?',
      confirmButtonText: 'Excluir',
      confirmButtonColor: 'warn'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed().subscribe(confirmed => {
        if (confirmed) {
          this.service.delete(id).subscribe({
            next: () => {
              this.loadAll();
              this.snackBar.open('Excluído com sucesso!', 'Fechar', { duration: 5000 });
            },
            error: () => this.snackBar.open('Erro ao excluir.', 'Fechar', { panelClass: 'error-snackbar' })
          });
        }
      });
  }
}
