# Fase 23 - Consulta academica consolidada por matricula

## Objetivo

Criar uma consulta consolidada por matricula antes de iniciar boletim.

O recorte desta fase reune em uma unica resposta:

- dados principais da matricula;
- dados do aluno;
- turma e periodo letivo;
- frequencias registradas;
- notas lancadas;
- indicadores simples de frequencia e desempenho.

## Endpoint adicionado

- `GET /api/matriculas/{matriculaId}/academico`

## Escopo realizado

- Criado controller REST de resumo academico da matricula.
- Criados DTOs especificos para:
  - resumo academico;
  - indicadores;
  - frequencias;
  - notas.
- Criado `MatriculaAcademicoResumoService`.
- Reaproveitados repositories ja existentes de:
  - matricula;
  - frequencia de aluno;
  - nota de aluno.
- Criado teste de integracao montando:
  - turma;
  - disciplina;
  - professor;
  - alocacao professor/turma/disciplina;
  - aluno;
  - matricula efetivada;
  - aula;
  - frequencia;
  - avaliacao;
  - nota.

## Indicadores retornados

- total de frequencias;
- presencas;
- faltas;
- faltas justificadas;
- total de notas;
- media simples das notas;
- media percentual em relacao ao valor maximo das avaliacoes.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaAcademicoController.java`
- `school-management-service/src/main/java/br/com/escola/matricula/application/service/MatriculaAcademicoResumoService.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/dto/MatriculaAcademicoResumoResponse.java`
- `school-management-service/src/test/java/br/com/escola/matricula/adapter/in/web/MatriculaAcademicoControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=MatriculaAcademicoControllerIntegrationTest test
```

Resultado:

- 1 teste executado.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Usar a consulta consolidada como base para boletim.
- Definir regras de fechamento de periodo avaliativo.
- Definir formula oficial de media final.
- Diferenciar frequencia por disciplina e frequencia global.
