import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { hasPermission } from '../../../core/auth/permission.util';
import { HasPermissionDirective } from '../../../shared/directives/has-permission.directive';
import { AccessAdminService } from '../../services/access-admin.service';
import { Profile } from '../../models/profile.model';
import { PerfilDialogEditComponent } from './dialog/edit-isert/perfil-dialog-edit';
import { PerfilDialogReadComponent } from './dialog/read/perfil-dialog-read';

@Component({
  selector: 'app-perfis',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatDialogModule,
    MatDividerModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
    HasPermissionDirective,
  ],
  templateUrl: './perfis.component.html',
  styleUrls: ['./perfis.component.scss'],
})
export class PerfisComponent implements OnInit {
  private s = inject(AccessAdminService);
  private dialog = inject(MatDialog);
  private authState = inject(AuthStateService);

  perfis: Profile[] = [];
  dataSource = new MatTableDataSource<Profile>([]);
  displayedColumns = ['nome', 'codigo', 'permissoes', 'actions'];
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
      this.perfis = [];
      this.dataSource.data = [];
      return;
    }

    this.s.listarPerfis().subscribe((v) => {
      this.perfis = v;
      this.dataSource.data = v;
    });
  }

  openDialogCreate() {
    this.dialog
      .open(PerfilDialogEditComponent, { width: '700px', disableClose: true })
      .afterClosed()
      .subscribe((result) => {
        if (!result) return;
        this.s.criarPerfil(result).subscribe(() => this.loadAll());
      });
  }

  openDialogEdit(perfil: Profile) {
    this.dialog
      .open(PerfilDialogEditComponent, { width: '700px', disableClose: true, data: perfil })
      .afterClosed()
      .subscribe((result) => {
        if (!result) return;
        this.s.atualizarPerfil(perfil.id, result).subscribe(() => this.loadAll());
      });
  }

  openDialogDetails(perfil: Profile) {
    this.dialog.open(PerfilDialogReadComponent, { width: '600px', data: perfil });
  }

  delete(id: string) {
    this.s.excluirPerfil(id).subscribe(() => this.loadAll());
  }

  private hasPermission(permission: string): boolean {
    return hasPermission(this.authState.getPermissions(), permission);
  }
}
