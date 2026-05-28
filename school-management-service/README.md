# school-management-service

API backend do sistema de gestao escolar, implementada em Spring Boot com arquitetura modular.

## Status atual

O backend possui base funcional para:

- `accesscontrol`: login JWT, refresh, logout, usuarios, perfis, permissoes e sessoes;
- `studentmanagement`: cadastro e manutencao de alunos;
- `responsavelmanagement`: cadastro de responsaveis, vinculo aluno-responsavel e consulta cadastral;
- `academiccatalog`: periodos letivos, series, turnos, turmas e catalogos academicos;
- `enrollment`: criacao, consulta, status e cancelamento/exclusao de matriculas;
- `studentdocument`: documentos de alunos;
- `schoolhistory`: historicos escolares;
- `transfermanagement`: escolas de origem e transferencias de alunos;
- `shared`: tratamento de excecoes, respostas de erro e componentes comuns.

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

## Banco de dados oficial

A base oficial atual e `gestao_escolar`, documentada pelo dump:

```text
../docs/v2/scriptdb.sql
```

Documento de referencia:

```text
../docs/20-base-dados-oficial-scriptdb.md
```

Regras praticas:

- `scriptdb.sql` deve orientar nomes de tabelas, colunas, constraints e relacionamentos.
- Scripts e documentos antigos ficam em `../docs/historico` e nao devem orientar novas implementacoes.
- A aplicacao local usa `src/main/resources/application-local.yaml`.

Configuracao local padrao:

- URL: `jdbc:postgresql://localhost:5432/gestao_escolar`
- Usuario: `postgres`
- Senha: `root123`

Para recriar a base local a partir do dump oficial:

```bash
dropdb -U postgres gestao_escolar
createdb -U postgres gestao_escolar
psql -U postgres -d gestao_escolar -f ../docs/v2/scriptdb.sql
```

## Como executar localmente

Pre-requisitos:

- Java 21
- PostgreSQL em execucao
- Banco `gestao_escolar` criado a partir de `../docs/v2/scriptdb.sql`

Executar com profile local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

No Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

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

A matriz granular de permissoes por funcionalidade sera fechada no final do projeto.

### Alunos

- `GET /api/alunos`
- `GET /api/alunos/{id}`
- `POST /api/alunos`
- `PUT /api/alunos/{id}`
- `DELETE /api/alunos/{id}`

Payload exemplo:

```json
{
  "nomeCompleto": "Joao da Silva",
  "cpf": "12345678901",
  "email": "joao.silva@example.com",
  "telefone": "11999999999",
  "dataNascimento": "2010-05-15",
  "cep": "01001000",
  "logradouro": "Praca da Se",
  "numero": "100",
  "bairro": "Se",
  "cidade": "Sao Paulo",
  "uf": "SP"
}
```

### Responsaveis

- `GET /api/responsaveis`
- `GET /api/responsaveis/{id}`
- `POST /api/responsaveis`
- `PUT /api/responsaveis/{id}`
- `DELETE /api/responsaveis/{id}`

### Vinculo aluno-responsavel

- `POST /api/alunos/{idAluno}/responsaveis`
- `POST /api/alunos/{idAluno}/responsaveis/{idResponsavel}`
- `GET /api/alunos/{idAluno}/responsaveis`
- `DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`

### Consulta cadastral

- `GET /api/consulta-cadastral`

Filtros:

- `nomeAluno`
- `cpfAluno`
- `nomeResponsavel`
- `cpfResponsavel`
- `page`
- `size`

### Catalogo academico

- `GET /api/periodos-letivos`
- `GET /api/periodos-letivos/{id}`
- `POST /api/periodos-letivos`
- `GET /api/series`
- `GET /api/series/{id}`
- `POST /api/series`
- `PUT /api/series/{id}`
- `GET /api/turnos`
- `GET /api/turnos/{id}`
- `POST /api/turnos`
- `PUT /api/turnos/{id}`
- `GET /api/turmas`
- `GET /api/turmas/{id}`
- `POST /api/turmas`
- `PUT /api/turmas/{id}`
- `GET /api/academico/catalogos/niveis-ensino`
- `GET /api/academico/catalogos/turnos`

Payload de periodo letivo:

```json
{
  "nome": "2026",
  "ano": 2026,
  "dataInicio": "2026-02-01",
  "dataFim": "2026-12-20"
}
```

Payload de turma:

```json
{
  "codigo": "2026-MANHA-5A",
  "nome": "5 Serie A",
  "capacidade": 30,
  "periodoLetivoId": "00000000-0000-0000-0000-000000000000",
  "serieId": "00000000-0000-0000-0000-000000000000",
  "turno": "MANHA",
  "status": "ATIVA"
}
```

### Matriculas

- `GET /api/matriculas`
- `POST /api/matriculas`
- `PATCH /api/matriculas/{id}/status`
- `DELETE /api/matriculas/{id}`
- `GET /api/matriculas/catalogos/tipos`
- `GET /api/matriculas/catalogos/status`
- `GET /api/matriculas/catalogos/status-etapas`

Payload de matricula:

```json
{
  "alunoId": "00000000-0000-0000-0000-000000000000",
  "turmaId": "00000000-0000-0000-0000-000000000000",
  "periodoLetivoId": "00000000-0000-0000-0000-000000000000",
  "tipoMatricula": "NOVA",
  "observacao": "Matricula inicial"
}
```

Payload de alteracao de status:

```json
{
  "status": "EFETIVADA",
  "justificativa": "Documentacao conferida"
}
```

Filtros:

- `alunoId`
- `turmaId`
- `periodoLetivoId`
- `status`

### Documentos, historico e transferencia

- `POST /api/documentos-alunos`
- `POST /api/documentos-alunos` com `multipart/form-data`
- `GET /api/documentos-alunos/{id}`
- `GET /api/documentos-alunos/alunos/{alunoId}`
- `DELETE /api/documentos-alunos/{id}`
- `GET /api/historicos-escolares`
- `GET /api/historicos-escolares/{id}`
- `POST /api/historicos-escolares`
- `PUT /api/historicos-escolares/{id}`
- `DELETE /api/historicos-escolares/{id}`
- `GET /api/escolas-origem`
- `GET /api/escolas-origem/{id}`
- `POST /api/escolas-origem`
- `GET /api/transferencias/{id}`
- `GET /api/transferencias/alunos/{alunoId}`
- `POST /api/transferencias`

## Testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```
