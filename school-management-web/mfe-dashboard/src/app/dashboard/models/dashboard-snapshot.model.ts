export interface DashboardIndicadorSnapshot {
  id: string;
  publicoDashboardId: string;
  publicoCodigo: string;
  codigoIndicador: string;
  descricao: string;
  valorNumeric?: number | string | null;
  valorTexto?: string | null;
  referenciaData: string;
}

export interface DashboardIndicadorHistoricoPonto {
  referenciaData: string;
  valorNumeric?: number | string | null;
  valorTexto?: string | null;
}

export interface DashboardIndicadorHistorico {
  publicoCodigo: string;
  codigoIndicador: string;
  descricao: string;
  valorAtual?: number | string | null;
  valorAnterior?: number | string | null;
  variacaoPercentual?: number | string | null;
  pontos: DashboardIndicadorHistoricoPonto[];
}
