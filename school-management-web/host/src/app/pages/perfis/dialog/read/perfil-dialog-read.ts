import { Component, Inject, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { PerfilResponse } from '../../../../models/perfil-response.model';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-perfil-dialog-read',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatChipsModule,
    MatDividerModule,
    MatIconModule
  ],
  templateUrl: './perfil-dialog-read.html',
  styleUrls: ['./perfil-dialog-read.scss']
})
export class PerfilDialogReadComponent {
  constructor(
    public dialogRef: MatDialogRef<PerfilDialogReadComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PerfilResponse
  ) {}
}
