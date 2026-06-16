import { Route } from '@angular/router';

type ProfessorDomainName = 'professores';
type ProfessorFutureRemoteName = 'mfe-professores';
type ProfessorRoutePath = 'teachers' | 'teachers/:id';
type ProfessorExposedModule = './ProfessorList' | './ProfessorDetail';
type ProfessorExposeFilePath =
  | './src/app/professor/exposes/professor-list.expose.ts'
  | './src/app/professor/exposes/professor-detail.expose.ts';
type ProfessorExportName = 'TeachersListComponent' | 'TeacherDetailComponent';
type ProfessorManifestKey = 'teacher-list' | 'teacher-detail';
type ProfessorRouteKind = 'list' | 'detail';
type ProfessorRouteRole = 'operational';
type ProfessorShellNavigation = 'business-menu' | 'contextual';

export interface ProfessorDomainManifestItem {
  key: ProfessorManifestKey;
  domain: ProfessorDomainName;
  futureRemoteName: ProfessorFutureRemoteName;
  path: ProfessorRoutePath;
  exposedModule: ProfessorExposedModule;
  exposeFilePath: ProfessorExposeFilePath;
  exportName: ProfessorExportName;
  routeKind: ProfessorRouteKind;
  routeRole: ProfessorRouteRole;
  shellNavigation: ProfessorShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const PROFESSOR_DOMAIN_MANIFEST: readonly ProfessorDomainManifestItem[] = [
  {
    key: 'teacher-list',
    domain: 'professores',
    futureRemoteName: 'mfe-professores',
    path: 'teachers',
    exposedModule: './ProfessorList',
    exposeFilePath: './src/app/professor/exposes/professor-list.expose.ts',
    exportName: 'TeachersListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/list/teachers-list.component').then(
        m => m.TeachersListComponent,
      ),
  },
  {
    key: 'teacher-detail',
    domain: 'professores',
    futureRemoteName: 'mfe-professores',
    path: 'teachers/:id',
    exposedModule: './ProfessorDetail',
    exposeFilePath: './src/app/professor/exposes/professor-detail.expose.ts',
    exportName: 'TeacherDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/detail/teacher-detail.component').then(
        m => m.TeacherDetailComponent,
      ),
  },
];

export const PROFESSOR_INTERNAL_ROUTES: Route[] = PROFESSOR_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
