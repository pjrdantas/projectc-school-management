const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'mfe-catalogo-academico',

  exposes: {
    './CatalogoPeriods': './src/app/catalogo/exposes/catalog-periods.expose.ts',
    './CatalogoSeries': './src/app/catalogo/exposes/catalog-series.expose.ts',
    './CatalogoShifts': './src/app/catalogo/exposes/catalog-shifts.expose.ts',
    './CatalogoClasses': './src/app/catalogo/exposes/catalog-classes.expose.ts',
    './HistoricoDisciplines': './src/app/catalogo/exposes/catalog-disciplines.expose.ts',
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
