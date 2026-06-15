# Fase 48Y - Uso pontual de EscolaContextoPort em PlanejamentoBimestralService

## Objetivo

Aplicar `EscolaContextoPort` em `PlanejamentoBimestralService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de planejamento bimestral.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `PlanejamentoBimestralService` passou a depender de `EscolaContextoPort`.
- `EstruturaTurmaPort` foi preservado.
- O metodo privado `escolaId()` manteve o comportamento de resolver a escola padrao.
- Controllers, DTOs, requests, responses e contratos HTTP permaneceram inalterados.
- `DiarioAulaService` e `AvaliacaoService` permaneceram fora do escopo.

## Decisoes

- A troca ficou limitada ao ponto de resolucao de escola padrao.
- Nao houve alteracao em regras de planejamento, aulas previstas, avaliacoes previstas ou status.
- Nao houve alteracao de migrations.
- Fluxos de frequencia, notas, matricula e seguranca continuam fora do escopo.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=PlanejamentoBimestralControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`PlanejamentoBimestralService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina.

## Estado apos Fase 48Z

A Fase 48Z consolidou o uso de `EscolaContextoPort` em `PlanejamentoBimestralService`, confirmando que o service nao depende mais diretamente de `EscolaTenantService` e que `DiarioAulaService` e `AvaliacaoService` seguem fora do escopo.

## Proxima fase sugerida

Fase 48Z - consolidacao de `EscolaContextoPort` em planejamento bimestral.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Confirmar que `PlanejamentoBimestralService` nao depende mais diretamente de `EscolaTenantService`.
- Reavaliar `DiarioAulaService` e `AvaliacaoService` antes de qualquer aplicacao pontual.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48Z

Fase 49A - diagnostico pontual de `DiarioAulaService` antes de aplicar `EscolaContextoPort`.
