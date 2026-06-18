export interface TeacherFrequency {
  id: string;
  aulaId: string;
  professorId: string;
  professorNome: string;
  presente: boolean;
  justificativa?: string;
  createdAt: string;
}

export interface TeacherFrequencyInput {
  presente: boolean;
  justificativa?: string;
}

export interface StudentFrequency {
  id: string;
  aulaId: string;
  matriculaId: string;
  alunoId: string;
  alunoNome: string;
  situacao: string;
  justificativa?: string;
  createdAt: string;
}

export interface StudentFrequencyInput {
  matriculaId: string;
  situacao: string;
  justificativa?: string;
}
