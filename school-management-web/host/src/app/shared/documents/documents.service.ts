import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { API_BASE_URL } from '../../core/config/api.config';
import { DocumentEntityType, DocumentInput, DocumentRecord } from './document.model';

@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  listByEntity(entidadeTipo: DocumentEntityType, entidadeId: string): Observable<DocumentRecord[]> {
    const params = new HttpParams()
      .set('entidadeTipo', entidadeTipo)
      .set('entidadeId', entidadeId);

    return this.http.get<DocumentRecord[]>(`${API_BASE_URL}/api/documentos`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  create(input: DocumentInput): Observable<DocumentRecord> {
    return this.http.post<DocumentRecord>(`${API_BASE_URL}/api/documentos`, input, {
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

    return this.http.post<DocumentRecord>(`${API_BASE_URL}/api/documentos`, formData, {
      headers: this.buildUploadHeaders(),
    });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/documentos/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  private buildHeaders(): HttpHeaders {
    const token = this.authState.getToken();
    if (!token) {
      return new HttpHeaders({ 'Content-Type': 'application/json' });
    }

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private buildUploadHeaders(): HttpHeaders {
    const token = this.authState.getToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
