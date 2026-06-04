## Fase 40 - Agendamento automatico de snapshots do dashboard

Esta fase adiciona uma rotina agendada para gerar snapshots de dashboard sem chamada manual da API.

### Rotina

Componente:

- `DashboardSnapshotAgendamentoScheduler`

Execucao padrao:

- diariamente as 01:15;
- timezone `America/Sao_Paulo`;
- publicos `ACADEMICO`, `SECRETARIA` e `DIRETOR`;
- todos os professores ativos.

### Configuracoes

As configuracoes ficam em `application.yaml` e podem ser sobrescritas por variaveis de ambiente:

- `DASHBOARD_SNAPSHOTS_SCHEDULER_ENABLED`
- `DASHBOARD_SNAPSHOTS_SCHEDULER_CRON`
- `DASHBOARD_SNAPSHOTS_SCHEDULER_ZONE`
- `DASHBOARD_SNAPSHOTS_SCHEDULER_PUBLICOS`

### Comportamento

- A data de referencia padrao e a data atual.
- A geracao reaproveita `DashboardSnapshotGeradorService`.
- A persistencia continua idempotente por publico, indicador e data.
- Em caso de erro em um publico ou professor, a rotina registra o erro e continua os proximos itens.

### Professores

Para o dashboard do professor, a rotina percorre professores ativos por `ProfessorJpaRepository.findByAtivoTrueOrderByCreatedAtAsc()` e chama a geracao individual definida na Fase 38.

### Decisao tecnica

Nao houve alteracao de schema ou Flyway. A fase usa o mecanismo de scheduling ja existente no projeto e adiciona apenas configuracao operacional.

### Validacao

- Geracao dos publicos configurados.
- Geracao dos professores ativos.
- Continuidade quando um publico falha.
- Respeito a configuracao de agendamento desabilitado.
