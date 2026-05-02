import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { AccessAdminService } from '../../../services/access-admin.service';
import { Profile } from '../../../models/profile.model';
import { Permission } from '../../../models/permission.model';

@Component({
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule],
  templateUrl: './auth-profiles-detail.component.html',
})
export class AuthProfilesDetailComponent implements OnInit {
  private s = inject(AccessAdminService);
  private r = inject(Router);

  perfil?: Profile;
  permissionNames: string[] = [];

  ngOnInit() {
    const id = this.s.currentProfileId();
    if (!id) return;

    this.s.buscarPerfil(id).subscribe(v => {
      this.perfil = v;
      this.loadPermissionNames(v.permissaoIds ?? []);
    });
  }

  private loadPermissionNames(ids: string[]) {
    if (!ids.length) {
      this.permissionNames = [];
      return;
    }

    this.s.listarPermissoes().subscribe(all => {
      const byId = new Map(all.map((p: Permission) => [p.id, p.codigo]));
      this.permissionNames = ids.map(id => byId.get(id) ?? id);
    });
  }

  voltar() { this.r.navigate(['/auth/profiles']); }
}
