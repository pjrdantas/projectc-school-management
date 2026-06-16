const { withNativeFederation, shareAll } =
  require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'microfrontend',

  exposes: {
    './Dashboard': './src/app/dashboard/exposes/dashboard-operational.expose.ts',
    './CatalogoPeriods': './src/app/catalogo/exposes/catalog-periods.expose.ts',
    './CatalogoSeries': './src/app/catalogo/exposes/catalog-series.expose.ts',
    './CatalogoShifts': './src/app/catalogo/exposes/catalog-shifts.expose.ts',
    './CatalogoClasses': './src/app/catalogo/exposes/catalog-classes.expose.ts',
    './ResponsavelList': './src/app/responsavel/exposes/responsible-list.expose.ts',
    './ResponsavelNew': './src/app/responsavel/exposes/responsible-new.expose.ts',
    './ResponsavelDetail': './src/app/responsavel/exposes/responsible-detail.expose.ts',
    './ResponsavelEdit': './src/app/responsavel/exposes/responsible-edit.expose.ts',
    './AlunoList': './src/app/aluno/exposes/student-list.expose.ts',
    './AlunoNew': './src/app/aluno/exposes/student-new.expose.ts',
    './AlunoDetail': './src/app/aluno/exposes/student-detail.expose.ts',
    './AlunoEdit': './src/app/aluno/exposes/student-edit.expose.ts',
    './Matricula': './src/app/matricula/exposes/enrollment.expose.ts',
    './HistoricoDisciplines': './src/app/catalogo/exposes/catalog-disciplines.expose.ts',
    './ProfessorList': './src/app/professor/exposes/professor-list.expose.ts',
    './ProfessorDetail': './src/app/professor/exposes/professor-detail.expose.ts',
    './LessonList': './src/app/professor/exposes/lesson-list.expose.ts',
    './LessonDetail': './src/app/professor/exposes/lesson-detail.expose.ts',
    './AssessmentList': './src/app/professor/exposes/assessment-list.expose.ts',
    './AssessmentDetail': './src/app/professor/exposes/assessment-detail.expose.ts',
    './PlanningList': './src/app/professor/exposes/planning-list.expose.ts',
    './PlanningDetail': './src/app/professor/exposes/planning-detail.expose.ts',
    './PlanningLibrary': './src/app/professor/exposes/planning-library.expose.ts',
    './DashboardConfigAdmin': './src/app/dashboard/exposes/dashboard-config-admin.expose.ts',
    './DashboardSnapshotsAdmin': './src/app/dashboard/exposes/dashboard-snapshots-admin.expose.ts',
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
