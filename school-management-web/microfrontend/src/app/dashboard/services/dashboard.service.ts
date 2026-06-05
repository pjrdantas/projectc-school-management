import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { DashboardFrontendResponse, DashboardPublicoCodigo } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  consultar(
    publicoCodigo: DashboardPublicoCodigo,
    professorId?: string | null,
  ): Observable<DashboardFrontendResponse> {
    let params = new HttpParams().set('publicoCodigo', publicoCodigo);
    if (professorId) {
      params = params.set('professorId', professorId);
    }

    return this.http.get<DashboardFrontendResponse>(`${this.apiBaseUrl}/api/dashboard/frontend`, {
      headers: this.buildHeaders(),
      params,
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
