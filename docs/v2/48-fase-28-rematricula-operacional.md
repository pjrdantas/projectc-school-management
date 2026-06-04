# Fase 28 - Rematrícula operacional

## Objetivo

Expor um fluxo operacional de rematrícula a partir de uma matrícula anterior concluída, reaproveitando as regras funcionais já existentes para renovação.

## Entregas

- Endpoint `POST /api/matriculas/{id}/rematricula`.
- DTO `MatriculaRematriculaRequest`.
- Validação da matrícula base:
  - deve existir;
  - deve estar com status `CONCLUIDA`.
- Criação da nova matrícula como tipo `RENOVACAO`.
- Reuso do `CriarMatriculaUseCase` para preservar as regras existentes:
  - aluno da matrícula anterior;
  - turma e período de destino informados;
  - validação de turma do período;
  - validação de série imediatamente posterior;
  - bloqueio de duplicidade no período.

## Contrato

```json
{
  "turmaId": "00000000-0000-0000-0000-000000000000",
  "periodoLetivoId": "00000000-0000-0000-0000-000000000000",
  "observacao": "Rematrícula gerada pelo fluxo operacional"
}
```

## Observações

- O endpoint retorna o mesmo `MatriculaResponse` usado na criação comum de matrícula.
- O status inicial da rematrícula continua `SOLICITADA`.
- A matrícula base apenas habilita o fluxo; a validação final continua centralizada no caso de uso de criação de matrícula.
