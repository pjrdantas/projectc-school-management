import { Injectable, inject } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AuthStateService } from '../auth/auth-state.service';
import { LoggerService } from '../logger/logger.service';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  private snackBar = inject(MatSnackBar);
  private logger = inject(LoggerService);
  private router = inject(Router);
  private authState = inject(AuthStateService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {

        // 1. Erro 401: Não Autorizado / Sessão Expirada
        if (error.status === 401) {
          this.logger.warn('Sessão encerrada (Login duplo ou expirado)');
          this.snackBar.open('Sessão encerrada (Login realizado em outro local)', 'Ok', { duration: 5000 });
          this.authState.clear();
          this.router.navigate(['/login']);
          return EMPTY;
        }

        // 2. Erro 403: Acesso Negado (A correção para o seu usuário novo)
        else if (error.status === 403) {
          const message = error.error?.message || 'Você não tem permissão para esta ação.';

          this.logger.error('Acesso Negado (403)', { url: req.url, message });

          this.snackBar.open(message, 'Fechar', {
            duration: 5000,
            panelClass: ['error-snackbar'] // Garante o visual vermelho padronizado
          });
        }

        // 3. Outros Erros (500, 404, etc)
        else {
          this.logger.error('HTTP Error', {
            url: req.url,
            status: error.status,
            message: error.message,
            error
          });

          this.snackBar.open(
            'Erro inesperado ao processar a requisição',
            'Fechar',
            { duration: 3000 }
          );
        }

        return throwError(() => error);
      })
    );
  }
}
