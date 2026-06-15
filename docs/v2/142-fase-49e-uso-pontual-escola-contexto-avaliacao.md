# Fase 49E - Uso pontual de EscolaContextoPort em AvaliacaoService

## Objetivo

Aplicar `EscolaContextoPort` em `AvaliacaoService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de avaliacao que envolve avaliacoes, notas, matricula e consistencia de turma.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Escopo implementado

- `AvaliacaoService` passou a depender de `EscolaContextoPort`.
- `EstruturaTurmaPort` foi preservado.
- O metodo privado `escolaId()` manteve o comportamento de resolver a escola padrao.
- Controllers, DTOs, requests, responses, rotas HTTP e repositories permaneceram inalterados.
- Fluxos de matricula, seguranca, usuarios e perfis permaneceram fora do escopo.

## Decisoes

- A troca ficou limitada ao ponto de resolucao de escola padrao.
- Nao houve alteracao nas regras de criacao de avaliacao.
- Nao houve alteracao nas regras de lancamento de nota.
- Nao houve alteracao nas regras de nota minima, nota maxima ou duplicidade de nota.
- Nao houve alteracao na consistencia entre turma da avaliacao e turma da matricula.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AvaliacaoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`AvaliacaoService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina.

## Proxima fase sugerida

Fase 49F - consolidacao de `EscolaContextoPort` em avaliacoes.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Confirmar que `AvaliacaoService` nao depende mais diretamente de `EscolaTenantService`.
- Reavaliar os usos remanescentes de `EscolaTenantService` antes de qualquer nova aplicacao pontual.
- Validar backend completo com `.\mvnw.cmd test`.
