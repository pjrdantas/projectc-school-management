import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly authenticated = signal<boolean>(!!localStorage.getItem('token'));

  readonly isAuthenticated = this.authenticated.asReadonly();

  signIn() {
    this.authenticated.set(true);
  }

  signOut() {
    this.authenticated.set(false);
  }
}
