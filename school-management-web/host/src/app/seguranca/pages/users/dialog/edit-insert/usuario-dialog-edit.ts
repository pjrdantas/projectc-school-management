import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { Profile } from '../../../../models/profile.model';
import { User, UserInput } from '../../../../models/user.model';
import { AccessAdminService } from '../../../../services/access-admin.service';

@Component({
  selector: 'app-usuario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule,
    MatSlideToggleModule,
  ],
  templateUrl: './usuario-dialog-edit.html',
  styleUrls: ['./usuario-dialog-edit.scss'],
})
export class UsuarioDialogEditComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<UsuarioDialogEditComponent>);
  public data = inject<User | null>(MAT_DIALOG_DATA, { optional: true });

  private service = inject(AccessAdminService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  perfisDisponiveis: Profile[] = [];

  form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senhaHash: ['', this.data ? [] : [Validators.required, Validators.minLength(6)]],
    ativo: [true],
    perfilIds: [[] as string[], Validators.required],
  });

  ngOnInit(): void {
    this.loadPerfis();

    if (this.data) {
      this.form.patchValue({
        nome: this.data.nome,
        username: this.data.username,
        email: this.data.email,
        ativo: this.data.ativo,
        perfilIds: this.data.perfilIds ?? [],
      });
    }
  }

  private loadPerfis(): void {
    this.service.listarPerfis().subscribe({
      next: (perfis) => (this.perfisDisponiveis = perfis),
      error: () => this.snackBar.open('Erro ao carregar perfis.', 'Fechar', { duration: 4000 }),
    });
  }

  save(): void {
    if (this.form.invalid) return;

    const rawValue = this.form.getRawValue();
    const usuarioRequest: UserInput = {
      nome: rawValue.nome,
      username: rawValue.username,
      email: rawValue.email,
      senhaHash: rawValue.senhaHash,
      ativo: rawValue.ativo,
      perfilIds: rawValue.perfilIds,
    };

    const request$ = this.data?.id
      ? this.service.atualizarUsuario(this.data.id, usuarioRequest)
      : this.service.criarUsuario(usuarioRequest);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          `Usuário ${this.data ? 'atualizado' : 'criado'} com sucesso!`,
          'Fechar',
          { duration: 4000 },
        );
        this.dialogRef.close(true);
      },
      error: (err: any) => {
        const message = err.error?.message || 'Erro ao processar requisição.';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
