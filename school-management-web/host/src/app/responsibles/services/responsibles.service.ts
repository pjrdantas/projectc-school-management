import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Responsible, ResponsibleInput } from '../models/responsible.model';

const STORAGE_KEY = 'responsibles-crud-v1';
const API_BASE_URL = 'http://localhost:8080';

interface ResponsavelApiResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  email?: string;
  telefone?: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ResponsiblesService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  private readonly responsiblesSubject = new BehaviorSubject<Responsible[]>(this.load());
  readonly responsibles$ = this.responsiblesSubject.asObservable();

  list(): Responsible[] {
    return this.responsiblesSubject.value;
  }

  syncFromApi(nome?: string, cpf?: string): Observable<Responsible[]> {
    let params = new HttpParams();
    if (nome) params = params.set('nome', nome);
    if (cpf) params = params.set('cpf', cpf);

    return this.http
      .get<ResponsavelApiResponse[]>(`${API_BASE_URL}/api/responsaveis`, {
        headers: this.buildHeaders(),
        params,
      })
      .pipe(
        map(response => response.map(item => this.mapToResponsible(item))),
        tap(responsibles => this.commit(responsibles)),
      );
  }

  fetchByIdFromApi(id: string): Observable<Responsible> {
    return this.http
      .get<ResponsavelApiResponse>(`${API_BASE_URL}/api/responsaveis/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(map(response => this.mapToResponsible(response)));
  }

  createOnApi(input: ResponsibleInput): Observable<Responsible> {
    return this.http
      .post<ResponsavelApiResponse>(`${API_BASE_URL}/api/responsaveis`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToResponsible(response)),
        tap(responsible => this.upsertResponsible(responsible)),
      );
  }

  updateOnApi(id: string, input: ResponsibleInput): Observable<Responsible> {
    return this.http
      .put<ResponsavelApiResponse>(`${API_BASE_URL}/api/responsaveis/${id}`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToResponsible(response)),
        tap(responsible => this.upsertResponsible(responsible)),
      );
  }

  removeOnApi(id: string): Observable<void> {
    return this.http
      .delete<void>(`${API_BASE_URL}/api/responsaveis/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(() => this.removeLocal(id)));
  }

  vincularAlunoResponsavel(idAluno: string, idResponsavel: string): Observable<void> {
    return this.http.post<void>(
      `${API_BASE_URL}/api/alunos/${idAluno}/responsaveis/${idResponsavel}`,
      {},
      { headers: this.buildHeaders() },
    );
  }

  listarResponsaveisPorAluno(idAluno: string): Observable<Responsible[]> {
    return this.http
      .get<ResponsavelApiResponse[]>(`${API_BASE_URL}/api/alunos/${idAluno}/responsaveis`, {
        headers: this.buildHeaders(),
      })
      .pipe(map(response => response.map(item => this.mapToResponsible(item))));
  }

  desvincularAlunoResponsavel(idAluno: string, idResponsavel: string): Observable<void> {
    return this.http.delete<void>(
      `${API_BASE_URL}/api/alunos/${idAluno}/responsaveis/${idResponsavel}`,
      { headers: this.buildHeaders() },
    );
  }

  private upsertResponsible(responsible: Responsible): void {
    const current = this.responsiblesSubject.value;
    const index = current.findIndex(item => item.id === responsible.id);

    if (index === -1) {
      this.commit([responsible, ...current]);
      return;
    }

    const next = [...current];
    next[index] = { ...next[index], ...responsible };
    this.commit(next);
  }

  private removeLocal(id: string): void {
    this.commit(this.responsiblesSubject.value.filter(item => item.id !== id));
  }

  private mapToResponsible(response: ResponsavelApiResponse): Responsible {
    return {
      id: response.id,
      nomeCompleto: response.nomeCompleto,
      cpf: response.cpf.replace(/\D/g, ''),
      email: response.email,
      telefone: response.telefone,
      createdAt: response.createdAt,
    };
  }

  private buildHeaders(): HttpHeaders {
    const token = this.authState.getToken();
    if (!token) return new HttpHeaders({ 'Content-Type': 'application/json' });

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private commit(responsibles: Responsible[]): void {
    this.responsiblesSubject.next(responsibles);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(responsibles));
  }

  private load(): Responsible[] {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as Responsible[];
    } catch {
      return [];
    }
  }
}
