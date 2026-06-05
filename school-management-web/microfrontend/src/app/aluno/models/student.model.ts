export interface Student {
  id: string;
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  orgaoEmissorRg?: string;
  ufRg?: string;
  email: string;
  dataNascimento: string;
  telefone?: string;
  nacionalidade?: string;
  naturalidade?: string;
  sexo?: string;
  nomeSocial?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  statusAluno?: string;
  createdAt: string;
}

export interface StudentInput {
  nomeCompleto: string;
  cpf: string;
  rg?: string;
  orgaoEmissorRg?: string;
  ufRg?: string;
  email: string;
  dataNascimento: string;
  telefone?: string;
  nacionalidade?: string;
  naturalidade?: string;
  sexo?: string;
  nomeSocial?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
  statusAluno?: string;
}
