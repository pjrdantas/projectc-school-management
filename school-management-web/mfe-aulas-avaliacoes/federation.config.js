const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'mfe-aulas-avaliacoes',

  exposes: {
    './LessonList': './src/app/professor/exposes/lesson-list.expose.ts',
    './LessonDetail': './src/app/professor/exposes/lesson-detail.expose.ts',
    './AssessmentList': './src/app/professor/exposes/assessment-list.expose.ts',
    './AssessmentDetail': './src/app/professor/exposes/assessment-detail.expose.ts',
    './ClassDiary': './src/app/professor/exposes/diario-classe.expose.ts',
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
