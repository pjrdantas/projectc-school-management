import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly authenticated = signal<boolean>(this.hasStoredSession());

  readonly isAuthenticated = this.authenticated.asReadonly();

  signIn() {
    this.authenticated.set(true);
  }

  signOut() {
    this.authenticated.set(false);
  }

  restoreFromStorage() {
    this.authenticated.set(this.hasStoredSession());
  }

  private hasStoredSession(): boolean {
    return !!localStorage.getItem('token');
  }
}
