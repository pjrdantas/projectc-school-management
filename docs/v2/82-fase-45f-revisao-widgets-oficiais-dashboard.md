# Fase 45F - Revisao de widgets oficiais do dashboard

## Objetivo

Melhorar a experiencia do ADMIN antes de provisionar widgets padrao, mostrando
quais widgets oficiais ja existem e quais ainda faltam no dashboard selecionado.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Configuracao de dashboards`.
- Adicionada revisao visual dos widgets oficiais no painel de widgets.
- Cada item do catalogo oficial aparece com:
  - titulo;
  - codigo;
  - situacao `Criado` ou `Faltante`.
- A revisao usa os widgets carregados do dashboard selecionado e compara por
  `codigo`.
- O botao `Padrao` continua criando apenas os widgets faltantes.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- A revisao e informativa; nao altera widgets existentes.
- Widgets customizados continuam visiveis na lista principal de widgets, sem
  interferir na revisao dos oficiais.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45G: permitir que o ADMIN aplique atualizacao controlada de metadados dos
widgets oficiais existentes, sem sobrescrever customizacoes sem confirmacao.
