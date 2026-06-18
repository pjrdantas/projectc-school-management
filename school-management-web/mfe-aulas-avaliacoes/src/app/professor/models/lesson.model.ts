export interface Lesson {
  id: string;
  professorTurmaDisciplinaId: string;
  professorId: string;
  professorNome: string;
  turmaId: string;
  turmaNome: string;
  disciplinaId: string;
  disciplinaNome: string;
  dataAula: string;
  horarioInicio?: string | null;
  horarioFim?: string | null;
  conteudoMinistrado?: string | null;
  observacao?: string | null;
  realizada: boolean;
  createdAt?: string | null;
}

export interface LessonInput {
  professorTurmaDisciplinaId: string;
  dataAula: string;
  horarioInicio?: string | null;
  horarioFim?: string | null;
  conteudoMinistrado?: string | null;
  observacao?: string | null;
  realizada?: boolean | null;
}
