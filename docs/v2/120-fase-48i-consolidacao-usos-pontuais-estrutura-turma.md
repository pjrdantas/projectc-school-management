# Fase 48I - Consolidacao dos usos pontuais de EstruturaTurmaPort

## Objetivo

Consolidar o estado atual dos usos pontuais de `EstruturaTurmaPort`, apos sua aplicacao em planejamento bimestral, diario de aula e avaliacoes.

Esta fase e de consolidacao documental e verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Consumidores atuais

| Consumidor | Momento de uso | Validacao feita | Comportamento preservado |
| --- | --- | --- | --- |
| `PlanejamentoBimestralService` | Criacao e atualizacao de planejamento | Confirma se a turma da alocacao possui a disciplina | Usa `ProfessorTurmaDisciplinaNaoEncontradaException` |
| `DiarioAulaService` | Criacao de aula | Confirma se a turma da alocacao possui a disciplina | Usa `ProfessorTurmaDisciplinaNaoEncontradaException` |
| `AvaliacaoService` | Criacao de avaliacao | Confirma se a turma da alocacao possui a disciplina | Usa `ProfessorTurmaDisciplinaNaoEncontradaException` |

## Padrao consolidado

Os tres usos seguem o mesmo padrao:

- a alocacao `ProfessorTurmaDisciplinaEntity` continua sendo carregada pelo repository transacional existente;
- a escola e resolvida pelo contexto atual via `EscolaTenantService`;
- `turmaId` e `disciplinaId` sao extraidos da alocacao carregada;
- `EstruturaTurmaPort.turmaPossuiDisciplina(escolaId, turmaId, disciplinaId)` e usado como validacao defensiva;
- a excecao publica do fluxo e preservada com `ProfessorTurmaDisciplinaNaoEncontradaException`;
- controllers, DTOs e contratos HTTP permanecem inalterados.

## Decisoes

- `EstruturaTurmaPort` continua sendo uma porta interna do monolito.
- `CatalogoAcademicoInternalService` continua sendo a implementacao atual da porta.
- A porta ainda nao substitui repositories transacionais nos fluxos de escrita.
- A validacao defensiva deve continuar restrita a pontos de baixo risco.
- Nao ha necessidade atual de BFF, microservico, Kafka, MongoDB, Redis ou componente runtime separado.

## Limites atuais

- Ainda existem fluxos que manipulam turma-disciplina diretamente sem passar por `EstruturaTurmaPort`.
- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario com multiplas escolas no fluxo operacional.
- Ainda nao ha cache de catalogo academico.
- Ainda nao ha contrato externo separado para catalogo academico.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos pontuais de `EstruturaTurmaPort` estao documentados e alinhados entre os tres consumidores atuais. A fronteira interna esta madura o bastante para continuar sendo usada em pontos pequenos, mas ainda nao justifica extracao de componente ou criacao de BFF.

## Proxima fase sugerida

Fase 48J - uso pontual de `EscolaContextoPort` no dashboard secretaria.

Objetivo sugerido:

- Aplicar `EscolaContextoPort` em um fluxo majoritariamente de leitura.
- Preservar controllers, DTOs e contrato HTTP.
- Manter tudo no monolito.
- Validar backend com teste especifico do dashboard secretaria e `.\mvnw.cmd test`.
