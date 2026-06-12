# Fase 47C - Escopo por escola em dashboards

## Objetivo

Aplicar a setima subfase de escopo por escola nos dashboards operacionais,
snapshots e historicos de indicadores, sem alterar frontend, BFF ou servicos
externos.

## Escopo implementado

- Dashboard academico passa a contar matriculas, boletins, historicos e vagas
  somente da escola padrao.
- Dashboard da secretaria passa a contar matriculas, documentos pendentes,
  transferencias e solicitacoes de exclusao somente da escola padrao.
- Dashboard do diretor passa a contar alunos, turmas, professores alocados,
  aulas, avaliacoes e pendencias somente da escola padrao.
- Dashboard do professor passa a aceitar apenas professor vinculado a escola
  padrao e filtrar suas alocacoes pelo mesmo escopo.
- Snapshots de indicadores passam a carregar `id_escola` diretamente.
- Listagem, historico e gravacao de snapshots passam a operar pela escola
  padrao.
- Resposta de snapshot passa a retornar `escolaId` e `escolaNome`.

## Decisoes tecnicas

- `dashboard_indicador_snapshot` recebeu `id_escola` direto por ser dado
  agregado e historico.
- `dashboard`, `dashboard_widget` e `publico_dashboard` permanecem globais
  nesta subfase.
- Configuracoes de dashboard por usuario continuam isoladas pelo proprio
  usuario; nao foi adicionada coluna `id_escola` direta nesta etapa.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.

## Fora do escopo

- Troca de escola ativa pelo frontend.
- Usuario com multiplas escolas.
- Customizacao de dashboards/widgets por escola.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao esperada

Backend:

- `.\mvnw.cmd "-Dtest=DashboardAcademicoControllerIntegrationTest,DashboardSecretariaControllerIntegrationTest,DashboardDiretorControllerIntegrationTest,DashboardProfessorControllerIntegrationTest,DashboardIndicadorSnapshotControllerIntegrationTest,DashboardSnapshotGeradorControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47D - consolidacao multi-escola basica:

- revisar contratos que agora retornam `escolaId`/`escolaNome`;
- identificar lacunas restantes de escopo por escola antes de iniciar BFF;
- manter revisao pontual, sem refatoracao ampla.
