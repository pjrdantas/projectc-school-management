export interface BimonthlyPlanning {
  id: string;
  professorTurmaDisciplinaId: string;
  professorId: string;
  professorNome: string;
  turmaId: string;
  turmaNome: string;
  disciplinaId: string;
  disciplinaNome: string;
  periodoAvaliativoId?: string | null;
  periodoAvaliativoNome?: string | null;
  status?: string | null;
  statusDescricao?: string | null;
  titulo: string;
  temaPrincipal: string;
  descricaoInicial: string;
  objetivoGeral?: string | null;
  observacaoProfessor?: string | null;
  conteudoFinalAprovado?: string | null;
  reutilizavel: boolean;
  criadoComAuxilioIA: boolean;
  aprovadoPeloProfessor: boolean;
  dataAprovacao?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  aulasPrevistas: BimonthlyPlanningLesson[];
  avaliacoesPrevistas: BimonthlyPlanningAssessment[];
}

export interface BimonthlyPlanningInput {
  professorTurmaDisciplinaId: string;
  periodoAvaliativoId?: string | null;
  titulo: string;
  temaPrincipal: string;
  descricaoInicial: string;
  objetivoGeral?: string | null;
  observacaoProfessor?: string | null;
  conteudoFinalAprovado?: string | null;
  reutilizavel?: boolean | null;
  criadoComAuxilioIA?: boolean | null;
}

export interface BimonthlyPlanningLesson {
  id: string;
  planejamentoBimestralId: string;
  numeroAula: number;
  temaAula: string;
  objetivoAula?: string | null;
  conteudoPrevisto?: string | null;
  metodologia?: string | null;
  recursos?: string | null;
  atividadePrevista?: string | null;
  observacao?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface BimonthlyPlanningLessonInput {
  numeroAula: number;
  temaAula: string;
  objetivoAula?: string | null;
  conteudoPrevisto?: string | null;
  metodologia?: string | null;
  recursos?: string | null;
  atividadePrevista?: string | null;
  observacao?: string | null;
}

export interface BimonthlyPlanningAssessment {
  id: string;
  planejamentoBimestralId: string;
  titulo: string;
  descricao?: string | null;
  dataPrevista?: string | null;
  peso: number;
  valorMaximo?: number | null;
  tipoAvaliacao: string;
  conteudoCobrado?: string | null;
  orientacaoAplicacao?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface BimonthlyPlanningAssessmentInput {
  titulo: string;
  descricao?: string | null;
  dataPrevista?: string | null;
  peso: number;
  valorMaximo?: number | null;
  tipoAvaliacao: string;
  conteudoCobrado?: string | null;
  orientacaoAplicacao?: string | null;
}

export interface BimonthlyPlanningFilters {
  professorId?: string;
  turmaId?: string;
  disciplinaId?: string;
  periodoAvaliativoId?: string;
}

export interface PlanningAiGenerateInput {
  promptProfessor: string;
  tipoConteudo: string;
  titulo?: string | null;
  reutilizavel?: boolean | null;
}

export interface PlanningAiVersionInput {
  conteudo: string;
  motivoAlteracao?: string | null;
}

export interface PlanningAiApprovalInput {
  numeroVersao: number;
  publicarBiblioteca?: boolean | null;
}

export interface PlanningAiInteraction {
  id: string;
  planejamentoBimestralId: string;
  promptProfessor: string;
  respostaIA: string;
  modeloIA?: string | null;
  tokensEntrada?: number | null;
  tokensSaida?: number | null;
  custoEstimado?: number | null;
  createdAt?: string | null;
}

export interface PlanningAiContent {
  id: string;
  planejamentoBimestralId: string;
  interacaoId?: string | null;
  titulo: string;
  conteudo: string;
  versao: number;
  hashConteudo?: string | null;
  aprovadoPeloProfessor: boolean;
  reutilizavel: boolean;
  ativo: boolean;
  status?: string | null;
  statusDescricao?: string | null;
  tipoConteudo: string;
  tipoConteudoDescricao?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PlanningAiContentVersion {
  id: string;
  conteudoGeradoId: string;
  numeroVersao: number;
  conteudo: string;
  motivoAlteracao?: string | null;
  createdAt?: string | null;
}

export interface PedagogicalContentLibraryItem {
  id: string;
  professorId?: string | null;
  professorNome?: string | null;
  disciplinaId?: string | null;
  disciplinaNome?: string | null;
  tipoConteudo?: string | null;
  tipoConteudoDescricao?: string | null;
  titulo: string;
  tema?: string | null;
  conteudo: string;
  origem?: string | null;
  reutilizavel: boolean;
  ativo: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PedagogicalContentLibraryFilters {
  professorId?: string;
  disciplinaId?: string;
  tipoConteudo?: string;
  tema?: string;
}
