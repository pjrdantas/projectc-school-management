export interface AplicativosRequest {
  nome: string;
  descricao: string;
  url: string;
  moduleName: string;
  exposedModule: string;
  routePath: string;
  ativo: 'S' | 'N';
}
