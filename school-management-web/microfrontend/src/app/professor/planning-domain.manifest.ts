import { Route } from '@angular/router';

type PlanningRoutePath = 'planning' | 'planning/:id' | 'planning-library';
type PlanningExposedModule = './PlanningList' | './PlanningDetail' | './PlanningLibrary';
type PlanningExposeFilePath =
  | './src/app/professor/exposes/planning-list.expose.ts'
  | './src/app/professor/exposes/planning-detail.expose.ts'
  | './src/app/professor/exposes/planning-library.expose.ts';
type PlanningExportName = 'PlanningListComponent' | 'PlanningDetailComponent' | 'PlanningLibraryComponent';
type PlanningManifestKey = 'planning-list' | 'planning-detail' | 'planning-library';

export interface PlanningDomainManifestItem {
  key: PlanningManifestKey;
  path: PlanningRoutePath;
  exposedModule: PlanningExposedModule;
  exposeFilePath: PlanningExposeFilePath;
  exportName: PlanningExportName;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const PLANNING_DOMAIN_MANIFEST: readonly PlanningDomainManifestItem[] = [
  {
    key: 'planning-list',
    path: 'planning',
    exposedModule: './PlanningList',
    exposeFilePath: './src/app/professor/exposes/planning-list.expose.ts',
    exportName: 'PlanningListComponent',
    loadComponent: () =>
      import('./pages/planning/planning-list.component').then(
        m => m.PlanningListComponent,
      ),
  },
  {
    key: 'planning-detail',
    path: 'planning/:id',
    exposedModule: './PlanningDetail',
    exposeFilePath: './src/app/professor/exposes/planning-detail.expose.ts',
    exportName: 'PlanningDetailComponent',
    loadComponent: () =>
      import('./pages/planning/planning-detail.component').then(
        m => m.PlanningDetailComponent,
      ),
  },
  {
    key: 'planning-library',
    path: 'planning-library',
    exposedModule: './PlanningLibrary',
    exposeFilePath: './src/app/professor/exposes/planning-library.expose.ts',
    exportName: 'PlanningLibraryComponent',
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
