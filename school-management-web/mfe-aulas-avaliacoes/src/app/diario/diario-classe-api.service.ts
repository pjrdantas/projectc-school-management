import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ShellContextService } from '../core/shell/shell-context.service';
import {
  DiarioClasseLoadRequest,
  DiarioClasseResponse,
  DiarioClasseSaveRequest,
  DiarioClasseSaveResponse,
} from './diario-classe-api.models';

@Injectable({ providedIn: 'root' })
export class DiarioClasseApiService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  carregarDiarioClasse(request: DiarioClasseLoadRequest): Observable<DiarioClasseResponse> {
    const params = new HttpParams()
      .set('idProfessor', request.idProfessor)
      .set('idTurma', request.idTurma)
      .set('idDisciplina', request.idDisciplina)
      .set('anoLetivo', request.anoLetivo)
      .set('mes', request.mes)
      .set('dataReferencia', request.dataReferencia);
    return this.http.get<DiarioClasseResponse>(`${this.apiBaseUrl}/api/diarios-classe`, {
      headers: this.headers(),
      params,
    });
  }

  salvarDiarioClasse(request: DiarioClasseSaveRequest): Observable<DiarioClasseSaveResponse> {
    const body = {
      ...request,
      frequencias: request.frequencias.map(({ status, ...frequencia }) => ({
        ...frequencia,
        situacao: status === 'F' ? 'FALTA' : 'PRESENTE',
      })),
    };
    return this.http.put<DiarioClasseSaveResponse>(
      `${this.apiBaseUrl}/api/diarios-classe/${request.idDiarioClasse}`,
      body,
      { headers: this.headers() },
    );
  }

  private get apiBaseUrl(): string {
    return this.shellContext.getApiBaseUrl();
  }

  private headers(): HttpHeaders {
    const token = this.shellContext.getToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
