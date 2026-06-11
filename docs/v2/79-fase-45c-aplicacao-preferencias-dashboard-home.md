# Fase 45C - Aplicacao das preferencias na Home

## Objetivo

Aplicar as preferencias salvas pelo usuario diretamente nos blocos visuais da
Home operacional.

## Escopo implementado

- Frontend do microfrontend academico.
- Cards de metricas passam a respeitar visibilidade e ordem quando houver widget
  configurado correspondente.
- Graficos passam a respeitar visibilidade e ordem:
  - matriculas por status;
  - vagas disponiveis;
  - tendencia operacional.
- Listas e paineis passam a respeitar visibilidade e ordem:
  - status das matriculas;
  - turmas com vagas;
  - documentos e historico;
  - resultado academico;
  - setores da direcao;
  - alertas.
- O mapeamento usa codigo, titulo, descricao, tipo e referencia do widget,
  normalizados sem acentos, para tolerar nomes cadastrados pela administracao.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Quando nao houver configuracao de dashboard ou quando um widget cadastrado nao
  corresponder a um bloco visual conhecido, a Home mantem o comportamento padrao.
- A configuracao administrativa segue livre, mas para afetar a Home o widget deve
  usar codigos ou titulos semanticamente proximos aos blocos existentes.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45D: padronizar e documentar uma lista oficial de codigos de widgets para
os dashboards padrao por perfil, reduzindo ambiguidades no cadastro
administrativo.
