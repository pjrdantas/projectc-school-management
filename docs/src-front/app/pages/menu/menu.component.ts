import { Component, OnInit, OnDestroy, ChangeDetectorRef, inject, NgZone } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule, AsyncPipe } from '@angular/common'; // Importes específicos
import { AuthStateService, UsuarioAuth } from '../../core/auth/auth-state.service';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';

import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe, // Essencial para o pipe | async no HTML
    RouterModule,
    MatToolbarModule,
    MatDividerModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatButtonModule,
    HasPermissionDirective
  ],
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  public authState = inject(AuthStateService);
  private cdr = inject(ChangeDetectorRef);
  private zone = inject(NgZone);

  usuario: UsuarioAuth | null = null;
  dataHoraFormatada = '';
  private timerId: ReturnType<typeof window.setInterval> | null = null;

  ngOnInit(): void {
    this.usuario = this.authState.getUsuario();

    this.authState.usuario$.subscribe((u: UsuarioAuth | null) => {
      this.usuario = u;
      this.cdr.detectChanges();
    });

    this.atualizarDataHora();

    this.zone.runOutsideAngular(() => {
      this.timerId = window.setInterval(() => {
        this.atualizarDataHora();

        this.zone.run(() => {
          this.cdr.detectChanges();
        });
      }, 1000);
    });
  }

  ngOnDestroy(): void {
    if (this.timerId !== null) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private atualizarDataHora(): void {
    const agora = new Date();
    this.dataHoraFormatada =
      agora.toLocaleDateString('pt-BR', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      }) +
      ' - ' +
      agora.toLocaleTimeString('pt-BR', {
        hour: '2-digit',
        minute: '2-digit',
      });
  }

  irUsuarios(): void {
    this.router.navigate(['/app/usuarios']);
  }

  irPerfis(): void {
    this.router.navigate(['/app/perfis']);
  }

  irPermissoes(): void {
    this.router.navigate(['/app/permissoes']);
  }

  irAplicativos(): void {
    this.router.navigate(['/app/aplicativos']);
  }

  abrirMicrofrontend(): void {
    this.router.navigate(['/app/microfrontend']);
  }

  irHome(): void {
    this.router.navigate(['/app/home']);
  }

  sair(): void {
    this.authState.clear();
    this.router.navigate(['/login']);
  }
}
