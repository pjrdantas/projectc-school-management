import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { Assessment, AssessmentInput, StudentGrade, StudentGradeInput } from '../models/assessment.model';

@Injectable({ providedIn: 'root' })
export class AssessmentsService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listar(filters: { professorTurmaDisciplinaId?: string; turmaId?: string } = {}): Observable<Assessment[]> {
    let params = new HttpParams();

    if (filters.professorTurmaDisciplinaId) {
      params = params.set('professorTurmaDisciplinaId', filters.professorTurmaDisciplinaId);
    }

    if (filters.turmaId) {
      params = params.set('turmaId', filters.turmaId);
    }

    return this.http.get<Assessment[]>(`${this.apiBaseUrl}/api/avaliacoes`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  buscarPorId(id: string): Observable<Assessment> {
    return this.http.get<Assessment>(`${this.apiBaseUrl}/api/avaliacoes/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  criar(input: AssessmentInput): Observable<Assessment> {
    return this.http.post<Assessment>(`${this.apiBaseUrl}/api/avaliacoes`, input, {
      headers: this.buildHeaders(),
    });
  }

  listarNotas(avaliacaoId: string): Observable<StudentGrade[]> {
    return this.http.get<StudentGrade[]>(`${this.apiBaseUrl}/api/avaliacoes/${avaliacaoId}/notas`, {
      headers: this.buildHeaders(),
    });
  }

  lancarNota(avaliacaoId: string, input: StudentGradeInput): Observable<StudentGrade> {
    return this.http.post<StudentGrade>(`${this.apiBaseUrl}/api/avaliacoes/${avaliacaoId}/notas`, input, {
      headers: this.buildHeaders(),
    });
  }

  listarNotasPorMatricula(matriculaId: string): Observable<StudentGrade[]> {
    return this.http.get<StudentGrade[]>(`${this.apiBaseUrl}/api/matriculas/${matriculaId}/notas`, {
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
