const { withNativeFederation, share, shareAll } = require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'host',


 shared: {
  ...shareAll({
    singleton: true,
    strictVersion: true,
    requiredVersion: 'auto',
  }),
  ...share({
    '@angular/material': {
      singleton: true,
      strictVersion: true,
      requiredVersion: 'auto',
      includeSecondaries: { keepAll: true, skip: [] },
    },
    '@angular/cdk': {
      singleton: true,
      strictVersion: true,
      requiredVersion: 'auto',
      includeSecondaries: { keepAll: true, skip: [] },
    },
  }),
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
