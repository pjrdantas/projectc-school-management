import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { AccessAdminService } from '../../../services/access-admin.service';
import { User } from '../../../models/user.model';
import { Profile } from '../../../models/profile.model';

@Component({
  standalone: true,
  selector: 'app-auth-users-detail',
  imports: [CommonModule, MatButtonModule, MatCardModule],
  templateUrl: './auth-users-detail.component.html',
})
export class AuthUsersDetailComponent implements OnInit {
  private service = inject(AccessAdminService);
  private router = inject(Router);

  usuario?: User;
  profileNames: string[] = [];

  ngOnInit() {
    const cached = this.service.currentUser();
    if (cached) {
      this.usuario = cached;
      if ((cached.perfis ?? []).length) {
        this.profileNames = cached.perfis ?? [];
      } else {
        this.loadProfileNames(cached.perfilIds ?? []);
      }
      return;
    }

    const id = this.service.currentUserId();
    if (!id) return;

    this.service.buscarUsuario(id).subscribe(u => {
      this.usuario = u;
      if ((u.perfis ?? []).length) {
        this.profileNames = u.perfis ?? [];
      } else {
        this.loadProfileNames(u.perfilIds ?? []);
      }
    });
  }

  private loadProfileNames(ids: string[]) {
    if (!ids.length) {
      this.profileNames = [];
      return;
    }

    this.service.listarPerfis().subscribe(all => {
      const byId = new Map(all.map((p: Profile) => [p.id, p.nome]));
      this.profileNames = ids.map(id => byId.get(id) ?? id);
    });
  }

  voltar() { this.router.navigate(['/auth/users']); }
}
