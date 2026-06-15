# Fase 48P - Consolidacao de EscolaContextoPort em dashboards e snapshots

## Objetivo

Consolidar os usos de `EscolaContextoPort` na area de dashboards e snapshots, apos as aplicacoes pontuais nos dashboards academico, secretaria, diretor, professor e snapshots de indicadores.

Esta fase e documental e de verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Consumidores consolidados

| Consumidor | Tipo de fluxo | Uso do contexto escolar | Contrato preservado |
| --- | --- | --- | --- |
| `DashboardAcademicoService` | Leitura e agregacao academica | `obterContextoPadrao()` | `/api/dashboard/academico` |
| `DashboardSecretariaService` | Leitura e agregacao operacional | `obterContextoPadrao()` | `/api/dashboard/secretaria` |
| `DashboardDiretorService` | Leitura e agregacao executiva | `obterContextoPadrao()` | `/api/dashboard/diretor` |
| `DashboardProfessorService` | Leitura filtrada por professor | `obterContextoPadrao()` | `/api/dashboard/professor/{professorId}` |
| `DashboardIndicadorSnapshotService` | Listagem, historico e persistencia de snapshots | `obterContextoPadrao()` | APIs de snapshots de dashboard |

## Verificacao de consistencia

- Os services de dashboard nao possuem uso direto remanescente de `EscolaTenantService`.
- `DashboardSnapshotGeradorService` continua compondo dashboards e snapshots sem resolver contexto escolar diretamente.
- `DashboardFrontendService` e `DashboardAlertaService` continuam como services de composicao e nao precisam receber `EscolaContextoPort` nesta fase.
- Services de configuracao de dashboard continuam fora do escopo porque nao resolvem escola pelo contrato atual.
- `EscolaTenantService` permanece como implementacao de `EscolaContextoPort`.

## Limites atuais

- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario com multiplas escolas no fluxo operacional.
- O contexto de perfis e permissoes existe em `EscolaContexto`, mas ainda nao e o eixo de autorizacao dos fluxos de dominio.
- Ainda existem usos diretos de `EscolaTenantService` fora da area de dashboards, principalmente em fluxos transacionais ou de persistencia.
- Esses usos fora de dashboards devem ser tratados por fases pontuais, evitando refatoracao ampla.

## Decisoes

- A area de dashboards e snapshots fica consolidada como consumidora da fronteira interna de contexto escolar.
- Nao ha justificativa atual para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente runtime.
- O proximo uso deve priorizar fluxo de leitura fora de dashboards.
- A troca em fluxos transacionais deve continuar lenta e orientada por testes existentes.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos de `EscolaContextoPort` em dashboards e snapshots estao consolidados e documentados. A evolucao multi-escola segue incremental dentro do monolito.

## Proxima fase sugerida

Fase 48Q - uso pontual de `EscolaContextoPort` no resumo academico da matricula.

Objetivo sugerido:

- Aplicar `EscolaContextoPort` em `MatriculaAcademicoResumoService`.
- Preservar controller, DTO e contrato HTTP.
- Manter os filtros por matricula e escola com o mesmo comportamento atual.
- Validar com `.\mvnw.cmd "-Dtest=MatriculaAcademicoControllerIntegrationTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.
