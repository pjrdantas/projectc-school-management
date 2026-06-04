# Fase 35 - Configuracao de dashboard por usuario

## Objetivo

Permitir que cada usuario autenticado personalize widgets dos dashboards ja configurados por publico.

## Usuario

Nesta fase, usuario significa `UsuarioEntity` do modulo de seguranca. O publico do dashboard continua sendo `PROFESSOR`, `SECRETARIA` ou `DIRETOR`; a configuracao por usuario salva preferencias individuais sobre os widgets desses dashboards.

## Endpoints

- `GET /api/dashboard/usuarios/{usuarioId}/configuracoes`
- `GET /api/dashboard/usuarios/{usuarioId}/configuracoes?dashboardId={dashboardId}`
- `PUT /api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao`
- `DELETE /api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao`

## Campos configuraveis

- `visivel`
- `ordem`
- `configuracaoJson`

## Decisoes

- Foi adicionada a coluna `configuracao_json` para guardar preferencias avancadas de widgets por usuario.
- O campo `configuracaoJson` e validado pela API e armazenado em formato JSON normalizado.
- O `PUT` e idempotente: cria a configuracao se ela nao existir e atualiza se ja existir para o par usuario/widget.
- A listagem retorna apenas configuracoes explicitamente salvas para o usuario.
- O Swagger usa tag explicita `Dashboard usuario`, sem sufixo `controller`.
- Houve alteracao controlada de schema via migration Flyway incremental.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardUsuarioConfiguracaoControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
