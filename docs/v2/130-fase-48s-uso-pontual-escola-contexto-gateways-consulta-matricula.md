# Fase 48S - Uso pontual de EscolaContextoPort nos gateways auxiliares de matricula

## Objetivo

Aplicar `EscolaContextoPort` nos gateways auxiliares de consulta de matricula, mantendo a evolucao incremental do contexto escolar em pontos de leitura e validacao defensiva.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `AlunoConsultaPersistenceGateway` passou a depender de `EscolaContextoPort`.
- `PeriodoLetivoConsultaPersistenceGateway` passou a depender de `EscolaContextoPort`.
- `TurmaConsultaPersistenceGateway` passou a depender de `EscolaContextoPort`.
- As interfaces de gateway foram preservadas.
- As consultas por aluno, periodo letivo e turma continuaram filtradas por escola.
- `MatriculaFluxoService` e `MatriculaPersistenceGateway` permaneceram fora do escopo.

## Decisoes

- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao de `escolaId`.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla de matricula.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=MatriculaControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

Os gateways auxiliares de consulta de matricula passam a consumir a fronteira interna de contexto escolar sem alterar comportamento externo.

## Estado apos Fase 48T

A Fase 48T consolidou os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares, confirmando que `MatriculaAcademicoResumoService`, `AlunoConsultaPersistenceGateway`, `PeriodoLetivoConsultaPersistenceGateway` e `TurmaConsultaPersistenceGateway` usam a porta interna de contexto escolar.

## Proxima fase sugerida

Fase 48T - consolidacao dos usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares.

Objetivo sugerido:

- Consolidar a matriz documental com os novos consumidores de matricula.
- Confirmar que `MatriculaAcademicoResumoService` e os gateways auxiliares usam `EscolaContextoPort`.
- Confirmar que `MatriculaFluxoService` e `MatriculaPersistenceGateway` continuam fora do escopo por serem transacionais.
- Mapear o proximo candidato seguro fora de matricula.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48T

Fase 48U - diagnostico dos usos remanescentes de `EscolaTenantService` e escolha do proximo candidato seguro.
