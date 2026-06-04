# Fase 30 - Dashboard academico-operacional

## Objetivo

Criar um primeiro endpoint de dashboard em tempo real para apoiar secretaria e gestao na leitura operacional do ciclo academico ja implementado.

## Endpoint

- `GET /api/dashboard/academico`

## Indicadores entregues

- Total de matriculas.
- Matriculas aguardando documentos.
- Matriculas concluidas.
- Matriculas efetivadas.
- Matriculas aptas para rematricula, considerando matriculas concluidas.
- Boletins fechados.
- Historicos internos gerados.
- Alunos aprovados por boletim fechado.
- Alunos reprovados por boletim fechado.
- Distribuicao de matriculas por status.
- Turmas ativas com vagas disponiveis.

## Decisoes

- O endpoint calcula os indicadores em tempo real e nao grava snapshots.
- As tabelas `dashboard`, `dashboard_widget`, `dashboard_usuario_configuracao` e `dashboard_indicador_snapshot` permanecem como base para uma fase posterior de configuracao e historizacao.
- O Swagger do novo endpoint usa tag explicita `Dashboard academico`, sem sufixo `controller`.

## Validacao

- Teste focado: `.\mvnw.cmd -Dtest=DashboardAcademicoControllerIntegrationTest test`
- Suite completa: `.\mvnw.cmd test`
