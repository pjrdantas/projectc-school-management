import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  SHELL_CONTEXT_EVENT,
  SHELL_CONTEXT_STORAGE_KEY,
} from '../shell/shell-context.model';

export interface UsuarioAuth {
  usuarioId?: string | null;
  professorId?: string | null;
  usuario: string;
  nome: string;
  perfis: string[];
  permissoes: string[];
}

@Injectable({
  providedIn: 'root',
})
export class AuthStateService {
  private usuarioSubject = new BehaviorSubject<UsuarioAuth | null>(
    this.getUsuarioFromStorage(),
  );
  private tokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('token'),
  );
  private refreshTokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('refreshToken'),
  );

  public usuario$: Observable<UsuarioAuth | null> = this.usuarioSubject.asObservable();
  public token$: Observable<string | null> = this.tokenSubject.asObservable();
  public refreshToken$: Observable<string | null> = this.refreshTokenSubject.asObservable();

  private getUsuarioFromStorage(): UsuarioAuth | null {
    const usuario = localStorage.getItem('usuarioObj');
    return usuario ? JSON.parse(usuario) : null;
  }

  getUsuario(): UsuarioAuth | null {
    return this.usuarioSubject.value;
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  getRefreshToken(): string | null {
    return this.refreshTokenSubject.value;
  }

  setAuth(token: string, refreshToken: string, usuario: UsuarioAuth) {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('usuarioObj', JSON.stringify(usuario));

    this.tokenSubject.next(token);
    this.refreshTokenSubject.next(refreshToken);
    this.usuarioSubject.next(usuario);
  }

  getPermissions(): string[] {
    const usuario = this.getUsuario();
    return usuario?.permissoes || [];
  }

  clear() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('usuarioObj');
    localStorage.removeItem('usuario');
    localStorage.removeItem('nome');
    localStorage.removeItem('perfis');
    localStorage.removeItem(SHELL_CONTEXT_STORAGE_KEY);

    this.tokenSubject.next(null);
    this.refreshTokenSubject.next(null);
    this.usuarioSubject.next(null);
    window.dispatchEvent(new CustomEvent(SHELL_CONTEXT_EVENT, { detail: null }));
  }
}
