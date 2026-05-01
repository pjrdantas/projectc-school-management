import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, map } from 'rxjs';
import { ConfigService } from './config.service';
import { AplicativosRequest } from '../models/aplicativos-request.model';
import { AplicativosResponse } from '../models/aplicativos-response.model';

@Injectable({
  providedIn: 'root'
})
export class AplicativosService {

  loading = false;
  private API = 'aplicativos';

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) {
    this.API = `${this.configService.getUrlService()}/${this.API}`;
  }

  listAll(): Observable<AplicativosResponse[]> {
    this.loading = true;
    return this.http.get<AplicativosResponse[]>(this.API).pipe(
      finalize(() => this.loading = false)
    );
  }

  listActive(): Observable<AplicativosResponse[]> {
    this.loading = true;
    return this.http.get<AplicativosResponse[]>(`${this.API}/ativos`).pipe(
      finalize(() => this.loading = false)
    );
  }

  findById(id: number): Observable<AplicativosResponse> {
    this.loading = true;
    return this.http.get<AplicativosResponse>(`${this.API}/${id}`).pipe(
      finalize(() => this.loading = false)
    );
  }

  // Removida a conversão manual: o payload já vem do Dialog como AplicativosRequest ('S' | 'N')
  create(payload: AplicativosRequest): Observable<AplicativosResponse> {
    this.loading = true;
    return this.http.post<AplicativosResponse>(this.API, payload).pipe(
      finalize(() => this.loading = false)
    );
  }

  // Removida a conversão manual: o payload já vem do Dialog como AplicativosRequest ('S' | 'N')
  update(id: number, payload: AplicativosRequest): Observable<AplicativosResponse> {
    this.loading = true;
    return this.http.put<AplicativosResponse>(`${this.API}/${id}`, payload).pipe(
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
