import { Routes } from '@angular/router';
import { DASHBOARD_INTERNAL_ROUTES } from './dashboard/dashboard-domain';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  ...DASHBOARD_INTERNAL_ROUTES,
];
