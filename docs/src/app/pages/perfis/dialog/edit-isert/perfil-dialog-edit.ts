import {
  Component,
  inject,
  OnInit,
  ViewChild,
  AfterViewInit,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule,
  MatDialog
} from '@angular/material/dialog';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PerfilResponse } from '../../../../models/perfil-response.model';
import { Permissao } from '../../../../models/permissao.model';
import { PermissaoService } from '../../../../services/permissao.service';

import { ConfirmationDialogComponent } from '../../../../shared/dialog-confirmacao/confirmation-dialog.component';
import { ConfirmationDialogData } from '../../../../models/confirmation-dialog.model';

@Component({
  selector: 'app-perfil-dialog-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatGridListModule,
    MatSnackBarModule
  ],
  templateUrl: './perfil-dialog-edit.html',
  styleUrls: ['./perfil-dialog-edit.scss']
})
export class PerfilDialogEditComponent implements OnInit, AfterViewInit {

  private dialogRef = inject(MatDialogRef<PerfilDialogEditComponent>);
  public data = inject<PerfilResponse | null>(MAT_DIALOG_DATA, { optional: true });

  private permissaoService = inject(PermissaoService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef); // ✅ FIX NG0100

  /** Formulário */
  form = this.fb.nonNullable.group({
    nmPerfil: ['', Validators.required],
    permissaoSelecionada: [null as number | null]
  });

  /** Listas e tabela */
  permissoesDisponiveis: Permissao[] = [];
  dataSource = new MatTableDataSource<Permissao>([]);
  displayedColumns = ['id', 'nmPermissao', 'actions'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    if (this.data) {
      this.form.patchValue({
        nmPerfil: this.data.nmPerfil
      });

      // mantém imutável
      this.dataSource.data = [...this.data.permissoes];
    }

    this.loadPermissoes();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
  }

  /** 🔥 CORRIGIDO: evita ExpressionChangedAfterItHasBeenCheckedError */
  private loadPermissoes(): void {
    this.permissaoService.listAll().subscribe(permissoes => {
      this.permissoesDisponiveis = permissoes;
      this.cdr.detectChanges(); // ✅ ponto-chave
    });
  }

  /** Adiciona permissão */
  addPermissao(): void {
    const id = this.form.value.permissaoSelecionada;
    if (!id) return;

    const permissao = this.permissoesDisponiveis.find(p => p.id === id);
    if (!permissao) return;

    const jaExiste = this.dataSource.data.some(p => p.id === id);
    if (jaExiste) {
      this.snackBar.open('Permissão já adicionada.', 'Fechar', { duration: 3000 });
      return;
    }

    const dialogData: ConfirmationDialogData = {
      message: `Adicionar a permissão "${permissao.nmPermissao}"?`,
      confirmButtonText: 'Adicionar',
      confirmButtonColor: 'primary'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.dataSource.data = [...this.dataSource.data, permissao];

          this.snackBar.open(
            'Permissão adicionada com sucesso!',
            'Fechar',
            { duration: 4000 }
          );

          this.form.patchValue({ permissaoSelecionada: null });
        }
      });
  }

  /** Remove permissão */
  removePermissao(permissao: Permissao): void {
    const dialogData: ConfirmationDialogData = {
      message: `Remover a permissão "${permissao.nmPermissao}"?`,
      confirmButtonText: 'Remover',
      confirmButtonColor: 'warn'
    };

    this.dialog.open(ConfirmationDialogComponent, { data: dialogData })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) {
          this.dataSource.data =
            this.dataSource.data.filter(p => p.id !== permissao.id);

          this.snackBar.open(
            'Permissão removida com sucesso!',
            'Fechar',
            { duration: 4000 }
          );
        }
      });
  }

  /** Salva perfil */
  save(): void {
    if (this.form.invalid) return;

    this.dialogRef.close({
      nmPerfil: this.form.getRawValue().nmPerfil,
      permissoesIds: this.dataSource.data.map(p => p.id!)
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
