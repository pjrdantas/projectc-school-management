# Configuracao do Jira

## Objetivo

Este documento traduz os artefatos de produto e arquitetura da pasta `docs/`
em uma estrutura pratica de Jira para o projeto de gestao escolar.

## Recomendacao Objetiva

Para este momento do projeto, a recomendacao e:

- criar um novo projeto do tipo `Jira Software`;
- escolher o template `Scrum`;
- usar `Team-managed` se o time ainda for pequeno e voce quiser configurar tudo com menos atrito;
- arquivar ou abandonar o projeto criado no template errado, caso ele ainda esteja vazio ou quase vazio.

Motivo:

- a documentacao ja esta organizada em MVP, epicos, historias e sprints;
- existe backlog orientado a Sprint 0 e Sprint 1;
- o modo Scrum encaixa melhor no planejamento ja registrado.

## Nome Sugerido do Projeto

- Nome: `Gestao Escolar`
- Key: `GES`

Se quiser manter o nome mais proximo do repositorio:

- Nome: `Academic Core`
- Key: `ACD`

## Estrutura Minima de Tipos de Issue

Usar pelo menos:

- `Epic`
- `Story`
- `Task`
- `Bug`
- `Subtask`

Opcional:

- `Spike` para investigacoes tecnicas curtas.

## Workflow Recomendado

Fluxo simples e suficiente para o inicio:

- `Backlog`
- `Selected for Development`
- `In Progress`
- `In Review`
- `Blocked`
- `Done`

Se quiser reduzir ainda mais:

- `Backlog`
- `To Do`
- `In Progress`
- `Done`

## Componentes Recomendados

Com base na arquitetura inicial:

- `access-control`
- `student-management`
- `academic-catalog`
- `enrollment`
- `frontend`
- `backend`
- `devops`
- `docs`

## Labels Recomendadas

- `mvp`
- `sprint-0`
- `sprint-1`
- `backend`
- `frontend`
- `auth`
- `student`
- `academic`
- `enrollment`

## Epicos do MVP

Criar os seguintes epicos no Jira:

1. `Autenticacao e Controle de Acesso`
2. `Cadastro Academico`
3. `Gestao de Alunos e Responsaveis`
4. `Gestao de Matricula`
5. `Portal Operacional e Consultas`

Epicos posteriores, para backlog futuro:

6. `Financeiro Escolar`
7. `Comunicacao e Notificacoes`
8. `Relatorios e Auditoria`

## Historias Iniciais Por Epico

### Epic: Autenticacao e Controle de Acesso

- Como administrador, quero autenticar no sistema para acessar as funcionalidades administrativas com seguranca.
- Como administrador, quero cadastrar perfis de acesso para limitar o que cada usuario pode visualizar e alterar.
- Como usuario operacional, quero recuperar minha senha para voltar a acessar o sistema sem depender de suporte manual.

### Epic: Cadastro Academico

- Como administrador academico, quero cadastrar um periodo letivo para organizar as matriculas por ciclo escolar.
- Como administrador academico, quero cadastrar turmas para disponibilizar vagas por periodo e organizacao escolar.
- Como administrador academico, quero vincular disciplinas a turmas para refletir a estrutura academica ofertada.

### Epic: Gestao de Alunos e Responsaveis

- Como atendente, quero cadastrar um aluno para que ele possa participar dos processos academicos.
- Como atendente, quero cadastrar os responsaveis de um aluno para manter os contatos e obrigacoes legais atualizados.
- Como atendente, quero consultar o cadastro de um aluno para confirmar seus dados antes de realizar a matricula.

### Epic: Gestao de Matricula

- Como atendente, quero matricular um aluno em uma turma para formalizar sua vaga no periodo letivo.
- Como atendente, quero consultar o status de uma matricula para acompanhar sua situacao operacional.
- Como atendente, quero cancelar uma matricula para corrigir situacoes em que a vaga nao deve mais permanecer ativa.
- Como administrador academico, quero visualizar o historico de status da matricula para auditar alteracoes relevantes.

### Epic: Portal Operacional e Consultas

- Como usuario operacional, quero pesquisar matriculas por aluno, turma e periodo para localizar registros rapidamente.
- Como usuario operacional, quero filtrar matriculas por status para priorizar meu trabalho diario.

## Como Organizar Sprint 0

Se a Sprint 0 ja aconteceu como fase de preparacao, voce tem duas opcoes validas:

### Opcao A. Registrar a Sprint 0 no Jira

Use esta opcao se quiser historico de planejamento.

Criar uma sprint chamada:

- `Sprint 0 - Fundacao do Produto`

Adicionar tasks como:

- validar escopo do MVP;
- revisar epicos e historias;
- fechar decisoes arquiteturais;
- consolidar arquitetura inicial;
- preparar repositorio;
- planejar Sprint 1.

### Opcao B. Nao registrar a Sprint 0 como sprint operacional

Use esta opcao se o Jira vai passar a ser usado apenas a partir da implementacao.

Nesse caso:

- deixar a Sprint 0 documentada apenas em `docs/05-sprint-0.md`;
- iniciar o Jira operacionalmente pela Sprint 1.

## Sprint 1 Recomendada

Criar a sprint:

- `Sprint 1 - Primeiro fluxo ponta a ponta`

Adicionar as historias:

1. `H1 - Autenticacao basica`
2. `H2 - Cadastro de aluno`
3. `H3 - Cadastro de periodo letivo e turma`
4. `H4 - Matricula de aluno`
5. `H5 - Consulta operacional de matricula`

## Subtasks Recomendadas da Sprint 1

### H1 - Autenticacao basica

- Backend: estruturar autenticacao inicial
- Backend: proteger endpoints
- Frontend: criar tela de login
- Frontend: integrar login com backend
- Frontend: proteger rotas autenticadas

### H2 - Cadastro de aluno

- Backend: modelar entidade aluno
- Backend: criar migration de aluno
- Backend: implementar endpoint de cadastro de aluno
- Frontend: criar tela de cadastro de aluno
- Frontend: validar formulario de aluno
- Frontend: integrar cadastro de aluno

### H3 - Cadastro de periodo letivo e turma

- Backend: modelar periodo letivo
- Backend: modelar turma
- Backend: criar migrations academicas
- Backend: implementar endpoints de cadastro academico
- Frontend: criar tela de periodo letivo
- Frontend: criar tela de turma
- Frontend: integrar formularios academicos

### H4 - Matricula de aluno

- Backend: modelar matricula
- Backend: definir estados iniciais da matricula
- Backend: implementar caso de uso de matricula
- Backend: validar aluno, turma e periodo
- Frontend: criar tela de matricula
- Frontend: integrar tela de matricula

### H5 - Consulta operacional de matricula

- Backend: implementar filtros por aluno
- Backend: implementar filtros por turma
- Backend: implementar filtros por periodo letivo
- Backend: implementar filtros por status
- Frontend: criar tela de consulta
- Frontend: exibir resultados e status

## Campos que Valem a Pena Preencher

Em cada issue, preencher pelo menos:

- `Epic Link` ou equivalente do projeto;
- `Component`;
- `Labels`;
- `Story Points`, se o time for estimar;
- `Assignee`, quando houver dono claro.

## Priorizacao Inicial

Ordem sugerida:

1. `Autenticacao e Controle de Acesso`
2. `Gestao de Alunos e Responsaveis`
3. `Cadastro Academico`
4. `Gestao de Matricula`
5. `Portal Operacional e Consultas`

Observacao:
para fins de implementacao da Sprint 1, `Cadastro Academico` e `Gestao de Alunos` andam antes da `Matricula`,
mesmo que todos facam parte do mesmo fluxo funcional.

## Board Recomendado

Um board Scrum unico ja atende bem o inicio do projeto.

Colunas sugeridas:

- `Backlog`
- `Selected for Development`
- `In Progress`
- `In Review`
- `Blocked`
- `Done`

## O Que Nao Fazer Agora

- nao criar muitos workflows complexos;
- nao abrir um projeto separado para backend e outro para frontend;
- nao criar estados demais;
- nao cadastrar todos os modulos futuros como sprint ativa;
- nao tentar modelar financeiro, notificacoes e mobile no mesmo ciclo do MVP.

## Decisao Recomendada para o Projeto Criado no Template Errado

Se o projeto criado por engano estiver vazio ou com pouca coisa:

- crie um novo projeto `Jira Software > Scrum`;
- recrie apenas o que fizer sentido;
- arquive o projeto anterior.

Se ele ja tiver issues relevantes:

- mantenha-o apenas como origem;
- mova ou recrie as issues no novo projeto Scrum;
- padronize naming, epicos e componentes no projeto novo.

## Checklist de Configuracao

1. Criar novo projeto Scrum.
2. Definir key do projeto.
3. Configurar tipos de issue.
4. Ajustar workflow simples.
5. Criar componentes.
6. Criar epicos do MVP.
7. Cadastrar historias iniciais.
8. Criar Sprint 1.
9. Quebrar historias em subtasks backend e frontend.
10. Priorizar backlog e iniciar a sprint.

## Fonte

Esta configuracao foi derivada principalmente de:

- `docs/01-mvp-gestao-escolar.md`
- `docs/02-epicos-e-historias.md`
- `docs/04-arquitetura-inicial.md`
- `docs/05-sprint-0.md`
- `docs/06-sprint-1-backlog.md`
