import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  DashboardConfiguracao,
  DashboardConfiguracaoInput,
  DashboardPublico,
  DashboardPublicoInput,
  DashboardWidget,
  DashboardWidgetInput,
} from '../models/dashboard-config.model';

@Injectable({ providedIn: 'root' })
export class DashboardConfigService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listarPublicos(): Observable<DashboardPublico[]> {
    return this.http.get<DashboardPublico[]>(`${this.baseUrl}/api/dashboard/configuracoes/publicos`, {
      headers: this.headers(),
    });
  }

  criarPublico(payload: DashboardPublicoInput): Observable<DashboardPublico> {
    return this.http.post<DashboardPublico>(`${this.baseUrl}/api/dashboard/configuracoes/publicos`, payload, {
      headers: this.headers(),
    });
  }

  atualizarPublico(id: string, payload: DashboardPublicoInput): Observable<DashboardPublico> {
    return this.http.put<DashboardPublico>(`${this.baseUrl}/api/dashboard/configuracoes/publicos/${id}`, payload, {
      headers: this.headers(),
    });
  }

  excluirPublico(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/dashboard/configuracoes/publicos/${id}`, {
      headers: this.headers(),
    });
  }

  listarDashboards(publicoDashboardId?: string | null): Observable<DashboardConfiguracao[]> {
    let params = new HttpParams();
    if (publicoDashboardId) {
      params = params.set('publicoDashboardId', publicoDashboardId);
    }

    return this.http.get<DashboardConfiguracao[]>(`${this.baseUrl}/api/dashboard/configuracoes/dashboards`, {
      headers: this.headers(),
      params,
    });
  }

  criarDashboard(payload: DashboardConfiguracaoInput): Observable<DashboardConfiguracao> {
    return this.http.post<DashboardConfiguracao>(`${this.baseUrl}/api/dashboard/configuracoes/dashboards`, payload, {
      headers: this.headers(),
    });
  }

  atualizarDashboard(id: string, payload: DashboardConfiguracaoInput): Observable<DashboardConfiguracao> {
    return this.http.put<DashboardConfiguracao>(`${this.baseUrl}/api/dashboard/configuracoes/dashboards/${id}`, payload, {
      headers: this.headers(),
    });
  }

  excluirDashboard(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/dashboard/configuracoes/dashboards/${id}`, {
      headers: this.headers(),
    });
  }

  listarWidgets(dashboardId: string): Observable<DashboardWidget[]> {
    return this.http.get<DashboardWidget[]>(`${this.baseUrl}/api/dashboard/configuracoes/dashboards/${dashboardId}/widgets`, {
      headers: this.headers(),
    });
  }

  criarWidget(payload: DashboardWidgetInput): Observable<DashboardWidget> {
    return this.http.post<DashboardWidget>(`${this.baseUrl}/api/dashboard/configuracoes/widgets`, payload, {
      headers: this.headers(),
    });
  }

  atualizarWidget(id: string, payload: DashboardWidgetInput): Observable<DashboardWidget> {
    return this.http.put<DashboardWidget>(`${this.baseUrl}/api/dashboard/configuracoes/widgets/${id}`, payload, {
      headers: this.headers(),
    });
  }

  excluirWidget(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/dashboard/configuracoes/widgets/${id}`, {
      headers: this.headers(),
    });
  }

  private get baseUrl(): string {
    return this.shellContext.getApiBaseUrl();
  }

  private headers(): HttpHeaders {
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
