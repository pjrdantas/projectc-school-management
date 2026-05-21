import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Permission } from '../../../../models/permission.model';

@Component({
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './permissao-dialog-edit.component.html',
  styleUrl: './permissao-dialog-edit.component.scss',
})
export class PermissaoDialogEditComponent {
  private fb = inject(FormBuilder);

  form = this.fb.group({
    codigo: ['', Validators.required],
    descricao: [''],
  });

  constructor(
    private dialogRef: MatDialogRef<PermissaoDialogEditComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Permission | null,
  ) {
    if (data) this.form.patchValue({ codigo: data.codigo, descricao: data.descricao ?? '' });
  }

  submit() {
    if (this.form.invalid) return;
    this.dialogRef.close(this.form.getRawValue());
  }
}
