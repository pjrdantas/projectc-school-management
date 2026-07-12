import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';
import { DIARIO_CLASSE_MOCK } from '../mock/diario-classe.mock';
import {
  DiarioClasseLoadRequest,
  DiarioClasseResponse,
  DiarioClasseSaveRequest,
  DiarioClasseSaveResponse
} from '../models/diario-classe-api.models';

@Injectable({ providedIn: 'root' })
export class DiarioClasseApiService {
  private readonly apiBaseUrl = 'http://localhost:8080/api';

  /**
   * Enquanto o backend Java não estiver ligado, mantenha true.
   * Quando a API estiver pronta, troque para false.
   */
  private readonly usarMock = true;

  constructor(private readonly http: HttpClient) {}

  carregarDiarioClasse(request: DiarioClasseLoadRequest): Observable<DiarioClasseResponse> {
    if (this.usarMock) {
      return of(structuredClone(DIARIO_CLASSE_MOCK)).pipe(delay(250));
    }

    const params = new HttpParams()
      .set('idProfessor', request.idProfessor)
      .set('idTurma', request.idTurma)
      .set('idDisciplina', request.idDisciplina)
      .set('anoLetivo', request.anoLetivo)
      .set('mes', request.mes)
      .set('dataReferencia', request.dataReferencia);

    return this.http.get<DiarioClasseResponse>(`${this.apiBaseUrl}/diarios-classe`, { params });
  }

  salvarDiarioClasse(request: DiarioClasseSaveRequest): Observable<DiarioClasseSaveResponse> {
    if (this.usarMock) {
      console.info('Payload enviado para salvar Diário de Classe:', request);
      const response: DiarioClasseSaveResponse = {
        idDiarioClasse: request.idDiarioClasse,
        status: 'SALVO',
        mensagem: 'Diário de Classe salvo com sucesso. O formulário foi bloqueado para novos lançamentos.',
        salvoEm: new Date().toISOString(),
        bloqueado: true
      };
      return of(response).pipe(delay(300));
    }

    return this.http.put<DiarioClasseSaveResponse>(`${this.apiBaseUrl}/diarios-classe/${request.idDiarioClasse}`, request);
  }
}
