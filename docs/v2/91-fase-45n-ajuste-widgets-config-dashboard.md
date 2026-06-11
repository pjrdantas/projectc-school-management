# Fase 45N - Ajuste visual de widgets e titulos de dashboard

## Objetivo

Corrigir a leitura dos widgets na tela `Configuracao de dashboards` e trocar a
terminologia visivel de Home para Dashboard nas telas operacionais.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Configuracao de dashboards`:
  - painel de widgets ganhou mais largura na grade;
  - cards da revisao de widgets oficiais ficaram maiores;
  - textos longos de codigo, titulo e descricao passam a quebrar dentro do
    proprio card;
  - status do widget deixa de sobrepor os textos.
- Tela de dashboard operacional:
  - `Home da secretaria` passou para `Dashboard da secretaria`;
  - `Home da direcao` passou para `Dashboard da direcao`;
  - `Home do professor` passou para `Dashboard do professor`;
  - `Home academica` passou para `Dashboard academica`;
  - mensagens internas tambem passaram a usar dashboard.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- O ajuste ficou restrito a estilo/texto do microfrontend.
- A grafia `Dashboard academica` segue a nomenclatura solicitada para substituir
  `Home academica`.

## Validacao

- `npm run build` em `school-management-web/microfrontend`, sem warning de
  budget.
- Validacao no navegador:
  - `/dashboard/config` carregou a tela `Configuracao de dashboards`;
  - widgets oficiais renderizados sem texto fora do card;
  - medicao DOM retornou `overflowWidgets: 0`;
  - `/dashboard` renderizou `Dashboard academica`;
  - nenhum texto `Home` apareceu no corpo do dashboard.

## Proxima fase sugerida

Continuar a revisao funcional da Fase 45N com a massa de testes, retomando os
fluxos de cadastro/edicao/exclusao de aluno e responsavel.
