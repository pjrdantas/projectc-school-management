export type DashboardPublicoCodigo = 'ACADEMICO' | 'SECRETARIA' | 'DIRETOR' | 'PROFESSOR';

export interface DashboardMatriculaStatus {
  status: string;
  total: number;
}

export interface DashboardTurmaVaga {
  turmaId: string;
  turmaNome: string;
  capacidade: number;
  vagasOcupadas: number;
  vagasDisponiveis: number;
}

export interface DashboardResumo {
  totalMatriculas?: number;
  matriculasAguardandoDocumentos?: number;
  matriculasConcluidas?: number;
  matriculasEfetivadas?: number;
  matriculasAptasRematricula?: number;
  boletinsFechados?: number;
  historicosInternosGerados?: number;
  alunosAprovados?: number;
  alunosReprovados?: number;
  matriculasSolicitadas?: number;
  matriculasEmAndamento?: number;
  matriculasAguardandoHistoricoEscolar?: number;
  matriculasComDocumentosPendentes?: number;
  transferencias?: number;
  solicitacoesExclusaoPendentes?: number;
  matriculasPendentes?: number;
  alunosAtivos?: number;
  alunosInativos?: number;
  turmasAtivas?: number;
  turmasLotadas?: number;
  professoresAlocados?: number;
  aulasRealizadas?: number;
  avaliacoesRegistradas?: number;
  avaliacoesComNotasPendentes?: number;
  turmasVinculadas?: number;
  alocacoesAtivas?: number;
  aulasPlanejadas?: number;
  frequenciasPendentes?: number;
  planejamentosBimestrais?: number;
  planejamentosBimestraisPendentes?: number;
  matriculasPorStatus?: DashboardMatriculaStatus[];
  turmasComVagas?: DashboardTurmaVaga[];
}

export interface DashboardAlerta {
  publicoCodigo: DashboardPublicoCodigo;
  professorId?: string | null;
  codigo: string;
  severidade: string;
  titulo: string;
  mensagem: string;
  valor: number;
  limite: number;
}

export interface DashboardHistoricoPonto {
  referenciaData: string;
  valorNumeric: number;
  valorTexto?: string | null;
}

export interface DashboardHistorico {
  publicoCodigo: DashboardPublicoCodigo;
  codigoIndicador: string;
  descricao: string;
  valorAtual: number;
  valorAnterior?: number | null;
  variacaoPercentual?: number | null;
  pontos: DashboardHistoricoPonto[];
}

export interface DashboardFrontendResponse {
  publicoCodigo: DashboardPublicoCodigo;
  usuarioId?: string | null;
  professorId?: string | null;
  resumo: DashboardResumo;
  alertas: DashboardAlerta[];
  historico: DashboardHistorico[];
}
