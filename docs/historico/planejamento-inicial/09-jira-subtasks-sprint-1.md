# Jira Subtasks Sprint 1

## Como usar

Use este arquivo para criar as subtasks dentro de cada story da Sprint 1.

Sugestao:

- crie primeiro as `Stories`;
- abra cada story no Jira;
- adicione as subtasks listadas abaixo;
- se o time for pequeno, voce pode manter backend e frontend na mesma story sem exagerar na quantidade de subtasks;
- se quiser reduzir volume, crie apenas as subtasks marcadas como essenciais.

## Story H1 - Autenticacao basica

### Subtask 1

**Tipo:** Subtask

**Resumo:** Backend - Estruturar autenticacao inicial

**Story pai:** H1 - Autenticacao basica

**Descricao:**

Implementar a base inicial de autenticacao no backend para permitir login de usuarios e emissao do mecanismo de sessao ou token definido para o MVP.

Resultado esperado:
- estrutura inicial de autenticacao criada;
- login funcional no backend;
- resposta de autenticacao consistente para integracao com frontend.

**Labels sugeridas:** `backend`, `auth`, `sprint-1`

**Componentes sugeridos:** `access-control`, `backend`

### Subtask 2

**Tipo:** Subtask

**Resumo:** Backend - Proteger endpoints autenticados

**Story pai:** H1 - Autenticacao basica

**Descricao:**

Configurar a protecao dos endpoints do MVP para que apenas usuarios autenticados consigam acessar os recursos protegidos.

Resultado esperado:
- endpoints protegidos exigem autenticacao;
- acessos nao autenticados recebem erro padronizado;
- politica minima de autorizacao inicial aplicada.

**Labels sugeridas:** `backend`, `auth`, `sprint-1`

**Componentes sugeridos:** `access-control`, `backend`

### Subtask 3

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de login

**Story pai:** H1 - Autenticacao basica

**Descricao:**

Criar a tela inicial de login do sistema administrativo com os campos e estados minimos necessarios para autenticar o usuario.

Resultado esperado:
- formulario de login funcional;
- validacoes minimas de campos;
- feedback visual para sucesso e erro.

**Labels sugeridas:** `frontend`, `auth`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 4

**Tipo:** Subtask

**Resumo:** Frontend - Integrar login com backend

**Story pai:** H1 - Autenticacao basica

**Descricao:**

Integrar a tela de login com o endpoint de autenticacao do backend e tratar adequadamente a resposta de sucesso ou falha.

Resultado esperado:
- login consome a API do backend;
- resposta de autenticacao e armazenada corretamente;
- erros de autenticacao sao exibidos ao usuario.

**Labels sugeridas:** `frontend`, `auth`, `sprint-1`

**Componentes sugeridos:** `frontend`, `access-control`

### Subtask 5

**Tipo:** Subtask

**Resumo:** Frontend - Proteger rotas autenticadas e implementar logout

**Story pai:** H1 - Autenticacao basica

**Descricao:**

Implementar o controle de navegacao autenticada no frontend, protegendo rotas internas e disponibilizando mecanismo de logout.

Resultado esperado:
- rotas protegidas exigem sessao valida;
- usuario autenticado navega corretamente;
- logout encerra a sessao local.

**Labels sugeridas:** `frontend`, `auth`, `sprint-1`

**Componentes sugeridos:** `frontend`

## Story H2 - Cadastro de aluno

### Subtask 1

**Tipo:** Subtask

**Resumo:** Backend - Modelar entidade de aluno

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Definir o modelo inicial de aluno no backend com os atributos necessarios para o cadastro minimo do MVP.

Resultado esperado:
- entidade de aluno definida;
- regras basicas de validacao representadas;
- estrutura pronta para persistencia.

**Labels sugeridas:** `backend`, `student`, `sprint-1`

**Componentes sugeridos:** `student-management`, `backend`

### Subtask 2

**Tipo:** Subtask

**Resumo:** Backend - Criar migration da tabela de aluno

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Criar a migration de banco para persistir os dados de aluno de forma consistente com o modelo definido para o MVP.

Resultado esperado:
- tabela de aluno criada;
- versionamento de banco atualizado;
- ambiente local consegue subir com a estrutura correta.

**Labels sugeridas:** `backend`, `student`, `sprint-1`

**Componentes sugeridos:** `student-management`, `backend`

### Subtask 3

**Tipo:** Subtask

**Resumo:** Backend - Implementar endpoint de cadastro de aluno

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Implementar o caso de uso e o endpoint de cadastro de aluno no backend, incluindo validacoes minimas e retorno padronizado.

Resultado esperado:
- endpoint de cadastro disponivel;
- payload invalido retorna erro consistente;
- aluno e persistido corretamente.

**Labels sugeridas:** `backend`, `student`, `sprint-1`

**Componentes sugeridos:** `student-management`, `backend`

### Subtask 4

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de cadastro de aluno

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Criar a tela de cadastro de aluno com estrutura inicial de formulario para o MVP.

Resultado esperado:
- tela disponivel na navegacao;
- formulario com campos essenciais;
- layout pronto para integracao.

**Labels sugeridas:** `frontend`, `student`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 5

**Tipo:** Subtask

**Resumo:** Frontend - Validar formulario de aluno

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Implementar as validacoes minimas do formulario de cadastro de aluno e o tratamento visual de inconsistencias.

Resultado esperado:
- campos obrigatorios validados;
- mensagens de erro exibidas com clareza;
- submissao invalida bloqueada.

**Labels sugeridas:** `frontend`, `student`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 6

**Tipo:** Subtask

**Resumo:** Frontend - Integrar cadastro de aluno com backend

**Story pai:** H2 - Cadastro de aluno

**Descricao:**

Integrar o formulario de cadastro de aluno com o endpoint correspondente no backend, tratando respostas de sucesso e erro.

Resultado esperado:
- cadastro envia dados corretamente;
- resposta de sucesso e exibida;
- erros retornados pela API sao apresentados ao usuario.

**Labels sugeridas:** `frontend`, `student`, `sprint-1`

**Componentes sugeridos:** `frontend`, `student-management`

## Story H3 - Cadastro de periodo letivo e turma

### Subtask 1

**Tipo:** Subtask

**Resumo:** Backend - Modelar entidade de periodo letivo

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Definir o modelo inicial de periodo letivo com os atributos necessarios para o contexto academico basico do MVP.

Resultado esperado:
- entidade de periodo letivo definida;
- regras minimas mapeadas;
- estrutura pronta para persistencia.

**Labels sugeridas:** `backend`, `academic`, `sprint-1`

**Componentes sugeridos:** `academic-catalog`, `backend`

### Subtask 2

**Tipo:** Subtask

**Resumo:** Backend - Modelar entidade de turma

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Definir o modelo inicial de turma com os atributos necessarios para permitir organizacao academica e matricula no MVP.

Resultado esperado:
- entidade de turma definida;
- relacoes minimas consideradas;
- estrutura pronta para persistencia.

**Labels sugeridas:** `backend`, `academic`, `sprint-1`

**Componentes sugeridos:** `academic-catalog`, `backend`

### Subtask 3

**Tipo:** Subtask

**Resumo:** Backend - Criar migrations academicas

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Criar as migrations necessarias para persistir periodo letivo e turma no banco de dados.

Resultado esperado:
- tabelas academicas criadas;
- versionamento de banco atualizado;
- ambiente local compatibilizado com o modulo academico.

**Labels sugeridas:** `backend`, `academic`, `sprint-1`

**Componentes sugeridos:** `academic-catalog`, `backend`

### Subtask 4

**Tipo:** Subtask

**Resumo:** Backend - Implementar endpoints de cadastro academico

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Implementar os endpoints de cadastro e consulta basica para periodo letivo e turma.

Resultado esperado:
- endpoints disponiveis;
- validacoes minimas aplicadas;
- registros retornam de forma consistente para uso no frontend e na matricula.

**Labels sugeridas:** `backend`, `academic`, `sprint-1`

**Componentes sugeridos:** `academic-catalog`, `backend`

### Subtask 5

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de cadastro de periodo letivo

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Criar a tela de cadastro de periodo letivo com os campos essenciais definidos para o MVP.

Resultado esperado:
- tela acessivel pela navegacao;
- formulario disponivel para integracao;
- validacoes basicas previstas.

**Labels sugeridas:** `frontend`, `academic`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 6

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de cadastro de turma

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Criar a tela de cadastro de turma com os campos minimos necessarios para o fluxo academico da Sprint 1.

Resultado esperado:
- tela de turma criada;
- formulario preparado para integracao;
- layout coerente com a navegacao administrativa.

**Labels sugeridas:** `frontend`, `academic`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 7

**Tipo:** Subtask

**Resumo:** Frontend - Integrar formularios academicos com backend

**Story pai:** H3 - Cadastro de periodo letivo e turma

**Descricao:**

Integrar as telas de periodo letivo e turma com os endpoints do backend, tratando respostas e mensagens ao usuario.

Resultado esperado:
- cadastro academico funcional ponta a ponta;
- mensagens de sucesso e erro exibidas corretamente;
- registros podem ser usados no fluxo de matricula.

**Labels sugeridas:** `frontend`, `academic`, `sprint-1`

**Componentes sugeridos:** `frontend`, `academic-catalog`

## Story H4 - Matricula de aluno

### Subtask 1

**Tipo:** Subtask

**Resumo:** Backend - Modelar entidade de matricula

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Definir o modelo inicial de matricula com as relacoes e atributos minimos necessarios para o fluxo do MVP.

Resultado esperado:
- entidade de matricula definida;
- relacao com aluno, turma e periodo considerada;
- estrutura pronta para persistencia.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 2

**Tipo:** Subtask

**Resumo:** Backend - Definir estados iniciais da matricula

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Definir os estados iniciais e as regras basicas de status da matricula para a primeira versao do fluxo.

Resultado esperado:
- status inicial definido;
- transicoes minimas previstas;
- comportamento consistente para persistencia e consulta.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 3

**Tipo:** Subtask

**Resumo:** Backend - Implementar caso de uso de matricula

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Implementar o caso de uso e o endpoint de matricula no backend para formalizar a vaga do aluno.

Resultado esperado:
- endpoint de matricula disponivel;
- matricula valida persistida corretamente;
- resposta consistente para o frontend.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 4

**Tipo:** Subtask

**Resumo:** Backend - Validar aluno, turma e periodo na matricula

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Implementar as validacoes de existencia e consistencia dos dados antes da criacao da matricula.

Resultado esperado:
- aluno inexistente nao pode ser matriculado;
- turma inexistente nao pode ser usada;
- periodo inexistente nao pode ser usado;
- erros de validacao retornam de forma padronizada.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 5

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de matricula

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Criar a tela de matricula permitindo selecionar aluno, turma e periodo letivo no fluxo da Sprint 1.

Resultado esperado:
- tela de matricula disponivel;
- campos principais definidos;
- formulario pronto para integracao com backend.

**Labels sugeridas:** `frontend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 6

**Tipo:** Subtask

**Resumo:** Frontend - Integrar tela de matricula com backend

**Story pai:** H4 - Matricula de aluno

**Descricao:**

Integrar a tela de matricula com o endpoint do backend e tratar o resultado da operacao para o usuario.

Resultado esperado:
- matricula enviada corretamente;
- retorno de sucesso ou falha exibido;
- status inicial apresentado de forma compreensivel.

**Labels sugeridas:** `frontend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `frontend`, `enrollment`

## Story H5 - Consulta operacional de matricula

### Subtask 1

**Tipo:** Subtask

**Resumo:** Backend - Implementar filtro de matricula por aluno

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Implementar filtro de consulta de matriculas por aluno no backend.

Resultado esperado:
- consulta por aluno disponivel;
- retorno consistente com os dados persistidos.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 2

**Tipo:** Subtask

**Resumo:** Backend - Implementar filtro de matricula por turma

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Implementar filtro de consulta de matriculas por turma no backend.

Resultado esperado:
- consulta por turma disponivel;
- retorno consistente com os dados persistidos.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 3

**Tipo:** Subtask

**Resumo:** Backend - Implementar filtro de matricula por periodo letivo

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Implementar filtro de consulta de matriculas por periodo letivo no backend.

Resultado esperado:
- consulta por periodo disponivel;
- retorno consistente com os dados persistidos.

**Labels sugeridas:** `backend`, `academic`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 4

**Tipo:** Subtask

**Resumo:** Backend - Implementar filtro de matricula por status

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Implementar filtro de consulta de matriculas por status no backend.

Resultado esperado:
- consulta por status disponivel;
- retorno consistente com as regras definidas para os estados da matricula.

**Labels sugeridas:** `backend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `enrollment`, `backend`

### Subtask 5

**Tipo:** Subtask

**Resumo:** Frontend - Criar tela de consulta operacional

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Criar a tela de consulta operacional de matriculas com os filtros basicos definidos para o MVP.

Resultado esperado:
- tela de consulta criada;
- filtros visiveis e utilizaveis;
- estrutura pronta para integrar resultados.

**Labels sugeridas:** `frontend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `frontend`

### Subtask 6

**Tipo:** Subtask

**Resumo:** Frontend - Exibir resultados e status da consulta

**Story pai:** H5 - Consulta operacional de matricula

**Descricao:**

Implementar a exibicao dos resultados da consulta de matriculas no frontend, incluindo o status de cada registro.

Resultado esperado:
- resultados exibidos de forma compreensivel;
- status da matricula visivel;
- retorno coerente com os filtros aplicados.

**Labels sugeridas:** `frontend`, `enrollment`, `sprint-1`

**Componentes sugeridos:** `frontend`

## Subtasks Essenciais

Se quiser criar menos cards no Jira no primeiro momento, priorize estas subtasks:

- Backend - Estruturar autenticacao inicial
- Frontend - Criar tela de login
- Backend - Implementar endpoint de cadastro de aluno
- Frontend - Integrar cadastro de aluno com backend
- Backend - Implementar endpoints de cadastro academico
- Frontend - Integrar formularios academicos com backend
- Backend - Implementar caso de uso de matricula
- Frontend - Integrar tela de matricula com backend
- Backend - Implementar filtro de matricula por status
- Frontend - Exibir resultados e status da consulta

## Ordem Recomendada de Cadastro

1. Criar as subtasks de `H1`.
2. Criar as subtasks de `H2`.
3. Criar as subtasks de `H3`.
4. Criar as subtasks de `H4`.
5. Criar as subtasks de `H5`.

## Observacao Pratica

Se o Jira estiver ficando muito carregado para o momento atual do time:

- mantenha todas as stories;
- reduza a quantidade de subtasks;
- concentre o detalhamento tecnico no comentario da story ou na descricao da task principal.
