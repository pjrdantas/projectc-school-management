import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AccessAdminService } from '../../../services/access-admin.service';
import { User } from '../../../models/user.model';
@Component({
  standalone: true,
  selector: 'app-auth-users-list',
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './auth-users-list.component.html',
  styleUrls: ['./auth-users-list.component.scss'],
})
export class AuthUsersListComponent implements OnInit {
  private service = inject(AccessAdminService);
  private router = inject(Router);
  usuarios: User[] = [];
  ngOnInit() {
    this.service.listarUsuarios().subscribe((r) => (this.usuarios = r));
  }
  novo() {
    this.service.selectUser(null);
    this.router.navigate(['/auth/users/new']);
  }
  detalhe(id: string) {
    const usuario = this.usuarios.find((u) => u.id === id) ?? null;
    this.service.selectUserEntity(usuario);
    this.router.navigate(['/auth/users/detail'], { queryParams: { id } });
  }
  editar(id: string) {
    this.service.selectUser(id);
    this.router.navigate(['/auth/users/edit'], { queryParams: { id } });
  }
  excluir(id: string) {
    this.service.excluirUsuario(id).subscribe(() => this.ngOnInit());
  }
}
