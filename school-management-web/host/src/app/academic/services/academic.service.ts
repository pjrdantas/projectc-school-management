import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import {
  AcademicClass,
  AcademicClassInput,
  AcademicPeriod,
  AcademicPeriodInput,
} from '../models/academic.model';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({ providedIn: 'root' })
export class AcademicService {
  private readonly http = inject(HttpClient);
  private readonly authState = inject(AuthStateService);

  private readonly periodsSubject = new BehaviorSubject<AcademicPeriod[]>([]);
  private readonly classesSubject = new BehaviorSubject<AcademicClass[]>([]);

  readonly periods$ = this.periodsSubject.asObservable();
  readonly classes$ = this.classesSubject.asObservable();

  listPeriods(): AcademicPeriod[] {
    return this.periodsSubject.value;
  }

  listClasses(): AcademicClass[] {
    return this.classesSubject.value;
  }

  createPeriod(input: AcademicPeriodInput): Observable<AcademicPeriod> {
    return this.http
      .post<AcademicPeriod>(`${API_BASE_URL}/api/periodos-letivos`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(period => this.upsertPeriod(period)));
  }

  updatePeriodLocally(id: string, input: AcademicPeriodInput): void {
    const current = this.periodsSubject.value;
    const index = current.findIndex(item => item.id === id);

    if (index === -1) {
      return;
    }

    const target = current[index];
    const next = [...current];
    next[index] = {
      ...target,
      nome: input.nome,
      dataInicio: input.dataInicio,
      dataFim: input.dataFim,
    };
    this.periodsSubject.next(next);
  }

  deletePeriodLocally(id: string): void {
    const current = this.periodsSubject.value;
    this.periodsSubject.next(current.filter(item => item.id !== id));
  }

  createClass(input: AcademicClassInput): Observable<AcademicClass> {
    return this.http
      .post<AcademicClass>(`${API_BASE_URL}/api/turmas`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turma => this.upsertClass(turma)));
  }

  fetchClassById(id: string): Observable<AcademicClass> {
    return this.http
      .get<AcademicClass>(`${API_BASE_URL}/api/turmas/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turma => this.upsertClass(turma)));
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

  private upsertPeriod(period: AcademicPeriod): void {
    const current = this.periodsSubject.value;
    const index = current.findIndex(item => item.id === period.id);

    if (index === -1) {
      this.periodsSubject.next([period, ...current]);
      return;
    }

    const next = [...current];
    next[index] = period;
    this.periodsSubject.next(next);
  }

  private upsertClass(turma: AcademicClass): void {
    const current = this.classesSubject.value;
    const index = current.findIndex(item => item.id === turma.id);

    if (index === -1) {
      this.classesSubject.next([turma, ...current]);
      return;
    }

    const next = [...current];
    next[index] = turma;
    this.classesSubject.next(next);
  }
}
