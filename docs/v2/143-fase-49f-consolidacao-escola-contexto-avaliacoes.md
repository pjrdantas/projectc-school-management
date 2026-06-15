# Fase 49F - Consolidacao de EscolaContextoPort em avaliacoes

## Objetivo

Consolidar o uso de `EscolaContextoPort` em `AvaliacaoService`, confirmando o estado do fluxo de avaliacoes e notas apos a aplicacao pontual da Fase 49E.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Estado consolidado

| Contrato interno | Implementacao | Consumidor atual | Tipo de uso |
| --- | --- | --- | --- |
| `EscolaContextoPort` | `EscolaTenantService` | `AvaliacaoService` | Resolver contexto escolar padrao para avaliacoes e notas |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `AvaliacaoService` | Validar turma-disciplina da alocacao antes de criar avaliacao |

## Verificacao de consistencia

- `AvaliacaoService` nao depende mais diretamente de `EscolaTenantService`.
- `AvaliacaoService` continua consumindo `EstruturaTurmaPort`.
- O metodo privado `escolaId()` continua centralizando a resolucao da escola padrao.
- Controllers, DTOs, requests, responses, repositories e contratos HTTP permanecem inalterados.
- As regras de criacao de avaliacao permanecem inalteradas.
- As regras de lancamento de nota permanecem inalteradas.
- As regras de nota minima, nota maxima e duplicidade de nota permanecem inalteradas.
- A consistencia entre turma da avaliacao e turma da matricula permanece inalterada.

## Usos remanescentes observados

Ainda ha usos diretos de `EscolaTenantService` em outros dominios:

- Catalogo: `DisciplinaService`, `PeriodoLetivoPersistenceGateway`, `SeriePersistenceGateway`, `TurmaPersistenceGateway` e uso da constante em `TurmaDisciplinaService`.
- Aluno e responsavel: gateways de persistencia de aluno, responsavel e vinculo aluno-responsavel.
- Matricula: `MatriculaFluxoService` e `MatriculaPersistenceGateway`.
- Documento: `DocumentoPersistenceGateway`.
- Historico: `HistoricoEscolarServiceImpl` e `BoletimService`.
- IA: `PlanejamentoIAService`.
- Pessoa: `PessoaFoundationService`.
- Professor: `ProfessorService`.
- Seguranca: `AuthService` e `UsuarioInteractor`.

## Decisoes

- A consolidacao de `EscolaContextoPort` em avaliacoes esta completa.
- Nao aplicar nova troca sem diagnostico atualizado dos usos remanescentes.
- Nao alterar fluxos transacionais de matricula, seguranca, autenticacao, usuarios ou perfis nesta fase.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Avaliacoes e notas estao documentadas como consumidoras de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP.

## Estado apos Fase 49G

A Fase 49G reclassificou os usos remanescentes de `EscolaTenantService` e escolheu `DisciplinaService` como proximo candidato para diagnostico pontual, mantendo fluxos transacionais e de seguranca fora do escopo.

## Proxima fase sugerida

Fase 49G - diagnostico atualizado dos usos remanescentes de `EscolaTenantService`.

Objetivo sugerido:

- Reclassificar os usos remanescentes de `EscolaTenantService` apos as fases 48V a 49F.
- Separar fluxos de leitura, persistencia, transacao, seguranca e IA.
- Escolher um unico candidato seguro para a proxima aplicacao pontual de `EscolaContextoPort`.
- Manter BFF, microservicos, Kafka, MongoDB, Redis e novos componentes frontend fora do escopo ate haver necessidade concreta.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 49G

Fase 49H - diagnostico pontual de `DisciplinaService` antes de aplicar `EscolaContextoPort`.
