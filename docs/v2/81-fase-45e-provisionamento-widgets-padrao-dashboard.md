# Fase 45E - Provisionamento de widgets padrao

## Objetivo

Facilitar a configuracao inicial de dashboards por perfil, permitindo que o
ADMIN crie os widgets oficiais faltantes sem cadastrar cada item manualmente.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Configuracao de dashboards`.
- Adicionado botao `Padrao` no painel de widgets.
- O botao cria automaticamente os widgets oficiais do catalogo aplicaveis ao
  publico selecionado.
- O provisionamento compara os widgets existentes por `codigo` e cria apenas os
  ausentes.
- O painel informa quantos widgets padrao ainda faltam no dashboard selecionado.
- Durante o provisionamento, a barra de progresso do painel de widgets e exibida
  e o botao fica bloqueado.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- O provisionamento usa os endpoints existentes de criacao de widget.
- Widgets existentes nao sao atualizados nem sobrescritos nesta fase.
- Widgets customizados continuam permitidos.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45F: melhorar a experiencia de revisao dos widgets provisionados, com
indicacao visual de quais widgets oficiais ja existem e quais ainda faltam antes
de executar o provisionamento.
