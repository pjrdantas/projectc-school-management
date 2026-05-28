import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { API_BASE_URL } from '../../core/config/api.config';
import { Enrollment, EnrollmentFilter, EnrollmentInput } from '../models/enrollment.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  create(input: EnrollmentInput): Observable<Enrollment> {
    return this.http.post<Enrollment>(`${API_BASE_URL}/api/matriculas`, input, {
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

    return this.http.get<Enrollment[]>(`${API_BASE_URL}/api/matriculas`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  updateStatus(id: string, status: string): Observable<Enrollment> {
    return this.http.patch<Enrollment>(
      `${API_BASE_URL}/api/matriculas/${id}/status`,
      { status },
      { headers: this.buildHeaders() },
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/matriculas/${id}`, {
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
}
