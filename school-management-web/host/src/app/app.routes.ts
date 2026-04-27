import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { authGuard, guestGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth/login',
    canMatch: [guestGuard],
    loadComponent: () =>
      import('./auth/pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    canMatch: [authGuard],
    loadComponent: () => import('./menu/pages/menu/menu').then(m => m.Menu),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home',
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./home/pages/home/home.component').then(m => m.HomeComponent),
      },
      {
        path: 'students',
        loadComponent: () =>
          import('./students/pages/list/students-list.component').then(
            m => m.StudentsListComponent,
          ),
      },
      {
        path: 'students/new',
        loadComponent: () =>
          import('./students/pages/new/students-new.component').then(
            m => m.StudentsNewComponent,
          ),
      },
      {
        path: 'students/:id',
        loadComponent: () =>
          import('./students/pages/detail/students-detail.component').then(
            m => m.StudentsDetailComponent,
          ),
      },
      {
        path: 'students/:id/edit',
        loadComponent: () =>
          import('./students/pages/new/students-new.component').then(
            m => m.StudentsNewComponent,
          ),
      },
      {
        path: 'academic/periods',
        loadComponent: () =>
          import('./academic/pages/periods/academic-periods.component').then(
            m => m.AcademicPeriodsComponent,
          ),
      },
      {
        path: 'academic/classes',
        loadComponent: () =>
          import('./academic/pages/classes/academic-classes.component').then(
            m => m.AcademicClassesComponent,
          ),
      },
      {
        path: 'enrollment/new',
        loadComponent: () =>
          import('./enrollment/pages/new/enrollment-new.component').then(
            m => m.EnrollmentNewComponent,
          ),
      },
      {
        path: 'enrollment/search',
        loadComponent: () =>
          import('./enrollment/pages/search/enrollment-search.component').then(
            m => m.EnrollmentSearchComponent,
          ),
      },
      {
        path: 'auth/users',
        loadComponent: () =>
          import('./auth/pages/users/auth-users.component').then(
            m => m.AuthUsersComponent,
          ),
      },
      {
        path: 'microfrontend',
        loadComponent: () =>
          loadRemoteModule('mfe1', './Component').then(m => m.HomeComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'auth/login',
  },
];
