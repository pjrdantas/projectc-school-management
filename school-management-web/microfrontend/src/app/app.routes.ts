import { Routes } from '@angular/router';
import { CATALOG_INTERNAL_ROUTES } from './catalogo/catalog-domain';
import { DASHBOARD_INTERNAL_ROUTES } from './dashboard/dashboard-domain';
import { PLANNING_INTERNAL_ROUTES } from './professor/planning-domain';
import { PROFESSOR_INTERNAL_ROUTES } from './professor/professor-domain';
import { TEACHING_INTERNAL_ROUTES } from './professor/teaching-domain';
import { RESPONSIBLE_INTERNAL_ROUTES } from './responsavel/responsible-domain';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  ...DASHBOARD_INTERNAL_ROUTES,
  ...CATALOG_INTERNAL_ROUTES,
  ...RESPONSIBLE_INTERNAL_ROUTES,
  {
    path: 'students',
    loadComponent: () =>
      import('./aluno/pages/list/students-list.component').then(
        m => m.StudentsListComponent,
      ),
  },
  {
    path: 'students/new',
    loadComponent: () =>
      import('./aluno/pages/new/students-new.component').then(
        m => m.StudentsNewComponent,
      ),
  },
  {
    path: 'students/:id',
    loadComponent: () =>
      import('./aluno/pages/detail/students-detail.component').then(
        m => m.StudentsDetailComponent,
      ),
  },
  {
    path: 'students/:id/edit',
    loadComponent: () =>
      import('./aluno/pages/new/students-new.component').then(
        m => m.StudentsNewComponent,
      ),
  },
  {
    path: 'enrollment',
    loadComponent: () =>
      import('./matricula/pages/new/enrollment-new.component').then(
        m => m.EnrollmentNewComponent,
      ),
  },
  ...PROFESSOR_INTERNAL_ROUTES,
  ...TEACHING_INTERNAL_ROUTES,
  ...PLANNING_INTERNAL_ROUTES,
];
