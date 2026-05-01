import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { Permissao } from '../../../models/permissao.model';

@Component({
  selector: 'app-permissao-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './permissao-dialog.html',
  styleUrls: ['./permissao-dialog.scss'] // Pode usar o mesmo padrão de estilo dos outros dialogs
})
export class PermissaoDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  public dialogRef = inject(MatDialogRef<PermissaoDialogComponent>);
  public data = inject<Permissao>(MAT_DIALOG_DATA, { optional: true });

  form!: FormGroup;
  isEdit = !!this.data;

  ngOnInit(): void {
    this.form = this.fb.group({
      nmPermissao: [this.data?.nmPermissao || '', [Validators.required]],

    });
  }

  save(): void {
    if (this.form.invalid) return;

    const payload = this.form.getRawValue();

    // Força o nome da permissão para MAIÚSCULO (padrão de segurança)
    payload.nmPermissao = payload.nmPermissao.toUpperCase().trim();

    if (this.isEdit && this.data?.id) {
      payload.id = this.data.id;
    }

    this.dialogRef.close(payload);
  }

  close(): void {
    this.dialogRef.close();
  }
}
