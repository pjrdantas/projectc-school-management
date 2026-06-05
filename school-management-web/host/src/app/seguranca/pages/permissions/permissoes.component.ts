import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { hasPermission } from '../../../core/auth/permission.util';
import { HasPermissionDirective } from '../../../compartilhado/directives/has-permission.directive';
import { AccessAdminService } from '../../services/access-admin.service';
import { Permission } from '../../models/permission.model';
import { PermissaoDialogEditComponent } from './dialog/permissao-dialog-edit.component';

@Component({
  selector: 'app-permissoes',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatDialogModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
    HasPermissionDirective,
  ],
  templateUrl: './permissoes.component.html',
  styleUrls: ['./permissoes.component.scss'],
})
export class PermissoesComponent implements OnInit {
  private s = inject(AccessAdminService);
  private dialog = inject(MatDialog);
  private authState = inject(AuthStateService);

  permissoes: Permission[] = [];
  dataSource = new MatTableDataSource<Permission>([]);
  displayedColumns = ['codigo', 'descricao', 'actions'];
  hasAnyReadPermission = true;

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

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.hasAnyReadPermission = this.hasPermission('READ_ALL');
    if (!this.hasAnyReadPermission) {
      this.permissoes = [];
      this.dataSource.data = [];
      return;
    }

    this.s.listarPermissoes().subscribe((v) => {
      this.permissoes = v;
      this.dataSource.data = v;
    });
  }

  openDialogCreate() {
    this.dialog
      .open(PermissaoDialogEditComponent, { width: '700px', disableClose: true })
      .afterClosed()
      .subscribe((result) => {
        if (!result) return;
        this.s.criarPermissao(result).subscribe(() => this.loadAll());
      });
  }

  openDialogEdit(permissao: Permission) {
    this.dialog
      .open(PermissaoDialogEditComponent, { width: '700px', disableClose: true, data: permissao })
      .afterClosed()
      .subscribe((result) => {
        if (!result) return;
        this.s.atualizarPermissao(permissao.id, result).subscribe(() => this.loadAll());
      });
  }


  delete(id: string) {
    this.s.excluirPermissao(id).subscribe(() => this.loadAll());
  }

  isAdmin(codigo?: string) {
    return (codigo || '').toUpperCase() === 'ADMIN';
  }

  private hasPermission(permission: string): boolean {
    return hasPermission(this.authState.getPermissions(), permission);
  }
}
