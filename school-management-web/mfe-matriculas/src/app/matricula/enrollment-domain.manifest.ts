import { Route } from '@angular/router';

type EnrollmentDomainName = 'matriculas';
type EnrollmentFutureRemoteName = 'mfe-matriculas';
type EnrollmentRoutePath = 'enrollment';
type EnrollmentExposedModule = './Matricula';
type EnrollmentExposeFilePath = './src/app/matricula/exposes/enrollment.expose.ts';
type EnrollmentExportName = 'EnrollmentNewComponent';
type EnrollmentManifestKey = 'enrollment-new';
type EnrollmentRouteKind = 'create';
type EnrollmentRouteRole = 'operational';
type EnrollmentShellNavigation = 'business-menu';

export interface EnrollmentDomainManifestItem {
  key: EnrollmentManifestKey;
  domain: EnrollmentDomainName;
  futureRemoteName: EnrollmentFutureRemoteName;
  path: EnrollmentRoutePath;
  exposedModule: EnrollmentExposedModule;
  exposeFilePath: EnrollmentExposeFilePath;
  exportName: EnrollmentExportName;
  routeKind: EnrollmentRouteKind;
  routeRole: EnrollmentRouteRole;
  shellNavigation: EnrollmentShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const ENROLLMENT_DOMAIN_MANIFEST: readonly EnrollmentDomainManifestItem[] = [
  {
    key: 'enrollment-new',
    domain: 'matriculas',
    futureRemoteName: 'mfe-matriculas',
    path: 'enrollment',
    exposedModule: './Matricula',
    exposeFilePath: './src/app/matricula/exposes/enrollment.expose.ts',
    exportName: 'EnrollmentNewComponent',
    routeKind: 'create',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: false,
    loadComponent: () =>
      import('./pages/new/enrollment-new.component').then(m => m.EnrollmentNewComponent),
  },
];

export const ENROLLMENT_INTERNAL_ROUTES: Route[] = ENROLLMENT_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
