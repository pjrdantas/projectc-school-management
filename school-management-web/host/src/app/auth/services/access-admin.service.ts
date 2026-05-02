import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Permission, PermissionInput } from '../models/permission.model';
import { Profile, ProfileInput } from '../models/profile.model';
import { User, UserInput } from '../models/user.model';
const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AccessAdminService {
  private readonly http = inject(HttpClient);

  private selectedUserId: string | null = null;
  private selectedProfileId: string | null = null;
  private selectedPermissionId: string | null = null;
  selectUser(id: string | null) { this.selectedUserId = id; }
  currentUserId() { return this.selectedUserId; }
  selectProfile(id: string | null) { this.selectedProfileId = id; }
  currentProfileId() { return this.selectedProfileId; }
  selectPermission(id: string | null) { this.selectedPermissionId = id; }
  currentPermissionId() { return this.selectedPermissionId; }

  listarUsuarios(): Observable<User[]> {
    return this.http.get<any[]>(`${API_BASE_URL}/api/usuarios`).pipe(map(rows => rows.map(this.mapUserFromApi)));
  }
  buscarUsuario(id: string): Observable<User> {
    return this.http.get<any>(`${API_BASE_URL}/api/usuarios/${id}`).pipe(map(this.mapUserFromApi));
  }
  criarUsuario(payload: UserInput): Observable<User> {
    return this.http.post<any>(`${API_BASE_URL}/api/usuarios`, this.mapUserToApi(payload)).pipe(map(this.mapUserFromApi));
  }
  atualizarUsuario(id: string, payload: UserInput): Observable<User> {
    return this.http.put<any>(`${API_BASE_URL}/api/usuarios/${id}`, this.mapUserToApi(payload)).pipe(map(this.mapUserFromApi));
  }
  excluirUsuario(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/usuarios/${id}`); }

  listarPerfis(): Observable<Profile[]> {
    return this.http.get<any[]>(`${API_BASE_URL}/api/perfis`).pipe(map(rows => rows.map(this.mapProfileFromApi)));
  }
  buscarPerfil(id: string): Observable<Profile> {
    return this.http.get<any>(`${API_BASE_URL}/api/perfis/${id}`).pipe(map(this.mapProfileFromApi));
  }
  criarPerfil(payload: ProfileInput): Observable<Profile> {
    return this.http.post<any>(`${API_BASE_URL}/api/perfis`, this.mapProfileToApi(payload)).pipe(map(this.mapProfileFromApi));
  }
  atualizarPerfil(id: string, payload: ProfileInput): Observable<Profile> {
    return this.http.put<any>(`${API_BASE_URL}/api/perfis/${id}`, this.mapProfileToApi(payload)).pipe(map(this.mapProfileFromApi));
  }
  excluirPerfil(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/perfis/${id}`); }

  listarPermissoes(): Observable<Permission[]> {
    return this.http.get<any[]>(`${API_BASE_URL}/api/permissoes`).pipe(map(rows => rows.map(this.mapPermissionFromApi)));
  }
  buscarPermissao(id: string): Observable<Permission> {
    return this.http.get<any>(`${API_BASE_URL}/api/permissoes/${id}`).pipe(map(this.mapPermissionFromApi));
  }
  criarPermissao(payload: PermissionInput): Observable<Permission> {
    return this.http.post<any>(`${API_BASE_URL}/api/permissoes`, this.mapPermissionToApi(payload)).pipe(map(this.mapPermissionFromApi));
  }
  atualizarPermissao(id: string, payload: PermissionInput): Observable<Permission> {
    return this.http.put<any>(`${API_BASE_URL}/api/permissoes/${id}`, this.mapPermissionToApi(payload)).pipe(map(this.mapPermissionFromApi));
  }
  excluirPermissao(id: string): Observable<void> { return this.http.delete<void>(`${API_BASE_URL}/api/permissoes/${id}`); }

  private normalizeIdList(value: any): string[] {
    if (!Array.isArray(value)) return [];
    return value
      .map(item => {
        if (item == null) return null;
        if (typeof item === 'string' || typeof item === 'number') return String(item);
        if (typeof item === 'object' && 'id' in item && item.id != null) return String(item.id);
        return null;
      })
      .filter((id): id is string => !!id);
  }

  private mapUserFromApi = (u: any): User => ({
    id: String(u.id),
    username: u.username ?? u.login ?? '',
    nome: u.nome ?? '',
    email: u.email ?? '',
    ativo: (u.ativo === 'S' || u.ativo === true),
    createdAt: u.createdAt ?? '',
    perfilIds: this.normalizeIdList(u.perfisIds ?? u.perfilIds ?? u.perfis),
  });

  private mapUserToApi(payload: UserInput): any {
    return {
      nome: payload.nome,
      username: payload.username,
      login: payload.username,
      email: payload.email,
      senhaHash: payload.senhaHash,
      senha: payload.senhaHash,
      ativo: payload.ativo,
      perfilIds: payload.perfilIds ?? [],
      perfisIds: payload.perfilIds ?? [],
    };
  }

  private mapProfileFromApi = (p: any): Profile => ({
    id: String(p.id),
    codigo: p.nmPerfil ?? p.codigo ?? p.nome ?? '',
    nome: p.nmPerfil ?? p.nome ?? '',
    descricao: p.descricao,
    createdAt: p.createdAt ?? '',
    permissaoIds: this.normalizeIdList(p.permissoesIds ?? p.permissaoIds ?? p.permissoes),
  });

  private mapProfileToApi(payload: ProfileInput): any {
    return {
      codigo: payload.codigo,
      nome: payload.nome,
      nmPerfil: payload.nome || payload.codigo,
      permissaoIds: payload.permissaoIds ?? [],
      permissoesIds: payload.permissaoIds ?? [],
    };
  }

  private mapPermissionFromApi = (p: any): Permission => ({
    id: String(p.id),
    codigo: p.nmPermissao ?? p.codigo ?? '',
    descricao: p.descricao,
    createdAt: p.createdAt ?? '',
  });

  private mapPermissionToApi(payload: PermissionInput): any {
    return {
      nmPermissao: payload.codigo,
      descricao: payload.descricao,
    };
  }
}
