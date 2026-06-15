# Fase 48Z - Consolidacao de EscolaContextoPort em planejamento bimestral

## Objetivo

Consolidar o uso de `EscolaContextoPort` em `PlanejamentoBimestralService`, confirmando o estado do fluxo de planejamento bimestral apos a aplicacao pontual da Fase 48Y.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Estado consolidado

| Contrato interno | Implementacao | Consumidor atual | Tipo de uso |
| --- | --- | --- | --- |
| `EscolaContextoPort` | `EscolaTenantService` | `PlanejamentoBimestralService` | Resolver contexto escolar padrao para planejamento bimestral |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `PlanejamentoBimestralService` | Validar turma-disciplina da alocacao antes de criar ou atualizar planejamento |

## Verificacao de consistencia

- `PlanejamentoBimestralService` nao depende mais diretamente de `EscolaTenantService`.
- `PlanejamentoBimestralService` continua consumindo `EstruturaTurmaPort`.
- O metodo privado `escolaId()` continua centralizando a resolucao da escola padrao.
- Controllers, DTOs, requests, responses e contratos HTTP permanecem inalterados.
- As regras de planejamento, aulas previstas, avaliacoes previstas e status permanecem inalteradas.
- `DiarioAulaService` e `AvaliacaoService` continuam fora do escopo.

## Limites atuais

- `DiarioAulaService` ainda usa `EscolaTenantService` diretamente em fluxo de aula, frequencia professor e frequencia aluno.
- `AvaliacaoService` ainda usa `EscolaTenantService` diretamente em fluxo de avaliacao, notas e matricula.
- Os dois fluxos restantes usam `EstruturaTurmaPort`, mas possuem maior impacto operacional que planejamento bimestral.
- Fluxos de seguranca, autenticacao, usuarios e perfis continuam fora do escopo.

## Decisoes

- A consolidacao de `EscolaContextoPort` em planejamento bimestral esta completa.
- Nao aplicar nova troca em diario de aula ou avaliacoes sem diagnostico pontual.
- Nao alterar fluxos de frequencia, notas ou matricula nesta fase.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

O planejamento bimestral esta documentado como consumidor de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP.

## Estado apos Fase 49A

A Fase 49A diagnosticou `DiarioAulaService` antes de qualquer troca, confirmando que a proxima fase pode aplicar `EscolaContextoPort` de forma pontual se preservar aula, frequencia professor, frequencia aluno e consistencia de matricula.

## Proxima fase sugerida

Fase 49A - diagnostico pontual de `DiarioAulaService` antes de aplicar `EscolaContextoPort`.

Objetivo sugerido:

- Mapear os usos de `escolaId()` em aulas e frequencias.
- Separar consultas de leitura, criacao de aula e registros de frequencia.
- Confirmar impactos sobre matricula e frequencia antes de qualquer troca.
- Escolher se `DiarioAulaService` pode ser migrado em uma fase pontual ou se precisa ser dividido.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 49A

Fase 49B - uso pontual de `EscolaContextoPort` em `DiarioAulaService`.
