import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { AplicativosResponse } from '../models/aplicativos-response.model';

@Injectable({ providedIn: 'root' })
export class AplicativosService {
  private readonly updateSubject = new BehaviorSubject<void>(undefined);
  readonly update$ = this.updateSubject.asObservable();

  listActive(): Observable<AplicativosResponse[]> {
    return of([]);
  }

  triggerUpdate() {
    this.updateSubject.next();
  }
}
