import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Subject, finalize, takeUntil } from 'rxjs';
import { AuthStateService, UsuarioAuth } from '../../../core/auth/auth-state.service';
import {
  SHELL_ACCESS_MENU,
  SHELL_BUSINESS_MENU,
  ShellMenuItem,
} from '../../../core/shell/shell-navigation.config';
import { AuthApiService } from '../../../seguranca/services/auth-api.service';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    CommonModule,
    AsyncPipe,
    RouterModule,
    RouterOutlet,
    MatToolbarModule,
    MatDividerModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatButtonModule,
  ],
  templateUrl: './menu.html',
  styleUrls: ['./menu.scss'],
})
export class Menu implements OnInit, OnDestroy {
  private router = inject(Router);
  public authState = inject(AuthStateService);
  private cdr = inject(ChangeDetectorRef);
  private zone = inject(NgZone);
  private authApi = inject(AuthApiService);

  usuario: UsuarioAuth | null = null;
  dataHoraFormatada = '';
  readonly businessMenuItems: ShellMenuItem[] = SHELL_BUSINESS_MENU;
  readonly accessMenuItems: ShellMenuItem[] = SHELL_ACCESS_MENU;
  private timerId: ReturnType<typeof window.setInterval> | null = null;
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.usuario = this.authState.getUsuario();

    this.authState.usuario$.pipe(takeUntil(this.destroy$)).subscribe(u => {
      this.usuario = u;
      this.cdr.detectChanges();
    });

    this.atualizarDataHora();

    this.zone.runOutsideAngular(() => {
      this.timerId = window.setInterval(() => {
        this.atualizarDataHora();
        this.zone.run(() => this.cdr.detectChanges());
      }, 1000);
    });
  }

  ngOnDestroy(): void {
    if (this.timerId) {
      window.clearInterval(this.timerId);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  irHome() {
    this.router.navigate(['/home']);
  }

  navegarMenuItem(item: ShellMenuItem): void {
    this.router.navigate([item.route]);
  }

  sair(): void {
    const refreshToken = this.authState.getRefreshToken();

    if (!refreshToken) {
      this.finalizarLogoutLocal();
      return;
    }

    this.authApi
      .logout(refreshToken)
      .pipe(finalize(() => this.finalizarLogoutLocal()))
      .subscribe();
  }

  private finalizarLogoutLocal(): void {
    this.authState.clear();
    this.router.navigate(['/login']);
  }

  private atualizarDataHora(): void {
    const agora = new Date();
    this.dataHoraFormatada =
      agora.toLocaleDateString('pt-BR') +
      ' - ' +
      agora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}
