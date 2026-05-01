import { Permissao } from './permissao.model';

export interface PerfilResponse {
  id: number;
  nmPerfil: string;
  permissoes: Permissao[];
  criadoEm?: string | Date;
  atualizadoEm?: string | Date;
}

