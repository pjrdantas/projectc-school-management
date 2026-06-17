import { NgFor, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface MessageDialogData {
  title: string;
  message: string;
  details?: string[];
  confirmLabel?: string;
  cancelLabel?: string;
  icon?: string;
  tone?: 'info' | 'success' | 'warning' | 'danger';
}

@Component({
  selector: 'app-message-dialog',
  standalone: true,
  imports: [NgIf, NgFor, MatButtonModule, MatDialogModule, MatIconModule],
  templateUrl: './message-dialog.component.html',
  styleUrls: ['./message-dialog.component.scss'],
})
export class MessageDialogComponent {
  protected readonly data = inject<MessageDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<MessageDialogComponent, boolean>);

  protected readonly tone = this.data.tone ?? 'info';
  protected readonly icon = this.data.icon ?? this.defaultIcon();
  protected readonly confirmLabel = this.data.confirmLabel ?? 'OK';

  constructor() {
    this.dialogRef.disableClose = true;
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  private defaultIcon(): string {
    switch (this.tone) {
      case 'success':
        return 'check_circle';
      case 'warning':
        return 'info';
      case 'danger':
        return 'warning';
      default:
        return 'notifications';
    }
  }
}
