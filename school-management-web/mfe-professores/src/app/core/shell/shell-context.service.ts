import { Injectable, OnDestroy, signal } from '@angular/core';
import {
  SHELL_CONTEXT_EVENT,
  SHELL_CONTEXT_STORAGE_KEY,
  SHELL_CONTEXT_VERSION,
  ShellContext,
} from './shell-context.model';

@Injectable({ providedIn: 'root' })
export class ShellContextService implements OnDestroy {
  private readonly contextSignal = signal<ShellContext | null>(this.readContextFromStorage());
  private readonly onContextChanged = (event: Event) => {
    const customEvent = event as CustomEvent<ShellContext | null>;
    this.contextSignal.set(this.normalizeContext(customEvent.detail));
  };

  readonly context = this.contextSignal.asReadonly();

  constructor() {
    window.addEventListener(SHELL_CONTEXT_EVENT, this.onContextChanged);
  }

  getApiBaseUrl(): string {
    return this.contextSignal()?.apiBaseUrl ?? 'http://localhost:8080';
  }

  getToken(): string | null {
    return this.contextSignal()?.accessToken ?? null;
  }

  getUsuario() {
    return this.contextSignal()?.usuario ?? null;
  }

  getPermissions(): string[] {
    return this.getUsuario()?.permissoes ?? [];
  }

  hasPermission(permission: string): boolean {
    const permissions = this.getPermissions();
    return permissions.includes(permission) || permissions.includes('ADMIN');
  }

  ngOnDestroy(): void {
    window.removeEventListener(SHELL_CONTEXT_EVENT, this.onContextChanged);
  }

  private readContextFromStorage(): ShellContext | null {
    const raw = localStorage.getItem(SHELL_CONTEXT_STORAGE_KEY);
    if (!raw) return null;

    try {
      return this.normalizeContext(JSON.parse(raw));
    } catch {
      return null;
    }
  }

  private normalizeContext(context: ShellContext | null): ShellContext | null {
    if (!context || context.version !== SHELL_CONTEXT_VERSION) return null;

    return {
      version: SHELL_CONTEXT_VERSION,
      apiBaseUrl: context.apiBaseUrl,
      accessToken: context.accessToken ?? null,
      usuario: context.usuario
        ? {
            usuarioId: context.usuario.usuarioId ?? null,
            professorId: context.usuario.professorId ?? null,
            usuario: context.usuario.usuario,
            nome: context.usuario.nome,
            perfis: context.usuario.perfis ?? [],
            permissoes: context.usuario.permissoes ?? [],
          }
        : null,
    };
  }
}

