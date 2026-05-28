# Sprint 1 Backlog

## Objetivo

Implementar o primeiro fluxo ponta a ponta do sistema de gestao escolar,
estabelecendo a base funcional e tecnica do produto real.

## Fluxo da Sprint 1

Fluxo priorizado:
- autenticacao basica;
- cadastro de aluno;
- cadastro de turma e periodo;
- matricula;
- consulta de matricula.

## Historias da Sprint 1

### H1. Autenticacao basica

Como administrador ou usuario operacional, quero autenticar no sistema
para acessar as funcionalidades permitidas pelo meu perfil.

### H2. Cadastro de aluno

Como atendente, quero cadastrar um aluno
para que ele possa ser matriculado.

### H3. Cadastro de periodo letivo e turma

Como administrador academico, quero cadastrar periodo letivo e turma
para disponibilizar vagas para matricula.

### H4. Matricula de aluno

Como atendente, quero matricular um aluno em uma turma e periodo letivo
para formalizar sua vaga.

### H5. Consulta operacional de matricula

Como usuario operacional, quero consultar matriculas por filtros basicos
para localizar rapidamente os registros.

## Escopo Tecnico da Sprint 1

Esta sprint nao cobre:
- recuperacao de senha;
- perfis complexos e matriz completa de permissoes;
- financeiro;
- notificacoes;
- mobile;
- integracoes assincronas avancadas;
- relatorios avancados.

## Backlog Tecnico de Backend

### B1. Estruturar projeto backend

- criar esqueleto do projeto real em Spring Boot;
- definir organizacao modular inicial;
- configurar gerenciamento de dependencias;
- configurar perfil local;
- preparar estrutura de pacotes por contexto.

### B2. Configurar infraestrutura base

- configurar conexao com PostgreSQL;
- configurar migracoes com Flyway ou Liquibase;
- configurar tratamento global de erros;
- configurar logs basicos;
- configurar endpoint de healthcheck.

### B3. Implementar modulo `access-control`

- modelar usuario;
- modelar perfil inicial;
- implementar autenticacao basica;
- definir estrategia de emissao e validacao de token;
- proteger endpoints por autenticacao.

### B4. Implementar modulo `student-management`

- modelar entidade de aluno;
- criar migration da tabela de aluno;
- implementar caso de uso de cadastro de aluno;
- implementar endpoint de cadastro de aluno;
- implementar endpoint de consulta de aluno por identificador.

### B5. Implementar modulo `academic-catalog`

- modelar entidade de periodo letivo;
- modelar entidade de turma;
- criar migrations correspondentes;
- implementar cadastro de periodo letivo;
- implementar cadastro de turma;
- implementar endpoints de consulta basica.

### B6. Implementar modulo `enrollment`

- modelar entidade de matricula;
- definir estados iniciais da matricula;
- criar migration da tabela de matricula;
- implementar caso de uso de matricular aluno;
- validar existencia de aluno, turma e periodo antes da matricula;
- implementar cancelamento basico de matricula, se couber na sprint;
- implementar consulta de matricula.

### B7. Implementar filtros operacionais

- consultar matriculas por aluno;
- consultar matriculas por turma;
- consultar matriculas por periodo letivo;
- consultar matriculas por status.

### B8. Testes de backend

- criar testes unitarios para casos de uso centrais;
- criar testes de integracao para endpoints principais;
- validar fluxo de matricula ponta a ponta no backend.

## Backlog Tecnico de Frontend

### F1. Estruturar projeto frontend

- criar projeto Angular 20;
- definir arquitetura por features;
- configurar roteamento inicial;
- configurar ambiente local;
- definir biblioteca base de componentes.

### F2. Implementar autenticacao inicial

- criar tela de login;
- integrar login com backend;
- armazenar sessao/token;
- proteger rotas autenticadas;
- criar mecanismo de logout.

### F3. Implementar feature de aluno

- criar tela de cadastro de aluno;
- criar formulario reativo;
- validar campos obrigatorios;
- integrar com endpoint de cadastro;
- exibir retorno de sucesso e erro.

### F4. Implementar feature de periodo e turma

- criar tela de cadastro de periodo letivo;
- criar tela de cadastro de turma;
- integrar formularios com backend;
- listar opcoes necessarias para a matricula.

### F5. Implementar feature de matricula

- criar tela de matricula;
- permitir selecionar aluno, turma e periodo;
- integrar com endpoint de matricula;
- exibir status da operacao.

### F6. Implementar consulta operacional

- criar tela de consulta de matriculas;
- implementar filtros basicos;
- exibir lista paginada ou equivalente;
- permitir consulta de status da matricula.

### F7. Base de UX e navegacao

- criar layout administrativo inicial;
- definir menu principal;
- definir navegacao entre modulos da sprint;
- padronizar feedback visual de erro e sucesso.

## Dependencias

### Backend antes do frontend

Itens idealmente disponiveis antes da integracao principal do frontend:
- autenticacao inicial;
- endpoints de cadastro de aluno;
- endpoints de cadastro de periodo e turma;
- endpoint de matricula;
- endpoint de consulta de matriculas.

### Dependencias internas do backend

- autenticacao depende da definicao de usuario e perfil inicial;
- matricula depende da existencia de aluno, turma e periodo;
- consulta depende do modelo de matricula e filtros definidos.

## Criterios de Aceite Tecnicos

### Autenticacao

- usuario autenticado consegue acessar endpoints protegidos;
- usuario nao autenticado nao acessa endpoints protegidos;
- token ou sessao possui comportamento consistente no frontend.

### Cadastro de aluno

- aluno pode ser cadastrado com validacoes minimas;
- dados invalidos retornam erro padronizado;
- aluno cadastrado pode ser recuperado para uso na matricula.

### Cadastro academico

- periodo letivo pode ser cadastrado;
- turma pode ser cadastrada e associada ao contexto academico necessario;
- registros cadastrados podem ser utilizados na tela de matricula.

### Matricula

- nao e possivel matricular aluno inexistente;
- nao e possivel matricular em turma inexistente;
- matricula valida e persistida corretamente;
- status inicial da matricula fica consistente com a regra definida.

### Consulta operacional

- matriculas podem ser consultadas por filtros basicos;
- retorno da consulta e consistente com os dados persistidos;
- frontend apresenta os resultados de forma compreensivel.

## Ordem Recomendada de Execucao

### Backend

1. estrutura do projeto e infraestrutura base;
2. autenticacao inicial;
3. cadastro de aluno;
4. cadastro de periodo letivo e turma;
5. matricula;
6. consulta operacional;
7. testes.

### Frontend

1. estrutura do projeto e autenticacao;
2. cadastro de aluno;
3. cadastro de periodo e turma;
4. matricula;
5. consulta operacional;
6. refinamento de navegacao e feedback visual.

## Riscos da Sprint 1

- tentar incluir regras demais no primeiro fluxo;
- atrasar o frontend por falta de contrato claro de API;
- atrasar o backend por falta de decisao sobre autenticacao;
- misturar modulos sem respeitar ownership do dominio;
- tentar introduzir eventos e integracoes avancadas antes da necessidade.

## Definicao de Pronto da Sprint 1

A Sprint 1 sera considerada pronta quando:
- o usuario conseguir autenticar;
- um aluno puder ser cadastrado;
- um periodo e uma turma puderem ser cadastrados;
- uma matricula puder ser realizada;
- a matricula puder ser consultada no frontend;
- backend e frontend estiverem integrados nesse fluxo;
- os testes essenciais do fluxo principal estiverem executando.
