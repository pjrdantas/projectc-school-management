# Fase 45M - Ajuste do dashboard ADMIN e massa completa de testes

## Objetivo

Corrigir a rolagem vertical do dashboard/Home do ADMIN em 1920x1080 e preparar
uma massa local completa para validar os fluxos atualmente disponiveis no
sistema.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Home academica`.
- Compactacao do dashboard ADMIN:
  - metricas menores;
  - bloco de preferencias reduzido;
  - graficos com menor altura;
  - lista de turmas com vagas limitada a pre-visualizacao de 5 itens;
  - indicador de quantidade restante quando houver mais turmas.
- Criado script SQL local:
  - `docs/sql/seed-massa-completa-testes.sql`.
- A massa contempla:
  - usuarios de secretaria, diretor e professores;
  - periodos letivos, series, turmas e disciplinas;
  - alunos e responsaveis;
  - vinculos aluno-responsavel;
  - funcionarios e professores;
  - alocacoes professor/turma/disciplina;
  - matriculas em diferentes status;
  - etapas e documentos de matricula;
  - historicos escolares e transferencias;
  - planejamentos, aulas, frequencias, avaliacoes, notas e boletins;
  - snapshots de dashboards.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- A massa de testes fica fora do Flyway e pode ser reaplicada manualmente.
- O script e idempotente e limpa apenas os registros de teste criados por ele.
- UUIDs seguem internos no script e nao sao expostos na interface.

## Validacao

- Massa aplicada em `gestao_escolar` via `psql`.
- `npm run build` em `school-management-web/microfrontend`.
- Validacao no navegador em 1920x1080:
  - `bodyScrollHeight`: 1080;
  - `bodyClientHeight`: 1080;
  - `cardHeight`: 974;
  - `scrollableCount`: 0.

## Proxima fase sugerida

Fase 45N: revisar no navegador os fluxos principais usando a massa aplicada,
comecando por matriculas, professores, aulas, avaliacoes e snapshots.
