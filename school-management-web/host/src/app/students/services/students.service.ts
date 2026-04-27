import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Student, StudentInput } from '../models/student.model';

const STORAGE_KEY = 'students-crud-v1';
const API_BASE_URL = 'http://localhost:8080';

interface AlunoApiResponse {
  id: number;
  nomeCompleto: string;
  cpf: string;
  email: string;
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

  isCpfInUse(cpf: string, exceptStudentId?: string): boolean {
    return this.studentsSubject.value.some(
      student => student.cpf === cpf && student.id !== exceptStudentId,
    );
  }

  create(input: StudentInput): Student {
    const student: Student = {
      id: crypto.randomUUID(),
      ...input,
      createdAt: new Date().toISOString(),
    };

    this.commit([student, ...this.studentsSubject.value]);
    return student;
  }

  createOnApi(input: StudentInput): Observable<Student> {
    const payload = {
      nomeCompleto: input.nomeCompleto,
      cpf: input.cpf,
      email: input.email,
      dataNascimento: input.dataNascimento,
    };

    return this.http
      .post<AlunoApiResponse>(`${API_BASE_URL}/api/alunos`, payload, {
        headers: this.buildHeaders(),
      })
      .pipe(
        map(response => this.mapToStudent(response, input.telefone)),
        tap(student => this.commit([student, ...this.studentsSubject.value])),
      );
  }

  update(id: string, input: StudentInput): Student | null {
    let updated: Student | null = null;

    const next = this.studentsSubject.value.map(student => {
      if (student.id !== id) {
        return student;
      }

      updated = { ...student, ...input };
      return updated;
    });

    if (!updated) {
      return null;
    }

    this.commit(next);
    return updated;
  }

  remove(id: string): boolean {
    const next = this.studentsSubject.value.filter(student => student.id !== id);
    if (next.length === this.studentsSubject.value.length) {
      return false;
    }

    this.commit(next);
    return true;
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
      id: String(response.id),
      nomeCompleto: response.nomeCompleto,
      cpf: this.onlyDigits(response.cpf),
      email: response.email,
      dataNascimento: response.dataNascimento,
      telefone,
      createdAt: response.createdAt,
    };
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
