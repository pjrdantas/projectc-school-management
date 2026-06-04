## Fase 38 - Geracao de snapshots do dashboard do professor

Esta fase adiciona a geracao automatica de snapshots para o dashboard do professor por docente.

### Endpoint

- `POST /api/dashboard/snapshots/geracoes/professores/{professorId}`
- `POST /api/dashboard/snapshots/geracoes/professores/{professorId}?referenciaData={yyyy-MM-dd}`

### Comportamento

- Busca o publico de dashboard `PROFESSOR`.
- Consulta os indicadores operacionais do professor pelo `DashboardProfessorService`.
- Persiste os indicadores na tabela `dashboard_indicador_snapshot`.
- Mantem idempotencia por professor, indicador e data usando codigo de indicador escopado pelo docente.

### Codigos gerados

Os codigos seguem o padrao:

- `PROFESSOR_{professorIdSemHifens}_TURMAS_VINCULADAS`
- `PROFESSOR_{professorIdSemHifens}_ALOCACOES_ATIVAS`
- `PROFESSOR_{professorIdSemHifens}_AULAS_PLANEJADAS`
- `PROFESSOR_{professorIdSemHifens}_AULAS_REALIZADAS`
- `PROFESSOR_{professorIdSemHifens}_FREQUENCIAS_PENDENTES`
- `PROFESSOR_{professorIdSemHifens}_AVALIACOES_REGISTRADAS`
- `PROFESSOR_{professorIdSemHifens}_AVALIACOES_COM_NOTAS_PENDENTES`
- `PROFESSOR_{professorIdSemHifens}_PLANEJAMENTOS_BIMESTRAIS`
- `PROFESSOR_{professorIdSemHifens}_PLANEJAMENTOS_BIMESTRAIS_PENDENTES`

O campo `valorTexto` recebe o `professorId`, permitindo identificar o docente sem alterar o schema atual.

### Decisao tecnica

A tabela `dashboard_indicador_snapshot` possui unicidade por publico, codigo do indicador e data de referencia. Por isso, snapshots de professores diferentes colidiriam se todos usassem codigos globais como `AULAS_REALIZADAS`.

Nesta fase foi mantido o schema atual e o escopo por professor foi incorporado ao `codigoIndicador`.

### Validacao

- Teste de integracao para geracao por professor.
- Teste de idempotencia por professor e data.
- Consulta posterior dos snapshots persistidos pelo publico `PROFESSOR`.
