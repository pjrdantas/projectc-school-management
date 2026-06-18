export interface DashboardPublico {
  id: string;
  codigo: string;
  descricao: string;
}

export interface DashboardPublicoInput {
  codigo: string;
  descricao: string;
}

export interface DashboardConfiguracao {
  id: string;
  publicoDashboardId: string;
  publicoCodigo: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface DashboardConfiguracaoInput {
  publicoDashboardId: string;
  codigo: string;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

export interface DashboardWidget {
  id: string;
  dashboardId: string;
  dashboardCodigo: string;
  codigo: string;
  titulo: string;
  descricao?: string | null;
  tipoWidget: string;
  ordem: number;
  queryReferencia?: string | null;
  ativo: boolean;
}

export interface DashboardWidgetInput {
  dashboardId: string;
  codigo: string;
  titulo: string;
  descricao?: string | null;
  tipoWidget: string;
  ordem: number;
  queryReferencia?: string | null;
  ativo: boolean;
}
