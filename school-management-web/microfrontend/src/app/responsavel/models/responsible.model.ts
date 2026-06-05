export interface Responsible {
  id: string;
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  createdAt: string;
}

export interface ResponsibleInput {
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
}
