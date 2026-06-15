# Fase 48E - Uso pontual dos contratos internos no planejamento

## Objetivo

Usar `EstruturaTurmaPort` em uma validacao pequena do fluxo de planejamento bimestral, mantendo a implementacao dentro do monolito e sem criar BFF real, microservico ou novo componente runtime.

## Escopo implementado

- `PlanejamentoBimestralService` passou a depender de `EstruturaTurmaPort`.
- A busca de alocacao professor/turma/disciplina passou a validar, via contrato interno, se a turma ainda possui a disciplina da alocacao.
- A validacao foi aplicada em criacao e atualizacao de planejamento, pois ambos reutilizam `findAlocacao`.

## Decisoes tecnicas

- O contrato HTTP de planejamento nao foi alterado.
- Nenhum controller foi alterado.
- Nenhuma migration foi criada.
- Nenhum BFF, microservico, Kafka, MongoDB ou Redis foi introduzido.
- A validacao e defensiva e reutiliza o contrato interno de estrutura academica criado na Fase 48C.
- A excecao existente `ProfessorTurmaDisciplinaNaoEncontradaException` foi reaproveitada para preservar o comportamento externo do fluxo.

## Fora do escopo

- Criar projeto BFF.
- Criar servicos independentes.
- Refatorar todo o planejamento para portas internas.
- Refatorar aulas, frequencias ou avaliacoes.
- Alterar frontend.
- Implementar troca dinamica de escola.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=PlanejamentoBimestralControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Frontend:

- Nao houve alteracao de frontend.

## Aviso sobre novos componentes

Ainda nao e necessario criar outro componente, BFF real ou servico separado.

O momento de criar novos componentes deve vir somente quando houver uma fase explicita de extracao e contratos internos mais usados e estabilizados.

## Proxima fase sugerida

Fase 48F - consolidacao documental dos contratos internos usados.

Objetivo sugerido:

- Consolidar quais contratos internos ja existem e onde sao usados.
- Identificar o proximo candidato seguro de uso interno.
- Ainda nao criar BFF real ou microservico.
