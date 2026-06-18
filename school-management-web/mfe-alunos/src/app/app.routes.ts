import { Routes } from '@angular/router';
import { STUDENT_INTERNAL_ROUTES } from './aluno/student-domain';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'students',
  },
  ...STUDENT_INTERNAL_ROUTES,
];
