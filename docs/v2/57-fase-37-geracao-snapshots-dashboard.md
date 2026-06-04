# Fase 37 - Geracao automatica de snapshots do dashboard

## Objetivo

Gerar snapshots de indicadores a partir dos dashboards em tempo real ja implementados, reduzindo a necessidade de carga manual por API.

## Endpoint

- `POST /api/dashboard/snapshots/geracoes/{publicoCodigo}`
- `POST /api/dashboard/snapshots/geracoes/{publicoCodigo}?referenciaData={yyyy-MM-dd}`

## Publicos suportados

- `ACADEMICO`
- `SECRETARIA`
- `DIRETOR`

## Decisoes

- A geracao reaproveita os services dos dashboards existentes e persiste cada indicador via service de snapshots da Fase 36.
- O processo e idempotente porque usa a chave funcional publico, codigo do indicador e data de referencia.
- O dashboard do professor nao entra nesta fase porque depende de `professorId`; ele deve ter geracao propria ou consolidacao especifica em fase posterior.
- O Swagger usa tag explicita `Dashboard snapshots`, sem sufixo `controller`.
- Nao houve alteracao em Maven, Flyway ou schema.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardSnapshotGeradorControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
