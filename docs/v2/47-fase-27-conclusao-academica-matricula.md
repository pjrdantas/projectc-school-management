# Fase 27 - Conclusão acadêmica da matrícula

## Objetivo

Conectar o boletim fechado ao desfecho acadêmico da matrícula, permitindo que a matrícula seja concluída somente quando o resultado oficial do boletim permitir.

## Entregas

- Inclusão do status `CONCLUIDA` no enum local de matrícula.
- Endpoint `POST /api/matriculas/{id}/conclusao-academica`.
- DTOs de request/response para conclusão acadêmica.
- Regra de validação para exigir boletim fechado pertencente à matrícula.
- Regra de validação para impedir conclusão quando o boletim não tem itens ou possui componentes pendentes.
- Resultado final calculado pelos itens do boletim:
  - todos aprovados: resultado `APROVADO` e matrícula `CONCLUIDA`;
  - qualquer reprovado: resultado `REPROVADO` e matrícula permanece `EFETIVADA` com registro em observação.
- Rematrícula passa a exigir matrícula anterior `CONCLUIDA`, não apenas `EFETIVADA`.

## Contrato

```json
{
  "boletimId": "00000000-0000-0000-0000-000000000000",
  "observacao": "Fechamento aprovado pela secretaria"
}
```

## Observações

- A regra preserva o boletim fechado como fonte oficial do desfecho acadêmico.
- A matrícula reprovada não é marcada como concluída, bloqueando a rematrícula automática para série posterior.
- O histórico escolar gerado na fase anterior permanece independente, mas usa a mesma fonte oficial: boletim fechado persistido.
