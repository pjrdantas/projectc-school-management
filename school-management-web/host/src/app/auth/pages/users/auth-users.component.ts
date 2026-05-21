import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { AccessAdminService } from '../../services/access-admin.service';

@Component({
  selector: 'app-auth-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatInputModule],
  templateUrl: './auth-users.component.html',
  styleUrls: ['./auth-users.component.scss'],
})
export class AuthUsersComponent implements OnInit {
  private service = inject(AccessAdminService);
  private fb = inject(FormBuilder);
  usuarios: any[] = [];
  form = this.fb.group({
    username: ['', Validators.required],
    nome: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senhaHash: ['', Validators.required],
  });

  ngOnInit(): void {
    this.carregar();
  }
  carregar() {
    this.service.listarUsuarios().subscribe((r) => (this.usuarios = r as any[]));
  }
  salvar() {
    if (this.form.invalid) return;
    this.service.criarUsuario({ ...(this.form.value as any), ativo: true }).subscribe(() => {
      this.form.reset();
      this.carregar();
    });
  }
}
