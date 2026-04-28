export interface Responsible {
  id: string;
  nomeCompleto: string;
  cpf: string;
  email?: string;
  telefone?: string;
  createdAt: string;
}

export interface ResponsibleInput {
  nomeCompleto: string;
  cpf: string;
  email?: string;
  telefone?: string;
}
