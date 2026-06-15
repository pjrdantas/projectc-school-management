# Fase 48H - Uso pontual de EstruturaTurmaPort em avaliacoes

## Objetivo

Aplicar o uso pontual de `EstruturaTurmaPort` no fluxo de avaliacoes, mantendo a evolucao incremental das fronteiras internas iniciada nas Fases 48E e 48G.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `AvaliacaoService` passou a depender de `EstruturaTurmaPort`.
- A criacao de avaliacao agora valida se a turma da alocacao realmente possui a disciplina informada.
- A validacao usa `escolaId`, `turmaId` e `disciplinaId` de forma explicita.
- Quando a estrutura academica nao confirma a relacao turma-disciplina, o fluxo preserva a excecao de dominio ja usada para alocacao inexistente.

## Decisoes

- O contrato publico de criacao de avaliacao foi preservado.
- Nao houve alteracao em controllers, requests ou responses.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla de avaliacoes, notas ou matriculas.
- O uso de `EstruturaTurmaPort` ficou limitado ao ponto de criacao da avaliacao.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AvaliacaoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

Avaliacoes passam a reutilizar a fronteira interna de estrutura academica em um ponto transacional de baixo risco, mantendo consistencia com o criterio ja aplicado no planejamento bimestral e no diario de aula.

## Proxima fase sugerida

Fase 48I - consolidacao dos usos pontuais de `EstruturaTurmaPort`.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EstruturaTurmaPort`.
- Verificar se os tres usos atuais seguem o mesmo padrao de excecao e contrato publico preservado.
- Nao criar BFF real, microservico ou novo componente runtime.
- Validar backend com `.\mvnw.cmd test`.
