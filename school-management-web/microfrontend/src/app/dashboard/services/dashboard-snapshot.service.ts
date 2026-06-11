import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { DashboardIndicadorSnapshot } from '../models/dashboard-snapshot.model';

@Injectable({ providedIn: 'root' })
export class DashboardSnapshotService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listarPorPublicoCodigo(publicoCodigo: string, referenciaData?: string | null): Observable<DashboardIndicadorSnapshot[]> {
    let params = new HttpParams();
    if (referenciaData) {
      params = params.set('referenciaData', referenciaData);
    }

    return this.http.get<DashboardIndicadorSnapshot[]>(
      `${this.baseUrl}/api/dashboard/snapshots/publicos/${publicoCodigo}`,
      {
        headers: this.headers(),
        params,
      },
    );
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
