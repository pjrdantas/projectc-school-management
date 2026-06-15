# Fase 48Q - Uso pontual de EscolaContextoPort no resumo academico da matricula

## Objetivo

Aplicar `EscolaContextoPort` em `MatriculaAcademicoResumoService`, iniciando o uso da fronteira interna de contexto escolar fora da area de dashboards em um fluxo de leitura controlado.

Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Escopo implementado

- `MatriculaAcademicoResumoService` passou a depender de `EscolaContextoPort`.
- O contexto escolar padrao passou a ser resolvido por `obterContextoPadrao()`.
- O `escolaId` usado para buscar a matricula, frequencias e notas passou a vir de `EscolaContexto`.
- O filtro por matricula e escola foi preservado.
- As consultas de frequencia e notas por matricula e escola foram preservadas.

## Decisoes

- O contrato publico de `/api/matriculas/{matriculaId}/academico` foi preservado.
- Nao houve alteracao em controller, request ou response.
- Nao houve alteracao de migrations.
- Nao houve refatoracao ampla de matriculas.
- O uso de `EscolaContextoPort` ficou limitado ao ponto de resolucao do contexto escolar.
- O monolito continua sendo o componente runtime unico nesta fase.

## Validacao esperada

- `.\mvnw.cmd "-Dtest=MatriculaAcademicoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

## Resultado

O resumo academico da matricula passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo.

## Estado apos Fase 48R

A Fase 48R consolidou o consumo de `EscolaContextoPort` no resumo academico da matricula e mapeou os proximos candidatos de baixo risco em matricula: gateways auxiliares de consulta de aluno, periodo letivo e turma.

## Proxima fase sugerida

Fase 48R - consolidacao do uso de `EscolaContextoPort` em resumo academico da matricula e mapeamento de proximos fluxos de leitura.

Objetivo sugerido:

- Consolidar a matriz documental com o novo consumidor fora de dashboards.
- Confirmar que o fluxo de resumo academico permanece isolado de refatoracoes transacionais de matricula.
- Mapear proximos candidatos de leitura antes de tocar criacao, edicao ou alteracao de status de matricula.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48R

Fase 48S - uso pontual de `EscolaContextoPort` nos gateways auxiliares de consulta de matricula.
