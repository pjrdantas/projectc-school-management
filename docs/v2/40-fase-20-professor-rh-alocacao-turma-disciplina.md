# Fase 20 - Professor, RH e alocacao em turma/disciplina

## Objetivo

Avancar o fluxo MVP de professor e RH sem abrir ainda um CRUD completo de funcionario.

O recorte desta fase permite:

- cadastrar professor a partir de um funcionario existente;
- consultar professores cadastrados;
- vincular professor a uma turma/disciplina ja configurada no catalogo academico;
- consultar alocacoes por professor;
- consultar professores vinculados a uma turma.

## Regra implementada

Um professor pode ser criado quando:

- o `funcionarioId` informado existe;
- o funcionario esta ativo;
- ainda nao existe professor cadastrado para a mesma pessoa do funcionario.

Um vinculo professor/turma/disciplina pode ser criado quando:

- o professor existe;
- o vinculo `turma_disciplina` existe;
- o professor ainda nao esta vinculado ao mesmo `turmaDisciplinaId`.

## Endpoints adicionados

- `POST /api/professores`
- `GET /api/professores`
- `GET /api/professores/{id}`
- `POST /api/professores/{id}/turmas-disciplinas`
- `GET /api/professores/{id}/turmas-disciplinas`
- `GET /api/turmas/{turmaId}/professores`

## Escopo realizado

- Criado controller REST de professor.
- Criado controller de consulta de professores por turma.
- Criados DTOs de request/response para professor e alocacao.
- Criado service com regras de cadastro e alocacao.
- Ampliado repository de `ProfessorTurmaDisciplinaEntity` para consultas por turma e duplicidade.
- Criadas excecoes de dominio para professor e alocacao.
- Adicionado tratamento HTTP para:
  - professor nao encontrado;
  - turma/disciplina nao encontrada;
  - professor duplicado;
  - alocacao duplicada;
  - funcionario inativo.
- Criado teste de integracao do fluxo MVP.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/professor/adapter/in/web/ProfessorController.java`
- `school-management-service/src/main/java/br/com/escola/professor/adapter/in/web/TurmaProfessorController.java`
- `school-management-service/src/main/java/br/com/escola/professor/application/service/ProfessorService.java`
- `school-management-service/src/main/java/br/com/escola/professor/adapter/out/persistence/repository/ProfessorTurmaDisciplinaJpaRepository.java`
- `school-management-service/src/test/java/br/com/escola/professor/adapter/in/web/ProfessorControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=ProfessorControllerIntegrationTest test
```

Resultado:

- 2 testes executados.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Criar CRUD proprio de RH/funcionario, em vez de depender de funcionario previamente existente.
- Criar consulta de grade de aulas/disciplinas por professor com filtros por periodo letivo.
- Definir regra de historico de alocacao quando um professor deixa de atuar em uma turma/disciplina.
- Avancar para o fluxo de aulas, frequencia e avaliacao.
