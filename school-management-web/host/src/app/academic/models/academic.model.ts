export interface AcademicPeriod {
  id: string;
  nome: string;
  dataInicio: string;
  dataFim: string;
  createdAt: string;
}

export interface AcademicPeriodInput {
  nome: string;
  dataInicio: string;
  dataFim: string;
}

export interface AcademicClass {
  id: string;
  codigo: string;
  nome: string;
  capacidade: number;
  periodoLetivoId: string;
  createdAt: string;
}

export interface AcademicClassInput {
  codigo: string;
  nome: string;
  capacidade: number;
  periodoLetivoId: string;
}

export interface ApiErrorResponse {
  message?: string;
}
