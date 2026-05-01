import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AplicativosResponse } from '../../../../models/aplicativos-response.model';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-aplicativos-dialog-read',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './aplicativos-dialog-read.html',
  styleUrls: ['./aplicativos-dialog-read.scss']
})
export class AplicativosDetailsDialogComponent {
  // Injeções modernas com inject()
  public dialogRef = inject(MatDialogRef<AplicativosDetailsDialogComponent>);
  public data = inject<AplicativosResponse>(MAT_DIALOG_DATA);

  close(): void {
    this.dialogRef.close();
  }
}
