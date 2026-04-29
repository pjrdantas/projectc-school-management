import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthSessionService } from '../../auth/services/auth-session.service';
import { AuthStateService } from './auth-state.service';

const AUTH_PUBLIC_ENDPOINTS = ['/api/auth/login', '/api/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
  const authSession = inject(AuthSessionService);
  const router = inject(Router);

  const isPublicEndpoint = AUTH_PUBLIC_ENDPOINTS.some(endpoint =>
    req.url.includes(endpoint),
  );

  const token = authState.getToken();
  const authReq =
    token && !isPublicEndpoint
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!isPublicEndpoint && (error.status === 401 || error.status === 403)) {
        authState.clear();
        authSession.signOut();
        router.navigate(['/auth/login']);
      }

      return throwError(() => error);
    }),
  );
};
