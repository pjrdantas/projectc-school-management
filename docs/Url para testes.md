## Checklist de testes negativos`

 1) 401 - Sem autenticação
Método: POST

```bash
URL: http://localhost:8080/api/alunos
````
Auth: não enviar

 Body:

```bash
{
  "nomeCompleto": "Sem Auth",
  "cpf": "12345678901",
  "email": "sem.auth@example.com",
  "dataNascimento": "2010-01-01"
}
```
Esperado: 401, error = "UNAUTHORIZED". 

2) 400 - CPF inválido
Método: POST

```bash
URL: http://localhost:8080/api/alunos

Body:

{
  "nomeCompleto": "Aluno Inválido",
  "cpf": "123",
  "email": "invalido@example.com",
  "dataNascimento": "2010-05-15"
}
```
Esperado: 400, error = "VALIDATION_ERROR", erro em fields.cpf. 

3) 404 - Aluno inexistente
Método: GET

```bash
URL: http://localhost:8080/api/alunos/999999
```
Esperado: 404, mensagem de aluno não encontrado. 

4) 400 - Período inválido (fim < início)
Método: POST

```bash
URL: http://localhost:8080/api/periodos-letivos

Body:

{
  "nome": "2026.1",
  "dataInicio": "2026-06-30",
  "dataFim": "2026-02-01"
}
````
Esperado: 400, mensagem dataFim deve ser maior ou igual a dataInicio. 

5) 409 - Turma duplicada (mesmo código + período)
Método: POST

```bash
URL: http://localhost:8080/api/turmas

Body (rodar 2x):

{
  "codigo": "TURMA-DUP",
  "nome": "Turma Duplicada",
  "capacidade": 30,
  "periodoLetivoId": 1
}
````
Esperado na 2ª execução: 409, mensagem de turma já existente. 

6) 404 - Matrícula com aluno inexistente
Método: POST

```bash
URL: http://localhost:8080/api/matriculas

Body:

{
  "alunoId": 999999,
  "turmaId": 1,
  "periodoLetivoId": 1
}
````
Esperado: 404, error = "RESOURCE_NOT_FOUND". 

7) 400 - Status inválido no filtro de matrícula
Método: GET

```bash
URL: http://localhost:8080/api/matriculas?status=INVALIDO
````
Esperado: 400, mensagem Status de matrícula inválido: INVALIDO. 

8) 400/404 - Turma x Período inconsistente
Método: POST
```bash
URL: http://localhost:8080/api/matriculas

Body:

{
  "alunoId": 1,
  "turmaId": 1,
  "periodoLetivoId": 999999
}
````

Esperado:

404 se período não existir;

400 se existir, mas não pertencer à turma. 

____________________________________________________________________________

## Checklist POSITIVO (ordem recomendada)
1) Criar aluno
Método: POST
```bash
URL: http://localhost:8080/api/alunos

Body:

{
  "nomeCompleto": "João da Silva",
  "cpf": "12345678901",
  "email": "joao.silva@example.com",
  "dataNascimento": "2010-05-15"
}
````
Esperado: 201 Created e retorno com id (guarde esse id, ex.: 1). 

2) Consultar aluno por ID
Método: GET
```bash
URL: http://localhost:8080/api/alunos/1
````
Esperado: 200 OK com os dados do aluno. 

3) Criar período letivo
Método: POST
```bash
URL: http://localhost:8080/api/periodos-letivos

Body:

{
  "nome": "2026.1",
  "dataInicio": "2026-02-01",
  "dataFim": "2026-06-30"
}
````
Esperado: 201 Created e retorno com id (ex.: 1). 

4) Consultar período por ID
Método: GET
```bash
URL: http://localhost:8080/api/periodos-letivos/1
````
Esperado: 200 OK. 

5) Criar turma
Método: POST
```bash
URL: http://localhost:8080/api/turmas

Body:

{
  "codigo": "TURMA-A",
  "nome": "Turma A",
  "capacidade": 30,
  "periodoLetivoId": 1
}
````
Esperado: 201 Created e retorno com id (ex.: 1). 

6) Consultar turma por ID
Método: GET
```bash
URL: http://localhost:8080/api/turmas/1
````
Esperado: 200 OK. 

7) Criar matrícula válida
Método: POST
```bash
URL: http://localhost:8080/api/matriculas

Body:

{
  "alunoId": 1,
  "turmaId": 1,
  "periodoLetivoId": 1
}
````
Esperado: 201 Created, com status = "ATIVA". 

8) Consultar matrículas (sem filtro)
Método: GET
```bash
URL: http://localhost:8080/api/matriculas
````
Esperado: 200 OK com lista (array). 

9) Consultar matrículas por filtro (opcional)
```bash
GET http://localhost:8080/api/matriculas?alunoId=1

GET http://localhost:8080/api/matriculas?turmaId=1

GET http://localhost:8080/api/matriculas?periodoLetivoId=1

GET http://localhost:8080/api/matriculas?status=ATIVA
````
Esperado: 200 OK e retorno coerente com o filtro.