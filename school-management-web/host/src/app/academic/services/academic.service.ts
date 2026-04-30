import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, forkJoin, map, of, tap } from 'rxjs';
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


  hydrateSeedData(): Observable<void> {
    const periodIds = Array.from({ length: 20 }, (_, i) =>
      `10000000-0000-0000-0000-${String(i + 1).padStart(12, '0')}`,
    );
    const classIds = Array.from({ length: 10 }, (_, i) =>
      `20000000-0000-0000-0000-${String(i + 1).padStart(12, '0')}`,
    );

    const periodRequests = periodIds.map(id =>
      this.http
        .get<AcademicPeriod>(`${API_BASE_URL}/api/periodos-letivos/${id}`, { headers: this.buildHeaders() })
        .pipe(catchError(() => of(null))),
    );

    const classRequests = classIds.map(id =>
      this.http
        .get<AcademicClass>(`${API_BASE_URL}/api/turmas/${id}`, { headers: this.buildHeaders() })
        .pipe(catchError(() => of(null))),
    );

    return forkJoin([...periodRequests, ...classRequests]).pipe(
      tap(results => {
        const periods = results.slice(0, periodIds.length).filter(Boolean) as AcademicPeriod[];
        const classes = results.slice(periodIds.length).filter(Boolean) as AcademicClass[];
        if (periods.length) {
          this.periodsSubject.next(periods);
        }
        if (classes.length) {
          this.classesSubject.next(classes);
        }
      }),
      map(() => void 0),
    );
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

  updateClassLocally(id: string, input: AcademicClassInput): void {
    const current = this.classesSubject.value;
    const index = current.findIndex(item => item.id === id);

    if (index === -1) {
      return;
    }

    const target = current[index];
    const next = [...current];
    next[index] = {
      ...target,
      codigo: input.codigo,
      nome: input.nome,
      capacidade: input.capacidade,
      periodoLetivoId: input.periodoLetivoId,
    };
    this.classesSubject.next(next);
  }

  deleteClassLocally(id: string): void {
    const current = this.classesSubject.value;
    this.classesSubject.next(current.filter(item => item.id !== id));
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
