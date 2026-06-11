# Fase 45K - Geracao de snapshots do professor

## Objetivo

Permitir que o ADMIN gere snapshots do dashboard de professor selecionando um
professor e uma data de referencia.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Snapshots de dashboard`.
- Carregamento de professores ativos para o filtro do publico `PROFESSOR`.
- Geracao manual de snapshots do professor usando:
  - `POST /api/dashboard/snapshots/geracoes/professores/{professorId}`;
  - `POST /api/dashboard/snapshots/geracoes/professores/{professorId}?referenciaData={yyyy-MM-dd}`.
- Consulta de snapshots do publico `PROFESSOR` com filtro local pelo professor
  selecionado.
- O codigo exibido dos indicadores de professor remove o prefixo tecnico que
  contem o identificador interno do professor.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- UUIDs de professor continuam apenas como valores internos de chamada e nao
  sao exibidos para o usuario final.
- A consulta sem professor selecionado continua listando todos os snapshots do
  publico `PROFESSOR`; a geracao exige professor selecionado.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45L: adicionar visualizacao historica comparativa dos snapshots no
microfrontend, consumindo `GET /api/dashboard/snapshots/historico/publicos/{publicoCodigo}`.
