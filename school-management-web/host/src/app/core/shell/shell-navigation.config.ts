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

export interface ShellRemoteRoute {
  path: string;
  domain: ShellDomain;
  exposedModule: string;
  exportName: string;
}

export interface ShellMenuItem {
  label: string;
  icon: string;
  route: string;
  domain: ShellDomain;
  perfis?: string[];
}

export const SHELL_REMOTE_ROUTES: ShellRemoteRoute[] = [
  {
    path: 'dashboard',
    domain: 'dashboard',
    exposedModule: './Dashboard',
    exportName: 'AcademicOperationalDashboardComponent',
  },
  {
    path: 'students',
    domain: 'alunos',
    exposedModule: './AlunoList',
    exportName: 'StudentsListComponent',
  },
  {
    path: 'students/new',
    domain: 'alunos',
    exposedModule: './AlunoNew',
    exportName: 'StudentsNewComponent',
  },
  {
    path: 'students/:id',
    domain: 'alunos',
    exposedModule: './AlunoDetail',
    exportName: 'StudentsDetailComponent',
  },
  {
    path: 'students/:id/edit',
    domain: 'alunos',
    exposedModule: './AlunoNew',
    exportName: 'StudentsNewComponent',
  },
  {
    path: 'responsibles',
    domain: 'responsaveis',
    exposedModule: './ResponsavelList',
    exportName: 'ResponsiblesListComponent',
  },
  {
    path: 'responsibles/new',
    domain: 'responsaveis',
    exposedModule: './ResponsavelNew',
    exportName: 'ResponsiblesNewComponent',
  },
  {
    path: 'responsibles/:id',
    domain: 'responsaveis',
    exposedModule: './ResponsavelDetail',
    exportName: 'ResponsiblesDetailComponent',
  },
  {
    path: 'responsibles/:id/edit',
    domain: 'responsaveis',
    exposedModule: './ResponsavelNew',
    exportName: 'ResponsiblesNewComponent',
  },
  {
    path: 'academic/periods',
    domain: 'catalogo-academico',
    exposedModule: './CatalogoPeriods',
    exportName: 'AcademicPeriodsComponent',
  },
  {
    path: 'academic/series',
    domain: 'catalogo-academico',
    exposedModule: './CatalogoSeries',
    exportName: 'AcademicSeriesComponent',
  },
  {
    path: 'academic/shifts',
    domain: 'catalogo-academico',
    exposedModule: './CatalogoShifts',
    exportName: 'AcademicShiftsComponent',
  },
  {
    path: 'academic/classes',
    domain: 'catalogo-academico',
    exposedModule: './CatalogoClasses',
    exportName: 'AcademicClassesComponent',
  },
  {
    path: 'academic/disciplines',
    domain: 'catalogo-academico',
    exposedModule: './HistoricoDisciplines',
    exportName: 'DisciplinesComponent',
  },
  {
    path: 'teachers',
    domain: 'professores',
    exposedModule: './ProfessorList',
    exportName: 'TeachersListComponent',
  },
  {
    path: 'teachers/:id',
    domain: 'professores',
    exposedModule: './ProfessorDetail',
    exportName: 'TeacherDetailComponent',
  },
  {
    path: 'lessons',
    domain: 'aulas-avaliacoes',
    exposedModule: './LessonList',
    exportName: 'LessonsListComponent',
  },
  {
    path: 'lessons/:id',
    domain: 'aulas-avaliacoes',
    exposedModule: './LessonDetail',
    exportName: 'LessonDetailComponent',
  },
  {
    path: 'assessments',
    domain: 'aulas-avaliacoes',
    exposedModule: './AssessmentList',
    exportName: 'AssessmentsListComponent',
  },
  {
    path: 'assessments/:id',
    domain: 'aulas-avaliacoes',
    exposedModule: './AssessmentDetail',
    exportName: 'AssessmentDetailComponent',
  },
  {
    path: 'planning',
    domain: 'planejamento-ia',
    exposedModule: './PlanningList',
    exportName: 'PlanningListComponent',
  },
  {
    path: 'planning/:id',
    domain: 'planejamento-ia',
    exposedModule: './PlanningDetail',
    exportName: 'PlanningDetailComponent',
  },
  {
    path: 'planning-library',
    domain: 'planejamento-ia',
    exposedModule: './PlanningLibrary',
    exportName: 'PlanningLibraryComponent',
  },
  {
    path: 'dashboard/config',
    domain: 'dashboard',
    exposedModule: './DashboardConfigAdmin',
    exportName: 'DashboardConfigAdminComponent',
  },
  {
    path: 'dashboard/snapshots',
    domain: 'dashboard',
    exposedModule: './DashboardSnapshotsAdmin',
    exportName: 'DashboardSnapshotsAdminComponent',
  },
  {
    path: 'enrollment',
    domain: 'matriculas',
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
