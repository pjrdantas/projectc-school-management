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
    './ProfessorList': './src/app/professor/pages/list/teachers-list.component.ts',
    './ProfessorDetail': './src/app/professor/pages/detail/teacher-detail.component.ts',
    './LessonList': './src/app/professor/pages/lessons/lessons-list.component.ts',
    './LessonDetail': './src/app/professor/pages/lessons/lesson-detail.component.ts',
    './AssessmentList': './src/app/professor/pages/assessments/assessments-list.component.ts',
    './AssessmentDetail': './src/app/professor/pages/assessments/assessment-detail.component.ts',
    './PlanningList': './src/app/professor/pages/planning/planning-list.component.ts',
    './PlanningDetail': './src/app/professor/pages/planning/planning-detail.component.ts',
    './DashboardConfigAdmin': './src/app/dashboard/pages/config/dashboard-config-admin.component.ts',
    './DashboardSnapshotsAdmin': './src/app/dashboard/pages/snapshots/dashboard-snapshots-admin.component.ts',
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
