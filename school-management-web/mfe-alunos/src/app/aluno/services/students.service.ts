import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import { Student, StudentInput } from '../models/student.model';

interface AlunoApiResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  orgaoEmissorRg?: string;
  ufRg?: string;
  email: string;
  telefone?: string;
  dataNascimento: string;
  nacionalidade?: string;
  naturalidade?: string;
  sexo?: string;
  nomeSocial?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  statusAluno?: string;
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
export class StudentsService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  private readonly studentsSubject = new BehaviorSubject<Student[]>([]);
  readonly students$ = this.studentsSubject.asObservable();

  list(): Student[] {
    return this.studentsSubject.value;
  }

  getById(id: string): Student | undefined {
    return this.studentsSubject.value.find(student => student.id === id);
  }

  syncFromApi(nome?: string): Observable<Student[]> {
    const params = nome ? new HttpParams().set('nome', nome) : undefined;

    return this.http
      .get<AlunoApiResponse[]>(`${this.apiBaseUrl}/api/alunos`, {
        headers: this.buildHeaders(),
        params,
      })
      .pipe(
        map(response => response.map(item => this.mapToStudent(item))),
        tap(students => this.commit(students)),
      );
  }

  fetchByIdFromApi(id: string): Observable<Student> {
    return this.http
      .get<AlunoApiResponse>(`${this.apiBaseUrl}/api/alunos/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response)),
        tap(student => this.upsertStudent(student)),
      );
  }

  isCpfInUse(cpf: string, exceptStudentId?: string): boolean {
    return this.studentsSubject.value.some(
      student => student.cpf === cpf && student.id !== exceptStudentId,
    );
  }

  createOnApi(input: StudentInput): Observable<Student> {
    const payload = this.toPayload(input);

    return this.http
      .post<AlunoApiResponse>(`${this.apiBaseUrl}/api/alunos`, payload, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response, input.telefone)),
        tap(student => this.upsertStudent(student)),
      );
  }

  updateOnApi(id: string, input: StudentInput): Observable<Student> {
    const payload = this.toPayload(input);

    return this.http
      .put<AlunoApiResponse>(`${this.apiBaseUrl}/api/alunos/${id}`, payload, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response, input.telefone)),
        tap(student => this.upsertStudent(student)),
      );
  }

  removeOnApi(id: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/api/alunos/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(() => this.removeLocal(id)));
  }

  consultarCep(cep: string): Observable<CepEndereco> {
    return this.http.get<CepEndereco>(`${this.apiBaseUrl}/enderecos/cep/${cep}`, {
      headers: this.buildHeaders(),
    });
  }

  private removeLocal(id: string): void {
    const next = this.studentsSubject.value.filter(student => student.id !== id);
    this.commit(next);
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

  private mapToStudent(response: AlunoApiResponse, telefone?: string): Student {
    return {
      id: response.id,
      nomeCompleto: response.nomeCompleto,
      cpf: this.onlyDigits(response.cpf),
      rg: response.rg,
      orgaoEmissorRg: response.orgaoEmissorRg,
      ufRg: response.ufRg,
      email: response.email,
      dataNascimento: response.dataNascimento,
      telefone: telefone ?? response.telefone,
      nacionalidade: response.nacionalidade,
      naturalidade: response.naturalidade,
      sexo: response.sexo,
      nomeSocial: response.nomeSocial,
      cep: response.cep,
      logradouro: response.logradouro,
      numero: response.numero,
      complemento: response.complemento,
      bairro: response.bairro,
      cidade: response.cidade,
      uf: response.uf,
      statusAluno: response.statusAluno,
      createdAt: response.createdAt,
    };
  }

  private toPayload(input: StudentInput): StudentInput {
    return {
      nomeCompleto: input.nomeCompleto,
      cpf: input.cpf,
      rg: input.rg,
      orgaoEmissorRg: input.orgaoEmissorRg,
      ufRg: input.ufRg,
      email: input.email,
      telefone: input.telefone,
      dataNascimento: input.dataNascimento,
      nacionalidade: input.nacionalidade,
      naturalidade: input.naturalidade,
      sexo: input.sexo,
      nomeSocial: input.nomeSocial,
      cep: input.cep,
      logradouro: input.logradouro,
      numero: input.numero,
      complemento: input.complemento,
      bairro: input.bairro,
      cidade: input.cidade,
      uf: input.uf,
      statusAluno: input.statusAluno,
    };
  }

  private upsertStudent(student: Student): void {
    const current = this.studentsSubject.value;
    const index = current.findIndex(item => item.id === student.id);

    if (index === -1) {
      this.commit([student, ...current]);
      return;
    }

    const next = [...current];
    next[index] = { ...next[index], ...student };
    this.commit(next);
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private commit(students: Student[]): void {
    this.studentsSubject.next(students);
  }
}
