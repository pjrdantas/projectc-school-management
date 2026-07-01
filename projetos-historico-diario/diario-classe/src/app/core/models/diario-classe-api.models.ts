export type FrequenciaStatus = 'P' | '.' | 'F';

export interface DiarioClasseLoadRequest {
  idProfessor: string;
  idTurma: string;
  idDisciplina: string;
  anoLetivo: number;
  mes: number;
  dataReferencia: string;
}

export interface DiarioClasseResponse {
  cabecalho: DiarioClasseCabecalhoResponse;
  alunos: DiarioClasseAlunoResponse[];
  conteudosPlanejados: ConteudoPlanejadoResponse[];
  observacoes: string[];
  avaliacoes: AvaliacaoDiarioResponse[];
  assinatura?: AssinaturaDiarioResponse;
  bloqueado?: boolean;
}

export interface DiarioClasseCabecalhoResponse {
  idDiarioClasse: string;
  idEscola: string;
  escola: string;
  diretoriaEnsino: string;
  municipio: string;
  anoLetivo: number;
  mes: number;
  dataAtual: string;
  idTurma: string;
  turmaSerie: string;
  turno: string;
  idDisciplina: string;
  disciplina: string;
  idProfessor: string;
  professor: string;
}

export interface DiarioClasseAlunoResponse {
  idAluno: string;
  numeroChamada: number;
  nome: string;
  frequencias: Record<number, FrequenciaStatus | ''>;
}

export interface ConteudoPlanejadoResponse {
  idPlanejamentoAula: string;
  periodo: string;
  descricao: string;
}

export interface AvaliacaoDiarioResponse {
  idAvaliacao: string;
  data: string;
  descricao: string;
  turma: string;
  valor: string;
}

export interface AssinaturaDiarioResponse {
  nomeProfessor: string;
  dataAssinatura: string;
}

export interface DiarioClasseSaveRequest {
  idDiarioClasse: string;
  dataLancamento: string;
  frequencias: FrequenciaAlunoSaveRequest[];
  conteudos: ConteudoMinistradoSaveRequest[];
  observacoes: string[];
  assinatura: AssinaturaDiarioSaveRequest;
}

export interface FrequenciaAlunoSaveRequest {
  idAluno: string;
  data: string;
  dia: number;
  status: FrequenciaStatus;
}

export interface ConteudoMinistradoSaveRequest {
  idPlanejamentoAula: string;
  periodo: string;
  descricao: string;
  alterado: boolean;
  observacaoJustificativa?: string;
}

export interface AssinaturaDiarioSaveRequest {
  nomeProfessor: string;
  dataAssinatura: string;
}

export interface DiarioClasseSaveResponse {
  idDiarioClasse: string;
  status: 'SALVO' | 'PENDENTE_VALIDACAO';
  mensagem: string;
  salvoEm: string;
  bloqueado?: boolean;
}
