# Fase 49A - Diagnostico pontual de DiarioAulaService antes de EscolaContextoPort

## Objetivo

Diagnosticar os usos de `EscolaTenantService` em `DiarioAulaService` antes de aplicar `EscolaContextoPort`, separando criacao de aula, consultas, frequencia de professor, frequencia de aluno e validacoes de matricula.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Usos de escola mapeados

| Area | Metodo ou ponto | Uso de `escolaId()` | Risco |
| --- | --- | --- | --- |
| Criacao de aula | `criarAula` | Busca alocacao por escola antes de criar aula | Medio |
| Consulta de aulas | `listarAulas` | Filtra por alocacao, turma ou escola | Baixo a medio |
| Consulta de aula | `buscarAulaPorId` e `findAula` | Busca aula por id e escola | Medio |
| Frequencia professor | `registrarFrequenciaProfessor` | Garante aula por escola e evita frequencia duplicada | Medio |
| Frequencia professor | `listarFrequenciaProfessor` | Verifica existencia da aula e lista por escola | Medio |
| Frequencia aluno | `registrarFrequenciaAluno` | Busca aula, matricula por escola e evita frequencia duplicada | Medio a alto |
| Frequencia aluno | `listarFrequenciasAlunos` | Verifica existencia da aula e lista por escola | Medio |
| Estrutura de turma | `validarEstruturaTurmaDisciplina` | Passa `escolaId` para `EstruturaTurmaPort` | Medio |

## Analise

- O uso direto de `EscolaTenantService` esta concentrado no metodo privado `escolaId()`.
- `DiarioAulaService` ja consome `EstruturaTurmaPort` para validar turma-disciplina.
- A troca por `EscolaContextoPort` pode ficar limitada ao ponto de resolucao de escola padrao.
- A fase seguinte pode preservar controllers, DTOs, requests, responses e contratos HTTP.
- O ponto mais sensivel do service e `registrarFrequenciaAluno`, porque cruza aula, matricula e turma.
- A troca de dependencia nao deve alterar regras de consistencia entre turma da aula e turma da matricula.

## Candidato escolhido para a proxima fase

`DiarioAulaService`.

Motivos:

- O service ja centraliza a escola em um metodo privado.
- A alteracao esperada e equivalente ao padrao aplicado em `PlanejamentoBimestralService`.
- A validacao de turma-disciplina por `EstruturaTurmaPort` deve ser preservada.
- O contrato HTTP e os DTOs nao precisam mudar.
- O risco e aceitavel para uma fase pontual desde que a validacao cubra `AulaControllerIntegrationTest` e o backend completo.

## Onde nao mexer na proxima fase

- `AvaliacaoService`.
- Fluxos de nota.
- Gateways de persistencia de matricula ou catalogo.
- Regras de consistencia entre aula, matricula e turma.
- Seguranca, autenticacao, usuarios e perfis.

## Decisoes

- A proxima fase deve aplicar `EscolaContextoPort` somente em `DiarioAulaService`.
- A troca deve preservar o metodo privado `escolaId()`.
- Nao deve haver alteracao em controller, DTO, request, response, rota HTTP ou migration.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AulaControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`DiarioAulaService` foi diagnosticado como candidato viavel para uma troca pontual para `EscolaContextoPort`, desde que a proxima fase mantenha escopo restrito ao ponto de resolucao de escola padrao.

## Proxima fase sugerida

Fase 49B - uso pontual de `EscolaContextoPort` em `DiarioAulaService`.

Objetivo sugerido:

- Trocar a dependencia direta de `EscolaTenantService` por `EscolaContextoPort` apenas em `DiarioAulaService`.
- Preservar o comportamento do metodo privado `escolaId()`.
- Preservar regras de aula, frequencia professor, frequencia aluno e consistencia de matricula.
- Validar com `.\mvnw.cmd "-Dtest=AulaControllerIntegrationTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.
