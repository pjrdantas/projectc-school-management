import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { HISTORICO_ESCOLAR_CADASTRO_MOCK, HISTORICO_ESCOLAR_IMPORTACAO_PDF_MOCK } from '../mock/historico-escolar.mock';
import {
  HistoricoEscolarLoadRequest,
  HistoricoEscolarPdfImportResponse,
  HistoricoEscolarResponse,
  HistoricoEscolarSaveRequest,
  HistoricoEscolarSaveResponse
} from '../models/historico-escolar-api.models';

@Injectable({ providedIn: 'root' })
export class HistoricoEscolarApiService {
  private readonly apiBaseUrl = 'http://localhost:8080/api';

  /**
   * Enquanto o backend Java não estiver ligado, mantenha true.
   * Quando a API estiver pronta, troque para false.
   */
  private readonly usarMock = true;

  constructor(private readonly http: HttpClient) {}

  carregarHistoricoEscolar(request: HistoricoEscolarLoadRequest): Observable<HistoricoEscolarResponse> {
    if (this.usarMock) {
      return of(structuredClone(HISTORICO_ESCOLAR_CADASTRO_MOCK)).pipe(delay(200));
    }

    const id = request.idHistoricoEscolar ?? 'novo';
    return this.http.get<HistoricoEscolarResponse>(`${this.apiBaseUrl}/historicos-escolares/${id}`);
  }


  importarHistoricoEscolarPdf(file: File): Observable<HistoricoEscolarPdfImportResponse> {
    if (this.usarMock) {
      console.info('PDF enviado para extração do Histórico Escolar:', file.name);

      const response: HistoricoEscolarPdfImportResponse = {
        historico: structuredClone(HISTORICO_ESCOLAR_IMPORTACAO_PDF_MOCK),
        nomeArquivo: file.name,
        confiancaGeral: 82,
        avisos: [
          'Importação simulada em mock. No backend real, conferir campos com baixa confiança.',
          'Assinaturas e carimbos devem permanecer manuais e não são importados.'
        ],
        importadoEm: new Date().toISOString()
      };

      return of(response).pipe(delay(600));
    }

    const formData = new FormData();
    formData.append('arquivo', file);

    return this.http.post<HistoricoEscolarPdfImportResponse>(`${this.apiBaseUrl}/historicos-escolares/importacao-pdf`, formData);
  }

  salvarHistoricoEscolar(request: HistoricoEscolarSaveRequest): Observable<HistoricoEscolarSaveResponse> {
    if (this.usarMock) {
      console.info('Payload enviado para salvar Histórico Escolar:', request);
      const response: HistoricoEscolarSaveResponse = {
        idHistoricoEscolar: request.idHistoricoEscolar || 'hist-mock-0001',
        status: request.statusPretendido,
        mensagem: request.statusPretendido === 'COMPLETO'
          ? 'Histórico Escolar salvo como completo.'
          : 'Histórico Escolar salvo com pendências. Complete as abas restantes quando possível.',
        salvoEm: new Date().toISOString(),
        pendencias: []
      };

      return of(response).pipe(delay(300));
    }

    if (request.idHistoricoEscolar) {
      return this.http.put<HistoricoEscolarSaveResponse>(`${this.apiBaseUrl}/historicos-escolares/${request.idHistoricoEscolar}`, request);
    }

    return this.http.post<HistoricoEscolarSaveResponse>(`${this.apiBaseUrl}/historicos-escolares`, request);
  }
}
