# Fase 45D - Catalogo oficial de widgets do dashboard

## Objetivo

Padronizar os codigos de widgets que controlam a Home operacional, reduzindo
ambiguidade entre a configuracao administrativa e a renderizacao personalizada
por usuario.

## Escopo implementado

- Frontend do microfrontend academico.
- Criado catalogo oficial de widgets em
  `dashboard-widget-catalog.ts`.
- Modal de cadastro/edicao de widget passou a oferecer o campo
  `Modelo oficial`.
- Ao selecionar um modelo oficial, o modal preenche:
  - codigo;
  - titulo;
  - tipo;
  - ordem;
  - referencia;
  - descricao.
- O usuario ADMIN ainda pode manter `Codigo livre` e editar os campos
  manualmente.
- A lista de modelos e filtrada pelo publico selecionado, mantendo tambem os
  widgets gerais.

## Codigos oficiais principais

- `TOTAL_MATRICULAS`
- `MATRICULAS_SOLICITADAS`
- `MATRICULAS_EM_ANDAMENTO`
- `DOCUMENTOS_PENDENTES`
- `TRANSFERENCIAS`
- `MATRICULAS_PENDENTES`
- `ALUNOS_ATIVOS`
- `TURMAS_ATIVAS`
- `MATRICULAS_CONCLUIDAS`
- `APTAS_REMATRICULA`
- `MATRICULAS_POR_STATUS`
- `VAGAS_DISPONIVEIS`
- `TENDENCIA_OPERACIONAL`
- `STATUS_DAS_MATRICULAS`
- `TURMAS_COM_VAGAS`
- `DOCUMENTOS_E_HISTORICO`
- `RESULTADO_ACADEMICO`
- `SETOR_ACADEMICO`
- `SETOR_ADMINISTRATIVO`
- `SETOR_PEDAGOGICO`
- `ALERTAS`

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- O catalogo oficial foi mantido no frontend nesta fase, pois a API atual ainda
  aceita configuracao livre de widgets.
- O uso do catalogo nao bloqueia codigos customizados; apenas guia o cadastro
  para os widgets que a Home ja sabe renderizar.

## Validacao

- Executar `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45E: criar um facilitador para provisionar dashboards padrao por perfil
usando o catalogo oficial, sem exigir que o ADMIN cadastre todos os widgets
manualmente.
