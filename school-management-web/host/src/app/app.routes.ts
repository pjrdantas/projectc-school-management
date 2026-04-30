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
    pathMatch: 'full',
    redirectTo: 'auth/login',
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
        path: 'responsibles',
        loadComponent: () =>
          import('./responsibles/pages/list/responsibles-list.component').then(
            m => m.ResponsiblesListComponent,
          ),
      },
      {
        path: 'responsibles/new',
        loadComponent: () =>
          import('./responsibles/pages/new/responsibles-new.component').then(
            m => m.ResponsiblesNewComponent,
          ),
      },
      {
        path: 'responsibles/:id',
        loadComponent: () =>
          import('./responsibles/pages/detail/responsibles-detail.component').then(
            m => m.ResponsiblesDetailComponent,
          ),
      },
      {
        path: 'responsibles/:id/edit',
        loadComponent: () =>
          import('./responsibles/pages/new/responsibles-new.component').then(
            m => m.ResponsiblesNewComponent,
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
        path: 'enrollment',
        loadComponent: () =>
          import('./enrollment/pages/new/enrollment-new.component').then(
            m => m.EnrollmentNewComponent,
          ),
      },
      {
        path: 'auth/users',
        loadComponent: () =>
          import('./auth/pages/users/list/auth-users-list.component').then(
            m => m.AuthUsersListComponent,
          ),
      },


      {
        path: 'auth/users/new',
        loadComponent: () =>
          import('./auth/pages/users/new/auth-users-new.component').then(
            m => m.AuthUsersNewComponent,
          ),
      },
      {
        path: 'auth/users/detail',
        loadComponent: () =>
          import('./auth/pages/users/detail/auth-users-detail.component').then(
            m => m.AuthUsersDetailComponent,
          ),
      },
      {
        path: 'auth/users/edit',
        loadComponent: () =>
          import('./auth/pages/users/new/auth-users-new.component').then(
            m => m.AuthUsersNewComponent,
          ),
      },
      {
        path: 'auth/profiles',
        loadComponent: () =>
          import('./auth/pages/profiles/list/auth-profiles-list.component').then(
            m => m.AuthProfilesListComponent,
          ),
      },
      {
        path: 'auth/permissions',
        loadComponent: () =>
          import('./auth/pages/permissions/list/auth-permissions-list.component').then(
            m => m.AuthPermissionsListComponent,
          ),
      },

      {
        path: 'auth/profiles/new',
        loadComponent: () =>
          import('./auth/pages/profiles/new/auth-profiles-new.component').then(
            m => m.AuthProfilesNewComponent,
          ),
      },
      {
        path: 'auth/profiles/detail',
        loadComponent: () =>
          import('./auth/pages/profiles/detail/auth-profiles-detail.component').then(
            m => m.AuthProfilesDetailComponent,
          ),
      },
      {
        path: 'auth/profiles/edit',
        loadComponent: () =>
          import('./auth/pages/profiles/new/auth-profiles-new.component').then(
            m => m.AuthProfilesNewComponent,
          ),
      },
      {
        path: 'auth/permissions/new',
        loadComponent: () =>
          import('./auth/pages/permissions/new/auth-permissions-new.component').then(
            m => m.AuthPermissionsNewComponent,
          ),
      },
      {
        path: 'auth/permissions/detail',
        loadComponent: () =>
          import('./auth/pages/permissions/detail/auth-permissions-detail.component').then(
            m => m.AuthPermissionsDetailComponent,
          ),
      },
      {
        path: 'auth/permissions/edit',
        loadComponent: () =>
          import('./auth/pages/permissions/new/auth-permissions-new.component').then(
            m => m.AuthPermissionsNewComponent,
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
