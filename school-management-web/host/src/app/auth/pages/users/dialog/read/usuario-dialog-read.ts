import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';

import { Profile } from '../../../../models/profile.model';
import { User } from '../../../../models/user.model';
import { AccessAdminService } from '../../../../services/access-admin.service';

@Component({
  selector: 'app-usuario-dialog-read',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
  ],
  templateUrl: './usuario-dialog-read.html',
  styleUrls: ['./usuario-dialog-read.scss'],
})
export class UsuarioDialogReadComponent implements OnInit {
  public dialogRef = inject(MatDialogRef<UsuarioDialogReadComponent>);
  public data = inject<User>(MAT_DIALOG_DATA);

  private service = inject(AccessAdminService);

  profileNames: string[] = [];

  ngOnInit(): void {
    if ((this.data.perfis ?? []).length) {
      this.profileNames = this.data.perfis ?? [];
      return;
    }

    this.loadProfileNames(this.data.perfilIds ?? []);
  }

  close(): void {
    this.dialogRef.close();
  }

  private loadProfileNames(ids: string[]): void {
    if (!ids.length) {
      this.profileNames = [];
      return;
    }

    this.service.listarPerfis().subscribe((all) => {
      const byId = new Map(all.map((p: Profile) => [p.id, p.nome]));
      this.profileNames = ids.map((id) => byId.get(id) ?? id);
    });
  }
}
