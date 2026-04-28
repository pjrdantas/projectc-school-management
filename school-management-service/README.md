# school-management-service

API backend do MVP de gestão escolar, implementada em Spring Boot com arquitetura modular.

## Status atual do projeto

Hoje o projeto já possui base funcional para:

- `accesscontrol` (autenticação básica via HTTP Basic);
- `studentmanagement` (cadastro de aluno com validações e persistência);
- `responsavelmanagement`:
  - cadastro de responsável (KAN-3 H2);
  - vínculo aluno-responsável (KAN-3 H3);
  - consulta cadastral consolidada (KAN-3 H4 - backend);
- `shared` (tratamento de exceções e respostas de erro).

Também já existe configuração de banco local com PostgreSQL e migrations com Flyway.

## Endpoints principais (backend)

### 1) Responsável (H2)

- `POST /api/responsaveis`
- `GET /api/responsaveis/{id}`
- `GET /api/responsaveis?nome=&cpf=`
- `PUT /api/responsaveis/{id}`
- `DELETE /api/responsaveis/{id}`

### 2) Vínculo Aluno-Responsável (H3)

- `POST /api/alunos/{idAluno}/responsaveis` com body:

```json
{ "idResponsavel": "<uuid>" }
```

- `POST /api/alunos/{idAluno}/responsaveis/{idResponsavel}` (atalho sem body)
- `GET /api/alunos/{idAluno}/responsaveis`
- `DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`

### 3) Consulta Cadastral Básica (H4 - backend)

- `GET /api/consulta-cadastral`

Parâmetros de filtro suportados:

- `nomeAluno`
- `cpfAluno`
- `nomeResponsavel`
- `cpfResponsavel`
- `page` (default `0`)
- `size` (default `20`, máximo `100`)

Resposta:

```json
{
  "content": [
    {
      "idAluno": "...",
      "nomeCompleto": "...",
      "cpf": "...",
      "email": "...",
      "telefone": "...",
      "dataNascimento": "...",
      "createdAt": "...",
      "responsaveis": [
        {
          "id": "...",
          "nomeCompleto": "...",
          "cpf": "...",
          "email": "...",
          "telefone": "...",
          "createdAt": "..."
        }
      ]
    }
  ],
  "totalElements": 1,
  "page": 0,
  "size": 20
}
```

## Banco de dados e migrations

- `V001__initial_baseline.sql`
- `V002__create_table_aluno.sql`
- `V003__create_table_periodo_letivo.sql`
- `V004__create_table_turma.sql`
- `V005__create_table_matricula.sql`
- `V006__add_column_telefone_to_aluno.sql`
- `V007__migrate_bigint_to_uuid.sql`
- `V008__create_table_responsavel.sql`
- `V009__create_table_aluno_responsavel.sql`

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

### Script SQL de apoio (UUID)

Para preparar o banco local já no padrão UUID (compatível com a API atual), use:

```bash
psql -U postgres -d school_management -f ../docs/school_management_uuid.sql
```

## Credenciais de autenticação local (HTTP Basic)

No profile local:

- usuário: `admin`
- senha: `admin123`
