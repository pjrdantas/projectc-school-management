# Fase 48X - Diagnostico dos consumidores de EstruturaTurmaPort que ainda usam EscolaTenantService

## Objetivo

Diagnosticar os consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService` diretamente, separando o uso da porta de estrutura de turma do ponto de resolucao de contexto escolar.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Consumidores mapeados

| Consumidor | Uso de `EstruturaTurmaPort` | Uso direto de `EscolaTenantService` | Tipo de fluxo | Risco |
| --- | --- | --- | --- | --- |
| `PlanejamentoBimestralService` | Valida se a turma da alocacao possui a disciplina antes de criar ou atualizar planejamento | Metodo privado `escolaId()` | Escrita e leitura de planejamento bimestral | Medio |
| `DiarioAulaService` | Valida se a turma da alocacao possui a disciplina antes de criar aula | Metodo privado `escolaId()` | Escrita e leitura de aula, frequencia professor e frequencia aluno | Medio a alto |
| `AvaliacaoService` | Valida se a turma da alocacao possui a disciplina antes de criar avaliacao | Metodo privado `escolaId()` | Escrita e leitura de avaliacao, notas e matricula | Medio a alto |

## Analise

### `PlanejamentoBimestralService`

- Usa `EstruturaTurmaPort` em validacao defensiva de alocacao.
- Resolve escola em consultas de planejamento, alocacao e periodo avaliativo.
- Escreve planejamento, aulas previstas, avaliacoes previstas e status.
- Nao registra frequencia real nem nota de aluno.
- E o candidato mais seguro entre os tres para uma troca pontual de contexto escolar.

### `DiarioAulaService`

- Usa `EstruturaTurmaPort` em validacao defensiva antes de criar aula.
- Resolve escola em consultas de aula, frequencia professor, frequencia aluno e matricula.
- Cruza aula, frequencia e matricula.
- Deve ser mantido para fase posterior por ter maior impacto operacional.

### `AvaliacaoService`

- Usa `EstruturaTurmaPort` em validacao defensiva antes de criar avaliacao.
- Resolve escola em consultas de avaliacao, notas e matricula.
- Cruza avaliacao, nota e matricula.
- Deve ser mantido para fase posterior por ter impacto direto em lancamento de notas.

## Candidato escolhido para a proxima fase

`PlanejamentoBimestralService`.

Motivos:

- Ja usa `EstruturaTurmaPort` de forma pontual e controlada.
- O uso direto de `EscolaTenantService` esta concentrado no metodo privado `escolaId()`.
- A troca pode ficar limitada ao ponto de resolucao de escola padrao.
- O fluxo e menos sensivel que frequencia real e lancamento de notas.
- Nao exige alterar controller, DTO, request, response, migration, frontend ou contrato HTTP.

## Onde nao mexer na proxima fase

- `DiarioAulaService`.
- `AvaliacaoService`.
- Gateways de persistencia de catalogo.
- Fluxos de frequencia, notas, matricula e seguranca.

## Decisoes

- A proxima fase deve aplicar `EscolaContextoPort` somente em `PlanejamentoBimestralService`.
- `DiarioAulaService` e `AvaliacaoService` devem ficar para diagnosticos ou fases pontuais posteriores.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService` foram classificados por risco. O proximo candidato seguro e `PlanejamentoBimestralService`.

## Estado apos Fase 48Y

A Fase 48Y aplicou `EscolaContextoPort` em `PlanejamentoBimestralService`, mantendo `DiarioAulaService` e `AvaliacaoService` fora do escopo por envolverem frequencia, matricula e notas.

## Proxima fase sugerida

Fase 48Y - uso pontual de `EscolaContextoPort` em `PlanejamentoBimestralService`.

Objetivo sugerido:

- Trocar a dependencia direta de `EscolaTenantService` por `EscolaContextoPort` apenas em `PlanejamentoBimestralService`.
- Preservar o comportamento do metodo privado `escolaId()`.
- Preservar controllers, DTOs e contratos HTTP.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48Y

Fase 48Z - consolidacao de `EscolaContextoPort` em planejamento bimestral.
