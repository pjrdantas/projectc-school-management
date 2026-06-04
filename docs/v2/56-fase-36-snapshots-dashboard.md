# Fase 36 - Snapshots de indicadores do dashboard

## Objetivo

Persistir historico de indicadores por publico de dashboard, permitindo acompanhar valores por data de referencia.

## Endpoints

- `GET /api/dashboard/snapshots?publicoDashboardId={id}`
- `GET /api/dashboard/snapshots?publicoDashboardId={id}&referenciaData={yyyy-MM-dd}`
- `GET /api/dashboard/snapshots/publicos/{publicoCodigo}`
- `GET /api/dashboard/snapshots/publicos/{publicoCodigo}?referenciaData={yyyy-MM-dd}`
- `PUT /api/dashboard/snapshots`
- `DELETE /api/dashboard/snapshots/{id}`

## Campos

- `publicoDashboardId`
- `codigoIndicador`
- `descricao`
- `valorNumeric`
- `valorTexto`
- `referenciaData`

## Decisoes

- O `PUT` e idempotente para o par publico, codigo do indicador e data de referencia.
- Codigos de indicadores sao normalizados para maiusculo.
- A fase persiste snapshots manualmente via API; geracao automatica a partir dos dashboards em tempo real fica para fase posterior.
- O Swagger usa tag explicita `Dashboard snapshots`, sem sufixo `controller`.
- Nao houve alteracao em Maven, Flyway ou schema.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardIndicadorSnapshotControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
