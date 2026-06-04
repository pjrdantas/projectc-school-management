# Fase 14 - Matricula MVP e integracao

## Objetivo

Validar o fluxo MVP de matricula usando a estrutura de dominio ja criada para o modulo `matricula`.

Esta fase nao altera a regra de negocio de rematricula, que segue bloqueada no `CriarMatriculaUseCase` ate uma decisao funcional sobre historico escolar/documentos.

## Escopo realizado

- Criado teste de integracao real para `MatriculaController`.
- Validado o fluxo de primeira matricula:
  - criacao de aluno;
  - criacao de periodo letivo;
  - criacao de turma;
  - criacao de matricula;
  - geracao de etapas iniciais;
  - consulta de matriculas por aluno e status;
  - atualizacao de status para `EFETIVADA` com justificativa.
- Validada a regra de bloqueio de matricula duplicada para o mesmo aluno no mesmo periodo letivo.

## Arquivos alterados

- `school-management-service/src/test/java/br/com/escola/matricula/adapter/in/web/MatriculaControllerIntegrationTest.java`

## Endpoints cobertos

- `POST /api/matriculas`
- `GET /api/matriculas`
- `PATCH /api/matriculas/{id}/status`

## Resultado dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test
```

Resultado:

- 2 testes executados.
- 0 falhas.
- 0 erros.

## Pontos pendentes para fases futuras

- Definir a regra funcional de rematricula.
- Expor operacoes especificas para avancar/concluir etapas de matricula.
- Integrar documentos exigidos e documentos entregues ao fluxo de matricula.
