import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Permission } from '../../../../models/permission.model';

@Component({
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  templateUrl: './permissao-dialog-read.component.html',
  styleUrl: './permissao-dialog-read.component.scss',
})
export class PermissaoDialogReadComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: Permission) {}
}
