import { Route } from '@angular/router';

type TeachingDomainName = 'aulas-avaliacoes';
type TeachingFutureRemoteName = 'mfe-aulas-avaliacoes';
type TeachingRoutePath =
  | 'lessons'
  | 'lessons/:id'
  | 'assessments'
  | 'assessments/:id'
  | 'class-diaries';
type TeachingExposedModule =
  | './LessonList'
  | './LessonDetail'
  | './AssessmentList'
  | './AssessmentDetail'
  | './ClassDiary';
type TeachingExposeFilePath =
  | './src/app/professor/exposes/lesson-list.expose.ts'
  | './src/app/professor/exposes/lesson-detail.expose.ts'
  | './src/app/professor/exposes/assessment-list.expose.ts'
  | './src/app/professor/exposes/assessment-detail.expose.ts'
  | './src/app/professor/exposes/diario-classe.expose.ts';
type TeachingExportName =
  | 'LessonsListComponent'
  | 'LessonDetailComponent'
  | 'AssessmentsListComponent'
  | 'AssessmentDetailComponent'
  | 'DiarioClasseComponent';
type TeachingManifestKey =
  | 'lesson-list'
  | 'lesson-detail'
  | 'assessment-list'
  | 'assessment-detail'
  | 'class-diary';
type TeachingRouteKind = 'list' | 'detail';
type TeachingRouteRole = 'operational';
type TeachingShellNavigation = 'business-menu' | 'contextual';

export interface TeachingDomainManifestItem {
  key: TeachingManifestKey;
  domain: TeachingDomainName;
  futureRemoteName: TeachingFutureRemoteName;
  path: TeachingRoutePath;
  exposedModule: TeachingExposedModule;
  exposeFilePath: TeachingExposeFilePath;
  exportName: TeachingExportName;
  routeKind: TeachingRouteKind;
  routeRole: TeachingRouteRole;
  shellNavigation: TeachingShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const TEACHING_DOMAIN_MANIFEST: readonly TeachingDomainManifestItem[] = [
  {
    key: 'class-diary',
    domain: 'aulas-avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    path: 'class-diaries',
    exposedModule: './ClassDiary',
    exposeFilePath: './src/app/professor/exposes/diario-classe.expose.ts',
    exportName: 'DiarioClasseComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () => import('../diario/diario-classe.component').then(m => m.DiarioClasseComponent),
  },
  {
    key: 'lesson-list',
    domain: 'aulas-avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    path: 'lessons',
    exposedModule: './LessonList',
    exposeFilePath: './src/app/professor/exposes/lesson-list.expose.ts',
    exportName: 'LessonsListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/lessons/lessons-list.component').then(
        m => m.LessonsListComponent,
      ),
  },
  {
    key: 'lesson-detail',
    domain: 'aulas-avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    path: 'lessons/:id',
    exposedModule: './LessonDetail',
    exposeFilePath: './src/app/professor/exposes/lesson-detail.expose.ts',
    exportName: 'LessonDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/lessons/lesson-detail.component').then(
        m => m.LessonDetailComponent,
      ),
  },
  {
    key: 'assessment-list',
    domain: 'aulas-avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    path: 'assessments',
    exposedModule: './AssessmentList',
    exposeFilePath: './src/app/professor/exposes/assessment-list.expose.ts',
    exportName: 'AssessmentsListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/assessments/assessments-list.component').then(
        m => m.AssessmentsListComponent,
      ),
  },
  {
    key: 'assessment-detail',
    domain: 'aulas-avaliacoes',
    futureRemoteName: 'mfe-aulas-avaliacoes',
    path: 'assessments/:id',
    exposedModule: './AssessmentDetail',
    exposeFilePath: './src/app/professor/exposes/assessment-detail.expose.ts',
    exportName: 'AssessmentDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/assessments/assessment-detail.component').then(
        m => m.AssessmentDetailComponent,
      ),
  },
];

export const TEACHING_INTERNAL_ROUTES: Route[] = TEACHING_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
