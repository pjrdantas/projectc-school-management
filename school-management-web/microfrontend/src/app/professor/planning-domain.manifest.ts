import { Route } from '@angular/router';

type PlanningDomainName = 'planejamento-ia';
type PlanningFutureRemoteName = 'mfe-planejamento-ia';
type PlanningRoutePath = 'planning' | 'planning/:id' | 'planning-library';
type PlanningExposedModule = './PlanningList' | './PlanningDetail' | './PlanningLibrary';
type PlanningExposeFilePath =
  | './src/app/professor/exposes/planning-list.expose.ts'
  | './src/app/professor/exposes/planning-detail.expose.ts'
  | './src/app/professor/exposes/planning-library.expose.ts';
type PlanningExportName = 'PlanningListComponent' | 'PlanningDetailComponent' | 'PlanningLibraryComponent';
type PlanningManifestKey = 'planning-list' | 'planning-detail' | 'planning-library';
type PlanningRouteKind = 'list' | 'detail' | 'library';
type PlanningRouteRole = 'operational';
type PlanningShellNavigation = 'business-menu' | 'contextual';

export interface PlanningDomainManifestItem {
  key: PlanningManifestKey;
  domain: PlanningDomainName;
  futureRemoteName: PlanningFutureRemoteName;
  path: PlanningRoutePath;
  exposedModule: PlanningExposedModule;
  exposeFilePath: PlanningExposeFilePath;
  exportName: PlanningExportName;
  routeKind: PlanningRouteKind;
  routeRole: PlanningRouteRole;
  shellNavigation: PlanningShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const PLANNING_DOMAIN_MANIFEST: readonly PlanningDomainManifestItem[] = [
  {
    key: 'planning-list',
    domain: 'planejamento-ia',
    futureRemoteName: 'mfe-planejamento-ia',
    path: 'planning',
    exposedModule: './PlanningList',
    exposeFilePath: './src/app/professor/exposes/planning-list.expose.ts',
    exportName: 'PlanningListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/planning/planning-list.component').then(
        m => m.PlanningListComponent,
      ),
  },
  {
    key: 'planning-detail',
    domain: 'planejamento-ia',
    futureRemoteName: 'mfe-planejamento-ia',
    path: 'planning/:id',
    exposedModule: './PlanningDetail',
    exposeFilePath: './src/app/professor/exposes/planning-detail.expose.ts',
    exportName: 'PlanningDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/planning/planning-detail.component').then(
        m => m.PlanningDetailComponent,
      ),
  },
  {
    key: 'planning-library',
    domain: 'planejamento-ia',
    futureRemoteName: 'mfe-planejamento-ia',
    path: 'planning-library',
    exposedModule: './PlanningLibrary',
    exposeFilePath: './src/app/professor/exposes/planning-library.expose.ts',
    exportName: 'PlanningLibraryComponent',
    routeKind: 'library',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/planning/planning-library.component').then(
        m => m.PlanningLibraryComponent,
      ),
  },
];

export const PLANNING_INTERNAL_ROUTES: Route[] = PLANNING_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
