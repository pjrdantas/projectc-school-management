## Fase 43C - Migracao piloto do dominio catalogo para o microfrontend

Esta fase migra as telas de catalogo escolar para o microfrontend, mantendo o host como shell de seguranca e roteador federado.

### Escopo migrado

Foram movidas do host para o microfrontend as telas:

- periodos letivos;
- series;
- turnos;
- turmas.

Diretorio de origem:

```text
school-management-web/host/src/app/catalogo/pages
```

Diretorio de destino:

```text
school-management-web/microfrontend/src/app/catalogo/pages
```

### Escopo preservado no host

O host ainda mantem temporariamente:

```text
school-management-web/host/src/app/catalogo/models
school-management-web/host/src/app/catalogo/services
```

Motivo: a tela de matricula ainda consome `AcademicService` e `academic.model` para listar periodos e turmas. Essa dependencia deve ser removida em uma fase posterior, quando matricula tambem for migrada ou quando for criado um contrato de dados apropriado.

### Ajustes no microfrontend

O microfrontend recebeu:

- `catalogo/models`;
- `catalogo/services`;
- `catalogo/pages`;
- `core/http/api-error.ts`.

O `AcademicService` do microfrontend passou a usar `ShellContextService` para obter:

- `apiBaseUrl`;
- token de acesso.

### Exposicao federada

O `federation.config.js` do microfrontend passou a expor:

```text
./CatalogoPeriods
./CatalogoSeries
./CatalogoShifts
./CatalogoClasses
```

As rotas publicas no host foram preservadas:

```text
academic/periods
academic/series
academic/shifts
academic/classes
```

Porem agora carregam componentes remotos do `mfe1`.

### Validacao

- `npm run build` em `school-management-web/microfrontend` executado com sucesso.
- `npm run build` em `school-management-web/host` executado com sucesso.
- `remoteEntry.json` gerado no build do microfrontend contem os exports `CatalogoPeriods`, `CatalogoSeries`, `CatalogoShifts` e `CatalogoClasses`.

### Observacao operacional

A tentativa de validacao live com `ng serve` do microfrontend nao publicou a porta dentro da janela do comando automatizado, parando na preparacao de artefatos do Native Federation. Como o build federado gerou os exports esperados e o host compilou consumindo esses remotos, nenhuma alteracao em `angular.json` ou `federation.config.js` foi feita sem causa tecnica adicional.

### Proxima fase sugerida

Fase 43D - migrar o dominio `responsavel` ou preparar a migracao de `matricula`.

Recomendacao: migrar `responsavel` antes de `matricula`, pois matricula ainda depende de catalogo, aluno e responsavel.
