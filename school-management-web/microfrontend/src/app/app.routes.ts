import { Routes } from '@angular/router';
import { DASHBOARD_INTERNAL_ROUTES } from './dashboard/dashboard-domain';
import { STUDENT_INTERNAL_ROUTES } from './aluno/student-domain';
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
  ...RESPONSIBLE_INTERNAL_ROUTES,
  ...STUDENT_INTERNAL_ROUTES,
  ...PROFESSOR_INTERNAL_ROUTES,
  ...TEACHING_INTERNAL_ROUTES,
  ...PLANNING_INTERNAL_ROUTES,
];
