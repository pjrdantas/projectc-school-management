import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AccessAdminService } from '../../../services/access-admin.service';
import { Profile } from '../../../models/profile.model';
import { PerfilDialogEditComponent } from './dialog/edit-isert/perfil-dialog-edit';
import { PerfilDialogReadComponent } from './dialog/read/perfil-dialog-read';

@Component({
  selector: 'app-perfis',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatDialogModule],
  templateUrl: './perfis.component.html',
  styleUrls: ['./perfis.component.scss'],
})
export class PerfisComponent implements OnInit {
  private s = inject(AccessAdminService);
  private dialog = inject(MatDialog);

  perfis: Profile[] = [];

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.s.listarPerfis().subscribe((v) => (this.perfis = v));
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
}
