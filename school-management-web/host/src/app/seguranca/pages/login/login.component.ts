import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, Renderer2, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { AuthApiService } from '../../services/auth-api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authState = inject(AuthStateService);
  private readonly authApi = inject(AuthApiService);
  private readonly fb = inject(FormBuilder);
  private readonly renderer = inject(Renderer2);

  loading = false;
  authError: string | null = null;
  mostrarSenha = false;

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
        usuarioId: response.usuarioId ?? null,
        professorId: response.professorId ?? null,
        usuario: response.username ?? response.login ?? '',
        nome: response.nome,
        perfis: response.perfis ?? [],
        permissoes: response.permissoes ?? [],
      });

      const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
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
