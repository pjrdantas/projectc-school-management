# Fase 48N - Uso pontual de EscolaContextoPort no dashboard professor

## Objetivo

Aplicar `EscolaContextoPort` em `DashboardProfessorService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de leitura filtrado por professor.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `DashboardProfessorService` passou a depender de `EscolaContextoPort`.
- O contexto escolar padrao passou a ser resolvido por `obterContextoPadrao()`.
- `escolaId` e `escolaNome` usados no response passaram a vir de `EscolaContexto`.
- A validacao de existencia do professor por escola foi preservada.
- O filtro das alocacoes por escola foi preservado.
- As consultas agregadas de aulas, frequencias, avaliacoes e planejamentos bimestrais continuam usando os repositories existentes.

## Decisoes

- O contrato publico de `/api/dashboard/professor/{professorId}` foi preservado.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla dos dashboards.
- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao do contexto escolar.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=DashboardProfessorControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

O dashboard professor passa a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico, secretaria e diretor sem alterar comportamento externo.

## Proxima fase sugerida

Fase 48O - uso pontual de `EscolaContextoPort` nos snapshots de indicadores de dashboard.

Objetivo sugerido:

- Avaliar `DashboardIndicadorSnapshotService` como proximo consumidor.
- Aplicar `EscolaContextoPort` apenas no ponto de resolucao de escola.
- Preservar contratos HTTP e comportamento de listagem, historico, criacao e exclusao de snapshots.
- Validar com `.\mvnw.cmd "-Dtest=DashboardIndicadorSnapshotControllerIntegrationTest,DashboardSnapshotAgendamentoSchedulerTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.
