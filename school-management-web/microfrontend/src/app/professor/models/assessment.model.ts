export interface Assessment {
  id: string;
  professorTurmaDisciplinaId: string;
  professorId: string;
  professorNome: string;
  turmaId: string;
  turmaNome: string;
  disciplinaId: string;
  disciplinaNome: string;
  titulo: string;
  descricao?: string | null;
  dataAplicacao?: string | null;
  valorMaximo: number;
  peso: number;
  tipoAvaliacao: string;
  createdAt?: string | null;
}

export interface AssessmentInput {
  professorTurmaDisciplinaId: string;
  titulo: string;
  descricao?: string | null;
  dataAplicacao?: string | null;
  valorMaximo: number;
  peso: number;
  tipoAvaliacao: string;
}

export interface StudentGrade {
  id: string;
  avaliacaoId: string;
  avaliacaoTitulo: string;
  matriculaId: string;
  alunoId: string;
  alunoNome: string;
  nota: number;
  observacao?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface StudentGradeInput {
  matriculaId: string;
  nota: number;
  observacao?: string | null;
}
