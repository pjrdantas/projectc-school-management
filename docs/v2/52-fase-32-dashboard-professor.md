# Fase 32 - Dashboard do professor

## Objetivo

Separar a visao de dashboard do professor por docente, usando os fluxos ja existentes de alocacao, aula, frequencia, avaliacao, notas e planejamento.

## Endpoint

- `GET /api/dashboard/professores/{professorId}`

## Indicadores entregues

- Turmas vinculadas ao professor.
- Alocacoes ativas.
- Aulas planejadas.
- Aulas realizadas.
- Frequencias pendentes.
- Avaliacoes registradas.
- Avaliacoes com notas pendentes.
- Planejamentos bimestrais.
- Planejamentos bimestrais pendentes de aprovacao.
- Lista de turmas/disciplinas vinculadas.

## Decisoes

- A visao e parametrizada por `professorId`, pois os indicadores dependem do professor autenticado ou selecionado.
- Frequencia pendente considera aula realizada sem registro de frequencia do professor.
- Notas pendentes considera avaliacao sem nenhum lancamento em `nota_aluno`.
- O Swagger usa tag explicita `Dashboard professor`, sem sufixo `controller`.
- Nao houve alteracao em Maven, Flyway ou schema.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardProfessorControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
