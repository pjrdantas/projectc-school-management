# Fase 45G - Atualizacao de metadados de widgets oficiais

## Objetivo

Permitir que o ADMIN alinhe widgets oficiais ja existentes ao catalogo atual,
sem recriar widgets e sem sobrescrever status operacional sem confirmacao.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Configuracao de dashboards`.
- A revisao de widgets oficiais agora indica tambem a situacao `Divergente`.
- Foi adicionado o botao `Atualizar` no painel de widgets.
- O botao atualiza apenas widgets oficiais existentes cujo metadado diverge do
  catalogo.
- Antes de atualizar, o ADMIN precisa confirmar a acao.
- A atualizacao alinha:
  - titulo;
  - descricao;
  - tipo;
  - ordem;
  - referencia.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- O campo `ativo` e preservado para respeitar a decisao operacional vigente.
- Widgets customizados continuam fora da rotina de atualizacao oficial.
- Widgets oficiais faltantes continuam sendo criados pelo botao `Padrao`.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45H: revisar a experiencia completa de configuracao de dashboards no
navegador e ajustar textos, compactacao e fluxo conforme uso real do ADMIN.
