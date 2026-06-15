# Fase 48W - Consolidacao de EscolaContextoPort nos contratos internos de catalogo

## Objetivo

Consolidar o uso de `EscolaContextoPort` em `CatalogoAcademicoInternalService`, confirmando o estado dos contratos internos de catalogo apos a aplicacao pontual da Fase 48V.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Estado consolidado

| Contrato interno | Implementacao | Consumidor atual | Tipo de uso |
| --- | --- | --- | --- |
| `EscolaContextoPort` | `EscolaTenantService` | `CatalogoAcademicoInternalService` | Resolver contexto escolar padrao para contratos internos de catalogo |
| `CatalogoAcademicoPort` | `CatalogoAcademicoInternalService` | `DashboardAcademicoService` | Listar turmas por escola para agregacao academica |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `PlanejamentoBimestralService` | Validar turma-disciplina da alocacao antes de criar ou atualizar planejamento |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `DiarioAulaService` | Validar turma-disciplina da alocacao antes de criar aula |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `AvaliacaoService` | Validar turma-disciplina da alocacao antes de criar avaliacao |

## Verificacao de consistencia

- `CatalogoAcademicoInternalService` nao depende mais diretamente de `EscolaTenantService`.
- `CatalogoAcademicoInternalService` continua implementando `CatalogoAcademicoPort`.
- `CatalogoAcademicoInternalService` continua implementando `EstruturaTurmaPort`.
- O fallback de escola padrao continua restrito a `resolverEscolaId(UUID escolaId)`.
- Os contratos internos continuam recebendo `escolaId` como parametro explicito.
- Nenhum contrato interno de catalogo expoe entidades JPA.
- Controllers, DTOs, requests, responses e contratos HTTP permanecem inalterados.

## Limites atuais

- Gateways de persistencia de catalogo ainda usam `EscolaTenantService` diretamente e continuam fora do escopo.
- `DisciplinaService` e `TurmaDisciplinaService` ainda exigem analise propria antes de qualquer troca.
- `PlanejamentoBimestralService`, `DiarioAulaService` e `AvaliacaoService` ja consomem `EstruturaTurmaPort`, mas ainda resolvem escola diretamente em fluxos de escrita.
- Fluxos de seguranca, autenticacao, usuarios e perfis continuam fora do escopo.

## Decisoes

- A consolidacao de `EscolaContextoPort` nos contratos internos de catalogo esta completa.
- Nao aplicar nova troca em persistencia de catalogo sem fase especifica.
- Nao aplicar nova troca em fluxos de escrita sem diagnostico pontual do impacto.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os contratos internos de catalogo estao documentados com `CatalogoAcademicoInternalService` consumindo `EscolaContextoPort` para resolver contexto escolar padrao, mantendo `CatalogoAcademicoPort` e `EstruturaTurmaPort` estaveis.

## Estado apos Fase 48X

A Fase 48X diagnosticou os consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService` e selecionou `PlanejamentoBimestralService` como proximo candidato seguro.

## Proxima fase sugerida

Fase 48X - diagnostico dos consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService`.

Objetivo sugerido:

- Mapear `PlanejamentoBimestralService`, `DiarioAulaService` e `AvaliacaoService`.
- Separar o uso de `EstruturaTurmaPort` do uso direto de `EscolaTenantService` em fluxos de escrita.
- Escolher um unico candidato seguro para eventual aplicacao pontual.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48X

Fase 48Y - uso pontual de `EscolaContextoPort` em `PlanejamentoBimestralService`.
