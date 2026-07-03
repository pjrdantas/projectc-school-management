import { Routes } from '@angular/router';
import { RESPONSIBLE_INTERNAL_ROUTES } from './responsavel/responsible-domain.manifest';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'responsibles',
  },
  ...RESPONSIBLE_INTERNAL_ROUTES,
];
