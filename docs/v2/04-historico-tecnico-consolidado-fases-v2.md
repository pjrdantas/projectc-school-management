# Historico tecnico consolidado das fases v2

Este documento substitui os arquivos individuais de registro de fases que existiam em docs/v2. Ele preserva o historico tecnico em formato compacto e evita que o projeto fique poluido por um arquivo por microfase.

## Documentos estruturais mantidos

- `01-plano-tecnico-refatoracao-backend-v2.md`
- `02-diagnostico-endpoints-backend-v2.md`
- `03-consolidacao-frontend-v2.md`
- `96-roadmap-pos-mvp-planejamento-ia-multiescola-bff-servicos.md`
- `enunciado_codex_refatoracao_v2_backend_frontend.md`
- `modelo_normalizado_escolar_v3_documentos_ia_dashboards.sql`
- `scriptdb.sql`

## Estado atual consolidado

- O monolito continua como runtime funcional e fonte dos dados em producao; o
  BFF e o primeiro servico de catalogo existem como fundacao do strangler, ainda
  sem cutover do frontend ou das escritas.
- PostgreSQL, Kafka, MongoDB e Redis possuem fundacao local no Compose. O
  PostgreSQL do catalogo possui modelo e migrations proprias, o Kafka recebe os
  eventos da outbox e o Redis pode armazenar snapshots descartaveis por escola
  quando as respectivas features sao habilitadas; MongoDB ainda nao participa
  do fluxo funcional.
- A arquitetura-alvo foi redefinida para incluir BFF orquestrador, servicos por
  dominio, Kafka, MongoDB e Redis. O plano executavel e o mapa de propriedade de
  dados estao em `96-roadmap-pos-mvp-planejamento-ia-multiescola-bff-servicos.md`.
- O roadmap arquitetural agora tambem documenta explicitamente a arvore final
  do monorepo, a arvore de plataforma, o papel de cada pasta raiz e a
  convencao estrutural obrigatoria de cada servico extraido.
- A primeira subfase da Fase 52 confirmou que identidade e tenant ainda estao
  acoplados no monolito atual: `AuthService` concentra sessao opaca,
  autenticacao e contexto autenticado; `EscolaTenantService` continua sendo a
  autoridade local de tenant por `usuario.id_escola` e escola padrao, sem
  `usuario_escola` nem troca explicita de escola ativa.
- `EscolaContextoPort` ja foi aplicado em dashboards, snapshots, consultas auxiliares de matricula, catalogo interno, planejamento bimestral, diario de aula e avaliacoes.
- `EstruturaTurmaPort` esta consolidado em planejamento bimestral, diario de aula e avaliacoes.
- Os usos remanescentes de `EscolaTenantService` exigem diagnostico pontual antes de novas trocas, principalmente em persistencia, matricula, documentos, historico, IA, pessoa, professor e seguranca.
- O diagnostico de `DisciplinaService` confirmou que a troca para
  `EscolaContextoPort` ja estava aplicada no `develop`; os riscos remanescentes
  sao de autoridade do tenant nos contratos de escrita.
- A Fase 51B criou o parent Maven, `school-management-bff`,
  `academic-catalog-service`, contratos transversais, Compose e testes de
  arquitetura/Testcontainers, sem migrar dados ou rotas.
- A Fase 51C criou a rota piloto read-only `GET /api/disciplinas` no BFF para o
  monolito, com contrato proprio, propagacao explicita do bearer/correlation ID,
  descarte de headers de tenant forjados, timeout, circuit breaker, metricas e
  rollback por feature flag. O frontend continua direto no monolito.
- A primeira subfase da Fase 51D criou o dominio e a persistencia independentes
  de periodo letivo, serie, turno, turma, disciplina e turma-disciplina no
  `academic-catalog-service`. Migrations para banco vazio, portas de repositorio,
  adaptadores JPA e chaves compostas garantem isolamento entre duas escolas, sem
  duplicar a entidade escola e sem cutover.
- A segunda subfase da Fase 51D criou casos de uso e contratos REST internos de
  leitura em `/internal/v1` para todo o catalogo piloto. Token service-to-service
  e contexto obrigatorio de usuario/escola protegem as rotas; DTOs proprios e
  testes Testcontainers com duas escolas preservam o isolamento sem acoplar o
  servico aos contratos do monolito.
- A terceira subfase da Fase 51D criou comandos internos de criacao com tenant,
  `Idempotency-Key` por escola e cinco eventos versionados. Agregado,
  idempotencia PostgreSQL e outbox `PENDENTE` compartilham a mesma transacao;
  conflitos e referencias entre escolas revertem todos os efeitos. O antigo
  `SETNX` Redis deixou de ser usado como fonte oficial de idempotencia.
- A quarta subfase da Fase 51D implementou o publisher da outbox no Kafka com
  reivindicacao concorrente, confirmacao pelo broker antes da mudanca de estado,
  retry exponencial, DLT, metricas e testes Testcontainers PostgreSQL/Kafka. A
  ativacao permanece protegida por feature flag e sem cutover.
- A quinta subfase da Fase 51D implementou cache Redis das leituras por escola,
  com chave versionada, TTL, invalidacao local e por eventos Kafka, metricas e
  comportamento fail-open comprovado com Testcontainers. PostgreSQL permanece
  como fonte oficial e a feature fica desabilitada por padrao.
- A sexta subfase da Fase 51D implementou migracao opt-in e repetivel do banco
  do monolito para o PostgreSQL do catalogo. O executor preserva IDs, valida
  referencias multi-escola, opera em dry-run por padrao e produz relatorio JSON
  com contagens por escola e IDs ausentes, inesperados ou divergentes. Dois
  PostgreSQL Testcontainers comprovaram repeticao e reconciliacao sem cutover.
- A setima subfase da Fase 51D expandiu o BFF para as leituras externas do
  catalogo e introduziu cutover rota a rota para o `academic-catalog-service`,
  sempre protegido por feature flag, gate de relatorio reconciliado e fallback
  automatico para o monolito. Como identity/tenant ainda pertencem ao monolito,
  o BFF passou a resolver `usuarioId` e `escolaId` pelo endpoint interno
  `GET /api/auth/contexto-atual` antes de chamar o servico novo.
- A oitava subfase da Fase 51D executou a migracao real reconciliada e habilitou
  no BFF as leituras `GET /api/disciplinas` e `GET /api/turnos/{id}`. O primeiro
  bloqueio operacional real veio de colisao entre os IDs dos seeds globais do
  servico novo e os IDs reais do monolito; o migrador passou a substituir esses
  seeds apenas quando o destino ainda nao possui dados escolares.
- A nona subfase da Fase 51D ampliou o cutover read-only para
  `GET /api/disciplinas/{id}`, `GET /api/periodos-letivos` e
  `GET /api/periodos-letivos/{id}`, com validacao de metricas e fallback
  comprovado apos indisponibilidade deliberada do `academic-catalog-service`.
- A decima subfase da Fase 51D ampliou o cutover read-only para
  `GET /api/series`, `GET /api/series/{id}`, `GET /api/turmas` e
  `GET /api/turmas/{id}`, com validacao operacional ponta a ponta, metricas e
  fallback comprovado tambem nessas quatro rotas.
- A decima-primeira subfase da Fase 51D concluiu o bloco read-only do catalogo
  com `GET /api/turmas/{turmaId}/disciplinas`, `GET /api/turnos`,
  `GET /api/academico/catalogos/turnos` e
  `GET /api/academico/catalogos/niveis-ensino`, com validacao operacional,
  metricas e fallback comprovado tambem nessas quatro rotas.
- A decima-segunda subfase da Fase 51D consolidou observabilidade e operacao do
  cutover read-only inteiro no BFF, com motivo explicito de roteamento,
  metricas por rota/alvo/resultado, contador de falhas do catalogo novo,
  contador de fallback e health dedicado para o gate do relatorio reconciliado.
- A decima-terceira subfase da Fase 51D iniciou a primeira escrita controlada
  do catalogo via BFF em `POST /api/periodos-letivos`, com feature flag
  propria, gate pelo relatorio reconciliado, `Idempotency-Key` repassado ou
  gerado no BFF, metricas de escrita, health dedicado e rejeicao de `escolaId`
  divergente do contexto autenticado. O rollback continua sendo por flag; nao
  ha fallback automatico para o monolito depois que a escrita tenta o servico
  novo.
- A decima-quarta subfase da Fase 51D expandiu a escrita controlada do catalogo
  via BFF para `POST /api/disciplinas`, mantendo feature flag propria por rota,
  gate pelo relatorio reconciliado, `Idempotency-Key`, metricas e health
  dedicados. Para reduzir risco, o BFF passou a enviar ao
  `academic-catalog-service` apenas payload compativel com o contrato novo
  (`status` ausente ou `ATIVA`) e a manter o monolito como destino direto
  quando o payload pede `status` nao compativel, sem fallback automatico depois
  de uma tentativa de escrita no servico novo.
- A decima-quinta subfase da Fase 51D diagnosticou a incompatibilidade pontual
  de contrato de `POST /api/series` (`nivelEnsino` textual no monolito versus
  `nivelEnsinoId` UUID no servico novo) e expandiu a escrita controlada no BFF
  com uma adaptacao minima e isolada: resolucao interna do nivel de ensino no
  catalogo novo antes da escrita. O BFF passou a enviar ao
  `academic-catalog-service` apenas payloads com `ordem` positiva e
  `nivelEnsino` resolvivel; quando o nivel nao vem informado ou nao resolve no
  catalogo novo, o destino continua sendo diretamente o monolito, sem fallback
  automatico depois de uma tentativa de escrita no servico novo.
- A decima-sexta subfase da Fase 51D diagnosticou a incompatibilidade pontual
  de contrato de `POST /api/turmas` (`turno` textual e `status` externo no
  monolito versus `turnoId` UUID e `ativo` interno no servico novo) e expandiu
  a escrita controlada no BFF com adaptacao minima e isolada: resolucao interna
  do turno no catalogo novo antes da escrita e gate explicito para `status`
  compativel. O BFF passou a enviar ao `academic-catalog-service` apenas
  payloads com `capacidade` positiva, `periodoLetivoId`, `serieId`, `turno`
  resolvivel e `status` ausente ou `ATIVA`; quando o turno nao resolve ou o
  status nao e compativel, o destino continua sendo diretamente o monolito, sem
  fallback automatico depois de uma tentativa de escrita no servico novo.
- A decima-setima subfase da Fase 51D diagnosticou o contrato de
  `POST /api/turmas/{turmaId}/disciplinas` e confirmou compatibilidade direta
  entre monolito e `academic-catalog-service` para o payload externo
  (`disciplinaId`, `cargaHoraria`) e para a resposta funcional do vinculo. Com
  isso, o BFF expandiu a escrita controlada dessa rota sem adaptacao ampla de
  contrato: `Idempotency-Key`, observabilidade, rollback por flag e ausencia de
  fallback automatico para o monolito depois que a escrita tenta o servico
  novo.
- A decima-oitava subfase da Fase 51D diagnosticou `POST /api/professores` e
  `POST /api/professores/{id}/turmas-disciplinas` e concluiu que a troca para
  cutover no BFF ainda nao e segura. O contrato do monolito depende do dominio
  de RH e pessoas (`funcionarioId`, `pessoa`, `escola`, `ativo`) para criar
  professor e do agregado academico ja montado (`turmaDisciplinaId`) para a
  alocacao, enquanto o unico servico novo em operacao nesta frente,
  `academic-catalog-service`, nao expoe endpoints nem modelo interno para
  professores ou alocacoes. Nesta etapa nao foi aplicada refatoracao ampla nem
  rota nova no BFF; o resultado foi o fechamento objetivo do ponto de bloqueio
  arquitetural e do proximo recorte minimo seguro.
- A decima-nona subfase da Fase 51D definiu o contrato interno minimo do
  dominio de professores dentro do monolito atual, sem alterar o contrato
  externo nem abrir rota nova no BFF: foram introduzidos DTOs internos de
  criacao e alocacao, a porta `ProfessorAcademicoPort` e a adaptacao minima do
  `ProfessorService` para expor criacao de professor, consulta por id,
  verificacao de existencia, alocacao professor-turma-disciplina e listagem de
  alocacoes por escola. Isso cria a fronteira interna necessaria para uma
  futura extracao incremental do dominio sem refatoracao ampla imediata.
- A vigesima subfase da Fase 51D expôs esse contrato interno de professores por
  um adaptador web dedicado e de baixo risco dentro do
  `school-management-service`, sem alterar o contrato externo atual nem abrir
  cutover no BFF. Foram adicionados endpoints internos autenticados com escopo
  explicito por `X-Escola-Id` para criacao de professor, consulta por id,
  criacao de alocacao professor-turma-disciplina e listagem de alocacoes do
  professor. O adaptador reutiliza a porta `ProfessorAcademicoPort`, preserva o
  monolito como implementacao unica nesta etapa e deixa o contrato
  backend/backend pronto para o proximo consumo incremental.
- A vigesima-primeira subfase da Fase 51D introduziu o cliente
  backend/backend desse adaptador interno e passou a consumi-lo de forma
  controlada no fluxo externo de professores, ainda sem mudar rotas externas do
  BFF. O `ProfessorController` passou a orquestrar criacao, consulta por id,
  alocacao professor-turma-disciplina e listagem de alocacoes via cliente
  interno opcional, protegido por feature flag, com metricas Micrometer e
  fallback local simples para o `ProfessorService` em caso de falha HTTP.
- A vigesima-segunda subfase da Fase 51D habilitou esse cliente em um teste
  operacional controlado dentro do proprio backend, usando porta aleatoria,
  `base-url` explicita e autenticacao real por `Bearer` obtido via
  `/api/auth/login`. O fluxo validou ponta a ponta as operacoes de criacao de
  professor, consulta por id, alocacao professor-turma-disciplina e listagem de
  alocacoes atraves do cliente HTTP interno, sem acionar fallback. Para suportar
  esse cenario com seguranca, a resolucao da `base-url` do
  `ProfessorInternalApiClient` passou a ser tardia, no momento da chamada,
  preservando a configuracao em runtime e em testes com `local.server.port`.
- A vigesima-terceira subfase da Fase 51D adicionou observabilidade operacional
  explicita para esse consumo interno de professores, ainda dentro do
  `school-management-service` e sem extracao fisica do dominio. Foi criado um
  `HealthIndicator` dedicado do cliente interno de professores, exposto pelo
  actuator em `/actuator/health/professorInternalClient`, com diagnostico de
  baixo risco baseado em configuracao resolvida (`enabled`,
  `fallbackLocalOnError`, `baseUrlScheme`, `baseUrlHost`) e sinais operacionais
  agregados das metricas ja existentes (`requestsTotal`, `fallbacksTotal`).
  Tambem foi aberto acesso anonimo apenas para `/actuator/health/**` e
  `/actuator/info`, permitindo verificacao operacional sem alterar rotas de
  negocio. Testes dedicados cobriram tanto a logica pura do indicador quanto a
  exposicao real do actuator, alem de preservar o teste operacional ponta a
  ponta do cliente interno.
- A vigesima-quarta subfase da Fase 51D introduziu esse contrato interno minimo
  de consulta/elegibilidade de funcionario para reduzir o acoplamento atual
  entre professor e RH/pessoa, ainda sem abrir runtime novo nem ampliar o BFF.
  Foi criada a porta `FuncionarioProfessorPort`, com DTO interno proprio e
  implementacao local em RH para busca de funcionario por escola e listagem de
  funcionarios elegiveis para cadastro de professor. O `ProfessorService`
  deixou de consultar `FuncionarioJpaRepository` e `FuncionarioEntity`
  diretamente, passando a criar professor a partir do resumo interno do
  funcionario e de uma referencia JPA de `PessoaEntity`. Tambem foram expostos
  endpoints internos autenticados `GET /internal/funcionarios/{id}/professor` e
  `GET /internal/funcionarios/professor-elegiveis`, preparando uma futura
  separacao fisica incremental do dominio com contrato backend/backend explicito.
- A vigesima-quinta subfase da Fase 51D preparou a primeira separacao fisica
  incremental do dominio de professores em modo shadow/read-only, sem mover
  escritas, sem alterar rotas do BFF e sem introduzir persistencia propria.
  Foi criado o modulo `academic-professor-service` no monorepo, com estrutura
  em camadas, actuator e API interna em `/internal/v1`. Esse runtime novo
  consome por HTTP os contratos internos ja estabilizados no monolito atual:
  `GET /internal/professores/{id}`,
  `GET /internal/professores/{id}/turmas-disciplinas` e
  `GET /internal/funcionarios/professor-elegiveis`. A protecao do novo runtime
  foi mantida por token interno mais contexto obrigatorio de correlacao,
  usuario e escola; o `Authorization` recebido e apenas propagado ao monolito
  para a leitura shadow. Testes com `MockWebServer` validaram ponta a ponta o
  roteamento interno, a propagacao de headers e o mapeamento de 404 sem iniciar
  um novo runtime real nem acionar banco.
- A vigesima-sexta subfase da Fase 51D expandiu esse runtime shadow para as
  leituras remanescentes mais seguras do dominio de professores, ainda sem
  mover escritas, sem alterar rotas do BFF e sem introduzir persistencia
  propria. O contrato interno do monolito passou a expor `GET /internal/professores`
  e `GET /internal/professores/turmas/{turmaId}`, reaproveitando a porta
  `ProfessorAcademicoPort` para separar listagem geral e listagem por turma do
  restante do fluxo de professor. Em paralelo, o
  `academic-professor-service` passou a consumir esses contratos e a expor
  `GET /internal/v1/professores` e `GET /internal/v1/turmas/{turmaId}/professores`
  como leituras shadow/read-only observaveis. Testes integrados no monolito e
  no runtime shadow validaram os novos contratos ponta a ponta, incluindo
  propagacao de contexto e mapeamento de respostas, sem abrir cutover no BFF.
- A vigesima-setima subfase da Fase 51D passou a consumir essas duas leituras
  shadow de forma observavel dentro do backend atual, ainda sem redirecionar o
  BFF nem mover escritas de professor. O `ProfessorFluxoOrquestradorService`
  passou a aplicar a mesma feature flag, as mesmas metricas por operacao e o
  mesmo fallback local imediato tambem para `GET /api/professores` e
  `GET /api/turmas/{turmaId}/professores`, reaproveitando o cliente interno ja
  estabilizado. O `TurmaProfessorController` deixou de chamar o servico local
  diretamente para usar o mesmo orquestrador e manter a observabilidade
  consistente nas duas rotas read-only alvo. Testes unitarios, integracoes web
  e fluxo operacional autenticado validaram as novas leituras com consumo do
  cliente interno, incremento de metricas e ausencia de fallback indevido.
- A vigesima-oitava subfase da Fase 51D consolidou esse bloco read-only
  observavel com diagnostico operacional dedicado, ainda sem cutover externo.
  O `HealthIndicator` dedicado do cliente interno de professores passou a
  expor um mapa explicito das rotas shadow de leitura por operacao, incluindo
  rota externa, rota interna correspondente e contadores separados de sucesso
  interno, erro interno, fallback local, feature desabilitada e total de
  fallbacks. Com isso, `GET /api/professores`,
  `GET /api/professores/{id}`, `GET /api/professores/{id}/turmas-disciplinas`
  e `GET /api/turmas/{turmaId}/professores` ficaram distinguiveis
  operacionalmente no actuator sem alterar o fluxo funcional. Testes unitarios
  e de endpoint validaram a nova visao detalhada no
  `/actuator/health/professorInternalClient`, enquanto os testes operacionais
  preservaram o consumo observavel do cliente interno.
- A vigesima-nona subfase da Fase 51D aplicou esse mesmo padrao de diagnostico
  explicito ao runtime shadow `academic-professor-service`, ainda sem cutover
  externo. O cliente `MonolithProfessorReadClient` passou a registrar metricas
  por operacao e resultado ao consumir o monolito, distinguindo sucesso,
  `not_found` e erro nas leituras shadow. Foi criado um `HealthIndicator`
  dedicado para o runtime shadow, expondo em
  `/actuator/health/professorShadowMonolith` um mapa por rota `/internal/v1`
  com a rota correspondente do monolito, contadores por resultado e sinais
  explicitos da dependencia remota, incluindo base URL e timeouts configurados.
  Testes unitarios, teste de endpoint do actuator e os testes ja existentes do
  controller shadow validaram a nova observabilidade sem alterar o contrato
  funcional das leituras.
- A trigesima subfase da Fase 51D usou essa observabilidade fechada para
  executar um smoke operacional controlado do runtime shadow em porta aleatoria,
  ainda sem cutover externo. O smoke exercitou tres cenarios contra a
  dependencia remota controlada: sucesso em `GET /internal/v1/professores`,
  `not_found` em `GET /internal/v1/professores/{id}` e erro remoto em
  `GET /internal/v1/professores/funcionarios-elegiveis`. Ao final, o teste
  consultou `/actuator/health/professorShadowMonolith` e comprovou, em runtime
  real do `academic-professor-service`, os contadores por rota e os sinais de
  sucesso, `not_found` e falha do lado remoto. Os testes anteriores do runtime
  shadow foram preservados e o backend principal permaneceu validado sem mover
  escrita alguma.
- A proxima subfase da 51D deve converter esse smoke controlado em um smoke
  operacional ponta a ponta mais proximo do ambiente real, conectando o
  `academic-professor-service` ao `school-management-service` real em perfil
  controlado para observar os healths dos dois lados na mesma execucao, ainda
  sem cutover externo.
- A trigesima primeira subfase da Fase 51D preparou exatamente esse passo
  operacional sem ampliar a topologia do build: foi criado no
  `school-management-service` um perfil de smoke `professor-shadow-operational`
  sobre H2, com seed minimo de usuario, contexto escolar e professor, e foi
  adicionado o script
  `scripts/operational/professor-shadow-operational-smoke.ps1` para subir os
  dois backends em portas controladas, autenticar no monolito, exercitar
  leituras externas e shadow e consolidar, no mesmo relatorio, os healths
  `/actuator/health/professorInternalClient` e
  `/actuator/health/professorShadowMonolith`. O backend principal permaneceu
  validado por teste, sem mover escrita alguma nem introduzir dependencias
  externas novas.
- A proxima subfase da 51D deve executar esse smoke operacional controlado para
  gerar o primeiro relatorio real ponta a ponta e, se estavel, ampliar a
  cobertura operacional para `listarFuncionariosElegiveis` e `listarPorTurma`,
  ainda sem cutover externo.
- A trigesima segunda subfase da Fase 51D executou esse smoke operacional em
  runtime real dos dois backends, gerando
  `target/professor-shadow-operational-smoke/report.json` com portas livres por
  execucao, usuario seedado, healths em `UP` e contadores reais de
  `GET /api/professores`, `GET /api/professores/{id}`,
  `GET /internal/v1/professores` e `GET /internal/v1/professores/{id}`. Para
  viabilizar a execucao ponta a ponta sem alterar contrato externo, foi preciso
  endurecer apenas o contrato interno: o `academic-professor-service` passou a
  aceitar alias `/internal/**` compativeis com o monolito atual e o
  `school-management-service` passou a repassar `X-Internal-Token`,
  `X-Correlation-Id` e `X-Usuario-Id` ao cliente interno de professor. O fluxo
  fechou sem fallback e sem mover escrita alguma.
- A proxima subfase da 51D deve ampliar o smoke operacional real para
  `listarFuncionariosElegiveis` e `listarPorTurma`, ainda mantendo todas as
  escritas no monolito e sem cutover externo.
- A trigesima-terceira subfase da Fase 51D ampliou esse smoke operacional real
  para cobrir tambem `listarFuncionariosElegiveis` e `listarPorTurma`, ainda
  sem mover escrita alguma nem alterar rotas externas. Para suportar o cenario
  ponta a ponta em runtime real, o perfil `professor-shadow-operational` do
  `school-management-service` passou a seedar as dependencias minimas do
  catalogo (`nivel_ensino`, `turno`, `serie`, `periodo_letivo`, `turma`,
  `disciplina`, `turma_disciplina` e `professor_turma_disciplina`) e um
  funcionario elegivel sem vinculo previo com professor. O script operacional
  `scripts/operational/professor-shadow-operational-smoke.ps1` passou a chamar
  `GET /api/professores/funcionarios-elegiveis`,
  `GET /api/turmas/{turmaId}/professores`,
  `GET /internal/v1/professores/funcionarios-elegiveis` e
  `GET /internal/v1/turmas/{turmaId}/professores`, consolidando no mesmo
  relatorio os contadores dessas rotas. A execucao real voltou a gerar
  `target/professor-shadow-operational-smoke/report.json` com os dois healths
  em `UP`, contadores positivos nas quatro leituras novas e ausencia de
  fallback no backend principal.
- A proxima subfase da 51D deve ampliar o smoke operacional real para
  `listarAlocacoes` (`GET /api/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/professores/{id}/turmas-disciplinas`), fechando o bloco
  read-only do runtime shadow de professores antes de qualquer discussao sobre
  escrita, persistencia propria ou cutover externo.
- A trigesima-quarta subfase da Fase 51D fechou esse bloco read-only do runtime
  shadow de professores em execucao real. O script
  `scripts/operational/professor-shadow-operational-smoke.ps1` passou a cobrir
  tambem `GET /api/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/professores/{id}/turmas-disciplinas`, consolidando no
  relatorio operacional os contadores reais de alocacoes junto das demais
  leituras shadow. A nova execucao voltou a gerar
  `target/professor-shadow-operational-smoke/report.json` com contadores
  positivos em `listar`, `buscarPorId`, `listarAlocacoes`,
  `listarPorTurma` e `listarFuncionariosElegiveis`, healths dos dois lados em
  `UP` e ausencia de fallback no backend principal. Nenhuma escrita foi movida,
  nenhum contrato externo foi alterado e o BFF permaneceu fora deste recorte.
- A proxima subfase da 51D deve sair do bloco puramente read-only e preparar o
  menor passo de escrita shadow de professores com risco controlado: diagnostico
  operacional minimo de `POST /api/professores`, definicao do seed/contrato de
  escrita observavel e prova de health/metricas sem cutover externo nem
  persistencia propria no runtime shadow.
- A trigesima-quinta subfase da Fase 51D executou exatamente esse menor passo
  de escrita shadow com risco controlado em `POST /api/professores`, ainda sem
  BFF, sem persistencia propria e sem cutover externo. O
  `academic-professor-service` passou a expor `POST /internal/v1/professores`
  como proxy do contrato interno `POST /internal/professores` do monolito,
  reaproveitando o mesmo contexto obrigatorio (`Authorization`,
  `X-Correlation-Id`, `X-Usuario-Id`, `X-Escola-Id`) e a mesma protecao por
  token interno. Os health indicators dedicados passaram a incluir a operacao
  `criar` nos contadores e no diagnostico por rota, e o smoke operacional real
  foi expandido para criar um professor via `POST /api/professores`, validar a
  leitura do professor criado pelo runtime shadow e confirmar contadores de
  escrita sem fallback. O resultado foi um `report.json` real com `criar`
  contabilizado nos dois lados, healths em `UP` e nenhum contrato externo novo
  alem do backend/backend shadow.
- A proxima subfase da 51D deve aplicar esse mesmo padrao minimo a
  `POST /api/professores/{id}/turmas-disciplinas`, mantendo o escopo estrito:
  proxy shadow sem persistencia propria, observabilidade dedicada, smoke
  operacional controlado e sem qualquer cutover no BFF.
- A trigesima-sexta subfase da Fase 51D aplicou exatamente esse mesmo padrao
  minimo ao segundo write interno de professores:
  `POST /api/professores/{id}/turmas-disciplinas`. O
  `academic-professor-service` passou a expor
  `POST /internal/v1/professores/{id}/turmas-disciplinas` como proxy do
  contrato interno `POST /internal/professores/{id}/turmas-disciplinas` do
  monolito, ainda sem persistencia propria e sem BFF. Os health indicators
  dedicados dos dois runtimes passaram a incluir a operacao
  `vincularTurmaDisciplina` no diagnostico por rota e nos contadores
  operacionais. O smoke operacional real foi expandido para criar um professor,
  aloca-lo via `POST /api/professores/{id}/turmas-disciplinas` e validar a
  leitura dessa alocacao pelo runtime shadow, fechando tambem esse segundo
  write com healths em `UP` e ausencia de fallback.
- A trigesima-setima subfase da Fase 51D abriu a primeira persistencia propria
  controlada do `academic-professor-service`, ainda sem alterar BFF nem
  cutover externo. O runtime shadow ganhou migrations proprias para
  `professor_shadow`, `professor_alocacao_shadow` e estados de sincronizacao
  por escola, professor e turma, alem de um migrador opt-in com runner
  controlado, dry-run/aplicacao e relatorio JSON reconciliado. Os testes de
  migracao comprovaram repeticao segura, reconciliacao por escola e deteccao de
  divergencia sem mudar o contrato externo de professores.
- A trigesima-oitava subfase da Fase 51D transformou os dois writes shadow de
  professores em automacao controlada de persistencia local. Depois do sucesso
  do proxy para o monolito, o `academic-professor-service` passou a registrar
  localmente a criacao de professor e a alocacao professor-turma-disciplina,
  com validacoes explicitas de divergencia, metricas dedicadas, health
  `professorShadowPersistence` e rollback simples por propriedade. O monolito
  permaneceu como autoridade de escrita; a copia local passou a ser apenas o
  reflexo controlado do resultado retornado.
- A trigesima-nona subfase da Fase 51D aplicou o primeiro cutover controlado de
  leitura sobre essa persistencia propria, ainda restrito ao backend/backend do
  `academic-professor-service`. As rotas `GET /internal/v1/professores`,
  `GET /internal/v1/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/turmas/{turmaId}/professores` passaram a preferir a copia
  local somente quando o respectivo estado de sincronizacao estiver completo;
  caso contrario, o runtime shadow faz fallback para o monolito e registra o
  motivo operacional (`sync_state_incomplete`, `feature_disabled` ou ausencia
  de registro local). Isso manteve o risco baixo e preparou o primeiro recorte
  de leitura controlada sem envolver o BFF.
- A quadragesima subfase da Fase 51D fechou o bloco restante do primeiro
  cutover controlado de leitura com `GET /internal/v1/professores/{id}`. Quando
  a propriedade `professor.shadow.local-persistence.buscar-por-id-cutover-enabled`
  esta ativa, a busca por id passa a responder diretamente da copia local e
  retorna `404 RESOURCE_NOT_FOUND` sem consultar o monolito se o professor nao
  existir localmente. Os testes comprovaram o nao-acesso remoto, a estrategia
  de rollback por propriedade e o diagnostico no actuator
  `professorShadowPersistence`. Com isso, o dominio de professores fechou a
  `51D` com writes shadow observaveis, persistencia propria controlada, leitura
  local incremental e sem qualquer alteracao de rota externa no BFF.
- Encerramento oficial da Fase 51D: o bloco de professores ficou coberto no
  recorte backend/backend atual, sem writes remanescentes fora do padrao
  controlado introduzido no `academic-professor-service`. A proxima frente
  sugerida passa a ser a Fase 52, iniciando pelo diagnostico pontual de
  identidade e tenant antes de qualquer extracao fisica ou cutover externo.
- A primeira subfase da Fase 52 executou esse diagnostico pontual de
  identidade e tenant e fixou o menor passo seguro da macrofase: antes de
  qualquer extracao fisica, a implementacao precisa separar dentro do monolito
  uma fronteira interna minima para sessao/autenticacao por token opaco e para
  resolucao do tenant ativo, preservando o contrato externo
  `GET /api/auth/contexto-atual` usado hoje pelo BFF. O diagnostico tambem
  confirmou que ainda nao existe `usuario_escola` e que o tenant ativo segue
  implicitamente em `usuario.id_escola`, com fallback para a escola padrao.
- A segunda subfase da Fase 52 implementou essa fronteira interna minima ainda
  dentro do `school-management-service`, sem runtime novo, sem migration e sem
  alterar o contrato externo do BFF. Foi criada a porta
  `IdentidadeTenantPort`, com DTOs internos proprios para sessao autenticada e
  contexto autenticado, e a implementacao `IdentidadeTenantService` passou a
  concentrar autenticacao por token opaco, refresh, logout, validacao de access
  token, resolucao de perfis/permissoes e contexto autenticado por escola. O
  `AuthService` foi reduzido a uma fachada de compatibilidade que apenas
  delega para essa nova fronteira e monta os DTOs externos ja existentes.
- A terceira subfase da Fase 52 aplicou o primeiro consumo interno seguro
  dessa nova fronteira no proprio encadeamento de seguranca do monolito.
  `JwtAuthenticationFilter` e `SecurityBeansConfig` deixaram de depender de
  `AuthService` para validacao do bearer token e passaram a consumir
  `IdentidadeTenantPort` diretamente. Para reduzir o acoplamento interno, a
  porta deixou de expor `UsuarioEntity` nesse fluxo e passou a entregar o DTO
  interno `PrincipalAutenticadoResumo`, contendo apenas identidade minima e
  permissoes necessarias para montar o principal autenticado no
  `SecurityContextHolder`.
- A quarta subfase da Fase 52 explicitou a autoridade atual de tenant sem
  introduzir ainda `usuario_escola`. Foi criada a porta interna
  `TenantAtivoPort`, com os DTOs `TenantAtivoResumo` e `OrigemTenantAtivo`,
  tornando declarada a origem da escola ativa em tres cenarios: escola
  persistida na sessao autenticada, escola vinda de `usuario.id_escola` e
  fallback para a escola padrao. `IdentidadeTenantService` passou a consumir
  essa fronteira na criacao de sessao, no refresh e na resolucao de contexto
  autenticado, preservando o contrato externo e preparando o menor ponto de
  evolucao futura para selecao explicita de escola ativa.
- A quinta subfase da Fase 52 iniciou a fundacao persistente de
  `usuario_escola` sem trocar ainda a leitura do tenant ativo. Foi criada uma
  migration aditiva com backfill do vinculo atual a partir de
  `usuario.id_escola`, alem da entidade, repositorio e porta interna
  `UsuarioEscolaPort`. O `UsuarioInteractor` passou a sincronizar esse vinculo
  explicitamente nos writes de usuario, sempre de forma aditiva e sem remover
  historico de escolas ja vinculadas. Com isso, o backend deixa preparado o
  primeiro bloco real para futura selecao explicita de escola ativa, enquanto
  o comportamento externo continua estavel.
- A sexta subfase da Fase 52 usou essa fundacao para criar o primeiro fluxo
  interno autenticado de selecao explicita de escola ativa. Foram adicionados
  os endpoints internos `GET /internal/auth/escolas` e
  `POST /internal/auth/escola-ativa`, apoiados por novas operacoes da porta
  `IdentidadeTenantPort`. A listagem passa a expor as escolas disponiveis na
  sessao a partir de `usuario_escola`, e a troca da escola ativa so e aceita
  quando o usuario possui vinculo explicito com a escola solicitada. Assim,
  `sessao_autenticacao.id_escola` passa a registrar uma escolha validada de
  tenant, ainda sem mudar o contrato externo do login ou exigir acao do BFF.
- A setima subfase da Fase 52 fechou o bloco minimo de compatibilidade do
  login multiescola no backend atual. `POST /api/auth/login` passou a aceitar
  `escolaId` opcional sem romper o payload existente; quando informado, o
  backend valida o vinculo em `usuario_escola` antes de criar a sessao, e
  `sessao_autenticacao.id_escola` ja nasce com a escola escolhida. Quando
  ausente, o comportamento legado continua implicito e retrocompativel. Com
  isso, sessao, tenant ativo, vinculo usuario-escola e selecao de escola ficam
  fechados como fronteiras internas preparatorias para a futura extracao fisica
  de `identity-access-service` e `institutional-tenant-service`.
- A primeira subfase da Fase 53 iniciou o desacoplamento de `people-service`
  pelo menor acoplamento remanescente com seguranca: `ProfessorEntity` deixou
  de manter relacionamento JPA direto com `UsuarioEntity` e passou a tratar
  `id_usuario` apenas como referencia externa UUID, preservando a mesma coluna
  fisica. `ProfessorJpaRepository` e a resolucao de `professorId` em
  `IdentidadeTenantService` foram ajustados para essa referencia simples,
  mantendo o comportamento externo do login e preparando o dominio de pessoas
  para a remocao progressiva de dependencias ORM com seguranca.
- A segunda subfase da Fase 53 criou o primeiro contrato interno explicito do
  futuro `people-service` no cadastro base de pessoas. A porta
  `PessoaCadastroPort` passou a encapsular a criacao/atualizacao de pessoa com
  tipo e endereco, busca por CPF e catalogos de tipos, mantendo
  `PessoaFoundationService` como implementacao local nesta etapa. Com isso,
  `AlunoPersistenceGateway`, `ResponsavelPersistenceGateway` e
  `PessoaCatalogoController` deixaram de depender diretamente da implementacao
  concreta de fundacao, passando a consumir uma fronteira interna preparatoria
  para a extracao fisica do modulo de people.
- A terceira subfase da Fase 53 reaplicou esse mesmo padrao de fronteira
  interna no fluxo de professores, sem abrir novo runtime nem ampliar o BFF. A
  porta `PessoaCadastroPort` passou a expor a busca controlada de pessoa por
  `pessoaId` e `escolaId`, `PessoaFoundationService` implementou essa
  resolucao e `ProfessorService` deixou de depender diretamente de
  `EntityManager` para materializar `PessoaEntity` no write local de professor.
  Com isso, o acoplamento residual entre professor e infraestrutura JPA
  compartilhada foi reduzido sem alterar as rotas externas nem o comportamento
  funcional do monolito.
- A quarta subfase da Fase 53 fechou o recorte remanescente de elegibilidade
  entre RH e professor dentro do backend atual. Foi criada a porta interna
  `ProfessorPessoaPort`, implementada localmente no dominio de professor apenas
  para responder a existencia de professor por `pessoaId` e `escolaId`, e
  `FuncionarioProfessorService` deixou de consultar `ProfessorJpaRepository`
  diretamente para calcular elegibilidade. Com isso, a consulta cruzada de RH
  para professor deixou de depender de repositorio JPA de outro modulo,
  preservando o comportamento funcional do cadastro de professor e da listagem
  de elegiveis.
- A quinta subfase da Fase 53 fechou o bloco interno de professores com dois
  ajustes finais de contrato. Primeiro, `ProfessorAcademicoPort` deixou de
  expor `ProfessorFuncionarioElegivelResponse`, que era DTO de adaptador web, e
  passou a usar um DTO interno proprio para funcionarios elegiveis. Segundo, o
  adaptador interno de professores passou a expor explicitamente
  `GET /internal/professores/funcionarios-elegiveis`, alinhando o endpoint
  real ao cliente interno e ao health operacional ja existente. Com isso, o
  recorte professor/RH/pessoas ficou fechado com fronteiras internas
  explicitas, sem depender de DTO externo no contrato backend/backend.
- A primeira subfase da Fase 54 abriu o bloco de `matricula e documentos` pelo
  menor recorte de menor risco: `transferencia`. Foram criados DTOs internos
  para transferencia e escola de origem, a nova porta
  `TransferenciaAlunoGateway` e sua implementacao local
  `TransferenciaAlunoPersistenceGateway`, encapsulando lookup de aluno,
  persistencia JPA e resolucao dos catalogos SQL. Com isso,
  `TransferenciaAlunoService` deixou de depender diretamente de repositorios e
  `JdbcTemplate`, enquanto `TransferenciaAlunoController` e
  `EscolaOrigemController` passaram a apenas mapear contrato externo para o
  contrato interno, preservando as APIs publicas existentes.
- A segunda subfase da Fase 54 aplicou o mesmo padrao ao write principal de
  `matricula`, sem alterar o contrato REST nem a modelagem fisica da tabela.
  Foi criada a porta interna `AlunoMatriculaPort`, implementada por
  `AlunoMatriculaService`, para encapsular existencia e materializacao escopada
  de aluno por escola. Com isso, `AlunoConsultaPersistenceGateway` deixou de
  consultar `AlunoJpaRepository` diretamente e `MatriculaPersistenceGateway`
  deixou de materializar `AlunoEntity` via
  `EntityManager.getReference(AlunoEntity.class, ...)`, passando a depender de
  uma fronteira interna explicita do dominio de aluno.
- A terceira subfase da Fase 54 reaproveitou essa mesma fronteira interna no
  recorte de `documento`, pelo menor ponto ainda acoplado do fluxo de aluno.
  `DocumentoPersistenceGateway` deixou de consultar `AlunoJpaRepository`
  diretamente para resolver `ALUNO -> pessoaId` e passou a usar
  `AlunoMatriculaPort`, preservando os controllers, casos de uso e storage
  local existentes. Com isso, o desacoplamento backend/backend do bloco
  matricula/documentos avancou sem abrir refatoracao ampla do gateway nem tocar
  no fluxo de responsavel nesta etapa.
- A quarta subfase da Fase 54 fechou o mesmo recorte de `documento` no lado de
  `responsavel`. Foi criada a porta interna `ResponsavelDocumentoPort`,
  implementada por `ResponsavelDocumentoService`, e
  `DocumentoPersistenceGateway` deixou de consultar
  `ResponsavelJpaRepository` diretamente para resolver
  `RESPONSAVEL -> pessoaId`. Com isso, o gateway documental passou a depender
  apenas de fronteiras internas explicitas para os vinculos hoje suportados,
  mantendo os contratos externos inalterados.
- A quinta subfase da Fase 54 iniciou o primeiro recorte minimo de
  `historico`. `HistoricoEscolarServiceImpl` deixou de consultar
  `AlunoJpaRepository` diretamente para validar existencia de aluno por escola e
  para materializar `AlunoEntity` no create/update do historico, passando a
  reutilizar `AlunoMatriculaPort`. Com isso, o primeiro desacoplamento interno
  do modulo de historico foi feito sem ampliar escopo para `boletim`, mapper ou
  refatoracao estrutural maior.
- A sexta subfase da Fase 54 fechou a revisao final do bloco interno e removeu
  um residuo do mesmo padrao ainda em `transferencia`.
  `TransferenciaAlunoPersistenceGateway` deixou de consultar
  `AlunoJpaRepository` diretamente para validar existencia de aluno e passou a
  reutilizar `AlunoMatriculaPort` com escopo de escola. Com isso, o bloco
  minimo de desacoplamento interno de `transferencia`, `matricula`,
  `documento` e `historico` ficou formalmente encerrado no backend atual.
- A primeira subfase da Fase 55 iniciou a macrofase pedagogica pelo menor
  recorte de leitura consolidada fora do proprio modulo: o resumo academico da
  matricula. `MatriculaAcademicoResumoService` deixou de consultar
  `NotaAlunoJpaRepository`, `FrequenciaAlunoJpaRepository` e entidades de
  `avaliacao`/`frequencia` diretamente, passando a reutilizar a nova fronteira
  interna `RendimentoAcademicoPort`, implementada por
  `RendimentoAcademicoService` com DTOs internos proprios. Com isso, o bloco
  pedagogico comeca a ser desacoplado sem alterar o endpoint externo da
  matricula nem abrir ainda o recorte de `boletim`.

## Historico resumido

| Arquivo original | Titulo | Resumo | Resultado ou proximo passo |
| --- | --- | --- | --- |
| `27-fase-7-consolidacao-arquitetural.md` | Fase 7 - Consolidacao arquitetural | Consolidar a estrutura criada nas fases anteriores sem transformar tabelas tecnicas do Flyway em dominio de negocio. A diretriz usada foi manter o backend aderente a modelagem oficial da base v2 e corrigir inconsistencias de pacote quando isso pudesse ser f... |  |
| `28-consolidado-fases-1-a-7-entidades-dominios.md` | Consolidado - Fases 1 a 7 de entidades e dominios | Consolidar o trabalho executado para estruturar, no backend, as entidades e repositorios baseados no modelo enviado e no `scriptdb.sql`. Este documento resume as fases 1 a 7, as decisoes tomadas e o saldo final da cobertura das tabelas. | \| Indicador \| Quantidade \| \| --- \| ---: \| \| Tabelas no modelo enviado \| 77 \| \| Tabelas no `scriptdb.sql` \| 77 \| \| Tabelas transacionais/de negocio estruturadas \| 75 \| \| Tabelas tecnicas nao mapeadas como dominio \| 2 \| \| Repositorios JPA e... |
| `29-fase-9-revisao-duplicidades-responsabilidades.md` | Fase 9 - Revisao de duplicidades e responsabilidades | Revisar os pontos conceituais levantados apos a criacao da base estrutural JPA, separando: - duplicidade real de entidade/tabela; - apenas sobreposicao conceitual entre dominios; - refatoracao recomendada para fase futura; - pontos que nao devem ser alterad... | Nao foi encontrada duplicidade fisica relevante para: - `FuncionarioEntity` - `NotaAlunoEntity` - `FrequenciaAlunoEntity` Essas entidades estao concentradas nos dominios mais adequados: - `FuncionarioEntity` em `rh` - `NotaAlunoEntity` em `avaliacao` - `Fre... |
| `30-fase-10-roteiro-mvp-fluxos-de-negocio.md` | Fase 10 - Roteiro MVP de fluxos de negocio | Definir a sequencia pragmatica para sair da estrutura JPA criada nas fases anteriores e avancar para fluxos funcionais de produto. As fases 1 a 7 estruturaram a persistencia. Esta fase define a ordem recomendada para criar: - services; - DTOs; - controllers... | Antes de iniciar os controllers e services, criar testes JPA focados nos relacionamentos mais importantes. Isso reduz o risco de descobrir problemas de mapeamento apenas quando os fluxos REST forem implementados. |
| `31-fase-11-testes-jpa-relacionamentos-criticos.md` | Fase 11 - Testes JPA de relacionamentos criticos | Adicionar uma primeira camada de testes automatizados para validar relacionamentos JPA criados nas fases estruturais. Esses testes nao implementam regras de negocio nem novos endpoints. Eles verificam se os mapeamentos principais conseguem persistir e consu... | Iniciar a implementacao do primeiro fluxo MVP: catalogo academico. O objetivo da proxima fase deve ser validar e, se necessario, completar services/controllers/DTOs para: - periodo letivo; - serie; - turno; - turma; - disciplina; - turma-disciplina. |
| `32-fase-12-catalogo-academico-mvp.md` | Fase 12 - Catalogo academico MVP | Avancar do mapeamento JPA para o primeiro fluxo MVP funcional: catalogo academico. O criterio desta fase foi permitir criar uma turma completa com disciplinas vinculadas, preservando os endpoints ja existentes de periodo letivo, serie, turno e turma. | O catalogo academico agora cobre o fluxo minimo: 1. Criar periodo letivo. 2. Criar/consultar serie. 3. Criar/consultar turno. 4. Criar turma. 5. Criar disciplina. 6. Vincular disciplina a turma. 7. Listar disciplinas de uma turma. |
| `33-fase-13-aluno-responsavel-ficha.md` | Fase 13 - Aluno e responsavel | Avancar o fluxo MVP de aluno e responsavel, reaproveitando os cadastros e vinculos ja existentes e adicionando uma consulta consolidada da ficha do aluno. | O fluxo aluno + responsavel agora cobre: 1. cadastro de aluno com pessoa; 2. cadastro de responsavel com pessoa; 3. vinculo aluno-responsavel; 4. listagem de responsaveis por aluno; 5. ficha consolidada do aluno. |
| `34-fase-14-matricula-mvp-integracao.md` | Fase 14 - Matricula MVP e integracao | Validar o fluxo MVP de matricula usando a estrutura de dominio ja criada para o modulo `matricula`. Esta fase nao altera a regra de negocio de rematricula, que segue bloqueada no `CriarMatriculaUseCase` ate uma decisao funcional sobre historico escolar/docu... | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 2 testes executados. - 0 falhas. - 0 erros. |
| `35-fase-15-matricula-etapas-documentos-entregues.md` | Fase 15 - Matricula etapas e documentos entregues | Evoluir o fluxo de matricula para operar etapas da matricula e documentos entregues, reaproveitando as entidades transacionais ja mapeadas. | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 3 testes executados. - 0 falhas. - 0 erros. |
| `36-fase-16-matricula-documentos-exigidos.md` | Fase 16 - Matricula documentos exigidos | Expor a leitura de documentos exigidos por matricula, usando a tabela transacional/catalogo `matricula_documento_exigido`. | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 3 testes executados. - 0 falhas. - 0 erros. |
| `37-fase-17-api-admin-documentos-exigidos-matricula.md` | Fase 17 - API admin de documentos exigidos da matricula | Criar API administrativa para configurar documentos exigidos por tipo de matricula. | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 4 testes executados. - 0 falhas. - 0 erros. |
| `38-fase-18-transicao-automatica-status-matricula-documentos.md` | Fase 18 - Transicao automatica de status da matricula por documentos | Adicionar transicao automatica de status da matricula quando todos os documentos obrigatorios configurados para seu tipo forem entregues. | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 4 testes executados. - 0 falhas. - 0 erros. |
| `39-fase-19-regra-funcional-rematricula.md` | Fase 19 - Regra funcional de rematricula | Habilitar o MVP de rematricula (`RENOVACAO`) com regras funcionais minimas e explicitas. | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test ``` Resultado: - 6 testes executados. - 0 falhas. - 0 erros. |
| `40-fase-20-professor-rh-alocacao-turma-disciplina.md` | Fase 20 - Professor, RH e alocacao em turma/disciplina | Avancar o fluxo MVP de professor e RH sem abrir ainda um CRUD completo de funcionario. O recorte desta fase permite: - cadastrar professor a partir de um funcionario existente; - consultar professores cadastrados; - vincular professor a uma turma/disciplina... | Comando focado: ```powershell .\mvnw.cmd -Dtest=ProfessorControllerIntegrationTest test ``` Resultado: - 2 testes executados. - 0 falhas. - 0 erros. |
| `41-fase-21-diario-aula-frequencia.md` | Fase 21 - Diario de aula e frequencia | Iniciar o fluxo MVP de execucao pedagogica usando a alocacao professor/turma/disciplina criada na fase anterior. O recorte desta fase cobre: - criacao de aula a partir de uma alocacao de professor; - consulta de aulas por alocacao ou turma; - registro da fr... | Comando focado: ```powershell .\mvnw.cmd -Dtest=AulaControllerIntegrationTest test ``` Resultado: - 2 testes executados. - 0 falhas. - 0 erros. |
| `42-fase-22-avaliacoes-notas.md` | Fase 22 - Avaliacoes e lancamento de notas | Completar o recorte MVP de execucao pedagogica iniciado na fase 21, adicionando avaliacoes e notas por aluno. O recorte desta fase cobre: - criacao de avaliacao a partir de uma alocacao professor/turma/disciplina; - consulta de avaliacoes por alocacao ou tu... | Comando focado: ```powershell .\mvnw.cmd -Dtest=AvaliacaoControllerIntegrationTest test ``` Resultado: - 2 testes executados. - 0 falhas. - 0 erros. |
| `43-fase-23-consulta-academica-matricula.md` | Fase 23 - Consulta academica consolidada por matricula | Criar uma consulta consolidada por matricula antes de iniciar boletim. O recorte desta fase reune em uma unica resposta: - dados principais da matricula; - dados do aluno; - turma e periodo letivo; - frequencias registradas; - notas lancadas; - indicadores ... | Comando focado: ```powershell .\mvnw.cmd -Dtest=MatriculaAcademicoControllerIntegrationTest test ``` Resultado: - 1 teste executado. - 0 falhas. - 0 erros. |
| `44-fase-24-boletim-consultivo-matricula.md` | Fase 24 - Boletim consultivo por matricula | Criar o primeiro endpoint de boletim usando as notas e frequencias ja registradas nas fases anteriores. Esta fase entrega um boletim consultivo, gerado em tempo real, sem persistir nas tabelas `boletim` e `boletim_item`. | Comando focado: ```powershell .\mvnw.cmd -Dtest=BoletimControllerIntegrationTest test ``` Resultado: - 1 teste executado. - 0 falhas. - 0 erros. |
| `45-fase-25-fechamento-persistido-boletim.md` | Fase 25 - Fechamento persistido do boletim | Transformar o boletim consultivo da matricula em um fechamento oficial persistido, mantendo o calculo em tempo real disponivel para consulta academica. |  |
| `46-fase-26-geracao-historico-por-boletim-fechado.md` | Fase 26 - Geracao de historico por boletim fechado | Permitir que o historico escolar interno seja gerado a partir de um boletim oficialmente fechado, evitando digitacao manual de componentes curriculares quando ja existem notas e frequencias consolidadas. |  |
| `47-fase-27-conclusao-academica-matricula.md` | Fase 27 - Conclusao academica da matricula | Conectar o boletim fechado ao desfecho academico da matricula, permitindo que a matricula seja concluida somente quando o resultado oficial do boletim permitir. |  |
| `48-fase-28-rematricula-operacional.md` | Fase 28 - Rematricula operacional | Expor um fluxo operacional de rematricula a partir de uma matricula anterior concluida, reaproveitando as regras funcionais ja existentes para renovacao. |  |
| `49-fase-29-elegibilidade-rematricula.md` | Fase 29 - Elegibilidade de rematricula | Permitir que a secretaria consulte se uma matricula concluida pode gerar rematricula antes de executar a criacao da nova matricula. |  |
| `50-fase-30-dashboard-academico-operacional.md` | Fase 30 - Dashboard academico-operacional | Criar um primeiro endpoint de dashboard em tempo real para apoiar secretaria e gestao na leitura operacional do ciclo academico ja implementado. |  |
| `51-fase-31-dashboard-secretaria.md` | Fase 31 - Dashboard da secretaria | Separar a primeira visao operacional de dashboard por publico, iniciando pela secretaria. |  |
| `52-fase-32-dashboard-professor.md` | Fase 32 - Dashboard do professor | Separar a visao de dashboard do professor por docente, usando os fluxos ja existentes de alocacao, aula, frequencia, avaliacao, notas e planejamento. |  |
| `53-fase-33-dashboard-diretor.md` | Fase 33 - Dashboard do diretor | Separar a visao executiva de dashboard para direcao, consolidando indicadores academicos, administrativos e operacionais ja estabilizados nas fases anteriores. |  |
| `54-fase-34-configuracao-dashboard.md` | Fase 34 - Configuracao de dashboards | Criar API administrativa para configurar publicos, dashboards e widgets, preparando a base para configuracao por usuario e historizacao de indicadores. |  |
| `55-fase-35-configuracao-dashboard-usuario.md` | Fase 35 - Configuracao de dashboard por usuario | Permitir que cada usuario autenticado personalize widgets dos dashboards ja configurados por publico. |  |
| `56-fase-36-snapshots-dashboard.md` | Fase 36 - Snapshots de indicadores do dashboard | Persistir historico de indicadores por publico de dashboard, permitindo acompanhar valores por data de referencia. |  |
| `57-fase-37-geracao-snapshots-dashboard.md` | Fase 37 - Geracao automatica de snapshots do dashboard | Gerar snapshots de indicadores a partir dos dashboards em tempo real ja implementados, reduzindo a necessidade de carga manual por API. |  |
| `58-fase-38-geracao-snapshots-dashboard-professor.md` | 58-fase-38-geracao-snapshots-dashboard-professor | Esta fase adiciona a geracao automatica de snapshots para o dashboard do professor por docente. - `POST /api/dashboard/snapshots/geracoes/professores/{professorId}` |  |
| `59-fase-39-historico-comparativo-dashboard.md` | 59-fase-39-historico-comparativo-dashboard | Esta fase adiciona consulta historica dos snapshots ja persistidos para transformar os indicadores em series temporais. - `GET /api/dashboard/snapshots/historico/publicos/{publicoCodigo}` |  |
| `60-fase-40-agendamento-snapshots-dashboard.md` | 60-fase-40-agendamento-snapshots-dashboard | Esta fase adiciona uma rotina agendada para gerar snapshots de dashboard sem chamada manual da API. Componente: |  |
| `61-fase-41-alertas-dashboard.md` | 61-fase-41-alertas-dashboard | Esta fase adiciona uma API de alertas calculados a partir dos dashboards operacionais ja existentes. - `GET /api/dashboard/alertas?publicoCodigo={publicoCodigo}` |  |
| `62-fase-42-dashboard-frontend-agregado.md` | 62-fase-42-dashboard-frontend-agregado | Esta fase adiciona um endpoint pensado para montagem inicial da tela de dashboard no frontend, reduzindo a necessidade de varias chamadas separadas. - `GET /api/dashboard/frontend?publicoCodigo={publicoCodigo}` |  |
| `63-pre-fase-43-refatoracao-frontend-dominios.md` | 63-pre-fase-43-refatoracao-frontend-dominios | Esta fase reorganiza a estrutura do Angular host para aproximar os dominios de negocio da nomenclatura usada no backend, antes de iniciar a implementacao visual dos dashboards. - `school-management-web/host/src/app/academic` para `school-management-web/host... |  |
| `64-fase-43a-estabilizacao-native-federation.md` | 64-fase-43a-estabilizacao-native-federation | Esta fase valida a execucao local do frontend federado antes de iniciar a migracao das funcionalidades escolares do host para o microfrontend. O host deve evoluir para uma casca de seguranca e administracao tecnica: |  |
| `65-fase-43b-contrato-shell-microfrontend.md` | 65-fase-43b-contrato-shell-microfrontend | Esta fase cria um contrato explicito para o microfrontend consumir contexto de seguranca publicado pelo host, sem duplicar autenticacao. Permitir que funcionalidades escolares migradas para o microfrontend consumam: |  |
| `66-fase-43c-migracao-catalogo-microfrontend.md` | 66-fase-43c-migracao-catalogo-microfrontend | Esta fase migra as telas de catalogo escolar para o microfrontend, mantendo o host como shell de seguranca e roteador federado. Foram movidas do host para o microfrontend as telas: |  |
| `67-fase-43d-migracao-responsavel-microfrontend.md` | 67-fase-43d-migracao-responsavel-microfrontend | Esta fase migra as telas de responsavel para o microfrontend, mantendo o host como shell de seguranca e roteador federado. Foram movidas do host para o microfrontend as telas: |  |
| `68-fase-43e-migracao-aluno-microfrontend.md` | 68-fase-43e-migracao-aluno-microfrontend | Esta fase migra as telas de aluno para o microfrontend, mantendo o host como shell de seguranca e roteador federado. Foram movidas do host para o microfrontend as telas: |  |
| `69-fase-43f-migracao-matricula-microfrontend.md` | Fase 43F - Migracao de matricula para o microfrontend | Migrar a funcionalidade de matricula do host para o microfrontend, preservando a rota publica `/enrollment` e mantendo o host como shell de seguranca, menu e administracao tecnica. | Migrar a tela restante de disciplinas/historico academico ainda residente no host ou fazer uma fase curta de limpeza das dependencias escolares que ficaram no host apos as migracoes de catalogo, responsavel, aluno e matricula. |
| `70-fase-43g-migracao-historico-disciplinas-limpeza-host.md` | Fase 43G - Migracao de historico/disciplinas e limpeza do host | Remover do host a ultima tela escolar local ainda ativa, `academic/disciplines`, e deixar o host mais proximo do papel definido para a arquitetura federada: shell, seguranca, menu e administracao tecnica. | Auditar o host para separar o que ainda e infraestrutura de shell do que ainda pode ser movido ou limpo, antes de iniciar novas telas de negocio ou dashboard no microfrontend. |
| `71-fase-43h-auditoria-limpeza-shell-host.md` | Fase 43H - Auditoria e limpeza do shell host | Auditar o host apos a migracao das telas escolares para o microfrontend e reduzir residuos locais sem alterar rotas publicas, contrato de shell ou configuracoes de build. | Definir o contrato de menu/rotas do shell em uma estrutura declarativa, para reduzir duplicacao entre menu, rotas estaticas federadas e documentacao. |
| `72-fase-43i-menu-rotas-declarativas-remocao-home-mfe.md` | Fase 43I - Menu/rotas declarativas e remocao da home marcador do microfrontend | Reduzir duplicacao entre menu e rotas federadas do host, e remover a tela antiga `pages/home` do microfrontend, que existia apenas para indicar que o remoto estava ativo. | Revisar o `AplicativosService` dinamico para decidir se ele continua no MVP ou se sera substituido integralmente pelo contrato declarativo do shell. |
| `73-fase-43j-remocao-aplicativos-dinamicos-host.md` | Fase 43J - Remocao do carregamento dinamico antigo de aplicativos | Remover do host o mecanismo antigo de aplicativos dinamicos, que ficou sem uso apos a centralizacao das rotas e menus no contrato declarativo do shell. | Auditar a documentacao e os READMEs para fechar a etapa de arquitetura federada antes de voltar para funcionalidades novas de negocio ou dashboard. |
| `74-fase-43k-consolidacao-documentacao-arquitetura-federada.md` | Fase 43K - Consolidacao da documentacao da arquitetura federada | Fechar a etapa de refatoracao federada do frontend com documentacao sincronizada ao estado real do repositorio. | Retomar a evolucao funcional pelo microfrontend. A proxima frente natural e dashboard operacional/academico, agora consumindo o backend e o contrato de shell estabilizado. |
| `75-fase-44-planejamento-epico-pedagogico-frontend.md` | Fase 44 - Planejamento do epico pedagogico no microfrontend | Retomar a evolucao funcional apos a consolidacao do dashboard/Home e organizar as proximas entregas em fases pequenas, iniciando pelo frontend dos fluxos pedagogicos que ja possuem suporte no backend. |  |
| `76-fase-44f-dashboard-professor-vinculo-real.md` | Fase 44F - Dashboard do professor com vinculo real | Tornar a Home do professor funcional quando o usuario autenticado estiver vinculado a um cadastro de professor. | Avaliar personalizacao de dashboards por usuario ou telas administrativas de configuracao de dashboards. |
| `77-fase-45a-frontend-admin-configuracao-dashboards.md` | Fase 45A - Frontend administrativo de configuracao de dashboards | Criar a tela administrativa para o ADMIN configurar publicos, dashboards e widgets ja suportados pela API da Fase 34. | Avancar para a personalizacao de dashboards por usuario, permitindo que cada usuario configure visibilidade, ordem e preferencias dos widgets do proprio dashboard. |
| `78-fase-45b-personalizacao-dashboard-usuario-frontend.md` | Fase 45B - Personalizacao de dashboard por usuario | Permitir que cada usuario ajuste a exibicao dos widgets do dashboard operacional sem alterar a configuracao global criada pela administracao. | Fase 45C: aplicar as preferencias salvas diretamente na renderizacao visual dos widgets, alinhando os codigos cadastrados na configuracao administrativa com os blocos reais exibidos na Home por perfil. |
| `79-fase-45c-aplicacao-preferencias-dashboard-home.md` | Fase 45C - Aplicacao das preferencias na Home | Aplicar as preferencias salvas pelo usuario diretamente nos blocos visuais da Home operacional. | Fase 45D: padronizar e documentar uma lista oficial de codigos de widgets para os dashboards padrao por perfil, reduzindo ambiguidades no cadastro administrativo. |
| `80-fase-45d-catalogo-oficial-widgets-dashboard.md` | Fase 45D - Catalogo oficial de widgets do dashboard | Padronizar os codigos de widgets que controlam a Home operacional, reduzindo ambiguidade entre a configuracao administrativa e a renderizacao personalizada por usuario. | Fase 45E: criar um facilitador para provisionar dashboards padrao por perfil usando o catalogo oficial, sem exigir que o ADMIN cadastre todos os widgets manualmente. |
| `81-fase-45e-provisionamento-widgets-padrao-dashboard.md` | Fase 45E - Provisionamento de widgets padrao | Facilitar a configuracao inicial de dashboards por perfil, permitindo que o ADMIN crie os widgets oficiais faltantes sem cadastrar cada item manualmente. | Fase 45F: melhorar a experiencia de revisao dos widgets provisionados, com indicacao visual de quais widgets oficiais ja existem e quais ainda faltam antes de executar o provisionamento. |
| `82-fase-45f-revisao-widgets-oficiais-dashboard.md` | Fase 45F - Revisao de widgets oficiais do dashboard | Melhorar a experiencia do ADMIN antes de provisionar widgets padrao, mostrando quais widgets oficiais ja existem e quais ainda faltam no dashboard selecionado. | Fase 45G: permitir que o ADMIN aplique atualizacao controlada de metadados dos widgets oficiais existentes, sem sobrescrever customizacoes sem confirmacao. |
| `83-fase-45g-atualizacao-metadados-widgets-oficiais.md` | Fase 45G - Atualizacao de metadados de widgets oficiais | Permitir que o ADMIN alinhe widgets oficiais ja existentes ao catalogo atual, sem recriar widgets e sem sobrescrever status operacional sem confirmacao. | Fase 45H: revisar a experiencia completa de configuracao de dashboards no navegador e ajustar textos, compactacao e fluxo conforme uso real do ADMIN. |
| `84-fase-45h-revisao-experiencia-configuracao-dashboard.md` | Fase 45H - Revisao da experiencia de configuracao de dashboards | Melhorar a leitura operacional da tela administrativa de configuracao de dashboards, mantendo o foco no uso do ADMIN para revisar publicos, dashboards e widgets oficiais. | Fase 45I: iniciar a administracao de snapshots de dashboard no microfrontend, permitindo ao ADMIN consultar snapshots por publico e data antes de avancar para edicoes manuais. |
| `85-fase-45i-administracao-snapshots-dashboard-frontend.md` | Fase 45I - Administracao de snapshots de dashboard no frontend | Iniciar a administracao de snapshots de dashboard no microfrontend, permitindo ao ADMIN consultar indicadores historizados por publico e data de referencia. | Fase 45J: adicionar a acao administrativa para gerar snapshots sob demanda por publico e data, reaproveitando os endpoints de geracao ja existentes. |
| `86-fase-45j-geracao-manual-snapshots-dashboard.md` | Fase 45J - Geracao manual de snapshots de dashboard | Permitir que o ADMIN gere snapshots de dashboard sob demanda por publico e data de referencia, reaproveitando os endpoints de geracao ja existentes no backend. | Fase 45K: adicionar geracao de snapshots do professor, com selecao de professor e data de referencia, usando o endpoint dedicado por `professorId`. |
| `87-fase-45k-geracao-snapshots-professor.md` | Fase 45K - Geracao de snapshots do professor | Permitir que o ADMIN gere snapshots do dashboard de professor selecionando um professor e uma data de referencia. | Fase 45L: adicionar visualizacao historica comparativa dos snapshots no microfrontend, consumindo `GET /api/dashboard/snapshots/historico/publicos/{publicoCodigo}`. |
| `88-fase-45l-historico-comparativo-snapshots.md` | Fase 45L - Historico comparativo de snapshots no frontend | Adicionar visualizacao historica comparativa dos snapshots de dashboard no microfrontend, permitindo ao ADMIN analisar valor atual, valor anterior e variacao percentual dos indicadores. | Fase 45M: revisar no navegador a experiencia completa da tela de snapshots e ajustar compactacao, textos e fluxo real de uso do ADMIN. |
| `89-ajuste-dashboard-admin-e-massa-completa-testes.md` | Fase 45M - Ajuste do dashboard ADMIN e massa completa de testes | Corrigir a rolagem vertical do dashboard/Home do ADMIN em 1920x1080 e preparar uma massa local completa para validar os fluxos atualmente disponiveis no sistema. | Fase 45N: revisar no navegador os fluxos principais usando a massa aplicada, comecando por matriculas, professores, aulas, avaliacoes e snapshots. |
| `90-fase-45n-correcao-combos-frontend.md` | Fase 45N - Correcao de combos bloqueados no frontend | Corrigir combos que ficavam bloqueados ou sem comportamento correto no microfrontend, principalmente em aluno, documentos, transferencia, responsaveis e matricula. | Continuar a revisao funcional da Fase 45N usando a massa de testes, seguindo para o fluxo de cadastro/edicao/exclusao de aluno e responsavel apos confirmar que os combos corrigidos estao adequados visualmente. |
| `91-fase-45n-ajuste-widgets-config-dashboard.md` | Fase 45N - Ajuste visual de widgets e titulos de dashboard | Corrigir a leitura dos widgets na tela `Configuracao de dashboards` e trocar a terminologia visivel de Home para Dashboard nas telas operacionais. | Continuar a revisao funcional da Fase 45N com a massa de testes, retomando os fluxos de cadastro/edicao/exclusao de aluno e responsavel. |
| `92-fase-45n-validacao-aluno-responsavel.md` | Fase 45N - Validacao de aluno e responsavel | Validar os fluxos principais de cadastro, edicao e exclusao de aluno e responsavel usando a massa de testes local. | Continuar a Fase 45N validando matriculas com a massa de testes: nova matricula, filtros, troca de status e exclusao de matricula. |
| `93-fase-45n-validacao-matriculas.md` | Fase 45N - Validacao de matriculas | Validar o fluxo operacional de matriculas no microfrontend, usando a massa de testes local e corrigindo somente problemas encontrados durante a validacao. | Continuar a Fase 45N validando documentos e transferencias do aluno com a massa de testes: cadastro, edicao, combos, filtros, exclusao e mensagens de erro. |
| `94-fase-45n-validacao-documentos-transferencias-aluno.md` | Fase 45N - Validacao de documentos e transferencias do aluno | Validar os fluxos de documentos e transferencias dentro da edicao de aluno, usando a massa de testes local e corrigindo somente problemas encontrados na interface. | Continuar a Fase 45N validando historico escolar do aluno: componentes curriculares, disciplina cadastrada, cadastro de historico, listagem, exclusao e ausencia de identificadores tecnicos na interface. |
| `95-fase-45n-validacao-historico-escolar-aluno.md` | Fase 45N - Validacao de historico escolar do aluno | Validar o fluxo de historico escolar dentro da edicao de aluno, cobrindo componentes curriculares, disciplina cadastrada, cadastro, listagem, exclusao e ausencia de identificadores tecnicos na interface. | Considerar a etapa de validacoes funcionais basicas concluida. A proxima fase recomendada e revisao final de PR/MVP: conferir `git status`, revisar diff, executar uma navegacao exploratoria curta nos fluxos principais e abrir PR de `develop` para `master` q... |
| `97-fase-46a-backend-planejamento-bimestral.md` | Fase 46A - Backend de planejamento bimestral | Criar a API funcional de planejamento bimestral, mantendo o backend monolitico atual e sem iniciar BFF, IA real, microservicos ou revisao ampla de permissoes. | Fase 46B - Frontend de planejamento bimestral. |
| `98-fase-46b-frontend-planejamento-bimestral.md` | Fase 46B - Frontend de planejamento bimestral | Criar a tela operacional de planejamento bimestral no microfrontend academico atual, consumindo a API entregue na Fase 46A. | Fase 46C - Backend de IA e conteudo pedagogico. |
| `99-fase-46c-backend-ia-conteudo-pedagogico.md` | Fase 46C - Backend de IA e conteudo pedagogico | Implementar o backend inicial para apoio pedagogico com IA no planejamento bimestral, sem integrar provedor externo neste momento. Esta fase segue o roadmap pos-MVP e mantem o escopo restrito ao backend monolitico atual, preparando pontos de extensao para e... |  |
| `100-fase-46d-frontend-ia-biblioteca-pedagogica.md` | Fase 46D - Frontend de IA e biblioteca pedagogica | Expor no frontend o fluxo inicial de IA pedagogica implementado na Fase 46C, ainda em modo simulado/local, e permitir o reaproveitamento de conteudos aprovados da biblioteca pedagogica. |  |
| `100-fase-47d-massa-teste-planejamento-ia.md` | Fase 47D - Massa de teste de planejamento com IA | Gerar uma massa local e controlada para validar os fluxos entregues nas fases de planejamento bimestral, conteudos de IA e biblioteca pedagogica, antes de seguir para novas fases funcionais. |  |
| `101-fase-46e-polimento-contratos-integracao-planejamento-ia.md` | Fase 46E - Polimento funcional e contratos de integracao do planejamento com IA | Estabilizar o fluxo funcional entre planejamento bimestral, conteudo gerado por IA, versoes aprovadas e biblioteca pedagogica, deixando explicitos os contratos para uma futura integracao real com provedor de IA. |  |
| `102-fase-47a-diagnostico-multiescola.md` | Fase 47A - Diagnostico multi-escola | Mapear o impacto de multi-escola no modelo atual antes de criar migrations, filtros ou contexto de tenant. Esta fase e apenas diagnostica. Nenhuma alteracao de schema, backend ou frontend foi aplicada. |  |
| `103-fase-47b-modelo-base-escola-tenant.md` | Fase 47B - Modelo base de escola/tenant | Introduzir o modelo minimo de escola como tenant funcional sem aplicar ainda isolamento completo nas consultas dos dominios escolares. |  |
| `104-fase-47c-escopo-escola-catalogos-academicos.md` | Fase 47C - Escopo por escola em catalogos academicos | Aplicar a primeira subfase de escopo por escola nos catalogos academicos, sem isolar todos os dominios do sistema de uma vez. |  |
| `105-fase-47c-escopo-escola-pessoas-papeis.md` | Fase 47C - Escopo por escola em pessoas e papeis | Aplicar a segunda subfase de escopo por escola no nucleo de pessoas e papeis, sem alterar fluxos de UI ou iniciar isolamento completo de todos os dominios. | Fase 47C - proxima subfase de escopo por escola em matriculas e documentos: - matriculas; - vinculos aluno-responsavel; - documentos ligados a pessoas/alunos; - pontos de consulta que ainda usam aluno ou responsavel sem filtro por escola. |
| `106-fase-47c-escopo-escola-matriculas-documentos.md` | Fase 47C - Escopo por escola em matriculas e documentos | Aplicar a terceira subfase de escopo por escola em matriculas, documentos e vinculos aluno-responsavel, sem alterar fluxo de UI ou separar servicos. | Fase 47C - proxima subfase de escopo por escola em aulas, frequencias e avaliacoes: - aulas; - frequencias; - avaliacoes; - notas; - consultas que partem de professor, turma, aluno ou matricula nesses dominios. |
| `107-fase-47c-escopo-escola-aulas-frequencias-avaliacoes.md` | Fase 47C - Escopo por escola em aulas, frequencias e avaliacoes | Aplicar a quarta subfase de escopo por escola em aulas, frequencias, avaliacoes e notas, sem alterar fluxo de UI ou separar servicos. | Fase 47C - proxima subfase de escopo por escola em historico e boletins: - historico escolar; - boletins; - consultas academicas que consolidam dados de matricula, avaliacao e frequencia. |
| `108-fase-47c-escopo-escola-historico-boletins.md` | Fase 47C - Escopo por escola em historico e boletins | Aplicar a quinta subfase de escopo por escola em historico escolar, boletins e consultas academicas consolidadas, sem alterar fluxo de UI ou separar servicos. | Fase 47C - proxima subfase de escopo por escola em planejamento e IA: - planejamentos bimestrais; - interacoes e conteudos gerados por IA; - biblioteca pedagogica. |
| `109-fase-47c-escopo-escola-planejamento-ia.md` | Fase 47C - Escopo por escola em planejamento e IA | Aplicar a sexta subfase de escopo por escola em planejamento bimestral, interacoes de IA, conteudos gerados e biblioteca pedagogica, sem alterar fluxo de UI ou separar servicos. | Fase 47C - proxima subfase de escopo por escola em dashboards: - dashboards por perfil; - snapshots e historicos de indicadores; - configuracoes de dashboards e widgets. |
| `110-fase-47c-escopo-escola-dashboards.md` | Fase 47C - Escopo por escola em dashboards | Aplicar a setima subfase de escopo por escola nos dashboards operacionais, snapshots e historicos de indicadores, sem alterar frontend, BFF ou servicos externos. | Fase 47D - consolidacao multi-escola basica: - revisar contratos que agora retornam `escolaId`/`escolaNome`; - identificar lacunas restantes de escopo por escola antes de iniciar BFF; - manter revisao pontual, sem refatoracao ampla. |
| `111-fase-47d-consolidacao-contratos-multiescola.md` | Fase 47D - Consolidacao de contratos multi-escola | Consolidar contratos que ja operam com escopo por escola, deixando explicito qual escola foi usada nas respostas de dashboards operacionais antes de iniciar BFF ou separacao de servicos. | Fase 48A - desenho dos BFFs: - definir BFFs por experiencia de uso; - listar contratos agregados esperados para admin, secretaria, professor e diretor; - ainda sem extrair servicos ou criar microfrontends separados. |
| `112-fase-48a-desenho-bffs.md` | Fase 48A - Desenho dos BFFs | Definir o desenho inicial dos BFFs da evolucao pos-MVP a partir das jornadas reais do sistema atual, mantendo o monolito como fonte de verdade nesta fase. Esta fase e documental e contratual. Ela nao cria aplicacoes BFF, nao extrai servicos, nao altera o ba... | Fase 48B - Fronteiras iniciais de servicos e contratos internos. Objetivo da proxima fase: - Desenhar os servicos de dominio que ficariam atras dos BFFs no futuro. - Separar candidatos a servico por responsabilidade: identidade/escola, secretaria, academico... |
| `113-fase-48b-fronteiras-servicos-contratos-internos.md` | Fase 48B - Fronteiras iniciais de servicos e contratos internos | Definir fronteiras iniciais de servicos de dominio e contratos internos antes de qualquer extracao de codigo do monolito. Esta fase e documental. Ela nao cria microservicos, nao cria BFFs, nao altera controllers, nao altera banco e nao introduz Kafka, Mongo... | Fase 48C - contratos internos de contexto escolar e catalogos academicos no monolito. Objetivo da proxima fase: - Criar ou consolidar portas internas pequenas para contexto escolar e catalogos academicos. - Manter tudo no `school-management-service`. - Nao ... |
| `114-fase-48c-contratos-internos-contexto-catalogos.md` | Fase 48C - Contratos internos de contexto escolar e catalogos academicos | Criar contratos internos pequenos para contexto escolar e catalogos academicos dentro do monolito, preparando consumo futuro por BFFs e servicos sem extrair codigo para outro runtime. | Fase 48D - uso pontual dos contratos internos em um fluxo de baixo risco. Objetivo sugerido: - Escolher um ponto de baixo risco, preferencialmente dashboard ou planejamento, para consumir `CatalogoAcademicoPort` ou `EstruturaTurmaPort`. - Manter o uso dentr... |
| `115-fase-48d-uso-pontual-contratos-internos-dashboard.md` | Fase 48D - Uso pontual dos contratos internos no dashboard academico | Usar os contratos internos criados na Fase 48C em um fluxo de baixo risco, mantendo tudo dentro do monolito e sem criar BFF real, microservico ou novo componente runtime. | Fase 48E - segundo uso pontual dos contratos internos em planejamento ou diario pedagogico. Objetivo sugerido: - Usar `EstruturaTurmaPort` em uma validacao pequena de planejamento, aula ou avaliacao. - Manter a alteracao dentro do monolito. - Nao criar BFF ... |
| `116-fase-48e-uso-pontual-contratos-internos-planejamento.md` | Fase 48E - Uso pontual dos contratos internos no planejamento | Usar `EstruturaTurmaPort` em uma validacao pequena do fluxo de planejamento bimestral, mantendo a implementacao dentro do monolito e sem criar BFF real, microservico ou novo componente runtime. | Fase 48F - consolidacao documental dos contratos internos usados. Objetivo sugerido: - Consolidar quais contratos internos ja existem e onde sao usados. - Identificar o proximo candidato seguro de uso interno. - Ainda nao criar BFF real ou microservico. |
| `117-fase-48f-consolidacao-contratos-internos-usados.md` | Fase 48F - Consolidacao dos contratos internos usados | Consolidar o estado atual dos contratos internos criados nas Fases 48C, 48D e 48E, deixando claro onde eles ja sao usados, quais limites ainda existem e quando sera necessario criar BFFs, servicos separados ou novos componentes runtime. Esta fase e document... | A Fase 48I consolidou os usos pontuais de `EstruturaTurmaPort` em planejamento bimestral, diario de aula e avaliacoes. A porta continua dentro do monolito e ainda nao exige BFF, microservico ou novo componente runtime. |
| `118-fase-48g-uso-pontual-estrutura-turma-diario-aula.md` | Fase 48G - Uso pontual de EstruturaTurmaPort no diario de aula | Aplicar o uso pontual de `EstruturaTurmaPort` no fluxo de diario de aula, mantendo a estrategia incremental definida na Fase 48F. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP. | O diario de aula passa a reutilizar a fronteira interna de estrutura academica em um ponto transacional de baixo risco, alinhando o fluxo com o mesmo criterio adotado anteriormente no planejamento bimestral. |
| `119-fase-48h-uso-pontual-estrutura-turma-avaliacoes.md` | Fase 48H - Uso pontual de EstruturaTurmaPort em avaliacoes | Aplicar o uso pontual de `EstruturaTurmaPort` no fluxo de avaliacoes, mantendo a evolucao incremental das fronteiras internas iniciada nas Fases 48E e 48G. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP. | Avaliacoes passam a reutilizar a fronteira interna de estrutura academica em um ponto transacional de baixo risco, mantendo consistencia com o criterio ja aplicado no planejamento bimestral e no diario de aula. |
| `120-fase-48i-consolidacao-usos-pontuais-estrutura-turma.md` | Fase 48I - Consolidacao dos usos pontuais de EstruturaTurmaPort | Consolidar o estado atual dos usos pontuais de `EstruturaTurmaPort`, apos sua aplicacao em planejamento bimestral, diario de aula e avaliacoes. Esta fase e de consolidacao documental e verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco... | Os usos pontuais de `EstruturaTurmaPort` estao documentados e alinhados entre os tres consumidores atuais. A fronteira interna esta madura o bastante para continuar sendo usada em pontos pequenos, mas ainda nao justifica extracao de componente ou criacao de... |
| `121-fase-48j-uso-pontual-escola-contexto-dashboard-secretaria.md` | Fase 48J - Uso pontual de EscolaContextoPort no dashboard secretaria | Aplicar `EscolaContextoPort` em um fluxo majoritariamente de leitura, usando o dashboard secretaria como ponto seguro de evolucao incremental das fronteiras internas. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou n... | O dashboard secretaria passa a consumir a fronteira interna de contexto escolar, alinhando-se ao padrao ja adotado pelo dashboard academico, sem alterar comportamento externo. |
| `122-fase-48k-consolidacao-usos-escola-contexto-dashboards.md` | Fase 48K - Consolidacao dos usos de EscolaContextoPort em dashboards | Consolidar o estado atual dos usos de `EscolaContextoPort` em dashboards, apos sua aplicacao no dashboard academico e no dashboard secretaria. Esta fase e de consolidacao documental e verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco ... | Os usos de `EscolaContextoPort` em dashboards estao documentados e alinhados. A fronteira interna de contexto escolar esta pronta para novos usos pontuais em dashboards de leitura, mas ainda nao justifica extracao de componente ou criacao de BFF. |
| `123-fase-48l-uso-pontual-escola-contexto-dashboard-diretor.md` | Fase 48L - Uso pontual de EscolaContextoPort no dashboard diretor | Aplicar `EscolaContextoPort` em `DashboardDiretorService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em fluxos de leitura e agregacao. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou... | O dashboard diretor passa a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico e secretaria sem alterar comportamento externo. |
| `124-fase-48m-consolidacao-dashboards-escola-contexto.md` | Fase 48M - Consolidacao dos dashboards com EscolaContextoPort | Consolidar o estado atual dos dashboards que ja usam `EscolaContextoPort`, apos as aplicacoes pontuais nos dashboards academico, secretaria e diretor. Esta fase e documental e de verificacao de consistencia. Ela nao cria BFF, microservico, fila, banco adici... | Os usos de `EscolaContextoPort` nos dashboards academico, secretaria e diretor estao consolidados e documentados. A fronteira interna de contexto escolar permanece pronta para novos usos pontuais sem alterar o desenho runtime atual. |
| `125-fase-48n-uso-pontual-escola-contexto-dashboard-professor.md` | Fase 48N - Uso pontual de EscolaContextoPort no dashboard professor | Aplicar `EscolaContextoPort` em `DashboardProfessorService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de leitura filtrado por professor. Esta fase nao cria BFF, microservico, fila, banco adicional, novo compone... | O dashboard professor passa a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico, secretaria e diretor sem alterar comportamento externo. |
| `126-fase-48o-uso-pontual-escola-contexto-snapshots-dashboard.md` | Fase 48O - Uso pontual de EscolaContextoPort nos snapshots de dashboard | Aplicar `EscolaContextoPort` em `DashboardIndicadorSnapshotService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em listagem, historico e persistencia de snapshots de indicadores. Esta fase nao cria BFF, microservico, fila, b... | Os snapshots de indicadores passam a consumir a fronteira interna de contexto escolar, alinhando-se aos dashboards academico, secretaria, diretor e professor sem alterar comportamento externo. |
| `127-fase-48p-consolidacao-escola-contexto-dashboards-snapshots.md` | Fase 48P - Consolidacao de EscolaContextoPort em dashboards e snapshots | Consolidar os usos de `EscolaContextoPort` na area de dashboards e snapshots, apos as aplicacoes pontuais nos dashboards academico, secretaria, diretor, professor e snapshots de indicadores. Esta fase e documental e de verificacao de consistencia. Ela nao c... | Os usos de `EscolaContextoPort` em dashboards e snapshots estao consolidados e documentados. A evolucao multi-escola segue incremental dentro do monolito. |
| `128-fase-48q-uso-pontual-escola-contexto-resumo-academico-matricula.md` | Fase 48Q - Uso pontual de EscolaContextoPort no resumo academico da matricula | Aplicar `EscolaContextoPort` em `MatriculaAcademicoResumoService`, iniciando o uso da fronteira interna de contexto escolar fora da area de dashboards em um fluxo de leitura controlado. Esta fase nao cria BFF, microservico, fila, banco adicional, novo compo... | O resumo academico da matricula passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo. |
| `129-fase-48r-consolidacao-resumo-academico-matricula.md` | Fase 48R - Consolidacao do resumo academico da matricula | Consolidar o uso de `EscolaContextoPort` em `MatriculaAcademicoResumoService` e mapear os proximos fluxos de leitura candidatos antes de qualquer refatoracao transacional de matricula. Esta fase e documental e de verificacao de consistencia. Ela nao cria BF... | O uso de `EscolaContextoPort` no resumo academico da matricula esta consolidado e o proximo passo seguro foi identificado sem ampliar o escopo para refatoracao transacional. |
| `130-fase-48s-uso-pontual-escola-contexto-gateways-consulta-matricula.md` | Fase 48S - Uso pontual de EscolaContextoPort nos gateways auxiliares de matricula | Aplicar `EscolaContextoPort` nos gateways auxiliares de consulta de matricula, mantendo a evolucao incremental do contexto escolar em pontos de leitura e validacao defensiva. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente front... | Os gateways auxiliares de consulta de matricula passam a consumir a fronteira interna de contexto escolar sem alterar comportamento externo. |
| `131-fase-48t-consolidacao-escola-contexto-matricula-leitura-gateways.md` | Fase 48T - Consolidacao de EscolaContextoPort em matricula leitura e gateways auxiliares | Consolidar os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares, apos a aplicacao pontual no resumo academico da matricula e nos gateways de consulta de aluno, periodo letivo e turma. Esta fase e documental e de verificacao de consiste... | Os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares estao consolidados e documentados, preservando comportamento externo e mantendo os fluxos transacionais fora do escopo. |
| `132-fase-48u-diagnostico-usos-remanescentes-escola-tenant-service.md` | Fase 48U - Diagnostico dos usos remanescentes de EscolaTenantService | Mapear os usos remanescentes de `EscolaTenantService` por dominio, risco e tipo de fluxo, escolhendo um unico candidato seguro para a proxima aplicacao pontual de `EscolaContextoPort`. Esta fase e documental e de verificacao. Ela nao cria BFF, microservico,... | Os usos remanescentes de `EscolaTenantService` foram classificados por dominio e risco. O proximo candidato seguro e `CatalogoAcademicoInternalService`, sem ampliar escopo para persistencia, seguranca ou fluxos transacionais. |
| `133-fase-48v-uso-pontual-escola-contexto-catalogo-internal-service.md` | Fase 48V - Uso pontual de EscolaContextoPort em CatalogoAcademicoInternalService | Aplicar `EscolaContextoPort` em `CatalogoAcademicoInternalService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um service interno de leitura e validacao de catalogos academicos. Esta fase nao cria BFF, microservico, fila,... | `CatalogoAcademicoInternalService` passa a consumir a fronteira interna de contexto escolar sem alterar os contratos internos de catalogo, os contratos HTTP ou o comportamento externo. |
| `134-fase-48w-consolidacao-escola-contexto-contratos-internos-catalogo.md` | Fase 48W - Consolidacao de EscolaContextoPort nos contratos internos de catalogo | Consolidar o uso de `EscolaContextoPort` em `CatalogoAcademicoInternalService`, confirmando o estado dos contratos internos de catalogo apos a aplicacao pontual da Fase 48V. Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banc... | Os contratos internos de catalogo estao documentados com `CatalogoAcademicoInternalService` consumindo `EscolaContextoPort` para resolver contexto escolar padrao, mantendo `CatalogoAcademicoPort` e `EstruturaTurmaPort` estaveis. |
| `135-fase-48x-diagnostico-consumidores-estrutura-turma-escola-tenant.md` | Fase 48X - Diagnostico dos consumidores de EstruturaTurmaPort que ainda usam EscolaTenantService | Diagnosticar os consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService` diretamente, separando o uso da porta de estrutura de turma do ponto de resolucao de contexto escolar. Esta fase e documental e de verificacao. Ela nao cria BFF, micro... | Os consumidores de `EstruturaTurmaPort` que ainda usam `EscolaTenantService` foram classificados por risco. O proximo candidato seguro e `PlanejamentoBimestralService`. |
| `136-fase-48y-uso-pontual-escola-contexto-planejamento-bimestral.md` | Fase 48Y - Uso pontual de EscolaContextoPort em PlanejamentoBimestralService | Aplicar `EscolaContextoPort` em `PlanejamentoBimestralService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de planejamento bimestral. Esta fase nao cria BFF, microservico, fila, banco adicional, novo componente f... | `PlanejamentoBimestralService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina. |
| `137-fase-48z-consolidacao-escola-contexto-planejamento-bimestral.md` | Fase 48Z - Consolidacao de EscolaContextoPort em planejamento bimestral | Consolidar o uso de `EscolaContextoPort` em `PlanejamentoBimestralService`, confirmando o estado do fluxo de planejamento bimestral apos a aplicacao pontual da Fase 48Y. Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco ad... | O planejamento bimestral esta documentado como consumidor de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP. |
| `138-fase-49a-diagnostico-diario-aula-escola-contexto.md` | Fase 49A - Diagnostico pontual de DiarioAulaService antes de EscolaContextoPort | Diagnosticar os usos de `EscolaTenantService` em `DiarioAulaService` antes de aplicar `EscolaContextoPort`, separando criacao de aula, consultas, frequencia de professor, frequencia de aluno e validacoes de matricula. Esta fase e documental e de verificacao... | `DiarioAulaService` foi diagnosticado como candidato viavel para uma troca pontual para `EscolaContextoPort`, desde que a proxima fase mantenha escopo restrito ao ponto de resolucao de escola padrao. |
| `139-fase-49b-uso-pontual-escola-contexto-diario-aula.md` | Fase 49B - Uso pontual de EscolaContextoPort em DiarioAulaService | Aplicar `EscolaContextoPort` em `DiarioAulaService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de diario de aula que envolve aulas, frequencia de professor, frequencia de aluno e matricula. Esta fase nao cria BF... | `DiarioAulaService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina. |
| `140-fase-49c-consolidacao-escola-contexto-diario-aula.md` | Fase 49C - Consolidacao de EscolaContextoPort em diario de aula | Consolidar o uso de `EscolaContextoPort` em `DiarioAulaService`, confirmando o estado do fluxo de diario de aula apos a aplicacao pontual da Fase 49B. Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo compo... | O diario de aula esta documentado como consumidor de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP. |
| `141-fase-49d-diagnostico-avaliacao-escola-contexto.md` | Fase 49D - Diagnostico pontual de AvaliacaoService antes de EscolaContextoPort | Diagnosticar os usos de `EscolaTenantService` em `AvaliacaoService` antes de aplicar `EscolaContextoPort`, separando criacao de avaliacao, consultas, lancamento de nota, consulta de notas e validacoes de matricula. Esta fase e documental e de verificacao. E... | `AvaliacaoService` foi diagnosticado como candidato viavel para uma troca pontual para `EscolaContextoPort`, desde que a proxima fase mantenha escopo restrito ao ponto de resolucao de escola padrao e preserve as regras de nota, matricula e turma. |
| `142-fase-49e-uso-pontual-escola-contexto-avaliacao.md` | Fase 49E - Uso pontual de EscolaContextoPort em AvaliacaoService | Aplicar `EscolaContextoPort` em `AvaliacaoService`, mantendo a evolucao incremental das fronteiras internas de contexto escolar em um fluxo de avaliacao que envolve avaliacoes, notas, matricula e consistencia de turma. Esta fase nao cria BFF, microservico, ... | `AvaliacaoService` passa a consumir a fronteira interna de contexto escolar sem alterar comportamento externo, mantendo `EstruturaTurmaPort` como contrato interno de validacao de turma-disciplina. |
| `143-fase-49f-consolidacao-escola-contexto-avaliacoes.md` | Fase 49F - Consolidacao de EscolaContextoPort em avaliacoes | Consolidar o uso de `EscolaContextoPort` em `AvaliacaoService`, confirmando o estado do fluxo de avaliacoes e notas apos a aplicacao pontual da Fase 49E. Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo co... | Avaliacoes e notas estao documentadas como consumidoras de `EscolaContextoPort` e `EstruturaTurmaPort`, preservando comportamento externo e contratos HTTP. |
| `144-fase-49g-diagnostico-atualizado-usos-remanescentes-escola-tenant-service.md` | Fase 49G - Diagnostico atualizado dos usos remanescentes de EscolaTenantService | Reclassificar os usos remanescentes de `EscolaTenantService` apos as aplicacoes pontuais de `EscolaContextoPort` em catalogo interno, dashboards, matricula leitura, planejamento bimestral, diario de aula e avaliacoes. Esta fase e documental e de verificacao... | Os usos remanescentes de `EscolaTenantService` foram reclassificados apos o ciclo de `EscolaContextoPort` em planejamento, diario de aula e avaliacoes. O proximo passo seguro e diagnosticar `DisciplinaService` de forma pontual antes de qualquer alteracao de... |

## Diretriz para documentacao futura

- Nao criar um arquivo novo por fase de implementacao.
- Atualizar este consolidado somente quando houver decisao tecnica relevante ou fechamento de um bloco grande de trabalho.
- Para documentacao de usuario, criar futuramente manuais por perfil padrao, quando os perfis estiverem definidos.
- Para mudancas de codigo, priorizar implementacao e validacao; documentacao deve ser pontual e util.
