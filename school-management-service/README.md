# school-management-service

API backend do MVP de gestão escolar, implementada em Spring Boot com arquitetura modular.

## Status atual do projeto

Hoje o projeto já possui uma base funcional para os módulos iniciais de:

- `accesscontrol` (autenticação básica via HTTP Basic);
- `studentmanagement` (cadastro de aluno com validações e persistência);
- `shared` (tratamento de exceções e respostas de erro).

Também já existe configuração de banco local com PostgreSQL e migrations com Flyway.

## O que já está implementado

### 1) Segurança e autenticação

- Configuração de segurança ativa com autenticação obrigatória para endpoints da API (exceto `health` e `info`);
- Endpoint de teste autenticado:
  - `GET /api/test/authenticated`

### 2) Cadastro de aluno

- Endpoint de criação de aluno:
  - `POST /api/alunos`
- Payload validado com Bean Validation (`nomeCompleto`, `cpf`, `email`, `dataNascimento`);
- Tratamento de conflito para CPF já cadastrado;
- Persistência em tabela `aluno`.

### 3) Banco de dados e migrations

- Migration baseline:
  - `V001__initial_baseline.sql`
- Migration de aluno:
  - `V002__create_table_aluno.sql`

## Estrutura atual (resumo)

```text
src/main/java/br/com/escola
  accesscontrol
    adapter/in/web
    infrastructure/security
  studentmanagement
    domain
    application/usecase
    adapter/in/web
    adapter/out/persistence
  shared/exception
```

## Como executar localmente

### Pré-requisitos

- Java 21
- PostgreSQL em execução
- Banco `school_management` criado

### Configuração local

As configurações de ambiente local estão em `src/main/resources/application-local.yaml`.

Para subir com esse profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Ou configurando variável:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## Credenciais de autenticação local (HTTP Basic)

No profile local:

- usuário: `admin`
- senha: `admin123`

## Próximos passos do backend (macro)

- Evoluir `accesscontrol` para login/token conforme estratégia final;
- Completar fluxo de consulta de aluno;
- Implementar módulos `academiccatalog` e `enrollment`.

