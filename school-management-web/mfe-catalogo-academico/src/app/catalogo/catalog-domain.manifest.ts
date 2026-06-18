import { Route } from '@angular/router';

type CatalogDomainName = 'catalogo-academico';
type CatalogFutureRemoteName = 'mfe-catalogo-academico';
type CatalogRoutePath =
  | 'academic/periods'
  | 'academic/series'
  | 'academic/shifts'
  | 'academic/classes'
  | 'academic/disciplines';
type CatalogExposedModule =
  | './CatalogoPeriods'
  | './CatalogoSeries'
  | './CatalogoShifts'
  | './CatalogoClasses'
  | './HistoricoDisciplines';
type CatalogExposeFilePath =
  | './src/app/catalogo/exposes/catalog-periods.expose.ts'
  | './src/app/catalogo/exposes/catalog-series.expose.ts'
  | './src/app/catalogo/exposes/catalog-shifts.expose.ts'
  | './src/app/catalogo/exposes/catalog-classes.expose.ts'
  | './src/app/catalogo/exposes/catalog-disciplines.expose.ts';
type CatalogExportName =
  | 'AcademicPeriodsComponent'
  | 'AcademicSeriesComponent'
  | 'AcademicShiftsComponent'
  | 'AcademicClassesComponent'
  | 'DisciplinesComponent';
type CatalogManifestKey =
  | 'academic-periods'
  | 'academic-series'
  | 'academic-shifts'
  | 'academic-classes'
  | 'academic-disciplines';
type CatalogRouteKind = 'list';
type CatalogRouteRole = 'operational';
type CatalogShellNavigation = 'business-menu';

export interface CatalogDomainManifestItem {
  key: CatalogManifestKey;
  domain: CatalogDomainName;
  futureRemoteName: CatalogFutureRemoteName;
  path: CatalogRoutePath;
  exposedModule: CatalogExposedModule;
  exposeFilePath: CatalogExposeFilePath;
  exportName: CatalogExportName;
  routeKind: CatalogRouteKind;
  routeRole: CatalogRouteRole;
  shellNavigation: CatalogShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const CATALOG_DOMAIN_MANIFEST: readonly CatalogDomainManifestItem[] = [
  {
    key: 'academic-periods',
    domain: 'catalogo-academico',
    futureRemoteName: 'mfe-catalogo-academico',
    path: 'academic/periods',
    exposedModule: './CatalogoPeriods',
    exposeFilePath: './src/app/catalogo/exposes/catalog-periods.expose.ts',
    exportName: 'AcademicPeriodsComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/periods/academic-periods.component').then(
        m => m.AcademicPeriodsComponent,
      ),
  },
  {
    key: 'academic-series',
    domain: 'catalogo-academico',
    futureRemoteName: 'mfe-catalogo-academico',
    path: 'academic/series',
    exposedModule: './CatalogoSeries',
    exposeFilePath: './src/app/catalogo/exposes/catalog-series.expose.ts',
    exportName: 'AcademicSeriesComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/series/academic-series.component').then(
        m => m.AcademicSeriesComponent,
      ),
  },
  {
    key: 'academic-shifts',
    domain: 'catalogo-academico',
    futureRemoteName: 'mfe-catalogo-academico',
    path: 'academic/shifts',
    exposedModule: './CatalogoShifts',
    exposeFilePath: './src/app/catalogo/exposes/catalog-shifts.expose.ts',
    exportName: 'AcademicShiftsComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/shifts/academic-shifts.component').then(
        m => m.AcademicShiftsComponent,
      ),
  },
  {
    key: 'academic-classes',
    domain: 'catalogo-academico',
    futureRemoteName: 'mfe-catalogo-academico',
    path: 'academic/classes',
    exposedModule: './CatalogoClasses',
    exposeFilePath: './src/app/catalogo/exposes/catalog-classes.expose.ts',
    exportName: 'AcademicClassesComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/classes/academic-classes.component').then(
        m => m.AcademicClassesComponent,
      ),
  },
  {
    key: 'academic-disciplines',
    domain: 'catalogo-academico',
    futureRemoteName: 'mfe-catalogo-academico',
    path: 'academic/disciplines',
    exposedModule: './HistoricoDisciplines',
    exposeFilePath: './src/app/catalogo/exposes/catalog-disciplines.expose.ts',
    exportName: 'DisciplinesComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('../historico/pages/disciplines/disciplines.component').then(
        m => m.DisciplinesComponent,
      ),
  },
];

export const CATALOG_INTERNAL_ROUTES: Route[] = CATALOG_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
