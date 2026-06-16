import { Provider } from '@angular/core';
import { Route } from '@angular/router';

type DashboardRoutePath = 'dashboard' | 'dashboard/config' | 'dashboard/snapshots';
type DashboardExposedModule = './Dashboard' | './DashboardConfigAdmin' | './DashboardSnapshotsAdmin';
type DashboardExportName =
  | 'AcademicOperationalDashboardComponent'
  | 'DashboardConfigAdminComponent'
  | 'DashboardSnapshotsAdminComponent';
type DashboardRouteRole = 'operational' | 'administrative';
type DashboardShellNavigation = 'landing' | 'access-menu';
type DashboardManifestKey = 'operational' | 'config-admin' | 'snapshots-admin';

export interface DashboardDomainManifestItem {
  key: DashboardManifestKey;
  path: DashboardRoutePath;
  exposedModule: DashboardExposedModule;
  exportName: DashboardExportName;
  routeRole: DashboardRouteRole;
  shellNavigation: DashboardShellNavigation;
  loadComponent: NonNullable<Route['loadComponent']>;
}

export const DASHBOARD_DOMAIN_PROVIDERS: Provider[] = [];

export const DASHBOARD_DOMAIN_MANIFEST: readonly DashboardDomainManifestItem[] = [
  {
    key: 'operational',
    path: 'dashboard',
    exposedModule: './Dashboard',
    exportName: 'AcademicOperationalDashboardComponent',
    routeRole: 'operational',
    shellNavigation: 'landing',
    loadComponent: () =>
      import('./pages/academic-operational-dashboard.component').then(
        m => m.AcademicOperationalDashboardComponent,
      ),
  },
  {
    key: 'config-admin',
    path: 'dashboard/config',
    exposedModule: './DashboardConfigAdmin',
    exportName: 'DashboardConfigAdminComponent',
    routeRole: 'administrative',
    shellNavigation: 'access-menu',
    loadComponent: () =>
      import('./pages/config/dashboard-config-admin.component').then(
        m => m.DashboardConfigAdminComponent,
      ),
  },
  {
    key: 'snapshots-admin',
    path: 'dashboard/snapshots',
    exposedModule: './DashboardSnapshotsAdmin',
    exportName: 'DashboardSnapshotsAdminComponent',
    routeRole: 'administrative',
    shellNavigation: 'access-menu',
    loadComponent: () =>
      import('./pages/snapshots/dashboard-snapshots-admin.component').then(
        m => m.DashboardSnapshotsAdminComponent,
      ),
  },
];

export const DASHBOARD_INTERNAL_ROUTES: Route[] = DASHBOARD_DOMAIN_MANIFEST.map(
  ({ path, loadComponent }) => ({
    path,
    loadComponent,
  }),
);
