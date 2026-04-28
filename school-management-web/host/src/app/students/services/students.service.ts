import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Student, StudentInput } from '../models/student.model';

const STORAGE_KEY = 'students-crud-v1';
const API_BASE_URL = 'http://localhost:8080';

interface AlunoApiResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  email: string;
  telefone?: string;
  dataNascimento: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class StudentsService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  private readonly studentsSubject = new BehaviorSubject<Student[]>(this.load());
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
      .get<AlunoApiResponse[]>(`${API_BASE_URL}/api/alunos`, {
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
      .get<AlunoApiResponse>(`${API_BASE_URL}/api/alunos/${id}`, {
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
    const payload = {
      nomeCompleto: input.nomeCompleto,
      cpf: input.cpf,
      email: input.email,
      telefone: input.telefone,
      dataNascimento: input.dataNascimento,
    };

    return this.http
      .post<AlunoApiResponse>(`${API_BASE_URL}/api/alunos`, payload, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response, input.telefone)),
        tap(student => this.upsertStudent(student)),
      );
  }

  updateOnApi(id: string, input: StudentInput): Observable<Student> {
    const payload = {
      nomeCompleto: input.nomeCompleto,
      cpf: input.cpf,
      email: input.email,
      telefone: input.telefone,
      dataNascimento: input.dataNascimento,
    };

    return this.http
      .put<AlunoApiResponse>(`${API_BASE_URL}/api/alunos/${id}`, payload, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response, input.telefone)),
        tap(student => this.upsertStudent(student)),
      );
  }

  removeOnApi(id: string): Observable<void> {
    return this.http
      .delete<void>(`${API_BASE_URL}/api/alunos/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(() => this.removeLocal(id)));
  }

  private removeLocal(id: string): void {
    const next = this.studentsSubject.value.filter(student => student.id !== id);
    this.commit(next);
  }

  private buildHeaders(): HttpHeaders {
    const token = this.authState.getToken();
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
      email: response.email,
      dataNascimento: response.dataNascimento,
      telefone: telefone ?? response.telefone,
      createdAt: response.createdAt,
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
    localStorage.setItem(STORAGE_KEY, JSON.stringify(students));
  }

  private load(): Student[] {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as Student[];
    } catch {
      return [];
    }
  }
}
