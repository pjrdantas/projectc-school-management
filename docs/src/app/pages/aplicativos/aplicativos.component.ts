import { Component, OnInit, ViewChild, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, BehaviorSubject } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AplicativosService } from '../../services/aplicativos.service';
import { AplicativosResponse } from '../../models/aplicativos-response.model';
import { AplicativosDialogComponent } from './dialog/edit-insert/aplicativos-dialog-edit';
import { ConfirmationDialogComponent } from '../../shared/dialog-confirmacao/confirmation-dialog.component';
import { ConfirmationDialogData } from '../../models/confirmation-dialog.model';
import { AplicativosDetailsDialogComponent } from './dialog/read/aplicativos-dialog-read';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';
import { AuthService } from '../../core/auth/auth.service'; // Ajuste o caminho se necessário
import { of } from 'rxjs';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-aplicativos',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDialogModule,
    MatSortModule,
    MatPaginatorModule,
    MatSnackBarModule,
    MatTooltipModule,
    HasPermissionDirective,
    MatDividerModule
  ],
  templateUrl: './aplicativos.component.html',
  styleUrls: ['./aplicativos.component.scss']
})
export class AplicativosComponent implements OnInit {
  private service = inject(AplicativosService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);
hasAnyReadPermission = true;
  dataSource = new MatTableDataSource<AplicativosResponse>([]);
  displayedColumns = ['id', 'nome', 'descricao', 'url', 'ativo', 'criadoEm', 'atualizadoEm', 'actions'];

  // ✅ Uso de Setters para garantir o vínculo com componentes que podem estar dentro de um @if
  @ViewChild(MatSort) set matSort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
      if (!this.dataSource.sort.active) {
        this.setDefaultSort(sort);
      }
    }
  }

  @ViewChild(MatPaginator) set matPaginator(paginator: MatPaginator) {
    if (paginator) {
      this.dataSource.paginator = paginator;
    }
  }

  aplicativos$!: Observable<AplicativosResponse[]>;
  private refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.aplicativos$ = this.refresh$.pipe(
      switchMap(() => {
        const hasReadAll = !!this.authService.hasPermission('READ_ALL');
        const hasReadActive = !!this.authService.hasPermission('READ_ACTIVE');

        // ATUALIZA A VARIÁVEL DE CONTROLE:
        this.hasAnyReadPermission = hasReadAll || hasReadActive;

        if (hasReadAll) {
          return this.service.listAll();
        }

        if (hasReadActive) {
          return this.service.listActive();
        }

        return of([]);
      }),
      tap(data => {
        this.dataSource.data = data;
        this.cdr.detectChanges();
      })
    );
  }



  private setDefaultSort(sort: MatSort) {
    setTimeout(() => {
      sort.active = 'id';
      sort.direction = 'asc';
      sort.sortChange.emit();
      this.cdr.detectChanges();
    });
  }

  loadAll(): void {
    this.refresh$.next();
  }

  openDialogCreate(): void {
    const dialogRef = this.dialog.open(AplicativosDialogComponent, {
      width: '600px',
      height: '430px',
      maxWidth: '90vw',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.service.create(result).subscribe({
          next: () => {
            this.loadAll();
            this.snackBar.open('Microfrontend criado com sucesso!', 'Fechar', { duration: 5000 });
          },
          error: () => this.snackBar.open('Erro ao criar microfrontend.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' })
        });
      }
    });
  }

 openDialogEdit(m: AplicativosResponse): void {
  const dialogRef = this.dialog.open(AplicativosDialogComponent, {
    width: '600px',
    height: '430px',
    maxWidth: '90vw',
    data: { ...m },
    disableClose: true
  });

  dialogRef.afterClosed().subscribe(result => {

    if (result && result.id) {

      this.service.update(result.id, result).subscribe({
        next: () => {
          this.loadAll();
          this.snackBar.open('Atualizado com sucesso!', 'Fechar', { duration: 5000 });
        },
        error: (err) => {
          console.error('Erro no update:', err);
          this.snackBar.open('Erro ao atualizar.', 'Fechar', { duration: 8000 });
        }
      });
    }
  });
}


  delete(id: number): void {
    const dialogData: ConfirmationDialogData = {
      message: 'Tem certeza que deseja excluir este microfrontend?',
      confirmButtonText: 'Excluir',
      confirmButtonColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, { data: dialogData });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.service.delete(id).subscribe({
          next: () => {
            this.loadAll();
            this.snackBar.open('Microfrontend excluído com sucesso!', 'Fechar', { duration: 5000 });
          },
          error: () => this.snackBar.open('Erro ao excluir. Verifique permissões.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' })
        });
      }
    });
  }

  openDialogDetails(m: AplicativosResponse): void {
    this.dialog.open(AplicativosDetailsDialogComponent, {
      width: '620px',
      height: '543px',
      maxWidth: '90vw',
      data: m,
      disableClose: true
    });
  }
}
