import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { AccessAdminService } from '../../../../services/access-admin.service';
import { Permission } from '../../../../models/permission.model';
import { Profile } from '../../../../models/profile.model';

@Component({
  selector: 'app-perfil-dialog-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule, MatSelectModule, MatOptionModule, MatTableModule],
  templateUrl: './perfil-dialog-edit.html',
  styleUrls: ['./perfil-dialog-edit.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilDialogEditComponent implements OnInit {
  private fb = inject(FormBuilder);
  private s = inject(AccessAdminService);
  dialogRef = inject(MatDialogRef<PerfilDialogEditComponent>);
  private cdr = inject(ChangeDetectorRef);
  constructor(@Inject(MAT_DIALOG_DATA) public data: Profile | null) {}

  permissions: Permission[] = [];
  selectedPermissions: Permission[] = [];
  displayedColumns = ['codigo', 'actions'];

  form = this.fb.group({
    nome: ['', Validators.required],
    permissaoSelecionada: [''],
  });

  ngOnInit() {
    if (this.data) {
      this.form.patchValue({ nome: this.data.nome });
    }
    this.s.listarPermissoes().subscribe((p: Permission[]) => {
      this.permissions = p;
      const ids = this.data?.permissaoIds ?? [];
      this.selectedPermissions = p.filter((x: Permission) => ids.includes(x.id));
      this.cdr.detectChanges();
    });
  }

  addPermissao() {
    const id = this.form.value.permissaoSelecionada;
    if (!id) return;
    const p = this.permissions.find((x) => x.id === id);
    if (!p || this.selectedPermissions.some((s) => s.id === id)) return;
    this.selectedPermissions = [...this.selectedPermissions, p];
    this.form.patchValue({ permissaoSelecionada: '' });
  }

  removePermissao(p: Permission) {
    this.selectedPermissions = this.selectedPermissions.filter((x) => x.id !== p.id);
  }

  save() {
    if (this.form.invalid) return;
    const nome = this.form.value.nome?.trim() || '';
    this.dialogRef.close({
      codigo: nome,
      nome,
      permissaoIds: this.selectedPermissions.map((p) => p.id),
    });
  }

  cancel() {
    this.dialogRef.close();
  }
}
