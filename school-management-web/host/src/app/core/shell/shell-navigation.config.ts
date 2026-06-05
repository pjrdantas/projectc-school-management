export interface ShellRemoteRoute {
  path: string;
  exposedModule: string;
  exportName: string;
}

export interface ShellMenuItem {
  label: string;
  icon: string;
  route: string;
  perfis?: string[];
}

export const SHELL_REMOTE_ROUTES: ShellRemoteRoute[] = [
  {
    path: 'dashboard',
    exposedModule: './Dashboard',
    exportName: 'AcademicOperationalDashboardComponent',
  },
  {
    path: 'students',
    exposedModule: './AlunoList',
    exportName: 'StudentsListComponent',
  },
  {
    path: 'students/new',
    exposedModule: './AlunoNew',
    exportName: 'StudentsNewComponent',
  },
  {
    path: 'students/:id',
    exposedModule: './AlunoDetail',
    exportName: 'StudentsDetailComponent',
  },
  {
    path: 'students/:id/edit',
    exposedModule: './AlunoNew',
    exportName: 'StudentsNewComponent',
  },
  {
    path: 'responsibles',
    exposedModule: './ResponsavelList',
    exportName: 'ResponsiblesListComponent',
  },
  {
    path: 'responsibles/new',
    exposedModule: './ResponsavelNew',
    exportName: 'ResponsiblesNewComponent',
  },
  {
    path: 'responsibles/:id',
    exposedModule: './ResponsavelDetail',
    exportName: 'ResponsiblesDetailComponent',
  },
  {
    path: 'responsibles/:id/edit',
    exposedModule: './ResponsavelNew',
    exportName: 'ResponsiblesNewComponent',
  },
  {
    path: 'academic/periods',
    exposedModule: './CatalogoPeriods',
    exportName: 'AcademicPeriodsComponent',
  },
  {
    path: 'academic/series',
    exposedModule: './CatalogoSeries',
    exportName: 'AcademicSeriesComponent',
  },
  {
    path: 'academic/shifts',
    exposedModule: './CatalogoShifts',
    exportName: 'AcademicShiftsComponent',
  },
  {
    path: 'academic/classes',
    exposedModule: './CatalogoClasses',
    exportName: 'AcademicClassesComponent',
  },
  {
    path: 'academic/disciplines',
    exposedModule: './HistoricoDisciplines',
    exportName: 'DisciplinesComponent',
  },
  {
    path: 'enrollment',
    exposedModule: './Matricula',
    exportName: 'EnrollmentNewComponent',
  },
];

export const SHELL_BUSINESS_MENU: ShellMenuItem[] = [
  { label: 'Alunos', icon: 'person', route: '/students', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Responsáveis', icon: 'family_restroom', route: '/responsibles', perfis: ['ADMIN', 'SECRETARIA'] },
  { label: 'Períodos letivos', icon: 'date_range', route: '/academic/periods', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Séries', icon: 'format_list_numbered', route: '/academic/series', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Turnos', icon: 'schedule', route: '/academic/shifts', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
  { label: 'Turmas', icon: 'class', route: '/academic/classes', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Disciplinas', icon: 'menu_book', route: '/academic/disciplines', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR', 'PROFESSOR'] },
  { label: 'Matrícula', icon: 'assignment', route: '/enrollment', perfis: ['ADMIN', 'SECRETARIA', 'DIRETOR'] },
];

export const SHELL_ACCESS_MENU: ShellMenuItem[] = [
  { label: 'Usuários', icon: 'group', route: '/auth/users', perfis: ['ADMIN'] },
  { label: 'Perfis', icon: 'badge', route: '/auth/profiles', perfis: ['ADMIN'] },
  { label: 'Permissões', icon: 'verified_user', route: '/auth/permissions', perfis: ['ADMIN'] },
];
