import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { authGuard } from './seguranca/guards/auth.guard';

function loadMfeComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

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
          loadMfeComponent('./AlunoList', 'StudentsListComponent'),
      },
      {
        path: 'students/new',
        loadComponent: () =>
          loadMfeComponent('./AlunoNew', 'StudentsNewComponent'),
      },
      {
        path: 'students/:id',
        loadComponent: () =>
          loadMfeComponent('./AlunoDetail', 'StudentsDetailComponent'),
      },
      {
        path: 'students/:id/edit',
        loadComponent: () =>
          loadMfeComponent('./AlunoNew', 'StudentsNewComponent'),
      },

      {
        path: 'responsibles',
        loadComponent: () =>
          loadMfeComponent('./ResponsavelList', 'ResponsiblesListComponent'),
      },
      {
        path: 'responsibles/new',
        loadComponent: () =>
          loadMfeComponent('./ResponsavelNew', 'ResponsiblesNewComponent'),
      },
      {
        path: 'responsibles/:id',
        loadComponent: () =>
          loadMfeComponent('./ResponsavelDetail', 'ResponsiblesDetailComponent'),
      },
      {
        path: 'responsibles/:id/edit',
        loadComponent: () =>
          loadMfeComponent('./ResponsavelNew', 'ResponsiblesNewComponent'),
      },
      {
        path: 'academic/periods',
        loadComponent: () =>
          loadMfeComponent('./CatalogoPeriods', 'AcademicPeriodsComponent'),
      },
      {
        path: 'academic/series',
        loadComponent: () =>
          loadMfeComponent('./CatalogoSeries', 'AcademicSeriesComponent'),
      },
      {
        path: 'academic/shifts',
        loadComponent: () =>
          loadMfeComponent('./CatalogoShifts', 'AcademicShiftsComponent'),
      },
      {
        path: 'academic/classes',
        loadComponent: () =>
          loadMfeComponent('./CatalogoClasses', 'AcademicClassesComponent'),
      },
      {
        path: 'academic/disciplines',
        loadComponent: () =>
          loadMfeComponent('./HistoricoDisciplines', 'DisciplinesComponent'),
      },
      {
        path: 'enrollment',
        loadComponent: () =>
          loadMfeComponent('./Matricula', 'EnrollmentNewComponent'),
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
          loadMfeComponent('./Component', 'HomeComponent'),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
