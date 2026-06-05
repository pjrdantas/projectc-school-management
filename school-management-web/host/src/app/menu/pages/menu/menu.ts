import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnDestroy,
  OnInit,
  Type,
  inject,
} from '@angular/core';
import { AsyncPipe, CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';

import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Subject, finalize, takeUntil } from 'rxjs';
import { AuthStateService, UsuarioAuth } from '../../../core/auth/auth-state.service';
import { AplicativosResponse } from '../../../models/aplicativos-response.model';
import { AplicativosService } from '../../../services/aplicativos.service';
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
    MatSnackBarModule,
  ],
  templateUrl: './menu.html',
  styleUrls: ['./menu.scss'],
})
export class Menu implements OnInit, OnDestroy {
  private router = inject(Router);
  public authState = inject(AuthStateService);
  private cdr = inject(ChangeDetectorRef);
  private zone = inject(NgZone);
  private aplicativosService = inject(AplicativosService);
  private snackBar = inject(MatSnackBar);
  private authApi = inject(AuthApiService);

  usuario: UsuarioAuth | null = null;
  dataHoraFormatada = '';
  aplicativosAtivos: AplicativosResponse[] = [];
  private timerId: ReturnType<typeof window.setInterval> | null = null;
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.usuario = this.authState.getUsuario();

    this.authState.usuario$.pipe(takeUntil(this.destroy$)).subscribe(u => {
      this.usuario = u;
      this.cdr.detectChanges();
    });

    this.carregarAplicativosAtivos();

    this.aplicativosService.update$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.carregarAplicativosAtivos());

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

  private carregarAplicativosAtivos(): void {
    this.aplicativosService
      .listActive()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: apps => {
          this.aplicativosAtivos = apps;
          this.registrarRotasMicrofrontend(apps);
          this.cdr.detectChanges();
        },
        error: () => (this.aplicativosAtivos = []),
      });
  }

  private isMicrofrontend(app: AplicativosResponse): boolean {
    return !!(app.url?.trim() && (app.exposedModule?.trim() || app.moduleName?.trim()));
  }

  private normalizarRoutePath(routePath: string): string {
    return (routePath || '').trim().replace(/^\/+/, '').replace(/^app\//i, '');
  }

  private carregarComponenteRemoto(
    remoteEntry: string,
    exposedModule: string,
  ): Promise<Type<unknown>> {
    return loadRemoteModule({ remoteEntry, exposedModule })
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      .then((m: any) => m['HomeComponent'] || m['AppComponent'] || m['App'])
      .catch(erro => {
        this.snackBar.open('Microfrontend não encontrado.', 'Fechar', {
          duration: 3500,
        });
        this.router.navigate(['/home']);
        throw erro;
      });
  }

  private registrarRotasMicrofrontend(apps: AplicativosResponse[]): void {
    let alterou = false;

    apps.filter(a => this.isMicrofrontend(a)).forEach(app => {
      const path = this.normalizarRoutePath(app.routePath) || `mfe-${app.id}`;
      this.upsertRotaMicrofrontend(path, app.url, app.exposedModule);
      alterou = true;
    });

    if (alterou) {
      this.router.resetConfig([...this.router.config]);
    }
  }

  private upsertRotaMicrofrontend(
    path: string,
    remoteEntry: string,
    exposedModule: string,
  ): void {
    const existente = this.router.config.find(r => r.path === path);

    if (existente) {
      existente.loadComponent = () =>
        this.carregarComponenteRemoto(remoteEntry, exposedModule);
      return;
    }

    this.router.config.splice(this.router.config.length - 1, 0, {
      path,
      loadComponent: () => this.carregarComponenteRemoto(remoteEntry, exposedModule),
    });
  }

  getIconeApp(app: AplicativosResponse): string {
    return this.isMicrofrontend(app) ? 'widgets' : 'open_in_new';
  }

  navegarApp(app: AplicativosResponse): void {
    if (this.isMicrofrontend(app)) {
      const path = this.normalizarRoutePath(app.routePath) || `mfe-${app.id}`;

      if (!app.exposedModule) {
        this.snackBar.open('Exposed Module não informado.', 'Fechar', {
          duration: 3500,
        });
        return;
      }

      this.upsertRotaMicrofrontend(path, app.url, app.exposedModule);
      this.router.resetConfig([...this.router.config]);
      this.router.navigate([`/${path}`]);
    } else if (app.routePath) {
      this.router.navigate([app.routePath]);
    } else if (app.url?.startsWith('http')) {
      window.open(app.url, '_blank');
    }
  }

  irUsuarios() {
    this.router.navigate(['/auth/users']);
  }
  irPerfis() {
    this.router.navigate(['/auth/profiles']);
  }
  irPermissoes() {
    this.router.navigate(['/auth/permissions']);
  }
  irHome() {
    this.router.navigate(['/home']);
  }
  irStudents() {
    this.router.navigate(['/students']);
  }
  irResponsibles() {
    this.router.navigate(['/responsibles']);
  }
  irAcademicPeriods() {
    this.router.navigate(['/academic/periods']);
  }
  irAcademicSeries() {
    this.router.navigate(['/academic/series']);
  }
  irAcademicShifts() {
    this.router.navigate(['/academic/shifts']);
  }
  irAcademicClasses() {
    this.router.navigate(['/academic/classes']);
  }
  irAcademicDisciplines() {
    this.router.navigate(['/academic/disciplines']);
  }
  irEnrollment() {
    this.router.navigate(['/enrollment']);
  }
  irMicrofrontend() {
    this.router.navigate(['/microfrontend']);
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
