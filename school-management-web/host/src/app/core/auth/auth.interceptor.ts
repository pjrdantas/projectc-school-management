import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthApiService } from '../../auth/services/auth-api.service';
import { AuthStateService } from './auth-state.service';

const AUTH_PUBLIC_ENDPOINTS = ['/api/auth/login', '/api/auth/refresh', '/api/auth/logout'];

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

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
        return throwError(() => error);
      }

      return handle401Error(authReq, next, authState, authApi, router);
    }),
  );
};

function handle401Error(
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authState: AuthStateService,
  authApi: AuthApiService,
  router: Router,
): Observable<HttpEvent<unknown>> {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const currentRefreshToken = authState.getRefreshToken() ?? '';

    return authApi.refresh(currentRefreshToken).pipe(
      switchMap(response => {
        authState.setAuth(response.accessToken, response.refreshToken, {
          usuario: response.username,
          nome: response.nome,
          perfis: response.perfis ?? [],
          permissoes: response.permissoes ?? [],
        });

        refreshTokenSubject.next(response.accessToken);
        isRefreshing = false;

        return next(request.clone({
          setHeaders: { Authorization: `Bearer ${response.accessToken}` },
        }));
      }),
      catchError(refreshError => {
        isRefreshing = false;
        authState.clear();
        router.navigate(['/auth/login']);
        return throwError(() => refreshError);
      }),
    );
  }

  return refreshTokenSubject.pipe(
    filter(token => token !== null),
    take(1),
    switchMap(token => next(request.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }))),
  );
}
