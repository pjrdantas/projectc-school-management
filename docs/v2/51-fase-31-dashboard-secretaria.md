# Fase 31 - Dashboard da secretaria

## Objetivo

Separar a primeira visao operacional de dashboard por publico, iniciando pela secretaria.

## Endpoint

- `GET /api/dashboard/secretaria`

## Indicadores entregues

- Total de matriculas.
- Matriculas solicitadas.
- Matriculas em andamento.
- Matriculas aguardando documentos.
- Matriculas aguardando historico escolar.
- Matriculas com documentos obrigatorios pendentes.
- Matriculas aptas para rematricula.
- Boletins fechados.
- Historicos internos gerados.
- Transferencias.
- Solicitacoes de exclusao pendentes.
- Distribuicao de matriculas por status.
- Turmas com vagas disponiveis.

## Decisoes

- O endpoint `GET /api/dashboard/academico` permanece como visao generica ja entregue na fase anterior.
- O endpoint `GET /api/dashboard/secretaria` explicita o publico-alvo e adiciona indicadores administrativos do enunciado v2.
- O Swagger usa tag explicita `Dashboard secretaria`, sem sufixo `controller`.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardSecretariaControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
