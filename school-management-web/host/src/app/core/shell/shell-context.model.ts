export const SHELL_CONTEXT_VERSION = 1;
export const SHELL_CONTEXT_STORAGE_KEY = 'school-management.shell.context.v1';
export const SHELL_CONTEXT_EVENT = 'school-management:shell-context-changed';

export interface ShellUsuario {
  usuarioId?: string | null;
  professorId?: string | null;
  usuario: string;
  nome: string;
  perfis: string[];
  permissoes: string[];
}

export interface ShellContext {
  version: typeof SHELL_CONTEXT_VERSION;
  apiBaseUrl: string;
  accessToken: string | null;
  usuario: ShellUsuario | null;
}
