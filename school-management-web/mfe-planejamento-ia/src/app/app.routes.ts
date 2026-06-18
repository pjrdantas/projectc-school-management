import { Routes } from '@angular/router';
import { PLANNING_INTERNAL_ROUTES } from './professor/planning-domain';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'planning',
  },
  ...PLANNING_INTERNAL_ROUTES,
];
