import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { API_BASE_URL } from '../../core/config/api.config';
import {
  CepEndereco,
  Disciplina,
  DisciplinaInput,
  HistoricoEscolar,
  HistoricoEscolarInput,
  PageResponse,
  TransferenciaAluno,
  TransferenciaAlunoInput,
} from '../models/student-records.model';

@Injectable({ providedIn: 'root' })
export class StudentRecordsService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  listarDisciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(`${API_BASE_URL}/api/disciplinas`, {
      headers: this.buildHeaders(),
    });
  }

  criarDisciplina(input: DisciplinaInput): Observable<Disciplina> {
    return this.http.post<Disciplina>(`${API_BASE_URL}/api/disciplinas`, input, {
      headers: this.buildHeaders(),
    });
  }

  atualizarDisciplina(id: string, input: DisciplinaInput): Observable<Disciplina> {
    return this.http.put<Disciplina>(`${API_BASE_URL}/api/disciplinas/${id}`, input, {
      headers: this.buildHeaders(),
    });
  }

  listarHistoricosPorAluno(alunoId: string): Observable<HistoricoEscolar[]> {
    return this.listarHistoricos().pipe(map((historicos) => historicos));
  }

  listarHistoricos(): Observable<HistoricoEscolar[]> {
    return this.http
      .get<PageResponse<HistoricoEscolar>>(`${API_BASE_URL}/api/historicos-escolares`, {
        headers: this.buildHeaders(),
        params: { page: 0, size: 100 },
      })
      .pipe(map((page) => page.content ?? []));
  }

  criarHistorico(input: HistoricoEscolarInput): Observable<HistoricoEscolar> {
    return this.http.post<HistoricoEscolar>(`${API_BASE_URL}/api/historicos-escolares`, input, {
      headers: this.buildHeaders(),
    });
  }

  atualizarHistorico(idHistorico: string, input: HistoricoEscolarInput): Observable<HistoricoEscolar> {
    return this.http.put<HistoricoEscolar>(
      `${API_BASE_URL}/api/historicos-escolares/${idHistorico}`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  excluirHistorico(idHistorico: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/historicos-escolares/${idHistorico}`, {
      headers: this.buildHeaders(),
    });
  }

  listarTransferenciasPorAluno(alunoId: string): Observable<TransferenciaAluno[]> {
    return this.http.get<TransferenciaAluno[]>(
      `${API_BASE_URL}/api/transferencias/alunos/${alunoId}`,
      { headers: this.buildHeaders() },
    );
  }

  criarTransferencia(input: TransferenciaAlunoInput): Observable<TransferenciaAluno> {
    return this.http.post<TransferenciaAluno>(`${API_BASE_URL}/api/transferencias`, input, {
      headers: this.buildHeaders(),
    });
  }

  consultarCep(cep: string): Observable<CepEndereco> {
    return this.http.get<CepEndereco>(`${API_BASE_URL}/enderecos/cep/${cep}`, {
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
