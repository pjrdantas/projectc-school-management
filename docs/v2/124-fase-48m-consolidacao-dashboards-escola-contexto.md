# Fase 48M - Consolidacao dos dashboards com EscolaContextoPort

## Objetivo

Consolidar o estado atual dos dashboards que ja usam `EscolaContextoPort`, apos as aplicacoes pontuais nos dashboards academico, secretaria e diretor.

Esta fase e documental e de verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Consumidores consolidados

| Consumidor | Momento de uso | Contexto usado | Comportamento preservado |
| --- | --- | --- | --- |
| `DashboardAcademicoService` | Consulta do dashboard academico | `obterContextoPadrao()` | Mantem o response publico do dashboard academico |
| `DashboardSecretariaService` | Consulta do dashboard secretaria | `obterContextoPadrao()` | Mantem o response publico do dashboard secretaria |
| `DashboardDiretorService` | Consulta do dashboard diretor | `obterContextoPadrao()` | Mantem o response publico do dashboard diretor |

## Padrao consolidado

Os tres dashboards seguem o mesmo desenho:

- o contexto escolar e resolvido por `EscolaContextoPort`;
- `escolaId` e `escolaNome` sao obtidos a partir de `EscolaContexto`;
- as agregacoes continuam usando repositories e portas internas existentes;
- controllers, DTOs e contratos HTTP permanecem inalterados;
- a resolucao continua apontando para a escola padrao enquanto o sistema opera em modo escola unica.

## Limites atuais

- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario com multiplas escolas no fluxo operacional.
- `DashboardProfessorService` ainda resolve a escola diretamente por `EscolaTenantService`.
- `DashboardIndicadorSnapshotService` ainda resolve a escola diretamente por `EscolaTenantService`.
- `DashboardSnapshotGeradorService` compoe dashboards e snapshots, mas nao deve ser refatorado nesta fase.
- O contexto de perfis e permissoes existe em `EscolaContexto`, mas ainda nao e o eixo de autorizacao dos fluxos de dominio.

## Decisoes

- `EscolaContextoPort` continua sendo uma porta interna do monolito.
- `EscolaTenantService` continua sendo a implementacao atual da porta.
- O uso da porta deve continuar incremental e concentrado em pontos de baixo risco.
- Fluxos de leitura e agregacao continuam sendo os melhores candidatos iniciais.
- Ainda nao ha necessidade de BFF, microservico, Kafka, MongoDB, Redis ou componente runtime separado.

## Verificacao realizada

- Conferidos os consumidores atuais de `EscolaContextoPort` em dashboards.
- Conferidos os usos remanescentes de `EscolaTenantService` em dashboards e snapshots.
- Atualizada a matriz documental de contratos internos.
- Mantido o limite de nao alterar controller, DTO, migration ou frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos de `EscolaContextoPort` nos dashboards academico, secretaria e diretor estao consolidados e documentados. A fronteira interna de contexto escolar permanece pronta para novos usos pontuais sem alterar o desenho runtime atual.

## Estado apos Fase 48N

A Fase 48N aplicou `EscolaContextoPort` em `DashboardProfessorService`, mantendo o mesmo padrao dos dashboards academico, secretaria e diretor. `DashboardIndicadorSnapshotService` continua como uso direto remanescente de `EscolaTenantService` na area de dashboards.

## Estado apos Fase 48O

A Fase 48O aplicou `EscolaContextoPort` em `DashboardIndicadorSnapshotService`. Os dashboards academico, secretaria, diretor, professor e os snapshots de indicadores agora resolvem contexto escolar pela porta interna.

## Proxima fase sugerida apos 48M

Fase 48N - uso pontual de `EscolaContextoPort` no dashboard professor.

Objetivo sugerido:

- Aplicar `EscolaContextoPort` em `DashboardProfessorService`.
- Preservar controller, DTO e contrato HTTP.
- Manter os filtros por professor e escola com o mesmo comportamento atual.
- Validar com `.\mvnw.cmd "-Dtest=DashboardProfessorControllerIntegrationTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48N

Fase 48O - uso pontual de `EscolaContextoPort` nos snapshots de indicadores de dashboard.

## Proxima fase sugerida apos 48O

Fase 48P - consolidacao dos usos de `EscolaContextoPort` em dashboards e snapshots.
