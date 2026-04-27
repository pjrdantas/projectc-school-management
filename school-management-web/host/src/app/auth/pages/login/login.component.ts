import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { AuthSessionService } from '../../services/auth-session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [MatButtonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authSession = inject(AuthSessionService);
  private readonly authState = inject(AuthStateService);

  signIn() {
    this.authState.setAuth('token-demo', 'refresh-token-demo', {
      usuario: 'admin',
      nome: 'Administrador',
      perfis: ['ADMIN'],
      permissoes: ['USUARIO'],
    });
    this.authSession.signIn();

    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/home';
    this.router.navigateByUrl(returnUrl);
  }
}
