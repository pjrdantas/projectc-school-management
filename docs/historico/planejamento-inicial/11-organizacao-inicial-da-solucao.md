# Organizacao Inicial da Solucao

## Objetivo

Registrar a organizacao inicial da solucao para o MVP, definindo o nome oficial do backend,
o nome oficial do frontend, os modulos internos do backend e a correspondencia inicial
entre backend e frontend.

## Decisao Principal

O sistema sera implementado como um monolito modular.

Isso significa:

- uma unica aplicacao backend;
- uma unica aplicacao frontend web administrativo;
- separacao interna por contexto de negocio;
- sem divisao em microservicos neste momento.

## Nome Oficial dos Projetos

### Backend

- `school-management-service`

### Frontend

- `school-management-web`

## Backend

### Estrutura de alto nivel

O backend sera uma unica API, organizada internamente por modulos de negocio.

Representacao conceitual:

```text
school-management-service
  accesscontrol
  studentmanagement
  academiccatalog
  enrollment
```

### Modulos internos do backend

#### 1. `accesscontrol`

Responsavel por:

- autenticacao;
- autorizacao;
- usuarios;
- perfis;
- controle de acesso inicial.

#### 2. `studentmanagement`

Responsavel por:

- cadastro de aluno;
- cadastro de responsavel;
- vinculo entre aluno e responsavel;
- consulta cadastral basica.

#### 3. `academiccatalog`

Responsavel por:

- periodo letivo;
- turma;
- disciplina;
- estrutura academica minima do MVP.

#### 4. `enrollment`

Responsavel por:

- matricula;
- cancelamento;
- historico de status;
- consulta operacional de matriculas.

### Base de pacote

No projeto novo, a base recomendada e:

- `br.com.escola`

### Estrutura interna sugerida por modulo

Cada modulo deve seguir, quando aplicavel, esta organizacao:

```text
br.com.escola
  <modulo>
    domain
    application
    adapter
    infrastructure
```

Exemplo conceitual:

```text
br.com.escola
  enrollment
    domain
    application
    adapter
      in
      out
    infrastructure
```

### Observacao importante

Os modulos internos do backend nao sao APIs separadas.

Eles sao apenas contextos internos do mesmo sistema.

Portanto:

- nao criar `accesscontrol-service`;
- nao criar `student-management-service`;
- nao criar `academic-catalog-management-service`;
- nao criar `enrollment-management-service`.

## Frontend

### Estrutura de alto nivel

O frontend sera uma unica aplicacao web administrativa:

- `school-management-web`

### Features iniciais do frontend

As features iniciais devem refletir o fluxo da Sprint 1 e os modulos do backend.

Representacao conceitual:

```text
school-management-web
  auth
  students
  academic
  enrollment
  shared
```

### Features previstas

#### 1. `auth`

Responsavel por:

- login;
- logout;
- guarda de autenticacao;
- controle de sessao.

#### 2. `students`

Responsavel por:

- cadastro de aluno;
- consulta cadastral basica;
- formularios ligados ao aluno.

#### 3. `academic`

Responsavel por:

- cadastro de periodo letivo;
- cadastro de turma;
- apoio ao fluxo academico do MVP.

#### 4. `enrollment`

Responsavel por:

- tela de matricula;
- consulta operacional de matriculas;
- exibicao de status da matricula.

#### 5. `shared`

Responsavel por:

- componentes reutilizaveis;
- modelos compartilhados;
- servicos transversais;
- utilitarios realmente comuns.

## Correspondencia Inicial Entre Backend e Frontend

| Backend | Frontend | Objetivo inicial |
|---|---|---|
| `accesscontrol` | `auth` | login e acesso |
| `studentmanagement` | `students` | cadastro de aluno |
| `academiccatalog` | `academic` | periodo letivo e turma |
| `enrollment` | `enrollment` | matricula e consulta |

## Estrutura Inicial Recomendada

### Backend

```text
school-management-service
  src/main/java/br/com/escola
    accesscontrol
    studentmanagement
    academiccatalog
    enrollment
```

### Frontend

```text
school-management-web
  src/app
    auth
    students
    academic
    enrollment
    shared
```

## Regra de Evolucao

Novos modulos ou features devem ser adicionados apenas quando houver necessidade clara de negocio.

Nao antecipar agora:

- financeiro;
- notificacoes;
- relatorios avancados;
- mobile;
- integracoes externas complexas.

## Decisao Registrada

Para o MVP inicial:

- o backend oficial sera `school-management-service`;
- o frontend oficial sera `school-management-web`;
- o backend sera um monolito modular;
- os modulos iniciais do backend serao `accesscontrol`, `studentmanagement`, `academiccatalog` e `enrollment`;
- as features iniciais do frontend serao `auth`, `students`, `academic`, `enrollment` e `shared`.
