import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface UsuarioAuth {
  usuario: string;
  nome: string;
  perfis: string[];
  permissoes: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthStateService {

  private usuarioSubject = new BehaviorSubject<UsuarioAuth | null>(this.getUsuarioFromStorage());
  private tokenSubject = new BehaviorSubject<string | null>(localStorage.getItem('token'));
  private refreshTokenSubject = new BehaviorSubject<string | null>(localStorage.getItem('refreshToken'));

  public usuario$: Observable<UsuarioAuth | null> = this.usuarioSubject.asObservable();
  public token$: Observable<string | null> = this.tokenSubject.asObservable();
  public refreshToken$: Observable<string | null> = this.refreshTokenSubject.asObservable();

  private getUsuarioFromStorage(): UsuarioAuth | null {
    const usuario = localStorage.getItem('usuarioObj');
    return usuario ? JSON.parse(usuario) : null;
  }

  // GETS
  getUsuario(): UsuarioAuth | null { return this.usuarioSubject.value; }
  getToken(): string | null { return this.tokenSubject.value; }
  getRefreshToken(): string | null { return this.refreshTokenSubject.value; }

  // SET AUTH
  setAuth(token: string, refreshToken: string, usuario: UsuarioAuth) {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
    // saved as object for modern usage
    localStorage.setItem('usuarioObj', JSON.stringify(usuario));
    // only store the object representation

    this.tokenSubject.next(token);
    this.refreshTokenSubject.next(refreshToken);
    this.usuarioSubject.next(usuario);
  }

    getPermissions(): string[] {
    const usuario = this.getUsuario();
    return usuario?.permissoes || [];
  }

  // CLEAR
  clear() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('usuarioObj');
    localStorage.removeItem('usuario');
    localStorage.removeItem('nome');
    localStorage.removeItem('perfis');

    this.tokenSubject.next(null);
    this.refreshTokenSubject.next(null);
    this.usuarioSubject.next(null);
  }
}
