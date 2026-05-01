import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthStateService, UsuarioAuth } from './auth-state.service';
import { ConfigService } from '../../services/config.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private authState = inject(AuthStateService);
  private configService = inject(ConfigService);

  private readonly API = `${this.configService.getUrlService()}/auth`;

  /**
   * ✅ USA O MÉTODO QUE VOCÊ JÁ CRIOU NO AUTH-STATE
   */
  hasPermission(permissionName: string): boolean {
    const permissoes = this.authState.getPermissions();
    return permissoes.includes(permissionName);
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.API}/login`, credentials).pipe(
      tap(res => this.atualizarEstadoAutenticacao(res))
    );
  }

  refreshToken(token?: string): Observable<any> {
    const refresh = token || this.authState.getRefreshToken();
    return this.http.post<any>(`${this.API}/refresh-token`, { refreshToken: refresh }).pipe(
      tap(res => this.atualizarEstadoAutenticacao(res))
    );
  }

  validateSession(token: string): Observable<any> {
    return this.http.post<any>(`${this.API}/validate-session`, { token });
  }

  private atualizarEstadoAutenticacao(res: any): void {
    const usuario: UsuarioAuth = {
      usuario: res.usuario,
      nome: res.nome,
      perfis: res.perfis || [],
      permissoes: res.permissoes || []
    };

    this.authState.setAuth(res.token, res.refreshToken, usuario);
  }
}
