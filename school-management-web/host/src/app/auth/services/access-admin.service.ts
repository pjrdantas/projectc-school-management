import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserInput } from '../models/user.model';
const API_BASE_URL = 'http://localhost:8080';
export interface PerfilPayload { codigo: string; nome: string; descricao?: string; }
export interface PermissaoPayload { codigo: string; descricao?: string; }
@Injectable({ providedIn: 'root' })
export class AccessAdminService {
  private readonly http = inject(HttpClient);
  listarUsuarios(): Observable<User[]> { return this.http.get<User[]>(`${API_BASE_URL}/api/usuarios`); }
  buscarUsuario(id: string): Observable<User> { return this.http.get<User>(`${API_BASE_URL}/api/usuarios/${id}`); }
  criarUsuario(payload: UserInput): Observable<User> { return this.http.post<User>(`${API_BASE_URL}/api/usuarios`, payload); }
  atualizarUsuario(id: string, payload: UserInput): Observable<User> { return this.http.put<User>(`${API_BASE_URL}/api/usuarios/${id}`, payload); }
  excluirUsuario(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/usuarios/${id}`); }
  listarPerfis(): Observable<unknown[]> { return this.http.get<unknown[]>(`${API_BASE_URL}/api/perfis`); }
  criarPerfil(payload: PerfilPayload): Observable<unknown> { return this.http.post(`${API_BASE_URL}/api/perfis`, payload); }
  listarPermissoes(): Observable<unknown[]> { return this.http.get<unknown[]>(`${API_BASE_URL}/api/permissoes`); }
  criarPermissao(payload: PermissaoPayload): Observable<unknown> { return this.http.post(`${API_BASE_URL}/api/permissoes`, payload); }
}
