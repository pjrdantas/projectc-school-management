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
