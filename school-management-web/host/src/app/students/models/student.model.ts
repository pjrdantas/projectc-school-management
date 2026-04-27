export interface Student {
  id: string;
  nomeCompleto: string;
  cpf: string;
  email: string;
  dataNascimento: string;
  telefone?: string;
  createdAt: string;
}

export interface StudentInput {
  nomeCompleto: string;
  cpf: string;
  email: string;
  dataNascimento: string;
  telefone?: string;
}
