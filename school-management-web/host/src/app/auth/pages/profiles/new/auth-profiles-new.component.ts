import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AccessAdminService } from '../../../services/access-admin.service';
import { Permission } from '../../../models/permission.model';
@Component({
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
  ],
  templateUrl: './auth-profiles-new.component.html',
  styleUrls: ['./auth-profiles-new.component.scss'],
})
export class AuthProfilesNewComponent implements OnInit {
  private fb = inject(FormBuilder);
  private s = inject(AccessAdminService);
  private r = inject(Router);
  private route = inject(ActivatedRoute);
  id = signal<string | null>(null);
  permissions: Permission[] = [];
  form = this.fb.group({
    codigo: ['', Validators.required],
    nome: ['', Validators.required],
    descricao: [''],
    permissaoIds: [[] as string[]],
  });
  ngOnInit() {
    this.s.listarPermissoes().subscribe((p) => (this.permissions = p));
    const id = this.route.snapshot.queryParamMap.get('id') ?? this.s.currentProfileId();
    if (id) {
      this.id.set(id);
      this.s.buscarPerfil(id).subscribe((v) => this.form.patchValue(v));
    }
  }
  salvar() {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const payload = {
      codigo: raw.codigo?.trim(),
      nome: raw.nome?.trim(),
      descricao: raw.descricao,
      permissaoIds: raw.permissaoIds ?? [],
    };
    const obs = this.id()
      ? this.s.atualizarPerfil(this.id()!, payload as any)
      : this.s.criarPerfil(payload as any);
    obs.subscribe(() => this.r.navigate(['/auth/profiles']));
  }
  isAdminPermission(codigo?: string) {
    return (codigo || '').toUpperCase() === 'ADMIN';
  }
  onCancel() {
    this.r.navigate(['/auth/profiles']);
  }
}
