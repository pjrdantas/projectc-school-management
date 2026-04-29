import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, Renderer2, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { AuthApiService } from '../../services/auth-api.service';
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

  async signIn(): Promise<void> {
    if (this.loading) {
      return;
    }

    this.authError = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const response = await firstValueFrom(this.authApi.login(this.form.getRawValue()));

      this.authState.setAuth(response.accessToken, response.refreshToken, {
        usuario: response.username,
        nome: response.nome,
        perfis: [],
        permissoes: [],
      });
      this.authSession.signIn();

      const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/home';
      await this.router.navigateByUrl(returnUrl);
    } catch (error) {
      const httpError = error as HttpErrorResponse;
      this.authError =
        httpError.status === 401
          ? 'Usuário ou senha inválidos.'
          : 'Não foi possível autenticar agora. Tente novamente.';
    } finally {
      this.loading = false;
    }
  }
}
