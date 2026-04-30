export interface User { id: string; username: string; nome: string; email: string; ativo: boolean; createdAt: string; }
export interface UserInput { username: string; nome: string; email: string; senhaHash: string; ativo: boolean; }
