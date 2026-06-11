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
