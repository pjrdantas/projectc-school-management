# Fase 29 - Elegibilidade de rematrícula

## Objetivo

Permitir que a secretaria consulte se uma matrícula concluída pode gerar rematrícula antes de executar a criação da nova matrícula.

## Entregas

- Endpoint `GET /api/matriculas/{id}/rematricula/elegibilidade`.
- DTO `MatriculaRematriculaElegibilidadeResponse`.
- Consulta opcional com `turmaId` e `periodoLetivoId`.
- Retorno com:
  - matrícula base;
  - aluno;
  - status base;
  - série base;
  - turma/período/série de destino, quando informados;
  - flag `elegivel`;
  - lista de motivos de bloqueio.

## Regras avaliadas

- Matrícula base deve estar `CONCLUIDA`.
- Turma de destino deve existir, quando informada.
- Turma de destino deve pertencer ao período letivo informado, quando ambos forem informados.
- Aluno não pode possuir matrícula no período letivo de destino.
- Turma de destino deve ser da série imediatamente posterior.
- Turma de destino deve possuir vaga disponível.

## Observações

- A consulta não cria matrícula.
- A criação operacional da rematrícula continua no endpoint `POST /api/matriculas/{id}/rematricula`.
- A consulta usa as mesmas premissas da criação para reduzir tentativa e erro no frontend.
