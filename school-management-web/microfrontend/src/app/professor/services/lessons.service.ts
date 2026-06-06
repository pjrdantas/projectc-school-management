import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { Lesson, LessonInput } from '../models/lesson.model';

@Injectable({ providedIn: 'root' })
export class LessonsService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listar(filters: { professorTurmaDisciplinaId?: string; turmaId?: string } = {}): Observable<Lesson[]> {
    let params = new HttpParams();

    if (filters.professorTurmaDisciplinaId) {
      params = params.set('professorTurmaDisciplinaId', filters.professorTurmaDisciplinaId);
    }

    if (filters.turmaId) {
      params = params.set('turmaId', filters.turmaId);
    }

    return this.http.get<Lesson[]>(`${this.apiBaseUrl}/api/aulas`, {
      headers: this.buildHeaders(),
      params,
    });
  }

  buscarPorId(id: string): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.apiBaseUrl}/api/aulas/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  criar(input: LessonInput): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.apiBaseUrl}/api/aulas`, input, {
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
