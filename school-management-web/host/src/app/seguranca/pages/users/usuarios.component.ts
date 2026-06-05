import { ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AuthStateService } from '../../../core/auth/auth-state.service';
import { hasPermission } from '../../../core/auth/permission.util';
import { HasPermissionDirective } from '../../../compartilhado/directives/has-permission.directive';
import { User } from '../../models/user.model';
import { AccessAdminService } from '../../services/access-admin.service';
import { UsuarioDialogEditComponent } from './dialog/edit-insert/usuario-dialog-edit';
import { UsuarioDialogReadComponent } from './dialog/read/usuario-dialog-read';

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
    MatDividerModule,
  ],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.scss'],
})
export class UsuariosComponent implements OnInit {
  private service = inject(AccessAdminService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
  private authState = inject(AuthStateService);

  hasAnyReadPermission = true;
  dataSource = new MatTableDataSource<User>([]);
  displayedColumns = ['nome', 'login', 'email', 'ativo', 'actions'];

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

  usuarios$!: Observable<User[]>;
  private refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.usuarios$ = this.refresh$.pipe(
      switchMap(() => {
        this.hasAnyReadPermission = this.hasPermission('READ_ALL');

        if (this.hasAnyReadPermission) {
          return this.service.listarUsuarios();
        }

        return of([]);
      }),
      tap((data) => {
        this.dataSource.data = data;
        this.cdr.detectChanges();
      }),
    );
  }

  loadAll(): void {
    this.refresh$.next();
  }

  openDialogCreate(): void {
    const dialogRef = this.dialog.open(UsuarioDialogEditComponent, {
      width: '650px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadAll();
    });
  }

  openDialogEdit(u: User): void {
    const dialogRef = this.dialog.open(UsuarioDialogEditComponent, {
      width: '650px',
      data: u,
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.loadAll();
    });
  }

  delete(id: string): void {
    const confirmed = window.confirm('Tem certeza que deseja excluir este usuário?');
    if (!confirmed) return;

    this.service.excluirUsuario(id).subscribe({
      next: () => {
        this.loadAll();
        this.snackBar.open('Usuário excluído com sucesso!', 'Fechar', { duration: 4000 });
      },
      error: () => this.snackBar.open('Erro ao excluir usuário.', 'Fechar', { duration: 5000 }),
    });
  }

  openDialogDetails(u: User): void {
    this.dialog.open(UsuarioDialogReadComponent, {
      width: '500px',
      data: u,
      disableClose: false,
    });
  }

  private hasPermission(permission: string): boolean {
    return hasPermission(this.authState.getPermissions(), permission);
  }
}
