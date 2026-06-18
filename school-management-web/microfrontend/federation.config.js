const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'microfrontend',

  exposes: {
    './ResponsavelList': './src/app/responsavel/exposes/responsible-list.expose.ts',
    './ResponsavelNew': './src/app/responsavel/exposes/responsible-new.expose.ts',
    './ResponsavelDetail': './src/app/responsavel/exposes/responsible-detail.expose.ts',
    './ResponsavelEdit': './src/app/responsavel/exposes/responsible-edit.expose.ts',
    './AlunoList': './src/app/aluno/exposes/student-list.expose.ts',
    './AlunoNew': './src/app/aluno/exposes/student-new.expose.ts',
    './AlunoDetail': './src/app/aluno/exposes/student-detail.expose.ts',
    './AlunoEdit': './src/app/aluno/exposes/student-edit.expose.ts',
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
