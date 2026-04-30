import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStateService } from './auth-state.service';

const AUTH_PUBLIC_ENDPOINTS = ['/api/auth/login', '/api/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
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
      if (!isPublicEndpoint && error.status === 401) {
        authState.clear();
        router.navigate(['/auth/login']);
      }

      return throwError(() => error);
    }),
  );
};
