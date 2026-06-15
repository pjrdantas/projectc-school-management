# Fase 48K - Consolidacao dos usos de EscolaContextoPort em dashboards

## Objetivo

Consolidar o estado atual dos usos de `EscolaContextoPort` em dashboards, apos sua aplicacao no dashboard academico e no dashboard secretaria.

Esta fase e de consolidacao documental e verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Consumidores atuais

| Consumidor | Momento de uso | Contexto usado | Comportamento preservado |
| --- | --- | --- | --- |
| `DashboardAcademicoService` | Consulta do dashboard academico | `obterContextoPadrao()` | Mantem o response publico do dashboard academico |
| `DashboardSecretariaService` | Consulta do dashboard secretaria | `obterContextoPadrao()` | Mantem o response publico do dashboard secretaria |

## Padrao consolidado

Os dois usos seguem o mesmo padrao:

- o contexto escolar e resolvido por `EscolaContextoPort`;
- `escolaId` e `escolaNome` sao obtidos a partir de `EscolaContexto`;
- as agregacoes continuam usando os repositories e portas internas ja existentes;
- controllers, DTOs e contratos HTTP permanecem inalterados;
- a resolucao ainda usa a escola padrao enquanto o produto opera em modo escola unica.

## Decisoes

- `EscolaContextoPort` continua sendo uma porta interna do monolito.
- `EscolaTenantService` continua sendo a implementacao atual da porta.
- A porta deve continuar sendo aplicada primeiro em fluxos de leitura e agregacao.
- Nao ha necessidade atual de BFF, microservico, Kafka, MongoDB, Redis ou componente runtime separado.
- A existencia da porta prepara a evolucao multi-escola, mas nao muda o comportamento operacional atual.

## Limites atuais

- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario com multiplas escolas no fluxo operacional.
- `DashboardDiretorService`, `DashboardProfessorService` e snapshots ainda possuem usos diretos de contexto escolar ou composicao indireta.
- O contexto de perfis e permissoes existe em `EscolaContexto`, mas ainda nao e o eixo de autorizacao dos fluxos de dominio.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos de `EscolaContextoPort` em dashboards estao documentados e alinhados. A fronteira interna de contexto escolar esta pronta para novos usos pontuais em dashboards de leitura, mas ainda nao justifica extracao de componente ou criacao de BFF.

## Proxima fase sugerida

Fase 48L - uso pontual de `EscolaContextoPort` no dashboard diretor.

Objetivo sugerido:

- Aplicar `EscolaContextoPort` em `DashboardDiretorService`.
- Preservar controller, DTOs e contrato HTTP.
- Manter tudo no monolito.
- Validar backend com `.\mvnw.cmd "-Dtest=DashboardDiretorControllerIntegrationTest" test` e `.\mvnw.cmd test`.
