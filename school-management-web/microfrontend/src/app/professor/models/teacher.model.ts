export interface Professor {
  id: string;
  pessoaId: string;
  nomeCompleto: string;
  registroProfissional?: string | null;
  formacao?: string | null;
  ativo: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface ProfessorInput {
  funcionarioId: string;
  registroProfissional?: string | null;
  formacao?: string | null;
  ativo?: boolean | null;
}

export interface ProfessorFuncionarioElegivel {
  funcionarioId: string;
  nomeCompleto: string;
  cargo?: string | null;
  ativo: boolean;
}
