export interface AcademicPeriod {
  id: string;
  nome: string;
  ano: number;
  dataInicio: string;
  dataFim: string;
  ativo: boolean;
  createdAt: string;
}

export interface AcademicPeriodInput {
  nome: string;
  ano?: number;
  dataInicio: string;
  dataFim: string;
}

export interface AcademicSeries {
  id: string;
  nome: string;
  ordem: number;
  nivelEnsino: string;
  createdAt: string;
}

export interface AcademicSeriesInput {
  nome: string;
  ordem: number;
  nivelEnsino?: string;
}

export interface AcademicShift {
  id: string;
  codigo: string;
  descricao: string;
}

export interface AcademicShiftInput {
  codigo: string;
  descricao: string;
}

export interface AcademicClass {
  id: string;
  codigo: string;
  nome: string;
  capacidade: number;
  periodoLetivoId: string;
  serieId: string;
  serieNome: string;
  turno: string;
  status: string;
  createdAt: string;
}

export interface AcademicClassInput {
  codigo: string;
  nome: string;
  capacidade: number;
  periodoLetivoId: string;
  serieId: string;
  turno?: string;
  status?: string;
}

export interface AcademicClassDiscipline {
  id: string;
  turmaId: string;
  disciplinaId: string;
  disciplinaNome: string;
  cargaHoraria: number;
  createdAt: string;
}
