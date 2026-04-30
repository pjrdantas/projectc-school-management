import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

const API_BASE_URL = 'http://localhost:8080';

export interface LoginRequest {
  login: string;
  senha: string;
}

export interface AuthApiResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
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
