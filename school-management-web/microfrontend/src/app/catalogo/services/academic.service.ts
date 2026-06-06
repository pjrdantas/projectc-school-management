import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, forkJoin, map, tap } from 'rxjs';
import { ShellContextService } from '../../core/shell/shell-context.service';
import {
  AcademicClass,
  AcademicClassDiscipline,
  AcademicClassInput,
  AcademicPeriod,
  AcademicPeriodInput,
  AcademicSeries,
  AcademicSeriesInput,
  AcademicShift,
  AcademicShiftInput,
} from '../models/academic.model';

@Injectable({ providedIn: 'root' })
export class AcademicService {
  private readonly http = inject(HttpClient);
  private readonly shellContext = inject(ShellContextService);

  private readonly periodsSubject = new BehaviorSubject<AcademicPeriod[]>([]);
  private readonly seriesSubject = new BehaviorSubject<AcademicSeries[]>([]);
  private readonly shiftsSubject = new BehaviorSubject<AcademicShift[]>([]);
  private readonly classesSubject = new BehaviorSubject<AcademicClass[]>([]);

  readonly periods$ = this.periodsSubject.asObservable();
  readonly series$ = this.seriesSubject.asObservable();
  readonly shifts$ = this.shiftsSubject.asObservable();
  readonly classes$ = this.classesSubject.asObservable();

  listPeriods(): AcademicPeriod[] {
    return this.periodsSubject.value;
  }

  listClasses(): AcademicClass[] {
    return this.classesSubject.value;
  }

  listSeries(): AcademicSeries[] {
    return this.seriesSubject.value;
  }

  listShifts(): AcademicShift[] {
    return this.shiftsSubject.value;
  }

  createSeries(input: AcademicSeriesInput): Observable<AcademicSeries> {
    return this.http
      .post<AcademicSeries>(`${this.apiBaseUrl}/api/series`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(serie => this.upsertSeries(serie)));
  }

  updateSeries(id: string, input: AcademicSeriesInput): Observable<AcademicSeries> {
    return this.http
      .put<AcademicSeries>(`${this.apiBaseUrl}/api/series/${id}`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(serie => this.upsertSeries(serie)));
  }

  deleteSeriesLocally(id: string): void {
    const current = this.seriesSubject.value;
    this.seriesSubject.next(current.filter(item => item.id !== id));
  }

  createShift(input: AcademicShiftInput): Observable<AcademicShift> {
    return this.http
      .post<AcademicShift>(`${this.apiBaseUrl}/api/turnos`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turno => this.upsertShift(turno)));
  }

  updateShift(id: string, input: AcademicShiftInput): Observable<AcademicShift> {
    return this.http
      .put<AcademicShift>(`${this.apiBaseUrl}/api/turnos/${id}`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turno => this.upsertShift(turno)));
  }

  deleteShiftLocally(id: string): void {
    const current = this.shiftsSubject.value;
    this.shiftsSubject.next(current.filter(item => item.id !== id));
  }

  syncFromApi(): Observable<void> {
    return forkJoin({
      periods: this.http.get<AcademicPeriod[]>(`${this.apiBaseUrl}/api/periodos-letivos`, {
        headers: this.buildHeaders(),
      }),
      series: this.http.get<AcademicSeries[]>(`${this.apiBaseUrl}/api/series`, {
        headers: this.buildHeaders(),
      }),
      shifts: this.http.get<AcademicShift[]>(`${this.apiBaseUrl}/api/turnos`, {
        headers: this.buildHeaders(),
      }),
      classes: this.http.get<AcademicClass[]>(`${this.apiBaseUrl}/api/turmas`, {
        headers: this.buildHeaders(),
      }),
    }).pipe(
      tap(({ periods, series, shifts, classes }) => {
        this.periodsSubject.next(periods);
        this.seriesSubject.next(series);
        this.shiftsSubject.next(shifts);
        this.classesSubject.next(classes);
      }),
      map(() => void 0),
    );
  }

  createPeriod(input: AcademicPeriodInput): Observable<AcademicPeriod> {
    return this.http
      .post<AcademicPeriod>(`${this.apiBaseUrl}/api/periodos-letivos`, input, {
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
      .post<AcademicClass>(`${this.apiBaseUrl}/api/turmas`, input, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turma => this.upsertClass(turma)));
  }

  updateClass(id: string, input: AcademicClassInput): Observable<AcademicClass> {
    return this.http
      .put<AcademicClass>(`${this.apiBaseUrl}/api/turmas/${id}`, input, {
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
      serieId: input.serieId,
      turno: input.turno ?? target.turno,
      status: input.status ?? target.status,
    };
    this.classesSubject.next(next);
  }

  deleteClassLocally(id: string): void {
    const current = this.classesSubject.value;
    this.classesSubject.next(current.filter(item => item.id !== id));
  }

  fetchClassById(id: string): Observable<AcademicClass> {
    return this.http
      .get<AcademicClass>(`${this.apiBaseUrl}/api/turmas/${id}`, {
        headers: this.buildHeaders(),
      })
      .pipe(tap(turma => this.upsertClass(turma)));
  }

  listClassDisciplines(classId: string): Observable<AcademicClassDiscipline[]> {
    return this.http.get<AcademicClassDiscipline[]>(
      `${this.apiBaseUrl}/api/turmas/${classId}/disciplinas`,
      {
        headers: this.buildHeaders(),
      },
    );
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

  private upsertSeries(serie: AcademicSeries): void {
    const current = this.seriesSubject.value;
    const index = current.findIndex(item => item.id === serie.id);

    if (index === -1) {
      this.seriesSubject.next([serie, ...current]);
      return;
    }

    const next = [...current];
    next[index] = serie;
    this.seriesSubject.next(next);
  }

  private upsertShift(turno: AcademicShift): void {
    const current = this.shiftsSubject.value;
    const index = current.findIndex(item => item.id === turno.id);

    if (index === -1) {
      this.shiftsSubject.next([turno, ...current]);
      return;
    }

    const next = [...current];
    next[index] = turno;
    this.shiftsSubject.next(next);
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
