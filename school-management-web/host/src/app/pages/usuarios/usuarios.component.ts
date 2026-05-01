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
import { MatChipsModule } from '@angular/material/chips';

import { UsuarioService } from '../../services/usuario.service';
import { UsuarioResponse } from '../../models/usuario-gestao.model';
import { UsuarioDialogEditComponent } from './dialog/edit-insert/usuario-dialog-edit';
import { UsuarioDialogReadComponent } from './dialog/read/usuario-dialog-read';
import { ConfirmationDialogComponent } from '../../shared/dialog-confirmacao/confirmation-dialog.component';
import { ConfirmationDialogData } from '../../models/confirmation-dialog.model';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';
import { AuthService } from '../../core/auth/auth.service'; // Adicionado
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-usuarios',
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
    MatChipsModule,
    HasPermissionDirective,
    MatDividerModule
  ],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.scss']
})
export class UsuariosComponent implements OnInit {
  private service = inject(UsuarioService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  private authService = inject(AuthService);

  hasAnyReadPermission = true; // Adicionado
  dataSource = new MatTableDataSource<UsuarioResponse>([]);
  displayedColumns = ['id', 'nome', 'login', 'email', 'ativo', 'actions'];

  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
    }
  }

  @ViewChild(MatPaginator) set matPaginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  usuarios$!: Observable<UsuarioResponse[]>;
  private refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.usuarios$ = this.refresh$.pipe(
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

  loadAll(): void {
    this.refresh$.next();
  }

  openDialogCreate(): void {
    const dialogRef = this.dialog.open(UsuarioDialogEditComponent, {
      width: '650px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadAll();
    });
  }

  openDialogEdit(u: UsuarioResponse): void {
    const dialogRef = this.dialog.open(UsuarioDialogEditComponent, {
      width: '650px',
      data: u,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadAll();
    });
  }

  delete(id: number): void {
    const dialogData: ConfirmationDialogData = {
      message: 'Tem certeza que deseja excluir este usuário?',
      confirmButtonText: 'Excluir',
      confirmButtonColor: 'warn'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed().subscribe(confirmed => {
        if (confirmed) {
          this.service.delete(id).subscribe({
            next: () => {
              this.loadAll();
              this.snackBar.open('Usuário excluído com sucesso!', 'Fechar', { duration: 4000 });
            },
            error: () => this.snackBar.open('Erro ao excluir usuário.', 'Fechar', { duration: 5000 })
          });
        }
      });
  }

  openDialogDetails(u: UsuarioResponse): void {
    this.dialog.open(UsuarioDialogReadComponent, {
      width: '500px',
      data: u,
      disableClose: false // Permitir fechar clicando fora já que é só leitura
    });
  }
}
