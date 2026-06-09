import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  DashboardFrontendResponse,
  DashboardPublicoCodigo,
  DashboardUsuarioConfiguracao,
  DashboardUsuarioConfiguracaoInput,
} from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  consultar(
    publicoCodigo: DashboardPublicoCodigo,
    professorId?: string | null,
  ): Observable<DashboardFrontendResponse> {
    let params = new HttpParams().set('publicoCodigo', publicoCodigo);
    const usuarioId = this.shellContext.getUsuario()?.usuarioId;
    if (usuarioId) {
      params = params.set('usuarioId', usuarioId);
    }
    if (professorId) {
      params = params.set('professorId', professorId);
    }

    return this.http.get<DashboardFrontendResponse>(`${this.apiBaseUrl}/api/dashboard/frontend`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  salvarConfiguracaoWidget(
    usuarioId: string,
    dashboardWidgetId: string,
    payload: DashboardUsuarioConfiguracaoInput,
  ): Observable<DashboardUsuarioConfiguracao> {
    return this.http.put<DashboardUsuarioConfiguracao>(
      `${this.apiBaseUrl}/api/dashboard/usuarios/${usuarioId}/widgets/${dashboardWidgetId}/configuracao`,
      payload,
      { headers: this.buildHeaders() },
    );
  }

  excluirConfiguracaoWidget(usuarioId: string, dashboardWidgetId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiBaseUrl}/api/dashboard/usuarios/${usuarioId}/widgets/${dashboardWidgetId}/configuracao`,
      { headers: this.buildHeaders() },
    );
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
