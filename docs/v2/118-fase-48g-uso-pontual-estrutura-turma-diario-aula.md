# Fase 48G - Uso pontual de EstruturaTurmaPort no diario de aula

## Objetivo

Aplicar o uso pontual de `EstruturaTurmaPort` no fluxo de diario de aula, mantendo a estrategia incremental definida na Fase 48F.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `DiarioAulaService` passou a depender de `EstruturaTurmaPort`.
- A criacao de aula agora valida se a turma da alocacao realmente possui a disciplina informada.
- A validacao usa `escolaId`, `turmaId` e `disciplinaId` de forma explicita.
- Quando a estrutura academica nao confirma a relacao turma-disciplina, o fluxo preserva a excecao de dominio ja usada para alocacao inexistente.

## Decisoes

- O contrato publico de criacao de aula foi preservado.
- Nao houve alteracao em controllers, requests ou responses.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla do diario de aula.
- O uso de `EstruturaTurmaPort` ficou limitado ao ponto de criacao da aula.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AulaControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

O diario de aula passa a reutilizar a fronteira interna de estrutura academica em um ponto transacional de baixo risco, alinhando o fluxo com o mesmo criterio adotado anteriormente no planejamento bimestral.

## Proxima fase sugerida

Fase 48H - uso pontual de `EstruturaTurmaPort` em avaliacoes.

Objetivo sugerido:

- Aplicar a mesma validacao defensiva antes de criar avaliacoes.
- Preservar contratos HTTP existentes.
- Manter tudo no monolito.
- Validar backend com `.\mvnw.cmd "-Dtest=AvaliacaoControllerIntegrationTest" test` e `.\mvnw.cmd test`.
