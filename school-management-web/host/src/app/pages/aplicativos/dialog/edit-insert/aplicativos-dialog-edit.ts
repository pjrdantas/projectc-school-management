import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';

import { AplicativosResponse } from '../../../../models/aplicativos-response.model';
import { urlValidator } from '../../../../validators/url-validator';

@Component({
  selector: 'app-aplicativos-dialog-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatIconModule
  ],
  templateUrl: './aplicativos-dialog-edit.html',
  styleUrls: ['./aplicativos-dialog-edit.scss']
})
export class AplicativosDialogComponent {
  private fb = inject(FormBuilder);
  public dialogRef = inject(MatDialogRef<AplicativosDialogComponent>);
  public data = inject<AplicativosResponse>(MAT_DIALOG_DATA, { optional: true });

  isEdit = !!this.data;

  form: FormGroup = this.fb.group({
    nome: [this.data?.nome || '', Validators.required],
    descricao: [this.data?.descricao || '', Validators.required],
    url: [this.data?.url || '', [Validators.required, urlValidator()]],
    moduleName: [this.data?.moduleName || '', Validators.required],
    exposedModule: [this.data?.exposedModule || '', Validators.required],
    routePath: [this.data?.routePath || '', Validators.required],
    // GARANTIA: Converte 'S' em true e qualquer outra coisa em false
    ativo: [this.data?.ativo === 'S']
  });

  save(): void {
    if (this.form.invalid) return;

    // Pegamos os valores brutos do formulário
    const rawValue = this.form.getRawValue();

    // Montamos o payload convertendo o boolean de volta para 'S' ou 'N'
    const payload = {
      ...rawValue,
      id: this.data?.id,
      ativo: rawValue.ativo === true ? 'S' : 'N'
    };

    // LOG PARA VOCÊ CONFERIR NO CONSOLE:
    console.log('VALOR DO ATIVO ENVIADO:', payload.ativo);

    this.dialogRef.close(payload);
  }

  close(): void {
    this.dialogRef.close();
  }
}
