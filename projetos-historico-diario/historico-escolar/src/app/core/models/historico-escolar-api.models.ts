export type HistoricoModoTela = 'CADASTRO' | 'EDICAO';
export type HistoricoStatus = 'RASCUNHO' | 'PENDENTE' | 'COMPLETO';

export interface HistoricoEscolarLoadRequest {
  idAluno?: string;
  idMatricula?: string;
  idHistoricoEscolar?: string;
  modo: HistoricoModoTela;
}

export interface HistoricoEscolarResponse {
  contexto: HistoricoEscolarContextoResponse;
  cabecalho: HistoricoCabecalhoResponse;
  aluno: HistoricoAlunoResponse;
  periodos: PeriodoLetivoResponse[];
  baseComum: CurriculoLinhaResponse[];
  parteDiversificada: CurriculoLinhaResponse[];
  totais: HistoricoTotaisResponse;
  estudosRealizados: EstudoRealizadoResponse[];
  observacoes: string;
  certificado: CertificadoHistoricoResponse;
  pendencias?: HistoricoPendenciaResponse[];
}

export interface HistoricoEscolarContextoResponse {
  idHistoricoEscolar?: string;
  idAluno?: string;
  idMatricula?: string;
  modo: HistoricoModoTela;
  status: HistoricoStatus;
  serieMatriculaAtual: number;
  serieConcluidaOrigem: number;
  escolaOrigem?: string;
  dataTransferencia?: string;
  bloqueado?: boolean;
}

export interface HistoricoCabecalhoResponse {
  governo: string;
  secretaria: string;
  diretoria: string;
  escola: string;
  atoLegal: string;
  atoLegalCriacao: string;
  endereco: string;
  numero: string;
  bairro: string;
  municipio: string;
  cep: string;
  telefone: string;
  email: string;
}

export interface HistoricoAlunoResponse {
  nome: string;
  rg: string;
  ra: string;
  nascimentoMunicipio: string;
  nascimentoEstado: string;
  nascimentoPais: string;
  nascimentoData: string;
}

export interface PeriodoLetivoResponse {
  ordem: number;
  anoLetivo: string;
  serie: string;
  equivalencia: string;
}

export interface CurriculoLinhaResponse {
  ordem: number;
  nome: string;
  valores: string[];
}

export interface HistoricoTotaisResponse {
  totalBaseComum: string[];
  totalParteDiversificada: string[];
  totalAulasAnuais: string[];
  totalCargaHoraria: string[];
}

export interface EstudoRealizadoResponse {
  ordem: number;
  serieAno: string;
  ano: string;
  escola: string;
  municipio: string;
  uf: string;
}

export interface CertificadoHistoricoResponse {
  serieConcluida: number | null;
  diretor: string;
  escola: string;
  rgAluno: string;
  ano: string;
  publicacao: string;
  data: string;
  gerenteNome: string;
  gerenteRg: string;
  diretorNome: string;
  diretorRg: string;
}

export interface HistoricoPendenciaResponse {
  codigo: string;
  severidade: 'AVISO' | 'ERRO';
  mensagem: string;
  aba?: 'CADASTRO' | 'ANOS_COMPONENTES' | 'ESTUDOS' | 'CERTIFICADO';
}

export interface HistoricoEscolarPdfImportResponse {
  historico: HistoricoEscolarResponse;
  nomeArquivo: string;
  confiancaGeral: number;
  avisos: string[];
  importadoEm: string;
}

export interface HistoricoEscolarSaveRequest {
  idHistoricoEscolar?: string;
  idAluno?: string;
  idMatricula?: string;
  modo: HistoricoModoTela;
  statusPretendido: HistoricoStatus;
  cabecalho: HistoricoCabecalhoResponse;
  aluno: HistoricoAlunoResponse;
  contexto: Pick<HistoricoEscolarContextoResponse, 'serieMatriculaAtual' | 'serieConcluidaOrigem' | 'escolaOrigem' | 'dataTransferencia'>;
  periodos: PeriodoLetivoResponse[];
  baseComum: CurriculoLinhaResponse[];
  parteDiversificada: CurriculoLinhaResponse[];
  totais: HistoricoTotaisResponse;
  estudosRealizados: EstudoRealizadoResponse[];
  observacoes: string;
  certificado: CertificadoHistoricoResponse;
}

export interface HistoricoEscolarSaveResponse {
  idHistoricoEscolar: string;
  status: HistoricoStatus;
  mensagem: string;
  salvoEm: string;
  pendencias: HistoricoPendenciaResponse[];
}
