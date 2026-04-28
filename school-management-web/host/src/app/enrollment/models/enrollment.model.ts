export interface EnrollmentInput {
  alunoId: string;
  turmaId: string;
  periodoLetivoId: string;
}

export interface Enrollment {
  id: string;
  alunoId: string;
  turmaId: string;
  periodoLetivoId: string;
  status: string;
  createdAt: string;
}

export interface EnrollmentFilter {
  alunoId?: string;
  turmaId?: string;
  periodoLetivoId?: string;
  status?: string;
}

export interface ApiErrorResponse {
  message?: string;
}
