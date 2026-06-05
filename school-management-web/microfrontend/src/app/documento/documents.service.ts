import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../core/shell/shell-context.service';
import { DocumentEntityType, DocumentInput, DocumentRecord } from './document.model';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listByEntity(entidadeTipo: DocumentEntityType, entidadeId: string): Observable<DocumentRecord[]> {
    const params = new HttpParams()
      .set('entidadeTipo', entidadeTipo)
      .set('entidadeId', entidadeId);

    return this.http.get<DocumentRecord[]>(`${this.apiBaseUrl}/api/documentos`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  create(input: DocumentInput): Observable<DocumentRecord> {
    return this.http.post<DocumentRecord>(`${this.apiBaseUrl}/api/documentos`, input, {
      headers: this.buildHeaders(),
    });
  }

  upload(input: DocumentInput, arquivo: File): Observable<DocumentRecord> {
    const formData = new FormData();
    formData.append('entidadeTipo', input.entidadeTipo);
    formData.append('entidadeId', input.entidadeId);
    formData.append('tipoDocumento', input.tipoDocumento);
    formData.append('numeroDocumento', input.numeroDocumento);
    formData.append('arquivo', arquivo);
    if (input.observacao) {
      formData.append('observacao', input.observacao);
    }

    return this.http.post<DocumentRecord>(`${this.apiBaseUrl}/api/documentos`, formData, {
      headers: this.buildUploadHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/documentos/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  private get apiBaseUrl(): string {
    return this.shellContext.getApiBaseUrl();
  }

  private buildHeaders(): HttpHeaders {
    const token = this.shellContext.getToken();
    if (!token) {
      return new HttpHeaders({ 'Content-Type': 'application/json' });
    }

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private buildUploadHeaders(): HttpHeaders {
    const token = this.shellContext.getToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
