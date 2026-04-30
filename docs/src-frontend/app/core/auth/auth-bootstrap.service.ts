import { Injectable, inject } from '@angular/core';
import { AuthStateService } from './auth-state.service';
import { SessionValidatorService } from './session-validator.service';

@Injectable({
  providedIn: 'root'
})
export class AuthBootstrapService {

  private authState = inject(AuthStateService);
  private sessionValidator = inject(SessionValidatorService);

  constructor() {
   /** console.log('✅ AuthBootstrapService inicializado'); */
  }

  init(): void {
  /**  console.log('🔐 AuthBootstrapService.init() chamado'); */
    const token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');
    const usuarioObj = localStorage.getItem('usuarioObj');

    if (token && refreshToken) {
      let usuario;

      if (usuarioObj) {
        try {
          usuario = JSON.parse(usuarioObj);
        } catch {
          usuario = { usuario: '', nome: '', perfis: [], permissoes: [] };
        }
      } else {
        // If no usuarioObj is present, initialize with empty values
        usuario = { usuario: '', nome: '', perfis: [], permissoes: [] };
      }

      this.authState.setAuth(
        token,
        refreshToken,
        usuario
      );
    }
  }
}
