# Fase 48T - Consolidacao de EscolaContextoPort em matricula leitura e gateways auxiliares

## Objetivo

Consolidar os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares, apos a aplicacao pontual no resumo academico da matricula e nos gateways de consulta de aluno, periodo letivo e turma.

Esta fase e documental e de verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Estado consolidado

| Consumidor | Tipo de fluxo | Uso do contexto escolar | Contrato preservado |
| --- | --- | --- | --- |
| `MatriculaAcademicoResumoService` | Leitura academica da matricula | `obterContextoPadrao()` | `/api/matriculas/{matriculaId}/academico` |
| `AlunoConsultaPersistenceGateway` | Validacao de existencia de aluno por escola | `obterContextoPadrao()` | Interface `AlunoConsultaGateway` |
| `PeriodoLetivoConsultaPersistenceGateway` | Validacao de existencia de periodo letivo por escola | `obterContextoPadrao()` | Interface `PeriodoLetivoConsultaGateway` |
| `TurmaConsultaPersistenceGateway` | Consulta auxiliar de turma, periodo, capacidade e serie por escola | `obterContextoPadrao()` | Interface `TurmaConsultaGateway` |

## Verificacao de consistencia

- Os quatro consumidores consolidados usam `EscolaContextoPort` para resolver a escola padrao.
- As consultas permanecem filtradas por escola.
- Controllers, DTOs, requests, responses e interfaces de gateway permanecem inalterados.
- `MatriculaFluxoService` permanece fora do escopo por concentrar fluxo transacional de matricula.
- `MatriculaPersistenceGateway` permanece fora do escopo por concentrar persistencia e escrita de matricula.
- Nao ha alteracao de migrations.

## Limites atuais

- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario operacional com multiplas escolas ativas.
- Perfis e permissoes no contexto escolar ainda nao sao eixo de autorizacao dos fluxos de dominio.
- Ainda existem usos diretos de `EscolaTenantService` fora de matricula leitura e gateways auxiliares.
- Os usos remanescentes precisam ser separados por dominio, risco e tipo de fluxo antes de nova aplicacao pontual.

## Decisoes

- A consolidacao de `EscolaContextoPort` em matricula leitura e gateways auxiliares esta completa.
- Fluxos transacionais de matricula nao devem ser alterados nesta etapa.
- A proxima fase deve diagnosticar os usos remanescentes de `EscolaTenantService` antes de escolher novo candidato.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares estao consolidados e documentados, preservando comportamento externo e mantendo os fluxos transacionais fora do escopo.

## Proxima fase sugerida

Fase 48U - diagnostico dos usos remanescentes de `EscolaTenantService` e escolha do proximo candidato seguro.

Objetivo sugerido:

- Mapear usos remanescentes por dominio, risco e tipo de fluxo.
- Separar fluxos de leitura e validacao defensiva de fluxos transacionais, persistencia e seguranca.
- Escolher um unico candidato de baixo risco para a proxima aplicacao pontual.
- Validar backend completo com `.\mvnw.cmd test`.
