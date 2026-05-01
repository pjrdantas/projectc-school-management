import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';

// Material
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../core/auth/auth.service';
import { AuthStateService } from '../../core/auth/auth-state.service';

interface AuthResponse {
  token: string;
  refreshToken: string;
  usuario: string;
  nome: string;
  perfis: string[];
  permissoes: string[];
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  // Injeção moderna com 'inject' resolve o erro de Injection Token em MFEs
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private authState = inject(AuthStateService);
  private cdr = inject(ChangeDetectorRef);
  private snackBar = inject(MatSnackBar);

  hide = true;
  loginForm: FormGroup;
  loading = false;
  private readonly MIN_LOADING_TIME = 500;

  constructor() {
    this.loginForm = this.fb.group({
      login: ['', [Validators.required, Validators.minLength(6)]],
      senha: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void { }

  fazerLogin(): void {
    if (this.loginForm.invalid || this.loading) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const inicio = Date.now();
    this.loading = true;
    this.cdr.detectChanges();

    this.authService.login(this.loginForm.value)
      .pipe(
        finalize(() => {
          const decorrido = Date.now() - inicio;
          const restante = this.MIN_LOADING_TIME - decorrido;
          setTimeout(() => {
            this.loading = false;
            this.cdr.detectChanges();
          }, Math.max(0, restante));
        })
      )
      .subscribe({
        next: (res: any) => {
          const response = res as AuthResponse;
          this.authState.setAuth(
            response.token,
            response.refreshToken,
            {
              usuario: response.usuario,
              nome: response.nome,
              perfis: response.perfis,
              permissoes: response.permissoes || []
            }
          );
          this.router.navigate(['/app']);
        },
        error: (err: any) => {
          if (err && err.status === 401) {
            this.snackBar.open('Usuário ou senha inválidos', 'Fechar', { duration: 5000 });
          } else {
            this.snackBar.open('Erro ao autenticar. Tente novamente.', 'Fechar', { duration: 8000, panelClass: 'error-snackbar' });
          }
        }
      });
  }
}
