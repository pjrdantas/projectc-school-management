export interface AuthRequest {
  nome: string;
  login: string;
  senha: string;
  ativo: 'S' | 'N';
  perfisIds?: number[];
  rolesIds?: number[];
  permissionsIds?: number[];
}
