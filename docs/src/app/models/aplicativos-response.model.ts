export interface AplicativosResponse {
  id: number;
  nome: string;
  descricao: string;
  url: string;
  moduleName: string;
  exposedModule: string;
  routePath: string;
  ativo: 'S' | 'N';
  criadoEm: string;
  atualizadoEm: string | null;
}
