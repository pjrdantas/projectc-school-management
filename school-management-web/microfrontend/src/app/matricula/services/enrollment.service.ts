import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  Enrollment,
  EnrollmentCatalogItem,
  EnrollmentFilter,
  EnrollmentInput,
} from '../models/enrollment.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  create(input: EnrollmentInput): Observable<Enrollment> {
    return this.http.post<Enrollment>(`${this.apiBaseUrl}/api/matriculas`, input, {
      headers: this.buildHeaders(),
    });
  }

  search(filter: EnrollmentFilter): Observable<Enrollment[]> {
    let params = new HttpParams();

    if (filter.alunoId) {
      params = params.set('alunoId', filter.alunoId);
    }

    if (filter.turmaId) {
      params = params.set('turmaId', filter.turmaId);
    }

    if (filter.periodoLetivoId) {
      params = params.set('periodoLetivoId', filter.periodoLetivoId);
    }

    if (filter.status) {
      params = params.set('status', filter.status);
    }

    return this.http.get<Enrollment[]>(`${this.apiBaseUrl}/api/matriculas`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  listStatuses(): Observable<EnrollmentCatalogItem[]> {
    return this.http.get<EnrollmentCatalogItem[]>(`${this.apiBaseUrl}/api/matriculas/catalogos/status`, {
      headers: this.buildHeaders(),
    });
  }

  updateStatus(id: string, status: string): Observable<Enrollment> {
    return this.http.patch<Enrollment>(
      `${this.apiBaseUrl}/api/matriculas/${id}/status`,
      { status },
      { headers: this.buildHeaders() },
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/matriculas/${id}`, {
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
}
