import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { authGuard } from './seguranca/guards/auth.guard';

function loadCatalogoRemoteComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

function loadResponsavelRemoteComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

function loadAlunoRemoteComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

function loadMatriculaRemoteComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

function loadHistoricoRemoteComponent(exposedModule: string, exportName: string) {
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
          loadAlunoRemoteComponent('./AlunoList', 'StudentsListComponent'),
      },
      {
        path: 'students/new',
        loadComponent: () =>
          loadAlunoRemoteComponent('./AlunoNew', 'StudentsNewComponent'),
      },
      {
        path: 'students/:id',
        loadComponent: () =>
          loadAlunoRemoteComponent('./AlunoDetail', 'StudentsDetailComponent'),
      },
      {
        path: 'students/:id/edit',
        loadComponent: () =>
          loadAlunoRemoteComponent('./AlunoNew', 'StudentsNewComponent'),
      },

      {
        path: 'responsibles',
        loadComponent: () =>
          loadResponsavelRemoteComponent('./ResponsavelList', 'ResponsiblesListComponent'),
      },
      {
        path: 'responsibles/new',
        loadComponent: () =>
          loadResponsavelRemoteComponent('./ResponsavelNew', 'ResponsiblesNewComponent'),
      },
      {
        path: 'responsibles/:id',
        loadComponent: () =>
          loadResponsavelRemoteComponent('./ResponsavelDetail', 'ResponsiblesDetailComponent'),
      },
      {
        path: 'responsibles/:id/edit',
        loadComponent: () =>
          loadResponsavelRemoteComponent('./ResponsavelNew', 'ResponsiblesNewComponent'),
      },
      {
        path: 'academic/periods',
        loadComponent: () =>
          loadCatalogoRemoteComponent('./CatalogoPeriods', 'AcademicPeriodsComponent'),
      },
      {
        path: 'academic/series',
        loadComponent: () =>
          loadCatalogoRemoteComponent('./CatalogoSeries', 'AcademicSeriesComponent'),
      },
      {
        path: 'academic/shifts',
        loadComponent: () =>
          loadCatalogoRemoteComponent('./CatalogoShifts', 'AcademicShiftsComponent'),
      },
      {
        path: 'academic/classes',
        loadComponent: () =>
          loadCatalogoRemoteComponent('./CatalogoClasses', 'AcademicClassesComponent'),
      },
      {
        path: 'academic/disciplines',
        loadComponent: () =>
          loadHistoricoRemoteComponent('./HistoricoDisciplines', 'DisciplinesComponent'),
      },
      {
        path: 'enrollment',
        loadComponent: () =>
          loadMatriculaRemoteComponent('./Matricula', 'EnrollmentNewComponent'),
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
