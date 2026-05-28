# 19 - Proximo Epico: Aulas, Professores, Notas e Dashboards

## Objetivo

Registrar o direcionamento do proximo epico funcional apos a aceitacao do MVP atual pelo cliente.

O foco deixa de ser validacao do fluxo inicial de matricula e passa a ser a operacao pedagogica diaria: aulas, professores, alunos em contexto de aula, notas, comportamento e dashboards de acompanhamento.

## Decisoes de produto

- O MVP atual foi considerado aceito pelo cliente dentro dos parametros desejados.
- Nao existe pendencia de decisao sobre matricula/status/cancelamento para o MVP atual.
- Historico, documentos e transferencia pertencem ao ciclo funcional ja desenvolvido e nao devem ser tratados como proximo epico.
- Permissoes granulares por rota, tela e endpoint serao implementadas no final do projeto, apos estabilizacao das funcionalidades principais.

## Escopo do proximo epico

### 1. Professores

Objetivo: permitir que a escola organize os professores que participam da operacao academica.

Possiveis capacidades:

- cadastro e manutencao de professores;
- vinculo de professor com turma/disciplina;
- consulta de carga academica por professor;
- visao operacional das turmas sob responsabilidade do professor.

### 2. Aulas planejadas e realizadas

Objetivo: registrar e acompanhar o contexto das aulas.

Possiveis capacidades:

- planejamento de aulas por turma/disciplina/professor;
- registro de aula realizada;
- conteudo previsto e conteudo aplicado;
- observacoes pedagogicas da aula;
- acompanhamento de aulas pendentes, realizadas e reprogramadas.

### 3. Alunos em contexto de aula

Objetivo: conectar a matricula do aluno com sua participacao real nas aulas.

Possiveis capacidades:

- listagem de alunos por turma/aula;
- registro de presenca ou participacao;
- anotacoes individuais no contexto da aula;
- acompanhamento de evolucao por aluno.

### 4. Notas e avaliacao

Objetivo: iniciar o acompanhamento academico de desempenho dos alunos.

Possiveis capacidades:

- cadastro de avaliacoes;
- lancamento de notas por aluno/turma/disciplina;
- consulta de notas por aluno;
- indicadores de evolucao academica;
- alertas operacionais para alunos com queda de desempenho.

### 5. Comportamento de alunos e professores

Objetivo: registrar eventos qualitativos relevantes para acompanhamento pedagogico e operacional.

Possiveis capacidades:

- ocorrencias comportamentais de alunos;
- observacoes de participacao e engajamento;
- registros operacionais relacionados a professores;
- indicadores por turma, aluno e professor.

### 6. Dashboards

Objetivo: transformar os dados operacionais em visoes de controle para acompanhamento da escola.

Dashboards iniciais sugeridos:

- aulas previstas x realizadas;
- turmas com maior quantidade de pendencias;
- evolucao de alunos por periodo/turma/disciplina;
- acompanhamento de notas;
- comportamento/ocorrencias por turma;
- matriculas por status e periodo;
- carga academica por professor.

## Fora deste epico

- matriz completa de permissoes por funcionalidade;
- financeiro avancado;
- mobile;
- notificacoes multicanal completas;
- relatorios gerenciais avancados fora dos dashboards iniciais.

## Ordem sugerida de entrega

1. Modelar professor e vinculo professor/turma/disciplina.
2. Modelar aula planejada e aula realizada.
3. Integrar alunos matriculados ao contexto de aula.
4. Implementar lancamento inicial de notas.
5. Implementar registros de comportamento/observacoes.
6. Construir dashboards operacionais iniciais.

## Criterio de sucesso

O epico sera considerado bem sucedido quando permitir:

- identificar quais professores atuam em quais turmas/disciplinas;
- planejar e registrar aulas;
- acompanhar alunos dentro do contexto de aula;
- registrar notas e acompanhar evolucao academica;
- registrar informacoes comportamentais relevantes;
- visualizar dashboards iniciais de aulas, matriculas e evolucao dos alunos.
