# Fase 45I - Administracao de snapshots de dashboard no frontend

## Objetivo

Iniciar a administracao de snapshots de dashboard no microfrontend, permitindo
ao ADMIN consultar indicadores historizados por publico e data de referencia.

## Escopo implementado

- Frontend do microfrontend academico.
- Nova tela `Snapshots de dashboard`.
- Nova rota federada `dashboard/snapshots`.
- Novo item de menu ADMIN `Snapshots`.
- Consulta de publicos pela configuracao de dashboard existente.
- Consulta de snapshots por codigo de publico usando:
  - `GET /api/dashboard/snapshots/publicos/{publicoCodigo}`;
  - `GET /api/dashboard/snapshots/publicos/{publicoCodigo}?referenciaData={yyyy-MM-dd}`.
- Exibicao compacta de resumo:
  - total de indicadores;
  - indicadores numericos;
  - indicadores textuais;
  - referencia consultada.
- Lista de snapshots com indicador, publico, valor e data.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Sem edicao, exclusao ou geracao manual de snapshots nesta fase.
- UUIDs de snapshots e publicos permanecem apenas internos nas chamadas e nao
  sao exibidos para o usuario final.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- `npm run build` em `school-management-web/host`.

## Proxima fase sugerida

Fase 45J: adicionar a acao administrativa para gerar snapshots sob demanda por
publico e data, reaproveitando os endpoints de geracao ja existentes.
