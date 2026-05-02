import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthApiService } from '../../auth/services/auth-api.service';
import { AuthStateService } from './auth-state.service';

const AUTH_PUBLIC_ENDPOINTS = ['/api/auth/login', '/api/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
  const authApi = inject(AuthApiService);
  const router = inject(Router);

  const isPublicEndpoint = AUTH_PUBLIC_ENDPOINTS.some(endpoint => req.url.includes(endpoint));

  const token = authState.getToken();
  const authReq = token && !isPublicEndpoint
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const shouldTryRefresh = !isPublicEndpoint && error.status === 401;
      const refreshToken = authState.getRefreshToken();

      if (!shouldTryRefresh || !refreshToken) {
        if (shouldTryRefresh) {
          authState.clear();
          router.navigate(['/auth/login']);
        }
        return throwError(() => error);
      }

      return authApi.refresh(refreshToken).pipe(
        switchMap(response => {
          authState.setAuth(response.accessToken, response.refreshToken, {
            usuario: (response.username ?? response.login ?? ""),
            nome: response.nome,
            perfis: response.perfis ?? [],
            permissoes: response.permissoes ?? [],
          });

          const retryReq = req.clone({
            setHeaders: { Authorization: `Bearer ${response.accessToken}` },
          });

          return next(retryReq);
        }),
        catchError(refreshError => {
          authState.clear();
          router.navigate(['/auth/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
