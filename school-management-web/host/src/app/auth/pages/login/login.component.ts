import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, Renderer2, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { AuthSessionService } from '../../services/auth-session.service';
import { AuthApiService } from '../../services/auth-api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authSession = inject(AuthSessionService);
  private readonly authState = inject(AuthStateService);
  private readonly authApi = inject(AuthApiService);
  private readonly fb = inject(FormBuilder);
  private readonly renderer = inject(Renderer2);

  loading = false;
  authError: string | null = null;

  readonly form = this.fb.nonNullable.group({
    login: ['', [Validators.required]],
    senha: ['', [Validators.required, Validators.minLength(4)]],
  });


  ngOnInit(): void {
    this.renderer.addClass(document.body, 'login-page-active');
  }

  ngOnDestroy(): void {
    this.renderer.removeClass(document.body, 'login-page-active');
  }
  signIn() {
    this.authError = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.authApi
      .login(this.form.getRawValue())
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: response => {
          this.authState.setAuth(response.accessToken, response.refreshToken, {
            usuario: response.username,
            nome: response.nome,
            perfis: [],
            permissoes: [],
          });
          this.authSession.signIn();

          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/home';
          this.router.navigateByUrl(returnUrl);
        },
        error: (error: HttpErrorResponse) => {
          this.authError =
            error.status === 401
              ? 'Usuário ou senha inválidos.'
              : 'Não foi possível autenticar agora. Tente novamente.';
        },
      });
  }
}
