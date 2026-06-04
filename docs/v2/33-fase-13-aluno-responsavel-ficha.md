# Fase 13 - Aluno e responsavel

Data: 2026-06-03

## Objetivo

Avancar o fluxo MVP de aluno e responsavel, reaproveitando os cadastros e vinculos ja existentes e adicionando uma consulta consolidada da ficha do aluno.

## Escopo executado

O projeto ja possuia:

- cadastro de aluno;
- cadastro de responsavel;
- vinculo aluno-responsavel;
- listagem de responsaveis por aluno;
- consulta cadastral mais ampla.

Nesta fase foi adicionada uma consulta direta da ficha do aluno.

## Endpoint adicionado

```text
GET /api/alunos/{id}/ficha
```

Resposta:

- dados completos do aluno;
- responsaveis vinculados;
- dados do vinculo, como parentesco, responsavel financeiro, responsavel pedagogico e autorizacao de retirada.

## Arquivos adicionados/alterados

Adicionado:

- `AlunoFichaResponse`

Alterado:

- `AlunoController`
- `AlunoControllerIntegrationTest`

## Reuso de casos de uso existentes

O endpoint novo reutiliza:

- `BuscarAlunoPorIdUseCase`
- `ListarResponsaveisPorAlunoUseCase`

Com isso, a fase nao duplicou regra de negocio nem criou nova consulta paralela para o mesmo relacionamento.

## Testes adicionados

Foi adicionado teste de integracao para:

1. criar aluno;
2. criar responsavel;
3. vincular responsavel ao aluno;
4. consultar `GET /api/alunos/{id}/ficha`;
5. validar dados do aluno e do responsavel vinculado.

## Validacao executada

Execucao focada:

```powershell
.\mvnw.cmd -Dtest=AlunoControllerIntegrationTest test
```

Resultado:

- Testes: 6
- Falhas: 0
- Erros: 0
- Build: sucesso

Execucao completa:

```powershell
.\mvnw.cmd test
```

Resultado:

- Testes: 37
- Falhas: 0
- Erros: 0
- Build: sucesso

## Resultado da fase

O fluxo aluno + responsavel agora cobre:

1. cadastro de aluno com pessoa;
2. cadastro de responsavel com pessoa;
3. vinculo aluno-responsavel;
4. listagem de responsaveis por aluno;
5. ficha consolidada do aluno.

## Proxima fase recomendada

Iniciar o fluxo `matricula`, com foco em:

- criar matricula para aluno;
- associar turma e periodo letivo;
- controlar status e tipo de matricula;
- consultar situacao da matricula;
- integrar documentos exigidos/entregues.
