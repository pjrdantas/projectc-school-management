export interface EnrollmentInput {
  alunoId: string;
  turmaId: string;
  periodoLetivoId: string;
  tipoMatricula?: string;
  observacao?: string;
}

export interface Enrollment {
  id: string;
  alunoId: string;
  turmaId: string;
  serieId?: string;
  serieNome?: string;
  periodoLetivoId: string;
  status: string;
  tipoMatricula: string;
  dataMatricula?: string;
  observacao?: string;
  createdAt: string;
  etapas?: EnrollmentStep[];
}

export interface EnrollmentStep {
  id?: string;
  descricao: string;
  ordem: number;
  status: string;
  dataInicio?: string;
  dataConclusao?: string;
  observacao?: string;
}

export interface EnrollmentFilter {
  alunoId?: string;
  turmaId?: string;
  periodoLetivoId?: string;
  status?: string;
}

export interface EnrollmentCatalogItem {
  id: string;
  codigo: string;
  descricao: string;
}
