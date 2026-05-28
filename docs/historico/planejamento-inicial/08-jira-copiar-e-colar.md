# Jira Copiar e Colar

## Como usar

Use os blocos abaixo para criar os cards manualmente no Jira.

Sugestao:

- crie primeiro os `Epics`;
- depois crie as `Stories`;
- em cada story, vincule ao epic correspondente;
- adicione `labels`, `componentes` e `sprint` conforme fizer sentido.

## Epics

### Epic 1

**Tipo:** Epic

**Resumo:** Autenticacao e Controle de Acesso

**Descricao:**

Permitir acesso seguro ao sistema com autenticacao e perfis basicos. A matriz granular de permissoes por funcionalidade sera refinada no final do projeto.

Este epic cobre o controle inicial de acesso ao sistema no MVP, incluindo login, perfis e protecao das funcionalidades principais.

Escopo inicial:
- login;
- logout;
- perfis de usuario;
- perfis basicos de acesso;
- recuperacao de senha em fase posterior do MVP.

Resultado esperado:
- usuarios autenticados acessam apenas o que lhes e permitido;
- endpoints protegidos exigem autenticacao;
- o frontend respeita o estado de sessao do usuario.

**Labels sugeridas:** `mvp`, `auth`

**Componentes sugeridos:** `access-control`, `backend`, `frontend`

### Epic 2

**Tipo:** Epic

**Resumo:** Cadastro Academico

**Descricao:**

Manter os dados academicos estruturais necessarios para organizar a operacao escolar e suportar o fluxo de matricula.

Este epic cobre o cadastro inicial de periodo letivo, turma e estruturas academicas minimas necessarias para o MVP.

Escopo inicial:
- cadastro de periodo letivo;
- cadastro de turma;
- vinculacoes academicas minimas para permitir matricula.

Resultado esperado:
- periodos e turmas podem ser criados e consultados;
- os dados academicos cadastrados ficam disponiveis para o processo de matricula.

**Labels sugeridas:** `mvp`, `academic`

**Componentes sugeridos:** `academic-catalog`, `backend`, `frontend`

### Epic 3

**Tipo:** Epic

**Resumo:** Gestao de Alunos e Responsaveis

**Descricao:**

Centralizar o cadastro de alunos e responsaveis para permitir operacao academica com dados minimamente confiaveis.

Este epic cobre o cadastro inicial de aluno, cadastro de responsavel e consulta cadastral basica no contexto do MVP.

Escopo inicial:
- cadastro de aluno;
- cadastro de responsavel;
- vinculo aluno-responsavel;
- consulta cadastral basica.

Resultado esperado:
- alunos e responsaveis podem ser cadastrados;
- os dados ficam disponiveis para consulta e para uso no fluxo de matricula.

**Labels sugeridas:** `mvp`, `student`

**Componentes sugeridos:** `student-management`, `backend`, `frontend`

### Epic 4

**Tipo:** Epic

**Resumo:** Gestao de Matricula

**Descricao:**

Permitir o ciclo inicial de matricula com regras basicas, controle de status e consulta operacional.

Este epic cobre a criacao de matricula, consulta de status, cancelamento basico e historico inicial de mudancas relevantes.

Escopo inicial:
- criar matricula;
- consultar matricula;
- cancelar matricula;
- manter historico basico de status.

Resultado esperado:
- o atendente consegue formalizar a matricula do aluno;
- a matricula fica persistida com status consistente;
- a operacao consegue acompanhar a situacao da matricula.

**Labels sugeridas:** `mvp`, `enrollment`

**Componentes sugeridos:** `enrollment`, `backend`, `frontend`

### Epic 5

**Tipo:** Epic

**Resumo:** Portal Operacional e Consultas

**Descricao:**

Permitir busca e acompanhamento rapido dos principais registros do sistema escolar no contexto operacional do MVP.

Este epic cobre consultas operacionais essenciais para localizar matriculas e priorizar o trabalho diario.

Escopo inicial:
- busca por aluno;
- busca por turma;
- busca por periodo;
- filtro por status de matricula.

Resultado esperado:
- usuarios operacionais localizam matriculas com rapidez;
- a tela de consulta fornece retorno confiavel com filtros basicos.

**Labels sugeridas:** `mvp`, `academic`, `enrollment`

**Componentes sugeridos:** `enrollment`, `frontend`, `backend`

## Stories

### Story 1

**Tipo:** Story

**Resumo:** H1 - Autenticacao basica

**Epic:** Autenticacao e Controle de Acesso

**Descricao:**

Como administrador ou usuario operacional, quero autenticar no sistema para acessar as funcionalidades permitidas pelo meu perfil.

Contexto:
Esta historia faz parte do primeiro fluxo ponta a ponta do MVP e estabelece a base de seguranca para os demais modulos.

Criterios de aceite:
- usuario autenticado consegue acessar funcionalidades protegidas;
- usuario nao autenticado nao acessa endpoints protegidos;
- o frontend mantem o estado de sessao de forma consistente;
- existe mecanismo de logout;
- erros de autenticacao sao exibidos de forma clara.

Fora do escopo nesta story:
- recuperacao de senha;
- matriz completa de perfis complexos, planejada para o final do projeto;
- auditoria avancada de acesso.

**Labels sugeridas:** `mvp`, `sprint-1`, `auth`

**Componentes sugeridos:** `access-control`, `backend`, `frontend`

### Story 2

**Tipo:** Story

**Resumo:** H2 - Cadastro de aluno

**Epic:** Gestao de Alunos e Responsaveis

**Descricao:**

Como atendente, quero cadastrar um aluno para que ele possa ser matriculado.

Contexto:
Esta historia habilita a base cadastral minima para que o fluxo academico do MVP funcione sem improviso manual.

Criterios de aceite:
- o usuario consegue cadastrar um aluno com os campos obrigatorios definidos;
- validacoes minimas impedem dados invalidos;
- o backend persiste o aluno corretamente;
- o frontend exibe mensagem clara de sucesso ou erro;
- o aluno cadastrado pode ser recuperado para uso no fluxo de matricula.

Fora do escopo nesta story:
- cadastro completo de responsaveis;
- documentos avancados;
- historico detalhado do aluno.

**Labels sugeridas:** `mvp`, `sprint-1`, `student`

**Componentes sugeridos:** `student-management`, `backend`, `frontend`

### Story 3

**Tipo:** Story

**Resumo:** H3 - Cadastro de periodo letivo e turma

**Epic:** Cadastro Academico

**Descricao:**

Como administrador academico, quero cadastrar periodo letivo e turma para disponibilizar vagas para matricula.

Contexto:
Esta historia prepara a base academica minima do MVP para suportar a matricula de alunos em um contexto escolar organizado.

Criterios de aceite:
- o usuario consegue cadastrar um periodo letivo;
- o usuario consegue cadastrar uma turma;
- os registros criados ficam disponiveis para uso na matricula;
- validacoes minimas impedem dados inconsistentes;
- os endpoints e telas permitem consulta basica desses registros.

Fora do escopo nesta story:
- grade curricular completa;
- disciplinas com regras avancadas;
- calendario academico detalhado.

**Labels sugeridas:** `mvp`, `sprint-1`, `academic`

**Componentes sugeridos:** `academic-catalog`, `backend`, `frontend`

### Story 4

**Tipo:** Story

**Resumo:** H4 - Matricula de aluno

**Epic:** Gestao de Matricula

**Descricao:**

Como atendente, quero matricular um aluno em uma turma e periodo letivo para formalizar sua vaga.

Contexto:
Esta historia representa o centro do primeiro fluxo ponta a ponta do MVP e depende da existencia de autenticacao, aluno, periodo e turma.

Criterios de aceite:
- nao e possivel matricular aluno inexistente;
- nao e possivel matricular em turma inexistente;
- nao e possivel matricular em periodo inexistente;
- uma matricula valida e persistida corretamente;
- a matricula recebe status inicial consistente com a regra definida;
- o frontend exibe o resultado da operacao com clareza.

Fora do escopo nesta story:
- rematricula;
- regras academicas avancadas;
- integracoes assincronas;
- financeiro associado a matricula.

**Labels sugeridas:** `mvp`, `sprint-1`, `enrollment`

**Componentes sugeridos:** `enrollment`, `backend`, `frontend`

### Story 5

**Tipo:** Story

**Resumo:** H5 - Consulta operacional de matricula

**Epic:** Portal Operacional e Consultas

**Descricao:**

Como usuario operacional, quero consultar matriculas por filtros basicos para localizar rapidamente os registros.

Contexto:
Esta historia fecha o primeiro fluxo do MVP, permitindo uso operacional do sistema apos a criacao das matriculas.

Criterios de aceite:
- matriculas podem ser consultadas por aluno;
- matriculas podem ser consultadas por turma;
- matriculas podem ser consultadas por periodo letivo;
- matriculas podem ser filtradas por status;
- os resultados apresentados no frontend sao consistentes com os dados persistidos;
- a interface apresenta o retorno de forma compreensivel para uso diario.

Fora do escopo nesta story:
- relatorios avancados;
- exportacoes;
- dashboards gerenciais;
- filtros complexos adicionais.

**Labels sugeridas:** `mvp`, `sprint-1`, `enrollment`, `academic`

**Componentes sugeridos:** `enrollment`, `backend`, `frontend`

## Ordem Recomendada de Cadastro no Jira

1. Criar os 5 epics.
2. Criar as 5 stories.
3. Vincular cada story ao seu epic.
4. Criar a sprint `Sprint 1 - Primeiro fluxo ponta a ponta`.
5. Mover as 5 stories para a Sprint 1.

## Observacao Pratica

Se quiser simplificar o cadastro manual:

- use o texto do campo `Resumo` como titulo do card;
- cole o texto do campo `Descricao` no corpo do card;
- depois adicione labels, componentes e sprint.
