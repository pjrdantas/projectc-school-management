# Fase 33 - Dashboard do diretor

## Objetivo

Separar a visao executiva de dashboard para direcao, consolidando indicadores academicos, administrativos e operacionais ja estabilizados nas fases anteriores.

## Endpoint

- `GET /api/dashboard/diretor`

## Indicadores entregues

- Total de matriculas.
- Matriculas pendentes, somando solicitadas, em andamento, aguardando documentos e aguardando historico escolar.
- Matriculas concluidas.
- Matriculas efetivadas.
- Matriculas aptas para rematricula.
- Alunos ativos.
- Alunos inativos.
- Turmas ativas.
- Turmas lotadas.
- Professores alocados.
- Aulas realizadas.
- Avaliacoes registradas.
- Avaliacoes com notas pendentes.
- Boletins fechados.
- Historicos internos gerados.
- Transferencias.
- Solicitacoes de exclusao pendentes.
- Matriculas com documentos obrigatorios pendentes.
- Distribuicao de matriculas por status.
- Turmas com vagas disponiveis.

## Decisoes

- A visao do diretor reaproveita os servicos dos dashboards academico e secretaria como base consolidada.
- Turma lotada considera turma ativa cuja capacidade e menor ou igual ao numero de matriculas que ocupam vaga.
- Avaliacao com nota pendente considera avaliacao sem nenhum lancamento em `nota_aluno`.
- O Swagger usa tag explicita `Dashboard diretor`, sem sufixo `controller`.
- Nao houve alteracao em Maven, Flyway ou schema.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardDiretorControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
