# Fase 47D - Consolidacao de contratos multi-escola

## Objetivo

Consolidar contratos que ja operam com escopo por escola, deixando explicito
qual escola foi usada nas respostas de dashboards operacionais antes de iniciar
BFF ou separacao de servicos.

## Escopo implementado

- Dashboard academico passa a retornar `escolaId` e `escolaNome`.
- Dashboard da secretaria passa a retornar `escolaId` e `escolaNome`.
- Dashboard do diretor passa a retornar `escolaId` e `escolaNome`.
- Dashboard do professor passa a retornar `escolaId` e `escolaNome`.
- Testes dos controllers de dashboard validam a escola padrao retornada.
- Testes unitarios de alertas e pacote frontend foram ajustados para os novos
  contratos.

## Decisoes tecnicas

- Nao foi criada troca dinamica de escola nesta fase.
- Os dashboards continuam usando a escola padrao resolvida pelo
  `EscolaTenantService`.
- Os novos campos seguem o padrao ja adotado em catalogos, pessoas,
  matriculas, documentos, aulas, avaliacoes, historico, planejamento, IA e
  snapshots.

## Fora do escopo

- BFF.
- Separacao de servicos.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- Customizacao de dashboards por escola.
- Kafka, MongoDB ou Redis.

## Validacao esperada

Backend:

- `.\mvnw.cmd "-Dtest=DashboardAcademicoControllerIntegrationTest,DashboardSecretariaControllerIntegrationTest,DashboardDiretorControllerIntegrationTest,DashboardProfessorControllerIntegrationTest,DashboardAlertaServiceTest,DashboardFrontendServiceTest" test`
- `.\mvnw.cmd test`

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 48A - desenho dos BFFs:

- definir BFFs por experiencia de uso;
- listar contratos agregados esperados para admin, secretaria, professor e
  diretor;
- ainda sem extrair servicos ou criar microfrontends separados.
