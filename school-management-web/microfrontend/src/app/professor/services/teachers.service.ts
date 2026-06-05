import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  Professor,
  ProfessorFuncionarioElegivel,
  ProfessorInput,
} from '../models/teacher.model';

@Injectable({ providedIn: 'root' })
export class TeachersService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  listar(): Observable<Professor[]> {
    return this.http.get<Professor[]>(`${this.apiBaseUrl}/api/professores`, {
      headers: this.buildHeaders(),
    });
  }

  buscarPorId(id: string): Observable<Professor> {
    return this.http.get<Professor>(`${this.apiBaseUrl}/api/professores/${id}`, {
      headers: this.buildHeaders(),
    });
  }

  listarFuncionariosElegiveis(): Observable<ProfessorFuncionarioElegivel[]> {
    return this.http.get<ProfessorFuncionarioElegivel[]>(
      `${this.apiBaseUrl}/api/professores/funcionarios-elegiveis`,
      {
        headers: this.buildHeaders(),
      },
    );
  }

  criar(input: ProfessorInput): Observable<Professor> {
    return this.http.post<Professor>(`${this.apiBaseUrl}/api/professores`, input, {
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
