# Epicos e Historias

## Objetivo

Este documento consolida os epicos iniciais e as primeiras historias do sistema de gestao escolar.

## Epico 1. Autenticacao e Controle de Acesso

### Objetivo

Permitir acesso seguro ao sistema. A matriz granular de autorizacao por perfil sera refinada no final do projeto, apos estabilizacao das funcionalidades principais.

### Historias

Historia 1:
Como administrador, quero autenticar no sistema para acessar as funcionalidades administrativas com seguranca.

Historia 2:
Como administrador, quero manter perfis basicos de acesso para organizar a operacao inicial, deixando a matriz granular de permissoes para o final do projeto.

Historia 3:
Como usuario operacional, quero recuperar minha senha para voltar a acessar o sistema sem depender de suporte manual.

## Epico 2. Cadastro Academico

### Objetivo

Manter os dados academicos estruturais usados pelos processos escolares.

### Historias

Historia 4:
Como administrador academico, quero cadastrar um periodo letivo para organizar as matriculas por ciclo escolar.

Historia 5:
Como administrador academico, quero cadastrar turmas para disponibilizar vagas por periodo e organizacao escolar.

Historia 6:
Como administrador academico, quero vincular disciplinas a turmas para refletir a estrutura academica ofertada.

## Epico 3. Gestao de Alunos e Responsaveis

### Objetivo

Centralizar os dados de alunos e seus responsaveis.

### Historias

Historia 7:
Como atendente, quero cadastrar um aluno para que ele possa participar dos processos academicos.

Historia 8:
Como atendente, quero cadastrar os responsaveis de um aluno para manter os contatos e obrigacoes legais atualizados.

Historia 9:
Como atendente, quero consultar o cadastro de um aluno para confirmar seus dados antes de realizar a matricula.

## Epico 4. Gestao de Matricula

### Objetivo

Permitir o ciclo inicial de matricula com controle de status e consulta.

### Historias

Historia 10:
Como atendente, quero matricular um aluno em uma turma para formalizar sua vaga no periodo letivo.

Historia 11:
Como atendente, quero consultar o status de uma matricula para acompanhar sua situacao operacional.

Historia 12:
Como atendente, quero cancelar uma matricula para corrigir situacoes em que a vaga nao deve mais permanecer ativa.

Historia 13:
Como administrador academico, quero visualizar o historico de status da matricula para auditar alteracoes relevantes.

## Epico 5. Portal Operacional e Consultas

### Objetivo

Permitir pesquisa e acompanhamento rapido dos registros escolares principais.

### Historias

Historia 14:
Como usuario operacional, quero pesquisar matriculas por aluno, turma e periodo para localizar registros rapidamente.

Historia 15:
Como usuario operacional, quero filtrar matriculas por status para priorizar meu trabalho diario.

## Epico 6. Aulas, Professores, Notas e Dashboards

### Objetivo

Organizar a operacao pedagogica diaria envolvendo professores, aulas, alunos, notas, comportamento e dashboards de acompanhamento.

### Historias

Historia 16:
Como administrador academico, quero cadastrar professores e vincula-los a turmas/disciplinas para organizar a operacao de aulas.

Historia 17:
Como professor ou administrador academico, quero planejar e registrar aulas para acompanhar o que foi previsto e realizado.

Historia 18:
Como professor, quero visualizar os alunos da turma no contexto da aula para registrar participacao, observacoes e acompanhamento.

Historia 19:
Como professor, quero lancar notas dos alunos para acompanhar o desempenho academico.

Historia 20:
Como equipe pedagogica, quero registrar eventos comportamentais de alunos e professores para apoiar o acompanhamento escolar.

Historia 21:
Como gestor, quero dashboards de aulas, matriculas e evolucao de alunos para acompanhar a operacao da escola.

## Epicos Posteriores

Os epicos abaixo sao esperados para fases seguintes:
- financeiro escolar;
- comunicacao e notificacoes;
- relatorios e auditoria avancados;
- portal do aluno ou responsavel;
- mobile, se houver demanda real;
- matriz granular de permissoes, ao final do projeto.
