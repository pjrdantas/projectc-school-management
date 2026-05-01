import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize } from 'rxjs';
import { ConfigService } from './config.service';
import { PerfilResponse } from '../models/perfil-response.model';
import { PerfilRequest } from '../models/perfil-request.model';

@Injectable({
  providedIn: 'root'
})
export class PerfilService {

  loading = false;
  private API = 'perfis';

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) {
    this.API = `${this.configService.getUrlService()}/${this.API}`;
  }

  listAll(): Observable<PerfilResponse[]> {
    this.loading = true;
    return this.http.get<PerfilResponse[]>(this.API).pipe(
      finalize(() => this.loading = false)
    );
  }

  findById(id: number): Observable<PerfilResponse> {
    this.loading = true;
    return this.http.get<PerfilResponse>(`${this.API}/${id}`).pipe(
      finalize(() => this.loading = false)
    );
  }

  /**
   * Cria um perfil enviando apenas o nome e os IDs das permissões
   */
  create(payload: {
    nmPerfil: string;
    permissoesIds: number[];
  }): Observable<PerfilResponse> {
    const requestPayload: PerfilRequest = {
      nmPerfil: payload.nmPerfil,
      permissoesIds: payload.permissoesIds
    };

    this.loading = true;
    return this.http.post<PerfilResponse>(this.API, requestPayload).pipe(
      finalize(() => this.loading = false)
    );
  }

  /**
   * Atualiza um perfil existente
   */
  update(id: number, payload: {
    nmPerfil: string;
    permissoesIds: number[];
  }): Observable<PerfilResponse> {
    const requestPayload: PerfilRequest = {
      nmPerfil: payload.nmPerfil,
      permissoesIds: payload.permissoesIds
    };

    this.loading = true;
    return this.http.put<PerfilResponse>(`${this.API}/${id}`, requestPayload).pipe(
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
