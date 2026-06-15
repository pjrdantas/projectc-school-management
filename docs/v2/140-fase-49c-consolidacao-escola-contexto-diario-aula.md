# Fase 49C - Consolidacao de EscolaContextoPort em diario de aula

## Objetivo

Consolidar o uso de `EscolaContextoPort` em `DiarioAulaService`, confirmando o estado do fluxo de diario de aula apos a aplicacao pontual da Fase 49B.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Estado consolidado

| Contrato interno | Implementacao | Consumidor atual | Tipo de uso |
| --- | --- | --- | --- |
| `EscolaContextoPort` | `EscolaTenantService` | `DiarioAulaService` | Resolver contexto escolar padrao para diario de aula e frequencias |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `DiarioAulaService` | Validar turma-disciplina da alocacao antes de criar aula |

## Verificacao de consistencia

- `DiarioAulaService` nao depende mais diretamente de `EscolaTenantService`.
- `DiarioAulaService` continua consumindo `EstruturaTurmaPort`.
- O metodo privado `escolaId()` continua centralizando a resolucao da escola padrao.
- Controllers, DTOs, requests, responses, repositories e contratos HTTP permanecem inalterados.
- As regras de criacao de aula permanecem inalteradas.
- As regras de frequencia de professor permanecem inalteradas.
- As regras de frequencia de aluno permanecem inalteradas.
- A consistencia entre turma da aula e turma da matricula permanece inalterada.
- `AvaliacaoService` continua fora do escopo.

## Limites atuais

- `AvaliacaoService` ainda usa `EscolaTenantService` diretamente em fluxo de avaliacao, notas e matricula.
- `AvaliacaoService` tambem consome `EstruturaTurmaPort`, mas envolve lancamento de notas e validacao de matricula.
- Fluxos de notas exigem diagnostico proprio antes de qualquer aplicacao pontual de `EscolaContextoPort`.
- Fluxos de seguranca, autenticacao, usuarios e perfis continuam fora do escopo.

## Decisoes

- A consolidacao de `EscolaContextoPort` em diario de aula esta completa.
- Nao aplicar nova troca em avaliacoes sem diagnostico pontual.
- Nao alterar fluxos de notas ou matricula nesta fase.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

O diario de aula esta documentado como consumidor de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP.

## Proxima fase sugerida

Fase 49D - diagnostico pontual de `AvaliacaoService` antes de aplicar `EscolaContextoPort`.

Objetivo sugerido:

- Mapear os usos de `escolaId()` em avaliacoes e notas.
- Separar consultas de avaliacao, criacao de avaliacao, lancamento de nota e consultas de notas.
- Confirmar impactos sobre matricula, consistencia de turma e validacao de nota antes de qualquer troca.
- Escolher se `AvaliacaoService` pode ser migrado em uma fase pontual ou se precisa ser dividido.
- Validar backend completo com `.\mvnw.cmd test`.
