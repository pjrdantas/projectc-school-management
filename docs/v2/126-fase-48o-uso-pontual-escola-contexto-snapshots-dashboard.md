# Fase 48O - Uso pontual de EscolaContextoPort nos snapshots de dashboard

## Objetivo

Aplicar `EscolaContextoPort` em `DashboardIndicadorSnapshotService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em listagem, historico e persistencia de snapshots de indicadores.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `DashboardIndicadorSnapshotService` passou a depender de `EscolaContextoPort`.
- A listagem de snapshots passou a resolver `escolaId` por `obterContextoPadrao()`.
- A consulta de historico passou a resolver `escolaId` por `obterContextoPadrao()`.
- A criacao e atualizacao de snapshots passaram a obter o contexto escolar pela porta interna.
- A persistencia continua usando `EscolaEntity` apenas como referencia JPA necessaria para gravar o relacionamento existente.
- Os filtros por publico, indicador, data e professor foram preservados.

## Decisoes

- Os contratos publicos de snapshots foram preservados.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla de snapshots ou dashboards.
- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao do contexto escolar.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=DashboardIndicadorSnapshotControllerIntegrationTest,DashboardSnapshotAgendamentoSchedulerTest" test`
- `.\mvnw.cmd test`

## Resultado

Os snapshots de indicadores passam a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico, secretaria, diretor e professor sem alterar comportamento externo.

## Estado apos Fase 48P

A Fase 48P consolidou a area de dashboards e snapshots como consumidora de `EscolaContextoPort`, sem novos ajustes de codigo. A proxima etapa sugerida passa a mirar um fluxo de leitura fora de dashboards.

## Proxima fase sugerida

Fase 48P - consolidacao dos usos de `EscolaContextoPort` em dashboards e snapshots.

Objetivo sugerido:

- Consolidar a matriz documental dos consumidores de `EscolaContextoPort` na area de dashboards.
- Confirmar que nao ha uso direto remanescente de `EscolaTenantService` nos services de dashboard.
- Identificar o proximo candidato fora da area de dashboards apenas se houver baixo risco.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48P

Fase 48Q - uso pontual de `EscolaContextoPort` no resumo academico da matricula.
