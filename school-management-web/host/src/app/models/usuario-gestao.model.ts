import { PerfilResumo } from './perfil-resumo.model';

export interface UsuarioResponse {
  id: number;
  login: string;
  nome: string;
  ativo: 'S' | 'N';
  email: string;
  criadoEm: string;
  atualizadoEm?: string;
  // Use o PerfilResumo aqui para manter a consistência
  perfisIds: PerfilResumo[];
}

export interface UsuarioRequest {
  login: string;
  senha?: string;
  nome: string;
  ativo: 'S' | 'N';
  email: string;
  perfisIds: number[]; // No Request são apenas os IDs (números)
}
