import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { UsuarioResponse } from '../../../../models/usuario-gestao.model';

@Component({
  selector: 'app-usuario-dialog-read',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule
  ],
  templateUrl: './usuario-dialog-read.html',
  styleUrls: ['./usuario-dialog-read.scss']
})
export class UsuarioDialogReadComponent {
  public dialogRef = inject(MatDialogRef<UsuarioDialogReadComponent>);
  public data = inject<UsuarioResponse>(MAT_DIALOG_DATA);

    constructor() {
    // Adicione este log para ver o que exatamente está chegando no diálogo
    console.log('Dados recebidos no Dialog Read:', this.data);
  }

  close(): void {
    this.dialogRef.close();
  }
}
