export interface Disciplina {
  id: string;
  nome: string;
  cargaHoraria?: number;
  status?: string;
  createdAt?: string;
}

export interface DisciplinaInput {
  nome: string;
  cargaHoraria?: number;
  status?: string;
}

export interface HistoricoEscolarItemInput {
  componenteCurricular: string;
  anoLetivo?: number;
  serie?: string;
  ciclo?: string;
  notaConceito?: string;
  totalAulas?: number;
  cargaHoraria?: number;
}

export interface HistoricoEscolarInput {
  alunoId: string;
  nomeAluno: string;
  rgRen?: string;
  ra?: string;
  rm?: string;
  dataNascimento?: string;
  municipioNascimento?: string;
  estadoNascimento?: string;
  paisNascimento?: string;
  nomeEscola?: string;
  enderecoEscola?: string;
  municipioEscola?: string;
  cepEscola?: string;
  telefoneEscola?: string;
  emailEscola?: string;
  anoConclusao?: number;
  ensinoConcluido?: string;
  dataEmissao?: string;
  diretorNome?: string;
  diretorRg?: string;
  gerenteOrganizacaoNome?: string;
  gerenteOrganizacaoRg?: string;
  doeNumero?: string;
  doeData?: string;
  doeVolume?: string;
  doePagina?: string;
  observacoes?: string;
  componentesCurriculares: HistoricoEscolarItemInput[];
}

export interface HistoricoEscolarItem {
  id: string;
  componenteCurricular: string;
  anoLetivo?: number;
  serie?: string;
  ciclo?: string;
  notaConceito?: string;
  totalAulas?: number;
  cargaHoraria?: number;
}

export interface HistoricoEscolar {
  id: string;
  alunoId: string;
  nomeAluno: string;
  rgRen?: string;
  ra?: string;
  rm?: string;
  dataNascimento?: string;
  municipioNascimento?: string;
  estadoNascimento?: string;
  paisNascimento?: string;
  nomeEscola?: string;
  enderecoEscola?: string;
  municipioEscola?: string;
  cepEscola?: string;
  telefoneEscola?: string;
  emailEscola?: string;
  anoConclusao?: number;
  ensinoConcluido?: string;
  dataEmissao?: string;
  diretorNome?: string;
  diretorRg?: string;
  gerenteOrganizacaoNome?: string;
  gerenteOrganizacaoRg?: string;
  doeNumero?: string;
  doeData?: string;
  doeVolume?: string;
  doePagina?: string;
  observacoes?: string;
  componentesCurriculares?: HistoricoEscolarItem[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface EscolaOrigemInput {
  nomeEscola: string;
  codigoInep?: string;
  cnpj?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  uf?: string;
}

export interface EscolaOrigem extends EscolaOrigemInput {
  id: string;
  createdAt?: string;
}

export interface TransferenciaAlunoInput {
  alunoId: string;
  escolaOrigemId?: string;
  escolaOrigem?: EscolaOrigemInput;
  serieOrigem: string;
  anoLetivoOrigem: string;
  dataTransferencia?: string;
  motivoTransferencia?: string;
  situacaoOrigem?: string;
  documentosEntregues?: string;
  tipoTransferencia?: string;
  statusTransferencia?: string;
  usuarioOperacao?: string;
  observacao?: string;
}

export interface TransferenciaAluno {
  id: string;
  alunoId: string;
  escolaOrigem: EscolaOrigem;
  serieOrigem: string;
  anoLetivoOrigem: string;
  dataTransferencia?: string;
  motivoTransferencia?: string;
  situacaoOrigem?: string;
  documentosEntregues?: string;
  tipoTransferencia?: string;
  statusTransferencia?: string;
  usuarioOperacao?: string;
  dataHoraOperacao?: string;
  observacao?: string;
  createdAt?: string;
}

export interface CepEndereco {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  uf: string;
  complemento?: string;
}
