export type ShellDomain =
  | 'dashboard'
  | 'alunos'
  | 'responsaveis'
  | 'matriculas'
  | 'catalogo-academico'
  | 'professores'
  | 'aulas-avaliacoes'
  | 'planejamento-ia'
  | 'documentos'
  | 'administracao';

export interface ShellDomainCatalogItem {
  domain: ShellDomain;
  label: string;
  futureRemoteName: string;
  currentPlacement: 'host' | 'microfrontend' | 'shared';
}

export interface ShellRouteExtractionPlan {
  candidate: boolean;
  targetRemoteName: string;
  routeRole: 'operational' | 'administrative';
  shellNavigation: 'landing' | 'business-menu' | 'access-menu' | 'contextual';
}

export interface ShellRemoteRoute {
  path: string;
  domain: ShellDomain;
  runtimeRemoteName: string;
  exposedModule: string;
  exportName: string;
  extractionPlan?: ShellRouteExtractionPlan;
}

export interface ShellMenuItem {
  label: string;
  icon: string;
  route: string;
  domain: ShellDomain;
  perfis?: string[];
}

export interface ShellMenuGroup {
  domain: ShellDomain;
  label: string;
  items: ShellMenuItem[];
}

export interface ShellDomainInventoryItem extends ShellDomainCatalogItem {
  remoteRoutes: ShellRemoteRoute[];
  menuItems: ShellMenuItem[];
}

export interface ShellExtractionCandidateManifestItem {
  domain: ShellDomain;
  label: string;
  currentPlacement: 'host' | 'microfrontend' | 'shared';
  targetRemoteName: string;
  runtimeRemoteNames: string[];
  expectedExposedModules: string[];
  routePaths: string[];
  operationalRoutePaths: string[];
  administrativeRoutePaths: string[];
  landingRoutes: string[];
  businessMenuRoutes: string[];
  accessMenuRoutes: string[];
  contextualRoutes: string[];
}

export const SHELL_DOMAIN_CATALOG: Record<ShellDomain, ShellDomainCatalogItem> = {
  dashboard: {
    domain: 'dashboard',
    label: 'Dashboard',
    futureRemoteName: 'mfe-dashboard',
    currentPlacement: 'microfrontend',
  },
  alunos: {
    domain: 'alunos',
    label: 'Alunos',
    futureRemoteName: 'mfe-alunos',
    currentPlacement: 'microfrontend',
  },
  responsaveis: {
    domain: 'responsaveis',
    label: 'Responsaveis',
    futureRemoteName: 'mfe-responsaveis',
    currentPlacement: 'microfrontend',
  },
  matriculas: {
    domain: 'matriculas',
    label: 'Matriculas',
    futureRemoteName: 'mfe-matriculas',
    currentPlacement: 'microfrontend',
  },
  'catalogo-academico': {
    domain: 'catalogo-academico',
    label: 'Catalogo academico',
    futureRemoteName: 'mfe-catalogo-academico',
    currentPlacement: 'microfrontend',
  },
  professores: {
    domain: 'professores',
    label: 'Professores',
    futureRemoteName: 'mfe-professores',
    currentPlacement: 'microfrontend',
  },
  'aulas-avaliacoes': {
    domain: 'aulas-avaliacoes',
    label: 'Aulas e avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    currentPlacement: 'microfrontend',
  },
  'planejamento-ia': {
    domain: 'planejamento-ia',
    label: 'Planejamento e IA',
    futureRemoteName: 'mfe-planejamento-ia',
    currentPlacement: 'microfrontend',
  },
  documentos: {
    domain: 'documentos',
    label: 'Documentos',
    futureRemoteName: 'mfe-documentos',
    currentPlacement: 'shared',
  },
  administracao: {
    domain: 'administracao',
    label: 'Administracao',
    futureRemoteName: 'mfe-admin',
    currentPlacement: 'host',
  },
};

export const SHELL_REMOTE_ROUTES: ShellRemoteRoute[] = [
  {
    path: 'dashboard',
    domain: 'dashboard',
    runtimeRemoteName: 'mfe-dashboard',
    exposedModule: './Dashboard',
    exportName: 'AcademicOperationalDashboardComponent',
  },
  {
    path: 'students',
    domain: 'alunos',
    runtimeRemoteName: 'mfe1',
    exposedModule: './AlunoList',
    exportName: 'StudentsListComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-alunos',
      routeRole: 'operational',
      shellNavigation: 'business-menu',
    },
  },
  {
    path: 'students/new',
    domain: 'alunos',
    runtimeRemoteName: 'mfe1',
    exposedModule: './AlunoNew',
    exportName: 'StudentsNewComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-alunos',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'students/:id',
    domain: 'alunos',
    runtimeRemoteName: 'mfe1',
    exposedModule: './AlunoDetail',
    exportName: 'StudentsDetailComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-alunos',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'students/:id/edit',
    domain: 'alunos',
    runtimeRemoteName: 'mfe1',
    exposedModule: './AlunoEdit',
    exportName: 'StudentsNewComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-alunos',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'responsibles',
    domain: 'responsaveis',
    runtimeRemoteName: 'mfe1',
    exposedModule: './ResponsavelList',
    exportName: 'ResponsiblesListComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-responsaveis',
      routeRole: 'operational',
      shellNavigation: 'business-menu',
    },
  },
  {
    path: 'responsibles/new',
    domain: 'responsaveis',
    runtimeRemoteName: 'mfe1',
    exposedModule: './ResponsavelNew',
    exportName: 'ResponsiblesNewComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-responsaveis',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'responsibles/:id',
    domain: 'responsaveis',
    runtimeRemoteName: 'mfe1',
    exposedModule: './ResponsavelDetail',
    exportName: 'ResponsiblesDetailComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-responsaveis',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'responsibles/:id/edit',
    domain: 'responsaveis',
    runtimeRemoteName: 'mfe1',
    exposedModule: './ResponsavelEdit',
    exportName: 'ResponsiblesNewComponent',
    extractionPlan: {
      candidate: true,
      targetRemoteName: 'mfe-responsaveis',
      routeRole: 'operational',
      shellNavigation: 'contextual',
    },
  },
  {
    path: 'academic/periods',
    domain: 'catalogo-academico',
    runtimeRemoteName: 'mfe-catalogo-academico',
    exposedModule: './CatalogoPeriods',
    exportName: 'AcademicPeriodsComponent',
  },
  {
    path: 'academic/series',
    domain: 'catalogo-academico',
    runtimeRemoteName: 'mfe-catalogo-academico',
    exposedModule: './CatalogoSeries',
    exportName: 'AcademicSeriesComponent',
  },
  {
    path: 'academic/shifts',
    domain: 'catalogo-academico',
    runtimeRemoteName: 'mfe-catalogo-academico',
    exposedModule: './CatalogoShifts',
    exportName: 'AcademicShiftsComponent',
  },
  {
    path: 'academic/classes',
    domain: 'catalogo-academico',
    runtimeRemoteName: 'mfe-catalogo-academico',
    exposedModule: './CatalogoClasses',
    exportName: 'AcademicClassesComponent',
  },
  {
    path: 'academic/disciplines',
    domain: 'catalogo-academico',
    runtimeRemoteName: 'mfe-catalogo-academico',
    exposedModule: './HistoricoDisciplines',
    exportName: 'DisciplinesComponent',
  },
  {
    path: 'teachers',
    domain: 'professores',
    runtimeRemoteName: 'mfe-professores',
    exposedModule: './ProfessorList',
    exportName: 'TeachersListComponent',
  },
  {
    path: 'teachers/:id',
    domain: 'professores',
    runtimeRemoteName: 'mfe-professores',
    exposedModule: './ProfessorDetail',
    exportName: 'TeacherDetailComponent',
  },
  {
    path: 'lessons',
    domain: 'aulas-avaliacoes',
    runtimeRemoteName: 'mfe-aulas-avaliacoes',
    exposedModule: './LessonList',
    exportName: 'LessonsListComponent',
  },
  {
    path: 'lessons/:id',
    domain: 'aulas-avaliacoes',
    runtimeRemoteName: 'mfe-aulas-avaliacoes',
    exposedModule: './LessonDetail',
    exportName: 'LessonDetailComponent',
  },
  {
    path: 'assessments',
    domain: 'aulas-avaliacoes',
    runtimeRemoteName: 'mfe-aulas-avaliacoes',
    exposedModule: './AssessmentList',
    exportName: 'AssessmentsListComponent',
  },
  {
    path: 'assessments/:id',
    domain: 'aulas-avaliacoes',
    runtimeRemoteName: 'mfe-aulas-avaliacoes',
    exposedModule: './AssessmentDetail',
    exportName: 'AssessmentDetailComponent',
  },
  {
    path: 'planning',
    domain: 'planejamento-ia',
    runtimeRemoteName: 'mfe-planejamento-ia',
    exposedModule: './PlanningList',
    exportName: 'PlanningListComponent',
  },
  {
    path: 'planning/:id',
    domain: 'planejamento-ia',
    runtimeRemoteName: 'mfe-planejamento-ia',
    exposedModule: './PlanningDetail',
    exportName: 'PlanningDetailComponent',
  },
  {
    path: 'planning-library',
    domain: 'planejamento-ia',
    runtimeRemoteName: 'mfe-planejamento-ia',
    exposedModule: './PlanningLibrary',
    exportName: 'PlanningLibraryComponent',
  },
  {
    path: 'dashboard/config',
    domain: 'dashboard',
    runtimeRemoteName: 'mfe-dashboard',
    exposedModule: './DashboardConfigAdmin',
    exportName: 'DashboardConfigAdminComponent',
  },
  {
    path: 'dashboard/snapshots',
    domain: 'dashboard',
    runtimeRemoteName: 'mfe-dashboard',
    exposedModule: './DashboardSnapshotsAdmin',
    exportName: 'DashboardSnapshotsAdminComponent',
  },
  {
    path: 'enrollment',
    domain: 'matriculas',
    runtimeRemoteName: 'mfe-matriculas',
    exposedModule: './Matricula',
    exportName: 'EnrollmentNewComponent',
  },
];

export const SHELL_BUSINESS_MENU: ShellMenuItem[] = [
  { label: 'Alunos', icon: 'person', route: '/students', domain: 'alunos', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Responsáveis', icon: 'family_restroom', route: '/responsibles', domain: 'responsaveis', perfis: ['ADMIN', 'SECRETARIA'] },
  { label: 'Períodos letivos', icon: 'date_range', route: '/academic/periods', domain: 'catalogo-academico', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Séries', icon: 'format_list_numbered', route: '/academic/series', domain: 'catalogo-academico', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Turnos', icon: 'schedule', route: '/academic/shifts', domain: 'catalogo-academico', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Turmas', icon: 'class', route: '/academic/classes', domain: 'catalogo-academico', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Disciplinas', icon: 'menu_book', route: '/academic/disciplines', domain: 'catalogo-academico', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Professores', icon: 'co_present', route: '/teachers', domain: 'professores', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Aulas', icon: 'event_note', route: '/lessons', domain: 'aulas-avaliacoes', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Avaliações', icon: 'grading', route: '/assessments', domain: 'aulas-avaliacoes', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Planejamento', icon: 'edit_calendar', route: '/planning', domain: 'planejamento-ia', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Biblioteca pedagógica', icon: 'local_library', route: '/planning-library', domain: 'planejamento-ia', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Matrícula', icon: 'assignment', route: '/enrollment', domain: 'matriculas', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
];

export const SHELL_ACCESS_MENU: ShellMenuItem[] = [
  { label: 'Usuários', icon: 'group', route: '/auth/users', domain: 'administracao', perfis: ['ADMIN'] },
  { label: 'Perfis', icon: 'badge', route: '/auth/profiles', domain: 'administracao', perfis: ['ADMIN'] },
  { label: 'Permissões', icon: 'verified_user', route: '/auth/permissions', domain: 'administracao', perfis: ['ADMIN'] },
  { label: 'Dashboards', icon: 'dashboard_customize', route: '/dashboard/config', domain: 'dashboard', perfis: ['ADMIN'] },
  { label: 'Snapshots', icon: 'timeline', route: '/dashboard/snapshots', domain: 'dashboard', perfis: ['ADMIN'] },
];

export const SHELL_BUSINESS_MENU_GROUPS: ShellMenuGroup[] = [
  {
    domain: 'alunos',
    label: SHELL_DOMAIN_CATALOG.alunos.label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'alunos'),
  },
  {
    domain: 'responsaveis',
    label: SHELL_DOMAIN_CATALOG.responsaveis.label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'responsaveis'),
  },
  {
    domain: 'catalogo-academico',
    label: SHELL_DOMAIN_CATALOG['catalogo-academico'].label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'catalogo-academico'),
  },
  {
    domain: 'professores',
    label: SHELL_DOMAIN_CATALOG.professores.label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'professores'),
  },
  {
    domain: 'aulas-avaliacoes',
    label: SHELL_DOMAIN_CATALOG['aulas-avaliacoes'].label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'aulas-avaliacoes'),
  },
  {
    domain: 'planejamento-ia',
    label: SHELL_DOMAIN_CATALOG['planejamento-ia'].label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'planejamento-ia'),
  },
  {
    domain: 'matriculas',
    label: SHELL_DOMAIN_CATALOG.matriculas.label,
    items: SHELL_BUSINESS_MENU.filter(item => item.domain === 'matriculas'),
  },
];

export const SHELL_ACCESS_MENU_GROUPS: ShellMenuGroup[] = [
  {
    domain: 'administracao',
    label: SHELL_DOMAIN_CATALOG.administracao.label,
    items: SHELL_ACCESS_MENU.filter(item => item.domain === 'administracao'),
  },
  {
    domain: 'dashboard',
    label: SHELL_DOMAIN_CATALOG.dashboard.label,
    items: SHELL_ACCESS_MENU.filter(item => item.domain === 'dashboard'),
  },
];

export function flattenShellMenuGroups(groups: ShellMenuGroup[]): ShellMenuItem[] {
  return groups.flatMap(group => group.items);
}

export const SHELL_EXTRACTION_CANDIDATES: ShellExtractionCandidateManifestItem[] = [
  {
    domain: 'alunos',
    label: 'Alunos',
    currentPlacement: 'microfrontend',
    targetRemoteName: 'mfe-alunos',
    runtimeRemoteNames: ['mfe1'],
    expectedExposedModules: [
      './AlunoList',
      './AlunoNew',
      './AlunoDetail',
      './AlunoEdit',
    ],
    routePaths: [
      'students',
      'students/new',
      'students/:id',
      'students/:id/edit',
    ],
    operationalRoutePaths: [
      'students',
      'students/new',
      'students/:id',
      'students/:id/edit',
    ],
    administrativeRoutePaths: [],
    landingRoutes: [],
    businessMenuRoutes: ['/students'],
    accessMenuRoutes: [],
    contextualRoutes: ['students/new', 'students/:id', 'students/:id/edit'],
  },
  {
    domain: 'responsaveis',
    label: 'Responsaveis',
    currentPlacement: 'microfrontend',
    targetRemoteName: 'mfe-responsaveis',
    runtimeRemoteNames: ['mfe1'],
    expectedExposedModules: [
      './ResponsavelList',
      './ResponsavelNew',
      './ResponsavelDetail',
      './ResponsavelEdit',
    ],
    routePaths: [
      'responsibles',
      'responsibles/new',
      'responsibles/:id',
      'responsibles/:id/edit',
    ],
    operationalRoutePaths: [
      'responsibles',
      'responsibles/new',
      'responsibles/:id',
      'responsibles/:id/edit',
    ],
    administrativeRoutePaths: [],
    landingRoutes: [],
    businessMenuRoutes: ['/responsibles'],
    accessMenuRoutes: [],
    contextualRoutes: [
      'responsibles/new',
      'responsibles/:id',
      'responsibles/:id/edit',
    ],
  },
];

export const SHELL_DOMAIN_INVENTORY: ShellDomainInventoryItem[] = Object.values(
  SHELL_DOMAIN_CATALOG,
).map(domain => ({
  ...domain,
  remoteRoutes: SHELL_REMOTE_ROUTES.filter(route => route.domain === domain.domain),
  menuItems: [...SHELL_BUSINESS_MENU, ...SHELL_ACCESS_MENU].filter(
    item => item.domain === domain.domain,
  ),
}));
