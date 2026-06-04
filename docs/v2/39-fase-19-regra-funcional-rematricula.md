# Fase 19 - Regra funcional de rematricula

## Objetivo

Habilitar o MVP de rematricula (`RENOVACAO`) com regras funcionais minimas e explicitas.

## Regra implementada

Uma rematricula e permitida quando:

- o aluno possui matricula anterior em periodo letivo diferente;
- a matricula anterior mais recente esta com status `EFETIVADA`;
- a turma de destino pertence a serie imediatamente posterior a serie da matricula anterior;
- nao existe outra matricula do mesmo aluno no periodo letivo de destino;
- a turma de destino possui vaga.

Uma rematricula e bloqueada quando:

- nao existe matricula anterior;
- a matricula anterior nao esta `EFETIVADA`;
- a turma de destino nao e da serie imediatamente posterior.

## Escopo realizado

- Removido o bloqueio temporario de rematricula no `CriarMatriculaUseCase`.
- Reaproveitado `MatriculaGateway.findHistoricoAnteriorMaisRecente`.
- Reaproveitado `TurmaConsultaGateway.findSerieOrdemByTurmaId`.
- Mantido status inicial de `RENOVACAO` como `SOLICITADA`.
- Ampliado teste de integracao de matricula para validar:
  - sucesso da rematricula com matricula anterior efetivada e serie posterior;
  - bloqueio da rematricula quando a matricula anterior nao esta efetivada.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/application/usecase/CriarMatriculaUseCase.java`
- `school-management-service/src/test/java/br/com/escola/matricula/adapter/in/web/MatriculaControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test
```

Resultado:

- 6 testes executados.
- 0 falhas.
- 0 erros.

## Decisoes pendentes

- Repetencia ainda nao foi implementada. Hoje a rematricula exige serie imediatamente posterior.
- Saltos de serie tambem nao foram implementados.
- Integracao com boletim/historico escolar para aprovar progressao fica para fase posterior.
