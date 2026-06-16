import { Route } from '@angular/router';

type ResponsibleDomainName = 'responsaveis';
type ResponsibleFutureRemoteName = 'mfe-responsaveis';
type ResponsibleRoutePath =
  | 'responsibles'
  | 'responsibles/new'
  | 'responsibles/:id'
  | 'responsibles/:id/edit';
type ResponsibleExposedModule =
  | './ResponsavelList'
  | './ResponsavelNew'
  | './ResponsavelDetail'
  | './ResponsavelEdit';
type ResponsibleExposeFilePath =
  | './src/app/responsavel/exposes/responsible-list.expose.ts'
  | './src/app/responsavel/exposes/responsible-new.expose.ts'
  | './src/app/responsavel/exposes/responsible-detail.expose.ts'
  | './src/app/responsavel/exposes/responsible-edit.expose.ts';
type ResponsibleExportName =
  | 'ResponsiblesListComponent'
  | 'ResponsiblesNewComponent'
  | 'ResponsiblesDetailComponent';
type ResponsibleManifestKey =
  | 'responsibles-list'
  | 'responsibles-new'
  | 'responsibles-detail'
  | 'responsibles-edit';
type ResponsibleRouteKind = 'list' | 'create' | 'detail' | 'edit';
type ResponsibleRouteRole = 'operational';
type ResponsibleShellNavigation = 'business-menu' | 'contextual';

export interface ResponsibleDomainManifestItem {
  key: ResponsibleManifestKey;
  domain: ResponsibleDomainName;
  futureRemoteName: ResponsibleFutureRemoteName;
  path: ResponsibleRoutePath;
  exposedModule: ResponsibleExposedModule;
  exposeFilePath: ResponsibleExposeFilePath;
  exportName: ResponsibleExportName;
  routeKind: ResponsibleRouteKind;
  routeRole: ResponsibleRouteRole;
  shellNavigation: ResponsibleShellNavigation;
  extractionCandidate: boolean;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const RESPONSIBLE_DOMAIN_MANIFEST: readonly ResponsibleDomainManifestItem[] = [
  {
    key: 'responsibles-list',
    domain: 'responsaveis',
    futureRemoteName: 'mfe-responsaveis',
    path: 'responsibles',
    exposedModule: './ResponsavelList',
    exposeFilePath: './src/app/responsavel/exposes/responsible-list.expose.ts',
    exportName: 'ResponsiblesListComponent',
    routeKind: 'list',
    routeRole: 'operational',
    shellNavigation: 'business-menu',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/list/responsibles-list.component').then(
        m => m.ResponsiblesListComponent,
      ),
  },
  {
    key: 'responsibles-new',
    domain: 'responsaveis',
    futureRemoteName: 'mfe-responsaveis',
    path: 'responsibles/new',
    exposedModule: './ResponsavelNew',
    exposeFilePath: './src/app/responsavel/exposes/responsible-new.expose.ts',
    exportName: 'ResponsiblesNewComponent',
    routeKind: 'create',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/new/responsibles-new.component').then(
        m => m.ResponsiblesNewComponent,
      ),
  },
  {
    key: 'responsibles-detail',
    domain: 'responsaveis',
    futureRemoteName: 'mfe-responsaveis',
    path: 'responsibles/:id',
    exposedModule: './ResponsavelDetail',
    exposeFilePath: './src/app/responsavel/exposes/responsible-detail.expose.ts',
    exportName: 'ResponsiblesDetailComponent',
    routeKind: 'detail',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/detail/responsibles-detail.component').then(
        m => m.ResponsiblesDetailComponent,
      ),
  },
  {
    key: 'responsibles-edit',
    domain: 'responsaveis',
    futureRemoteName: 'mfe-responsaveis',
    path: 'responsibles/:id/edit',
    exposedModule: './ResponsavelEdit',
    exposeFilePath: './src/app/responsavel/exposes/responsible-edit.expose.ts',
    exportName: 'ResponsiblesNewComponent',
    routeKind: 'edit',
    routeRole: 'operational',
    shellNavigation: 'contextual',
    extractionCandidate: true,
    loadComponent: () =>
      import('./pages/new/responsibles-new.component').then(
        m => m.ResponsiblesNewComponent,
      ),
  },
];

export const RESPONSIBLE_INTERNAL_ROUTES: Route[] = RESPONSIBLE_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
