# Convencoes Tecnicas Iniciais

## Objetivo

Definir as convencoes de nomenclatura iniciais do projeto para manter consistencia entre backend, frontend e documentacao.

## Escopo Inicial

Este documento cobre primeiro:

- convencoes de nomenclatura;
- padrao de nomes de modulos, pacotes, classes e arquivos;
- padrao minimo para artefatos de backend e frontend;
- padrao de nomes para documentos e cards do Jira.

Outras convencoes tecnicas podem ser adicionadas depois neste mesmo arquivo.

## Regra Geral

Os nomes devem priorizar:

- clareza de negocio;
- previsibilidade;
- consistencia entre camadas;
- baixo acoplamento com tecnologia desnecessaria.

Sempre que houver duvida, preferir nomes orientados ao dominio em vez de nomes genéricos como `Manager`, `Helper`, `Util` ou `Service` sem contexto.

## Idioma

### Dominio e negocio

Os conceitos de negocio podem permanecer em portugues quando fizerem parte do dominio central do sistema.

Exemplos:

- `Aluno`
- `Matricula`
- `Turma`
- `PeriodoLetivo`

### Estrutura tecnica

Os nomes de pastas tecnicas, camadas e convencoes de framework devem seguir o padrao tecnico mais comum da stack.

Exemplos:

- `adapter`
- `application`
- `domain`
- `infrastructure`
- `repository`
- `usecase`

## Backend

### Base de pacote

Manter a base:

- `br.com.escola`

### Organizacao de pacote

Organizacao base padrao:

```text
br.com.escola
  domain
  application
  adapter
  infrastructure
```

Nos modulos do produto real, o padrao preferido e por contexto de negocio, por exemplo:

```text
br.com.escola.accesscontrol
br.com.escola.studentmanagement
br.com.escola.academiccatalog
br.com.escola.enrollment
```

### Nomes de pacotes

- sempre em minusculo;
- sem acento;
- sem underscore;
- sem hifen;
- sem plural desnecessario quando o contexto ja for claro.

Exemplos validos:

- `domain.core`
- `application.usecase`
- `adapter.in.web`
- `adapter.out.persistence`

## Classes Java

### Regra geral

- usar `PascalCase`;
- nomes devem ser substantivos ou verbos coerentes com o papel da classe;
- evitar abreviacoes desnecessarias.

### Entidades de dominio

Padrao:

- nome de negocio puro, sem sufixo tecnico.

Exemplos:

- `Aluno`
- `Matricula`
- `Turma`

### Entidades JPA

Padrao:

- nome de negocio + `Entity`

Exemplos:

- `AlunoEntity`
- `MatriculaEntity`
- `TurmaEntity`

### Repositorios de porta

Padrao:

- nome de negocio + `Repository`

Exemplos:

- `AlunoRepository`
- `MatriculaRepository`

### Repositorios Spring Data

Padrao:

- nome de negocio + `JpaRepository`

Exemplos:

- `AlunoJpaRepository`
- `MatriculaJpaRepository`

### Adaptadores

Padrao:

- nome do papel + `Adapter`

Exemplos:

- `AlunoRepositoryAdapter`
- `MatriculaRepositoryAdapter`

### Casos de uso

Padrao:

- verbo no infinitivo + conceito de negocio + `UseCase`

Exemplos:

- `CriarAlunoUseCase`
- `MatricularAlunoUseCase`
- `CancelarMatriculaUseCase`

### Controllers

Padrao:

- conceito exposto + `Controller`

Exemplos:

- `MatriculaController`
- `AlunoController`

### DTOs de entrada e saida

Padrao:

- conceito + `Request`
- conceito + `Response`

Exemplos:

- `MatriculaRequest`
- `AlunoResponse`

### Mappers

Padrao:

- conceito + `Mapper`

Exemplos:

- `AlunoMapper`
- `MatriculaMapper`

### Excecoes

Padrao:

- contexto + `Exception`

Exemplos:

- `BusinessException`
- `ResourceNotFoundException`

## Metodos e variaveis

- usar `camelCase`;
- nomes devem expressar intencao;
- booleanos devem soar como pergunta ou estado.

Exemplos:

- `criarAluno`
- `buscarMatriculaPorId`
- `matriculaAtiva`
- `usuarioAutenticado`

## Constantes

- usar `UPPER_SNAKE_CASE`;
- nomes completos, sem abreviacao ambigua.

Exemplos:

- `DEFAULT_PAGE_SIZE`
- `TOKEN_EXPIRATION_MINUTES`

## Banco de dados

### Tabelas

- usar `snake_case`;
- preferir nomes no singular;
- refletir o conceito de negocio.

Exemplos:

- `aluno`
- `matricula`
- `periodo_letivo`

### Colunas

- usar `snake_case`;
- nomes simples e descritivos;
- chaves estrangeiras com sufixo `_id`.

Exemplos:

- `nome_completo`
- `status`
- `turma_id`

### Arquivos de migration

Padrao recomendado:

- `V001__descricao_objetiva.sql`
- `V002__cria_tabela_aluno.sql`
- `V003__cria_tabela_matricula.sql`

## APIs REST

### Paths

- usar minusculo;
- usar hifen apenas em recursos compostos;
- nomes no plural para colecoes.

Exemplos:

- `/alunos`
- `/matriculas`
- `/periodos-letivos`

### JSON

- usar `camelCase` nas propriedades expostas pela API;
- manter alinhamento com DTOs.

Exemplos:

- `nomeCompleto`
- `dataNascimento`
- `periodoLetivoId`

## Frontend Angular

### Pastas e arquivos

- usar `kebab-case`;
- nomear por feature ou papel da tela;
- evitar nomes genericos como `page1`, `component2`, `utils`.

Exemplos:

- `student-registration`
- `enrollment-list`
- `login-page`

### Componentes

- nome do arquivo em `kebab-case`;
- nome da classe em `PascalCase`;
- sufixar pelo tipo quando fizer sentido.

Exemplos:

- `login-page.component.ts`
- `student-form.component.ts`
- `enrollment-list.component.ts`

### Services

Padrao:

- conceito + `.service.ts`
- classe em `PascalCase` com sufixo `Service`

Exemplos:

- `auth.service.ts`
- `student.service.ts`

### Models e interfaces

- nome do arquivo em `kebab-case`;
- interface ou tipo em `PascalCase`;
- evitar prefixo `I` por padrao.

Exemplos:

- `student.model.ts`
- `enrollment-status.type.ts`

### Rotas

- usar `kebab-case`;
- refletir a feature funcional.

Exemplos:

- `/login`
- `/alunos/novo`
- `/matriculas`

## Documentacao

### Arquivos Markdown

- usar prefixo numerico quando o documento fizer parte de uma sequencia de leitura;
- usar `kebab-case`;
- evitar nomes vagos.

Exemplos:

- `01-mvp-gestao-escolar.md`
- `06-sprint-1-backlog.md`
- `10-convencoes-tecnicas-iniciais.md`

## Jira

### Epicos

- usar nome funcional, sem codigo manual no titulo.

Exemplos:

- `Gestao de Matricula`
- `Cadastro Academico`

### Historias

- usar prefixo funcional curto apenas quando ajudar a leitura do backlog.

Exemplos:

- `H1 - Autenticacao basica`
- `H2 - Cadastro de aluno`

### Subtasks

- usar formato `Area - Acao objetiva`

Exemplos:

- `Backend - Estruturar projeto Spring Boot`
- `Frontend - Integrar tela de matricula com backend`
- `Arquitetura - Definir convencoes tecnicas iniciais`

## Regras de consistencia

- nao misturar ingles e portugues no mesmo nome de classe de dominio;
- nao usar siglas opacas em nomes publicos;
- nao usar nomes genericos como `BaseService`, `CommonUtils`, `GenericController`;
- nao criar nomes diferentes para o mesmo conceito em backend, frontend e banco.

## Decisao Inicial

Para este projeto, a nomenclatura de dominio principal permanece em portugues,
enquanto a estrutura tecnica segue convencoes comuns da stack Java, Spring e Angular.
