# 18 - URLs para Testes Manuais

## Objetivo

Checklist de testes HTTP para validar o fluxo MVP usando o backend em:

```text
http://localhost:8080
```

Os exemplos usam `UUID`. Substitua os placeholders retornados nos passos anteriores.

## Autenticacao

### Login

```http
POST http://localhost:8080/api/auth/login
```

```json
{
  "login": "admin",
  "senha": "admin123"
}
```

Esperado:

- `200 OK`;
- resposta com `accessToken` e `refreshToken`.

Use o `accessToken` nos proximos requests:

```text
Authorization: Bearer <accessToken>
```

## Checklist positivo

### 1. Criar aluno

```http
POST http://localhost:8080/api/alunos
```

```json
{
  "nomeCompleto": "Joao da Silva",
  "cpf": "12345678901",
  "email": "joao.silva@example.com",
  "telefone": "11999999999",
  "dataNascimento": "2010-05-15"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<alunoId>
```

### 2. Consultar aluno por ID

```http
GET http://localhost:8080/api/alunos/<alunoId>
```

Esperado: `200 OK`.

### 3. Criar responsavel

```http
POST http://localhost:8080/api/responsaveis
```

```json
{
  "nomeCompleto": "Maria Responsavel",
  "cpf": "98765432100",
  "email": "maria.responsavel@example.com",
  "telefone": "11988888888"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<responsavelId>
```

### 4. Vincular aluno e responsavel

```http
POST http://localhost:8080/api/alunos/<alunoId>/responsaveis/<responsavelId>
```

Esperado: `201 Created` ou sucesso equivalente definido pelo controller.

### 5. Consultar responsaveis do aluno

```http
GET http://localhost:8080/api/alunos/<alunoId>/responsaveis
```

Esperado: `200 OK` com lista contendo o responsavel vinculado.

### 6. Criar periodo letivo

```http
POST http://localhost:8080/api/periodos-letivos
```

```json
{
  "nome": "2026.1",
  "dataInicio": "2026-02-01",
  "dataFim": "2026-06-30"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<periodoLetivoId>
```

### 7. Criar turma

```http
POST http://localhost:8080/api/turmas
```

```json
{
  "codigo": "TURMA-A",
  "nome": "Turma A",
  "capacidade": 30,
  "periodoLetivoId": "<periodoLetivoId>"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<turmaId>
```

### 8. Criar matricula

```http
POST http://localhost:8080/api/matriculas
```

```json
{
  "alunoId": "<alunoId>",
  "turmaId": "<turmaId>",
  "periodoLetivoId": "<periodoLetivoId>"
}
```

Esperado:

- `201 Created`;
- status da matricula igual a `ATIVA`.

### 9. Consultar matriculas

```http
GET http://localhost:8080/api/matriculas
```

Filtros opcionais:

```http
GET http://localhost:8080/api/matriculas?alunoId=<alunoId>
GET http://localhost:8080/api/matriculas?turmaId=<turmaId>
GET http://localhost:8080/api/matriculas?periodoLetivoId=<periodoLetivoId>
GET http://localhost:8080/api/matriculas?status=ATIVA
```

Esperado: `200 OK`.

### 10. Consultar cadastro consolidado

```http
GET http://localhost:8080/api/consulta-cadastral?nomeAluno=Joao
```

Esperado: `200 OK` com aluno e responsaveis vinculados.

## Checklist negativo

### 1. Sem autenticacao

```http
POST http://localhost:8080/api/alunos
```

Nao enviar `Authorization`.

Esperado: `401 Unauthorized`.

### 2. CPF de aluno invalido

```http
POST http://localhost:8080/api/alunos
```

```json
{
  "nomeCompleto": "Aluno Invalido",
  "cpf": "123",
  "email": "invalido@example.com",
  "telefone": "11999999999",
  "dataNascimento": "2010-05-15"
}
```

Esperado: `400 Bad Request`.

### 3. UUID inexistente

```http
GET http://localhost:8080/api/alunos/00000000-0000-0000-0000-000000000000
```

Esperado: `404 Not Found`.

### 4. Periodo letivo invalido

```http
POST http://localhost:8080/api/periodos-letivos
```

```json
{
  "nome": "2026.1",
  "dataInicio": "2026-06-30",
  "dataFim": "2026-02-01"
}
```

Esperado: `400 Bad Request`.

### 5. Turma duplicada no mesmo periodo

Execute duas vezes com o mesmo `codigo` e `periodoLetivoId`:

```http
POST http://localhost:8080/api/turmas
```

```json
{
  "codigo": "TURMA-DUP",
  "nome": "Turma Duplicada",
  "capacidade": 30,
  "periodoLetivoId": "<periodoLetivoId>"
}
```

Esperado na segunda execucao: `409 Conflict`.

### 6. Matricula com aluno inexistente

```http
POST http://localhost:8080/api/matriculas
```

```json
{
  "alunoId": "00000000-0000-0000-0000-000000000000",
  "turmaId": "<turmaId>",
  "periodoLetivoId": "<periodoLetivoId>"
}
```

Esperado: `404 Not Found`.

### 7. Status invalido no filtro de matricula

```http
GET http://localhost:8080/api/matriculas?status=INVALIDO
```

Esperado: `400 Bad Request`.
