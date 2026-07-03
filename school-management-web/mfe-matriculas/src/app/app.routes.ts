import { Routes } from '@angular/router';
import { ENROLLMENT_INTERNAL_ROUTES } from './matricula/enrollment-domain.manifest';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'enrollment',
  },
  ...ENROLLMENT_INTERNAL_ROUTES,
];
