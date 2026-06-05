import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { authGuard } from './seguranca/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./seguranca/pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./seguranca/pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
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
        path: 'academic/disciplines',
        loadComponent: () =>
          import('./historico/pages/disciplines/disciplines.component').then(
            m => m.DisciplinesComponent,
          ),
      },
      {
        path: 'enrollment',
        loadComponent: () =>
          import('./matricula/pages/new/enrollment-new.component').then(
            m => m.EnrollmentNewComponent,
          ),
      },
      {
        path: 'auth/users',
        loadComponent: () =>
          import('./seguranca/pages/users/usuarios.component').then(
            m => m.UsuariosComponent,
          ),
      },


      {
        path: 'auth/users/new',
        loadComponent: () =>
          import('./seguranca/pages/users/usuarios.component').then(
            m => m.UsuariosComponent,
          ),
      },
      {
        path: 'auth/users/detail',
        loadComponent: () =>
          import('./seguranca/pages/users/usuarios.component').then(
            m => m.UsuariosComponent,
          ),
      },
      {
        path: 'auth/users/edit',
        loadComponent: () =>
          import('./seguranca/pages/users/usuarios.component').then(
            m => m.UsuariosComponent,
          ),
      },
      {
        path: 'auth/profiles',
        loadComponent: () =>
          import('./seguranca/pages/profiles/perfis.component').then(
            m => m.PerfisComponent,
          ),
      },
      {
        path: 'auth/permissions',
        loadComponent: () =>
          import('./seguranca/pages/permissions/permissoes.component').then(
            m => m.PermissoesComponent,
          ),
      },

      {
        path: 'auth/profiles/new',
        loadComponent: () =>
          import('./seguranca/pages/profiles/perfis.component').then(
            m => m.PerfisComponent,
          ),
      },
      {
        path: 'auth/profiles/detail',
        loadComponent: () =>
          import('./seguranca/pages/profiles/perfis.component').then(
            m => m.PerfisComponent,
          ),
      },
      {
        path: 'auth/profiles/edit',
        loadComponent: () =>
          import('./seguranca/pages/profiles/perfis.component').then(
            m => m.PerfisComponent,
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
    redirectTo: 'login',
  },
];
