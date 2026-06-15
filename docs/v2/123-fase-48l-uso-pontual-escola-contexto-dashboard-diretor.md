# Fase 48L - Uso pontual de EscolaContextoPort no dashboard diretor

## Objetivo

Aplicar `EscolaContextoPort` em `DashboardDiretorService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em fluxos de leitura e agregacao.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `DashboardDiretorService` passou a depender de `EscolaContextoPort`.
- O contexto escolar padrao passou a ser resolvido por `obterContextoPadrao()`.
- `escolaId` e `escolaNome` usados no response passaram a vir de `EscolaContexto`.
- As composicoes com dashboards academico e secretaria foram preservadas.
- As consultas agregadas especificas do dashboard diretor continuam usando os repositories existentes.

## Decisoes

- O contrato publico de `/api/dashboard/diretor` foi preservado.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla dos dashboards.
- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao do contexto escolar.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=DashboardDiretorControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

O dashboard diretor passa a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico e secretaria sem alterar comportamento externo.

## Proxima fase sugerida

Fase 48M - consolidacao dos dashboards com `EscolaContextoPort`.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Verificar os usos atuais em dashboards academico, secretaria e diretor.
- Identificar o proximo candidato seguro sem refatoracao ampla.
- Validar backend com `.\mvnw.cmd test`.
