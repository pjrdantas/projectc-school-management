import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AccessAdminService } from '../../services/access-admin.service';
import { Permission } from '../../models/permission.model';
import { PermissaoDialogEditComponent } from './dialog/edit-insert/permissao-dialog-edit.component';
import { PermissaoDialogReadComponent } from './dialog/read/permissao-dialog-read.component';

@Component({
  selector: 'app-permissoes',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatDialogModule],
  templateUrl: './permissoes.component.html',
  styleUrls: ['./permissoes.component.scss'],
})
export class PermissoesComponent implements OnInit {
  private s = inject(AccessAdminService);
  private dialog = inject(MatDialog);

  permissoes: Permission[] = [];

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.s.listarPermissoes().subscribe((v) => (this.permissoes = v));
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

  openDialogDetails(permissao: Permission) {
    this.dialog.open(PermissaoDialogReadComponent, { width: '600px', data: permissao });
  }

  delete(id: string) {
    this.s.excluirPermissao(id).subscribe(() => this.loadAll());
  }

  isAdmin(codigo?: string) {
    return (codigo || '').toUpperCase() === 'ADMIN';
  }
}
