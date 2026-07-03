import { Routes } from '@angular/router';
import { TEACHING_INTERNAL_ROUTES } from './professor/teaching-domain.manifest';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'lessons',
  },
  ...TEACHING_INTERNAL_ROUTES,
];
