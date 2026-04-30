import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

const API_BASE_URL = 'http://localhost:8080';

export interface UsuarioPayload { username: string; nome: string; email: string; senhaHash: string; ativo: boolean; }
export interface PerfilPayload { codigo: string; nome: string; descricao?: string; }
export interface PermissaoPayload { codigo: string; descricao?: string; }

@Injectable({ providedIn: 'root' })
export class AccessAdminService {
  private readonly http = inject(HttpClient);
  listarUsuarios(): Observable<unknown[]> { return this.http.get<unknown[]>(`${API_BASE_URL}/api/usuarios`); }
  criarUsuario(payload: UsuarioPayload): Observable<unknown> { return this.http.post(`${API_BASE_URL}/api/usuarios`, payload); }
  listarPerfis(): Observable<unknown[]> { return this.http.get<unknown[]>(`${API_BASE_URL}/api/perfis`); }
  criarPerfil(payload: PerfilPayload): Observable<unknown> { return this.http.post(`${API_BASE_URL}/api/perfis`, payload); }
  listarPermissoes(): Observable<unknown[]> { return this.http.get<unknown[]>(`${API_BASE_URL}/api/permissoes`); }
  criarPermissao(payload: PermissaoPayload): Observable<unknown> { return this.http.post(`${API_BASE_URL}/api/permissoes`, payload); }
}
