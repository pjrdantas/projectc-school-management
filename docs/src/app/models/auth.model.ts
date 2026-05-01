export interface Auth {
  id?: number;
  nome: string;
  login: string;
  ativo: 'S' | 'N';
  senha?: string;
  perfisIds?: number[];
  rolesIds?: number[];
  permissionsIds?: number[];
}
