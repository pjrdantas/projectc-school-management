# Fase 47C - Escopo por escola em planejamento e IA

## Objetivo

Aplicar a sexta subfase de escopo por escola em planejamento bimestral,
interacoes de IA, conteudos gerados e biblioteca pedagogica, sem alterar fluxo
de UI ou separar servicos.

## Escopo implementado

- Planejamentos bimestrais passam a validar a alocacao
  professor-turma-disciplina dentro da escola padrao.
- Listagem, busca, atualizacao, alteracao de status e inclusao de aulas ou
  avaliacoes previstas passam a operar somente dentro da escola padrao.
- Periodo avaliativo do planejamento passa a ser validado pela escola do
  periodo letivo.
- Geracao de conteudo IA passa a aceitar somente planejamentos da escola
  padrao.
- Listagem de interacoes e conteudos IA passa a ser filtrada pela escola do
  planejamento.
- Busca, versionamento, aprovacao e publicacao de conteudos IA passam a validar
  a escola do planejamento vinculado.
- Biblioteca pedagogica passa a listar conteudos ativos somente da escola do
  professor vinculado.
- Adicionados `escolaId` e `escolaNome` nas respostas de planejamento
  bimestral, interacao IA, conteudo IA e biblioteca pedagogica.

## Decisoes tecnicas

- `planejamento_bimestral`, `planejamento_ia_*` e
  `biblioteca_conteudo_pedagogico` nao receberam `id_escola` direto nesta
  subfase.
- A escola do planejamento e inferida pela turma da alocacao
  professor-turma-disciplina.
- A escola dos conteudos e interacoes IA e inferida pelo planejamento
  bimestral.
- A escola da biblioteca pedagogica e inferida pelo professor vinculado ao
  conteudo publicado.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.

## Fora do escopo

- Escopo por escola em dashboards.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- Migration para adicionar `id_escola` direto em planejamento, IA ou
  biblioteca.
- Integracao real com provedor de IA.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=PlanejamentoBimestralControllerIntegrationTest,PlanejamentoIAControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47C - proxima subfase de escopo por escola em dashboards:

- dashboards por perfil;
- snapshots e historicos de indicadores;
- configuracoes de dashboards e widgets.
