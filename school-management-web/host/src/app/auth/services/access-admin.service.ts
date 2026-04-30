import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Permission, PermissionInput } from '../models/permission.model';
import { Profile, ProfileInput } from '../models/profile.model';
import { User, UserInput } from '../models/user.model';
const API_BASE_URL = 'http://localhost:8080';
@Injectable({ providedIn: 'root' })
export class AccessAdminService {
  private readonly http = inject(HttpClient);
  listarUsuarios(): Observable<User[]> { return this.http.get<User[]>(`${API_BASE_URL}/api/usuarios`); }
  buscarUsuario(id: string): Observable<User> { return this.http.get<User>(`${API_BASE_URL}/api/usuarios/${id}`); }
  criarUsuario(payload: UserInput): Observable<User> { return this.http.post<User>(`${API_BASE_URL}/api/usuarios`, payload); }
  atualizarUsuario(id: string, payload: UserInput): Observable<User> { return this.http.put<User>(`${API_BASE_URL}/api/usuarios/${id}`, payload); }
  excluirUsuario(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/usuarios/${id}`); }
  listarPerfis(): Observable<Profile[]> { return this.http.get<Profile[]>(`${API_BASE_URL}/api/perfis`); }
  buscarPerfil(id: string): Observable<Profile> { return this.http.get<Profile>(`${API_BASE_URL}/api/perfis/${id}`); }
  criarPerfil(payload: ProfileInput): Observable<Profile> { return this.http.post<Profile>(`${API_BASE_URL}/api/perfis`, payload); }
  atualizarPerfil(id: string, payload: ProfileInput): Observable<Profile> { return this.http.put<Profile>(`${API_BASE_URL}/api/perfis/${id}`, payload); }
  excluirPerfil(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/perfis/${id}`); }
  listarPermissoes(): Observable<Permission[]> { return this.http.get<Permission[]>(`${API_BASE_URL}/api/permissoes`); }
  buscarPermissao(id: string): Observable<Permission> { return this.http.get<Permission>(`${API_BASE_URL}/api/permissoes/${id}`); }
  criarPermissao(payload: PermissionInput): Observable<Permission> { return this.http.post<Permission>(`${API_BASE_URL}/api/permissoes`, payload); }
  atualizarPermissao(id: string, payload: PermissionInput): Observable<Permission> { return this.http.put<Permission>(`${API_BASE_URL}/api/permissoes/${id}`, payload); }
  excluirPermissao(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/permissoes/${id}`); }
}
