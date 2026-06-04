## Fase 39 - Historico comparativo de indicadores do dashboard

Esta fase adiciona consulta historica dos snapshots ja persistidos para transformar os indicadores em series temporais.

### Endpoint

- `GET /api/dashboard/snapshots/historico/publicos/{publicoCodigo}`

### Filtros

- `codigoIndicador`: indicador especifico.
- `dataInicio`: inicio do periodo.
- `dataFim`: fim do periodo.
- `professorId`: filtra snapshots de professor pelo identificador salvo em `valorTexto`.

### Retorno

Cada indicador retorna:

- publico do dashboard;
- codigo do indicador;
- descricao vigente no ultimo ponto da serie;
- valor atual;
- valor anterior;
- variacao percentual;
- pontos historicos ordenados por data.

### Regra de professor

Como a tabela atual de snapshots nao possui coluna de entidade de referencia, os snapshots do professor seguem o padrao definido na Fase 38:

- `PROFESSOR_{professorIdSemHifens}_{codigoIndicador}`

Quando `professorId` e `codigoIndicador` sao informados juntos, a API aceita o sufixo simples do indicador, como `aulas_realizadas`, e resolve o codigo completo internamente.

### Decisao tecnica

Nao houve alteracao de schema. A consulta historica trabalha sobre a estrutura atual de `dashboard_indicador_snapshot` e calcula a variacao percentual em memoria a partir dos dois ultimos pontos numericos da serie filtrada.

### Validacao

- Historico por indicador com tres pontos.
- Calculo de valor atual, anterior e variacao percentual.
- Filtro de professor por `professorId`.
- Rejeicao de periodo invertido.
