# Fase 45L - Historico comparativo de snapshots no frontend

## Objetivo

Adicionar visualizacao historica comparativa dos snapshots de dashboard no
microfrontend, permitindo ao ADMIN analisar valor atual, valor anterior e
variacao percentual dos indicadores.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Snapshots de dashboard`.
- Consulta de historico usando:
  - `GET /api/dashboard/snapshots/historico/publicos/{publicoCodigo}`;
  - filtros opcionais `dataInicio`, `dataFim` e `professorId`.
- Novos filtros de periodo historico:
  - `Historico de`;
  - `Historico ate`.
- Nova secao `Historico comparativo`, exibindo:
  - valor atual;
  - valor anterior;
  - variacao percentual;
  - pontos historicos por data.
- Para o publico `PROFESSOR`, o historico exige professor selecionado para
  evitar misturar indicadores de docentes diferentes.
- Codigos de indicadores do professor continuam sem o prefixo tecnico na
  exibicao.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Sem exibicao de UUIDs para o usuario final.
- A consulta historica e atualizada junto com a consulta principal quando os
  filtros permitem.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45M: revisar no navegador a experiencia completa da tela de snapshots e
ajustar compactacao, textos e fluxo real de uso do ADMIN.
