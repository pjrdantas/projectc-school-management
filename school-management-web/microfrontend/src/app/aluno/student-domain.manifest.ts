import { Route } from '@angular/router';

type StudentDomainName = 'alunos';
type StudentFutureRemoteName = 'mfe-alunos';
type StudentRoutePath = 'students' | 'students/new' | 'students/:id' | 'students/:id/edit';
type StudentExposedModule = './AlunoList' | './AlunoNew' | './AlunoDetail' | './AlunoEdit';
type StudentExposeFilePath =
  | './src/app/aluno/exposes/student-list.expose.ts'
  | './src/app/aluno/exposes/student-new.expose.ts'
  | './src/app/aluno/exposes/student-detail.expose.ts'
  | './src/app/aluno/exposes/student-edit.expose.ts';
type StudentExportName =
  | 'StudentsListComponent'
  | 'StudentsNewComponent'
  | 'StudentsDetailComponent';
type StudentManifestKey = 'students-list' | 'students-new' | 'students-detail' | 'students-edit';
type StudentRouteKind = 'list' | 'create' | 'detail' | 'edit';
type StudentRouteRole = 'operational';
type StudentShellNavigation = 'business-menu' | 'contextual';

export interface StudentDomainManifestItem {
  key: StudentManifestKey;
  domain: StudentDomainName;
  futureRemoteName: StudentFutureRemoteName;
  path: StudentRoutePath;
  exposedModule: StudentExposedModule;
  exposeFilePath: StudentExposeFilePath;
  exportName: StudentExportName;
  routeKind: StudentRouteKind;
  routeRole: StudentRouteRole;
  shellNavigation: StudentShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const STUDENT_DOMAIN_MANIFEST: readonly StudentDomainManifestItem[] = [
  {
    key: 'students-list',
    domain: 'alunos',
    futureRemoteName: 'mfe-alunos',
    path: 'students',
    exposedModule: './AlunoList',
    exposeFilePath: './src/app/aluno/exposes/student-list.expose.ts',
    exportName: 'StudentsListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/list/students-list.component').then(m => m.StudentsListComponent),
  },
  {
    key: 'students-new',
    domain: 'alunos',
    futureRemoteName: 'mfe-alunos',
    path: 'students/new',
    exposedModule: './AlunoNew',
    exposeFilePath: './src/app/aluno/exposes/student-new.expose.ts',
    exportName: 'StudentsNewComponent',
    routeKind: 'create',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/new/students-new.component').then(m => m.StudentsNewComponent),
  },
  {
    key: 'students-detail',
    domain: 'alunos',
    futureRemoteName: 'mfe-alunos',
    path: 'students/:id',
    exposedModule: './AlunoDetail',
    exposeFilePath: './src/app/aluno/exposes/student-detail.expose.ts',
    exportName: 'StudentsDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/detail/students-detail.component').then(m => m.StudentsDetailComponent),
  },
  {
    key: 'students-edit',
    domain: 'alunos',
    futureRemoteName: 'mfe-alunos',
    path: 'students/:id/edit',
    exposedModule: './AlunoEdit',
    exposeFilePath: './src/app/aluno/exposes/student-edit.expose.ts',
    exportName: 'StudentsNewComponent',
    routeKind: 'edit',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/new/students-new.component').then(m => m.StudentsNewComponent),
  },
];

export const STUDENT_INTERNAL_ROUTES: Route[] = STUDENT_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
