import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { Profile } from '../../../../models/profile.model';

@Component({
  selector: 'app-perfil-dialog-read',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatChipsModule, MatDividerModule, MatIconModule],
  templateUrl: './perfil-dialog-read.html',
  styleUrls: ['./perfil-dialog-read.scss'],
})
export class PerfilDialogReadComponent {
  constructor(public dialogRef: MatDialogRef<PerfilDialogReadComponent>, @Inject(MAT_DIALOG_DATA) public data: Profile) {}
}
