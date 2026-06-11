# Fase 45J - Geracao manual de snapshots de dashboard

## Objetivo

Permitir que o ADMIN gere snapshots de dashboard sob demanda por publico e data
de referencia, reaproveitando os endpoints de geracao ja existentes no backend.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Snapshots de dashboard`.
- Adicionada acao `Gerar snapshot`.
- A geracao usa:
  - `POST /api/dashboard/snapshots/geracoes/{publicoCodigo}`;
  - `POST /api/dashboard/snapshots/geracoes/{publicoCodigo}?referenciaData={yyyy-MM-dd}`.
- A acao exige confirmacao antes de chamar o backend.
- A lista de snapshots e recarregada ao final da geracao.
- O publico `PROFESSOR` fica bloqueado na tela, pois o backend exige
  `professorId` especifico para esse caso.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Sem exibicao de UUIDs para o usuario final.
- A geracao por professor fica para fase propria, com selecao explicita de
  professor.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45K: adicionar geracao de snapshots do professor, com selecao de professor
e data de referencia, usando o endpoint dedicado por `professorId`.
