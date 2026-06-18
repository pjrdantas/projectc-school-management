import { Routes } from '@angular/router';
import { STUDENT_INTERNAL_ROUTES } from './aluno/student-domain';
import { RESPONSIBLE_INTERNAL_ROUTES } from './responsavel/responsible-domain';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'students',
  },
  ...RESPONSIBLE_INTERNAL_ROUTES,
  ...STUDENT_INTERNAL_ROUTES,
];
