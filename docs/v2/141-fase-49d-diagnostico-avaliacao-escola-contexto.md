# Fase 49D - Diagnostico pontual de AvaliacaoService antes de EscolaContextoPort

## Objetivo

Diagnosticar os usos de `EscolaTenantService` em `AvaliacaoService` antes de aplicar `EscolaContextoPort`, separando criacao de avaliacao, consultas, lancamento de nota, consulta de notas e validacoes de matricula.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Usos de escola mapeados

| Area | Metodo ou ponto | Uso de `escolaId()` | Risco |
| --- | --- | --- | --- |
| Criacao de avaliacao | `criar` | Busca alocacao por escola antes de criar avaliacao | Medio |
| Consulta de avaliacoes | `listar` | Filtra por alocacao, turma ou escola | Baixo a medio |
| Consulta de avaliacao | `buscarPorId` e `findAvaliacao` | Busca avaliacao por id e escola | Medio |
| Lancamento de nota | `lancarNota` | Busca avaliacao, matricula por escola e evita nota duplicada | Medio a alto |
| Lancamento de nota | `lancarNota` | Valida consistencia entre turma da avaliacao e turma da matricula | Alto |
| Lancamento de nota | `lancarNota` | Valida intervalo da nota contra valor maximo da avaliacao | Medio |
| Consulta de notas | `listarNotasPorAvaliacao` | Verifica existencia da avaliacao e lista notas por escola | Medio |
| Consulta de notas | `listarNotasPorMatricula` | Verifica matricula por escola e lista notas por matricula | Medio |
| Estrutura de turma | `validarEstruturaTurmaDisciplina` | Passa `escolaId` para `EstruturaTurmaPort` | Medio |

## Analise

- O uso direto de `EscolaTenantService` esta concentrado no metodo privado `escolaId()`.
- `AvaliacaoService` ja consome `EstruturaTurmaPort` para validar turma-disciplina.
- A troca por `EscolaContextoPort` pode ficar limitada ao ponto de resolucao de escola padrao.
- A fase seguinte pode preservar controllers, DTOs, requests, responses e contratos HTTP.
- O ponto mais sensivel do service e `lancarNota`, porque cruza avaliacao, matricula, turma, duplicidade e valor maximo.
- A troca de dependencia nao deve alterar regras de consistencia entre turma da avaliacao e turma da matricula.
- A troca de dependencia nao deve alterar regra de nota minima, nota maxima ou duplicidade de nota.

## Candidato escolhido para a proxima fase

`AvaliacaoService`.

Motivos:

- O service centraliza a escola em um metodo privado.
- A alteracao esperada e equivalente ao padrao aplicado em `PlanejamentoBimestralService` e `DiarioAulaService`.
- A validacao de turma-disciplina por `EstruturaTurmaPort` deve ser preservada.
- O contrato HTTP e os DTOs nao precisam mudar.
- O risco e aceitavel para uma fase pontual desde que a validacao cubra `AvaliacaoControllerIntegrationTest` e o backend completo.

## Onde nao mexer na proxima fase

- `MatriculaFluxoService`.
- Gateways de persistencia de matricula.
- Repositories de avaliacao ou nota.
- Controllers, DTOs, requests e responses.
- Regras de nota minima, nota maxima ou duplicidade de nota.
- Regras de consistencia entre avaliacao, matricula e turma.
- Seguranca, autenticacao, usuarios e perfis.

## Decisoes

- A proxima fase deve aplicar `EscolaContextoPort` somente em `AvaliacaoService`.
- A troca deve preservar o metodo privado `escolaId()`.
- Nao deve haver alteracao em controller, DTO, request, response, rota HTTP ou migration.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AvaliacaoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`AvaliacaoService` foi diagnosticado como candidato viavel para uma troca pontual para `EscolaContextoPort`, desde que a proxima fase mantenha escopo restrito ao ponto de resolucao de escola padrao e preserve as regras de nota, matricula e turma.

## Estado apos Fase 49E

A Fase 49E aplicou `EscolaContextoPort` em `AvaliacaoService`, mantendo `EstruturaTurmaPort`, o metodo privado `escolaId()`, as regras de avaliacao e nota e os contratos HTTP inalterados.

## Estado apos Fase 49F

A Fase 49F consolidou `AvaliacaoService` como consumidor de `EscolaContextoPort` e `EstruturaTurmaPort`, encerrando o ciclo pontual de planejamento, diario de aula e avaliacoes antes do proximo diagnostico de usos remanescentes.

## Proxima fase sugerida

Fase 49E - uso pontual de `EscolaContextoPort` em `AvaliacaoService`.

Objetivo sugerido:

- Trocar a dependencia direta de `EscolaTenantService` por `EscolaContextoPort` apenas em `AvaliacaoService`.
- Preservar o comportamento do metodo privado `escolaId()`.
- Preservar regras de avaliacao, nota, duplicidade, valor maximo e consistencia de matricula.
- Validar com `.\mvnw.cmd "-Dtest=AvaliacaoControllerIntegrationTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 49E

Fase 49F - consolidacao de `EscolaContextoPort` em avaliacoes.

## Proxima fase sugerida apos 49F

Fase 49G - diagnostico atualizado dos usos remanescentes de `EscolaTenantService`.
