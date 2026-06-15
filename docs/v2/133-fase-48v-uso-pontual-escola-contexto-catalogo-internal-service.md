# Fase 48V - Uso pontual de EscolaContextoPort em CatalogoAcademicoInternalService

## Objetivo

Aplicar `EscolaContextoPort` em `CatalogoAcademicoInternalService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um service interno de leitura e validacao de catalogos academicos.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `CatalogoAcademicoInternalService` passou a depender de `EscolaContextoPort`.
- `CatalogoAcademicoPort` foi preservado.
- `EstruturaTurmaPort` foi preservado.
- O fallback de `resolverEscolaId(UUID escolaId)` continua usando a escola padrao quando `escolaId` nao e informado.
- Controllers, DTOs, requests, responses e contratos HTTP permaneceram inalterados.

## Decisoes

- A troca ficou limitada ao ponto de resolucao de escola padrao.
- Gateways de persistencia de catalogo permaneceram fora do escopo.
- Use cases transacionais e de seguranca permaneceram fora do escopo.
- Nao houve alteracao de migrations.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=AcademicCatalogControllerIntegrationTest,DashboardAcademicoControllerIntegrationTest,PlanejamentoBimestralControllerIntegrationTest,AulaControllerIntegrationTest,AvaliacaoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

`CatalogoAcademicoInternalService` passa a consumir a fronteira interna de contexto escolar sem alterar os contratos internos de catalogo, os contratos HTTP ou o comportamento externo.

## Estado apos Fase 48W

A Fase 48W consolidou a matriz documental dos contratos internos de catalogo, confirmando que `CatalogoAcademicoInternalService` usa `EscolaContextoPort` e preserva `CatalogoAcademicoPort` e `EstruturaTurmaPort`.

## Proxima fase sugerida

Fase 48W - consolidacao de `EscolaContextoPort` nos contratos internos de catalogo.

Objetivo sugerido:

- Atualizar a matriz documental de consumidores de `EscolaContextoPort`.
- Confirmar que `CatalogoAcademicoInternalService` nao depende mais diretamente de `EscolaTenantService`.
- Reavaliar os proximos candidatos fora de catalogo interno sem tocar persistencia ou seguranca.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48W

Fase 48X - diagnostico dos consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService`.
