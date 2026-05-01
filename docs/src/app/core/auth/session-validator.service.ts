import { Injectable, inject, OnDestroy } from '@angular/core';
import { interval, Subject, takeUntil } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from './auth.service';
import { AuthStateService } from './auth-state.service';

@Injectable({
  providedIn: 'root'
})
export class SessionValidatorService implements OnDestroy {
  private authService = inject(AuthService);
  private authState = inject(AuthStateService);
  private destroy$ = new Subject<void>();
  private validationIntervalMs = 10000;

  constructor() {
    this.startSessionValidation();
  }

  private startSessionValidation(): void {
    interval(this.validationIntervalMs)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => {
          const token = this.authState.getToken();
          if (!token) return of(null);

          return this.authService.validateSession(token).pipe(
            catchError(() => {

              this.authState.clear();
              return of({ valid: false });
            })
          );

        })
      )
      .subscribe({
        next: (response) => {
          if (response && response.valid === false) {
            this.authState.clear();
          }
        }
      });
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
