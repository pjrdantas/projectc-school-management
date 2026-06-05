const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'microfrontend',

  exposes: {
    './Dashboard': './src/app/dashboard/pages/academic-operational-dashboard.component.ts',
    './CatalogoPeriods': './src/app/catalogo/pages/periods/academic-periods.component.ts',
    './CatalogoSeries': './src/app/catalogo/pages/series/academic-series.component.ts',
    './CatalogoShifts': './src/app/catalogo/pages/shifts/academic-shifts.component.ts',
    './CatalogoClasses': './src/app/catalogo/pages/classes/academic-classes.component.ts',
    './ResponsavelList': './src/app/responsavel/pages/list/responsibles-list.component.ts',
    './ResponsavelNew': './src/app/responsavel/pages/new/responsibles-new.component.ts',
    './ResponsavelDetail': './src/app/responsavel/pages/detail/responsibles-detail.component.ts',
    './AlunoList': './src/app/aluno/pages/list/students-list.component.ts',
    './AlunoNew': './src/app/aluno/pages/new/students-new.component.ts',
    './AlunoDetail': './src/app/aluno/pages/detail/students-detail.component.ts',
    './Matricula': './src/app/matricula/pages/new/enrollment-new.component.ts',
    './HistoricoDisciplines': './src/app/historico/pages/disciplines/disciplines.component.ts',
  },

 shared: {
  ...shareAll({
    singleton: true,
    strictVersion: true,
    requiredVersion: 'auto',
  }),

  '@angular/material': { singleton: true },
  '@angular/cdk': { singleton: true },
  '@angular/cdk/a11y': { singleton: true },
},

skip: [
  'rxjs',
  'rxjs/ajax',
  'rxjs/fetch',
  'rxjs/testing',
  'rxjs/webSocket',
],

features: {
  ignoreUnusedDeps: true,
}

});
