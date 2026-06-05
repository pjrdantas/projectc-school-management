export type DocumentEntityType =
  | 'ALUNO'
  | 'RESPONSAVEL'
  | 'FUNCIONARIO'
  | 'PROFESSOR'
  | 'SECRETARIO'
  | 'INSPETOR'
  | 'SERVENTE'
  | 'MATRICULA'
  | 'TRANSFERENCIA_ALUNO';

export interface DocumentRecord {
  id: string;
  entidadeTipo: DocumentEntityType;
  entidadeId: string;
  tipoDocumento: string;
  numeroDocumento: string;
  caminhoArquivo: string;
  dataUpload?: string;
  observacao?: string;
}

export interface DocumentInput {
  entidadeTipo: DocumentEntityType;
  entidadeId: string;
  tipoDocumento: string;
  numeroDocumento: string;
  caminhoArquivo?: string;
  observacao?: string;
}
