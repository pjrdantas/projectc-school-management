# Fase 48R - Consolidacao do resumo academico da matricula

## Objetivo

Consolidar o uso de `EscolaContextoPort` em `MatriculaAcademicoResumoService` e mapear os proximos fluxos de leitura candidatos antes de qualquer refatoracao transacional de matricula.

Esta fase e documental e de verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Estado consolidado

| Consumidor | Tipo de fluxo | Uso do contexto escolar | Contrato preservado |
| --- | --- | --- | --- |
| `MatriculaAcademicoResumoService` | Leitura academica da matricula | `obterContextoPadrao()` | `/api/matriculas/{matriculaId}/academico` |

## Verificacao de consistencia

- O resumo academico da matricula usa `EscolaContextoPort` apenas para obter `escolaId`.
- O filtro por matricula e escola continua sendo feito nas consultas existentes.
- As consultas de frequencias e notas continuam filtradas por matricula e escola.
- Controller, DTOs e contrato HTTP permanecem inalterados.
- O fluxo continua separado de criacao, edicao e alteracao de status de matricula.

## Proximos candidatos mapeados

| Candidato | Tipo de uso atual | Risco | Observacao |
| --- | --- | --- | --- |
| `AlunoConsultaPersistenceGateway` | Validacao de existencia de aluno por escola | Baixo | Consulta simples usada por matricula |
| `PeriodoLetivoConsultaPersistenceGateway` | Validacao de existencia de periodo letivo por escola | Baixo | Consulta simples usada por matricula |
| `TurmaConsultaPersistenceGateway` | Consulta de turma, periodo, capacidade e serie por escola | Baixo a medio | Possui mais metodos, mas ainda e gateway de leitura |

## Onde ainda nao mexer

- `MatriculaFluxoService`.
- `MatriculaPersistenceGateway`.
- Criacao de matricula.
- Atualizacao de status de matricula.
- Fluxos de documento ou historico vinculados a matricula.

Motivo:

- Esses pontos participam de escrita, regras transacionais ou composicoes maiores.
- A fase atual busca manter a evolucao de contexto escolar em fluxos de leitura e validacao defensiva.

## Decisoes

- O proximo passo deve permanecer dentro do monolito.
- Nao ha justificativa atual para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente runtime.
- Os gateways auxiliares de consulta de matricula sao candidatos melhores do que services transacionais.
- A validacao deve cobrir `MatriculaControllerIntegrationTest`, porque esses gateways participam do fluxo principal de matricula.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

O uso de `EscolaContextoPort` no resumo academico da matricula esta consolidado e o proximo passo seguro foi identificado sem ampliar o escopo para refatoracao transacional.

## Proxima fase sugerida

Fase 48S - uso pontual de `EscolaContextoPort` nos gateways auxiliares de consulta de matricula.

Objetivo sugerido:

- Aplicar `EscolaContextoPort` em `AlunoConsultaPersistenceGateway`.
- Aplicar `EscolaContextoPort` em `PeriodoLetivoConsultaPersistenceGateway`.
- Aplicar `EscolaContextoPort` em `TurmaConsultaPersistenceGateway`.
- Preservar interfaces de gateway e comportamento externo.
- Validar com `.\mvnw.cmd "-Dtest=MatriculaControllerIntegrationTest" test`.
- Validar backend completo com `.\mvnw.cmd test`.
