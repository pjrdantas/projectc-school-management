# Fase 21 - Diario de aula e frequencia

## Objetivo

Iniciar o fluxo MVP de execucao pedagogica usando a alocacao professor/turma/disciplina criada na fase anterior.

O recorte desta fase cobre:

- criacao de aula a partir de uma alocacao de professor;
- consulta de aulas por alocacao ou turma;
- registro da frequencia do professor na aula;
- registro da frequencia de alunos na aula;
- consulta das frequencias registradas por aula.

## Regra implementada

Uma aula pode ser criada quando:

- o `professorTurmaDisciplinaId` informado existe;
- a data da aula e informada.

A frequencia do professor pode ser registrada quando:

- a aula existe;
- ainda nao existe frequencia registrada para o professor da alocacao naquela aula.

A frequencia do aluno pode ser registrada quando:

- a aula existe;
- a matricula existe;
- a matricula pertence a mesma turma da aula;
- a situacao de frequencia existe no catalogo (`PRESENTE`, `FALTA`, `FALTA_JUSTIFICADA`);
- ainda nao existe frequencia registrada para a mesma matricula naquela aula.

## Endpoints adicionados

- `POST /api/aulas`
- `GET /api/aulas`
- `GET /api/aulas/{id}`
- `POST /api/aulas/{id}/frequencia-professor`
- `GET /api/aulas/{id}/frequencia-professor`
- `POST /api/aulas/{id}/frequencias-alunos`
- `GET /api/aulas/{id}/frequencias-alunos`

## Escopo realizado

- Criado controller REST de aulas.
- Criados DTOs de aula, frequencia de professor e frequencia de aluno.
- Criado `DiarioAulaService` com regras do fluxo.
- Ampliados repositories de aula e frequencia para consultas por turma, aula e duplicidade.
- Criadas excecoes de dominio para:
  - aula nao encontrada;
  - situacao de frequencia nao encontrada;
  - frequencia duplicada de professor;
  - frequencia duplicada de aluno;
  - matricula de aluno em turma diferente da aula.
- Adicionado tratamento HTTP para os novos erros de negocio.
- Criado teste de integracao do fluxo MVP.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/professor/adapter/in/web/AulaController.java`
- `school-management-service/src/main/java/br/com/escola/professor/application/service/DiarioAulaService.java`
- `school-management-service/src/main/java/br/com/escola/professor/adapter/out/persistence/repository/AulaJpaRepository.java`
- `school-management-service/src/main/java/br/com/escola/frequencia/adapter/out/persistence/repository/FrequenciaAlunoJpaRepository.java`
- `school-management-service/src/main/java/br/com/escola/frequencia/adapter/out/persistence/repository/FrequenciaProfessorJpaRepository.java`
- `school-management-service/src/test/java/br/com/escola/professor/adapter/in/web/AulaControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=AulaControllerIntegrationTest test
```

Resultado:

- 2 testes executados.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Criar endpoints de avaliacao e lancamento de notas.
- Criar consulta de frequencia por matricula/aluno.
- Definir regras de fechamento de aula e edicao/correcao de frequencia.
- Integrar frequencia e notas ao boletim.
