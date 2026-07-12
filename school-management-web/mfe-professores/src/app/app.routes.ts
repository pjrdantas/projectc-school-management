import { Routes } from '@angular/router';
import { PROFESSOR_INTERNAL_ROUTES } from './professor/professor-domain.manifest';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'teachers',
  },
  ...PROFESSOR_INTERNAL_ROUTES,
];
