import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { Responsible, ResponsibleInput } from './responsible.model';

interface ResponsavelApiResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  createdAt: string;
}

export interface CepEndereco {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  uf: string;
  complemento?: string;
}

@Injectable({ providedIn: 'root' })
export class ResponsiblesService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  private readonly responsiblesSubject = new BehaviorSubject<Responsible[]>([]);
  readonly responsibles$ = this.responsiblesSubject.asObservable();

  list(): Responsible[] {
    return this.responsiblesSubject.value;
  }

  syncFromApi(nome?: string, cpf?: string): Observable<Responsible[]> {
    let params = new HttpParams();
    if (nome) params = params.set('nome', nome);
    if (cpf) params = params.set('cpf', cpf);

    return this.http
      .get<ResponsavelApiResponse[]>(`${this.apiBaseUrl}/api/responsaveis`, {
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
      .get<ResponsavelApiResponse>(`${this.apiBaseUrl}/api/responsaveis/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(map(response => this.mapToResponsible(response)));
  }

  createOnApi(input: ResponsibleInput): Observable<Responsible> {
    return this.http
      .post<ResponsavelApiResponse>(`${this.apiBaseUrl}/api/responsaveis`, this.toPayload(input), {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToResponsible(response)),
        tap(responsible => this.upsertResponsible(responsible)),
      );
  }

  updateOnApi(id: string, input: ResponsibleInput): Observable<Responsible> {
    return this.http
      .put<ResponsavelApiResponse>(
        `${this.apiBaseUrl}/api/responsaveis/${id}`,
        this.toPayload(input),
        {
          headers: this.buildHeaders(),
        },
      )
      .pipe(
        map(response => this.mapToResponsible(response)),
        tap(responsible => this.upsertResponsible(responsible)),
      );
  }

  removeOnApi(id: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/api/responsaveis/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(() => this.removeLocal(id)));
  }

  consultarCep(cep: string): Observable<CepEndereco> {
    return this.http.get<CepEndereco>(`${this.apiBaseUrl}/enderecos/cep/${cep}`, {
      headers: this.buildHeaders(),
    });
  }

  vincularAlunoResponsavel(idAluno: string, idResponsavel: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiBaseUrl}/api/alunos/${idAluno}/responsaveis/${idResponsavel}`,
      {},
      { headers: this.buildHeaders() },
    );
  }

  listarResponsaveisPorAluno(idAluno: string): Observable<Responsible[]> {
    return this.http
      .get<ResponsavelApiResponse[]>(`${this.apiBaseUrl}/api/alunos/${idAluno}/responsaveis`, {
        headers: this.buildHeaders(),
      })
      .pipe(map(response => response.map(item => this.mapToResponsible(item))));
  }

  desvincularAlunoResponsavel(idAluno: string, idResponsavel: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiBaseUrl}/api/alunos/${idAluno}/responsaveis/${idResponsavel}`,
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
      rg: response.rg,
      email: response.email,
      telefone: response.telefone,
      cep: response.cep,
      logradouro: response.logradouro,
      numero: response.numero,
      complemento: response.complemento,
      bairro: response.bairro,
      cidade: response.cidade,
      uf: response.uf,
      createdAt: response.createdAt,
    };
  }

  private toPayload(input: ResponsibleInput): ResponsibleInput {
    return {
      nomeCompleto: input.nomeCompleto,
      cpf: input.cpf,
      rg: input.rg,
      email: input.email,
      telefone: input.telefone,
      cep: input.cep,
      logradouro: input.logradouro,
      numero: input.numero,
      complemento: input.complemento,
      bairro: input.bairro,
      cidade: input.cidade,
      uf: input.uf,
    };
  }

  private get apiBaseUrl(): string {
    return this.shellContext.getApiBaseUrl();
  }

  private buildHeaders(): HttpHeaders {
    const token = this.shellContext.getToken();
    if (!token) return new HttpHeaders({ 'Content-Type': 'application/json' });

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private commit(responsibles: Responsible[]): void {
    this.responsiblesSubject.next(responsibles);
  }
}
