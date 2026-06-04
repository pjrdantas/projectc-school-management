# Fase 25 - Fechamento persistido do boletim

## Objetivo

Transformar o boletim consultivo da matrícula em um fechamento oficial persistido, mantendo o cálculo em tempo real disponível para consulta acadêmica.

## Entregas

- Correção do mapeamento JPA de `boletim` para respeitar a unicidade por matrícula e período de referência.
- Correção do mapeamento JPA de `boletim_item` para respeitar a unicidade por boletim e disciplina.
- Endpoint `POST /api/matriculas/{matriculaId}/boletim/fechamento`.
- Endpoint `GET /api/matriculas/{matriculaId}/boletim/fechamentos`.
- Regra de duplicidade: um boletim já fechado para a mesma matrícula e período retorna conflito, salvo quando a requisição informar `sobrescrever=true`.
- Persistência dos itens calculados por disciplina com média, frequência percentual e resultado.

## Contrato de fechamento

```json
{
  "periodoReferencia": "2043.2",
  "dataFechamento": "2043-12-21",
  "observacao": "Fechamento oficial",
  "sobrescrever": false
}
```

## Observações

- O endpoint `GET /api/matriculas/{matriculaId}/boletim` permanece consultivo e não grava dados.
- O fechamento reaproveita o cálculo do boletim consultivo para evitar divergência entre consulta e persistência.
- A data de fechamento assume a data atual quando não for informada.
