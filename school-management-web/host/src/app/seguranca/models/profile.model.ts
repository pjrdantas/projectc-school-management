export interface Profile { id: string; codigo: string; nome: string; descricao?: string; createdAt: string; permissaoIds?: string[]; permissoes?: string[]; }
export interface ProfileInput { codigo: string; nome: string; descricao?: string; permissaoIds?: string[]; permissoes?: string[]; }
