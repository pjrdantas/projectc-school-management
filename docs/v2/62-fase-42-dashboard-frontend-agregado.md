## Fase 42 - Pacote agregado para frontend dos dashboards

Esta fase adiciona um endpoint pensado para montagem inicial da tela de dashboard no frontend, reduzindo a necessidade de varias chamadas separadas.

### Endpoint

- `GET /api/dashboard/frontend?publicoCodigo={publicoCodigo}`
- `GET /api/dashboard/frontend?publicoCodigo={publicoCodigo}&usuarioId={usuarioId}`
- `GET /api/dashboard/frontend?publicoCodigo=PROFESSOR&professorId={professorId}`
- `GET /api/dashboard/frontend?publicoCodigo=PROFESSOR&professorId={professorId}&usuarioId={usuarioId}`

### Publicos suportados

- `ACADEMICO`
- `SECRETARIA`
- `DIRETOR`
- `PROFESSOR`

Para `PROFESSOR`, `professorId` e obrigatorio.

### Retorno agregado

O payload inclui:

- publico do dashboard;
- usuario, quando informado;
- professor, quando aplicavel;
- resumo atual do dashboard;
- alertas criticos;
- dashboards configurados para o publico;
- widgets de cada dashboard;
- configuracoes individuais do usuario, quando `usuarioId` for informado;
- historico principal dos indicadores.

### Historico

A janela padrao do historico e de 30 dias e pode ser configurada por:

- `DASHBOARD_FRONTEND_HISTORICO_DIAS`

### Decisao tecnica

Nao houve alteracao de schema. O endpoint agrega services ja existentes:

- dashboards operacionais;
- alertas;
- configuracao administrativa;
- configuracao por usuario;
- historico de snapshots.

### Objetivo para o frontend

Permitir que a primeira tela do dashboard seja montada com uma chamada principal, deixando chamadas especificas apenas para interacoes posteriores, como edicao de configuracao ou detalhamento de indicadores.

### Validacao

- Teste unitario do agregador.
- Teste de configuracao por usuario no pacote.
- Teste de obrigatoriedade do `professorId`.
- Teste de integracao da rota.
