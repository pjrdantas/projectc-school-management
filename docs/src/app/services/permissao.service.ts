import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, map } from 'rxjs';
import { ConfigService } from './config.service';
import { Permissao } from '../models/permissao.model';

@Injectable({
  providedIn: 'root'
})
export class PermissaoService {

  loading = false;
  private API = 'permissoes';

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) {
    // Monta a URL base: http://localhost:8080/api/permissoes
    this.API = `${this.configService.getUrlService()}/${this.API}`;
  }

  listAll(): Observable<Permissao[]> {
    this.loading = true;
    return this.http.get<any[]>(this.API).pipe(
      map(data =>
        data.map(p => ({
          id: p.id,
          nmPermissao : p.nmPermissao // Mapeia nmPermissao (Java) para nome (Angular Model)
        }))
      ),
      finalize(() => this.loading = false)
    );
  }

  findById(id: number): Observable<Permissao> {
    this.loading = true;
    return this.http.get<any>(`${this.API}/${id}`).pipe(
      map(p => ({
        id: p.id,
        nmPermissao : p.nmPermissao
      })),
      finalize(() => this.loading = false)
    );
  }

  create(payload: { nmPermissao : string }): Observable<Permissao> {
    // Converte o model Angular para o DTO que o Java espera
    const requestPayload = {
      nmPermissao: payload.nmPermissao
    };

    this.loading = true;
    return this.http.post<any>(this.API, requestPayload).pipe(
      map(p => ({
        id: p.id,
        nmPermissao : p.nmPermissao
      })),
      finalize(() => this.loading = false)
    );
  }

  update(id: number, payload: { nmPermissao : string }): Observable<Permissao> {
    const requestPayload = {
      nmPermissao: payload.nmPermissao
    };

    this.loading = true;
    return this.http.put<any>(`${this.API}/${id}`, requestPayload).pipe(
      map(p => ({
        id: p.id,
        nmPermissao : p.nmPermissao
      })),
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
