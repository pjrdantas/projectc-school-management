# 18 - URLs para Testes Manuais

## Objetivo

Checklist de testes HTTP para validar manualmente os fluxos principais usando o backend em:

```text
http://localhost:8080
```

A base oficial para estes testes e:

```text
docs/v2/scriptdb.sql
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
  "dataNascimento": "2010-05-15",
  "cep": "01001000",
  "logradouro": "Praca da Se",
  "numero": "100",
  "bairro": "Se",
  "cidade": "Sao Paulo",
  "uf": "SP"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<alunoId>
```

### 2. Criar responsavel

```http
POST http://localhost:8080/api/responsaveis
```

```json
{
  "nomeCompleto": "Maria Responsavel",
  "cpf": "98765432100",
  "email": "maria.responsavel@example.com",
  "telefone": "11988888888",
  "cep": "01001000",
  "logradouro": "Praca da Se",
  "numero": "200",
  "bairro": "Se",
  "cidade": "Sao Paulo",
  "uf": "SP"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<responsavelId>
```

### 3. Vincular aluno e responsavel

```http
POST http://localhost:8080/api/alunos/<alunoId>/responsaveis/<responsavelId>
```

Esperado: `201 Created` ou sucesso equivalente definido pelo controller.

### 4. Consultar cadastro consolidado

```http
GET http://localhost:8080/api/consulta-cadastral?nomeAluno=Joao
```

Esperado: `200 OK` com aluno e responsaveis vinculados.

### 5. Criar periodo letivo

```http
POST http://localhost:8080/api/periodos-letivos
```

```json
{
  "nome": "2026",
  "ano": 2026,
  "dataInicio": "2026-02-01",
  "dataFim": "2026-12-20"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID.

Guarde:

```text
<periodoLetivoId>
```

### 6. Listar series e selecionar uma serie

```http
GET http://localhost:8080/api/series
```

Esperado: `200 OK`.

Guarde um identificador retornado:

```text
<serieId>
```

Se nao houver serie cadastrada na base usada no teste, crie uma:

```http
POST http://localhost:8080/api/series
```

```json
{
  "nome": "5 Serie",
  "ordem": 5,
  "nivelEnsino": "FUNDAMENTAL"
}
```

### 7. Criar turma

```http
POST http://localhost:8080/api/turmas
```

```json
{
  "codigo": "2026-MANHA-5A",
  "nome": "5 Serie A",
  "capacidade": 30,
  "periodoLetivoId": "<periodoLetivoId>",
  "serieId": "<serieId>",
  "turno": "MANHA",
  "status": "ATIVA"
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
  "periodoLetivoId": "<periodoLetivoId>",
  "tipoMatricula": "NOVA",
  "observacao": "Matricula inicial"
}
```

Esperado:

- `201 Created`;
- resposta com `id` UUID e status definido pelo fluxo de matricula em uso.

Guarde:

```text
<matriculaId>
```

### 9. Atualizar status da matricula

```http
PATCH http://localhost:8080/api/matriculas/<matriculaId>/status
```

```json
{
  "status": "EFETIVADA",
  "justificativa": "Documentacao conferida"
}
```

Esperado: `200 OK` com o status atualizado.

### 10. Consultar matriculas

```http
GET http://localhost:8080/api/matriculas
```

Filtros opcionais:

```http
GET http://localhost:8080/api/matriculas?alunoId=<alunoId>
GET http://localhost:8080/api/matriculas?turmaId=<turmaId>
GET http://localhost:8080/api/matriculas?periodoLetivoId=<periodoLetivoId>
GET http://localhost:8080/api/matriculas?status=EFETIVADA
```

Esperado: `200 OK`.

### 11. Documentos de aluno

```http
POST http://localhost:8080/api/documentos-alunos
```

```json
{
  "alunoId": "<alunoId>",
  "tipoDocumento": "RG",
  "numeroDocumento": "123456789",
  "caminhoArquivo": "/tmp/rg-aluno.pdf",
  "observacao": "Documento conferido"
}
```

Esperado: `201 Created`.

Consultar documentos do aluno:

```http
GET http://localhost:8080/api/documentos-alunos/alunos/<alunoId>
```

### 12. Transferencia de aluno

Criar escola de origem:

```http
POST http://localhost:8080/api/escolas-origem
```

```json
{
  "nomeEscola": "Escola Origem",
  "codigoInep": "12345678",
  "cidade": "Sao Paulo",
  "uf": "SP"
}
```

Guarde:

```text
<escolaOrigemId>
```

Criar transferencia:

```http
POST http://localhost:8080/api/transferencias
```

```json
{
  "alunoId": "<alunoId>",
  "escolaOrigemId": "<escolaOrigemId>",
  "serieOrigem": "4 Serie",
  "anoLetivoOrigem": "2025",
  "dataTransferencia": "2026-01-20",
  "tipoTransferencia": "ENTRADA",
  "statusTransferencia": "CONFIRMADA",
  "usuarioOperacao": "admin"
}
```

Esperado: `201 Created`.

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
  "nome": "2026",
  "ano": 2026,
  "dataInicio": "2026-12-20",
  "dataFim": "2026-02-01"
}
```

Esperado: `400 Bad Request`.

### 5. Matricula com aluno inexistente

```http
POST http://localhost:8080/api/matriculas
```

```json
{
  "alunoId": "00000000-0000-0000-0000-000000000000",
  "turmaId": "<turmaId>",
  "periodoLetivoId": "<periodoLetivoId>",
  "tipoMatricula": "NOVA"
}
```

Esperado: `404 Not Found`.

### 6. Status invalido no filtro de matricula

```http
GET http://localhost:8080/api/matriculas?status=INVALIDO
```

Esperado: `400 Bad Request`.
