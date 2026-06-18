const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'mfe-planejamento-ia',

  exposes: {
    './PlanningList': './src/app/professor/exposes/planning-list.expose.ts',
    './PlanningDetail': './src/app/professor/exposes/planning-detail.expose.ts',
    './PlanningLibrary': './src/app/professor/exposes/planning-library.expose.ts',
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
