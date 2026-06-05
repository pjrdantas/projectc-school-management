import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ShellContextService } from '../../core/shell/shell-context.service';
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
  private readonly shellContext = inject(ShellContextService);

  listarDisciplinas(): Observable<Disciplina[]> {
    return this.http.get<Disciplina[]>(`${this.apiBaseUrl}/api/disciplinas`, {
      headers: this.buildHeaders(),
    });
  }

  criarDisciplina(input: DisciplinaInput): Observable<Disciplina> {
    return this.http.post<Disciplina>(`${this.apiBaseUrl}/api/disciplinas`, input, {
      headers: this.buildHeaders(),
    });
  }

  atualizarDisciplina(id: string, input: DisciplinaInput): Observable<Disciplina> {
    return this.http.put<Disciplina>(`${this.apiBaseUrl}/api/disciplinas/${id}`, input, {
      headers: this.buildHeaders(),
    });
  }

  listarHistoricosPorAluno(alunoId: string): Observable<HistoricoEscolar[]> {
    return this.listarHistoricos().pipe(map((historicos) => historicos));
  }

  listarHistoricos(): Observable<HistoricoEscolar[]> {
    return this.http
      .get<PageResponse<HistoricoEscolar>>(`${this.apiBaseUrl}/api/historicos-escolares`, {
        headers: this.buildHeaders(),
        params: { page: 0, size: 100 },
      })
      .pipe(map((page) => page.content ?? []));
  }

  criarHistorico(input: HistoricoEscolarInput): Observable<HistoricoEscolar> {
    return this.http.post<HistoricoEscolar>(`${this.apiBaseUrl}/api/historicos-escolares`, input, {
      headers: this.buildHeaders(),
    });
  }

  atualizarHistorico(idHistorico: string, input: HistoricoEscolarInput): Observable<HistoricoEscolar> {
    return this.http.put<HistoricoEscolar>(
      `${this.apiBaseUrl}/api/historicos-escolares/${idHistorico}`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  excluirHistorico(idHistorico: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/historicos-escolares/${idHistorico}`, {
      headers: this.buildHeaders(),
    });
  }

  listarTransferenciasPorAluno(alunoId: string): Observable<TransferenciaAluno[]> {
    return this.http.get<TransferenciaAluno[]>(
      `${this.apiBaseUrl}/api/transferencias/alunos/${alunoId}`,
      { headers: this.buildHeaders() },
    );
  }

  criarTransferencia(input: TransferenciaAlunoInput): Observable<TransferenciaAluno> {
    return this.http.post<TransferenciaAluno>(`${this.apiBaseUrl}/api/transferencias`, input, {
      headers: this.buildHeaders(),
    });
  }

  consultarCep(cep: string): Observable<CepEndereco> {
    return this.http.get<CepEndereco>(`${this.apiBaseUrl}/enderecos/cep/${cep}`, {
      headers: this.buildHeaders(),
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
