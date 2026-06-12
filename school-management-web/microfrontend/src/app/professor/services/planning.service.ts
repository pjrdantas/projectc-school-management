import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  BimonthlyPlanning,
  BimonthlyPlanningAssessment,
  BimonthlyPlanningAssessmentInput,
  BimonthlyPlanningFilters,
  BimonthlyPlanningInput,
  BimonthlyPlanningLesson,
  BimonthlyPlanningLessonInput,
  PedagogicalContentLibraryFilters,
  PedagogicalContentLibraryItem,
  PlanningAiApprovalInput,
  PlanningAiContent,
  PlanningAiContentVersion,
  PlanningAiGenerateInput,
  PlanningAiInteraction,
  PlanningAiVersionInput,
} from '../models/planning.model';

@Injectable({ providedIn: 'root' })
export class PlanningService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listar(filters: BimonthlyPlanningFilters = {}): Observable<BimonthlyPlanning[]> {
    let params = new HttpParams();

    if (filters.professorId) {
      params = params.set('professorId', filters.professorId);
    }

    if (filters.turmaId) {
      params = params.set('turmaId', filters.turmaId);
    }

    if (filters.disciplinaId) {
      params = params.set('disciplinaId', filters.disciplinaId);
    }

    if (filters.periodoAvaliativoId) {
      params = params.set('periodoAvaliativoId', filters.periodoAvaliativoId);
    }

    return this.http.get<BimonthlyPlanning[]>(`${this.apiBaseUrl}/api/planejamentos-bimestrais`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  buscarPorId(id: string): Observable<BimonthlyPlanning> {
    return this.http.get<BimonthlyPlanning>(`${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  criar(input: BimonthlyPlanningInput): Observable<BimonthlyPlanning> {
    return this.http.post<BimonthlyPlanning>(`${this.apiBaseUrl}/api/planejamentos-bimestrais`, input, {
      headers: this.buildHeaders(),
    });
  }

  atualizar(id: string, input: BimonthlyPlanningInput): Observable<BimonthlyPlanning> {
    return this.http.put<BimonthlyPlanning>(`${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}`, input, {
      headers: this.buildHeaders(),
    });
  }

  adicionarAulaPrevista(id: string, input: BimonthlyPlanningLessonInput): Observable<BimonthlyPlanningLesson> {
    return this.http.post<BimonthlyPlanningLesson>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/aulas-previstas`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  adicionarAvaliacaoPrevista(
    id: string,
    input: BimonthlyPlanningAssessmentInput,
  ): Observable<BimonthlyPlanningAssessment> {
    return this.http.post<BimonthlyPlanningAssessment>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/avaliacoes-previstas`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  alterarStatus(id: string, status: string): Observable<BimonthlyPlanning> {
    return this.http.patch<BimonthlyPlanning>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/status`,
      { status },
      { headers: this.buildHeaders() },
    );
  }

  gerarConteudoIA(id: string, input: PlanningAiGenerateInput): Observable<PlanningAiContent> {
    return this.http.post<PlanningAiContent>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/ia/conteudos`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  listarInteracoesIA(id: string): Observable<PlanningAiInteraction[]> {
    return this.http.get<PlanningAiInteraction[]>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/ia/interacoes`,
      { headers: this.buildHeaders() },
    );
  }

  listarConteudosIA(id: string): Observable<PlanningAiContent[]> {
    return this.http.get<PlanningAiContent[]>(
      `${this.apiBaseUrl}/api/planejamentos-bimestrais/${id}/ia/conteudos`,
      { headers: this.buildHeaders() },
    );
  }

  criarVersaoIA(conteudoId: string, input: PlanningAiVersionInput): Observable<PlanningAiContentVersion> {
    return this.http.post<PlanningAiContentVersion>(
      `${this.apiBaseUrl}/api/ia/conteudos/${conteudoId}/versoes`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  listarVersoesIA(conteudoId: string): Observable<PlanningAiContentVersion[]> {
    return this.http.get<PlanningAiContentVersion[]>(
      `${this.apiBaseUrl}/api/ia/conteudos/${conteudoId}/versoes`,
      { headers: this.buildHeaders() },
    );
  }

  aprovarVersaoIA(conteudoId: string, input: PlanningAiApprovalInput): Observable<PlanningAiContent> {
    return this.http.patch<PlanningAiContent>(
      `${this.apiBaseUrl}/api/ia/conteudos/${conteudoId}/aprovar-versao`,
      input,
      { headers: this.buildHeaders() },
    );
  }

  publicarBibliotecaIA(conteudoId: string): Observable<PedagogicalContentLibraryItem> {
    return this.http.post<PedagogicalContentLibraryItem>(
      `${this.apiBaseUrl}/api/ia/conteudos/${conteudoId}/publicar-biblioteca`,
      {},
      { headers: this.buildHeaders() },
    );
  }

  listarBiblioteca(filters: PedagogicalContentLibraryFilters = {}): Observable<PedagogicalContentLibraryItem[]> {
    let params = new HttpParams();

    if (filters.professorId) {
      params = params.set('professorId', filters.professorId);
    }

    if (filters.disciplinaId) {
      params = params.set('disciplinaId', filters.disciplinaId);
    }

    if (filters.tipoConteudo) {
      params = params.set('tipoConteudo', filters.tipoConteudo);
    }

    if (filters.tema) {
      params = params.set('tema', filters.tema);
    }

    return this.http.get<PedagogicalContentLibraryItem[]>(
      `${this.apiBaseUrl}/api/biblioteca-conteudos-pedagogicos`,
      {
        headers: this.buildHeaders(),
        params,
      },
    );
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
