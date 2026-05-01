import { Injectable } from '@angular/core';
import {
  HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse
} from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';
import { AuthStateService } from '../auth/auth-state.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private authService: AuthService,
    private authState: AuthStateService
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authState.getToken();

    const authReq = token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.isAuthEndpoint(req.url)) {
          return this.handle401Error(authReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      // Pega o token atual para enviar no refresh
      const currentRefreshToken = this.authState.getRefreshToken() || '';

      return this.authService.refreshToken(currentRefreshToken).pipe(
        switchMap((response: any) => { // Tipado como 'any' para evitar erro TS18046
          const newToken = response.token || response.accessToken; // Aceita tanto 'token' quanto 'accessToken'

          this.refreshTokenSubject.next(newToken);
          this.isRefreshing = false;

          return next.handle(
            request.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } })
          );
        }),
        catchError(err => {
          this.isRefreshing = false;
          this.authState.clear();
          return throwError(() => err);
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter(t => t !== null),
        take(1),
        switchMap(t =>
          next.handle(request.clone({ setHeaders: { Authorization: `Bearer ${t}` } }))
        )
      );
    }
  }

  private isAuthEndpoint(url: string): boolean {
    return url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/validate-session');
  }
}
