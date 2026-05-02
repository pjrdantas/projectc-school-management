export interface User { id: string; username: string; nome: string; email: string; ativo: boolean; createdAt: string; perfilIds?: string[]; perfis?: string[]; }
export interface UserInput { username: string; nome: string; email: string; senhaHash: string; ativo: boolean; perfilIds?: string[]; perfis?: string[]; }
