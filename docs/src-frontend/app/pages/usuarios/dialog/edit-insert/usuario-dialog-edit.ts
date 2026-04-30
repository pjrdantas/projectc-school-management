import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { UsuarioResponse, UsuarioRequest } from '../../../../models/usuario-gestao.model';
import { PerfilResponse } from '../../../../models/perfil-response.model';
import { UsuarioService } from '../../../../services/usuario.service';
import { PerfilService } from '../../../../services/perfil.service';

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
    MatSlideToggleModule
  ],
  templateUrl: './usuario-dialog-edit.html',
  styleUrls: ['./usuario-dialog-edit.scss']
})
export class UsuarioDialogEditComponent implements OnInit {
  // Injeções seguindo o padrão PerfilDialog
  private dialogRef = inject(MatDialogRef<UsuarioDialogEditComponent>);
  public data = inject<UsuarioResponse | null>(MAT_DIALOG_DATA, { optional: true });

  private usuarioService = inject(UsuarioService);
  private perfilService = inject(PerfilService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  perfisDisponiveis: PerfilResponse[] = [];

  /**
   * Formulário ajustado para o UsuarioRequest do Java
   * Nota: 'ativo' no backend é 'S'/'N', mas no form usamos boolean para o slide-toggle
   */
  form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    login: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', this.data ? [] : [Validators.required, Validators.minLength(6)]],
    ativo: [true],
    perfisIds: [[] as number[], Validators.required]
  });

 ngOnInit(): void {
  this.loadPerfis();
  if (this.data) {
    this.form.patchValue({
      nome: this.data.nome,
      login: this.data.login,
      email: this.data.email,
      ativo: this.data.ativo === 'S',
      perfisIds: this.data.perfisIds.map(p => p.id)
    });
  }
}


  private loadPerfis(): void {
    this.perfilService.listAll().subscribe({
      next: (perfis) => this.perfisDisponiveis = perfis,
      error: () => this.snackBar.open('Erro ao carregar perfis.', 'Fechar', { duration: 4000 })
    });
  }

  save(): void {
    if (this.form.invalid) return;

    const rawValue = this.form.getRawValue();

    const usuarioRequest: UsuarioRequest = {
      nome: rawValue.nome,
      login: rawValue.login,
      email: rawValue.email,
      senha: rawValue.senha,
      ativo: rawValue.ativo ? 'S' : 'N',
      perfisIds: rawValue.perfisIds
    };

    // Lógica de Create ou Update
    const request$ = this.data?.id
      ? this.usuarioService.update(this.data.id, usuarioRequest)
      : this.usuarioService.create(usuarioRequest);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          `Usuário ${this.data ? 'atualizado' : 'criado'} com sucesso!`,
          'Fechar',
          { duration: 4000 }
        );
        this.dialogRef.close(true);
      },
      error: (err: any) => {
        const message = err.error?.message || 'Erro ao processar requisição.';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
