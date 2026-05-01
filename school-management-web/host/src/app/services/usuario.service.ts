import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize } from 'rxjs';
import { ConfigService } from './config.service';
import { UsuarioResponse, UsuarioRequest } from '../models/usuario-gestao.model';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  loading = false;
  private API = 'usuarios';
  private http = inject(HttpClient);
  private configService = inject(ConfigService);

  constructor() {
    // Monta a URL correta (ex: http://localhost:8080/api/usuarios)
    this.API = `${this.configService.getUrlService()}/${this.API}`;
  }

  // Helpers para conversão S/N (seguindo padrão AplicativosService)
  toBoolean(value: 'S' | 'N'): boolean {
    return value === 'S';
  }

  toString(value: boolean): 'S' | 'N' {
    return value ? 'S' : 'N';
  }

  listAll(): Observable<UsuarioResponse[]> {
    this.loading = true;
    return this.http.get<UsuarioResponse[]>(this.API).pipe(
      finalize(() => this.loading = false)
    );
  }

  findById(id: number): Observable<UsuarioResponse> {
    this.loading = true;
    return this.http.get<UsuarioResponse>(`${this.API}/${id}`).pipe(
      finalize(() => this.loading = false)
    );
  }

  create(usuario: UsuarioRequest): Observable<UsuarioResponse> {
    this.loading = true;
    return this.http.post<UsuarioResponse>(this.API, usuario).pipe(
      finalize(() => this.loading = false)
    );
  }

  update(id: number, usuario: UsuarioRequest): Observable<UsuarioResponse> {
    this.loading = true;
    return this.http.put<UsuarioResponse>(`${this.API}/${id}`, usuario).pipe(
      finalize(() => this.loading = false)
    );
  }

  delete(id: number): Observable<void> {
    this.loading = true;
    return this.http.delete<void>(`${this.API}/${id}`).pipe(
      finalize(() => this.loading = false)
    );
  }
}
