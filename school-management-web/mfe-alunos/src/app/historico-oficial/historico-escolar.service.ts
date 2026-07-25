import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../core/shell/shell-context.service';

export type HistoricoStatus = 'RASCUNHO' | 'PENDENTE' | 'COMPLETO';
export interface HistoricoEscolar { contexto: { idHistoricoEscolar?: string; idAluno?: string; idMatricula?: string; modo: 'CADASTRO' | 'EDICAO'; status: HistoricoStatus; serieMatriculaAtual: number; serieConcluidaOrigem: number; escolaOrigem?: string; dataTransferencia?: string; bloqueado?: boolean }; cabecalho: Record<string, string>; aluno: Record<string, string>; periodos: Array<{ ordem: number; anoLetivo: string; serie: string; equivalencia: string }>; baseComum: Array<{ ordem: number; nome: string; valores: string[] }>; parteDiversificada: Array<{ ordem: number; nome: string; valores: string[] }>; totais: { totalBaseComum: string[]; totalParteDiversificada: string[]; totalAulasAnuais: string[]; totalCargaHoraria: string[] }; estudosRealizados: Array<{ ordem: number; serieAno: string; ano: string; escola: string; municipio: string; uf: string }>; observacoes: string; certificado: Record<string, string | number | null>; pendencias?: Array<{ mensagem: string; severidade: 'AVISO' | 'ERRO' }> }

@Injectable({ providedIn: 'root' })
export class HistoricoEscolarOficialService {
  private readonly http = inject(HttpClient); private readonly shellContext = inject(ShellContextService);
  novo(idAluno: string, idMatricula: string): Observable<HistoricoEscolar> { return this.http.get<HistoricoEscolar>(`${this.baseUrl}/api/historicos-escolares/novo`, { headers: this.headers(), params: new HttpParams({ fromObject: { idAluno, idMatricula, modo: 'CADASTRO' } }) }); }
  carregar(id: string): Observable<HistoricoEscolar> { return this.http.get<HistoricoEscolar>(`${this.baseUrl}/api/historicos-escolares/${id}/carregamento`, { headers: this.headers() }); }
  salvar(historico: HistoricoEscolar): Observable<{ idHistoricoEscolar: string; status: HistoricoStatus; mensagem: string }> { const id = historico.contexto.idHistoricoEscolar; return id ? this.http.put<{ idHistoricoEscolar: string; status: HistoricoStatus; mensagem: string }>(`${this.baseUrl}/api/historicos-escolares/${id}`, historico, { headers: this.headers() }) : this.http.post<{ idHistoricoEscolar: string; status: HistoricoStatus; mensagem: string }>(`${this.baseUrl}/api/historicos-escolares`, historico, { headers: this.headers() }); }
  importar(arquivo: File): Observable<{ historico: HistoricoEscolar; nomeArquivo: string; confiancaGeral: number; avisos: string[] }> { const dados = new FormData(); dados.append('arquivo', arquivo); return this.http.post<{ historico: HistoricoEscolar; nomeArquivo: string; confiancaGeral: number; avisos: string[] }>(`${this.baseUrl}/api/historicos-escolares/importacao-pdf`, dados, { headers: this.headers() }); }
  private get baseUrl(): string { return this.shellContext.getApiBaseUrl(); } private headers(): HttpHeaders { const token = this.shellContext.getToken(); return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders(); }
}
