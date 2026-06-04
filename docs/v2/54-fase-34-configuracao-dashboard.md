# Fase 34 - Configuracao de dashboards

## Objetivo

Criar API administrativa para configurar publicos, dashboards e widgets, preparando a base para configuracao por usuario e historizacao de indicadores.

## Endpoints

- `GET /api/dashboard/configuracoes/publicos`
- `POST /api/dashboard/configuracoes/publicos`
- `PUT /api/dashboard/configuracoes/publicos/{id}`
- `DELETE /api/dashboard/configuracoes/publicos/{id}`
- `GET /api/dashboard/configuracoes/dashboards`
- `GET /api/dashboard/configuracoes/dashboards?publicoCodigo={codigo}`
- `POST /api/dashboard/configuracoes/dashboards`
- `PUT /api/dashboard/configuracoes/dashboards/{id}`
- `DELETE /api/dashboard/configuracoes/dashboards/{id}`
- `GET /api/dashboard/configuracoes/dashboards/{dashboardId}/widgets`
- `POST /api/dashboard/configuracoes/widgets`
- `PUT /api/dashboard/configuracoes/widgets/{id}`
- `DELETE /api/dashboard/configuracoes/widgets/{id}`

## Decisoes

- Codigos sao normalizados para maiusculo.
- Dashboards mantem codigo unico global, conforme entidade atual.
- Widgets mantem unicidade por dashboard e codigo.
- O endpoint nao grava snapshots nem configuracao por usuario; esses itens ficam para fases posteriores.
- O Swagger usa tag explicita `Dashboard configuracao`, sem sufixo `controller`.
- Nao houve alteracao em Maven, Flyway ou schema.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardConfiguracaoAdminControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
