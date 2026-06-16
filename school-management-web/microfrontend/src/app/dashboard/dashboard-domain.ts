import { Provider } from '@angular/core';
import { Routes } from '@angular/router';

export const DASHBOARD_DOMAIN_PROVIDERS: Provider[] = [];

export const DASHBOARD_INTERNAL_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/academic-operational-dashboard.component').then(
        m => m.AcademicOperationalDashboardComponent,
      ),
  },
  {
    path: 'dashboard/config',
    loadComponent: () =>
      import('./pages/config/dashboard-config-admin.component').then(
        m => m.DashboardConfigAdminComponent,
      ),
  },
  {
    path: 'dashboard/snapshots',
    loadComponent: () =>
      import('./pages/snapshots/dashboard-snapshots-admin.component').then(
        m => m.DashboardSnapshotsAdminComponent,
      ),
  },
];

export { AcademicOperationalDashboardComponent } from './pages/academic-operational-dashboard.component';
export { DashboardConfigAdminComponent } from './pages/config/dashboard-config-admin.component';
export { DashboardSnapshotsAdminComponent } from './pages/snapshots/dashboard-snapshots-admin.component';
