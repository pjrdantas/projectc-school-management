import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { ShellContextService } from '../core/shell/shell-context.service';
import { HISTORICO_ESCOLAR_CADASTRO_MOCK } from './referencia/core/mock/historico-escolar.mock';
import { HistoricoEscolarLoadRequest, HistoricoEscolarPdfImportResponse, HistoricoEscolarResponse, HistoricoEscolarSaveRequest, HistoricoEscolarSaveResponse } from './referencia/core/models/historico-escolar-api.models';

@Injectable()
export class HistoricoEscolarBffService {
  private readonly http = inject(HttpClient); private readonly shellContext = inject(ShellContextService);
  carregarHistoricoEscolar(_request: HistoricoEscolarLoadRequest): Observable<HistoricoEscolarResponse> {
    const partes = window.location.pathname.split('/').filter(Boolean); const id = partes.at(-1); const query = new URLSearchParams(window.location.search); const aluno = query.get('idAluno'); const matricula = query.get('idMatricula');
    const response = id && id !== 'new'
      ? this.http.get<HistoricoEscolarResponse>(`${this.baseUrl}/api/historicos-escolares/${id}/carregamento`, { headers: this.headers() })
      : aluno && matricula
        ? this.http.get<HistoricoEscolarResponse>(`${this.baseUrl}/api/historicos-escolares/novo`, { headers: this.headers(), params: new HttpParams({ fromObject: { idAluno: aluno, idMatricula: matricula, modo: 'CADASTRO' } }) })
        : of(structuredClone(HISTORICO_ESCOLAR_CADASTRO_MOCK));
    return response.pipe(catchError(() => of(structuredClone(HISTORICO_ESCOLAR_CADASTRO_MOCK))));
  }
  importarHistoricoEscolarPdf(arquivo: File): Observable<HistoricoEscolarPdfImportResponse> { const form = new FormData(); form.append('arquivo', arquivo); return this.http.post<HistoricoEscolarPdfImportResponse>(`${this.baseUrl}/api/historicos-escolares/importacao-pdf`, form, { headers: this.headers() }); }
  salvarHistoricoEscolar(request: HistoricoEscolarSaveRequest): Observable<HistoricoEscolarSaveResponse> { return request.idHistoricoEscolar ? this.http.put<HistoricoEscolarSaveResponse>(`${this.baseUrl}/api/historicos-escolares/${request.idHistoricoEscolar}`, request, { headers: this.headers() }) : this.http.post<HistoricoEscolarSaveResponse>(`${this.baseUrl}/api/historicos-escolares`, request, { headers: this.headers() }); }
  private get baseUrl(): string { return this.shellContext.getApiBaseUrl(); }
  private headers(): HttpHeaders { const token = this.shellContext.getToken(); return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders(); }
}
