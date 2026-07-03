import { Routes } from '@angular/router';
import { CATALOG_INTERNAL_ROUTES } from './catalogo/catalog-domain.manifest';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'academic/periods',
  },
  ...CATALOG_INTERNAL_ROUTES,
];
