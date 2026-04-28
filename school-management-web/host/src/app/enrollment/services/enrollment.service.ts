import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Enrollment, EnrollmentFilter, EnrollmentInput } from '../models/enrollment.model';

const API_BASE_URL = 'http://localhost:8080';

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
