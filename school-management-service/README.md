# school-management-service

API backend do MVP de gestao escolar, implementada em Spring Boot com arquitetura modular.

## Status atual

O backend possui base funcional para:

- `accesscontrol`: login JWT, refresh, logout, usuarios, perfis, permissoes e sessoes;
- `studentmanagement`: cadastro de aluno com validacoes e persistencia;
- `responsavelmanagement`: cadastro de responsavel, vinculo aluno-responsavel e consulta cadastral;
- `academiccatalog`: cadastro e consulta de periodos letivos e turmas;
- `enrollment`: criacao e consulta de matriculas;
- `shared`: tratamento de excecoes e respostas de erro.

IDs principais usam `UUID`.

## Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Springdoc OpenAPI

## Como executar localmente

Pre-requisitos:

- Java 21
- PostgreSQL em execucao
- Banco `gestao_escolar` criado com a modelagem v2

Executar com profile local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

No Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## Banco de dados

As configuracoes locais ficam em:

```text
src/main/resources/application-local.yaml
```

Configuracao local padrao:

- URL: `jdbc:postgresql://localhost:5432/gestao_escolar`
- Usuario: `postgres`
- Senha: `root123`

A base v2 ja deve estar criada antes de subir a aplicacao. O Flyway foi mantido habilitado para as proximas evolucoes da nova estrutura, mas os scripts antigos da base anterior foram removidos.

## Autenticacao local

Credenciais seed:

- login: `admin`
- senha: `admin123`

Login:

```http
POST /api/auth/login
```

```json
{
  "login": "admin",
  "senha": "admin123"
}
```

A resposta retorna `accessToken` e `refreshToken`.

## Endpoints principais

### Autenticacao

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### Controle de acesso

- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`
- `GET /api/perfis`
- `GET /api/perfis/{id}`
- `POST /api/perfis`
- `PUT /api/perfis/{id}`
- `DELETE /api/perfis/{id}`
- `GET /api/permissoes`
- `GET /api/permissoes/{id}`
- `POST /api/permissoes`
- `PUT /api/permissoes/{id}`
- `DELETE /api/permissoes/{id}`

### Alunos

- `GET /api/alunos`
- `GET /api/alunos/{id}`
- `POST /api/alunos`
- `PUT /api/alunos/{id}`
- `DELETE /api/alunos/{id}`

Payload:

```json
{
  "nomeCompleto": "Joao da Silva",
  "cpf": "12345678901",
  "email": "joao.silva@example.com",
  "telefone": "11999999999",
  "dataNascimento": "2010-05-15"
}
```

### Responsaveis

- `GET /api/responsaveis`
- `GET /api/responsaveis/{id}`
- `POST /api/responsaveis`
- `PUT /api/responsaveis/{id}`
- `DELETE /api/responsaveis/{id}`

Payload:

```json
{
  "nomeCompleto": "Maria Responsavel",
  "cpf": "98765432100",
  "email": "maria@example.com",
  "telefone": "11988888888"
}
```

### Vinculo aluno-responsavel

- `POST /api/alunos/{idAluno}/responsaveis`
- `POST /api/alunos/{idAluno}/responsaveis/{idResponsavel}`
- `GET /api/alunos/{idAluno}/responsaveis`
- `DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`

Payload alternativo:

```json
{
  "idResponsavel": "00000000-0000-0000-0000-000000000000"
}
```

### Consulta cadastral

- `GET /api/consulta-cadastral`

Filtros:

- `nomeAluno`
- `cpfAluno`
- `nomeResponsavel`
- `cpfResponsavel`
- `page`
- `size`

### Periodos letivos

- `GET /api/periodos-letivos`
- `GET /api/periodos-letivos/{id}`
- `POST /api/periodos-letivos`

Payload:

```json
{
  "nome": "2026.1",
  "dataInicio": "2026-02-01",
  "dataFim": "2026-06-30"
}
```

### Turmas

- `GET /api/turmas`
- `GET /api/turmas/{id}`
- `POST /api/turmas`

Payload:

```json
{
  "codigo": "TURMA-A",
  "nome": "Turma A",
  "capacidade": 30,
  "periodoLetivoId": "00000000-0000-0000-0000-000000000000"
}
```

### Matriculas

- `GET /api/matriculas`
- `POST /api/matriculas`

Payload:

```json
{
  "alunoId": "00000000-0000-0000-0000-000000000000",
  "turmaId": "00000000-0000-0000-0000-000000000000",
  "periodoLetivoId": "00000000-0000-0000-0000-000000000000"
}
```

Filtros:

- `alunoId`
- `turmaId`
- `periodoLetivoId`
- `status`

## Testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```
