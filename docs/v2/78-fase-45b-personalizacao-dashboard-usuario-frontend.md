# Fase 45B - Personalizacao de dashboard por usuario

## Objetivo

Permitir que cada usuario ajuste a exibicao dos widgets do dashboard operacional
sem alterar a configuracao global criada pela administracao.

## Escopo implementado

- Frontend do microfrontend academico.
- Leitura dos dashboards e widgets recebidos no payload agregado da Home.
- Leitura das configuracoes individuais recebidas em `configuracoesUsuario`.
- Painel de preferencias na Home operacional quando houver widgets configurados.
- Alteracao de visibilidade por widget.
- Alteracao de ordem por widget.
- Restauracao da preferencia individual para voltar ao padrao global.

## Contratos utilizados

- `PUT /api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao`
- `DELETE /api/dashboard/usuarios/{usuarioId}/widgets/{dashboardWidgetId}/configuracao`

Nao houve alteracao backend nesta fase, pois os contratos e DTOs ja estavam
disponiveis.

## Validacao esperada

- Executar `npm run build` em `school-management-web/microfrontend`.
- Nao e necessario executar `.\mvnw.cmd test` quando nao houver mudanca backend.

## Proxima fase sugerida

Fase 45C: aplicar as preferencias salvas diretamente na renderizacao visual dos
widgets, alinhando os codigos cadastrados na configuracao administrativa com os
blocos reais exibidos na Home por perfil.
