import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'academic/periods',
    loadComponent: () =>
      import('./catalogo/pages/periods/academic-periods.component').then(
        m => m.AcademicPeriodsComponent,
      ),
  },
  {
    path: 'academic/series',
    loadComponent: () =>
      import('./catalogo/pages/series/academic-series.component').then(
        m => m.AcademicSeriesComponent,
      ),
  },
  {
    path: 'academic/shifts',
    loadComponent: () =>
      import('./catalogo/pages/shifts/academic-shifts.component').then(
        m => m.AcademicShiftsComponent,
      ),
  },
  {
    path: 'academic/classes',
    loadComponent: () =>
      import('./catalogo/pages/classes/academic-classes.component').then(
        m => m.AcademicClassesComponent,
      ),
  },
  {
    path: 'responsibles',
    loadComponent: () =>
      import('./responsavel/pages/list/responsibles-list.component').then(
        m => m.ResponsiblesListComponent,
      ),
  },
  {
    path: 'responsibles/new',
    loadComponent: () =>
      import('./responsavel/pages/new/responsibles-new.component').then(
        m => m.ResponsiblesNewComponent,
      ),
  },
  {
    path: 'responsibles/:id',
    loadComponent: () =>
      import('./responsavel/pages/detail/responsibles-detail.component').then(
        m => m.ResponsiblesDetailComponent,
      ),
  },
  {
    path: 'responsibles/:id/edit',
    loadComponent: () =>
      import('./responsavel/pages/new/responsibles-new.component').then(
        m => m.ResponsiblesNewComponent,
      ),
  },
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
];
