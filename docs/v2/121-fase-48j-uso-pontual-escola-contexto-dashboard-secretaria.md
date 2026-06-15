# Fase 48J - Uso pontual de EscolaContextoPort no dashboard secretaria

## Objetivo

Aplicar `EscolaContextoPort` em um fluxo majoritariamente de leitura, usando o dashboard secretaria como ponto seguro de evolucao incremental das fronteiras internas.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `DashboardSecretariaService` passou a depender de `EscolaContextoPort`.
- O contexto escolar padrao passou a ser resolvido por `obterContextoPadrao()`.
- `escolaId` e `escolaNome` usados no response passaram a vir de `EscolaContexto`.
- As consultas agregadas do dashboard secretaria continuam usando os repositories existentes.

## Decisoes

- O contrato publico de `/api/dashboard/secretaria` foi preservado.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla dos dashboards.
- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao do contexto escolar.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=DashboardSecretariaControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

O dashboard secretaria passa a consumir a fronteira interna de contexto escolar, alinhando-se ao padrao ja adotado pelo dashboard academico, sem alterar comportamento externo.

## Proxima fase sugerida

Fase 48K - consolidacao dos usos de EscolaContextoPort em dashboards.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Verificar os usos atuais em dashboards academico e secretaria.
- Manter tudo no monolito.
- Validar backend com `.\mvnw.cmd test`.
