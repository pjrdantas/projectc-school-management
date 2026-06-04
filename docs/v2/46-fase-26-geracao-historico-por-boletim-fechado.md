# Fase 26 - Geração de histórico por boletim fechado

## Objetivo

Permitir que o histórico escolar interno seja gerado a partir de um boletim oficialmente fechado, evitando digitação manual de componentes curriculares quando já existem notas e frequências consolidadas.

## Entregas

- Endpoint `POST /api/historicos-escolares/matriculas/{matriculaId}/geracao-por-boletim`.
- DTO de geração com `boletimId`, `sobrescrever`, `ensinoConcluido` e `observacoes`.
- Validação para exigir boletim fechado existente e pertencente à matrícula informada.
- Geração de `HistoricoEscolar` com origem `INTERNO`.
- Geração de `HistoricoEscolarItem` a partir dos itens persistidos do boletim.
- Persistência de período letivo, série, disciplina, nota/conceito, frequência, carga horária e resultado nos itens internos.
- Regra de duplicidade por aluno e período letivo, com sobrescrita opcional.

## Contrato

```json
{
  "boletimId": "00000000-0000-0000-0000-000000000000",
  "sobrescrever": false,
  "ensinoConcluido": "ENSINO FUNDAMENTAL",
  "observacoes": "Histórico gerado por boletim fechado"
}
```

## Observações

- O cadastro manual de históricos permanece disponível.
- Históricos manuais continuam assumindo origem `EXTERNO`.
- A geração interna usa somente boletins já persistidos; o boletim consultivo não é usado como fonte oficial para histórico.
