import { Injectable, OnDestroy, inject } from '@angular/core';
import { combineLatest, Subscription } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { AuthStateService } from '../auth/auth-state.service';
import {
  SHELL_CONTEXT_EVENT,
  SHELL_CONTEXT_STORAGE_KEY,
  SHELL_CONTEXT_VERSION,
  ShellContext,
} from './shell-context.model';

@Injectable({ providedIn: 'root' })
export class ShellContextPublisherService implements OnDestroy {
  private readonly authState = inject(AuthStateService);
  private subscription?: Subscription;

  start(): void {
    if (this.subscription) return;

    this.publishCurrentContext();
    this.subscription = combineLatest([
      this.authState.token$,
      this.authState.usuario$,
    ]).subscribe(() => this.publishCurrentContext());
  }

  publishCurrentContext(): void {
    const context: ShellContext = {
      version: SHELL_CONTEXT_VERSION,
      apiBaseUrl: API_BASE_URL,
      accessToken: this.authState.getToken(),
      usuario: this.authState.getUsuario(),
    };

    localStorage.setItem(SHELL_CONTEXT_STORAGE_KEY, JSON.stringify(context));
    window.dispatchEvent(new CustomEvent<ShellContext>(SHELL_CONTEXT_EVENT, { detail: context }));
  }

  clearContext(): void {
    localStorage.removeItem(SHELL_CONTEXT_STORAGE_KEY);
    window.dispatchEvent(new CustomEvent<ShellContext | null>(SHELL_CONTEXT_EVENT, { detail: null }));
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}

