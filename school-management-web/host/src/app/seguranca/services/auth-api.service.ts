import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config/api.config';

export interface LoginRequest {
  login: string;
  senha: string;
}

export interface AuthApiResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  usuarioId?: string | null;
  professorId?: string | null;
  username?: string;
  login?: string;
  nome: string;
  perfis: string[];
  permissoes: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(payload: LoginRequest): Observable<AuthApiResponse> {
    return this.http.post<AuthApiResponse>(`${API_BASE_URL}/api/auth/login`, payload);
  }

  refresh(refreshToken: string): Observable<AuthApiResponse> {
    return this.http.post<AuthApiResponse>(`${API_BASE_URL}/api/auth/refresh`, { refreshToken });
  }

  logout(refreshToken: string): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/api/auth/logout`, { refreshToken });
  }
}
