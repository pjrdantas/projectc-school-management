# Fase 49B - Uso pontual de EscolaContextoPort em DiarioAulaService

## Objetivo

Aplicar `EscolaContextoPort` em `DiarioAulaService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de diario de aula que envolve aulas, frequencia de professor, frequencia de aluno e matricula.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Escopo implementado

- `DiarioAulaService` passou a depender de `EscolaContextoPort`.
- `EstruturaTurmaPort` foi preservado.
- O metodo privado `escolaId()` manteve o comportamento de resolver a escola padrao.
- Controllers, DTOs, requests, responses, rotas HTTP e repositories permaneceram inalterados.
- `AvaliacaoService` permaneceu fora do escopo.

## Decisoes

- A troca ficou limitada ao ponto de resolucao de escola padrao.
- Nao houve alteracao nas regras de criacao de aula.
- Nao houve alteracao nas regras de frequencia de professor.
- Nao houve alteracao nas regras de frequencia de aluno.
- Nao houve alteracao na consistencia entre turma da aula e turma da matricula.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AulaControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`DiarioAulaService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina.

## Proxima fase sugerida

Fase 49C - consolidacao de `EscolaContextoPort` em diario de aula.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Confirmar que `DiarioAulaService` nao depende mais diretamente de `EscolaTenantService`.
- Reavaliar `AvaliacaoService` antes de qualquer aplicacao pontual.
- Validar backend completo com `.\mvnw.cmd test`.
