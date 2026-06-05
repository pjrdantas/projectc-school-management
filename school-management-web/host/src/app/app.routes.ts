import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { authGuard } from './seguranca/guards/auth.guard';
import { SHELL_REMOTE_ROUTES } from './core/shell/shell-navigation.config';

function loadMfeComponent(exposedModule: string, exportName: string) {
  return loadRemoteModule('mfe1', exposedModule).then(m => m[exportName]);
}

const remoteRoutes: Routes = SHELL_REMOTE_ROUTES.map(route => ({
  path: route.path,
  loadComponent: () => loadMfeComponent(route.exposedModule, route.exportName),
}));

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
      ...remoteRoutes,
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
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
