import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../core/shell/shell-context.service';

export type FrequenciaStatus = 'P' | '.' | 'F';

export interface DiarioClasse {
  cabecalho: { idDiarioClasse: string; escola: string; diretoriaEnsino: string; municipio: string; anoLetivo: number; mes: number; dataAtual: string; turmaSerie: string; turno: string; disciplina: string; professor: string };
  alunos: Array<{ idAluno: string; numeroChamada: number; nome: string; frequencias: Record<number, FrequenciaStatus | ''> }>;
  conteudosPlanejados: Array<{ idPlanejamentoAula: string; periodo: string; descricao: string }>;
  observacoes: string[];
  avaliacoes: Array<{ idAvaliacao: string; data: string; descricao: string; turma: string; valor: string }>;
  assinatura?: { nomeProfessor: string; dataAssinatura: string };
  bloqueado?: boolean;
}

export interface DiarioClasseSalvar {
  idDiarioClasse: string;
  dataLancamento: string;
  frequencias: Array<{ idAluno: string; data: string; dia: number; status: FrequenciaStatus }>;
  conteudos: Array<{ idPlanejamentoAula: string; periodo: string; descricao: string; alterado: boolean; observacaoJustificativa?: string }>;
  observacoes: string[];
  assinatura: { nomeProfessor: string; dataAssinatura: string };
}

@Injectable({ providedIn: 'root' })
export class DiarioClasseService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  carregar(params: { idProfessor: string; idTurma: string; idDisciplina: string; anoLetivo: number; mes: number; dataReferencia: string }): Observable<DiarioClasse> {
    return this.http.get<DiarioClasse>(`${this.apiBaseUrl}/api/diarios-classe`, { headers: this.headers(), params: new HttpParams({ fromObject: params }) });
  }

  salvar(id: string, request: DiarioClasseSalvar): Observable<{ mensagem: string; bloqueado?: boolean }> {
    return this.http.put<{ mensagem: string; bloqueado?: boolean }>(`${this.apiBaseUrl}/api/diarios-classe/${id}`, request, { headers: this.headers() });
  }

  private get apiBaseUrl(): string { return this.shellContext.getApiBaseUrl(); }
  private headers(): HttpHeaders {
    const token = this.shellContext.getToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
