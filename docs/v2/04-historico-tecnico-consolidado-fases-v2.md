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
- A pasta `projetos-historico-diario` passa a ser insumo funcional oficial do
  MVP inicial para duas frentes futuras: `diario de classe` e substituicao da
  tela atual de `historico escolar`. Os documentos internos desse pacote
  descrevem contratos backend, regras de bloqueio por assinatura e checagem,
  importacao de PDF e ajustes minimos de banco que devem orientar as proximas
  macrofases desses dominios.
- O `diario de classe` foi classificado como evolucao prioritariamente do bloco
  pedagogico, porque depende de `professor_turma_disciplina`, `aula`,
  `frequencia_aluno`, `planejamento_bimestral_aula`, `avaliacao` e workflow de
  bloqueio por papeis. A necessidade de impedir alteracoes apos assinatura do
  professor e validacao posterior por coordenacao e direcao ainda nao existe no
  backend atual e deve ser tratada como evolucao propria.
- Fica explicito para as proximas fases que `shadow` e `cutover` nao devem ser
  introduzidos em toda etapa. Quando o trabalho permanecer restrito ao backend
  atual/monolito, sem novo runtime recebendo trafego, sem BFF redirecionando
  rota, sem migracao fisica de banco, sem alteracao de contrato externo e sem
  troca real de caminho da aplicacao, a execucao deve ficar limitada a:
  diagnosticar o menor acoplamento, implementar a fronteira interna, preservar
  contrato REST e comportamento funcional, ajustar testes, executar testes e
  registrar objetivamente a entrega. `Cutover` fica reservado para migracao
  real de rota/trafego e `shadow` fica reservado para runtime paralelo com
  comparacao/observabilidade real; em ambos os casos, esses termos nao devem
  aparecer como nome de classe de negocio.
- A nova tela de `historico escolar` foi classificada como substituta funcional
  do fluxo atual em `historico`, exigindo evolucao do contrato hoje exposto em
  `/api/historicos-escolares`: cadastro inicial incompleto, pendencias
  calculadas, snapshot documental de cabecalho e aluno, estudos realizados,
  certificado evolutivo e importacao de PDF como pre-preenchimento revisavel.
- A diretriz de frontend associada a esse pacote tambem fica registrada: as
  duas novas telas passam a ser referencia visual para o layout futuro do
  sistema. Isso nao altera a frente atual exclusivamente backend, mas deve ser
  considerado quando houver macrofase propria de consolidacao visual no
  `school-management-web`.
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
- A segunda subfase da Fase 55 aplicou o mesmo criterio ao primeiro recorte de
  `boletim`. `BoletimService` deixou de consultar
  `NotaAlunoJpaRepository`, `FrequenciaAlunoJpaRepository` e entidades de
  `avaliacao`/`frequencia` diretamente para calcular o boletim em memoria,
  passando a reutilizar `RendimentoAcademicoPort`. Com isso, o desacoplamento
  pedagogico avancou no fluxo de boletim sem alterar o endpoint externo nem o
  fechamento persistido.
- A terceira subfase da Fase 55 fechou o primeiro bloco interno pedagogico.
  `BoletimService` e `HistoricoEscolarServiceImpl` deixaram de resolver escola
  padrao via `EscolaTenantService` e passaram a reutilizar `EscolaContextoPort`,
  alinhando `boletim` e `historico` ao mesmo padrao interno de contexto escolar
  ja adotado em leituras consolidadas e gateways auxiliares. Com isso, o bloco
  minimo de fronteiras internas em resumo academico, boletim e historico ficou
  formalmente encerrado, restando como proximos passos apenas recortes mais
  estruturais do dominio pedagogico.
- A quarta subfase da Fase 55 iniciou o primeiro recorte estrutural controlado
  na geracao oficial de `historico` a partir de `boletim`. Foi criada a
  fronteira interna `BoletimHistoricoPort`, implementada por
  `BoletimHistoricoService`, com DTOs internos proprios para o resumo do
  boletim fechado e dos itens usados na geracao oficial. Com isso,
  `HistoricoEscolarServiceImpl` deixou de consultar diretamente
  `BoletimJpaRepository`, `BoletimItemJpaRepository`, `BoletimEntity` e
  `BoletimItemEntity` nesse fluxo, sem alterar a rota externa nem abrir
  persistencia propria.
- A quinta subfase da Fase 55 consolidou esse mesmo fluxo oficial de geracao
  extraindo de `HistoricoEscolarServiceImpl` a montagem estrutural do
  `HistoricoEscolar` para a fabrica interna `HistoricoEscolarGeracaoFactory`.
  A composicao do cabecalho oficial, dos itens e da observacao padrao de
  geracao passou a ficar centralizada nessa fabrica, mantendo o service como
  orquestrador do fluxo e preservando contratos externos e persistencia atual.
- A sexta subfase da Fase 55 fechou o bloco estrutural minimo remanescente da
  geracao oficial de `historico` por `boletim`. O DTO interno
  `BoletimHistoricoItemResumo` deixou de transportar `PeriodoLetivoEntity`,
  `SerieEntity` e `DisciplinaEntity`, passando a expor apenas IDs, enquanto a
  resolucao JPA dessas referencias ficou encapsulada em
  `HistoricoEscolarGeracaoFactory` por `EntityManager.getReference(...)`. Com
  isso, a Fase 55 fica formalmente encerrada no backend atual sem alterar rota
  externa nem abrir persistencia propria adicional.
- A primeira subfase da Fase 56 abriu o bloco de `planejamento e IA` pelo
  menor acoplamento backend/backend identificado em `PlanejamentoIAService`.
  Foi criada a fronteira interna `PlanejamentoIAPort`, implementada por
  `PlanejamentoIAPlanejamentoService`, com DTO proprio `PlanejamentoIAResumo`
  para resumir o planejamento usado na geracao de conteudo. Com isso,
  `PlanejamentoIAService` deixou de consultar diretamente
  `PlanejamentoBimestralJpaRepository` para esse fluxo, mantendo apenas a
  referencia JPA do planejamento no ponto de persistencia da interacao e do
  conteudo gerado.
- A segunda subfase da Fase 56 aplicou o mesmo criterio ao bloco de
  publicacao da biblioteca pedagogica. Foi criada
  `PlanejamentoIABibliotecaFactory`, apoiada pelo DTO interno
  `PlanejamentoIABibliotecaPublicacaoResumo`, para encapsular a montagem do
  contexto de publicacao e da entidade de biblioteca a partir do conteudo
  aprovado. Com isso, `PlanejamentoIAService` deixa de concentrar a travessia
  direta de entidades de `planejamento`, `professor` e `catalogo` nesse fluxo.
- A terceira subfase da Fase 56 fechou o ponto residual de mapeamento de
  contexto escolar em `PlanejamentoIAService`. `toInteracaoResponse` e
  `toConteudoResponse` passaram a usar `EscolaContextoPort` diretamente para
  escola atual, eliminando a dependencia da travessia de `PlanejamentoBimestral`
  apenas para compor resposta. Com isso, o primeiro bloco interno de
  `planejamento e IA` fica formalmente encerrado no backend atual.
- A quarta subfase da Fase 56 abriu o bloco seguinte pelo menor acoplamento
  remanescente de `PlanejamentoIAService` com a biblioteca pedagogica. Foi
  criada a porta interna `PlanejamentoIABibliotecaPort`, implementada por
  `PlanejamentoIABibliotecaService`, e o DTO interno
  `PlanejamentoIABibliotecaResumo` para listar e publicar conteudos da
  biblioteca sem acesso direto de `PlanejamentoIAService` ao
  `BibliotecaConteudoPedagogicoJpaRepository`. O contrato REST externo e o
  comportamento funcional permaneceram inalterados.
- A quinta subfase da Fase 56 fechou o ultimo acoplamento visivel de escrita em
  `PlanejamentoIAService` com o modulo de planejamento. Foi criada
  `PlanejamentoIAEscritaFactory` para encapsular a referencia JPA ao
  `PlanejamentoBimestral` e a montagem das entidades de interacao, conteudo
  gerado e versao. Com isso, `PlanejamentoIAService` deixou de depender
  diretamente de `PlanejamentoBimestralEntity` e `EntityManager`, encerrando
  formalmente a macrofase backend de `planejamento e IA` no monolito atual.
- O encerramento oficial da Fase 56 consolidou que o bloco de `planejamento e
  IA` fica fechado no backend atual sem mudanca de rotas externas, sem cutover
  de BFF e sem nova persistencia distribuida. A proxima macrofase sugerida
  passa a ser a Fase 57, iniciando por diagnostico pontual do menor recorte
  backend/backend de `dashboard`.
- A primeira subfase da Fase 57 executou esse diagnostico pontual de
  `dashboard` e identificou `DashboardAcademicoService` como o menor recorte
  backend/backend de menor risco para abrir a macrofase. O servico ja expõe um
  resumo consolidado reutilizado por `DashboardSecretariaService` e
  `DashboardDiretorService`, concentra dependencias cruzadas reais de
  `matricula`, `catalogo` e `historico`, e permite rollback interno simples sem
  alterar rotas externas, snapshots ou infraestrutura distribuida.
- A segunda subfase da Fase 57 materializou essa primeira fronteira interna de
  dashboard. Foram criados `DashboardAcademicoPort` e `DashboardAcademicoResumo`
  com tipos internos proprios para o resumo academico, e
  `DashboardSecretariaService` e `DashboardDiretorService` passaram a consumir
  esse contrato em vez da implementacao concreta de `DashboardAcademicoService`.
  Os contratos REST externos permaneceram inalterados e o bloco de snapshots
  ficou fora desta etapa.
- A terceira subfase da Fase 57 reaplicou o mesmo criterio ao primeiro
  consumidor interno adicional de menor risco desse resumo: o caminho
  `ACADEMICO` de `DashboardSnapshotGeradorService` passou a consumir
  `DashboardAcademicoPort` em vez da implementacao concreta de
  `DashboardAcademicoService`. O comportamento externo dos snapshots e os
  fluxos de `SECRETARIA`, `DIRETOR` e `PROFESSOR` permaneceram inalterados.
- A quarta subfase da Fase 57 aplicou o mesmo padrao ao proximo resumo interno
  de menor risco do bloco. Foram criados `DashboardSecretariaPort` e
  `DashboardSecretariaResumo`, `DashboardSecretariaService` passou a separar o
  resumo backend/backend da resposta REST externa, e
  `DashboardDiretorService` junto com o caminho `SECRETARIA` de
  `DashboardSnapshotGeradorService` passaram a consumir esse contrato em vez da
  implementacao concreta. O comportamento externo permaneceu inalterado.
- A quinta subfase da Fase 57 aplicou o mesmo criterio ao ultimo resumo interno
  minimo deste primeiro ciclo. Foram criados `DashboardDiretorPort` e
  `DashboardDiretorResumo`, `DashboardDiretorService` passou a separar o resumo
  backend/backend da resposta REST externa, e o caminho `DIRETOR` de
  `DashboardSnapshotGeradorService` passou a consumir esse contrato em vez da
  implementacao concreta. Com isso, o primeiro bloco minimo de fronteiras
  internas de `dashboard` ficou fechado sem alterar comportamento externo.
- A sexta subfase da Fase 57 encerrou formalmente esse primeiro ciclo interno
  de `dashboard`. O diagnostico confirmou que os resumos reutilizaveis de
  academico, secretaria e diretor ficaram isolados por portas internas, e que
  o recorte remanescente ja e de outra natureza: `DashboardProfessorService`
  segue mais acoplado por fluxo especifico de professor, enquanto
  `DashboardFrontendService` e `DashboardAlertaService` ainda operam como
  composicoes externas sobre services concretos. A macrofase foi fechada sem
  ampliar escopo para nova refatoracao nem para infraestrutura distribuida.
- A primeira subfase da macrofase seguinte de `dashboard` executou o
  diagnostico comparativo do proximo recorte minimo apos o fechamento da Fase
  57. O resultado objetivo foi: `DashboardProfessorService` segue pesado demais
  para abrir a nova etapa, `DashboardFrontendService` agrega dependencias
  demais para um primeiro passo seguro, e `DashboardAlertaService` passa a ser
  o menor candidato de composicao interna por conseguir reaproveitar as portas
  ja criadas de academico, secretaria e diretor sem exigir refatoracao ampla
  nem mudanca externa.
- A segunda subfase dessa nova macrofase aplicou esse primeiro passo de
  composicao minima: `DashboardAlertaService` passou a consumir
  `DashboardAcademicoPort`, `DashboardSecretariaPort` e `DashboardDiretorPort`
  para os publicos ja cobertos pelo bloco anterior, mantendo
  `DashboardProfessorService` concreto por ora. O comportamento externo
  permaneceu inalterado e o recorte continuou restrito ao backend atual.
- A terceira subfase dessa nova macrofase encerrou formalmente esse segundo
  bloco minimo de `dashboard`. O diagnostico final confirmou que
  `DashboardAlertaService` esgota o proximo consumidor de composicao de baixo
  risco, e que os caminhos remanescentes ja exigem uma decisao explicita entre
  abrir `DashboardFrontendService` como composicao externa maior ou
  `DashboardProfessorService` como recorte de dominio mais pesado. O bloco foi
  fechado sem ampliar o escopo tecnico nem introduzir infraestrutura
  distribuida.
- A primeira subfase da macrofase seguinte executou esse diagnostico
  comparativo final e escolheu `DashboardFrontendService` como o proximo
  recorte minimo seguro. O motivo objetivo foi que `DashboardFrontendService`
  continua sendo uma camada de composicao sem consultas JPA proprias e pode
  reaproveitar incrementalmente as portas internas ja abertas, enquanto
  `DashboardProfessorService` segue mais pesado por concentrar consultas
  especificas de professor, alocacoes, aulas, frequencias, avaliacoes, notas e
  planejamentos.
- A segunda subfase dessa macrofase aplicou esse passo de composicao minima em
  `DashboardFrontendService`, que passou a consumir
  `DashboardAcademicoPort`, `DashboardSecretariaPort` e
  `DashboardDiretorPort` para montar o pacote agregado dos publicos ja cobertos
  pelas fronteiras internas anteriores. O contrato externo do frontend foi
  preservado por mapeamento interno para os DTOs REST existentes, enquanto o
  caminho `PROFESSOR` permaneceu concreto por ora.
- A terceira subfase dessa macrofase encerrou formalmente esse bloco minimo de
  composicao em `DashboardFrontendService`. Os testes do agregador passaram a
  cobrir explicitamente `ACADEMICO`, `SECRETARIA` e `DIRETOR` via portas
  internas, confirmando que o unico caminho ainda concreto no service e
  `PROFESSOR`, dependente de `DashboardProfessorService`. Com isso, o recorte
  seguro desta macro-subfase foi considerado esgotado antes de qualquer entrada
  no dominio mais pesado de professor.
- A primeira subfase da macrofase seguinte executou o diagnostico comparativo
  do bloco remanescente de `dashboard` e confirmou que o proximo recorte real
  precisa abrir `DashboardProfessorService`. Os services de configuracao e
  snapshots foram descartados como proximo passo porque atuam como componentes
  transversais com persistencia propria, enquanto `DashboardProfessorService`
  segue sem porta interna e ainda e consumido diretamente por controller,
  frontend, alertas e geracao de snapshots. Como base para a proxima troca de
  fronteira, foi adicionada a primeira cobertura unitária direta do service.
- A segunda subfase dessa macrofase introduziu `DashboardProfessorPort` e um
  DTO interno proprio para o resumo do professor, mantendo
  `DashboardProfessorService` como implementacao concreta inicial. Nessa mesma
  etapa, `DashboardAlertaService` e `DashboardSnapshotGeradorService` passaram
  a depender da nova porta interna, enquanto controller REST e
  `DashboardFrontendService` permaneceram inalterados para preservar o criterio
  de menor risco no bloco.
- A terceira subfase dessa macrofase concluiu a aplicacao da fronteira interna
  de professor em `DashboardFrontendService`, que passou a consumir
  `DashboardProfessorPort` no caminho `PROFESSOR` e a mapear o resumo interno
  para o payload REST ja existente. Com isso, o unico ponto ainda ligado
  diretamente a `DashboardProfessorService` ficou restrito ao controller REST
  como adaptador externo simples.
- A quarta subfase dessa macrofase executou o fechamento formal do bloco e
  confirmou que nao resta mais consumo backend/backend de
  `DashboardProfessorService` fora de `DashboardProfessorPort`. O uso direto
  remanescente ficou apenas em `DashboardProfessorController`, tratado como
  adaptador REST externo e nao como recorte interno adicional nesta macrofase.
- A subfase seguinte executou o diagnostico de decisao do proximo recorte
  backend apos o fechamento de `dashboard`. A comparacao objetiva entre
  `DashboardProfessorController`, `PlanejamentoBimestralService`,
  `HistoricoEscolarServiceImpl` e `MatriculaFluxoService` confirmou que nao
  vale abrir nova macrofase em `dashboard`: o controller de professor restante
  atua apenas como adaptador REST fino sobre `DashboardProfessorPort`, enquanto
  `planejamento` e `historico` ja estao mais protegidos por portas internas e
  contexto escolar explicito. O maior acoplamento transacional remanescente com
  melhor relacao risco/ganho passa a ser `matricula`, especialmente em
  `MatriculaFluxoService`, que ainda concentra writes, repositorios JPA
  cruzados e consistencia local entre matricula, documentos, turma e boletim.
  Com isso, `dashboard` fica formalmente encerrado como bloco interno e a
  proxima macrofase backend passa a ser aberta em `matricula`.
- A subfase seguinte iniciou essa macrofase de `matricula` pelo menor write de
  menor risco dentro de `MatriculaFluxoService`: a atualizacao de status de
  etapa em `PATCH /api/matriculas/{id}/etapas/{etapaId}/status`. Foi criado o
  DTO interno `AtualizarMatriculaEtapaStatusSolicitacao` e a porta
  `MatriculaEtapaPort`, com implementacao local pelo proprio
  `MatriculaFluxoService`. O `MatriculaController` deixou de repassar esse
  write diretamente pelo payload web para a implementacao concreta e passou a
  mapear o request externo para o contrato interno minimo da etapa. O recorte
  permaneceu deliberadamente pequeno, sem alterar schema, sem abrir endpoint
  interno HTTP, sem tocar `documento` ou `historico` e preservando rollback
  simples no mesmo runtime.
- A subfase seguinte aplicou o mesmo criterio ao proximo write de
  `MatriculaFluxoService` com maior ganho arquitetural ainda controlado:
  `POST /api/matriculas/{id}/documentos-entregues`. Foi criado o DTO interno
  `RegistrarMatriculaDocumentoEntregueSolicitacao` e a porta
  `MatriculaDocumentoEntreguePort`, novamente com implementacao local pelo
  proprio `MatriculaFluxoService`. O `MatriculaController` passou a adaptar o
  request externo para esse contrato interno minimo antes de acionar o write,
  deixando explicita a fronteira do primeiro ponto que cruza `matricula` com
  `documento` e com a transicao local de status da matricula. O recorte
  permaneceu propositalmente restrito: nenhuma rota nova, nenhum schema novo,
  nenhum endpoint interno HTTP e nenhuma ampliacao para leituras, `historico`
  ou `rematricula`.
- A subfase seguinte executou o fechamento formal desse primeiro bloco minimo
  da macrofase de `matricula`. A revisao confirmou que os dois writes mais
  seguros de `MatriculaFluxoService` dentro do criterio atual
  (`atualizarStatusEtapa` e `registrarDocumentoEntregue`) ja ficaram cobertos
  por contratos internos explicitos, enquanto os proximos passos naturais
  (`concluirAcademicamente` e `rematricula`) ja entram em acoplamento
  transacional mais pesado com `boletim`, `historico` e regras academicas de
  progressao. Com isso, o primeiro bloco minimo de `matricula` fica formalmente
  encerrado sem abrir endpoint interno HTTP, sem migracao de schema e sem
  persistencia propria fora do monolito.
- A subfase seguinte iniciou o proximo bloco da macrofase de `matricula` pelo
  diagnostico pontual de `concluirAcademicamente`. O mapeamento confirmou que
  esse fluxo ja nao tem o mesmo perfil dos writes anteriores: ele depende da
  leitura oficial de `boletim` fechado, da inspeccao dos `boletim_item` para
  validar pendencias e resultado final, e da transicao de status da matricula
  com observacao historizada. Tambem ficou identificado que ja existe no modulo
  de `historico` a fronteira interna `BoletimHistoricoPort`, mas ela hoje esta
  orientada a geracao de historico escolar e nao explicita ainda um contrato
  dedicado ao desfecho academico da matricula. O resultado da fase foi o
  fechamento objetivo do recorte seguinte: antes de qualquer troca concreta, o
  menor passo seguro passa a ser separar o contrato interno de consulta
  academica do boletim usado por `concluirAcademicamente`.
- A subfase seguinte implementou esse primeiro passo minimo do novo bloco. A
  fronteira interna `BoletimHistoricoPort` foi evoluida com o resumo dedicado
  `BoletimConclusaoAcademicaResumo`, contendo apenas os dados realmente
  consumidos por `concluirAcademicamente` (`boletimId`, `matriculaId` e
  resultados dos itens). `BoletimHistoricoService` passou a materializar esse
  resumo proprio e `MatriculaFluxoService` deixou de depender diretamente de
  `BoletimJpaRepository` e `BoletimItemJpaRepository`, passando a consumir essa
  leitura interna para validar pertencimento do boletim, itens pendentes e
  resultado final. O recorte permaneceu backend/backend, sem endpoint interno
  HTTP, sem migracao de schema e sem ampliar a mudanca para `historico`
  completo ou `rematricula`.
- A subfase seguinte abriu o primeiro recorte minimo de `rematricula` sem
  alterar a escrita final: a elegibilidade passou a ter fronteira interna
  propria por `MatriculaRematriculaPort`, implementada em
  `MatriculaRematriculaService` com o resumo
  `MatriculaRematriculaElegibilidadeResumo`. Com isso,
  `MatriculaFluxoService` deixou de concentrar diretamente a leitura de
  `matricula` e `turma` para o endpoint de elegibilidade, preservando o mesmo
  contrato REST externo, o mesmo runtime e a mesma criacao final via
  `CriarMatriculaUseCase`.
- A subfase seguinte aplicou o mesmo padrao ao write operacional minimo de
  `rematricula`: a mesma fronteira interna passou a expor o resumo
  `MatriculaRematriculaBaseResumo`, contendo apenas `matriculaBaseId`,
  `alunoId` e `statusBase`. Com isso, `MatriculaFluxoService` deixou de carregar
  diretamente a matricula base tambem em `rematricular`, passando a validar o
  status e obter o `alunoId` pela porta `MatriculaRematriculaPort` antes de
  delegar a criacao final ao `CriarMatriculaUseCase`.
- A subfase seguinte encerrou formalmente esse bloco de `rematricula`. A
  revisao final confirmou que, dentro desse recorte, `MatriculaFluxoService`
  nao concentra mais leitura direta da matricula base nem da turma de destino
  para os endpoints externos de rematricula: a elegibilidade e a carga minima
  da base passaram a ser resolvidas por `MatriculaRematriculaPort`, enquanto a
  criacao final continua centralizada no `CriarMatriculaUseCase`. O bloco foi
  fechado sem novo endpoint interno HTTP, sem migracao de schema e sem ampliar
  a mudanca para outro fluxo mais pesado de `matricula`.
- A subfase seguinte iniciou a macrofase backend posterior a `matricula` por um
  diagnostico comparativo dos proximos fluxos ainda concentrados no monolito.
  A comparacao entre candidatos como `AlunoPersistenceGateway` e
  `BoletimService` confirmou que o menor recorte incremental seguinte nao esta
  em fluxos destrutivos de aluno, que ainda concentram limpeza coordenada de
  responsavel, documento, transferencia e historico, mas sim em `boletim`,
  dentro do bloco pedagogico. O mapeamento mostrou que `BoletimService` ainda
  cruza diretamente `matricula` e `catalogo` por `MatriculaJpaRepository`,
  `MatriculaEntity`, `DisciplinaJpaRepository` e `DisciplinaEntity`, enquanto a
  parte de rendimento academico ja foi isolada anteriormente por
  `RendimentoAcademicoPort`. Com isso, o proximo passo minimo recomendado fica
  definido como abrir primeiro a fronteira interna do resumo de matricula
  consumido por `boletim`, antes de atacar a resolucao estrutural de disciplina
  ou qualquer persistencia propria adicional.
- A subfase seguinte materializou esse primeiro recorte minimo de `boletim`.
  Foi criada a fronteira interna `MatriculaBoletimPort`, implementada por
  `MatriculaBoletimService`, com o resumo `MatriculaBoletimResumo` contendo
  apenas os dados de matricula realmente usados por `BoletimService` na
  consulta e na composicao de resposta do fechamento. Com isso, `BoletimService`
  deixou de consultar `MatriculaJpaRepository` diretamente para leitura
  escopada da matricula e passou a manter o detalhe estrutural de
  `MatriculaEntity` apenas no ponto da persistencia oficial do fechamento por
  `EntityManager.getReference(...)`.
- A subfase seguinte aplicou o mesmo criterio ao detalhe estrutural restante de
  `disciplina` no fechamento persistido de `boletim`. Foi criada a fronteira
  interna `DisciplinaBoletimPort`, implementada em
  `CatalogoAcademicoInternalService`, com o resumo `DisciplinaBoletimResumo`
  contendo apenas `disciplinaId`, `nome` e `cargaHoraria`. Com isso,
  `BoletimService` deixou de usar `DisciplinaJpaRepository` diretamente e
  passou a resolver a disciplina por contrato interno escopado por escola,
  mantendo `DisciplinaEntity` apenas como referencia JPA local em
  `EntityManager.getReference(...)` na persistencia de `boletim_item`.
- A subfase final deste bloco fez a revisao formal do recorte de `boletim` e
  confirmou que nao restou leitura direta fora do padrao interno aplicado. O
  fluxo permaneceu dependente apenas de `MatriculaBoletimPort`,
  `RendimentoAcademicoPort` e `DisciplinaBoletimPort` para leitura e resumo de
  contexto, mantendo `MatriculaEntity` e `DisciplinaEntity` somente como
  referencias JPA locais nos pontos estritamente necessarios de persistencia.
  Com isso, o primeiro ciclo de cutover controlado backend/backend em
  `boletim` fica formalmente encerrado sem alterar contratos REST, sem mexer no
  BFF e sem abrir persistencia propria adicional nesta mesma fase.
- A macrofase seguinte foi aberta por diagnostico pontual do novo pacote
  funcional `diario de classe + historico escolar`, usando a pasta
  `projetos-historico-diario` como insumo de MVP. A comparacao entre as duas
  frentes mostrou que `diario de classe` possui risco transacional maior neste
  momento, porque exige agregacao mensal propria, bloqueio por data util,
  assinatura do professor, checagem por coordenacao/direcao e nova governanca
  de alteracao sobre `aula`, `frequencia_aluno`, `planejamento` e
  `avaliacao`. Ja a substituicao da tela de `historico escolar`, embora amplie
  bastante o contrato atual, permanece mais concentrada no proprio dominio de
  `historico`, que ja possui endpoint, entidade e fluxo oficial em operacao.
- Com isso, o menor primeiro recorte seguro dessa macrofase fica definido como
  a leitura e preparacao contratual do novo `historico escolar`: abrir o
  carregamento de cadastro/edicao da nova tela com contexto de matricula,
  pendencias e dados documentais sem substituir ainda o salvamento atual, sem
  importar PDF nesta etapa e sem avancar para o workflow mais pesado de
  `diario de classe`.
- A primeira subfase pratica dessa macrofase implementou esse carregamento de
  forma aditiva no backend atual: `GET /api/historicos-escolares/novo` e
  `GET /api/historicos-escolares/{id}/carregamento` passaram a expor contexto
  de matricula, cabecalho documental, periodos, componentes e pendencias para
  a nova tela, preservando intactos o contrato de salvamento vigente e a
  ausencia de importacao de PDF nesta etapa.
- A segunda subfase pratica iniciou a persistencia minima controlada desse
  recorte sem trocar rotas externas: os campos documentais ja aceitos pelo
  contrato atual de `POST/PUT /api/historicos-escolares` deixaram de ficar
  apenas em memoria e passaram a compor o snapshot persistido do historico,
  permitindo que o carregamento em modo edicao reutilize `nomeEscola`,
  `enderecoEscola`, `municipioEscola`, `cepEscola`, `emailEscola`, dados de
  nascimento e demais informacoes basicas ja suportadas pelo fluxo atual.
- A terceira subfase pratica fechou o bloco minimo de persistencia contextual
  desse historico sem alterar a fachada externa: `historico_escolar` passou a
  guardar `id_matricula`, `status`, `bloqueado`, serie atual/origem e dados
  basicos de transferencia, com preenchimento automatico a partir da matricula
  e da transferencia mais recente do aluno no fluxo atual de criacao/edicao.
  Assim, o carregamento em modo edicao deixa de reconstruir esse contexto
  apenas por fallback e passa a ler primeiro o snapshot persistido.
- O recorte inicial de `diario de classe` tambem foi entregue de forma
  incremental no backend atual, sem BFF e sem frontend: a leitura mensal
  `GET /api/diarios-classe` consolida alunos, frequencias, conteudos
  planejados, observacoes, avaliacoes, assinatura e bloqueio; o salvamento
  `PUT /api/diarios-classe/{idDiarioClasse}` persiste o lancamento controlado
  em `diario_classe_lancamento`, vincula a `aula`, grava frequencias e bloqueia
  duplicidade por alocacao/data apos assinatura do professor.
- A subfase final desse recorte fechou as regras minimas de consistencia de
  lancamento do diario: o backend aceita escrita somente para o dia corrente e
  em dia util, usando relogio injetavel para teste deterministico. O workflow
  posterior de checagem por coordenacao/direcao permanece fora deste MVP
  backend inicial e deve ser tratado em macrofase propria.
- A subfase seguinte iniciou o diagnostico controlado desse workflow de
  checagem e confirmou que ainda nao e seguro abrir endpoint de coordenacao ou
  direcao diretamente: o contrato do prototipo de diario cobre carregamento,
  salvamento, assinatura e bloqueio, mas nao define payloads ou transicoes de
  checagem; o backend atual possui `cargo` com codigos como `COORDENADOR` e
  `DIRETOR` no modelo base, porem nao possui uma fronteira interna que resolva,
  de forma testavel, o usuario autenticado como funcionario autorizado para
  checar diario por escola.
- A subfase pratica seguinte criou essa fronteira interna de autoridade
  pedagogica dentro do `school-management-service`, sem BFF e sem frontend:
  `AutoridadePedagogicaPort` resolve usuario, escola, funcionario ativo e cargo
  permitido (`COORDENADOR`/`DIRETOR`) por contrato proprio antes de qualquer
  rota de checagem ou nova migracao de workflow. A resolucao usa o contexto
  autenticado, o usuario ativo e o vinculo `pessoa`/RH por e-mail dentro da
  escola, falhando fechado quando o funcionario nao existe ou o cargo nao e
  permitido.
- O proximo recorte seguro e modelar os campos/transicoes internas de checagem
  do diario sobre essa autoridade, ainda sem alterar rotas externas no BFF.
- A subfase seguinte materializou esse recorte interno: a tabela
  `diario_classe_lancamento` passa a ter metadados auditaveis para checagem da
  coordenacao e da direcao, o status aceita `CHECADO_COORDENACAO` e
  `CHECADO_DIRECAO`, e `DiarioClasseChecagemService` aplica a sequencia
  controlada `BLOQUEADO -> CHECADO_COORDENACAO -> CHECADO_DIRECAO` usando
  `AutoridadePedagogicaPort`. A entrega continua sem BFF, sem frontend e sem
  endpoint publico.
- O proximo passo seguro e abrir o primeiro adaptador controlado para essa
  checagem, reutilizando o servico interno e mantendo validação por escola,
  funcionario ativo, cargo e ordem da transicao.
- A ultima subfase pratica abriu esse adaptador backend controlado sem BFF e sem
  frontend: `/api/diarios-classe/lancamentos/{idLancamento}/checagens/coordenacao`
  e `/api/diarios-classe/lancamentos/{idLancamento}/checagens/direcao` exigem
  Bearer token e delegam a validacao de escola, cargo e ordem do workflow para
  `DiarioClasseChecagemService` e `AutoridadePedagogicaPort`. O payload aceita
  apenas observacao, evitando que cliente informe cargo, escola ou autoridade.
- Com essa entrega, o bloco backend de checagem do diario esta apto para
  fechamento formal antes de qualquer recorte de BFF/frontend.
- O fechamento formal confirmou o bloco backend de checagem do diario como
  encerrado: a solucao cobre autoridade pedagogica, persistencia dos metadados
  de coordenacao/direcao, transicoes internas, migration, adaptador backend
  controlado e testes automatizados. O BFF e o frontend permanecem fora desse
  bloco.
- A proxima macrofase deve decidir se o diario avanca para BFF/frontend ou se a
  frente continua exclusivamente backend em outra familia funcional.
- A decisao aplicada foi manter a frente backend-only e retomar o novo
  `historico escolar`. A primeira subfase dessa macrofase separou o calculo de
  pendencias em uma fronteira interna propria:
  `HistoricoEscolarPendenciaPort`, `HistoricoEscolarPendenciaContexto` e
  `HistoricoEscolarPendenciaService`. O contrato HTTP de carregamento segue
  inalterado, sem BFF, sem frontend e sem migration.
- A macrofase de novo historico escolar passa a ter contagem regressiva
  estimada de 2 subfases restantes: persistencia propria minima de pendencias,
  se confirmada, e fechamento formal do bloco backend antes de qualquer
  substituicao de tela.
- A subfase seguinte confirmou essa persistencia propria minima de pendencias:
  `historico_escolar_pendencia` passou a armazenar o snapshot das pendencias
  abertas no salvamento do historico, com entidade/repository JPA e migration
  Flyway aditiva. O carregamento da nova tela usa as pendencias persistidas
  quando disponiveis e preserva fallback calculado para historicos legados, sem
  criar rota externa, BFF ou frontend.
- A contagem regressiva da macrofase novo historico escolar passa a 1 subfase
  restante estimada: fechamento formal do bloco backend e decisao sobre quando
  a substituicao de tela podera ser tratada fora desta frente backend-only.
- O fechamento formal encerrou a macrofase backend-only do novo historico
  escolar neste recorte. O bloco agora cobre carregamento de cadastro/edicao,
  contexto minimo persistido, fronteira interna de pendencias, persistencia
  propria minima das pendencias abertas e fallback calculado para historicos
  legados. Permanecem fora deste bloco: troca da tela atual, BFF/frontend,
  importacao de PDF e persistencia das estruturas completas de cabecalho,
  periodos, estudos realizados e certificados evolutivos.
- A contagem regressiva desta macrofase chega a 0. A proxima fase deve abrir
  novo bloco funcional backend-only ou, se a prioridade mudar, iniciar uma
  macrofase separada para BFF/frontend da nova experiencia.
- A fase seguinte abriu o bloco backend-only de documentos/storage pelo menor
  recorte seguro do futuro `enrollment-document-service`: a raiz do storage
  local de documentos saiu da implementacao hardcoded e passou a ser
  configuravel por `documento.storage.local.root`, preservando o default atual
  `uploads/documentos` e a porta `DocumentoArquivoStorage`.
- `LocalDocumentoArquivoStorage` passou a normalizar raiz e diretorios por
  entidade antes da gravacao, sem mudar contratos HTTP, sem BFF/frontend, sem
  storage externo e sem nova persistencia transacional. A contagem regressiva
  da macrofase documentos/storage passa a 2 subfases restantes estimadas:
  diagnostico do contrato minimo para object storage e fechamento formal do
  bloco antes de qualquer cutover.
- A subfase seguinte fechou o contrato minimo para futuro object storage sem
  ativar storage externo: `DocumentoArquivoStorage` passou a devolver
  `DocumentoArquivoReferencia`, separando tipo de storage, chave interna e
  localizacao persistivel. O storage local gera chave relativa por entidade e
  continua expondo `caminhoPersistencia()` compativel com
  `documento.caminho_arquivo`, sem migration, sem BFF/frontend e sem alteracao
  de rotas externas. A contagem regressiva da macrofase documentos/storage
  passa a 1 subfase restante estimada: fechamento formal do bloco e decisao
  sobre quando iniciar object storage real em macrofase propria.
- O fechamento formal encerrou a macrofase documentos/storage neste recorte
  backend-only: existem raiz local configuravel, normalizacao defensiva de
  diretorios, contrato interno estruturado para referencia de arquivo e
  compatibilidade com `documento.caminho_arquivo`. Permanecem fora deste bloco:
  object storage real, bucket, dependencia de cloud, migration de arquivos,
  BFF/frontend e alteracao de contratos HTTP. A contagem regressiva desta
  macrofase chega a 0.
- A macrofase seguinte iniciou object storage real controlado para documentos:
  o `school-management-service` recebeu a dependencia `software.amazon.awssdk:s3`
  e passou a selecionar o backend de arquivos por `documento.storage.backend`,
  mantendo `local` como default. O novo `S3DocumentoArquivoStorage` fica ativo
  apenas com `documento.storage.backend=s3`, usa configuracao de bucket,
  prefixo, regiao, endpoint S3-compatible, credenciais opcionais e path-style
  access, e retorna `DocumentoArquivoReferencia` com `OBJECT_STORAGE` e
  `s3://bucket/chave`. Nao houve bucket, runtime MinIO/S3, migration,
  BFF/frontend ou mudanca de contrato HTTP. A contagem regressiva da macrofase
  object storage real controlado passa a 2 subfases restantes estimadas.
- A subfase seguinte validou operacionalmente o modo
  `documento.storage.backend=s3` no contexto Spring real do backend: o teste
  executa o upload multipart de `/api/documentos-alunos`, cria o aluno, passa
  por controller, service, use case e `S3DocumentoArquivoStorage`, e confirma
  a persistencia de `s3://bucket/chave`. O `S3Client` foi substituido por mock
  no limite externo para nao iniciar MinIO/S3, criar bucket ou depender de
  credenciais reais nesta etapa. A contagem regressiva da macrofase object
  storage real controlado passa a 1 subfase restante estimada.
- O fechamento formal da Fase 59 encerrou a macrofase object storage real
  controlado sem iniciar runtime S3-compatible real: a decisao foi manter o
  recorte backend-only preparado por configuracao, com `local` como default,
  `s3` como modo opt-in validado por testes e rollback imediato via
  `documento.storage.backend=local`. Nao houve Docker/MinIO, bucket,
  credenciais reais, migration, BFF/frontend, mudanca de contrato HTTP ou
  migracao de arquivos existentes. A contagem regressiva desta macrofase
  chega a 0; uma evolucao real de ambiente S3-compatible deve abrir macrofase
  propria com migracao, reconciliacao e rollback operacional.
- A primeira subfase da Fase 60 abriu a macrofase backend-only de
  `people-service` por diagnostico, sem criar runtime fisico e sem alterar
  rotas externas. O diagnostico confirmou que o monolito ja tem fronteiras
  internas iniciais (`PessoaCadastroPort` e `FuncionarioProfessorPort`), mas
  `PessoaCadastroPort` ainda expoe `PessoaEntity` e mantem acoplamento JPA
  direto com aluno, responsavel, professor e funcionario. Por isso, o menor
  proximo recorte seguro foi definido como contrato interno entity-free de
  consulta cadastral e catalogos de pessoa, reaproveitando os comportamentos
  atuais de `/api/consulta-cadastral` e `/api/pessoas/catalogos`, sem mover
  escrita, sem migration, sem BFF/frontend e mantendo o monolito como
  autoridade. A contagem regressiva da macrofase `people-service` passa a 3
  subfases restantes estimadas.
- A segunda subfase da Fase 60 materializou esse contrato interno entity-free:
  foi criada a porta `PessoaConsultaPort` com DTOs internos proprios para
  catalogos e consulta cadastral, e a implementacao `PessoaConsultaService`
  passou a concentrar a leitura usada por `/api/consulta-cadastral` e
  `/api/pessoas/catalogos`. `ConsultarCadastroAlunoResponsavelUseCase` e
  `PessoaCatalogoController` passaram a consumir a nova porta, enquanto
  `PessoaCadastroPort` ficou restrita ao contrato de cadastro/escrita. O
  gateway antigo de consulta cadastral em `responsavel` foi removido. Nao houve
  BFF/frontend, migration, nova rota, runtime fisico de `people-service`,
  alteracao de payload externo ou movimentacao de escrita. A contagem
  regressiva da macrofase `people-service` passa a 2 subfases restantes
  estimadas.
- A terceira subfase da Fase 60 aplicou o primeiro consumo interno adicional de
  baixo risco sobre `PessoaConsultaPort`: a porta passou a expor consulta
  entity-free de pessoa por `pessoaId` e `escolaId`, retornando `PessoaResumo`,
  e `FuncionarioProfessorService` passou a montar o resumo de funcionarios
  elegiveis para professor a partir dessa fronteira em vez de ler nome/escola
  diretamente de `FuncionarioEntity.getPessoa()`. A criacao de professor
  permaneceu em `PessoaCadastroPort`, porque ainda depende de `PessoaEntity`
  para o relacionamento JPA atual. Nao houve BFF/frontend, migration, nova
  rota, runtime fisico de `people-service`, alteracao de payload externo ou
  movimentacao de escrita. A contagem regressiva da macrofase `people-service`
  passa a 1 subfase restante estimada.
- O fechamento formal da Fase 60 autorizou a proxima macrofase apenas para
  abertura fisica do `people-service` em modo backend/backend shadow/read-only,
  consumindo os contratos entity-free ja estabilizados em `PessoaConsultaPort`.
  O recorte inicial deve ficar limitado a catalogos de pessoa, consulta
  cadastral e resumo de pessoa por `pessoaId` e `escolaId`, sem mover escrita,
  sem BFF/frontend, sem rota externa nova, sem migration de dados e sem
  persistencia propria autoritativa. A escrita permanece bloqueada porque
  `PessoaCadastroPort`, aluno, responsavel, professor, funcionario, endereco e
  vinculos JPA ainda dependem do schema e das transacoes locais do
  `school-management-service`. A contagem regressiva da macrofase
  `people-service` chega a 0.
- A primeira subfase da Fase 61 iniciou o runtime fisico `people-service` em
  modo backend/backend shadow/read-only. O monorepo passou a ter o modulo Maven
  `people-service`, sem JPA/Flyway e sem persistencia propria, expondo apenas
  rotas internas `/internal/v1/pessoas/**` e `/internal/pessoas/**`. O
  `school-management-service` passou a expor o adaptador interno
  `/internal/pessoas/**` sobre `PessoaConsultaPort`, e o novo runtime consome
  esse contrato por proxy HTTP, repassando headers internos e mantendo o
  monolito como autoridade de leitura/escrita. Foram adicionados health e
  metricas `people.shadow.monolith.*`. Nao houve BFF/frontend, migration, schema
  proprio, rota externa nova, cutover de escrita ou autoridade local de dados.
  A contagem regressiva da macrofase `people-service` fisico passa a 2 subfases
  restantes estimadas.
- A segunda subfase da Fase 61 consolidou o smoke operacional do
  `people-service` fisico em modo shadow/read-only. Foram adicionados testes de
  health/metricas para `peopleShadowMonolith`, cobrindo diagnostico por rota,
  totais de requests, totais de falhas, `base-url` invalida e um fluxo com
  runtime em porta aleatoria usando `MockWebServer` como monolito simulado. O
  smoke comprova sucesso, `not_found` e erro downstream refletidos em
  `/actuator/health/peopleShadowMonolith`, sem BFF/frontend, write, migration,
  banco proprio, cutover externo ou autoridade local de dados. A contagem
  regressiva da macrofase `people-service` fisico passa a 1 subfase restante
  estimada.
- O fechamento formal da Fase 61 encerrou a macrofase de abertura fisica
  read-only do `people-service`. O runtime fica limitado a proxy
  backend/backend shadow/read-only sobre os contratos entity-free de
  `PessoaConsultaPort`, com cobertura operacional para catalogos de pessoa,
  catalogos de endereco, consulta cadastral e resumo de pessoa por
  `pessoaId` + `escolaId`. A decisao manteve bloqueados BFF/frontend, rota
  externa nova, write, migration, banco proprio, schema autoritativo e cutover
  externo. O rollback segue trivial, desligando o runtime shadow e mantendo o
  monolito como fonte unica. A contagem regressiva da macrofase
  `people-service` fisico chega a 0; a proxima macrofase sugerida e diagnostico
  de persistencia propria controlada, ainda sem mover escrita.
- A primeira subfase da Fase 62 diagnosticou o menor caminho seguro para
  persistencia propria controlada do `people-service`. A decisao foi iniciar
  apenas por read model local opcional, sem migration aplicada nesta subfase,
  sem schema autoritativo, sem write, sem BFF/frontend e sem cutover externo. O
  primeiro conjunto candidato fica restrito a `pessoa`, `tipo_pessoa`,
  `pessoa_tipo_pessoa`, `endereco`, `tipo_endereco` e `pessoa_endereco`, com
  backfill idempotente e reconciliacao por escola/CPF/tipo/endereco principal.
  `aluno`, `responsavel`, `funcionario`, `professor`, documentos, historico,
  matricula, diario, avaliacao e IA continuam fora do schema local inicial por
  dependerem das transacoes e joins do monolito. A contagem regressiva da
  macrofase de persistencia controlada do `people-service` passa a 3 subfases
  restantes estimadas.
- A segunda subfase da Fase 62 adicionou ao `people-service` a fundacao opt-in
  de persistencia local read-only apenas como contrato interno, configuracao e
  observabilidade. Foram criadas as flags
  `people.shadow.local-persistence.enabled`, `migration-enabled`,
  `read-model-cutover-enabled` e `fail-on-error`, todas desligadas por padrao,
  e o health dedicado `peopleLocalPersistence`, que expoe tabelas candidatas,
  rotas shadow impactadas, tabelas excluidas, estrategia de rollback e
  contadores esperados de backfill, divergencia e falha. A ativacao prematura
  da persistencia local ou do cutover de leitura retorna `OUT_OF_SERVICE`,
  preservando o monolito como fonte unica. Nao houve JPA, Flyway, datasource,
  migration, schema fisico, rota nova, BFF/frontend, escrita autoritativa ou
  cutover. A contagem regressiva da macrofase de persistencia controlada do
  `people-service` passa a 2 subfases restantes estimadas.
- A terceira subfase da Fase 62 adicionou ao `people-service` o primeiro
  esqueleto controlado de backfill/reconciliacao para o read model de pessoas,
  ainda sem schema local, adapter de banco, escrita autoritativa ou cutover.
  Foram criadas as flags
  `people.shadow.local-persistence.backfill-enabled`,
  `reconciliation-enabled` e `backfill-batch-size`, todas inertes por padrao,
  alem de um coordenador que gera relatorio em modo `planned_only` para as seis
  tabelas candidatas, com chaves idempotentes, origem `monolith_proxy`, destino
  candidato `people_read_model_candidate`, escrita desligada e cutover
  desligado. Um runner de startup executa o ciclo somente quando backfill ou
  reconciliacao forem habilitados explicitamente, e o health
  `peopleLocalPersistence` passou a expor plano, tamanho de lote e contadores
  de ciclos/tabelas planejadas. A contagem regressiva da macrofase de
  persistencia controlada do `people-service` passa a 1 subfase restante
  estimada.
- A quarta subfase da Fase 62 fechou a macrofase de persistencia controlada do
  `people-service` com uma guarda interna de cutover de leitura. A guarda e
  chamada antes das consultas shadow apenas para decidir e medir roteamento,
  mantendo o `PessoaReadPort` efetivo apontado para `monolith_proxy`. Foi
  adicionada a flag
  `people.shadow.local-persistence.read-model-fallback-enabled`, ligada por
  padrao, e cada operacao candidata passou a ter decisao observavel com origem
  candidata, origem selecionada, elegibilidade, motivo de bloqueio e escrita
  desligada. Mesmo com `read-model-cutover-enabled=true`, a leitura local
  continua inelegivel sem persistencia local habilitada, reconciliacao verde,
  ausencia de falhas/divergencias e adapter local implementado. O health
  `peopleLocalPersistence` expõe o plano de roteamento e retorna
  `OUT_OF_SERVICE` quando o cutover e solicitado sem cumprir as pre-condicoes.
  Nao houve schema local fisico, migration, BFF/frontend, escrita ou cutover
  real. A contagem regressiva da macrofase de persistencia controlada do
  `people-service` chega a 0.
- A primeira subfase da Fase 63 iniciou a macrofase de schema local read-only
  controlado do `people-service`. O diagnostico definiu `tipo_pessoa` e
  `tipo_endereco` como o menor recorte fisico seguro, por serem catalogos
  globais simples, com UUID, `codigo` unico e sem dependencia de tenant ou
  transacao de cadastro. `pessoa`, `pessoa_tipo_pessoa`, `endereco`,
  `pessoa_endereco`, `pessoa_documento`, `aluno`, `responsavel`, `funcionario`,
  `professor` e demais tabelas transacionais continuam fora da primeira
  migration fisica. Foi adicionado ao `people-service` um plano interno de
  schema de catalogo no health `peopleLocalPersistence`, expondo tabelas
  candidatas, colunas, chaves, origem de seed, bloqueios, rollback e proximo
  passo recomendado. Nao houve datasource, Flyway, JPA, migration fisica,
  adapter local, BFF/frontend, escrita ou cutover. A contagem regressiva da
  macrofase de schema local read-only do `people-service` passa a 3 subfases
  restantes estimadas.
- A segunda subfase da Fase 63 adicionou ao `people-service` a preparacao
  fisica opt-in do schema local read-only de `tipo_pessoa` e `tipo_endereco`.
  Foram adicionados Flyway e driver PostgreSQL sem datasource automatico e sem
  JPA, alem da migration `V1__create_people_catalog_read_model.sql`, restrita
  aos dois catalogos globais com IDs originais e `codigo` unico. Um runner
  manual executa a migration somente quando
  `people.shadow.local-persistence.migration-enabled` esta habilitado e exige
  URL explicita em `people.shadow.local-persistence.schema-migration.*`;
  habilitacao sem destino configurado fica bloqueada e respeita `fail-on-error`.
  O health `peopleLocalPersistence` passou a expor estado e metricas de schema
  migration. Nao houve adapter local, alteracao de leitura, BFF/frontend,
  escrita ou `read-model-cutover`. A contagem regressiva da macrofase de schema
  local read-only do `people-service` passa a 2 subfases restantes estimadas.
- A terceira subfase da Fase 63 adicionou ao `people-service`
  backfill/reconciliacao opt-in real dos catalogos `tipo_pessoa` e
  `tipo_endereco`. A origem do monolito e configurada explicitamente em
  `people.shadow.local-persistence.catalog-backfill.source-*` e o destino
  local reaproveita `people.shadow.local-persistence.schema-migration.*`. O
  adapter JDBC copia somente esses dois catalogos com upsert idempotente por
  UUID, preserva `codigo` como chave natural e reconcilia divergencias por
  codigo, UUID e descricao. O health `peopleLocalPersistence` passou a expor o
  ultimo relatorio `catalogBackfill`, totais de origem/destino, registros
  copiados, divergencias e status agregado. Nao houve adapter local de leitura,
  escrita, BFF/frontend, tabela transacional ou `read-model-cutover`. A
  contagem regressiva da macrofase de schema local read-only do
  `people-service` passa a 1 subfase restante estimada.
- A quarta subfase da Fase 63 fechou a macrofase de schema local read-only do
  `people-service`. Foi adicionado adapter local de leitura apenas para
  `tipo_pessoa` e `tipo_endereco`, usando o schema local configurado em
  `people.shadow.local-persistence.schema-migration.*`, sem datasource
  automatico e sem JPA. A guarda de leitura so libera fonte local para
  `listarTiposPessoa` e `listarTiposEndereco` quando a leitura local e
  explicitamente pedida, o fallback esta ligado, persistencia/backfill/
  reconciliacao estao habilitados, o ultimo `catalogBackfill` esta `completed`
  e nao ha divergencias/falhas. `buscarPorId` e `consultarCadastro` continuam
  em `monolith_proxy`; falha no adapter local registra fallback e retorna ao
  monolito. Nao houve escrita, BFF/frontend, tabela transacional, mudanca de
  payload externo ou cutover amplo. A contagem regressiva da macrofase de
  schema local read-only do `people-service` chega a 0.
- A primeira subfase da Fase 64 iniciou o diagnostico da proxima fatia
  transacional do `people-service`. Foi adicionado ao health
  `peopleLocalPersistence` o plano `transactionalReadModelExpansionPlan`,
  separando candidato minimo, dependencias, bloqueios, rollback e proximo passo.
  A decisao foi nao migrar de uma vez `pessoa`, `pessoa_tipo_pessoa`,
  `endereco` e `pessoa_endereco`, porque isso mistura identidade, papeis,
  endereco, consulta cadastral, PII e derivacao de escopo escolar no mesmo
  passo. O menor recorte candidato ficou como `pessoa_identity_read_model`,
  composto por `pessoa` e `pessoa_tipo_pessoa`, limitado inicialmente a preparar
  `buscarPorId`. `endereco` e `pessoa_endereco` ficam fora da primeira fatia
  transacional. Nao houve migration, backfill, adapter local transacional,
  escrita, BFF/frontend ou cutover. A contagem regressiva da macrofase Fase 64
  passa a 3 subfases restantes estimadas.
- A segunda subfase da Fase 64 preparou o schema fisico opt-in da fatia de
  identidade do `people-service`. Foi adicionada a migration
  `V2__create_people_identity_read_model.sql`, executada apenas pelo runner
  opt-in existente quando
  `people.shadow.local-persistence.migration-enabled` esta habilitado com
  destino explicito em `people.shadow.local-persistence.schema-migration.*`.
  A migration cria somente `pessoa` e `pessoa_tipo_pessoa`, preservando IDs
  originais do monolito, `id_escola` como identificador copiado de escopo e o
  vinculo com o catalogo local `tipo_pessoa`. `endereco`,
  `pessoa_endereco`, `aluno`, `responsavel`, `funcionario`, `professor` e
  documentos continuam fora. O health passou a reportar
  `pessoa_identity_schema_prepared_opt_in`, liberando apenas migration opt-in e
  mantendo backfill, leitura local transacional, escrita, BFF/frontend e
  cutover bloqueados. A contagem regressiva da macrofase Fase 64 passa a 2
  subfases restantes estimadas.
- A terceira subfase da Fase 64 implementou backfill/reconciliacao opt-in da
  fatia de identidade do `people-service`. O ciclo controlado passou a
  sincronizar `tipo_pessoa`, `tipo_endereco`, `pessoa` e
  `pessoa_tipo_pessoa`, nessa ordem, usando JDBC do monolito para o schema
  local e upsert idempotente por ID original. A reconciliacao compara
  ID/CPF/campos de identidade e papeis, reportando divergencias no mesmo
  `PeopleLocalPersistenceOperationReport`. O health passou a expor
  `localReadModelBackfill`, mantendo `catalogBackfill` por compatibilidade, e o
  plano transacional passou a reportar
  `pessoa_identity_backfill_reconciliation_prepared_opt_in`. Nao houve rota
  externa, BFF/frontend, escrita, adapter local transacional ou cutover de
  `buscarPorId`. A contagem regressiva da macrofase Fase 64 passa a 1 subfase
  restante estimada.
- A quarta subfase da Fase 64 fechou o primeiro cutover controlado de leitura
  local de identidade do `people-service`. Foi criada a porta
  `PeopleIdentityLocalReadPort` e o adapter local
  `JdbcPeopleIdentityLocalReadAdapter` para `buscarPorId`, consultando
  `pessoa` por `id_pessoa` e `id_escola` somente quando
  `PeopleLocalReadCutoverGuard` liberar a rota com read model verde. O read
  model passou a copiar `escola_nome` como campo desnormalizado de leitura para
  preservar o contrato de `PessoaResumoResponse` sem tornar o servico
  autoridade de escola. Em ausencia local ou erro, o fallback obrigatorio para
  o monolito permanece ativo e observado por metrica propria. `consultarCadastro`,
  `endereco`, `pessoa_endereco`, escritas, BFF/frontend e cutover amplo
  continuam fora. A contagem regressiva da macrofase Fase 64 chega a 0.
- A primeira subfase da Fase 65 iniciou a macrofase seguinte do `people-service`
  por diagnostico, sem migration, backfill, adapter novo ou cutover. O planner
  interno passou a classificar o proximo recorte minimo como
  `pessoa_address_read_model`, formado por `endereco` e `pessoa_endereco`, para
  preparar futuramente `consultarCadastro`. `pessoa` e `pessoa_tipo_pessoa`
  ficam registradas como fatia ja preparada para `buscarPorId`, enquanto
  `endereco` e `pessoa_endereco` sao candidatas da proxima fatia, ainda sem
  autorizacao de migration, backfill ou leitura local. A contagem regressiva da
  macrofase Fase 65 passa a 3 subfases restantes estimadas.
- A segunda subfase da Fase 65 mapeou o contrato real de `consultarCadastro`.
  A rota atual filtra por aluno/responsavel e pagina o resultado, mas o payload
  nao expõe endereco. Cada item retorna dados do aluno e sua lista de
  responsaveis; a consulta do monolito usa `aluno`, `responsavel` e
  `aluno_responsavel`. Com isso, a hipotese inicial de preparar
  `endereco`/`pessoa_endereco` foi bloqueada para este contrato, e o planner
  passou a indicar `pessoa_student_responsible_read_model` como proximo recorte
  minimo, ainda sem migration, backfill, adapter local ou cutover. A contagem
  regressiva da macrofase Fase 65 passa a 2 subfases restantes estimadas.
- A terceira subfase da Fase 65 preparou o schema opt-in do read model minimo de
  `consultarCadastro` no `people-service`. Foi adicionada a migration
  `V3__create_people_student_responsible_read_model.sql` com as tabelas locais
  `aluno`, `responsavel` e `aluno_responsavel`, preservando os IDs do monolito e
  os campos hoje retornados pela consulta. O health, o runner de migration e o
  planner transacional passaram a reportar essa fatia como schema preparado por
  opt-in, mas backfill, reconciliacao, adapter local, BFF/frontend, escrita local
  e cutover seguem bloqueados. `endereco`, `pessoa_endereco` e `tipo_endereco`
  continuam fora porque nao compoem o payload real. A contagem regressiva da
  macrofase Fase 65 passa a 1 subfase restante estimada.
- A quarta subfase da Fase 65 expandiu o ciclo controlado de
  backfill/reconciliacao do `people-service` para `aluno`, `responsavel` e
  `aluno_responsavel`, mantendo no mesmo fluxo opt-in as tabelas ja existentes
  de catalogo e identidade. O adapter JDBC passou a copiar e reconciliar a fatia
  `people_read_model_student_responsible` com IDs preservados do monolito e
  campos usados pelo contrato atual de `consultarCadastro`. A rota continua no
  proxy do monolito porque ainda nao ha adapter local e o guard de cutover segue
  bloqueando `consultarCadastro` com `local-read-adapter-not-configured`. Nao
  houve BFF/frontend, escrita local, endereco ou cutover. A contagem regressiva
  da macrofase Fase 65 chega a 0.
- A primeira subfase da Fase 66 abriu a macrofase de adapter local controlado de
  `consultarCadastro` no `people-service` apenas por diagnostico observavel. O
  menor recorte remanescente foi definido como adapter local read-only sobre
  `aluno`, `responsavel` e `aluno_responsavel`, porque schema, backfill e
  reconciliacao opt-in dessa fatia ja foram fechados na Fase 65. O planner
  transacional passou a reportar a fatia
  `pessoa_student_responsible_local_read_adapter` no health, com migration e
  backfill ja permitidos por opt-in, mas `localReadCutoverAllowedNow=false`.
  `PessoaQueryService` continua delegando `consultarCadastro` ao monolito e o
  guard segue bloqueando a rota com `local-read-adapter-not-configured`. Nao
  houve adapter novo, BFF/frontend, escrita local, endereco ou cutover. A
  contagem regressiva da macrofase Fase 66 passa a 2 subfases restantes
  estimadas.
- A segunda subfase da Fase 66 implementou o contrato interno
  `PeopleStudentResponsibleLocalReadPort` e o adapter JDBC read-only de
  `consultarCadastro` sobre `aluno`, `responsavel` e `aluno_responsavel`,
  reproduzindo filtros, paginacao, limite de pagina, ordenacao por nome do
  aluno e agregacao de responsaveis por aluno. `PessoaQueryService` continua
  retornando pelo monolito, sem roteamento local nem cutover, e o planner passou
  a reportar
  `consultar_cadastro_local_adapter_prepared_without_routing` com
  `localReadCutoverAllowedNow=false`. Nao houve BFF/frontend, escrita local,
  endereco ou mudanca de contrato externo. A contagem regressiva da macrofase
  Fase 66 passa a 1 subfase restante estimada.
- A terceira subfase da Fase 66 conectou `consultarCadastro` ao adapter local
  apenas atras do guard de cutover do `people-service`, com fallback obrigatorio
  para o monolito quando o read model/reconciliacao nao esta verde ou quando a
  leitura local falha. Em seguida, o bloco foi fechado operacionalmente com o
  diagnostico `guardedReadCutoverClosure` no health `peopleLocalPersistence`,
  deixando explicitos fonte selecionada, fallback, criterios verdes, metricas e
  rollback. A macrofase Fase 66 chega a 0 subfases restantes.
- A primeira subfase pos-Fase 66 iniciou o diagnostico profundo de `endereco`.
  O planner do `people-service` passou a reportar
  `address_read_model_deep_diagnostic_closed_no_schema`, mantendo migration,
  backfill e cutover bloqueados. O health `peopleLocalPersistence` passou a
  detalhar `nextBlockedSliceDiagnostic` com consumidores reais do monolito:
  escrita por `PessoaFoundationService`, criacao/atualizacao de aluno e
  responsavel, limpeza de vinculos/orfaos nos gateways e separacao obrigatoria
  da consulta externa ViaCEP. A decisao tecnica e que a proxima subfase segura
  deve criar apenas contrato interno entity-free de endereco, ainda sem schema
  local, sem BFF/frontend, sem escrita local e sem cutover.
- A segunda subfase da Fase 67 preparou esse contrato interno entity-free de
  endereco dentro do monolito, ainda sem schema local e sem rota externa. Foram
  criados `PessoaEnderecoPort` e `PessoaEnderecoResumo` para expor endereco
  principal por pessoa e limpeza de vinculos/endereco orfao sem vazar
  `EnderecoEntity`, `PessoaEnderecoEntity` ou repositorios JPA para os
  consumidores. `AlunoPersistenceGateway` e `ResponsavelPersistenceGateway`
  passaram a consultar endereco principal e remover enderecos via essa porta,
  preservando comportamento externo. O health do `people-service` passou a
  reportar `preparedInternalContract`, enquanto schema, backfill, BFF/frontend,
  escrita local e cutover continuam bloqueados. A contagem regressiva da Fase
  67 chega a 0.
- A primeira subfase da Fase 68 fechou o diagnostico de schema/backfill local de
  `endereco` e `pessoa_endereco` no `people-service`, ainda sem criar migration
  nem executar backfill. O planner passou a reportar
  `address_schema_backfill_diagnostic_closed_no_migration`, mantendo migration,
  backfill e cutover desligados. O health `peopleLocalPersistence` passou a
  expor `addressSchemaBackfillDiagnostic`, com colunas minimas, tabelas
  candidatas, tabelas de referencia, chave de reconciliacao por
  `pessoa_endereco.id_pessoa_endereco`, checks secundarios, regra de endereco
  principal, politica ViaCEP e rollback. A proxima subfase segura e preparar
  apenas migration opt-in de schema para `endereco`/`pessoa_endereco`, sem
  backfill automatico, sem BFF/frontend, sem escrita local e sem cutover. A
  contagem regressiva da Fase 68 passa a 2 subfases restantes estimadas.
- A segunda subfase da Fase 68 preparou a migration opt-in
  `V4__create_people_address_read_model.sql` no `people-service`, criando as
  tabelas read-only candidatas `endereco` e `pessoa_endereco` com IDs
  preservados do monolito, dependencias para `pessoa` e `tipo_endereco`, e
  indices de apoio. O runner de schema passou a reportar essas tabelas no
  conjunto migravel quando a flag de migration for habilitada, mas a flag
  continua desligada por padrao. O planner passou a reportar
  `address_schema_migration_opt_in_prepared_no_backfill`, liberando apenas a
  migration opt-in e mantendo backfill/cutover bloqueados. O health
  `peopleLocalPersistence` passou a indicar
  `schema_migration_opt_in_prepared_backfill_still_blocked`, com referencia
  explicita a V4, `enabledByDefault=false` e `automaticBackfill=false`. Nao
  houve backfill, reconciliacao, leitura local de endereco, BFF/frontend,
  escrita local ou cutover. A contagem regressiva da Fase 68 passa a 1 subfase
  restante estimada.
- A terceira subfase da Fase 68 preparou o backfill/reconciliacao opt-in de
  `endereco` e `pessoa_endereco` no `people-service`, mantendo os IDs originais
  do monolito como chaves idempotentes (`id_endereco` e
  `id_pessoa_endereco`) e execucao desligada por padrao via flags. A
  reconciliacao de endereco compara campos normalizados, a de
  `pessoa_endereco` valida pessoa/endereco/tipo/principal, e a origem com mais
  de um endereco principal para a mesma pessoa bloqueia o relatorio com
  `address-principal-rule-violated`. O planner passou a reportar
  `address_backfill_reconciliation_prepared_no_read_cutover` e o health passou
  a reportar `backfill_reconciliation_prepared_read_cutover_still_blocked`,
  incluindo alvo `people_read_model_address`, blockers de reconciliacao verde e
  rollback por desligamento de flags. Nao houve leitura local de endereco,
  BFF/frontend, rota externa, escrita local ou cutover. A contagem regressiva da
  Fase 68 chega a 0.
- A Fase 68 foi formalmente encerrada apos o commit/push da terceira subfase.
  O bloco fica fechado com diagnostico de schema/backfill, migration opt-in e
  backfill/reconciliacao opt-in de `endereco`/`pessoa_endereco`, sem leitura
  local de endereco, sem adapter de rota de negocio, sem BFF/frontend, sem
  escrita local e sem cutover. A proxima macrofase sugerida e diagnosticar o
  contrato de leitura local de endereco no `people-service`, separando payload
  interno, guard, reconciliacao verde, fallback obrigatorio para o monolito e
  rollback por flags antes de qualquer uso operacional.
- A primeira subfase da Fase 69 iniciou a macrofase backend-only do contrato de
  leitura local de endereco no `people-service`. O planner passou a reportar
  `address_local_read_contract_diagnostic_started_no_cutover`, mantendo
  `localReadCutoverAllowedNow=false` e deixando `endereco`/`pessoa_endereco`
  apenas como candidatos dependentes de contrato. O health
  `peopleLocalPersistence` passou a expor `addressLocalReadContractDiagnostic`,
  com origem candidata `people_read_model_address`, fallback obrigatorio para
  `monolith_proxy`, operacoes internas candidatas, payload minimo, pre-condicoes
  de guard, regras de consistencia, out-of-scope e rollback por flags. Nao houve
  rota REST nova, adapter operacional, alteracao de `consultarCadastro`,
  BFF/frontend, escrita local ou cutover. A contagem regressiva da Fase 69 passa
  a 2 subfases restantes estimadas.
- A segunda subfase da Fase 69 criou o contrato interno
  `PeopleAddressLocalReadPort` e o DTO `PessoaEnderecoLocalReadResponse` no
  `people-service`, ainda sem implementacao JDBC, sem injecao em use case, sem
  rota REST e sem conectar `consultarCadastro`. O planner passou a reportar
  `address_local_read_port_contract_prepared_no_adapter`, apontando a proxima
  etapa para avaliar um adapter local atras de guard. O health passou a expor
  `preparedArtifacts` dentro de `addressLocalReadContractDiagnostic`, marcando
  `routeCreated=false`, `adapterCreated=false` e `queryServiceConnected=false`.
  Nao houve BFF/frontend, escrita local, rota externa, adapter operacional ou
  cutover. A contagem regressiva da Fase 69 passa a 1 subfase restante
  estimada.
- A terceira subfase da Fase 69 criou o adapter
  `JdbcPeopleAddressLocalReadAdapter`, implementando
  `PeopleAddressLocalReadPort` sobre o read model local de endereco. O adapter
  le apenas `pessoa`, `pessoa_endereco`, `endereco` e `tipo_endereco`, aplica
  escopo por escola via `pessoa.id_escola`, retorna o payload interno
  `PessoaEnderecoLocalReadResponse`, lista enderecos com principal primeiro e
  bloqueia `buscarEnderecoPrincipalPorPessoa` com
  `address-principal-rule-violated` quando houver mais de um endereco principal
  para a mesma pessoa. O planner passou a reportar
  `address_local_read_adapter_prepared_no_route_no_cutover`, e o health passou
  a indicar `adapterCreated=true`, `queryServiceConnected=false`,
  `localReadAdapterConnected=false`, fallback obrigatorio para o monolito e
  cutover bloqueado. Nao houve rota REST nova, alteracao de
  `consultarCadastro`, BFF/frontend, escrita local ou cutover. A contagem
  regressiva da Fase 69 chega a 0, restando fechamento formal antes de decidir
  qualquer proximo recorte de leitura local de endereco.
- A Fase 69 foi formalmente encerrada apos o commit/push da terceira subfase.
  O bloco fica fechado com diagnostico do contrato de leitura local de
  endereco, porta/DTO internos e adapter JDBC local preparado sobre
  `people_read_model_address`, ainda sem rota REST nova, sem alteracao de
  `consultarCadastro`, sem BFF/frontend, sem escrita local, sem leitura
  operacional de endereco e sem cutover. O adapter permanece como artefato
  preparado, com fallback obrigatorio para o monolito, bloqueio por
  `address-principal-rule-violated` e dependencia de guard/reconciliacao verde
  antes de qualquer ativacao. A proxima fase sugerida e decidir, em recorte
  separado, se o proximo passo sera diagnosticar cutover guardado de leitura
  local de endereco ou escolher outra familia backend.
- A primeira subfase da Fase 70 iniciou o diagnostico backend-only de
  elegibilidade do cutover de leitura local de endereco no `people-service`.
  O planner passou a reportar
  `address_local_read_cutover_eligibility_diagnostic_started`, com fatia minima
  `address_read_cutover_eligibility_diagnostic_no_connection`, mantendo
  `localReadCutoverAllowedNow=false`. O health passou a expor
  `addressLocalReadCutoverEligibilityDiagnostic`, marcando
  `adapterPrepared=true`, `queryServiceConnected=false`, `routeCreated=false`,
  fallback obrigatorio, bloqueio de BFF/frontend e bloqueio de escrita local.
  O diagnostico registrou criterios minimos de guard, blockers antes de qualquer
  conexao operacional, rollback por flags e out-of-scope explicito para rota
  REST, `consultarCadastro`, frontend e write cutover. Nao houve conexao do
  adapter ao fluxo operacional, rota nova, BFF/frontend, escrita local ou
  cutover. A contagem regressiva da Fase 70 passa a 2 subfases restantes
  estimadas.
- A segunda subfase da Fase 70 definiu a operacao interna de roteamento
  `addressLocalRead` no `PeopleLocalReadCutoverGuard`, com candidato
  `people_read_model_address` e shadow route
  `internal-operation:PeopleAddressLocalReadPort`. A operacao ficou isolada de
  `avaliarTodas()` para nao alterar catalogo, identidade ou `consultarCadastro`,
  registra metricas em
  `people.shadow.local.persistence.read.routing.decisions{operation=addressLocalRead}`
  e `people.shadow.local.persistence.address.read.routing.decisions`, mas
  continua selecionando `monolith_proxy` com
  `address-local-read-connection-disabled` mesmo quando o read model geral esta
  verde. O health passou a expor a decisao de `addressLocalRead`, os nomes das
  metricas e `addressReadRoutingDecisionsTotal`, mantendo
  `queryServiceConnected=false`, `routeCreated=false`,
  `localReadCutoverAllowedNow=false` e fallback obrigatorio. Nao houve conexao
  do adapter ao fluxo operacional, rota nova, BFF/frontend, escrita local,
  alteracao de payload ou cutover. A contagem regressiva da Fase 70 passa a 1
  subfase restante estimada.
- A terceira subfase da Fase 70 conectou a leitura local de endereco apenas ao
  fluxo interno guardado. `PeopleLocalReadCutoverGuard` passou a permitir
  `addressLocalRead` como elegivel para `people_read_model_address` somente
  quando o mesmo conjunto de criterios verdes estiver satisfeito: cutover de
  leitura habilitado, fallback ligado, persistencia/backfill/reconciliacao
  ligados, relatorio local concluido, divergencias zeradas e falhas zeradas.
  Foi criado `PeopleAddressLocalReadService`, que chama
  `PeopleAddressLocalReadPort` somente atras do guard e registra
  `people.shadow.local.persistence.address.reads`, retornando vazio para manter
  fallback quando o guard bloqueia, quando nao ha endereco ou quando o adapter
  falha. O health passou a expor `localAddressReadsTotal`,
  `internalGuardedServiceConnected=true` e
  `adapter_connected_to_internal_guard_no_route`, mantendo
  `queryServiceConnected=false`, `routeCreated=false`, sem rota REST, sem
  BFF/frontend, sem alteracao de `consultarCadastro`, sem payload externo novo
  e sem escrita local. O planner passou a recomendar
  `close_phase_70_and_plan_address_write_authority_diagnostic`. A contagem
  regressiva da Fase 70 chega a 0.
- A primeira subfase da Fase 71 iniciou o diagnostico backend-only de autoridade
  de escrita de endereco no `people-service`, sem mover escrita do monolito.
  Foram criados `PeopleAddressWriteAuthorityPlanner` e
  `PeopleAddressWriteAuthorityPlan`, e o health `peopleLocalPersistence` passou
  a expor `addressWriteAuthorityDiagnostic` com estado
  `diagnostic_started_no_write_cutover`. O diagnostico separa as operacoes
  candidatas `create-person-with-principal-address`,
  `update-person-principal-address`,
  `remove-person-address-links-and-orphans` e `cep-lookup-for-address-input`,
  todas com `allowedNow=false`. Foram registrados como autoridades atuais do
  monolito `PessoaFoundationService`, `PessoaEnderecoPort`, repositorios JPA de
  endereco e `ViaCepService`, alem de contratos exigidos, blockers de
  consistencia, rollback e out-of-scope. Nao houve rota REST nova,
  BFF/frontend, escrita local, remocao do caminho do monolito ou alteracao de
  payload. A contagem regressiva da Fase 71 passa a 2 subfases restantes
  estimadas.
- A segunda subfase da Fase 71 definiu o contrato interno de comando de escrita
  de endereco no `people-service`, ainda sem execucao operacional. Foram
  criados `PeopleAddressWritePort`, `PessoaEnderecoWriteCommand`,
  `PessoaEnderecoCleanupCommand` e `PessoaEnderecoWriteResult`, separando
  criacao/atualizacao de endereco principal e cleanup de vinculos/orfaos com
  `commandId`, `pessoaId`, `escolaId`, campos persistidos de endereco,
  `idempotencyKey`, `requestedBy`, `selectedSource`, `persistedLocally` e
  `fallbackRequired`. O planner passou a reportar
  `command_contract_defined_no_write_cutover` e o health passou a expor
  `preparedCommandArtifacts`, mantendo `writeCutoverAllowedNow=false`,
  `adapterCreated=false`, `routeCreated=false` e
  `localPersistenceConnected=false`. Nao houve rota REST, BFF/frontend,
  adapter JDBC de escrita, escrita local, remocao do caminho do monolito ou
  alteracao dos fluxos atuais. A contagem regressiva da Fase 71 passa a 1
  subfase restante estimada.
- A terceira subfase da Fase 71 criou o piloto shadow interno de comando de
  endereco no `people-service`, ainda sem persistencia local e sem rota externa.
  O novo `PeopleAddressWriteShadowService` implementa
  `PeopleAddressWritePort`, recebe comandos de escrita/cleanup, valida
  `commandId`, `pessoaId`, `escolaId` e `idempotencyKey`, registra a metrica
  `people.shadow.local.persistence.address.write.shadow.commands` e devolve
  sempre `selectedSource=monolith_proxy`, `persistedLocally=false` e
  `fallbackRequired=true`. O health `peopleLocalPersistence` passou a expor o
  servico em `preparedCommandArtifacts` e `shadowCommandExecution`, mantendo
  `writeCutoverAllowedNow=false`, `routeCreated=false` e
  `localPersistenceConnected=false`. Nao houve chamada ao monolito, adapter JDBC
  de escrita, migration, BFF/frontend, rota REST ou alteracao dos fluxos atuais.
  A contagem regressiva da Fase 71 chega a 0.
- A primeira subfase da Fase 72 iniciou o diagnostico do adapter
  backend/backend de escrita de endereco para o monolito. Foram criados
  `PeopleAddressWriteMonolithAdapterPlanner` e
  `PeopleAddressWriteMonolithAdapterPlan`, expondo no health
  `addressWriteMonolithAdapterDiagnostic` o estado
  `monolith_http_write_contract_missing_adapter_blocked`. O diagnostico
  confirmou que o monolito ainda possui apenas autoridades Java internas
  (`PessoaFoundationService` e `PessoaEnderecoPort`) para escrita/cleanup de
  endereco, sem contrato HTTP interno para o `people-service` chamar. Foram
  mapeadas as operacoes candidatas `create-or-update-principal-address` e
  `cleanup-person-address-links-and-orphans`, ambas bloqueadas para adapter, e
  definidos os contratos minimos futuros
  `PUT /internal/pessoas/{pessoaId}/endereco-principal` e
  `DELETE /internal/pessoas/{pessoaId}/enderecos`, com `Idempotency-Key` e
  propagacao de contexto interno. Nao houve cliente HTTP de escrita, rota REST
  externa, BFF/frontend, migration, persistencia local, chamada ao monolito ou
  alteracao dos fluxos atuais. A contagem regressiva da Fase 72 passa a 2
  subfases restantes estimadas.
- A segunda subfase da Fase 72 criou no `school-management-service` os
  contratos HTTP internos minimos de escrita/cleanup de endereco:
  `PUT /internal/pessoas/{pessoaId}/endereco-principal` e
  `DELETE /internal/pessoas/{pessoaId}/enderecos`. Os endpoints exigem
  `X-Escola-Id`, `X-Usuario-Id`, `X-Correlation-Id` e `Idempotency-Key`, e
  continuam delegando a autoridade de escrita ao monolito por
  `PessoaEnderecoPort`/`PessoaFoundationService`, incluindo a nova operacao
  `atualizarEnderecoPrincipalDaPessoa`. O diagnostico do `people-service`
  passou para `monolith_http_write_contract_defined_adapter_not_connected`,
  indicando contrato do monolito disponivel, adapter ainda nao conectado,
  `writeCutoverAllowedNow=false` e `localPersistenceAllowedNow=false`. Nao
  houve cliente HTTP de escrita no `people-service`, rota externa, BFF/frontend,
  migration, persistencia local, escrita local ou alteracao dos fluxos atuais.
  A contagem regressiva da Fase 72 passa a 1 subfase restante estimada.
- A terceira subfase da Fase 72 criou o adapter backend/backend
  `MonolithPessoaAddressWriteClient` no `people-service`, implementando
  `PeopleAddressWritePort` para chamar os contratos internos do monolito de
  atualizacao de endereco principal e cleanup de enderecos. O adapter propaga
  `X-Escola-Id`, `X-Usuario-Id`, `X-Correlation-Id` e `Idempotency-Key`, mapeia
  a resposta para `PessoaEnderecoWriteResult` e registra metricas
  `people.shadow.monolith.address.write.requests` e
  `people.shadow.monolith.address.write.failures`. O bean fica atras da flag
  `people.shadow.monolith.address-write-adapter-enabled`, desligada por padrao;
  em falha do monolito, retorna `fallbackRequired=true` sem persistencia local.
  O health passou para
  `monolith_write_adapter_prepared_guard_disabled_no_cutover`. Nao houve rota
  externa, BFF/frontend, migration, persistencia local, ativacao do adapter por
  padrao, escrita local ou remocao dos fluxos atuais do monolito. A contagem
  regressiva da Fase 72 chega a 0.
- A primeira subfase da Fase 73 formalizou no `people-service` o fechamento do
  recorte pessoa/endereco antes da proxima familia. Foi criado no health
  `peopleLocalPersistence` o diagnostico
  `peopleAddressScopeClosureDiagnostic`, consolidando que catalogos, identidade,
  `consultarCadastro` guardado com fallback obrigatorio, contrato interno de
  leitura de endereco e preparacao do adapter backend/backend de escrita ja
  estao fechados para o escopo atual sem exigir ativacao operacional. O
  diagnostico passou a expor `readScopeClosed=true`,
  `writeScopePreparedWithoutCutover=true`, `activationRequiredNow=false` e
  `safeToStartNextFamilyDiagnostic=true`, alem de registrar os bloqueios que
  seguem apenas para fases futuras de ativacao: criacao de pessoa com endereco
  ainda acoplada a transacao do monolito, read model local sem autoridade de
  escrita e guarda `people.shadow.monolith.address-write-adapter-enabled`
  mantida desligada por padrao. A proxima familia candidata minima foi marcada
  como `pessoa_documento`, antes de `funcionario`, enquanto `professor`
  permanece fora deste recorte por ja estar coberto no
  `academic-professor-service`. Nao houve reativacao de endereco, rota externa,
  BFF/frontend, persistencia local autoritativa nem remocao de fluxos atuais do
  monolito. A contagem regressiva da Fase 73 passa a 2 subfases restantes
  estimadas.
- A segunda subfase da Fase 73 abriu o diagnostico minimo de
  `pessoa_documento` no `people-service` sem reabrir `pessoa/endereco`. Foi
  criado no health `peopleLocalPersistence` o diagnostico
  `peopleDocumentScopeDiagnostic`, registrando que o menor recorte seguro e um
  contrato interno read-only de metadados por pessoa, enquanto upload,
  exclusao, cleanup e persistencia autoritativa seguem no monolito. O
  diagnostico expõe `diagnosticReadyNow=true`,
  `internalContractSeparationAllowedNow=true`,
  `localPersistenceAllowedNow=false`,
  `externalRouteChangeAllowedNow=false` e
  `fallbackToCurrentMonolithRequired=true`, alem de mapear como dependencias
  atuais `DocumentoController`, `DocumentoAlunoController`,
  `DocumentoPersistenceGateway`, `DocumentoJpaRepository` e os cleanups em
  `AlunoPersistenceGateway`/`ResponsavelPersistenceGateway`. Tambem ficaram
  formalizados os impactos de consistencia e PII: reconciliacao futura entre
  `pessoa_documento` e `documento`, dependencia de ownership por
  `pessoa.id_escola` e nao ampliacao de campos sensiveis como `numeroDocumento`
  e `caminhoArquivo` nesta subfase. Nao houve rota externa, BFF/frontend,
  storage binario, escrita local nem mudanca funcional no monolito. A contagem
  regressiva da Fase 73 passa a 1 subfase restante estimada.
- A terceira subfase da Fase 73 fechou a primeira preparacao pratica de
  `pessoa_documento` no `people-service`. Foram criados os artefatos internos
  minimos `PessoaDocumentoMetadataLocalReadResponse`,
  `PeopleDocumentMetadataLocalReadPort` e
  `PeopleDocumentMetadataLocalReadService`, limitados a leitura read-only de
  metadados por pessoa e por documento, sem rota externa, sem entidade JPA e
  sem adapter JDBC conectado. O health `peopleLocalPersistence` passou a expor
  `peopleDocumentInternalMetadataReadContractDiagnostic`, registrando
  `contractPrepared=true`, `internalServicePrepared=true`,
  `adapterCreated=false`, `localPersistenceConnected=false`,
  `externalRouteCreated=false` e `fallbackRequired=true`, com payload minimo de
  `id_pessoa_documento`, `id_pessoa`, `id_documento`, `id_tipo_documento`,
  codigos/descricoes de tipo, `numero_documento`, `caminho_arquivo`,
  `observacao` e `data_upload`. O servico interno foi preparado para fallback
  seguro quando ainda nao houver adapter local, registrando apenas metrica
  interna e preservando o monolito como fonte unica para upload, exclusao,
  cleanup e storage binario. Nao houve BFF/frontend, persistencia local,
  migration, rota publica nem alteracao funcional no `school-management-service`.
  A contagem regressiva da Fase 73 chega a 0.
- A primeira subfase da Fase 74 iniciou a nova macrofase backend de
  `pessoa_documento` com diagnostico do candidato local de leitura de
  metadados. Foi criado no health `peopleLocalPersistence` o diagnostico
  `peopleDocumentLocalReadCandidateDiagnostic`, registrando a decisao de seguir
  na familia `pessoa_documento` antes de abrir `funcionario`:
  `continueWithDocumentFamilyNow=true`,
  `switchToFuncionarioNow=false`,
  `schemaDiagnosticAllowedNow=true`,
  `adapterDiagnosticAllowedNow=true` e
  `localReadCutoverAllowedNow=false`. O diagnostico separou como fontes minimas
  `pessoa_documento`, `documento`, `tipo_documento` e `pessoa`, definiu
  `pessoa_documento.id_pessoa_documento` como chave primaria de reconciliacao e
  listou checagens secundarias por `id_pessoa`, `id_documento`,
  `id_tipo_documento`, `data_upload` e `pessoa.id_escola`. Tambem formalizou os
  blockers para seguir: ownership por escola ainda dependente de join com
  `pessoa`, normalizacao de `caminho_arquivo`, e manutencao do monolito como
  autoridade de upload e delete. Nao houve migration, adapter JDBC, rota
  externa, BFF/frontend, escrita local nem alteracao funcional no
  `school-management-service`. A contagem regressiva da Fase 74 passa a 1
  subfase restante estimada.
- A segunda subfase da Fase 74 fechou o diagnostico de schema fisico minimo de
  metadados de `pessoa_documento`. Foi criado no health
  `peopleLocalPersistence` o diagnostico
  `peopleDocumentMetadataSchemaDiagnostic`, registrando
  `migrationAllowedNow=true`, `backfillAllowedNow=true`,
  `localReadAdapterAllowedNow=false` e `localReadCutoverAllowedNow=false`. O
  read model candidato ficou consolidado em uma tabela minima
  `people_documento_read_model` com reconciliacao por
  `id_pessoa_documento` e colunas de metadata/ownership estritamente
  necessarias: ids de pessoa/documento/tipo, codigo/descricao do tipo,
  `numero_documento`, `caminho_arquivo`, `observacao`, `data_upload`,
  `id_escola` e `created_at`. Tambem ficaram formalizados a migration opt-in
  `V5__create_people_document_metadata_read_model.sql`, o backfill futuro de
  `monolith_jdbc` para o read model local e os blockers antes de qualquer
  adapter: drift de `tipo_documento`, definicao de normalizacao de
  `caminho_arquivo`, documentos orfaos fora do modelo e ownership divergente por
  `pessoa.id_escola`. Nao houve migration executada, adapter JDBC, rota
  externa, BFF/frontend, escrita local ou alteracao funcional no
  `school-management-service`. A contagem regressiva da Fase 74 chega a 0.
- A primeira subfase da Fase 75 materializou a primeira preparacao concreta do
  adapter local de metadados de `pessoa_documento` no `people-service`. Foi
  criado o adapter JDBC `JdbcPeopleDocumentMetadataLocalReadAdapter`, ligado ao
  contrato interno `PeopleDocumentMetadataLocalReadPort`, lendo
  `people_documento_read_model` por `id_escola` e mantendo fallback seguro
  quando nao houver schema local configurado. Tambem foi criada a migration
  opt-in `V5__create_people_document_metadata_read_model.sql` e o health
  `peopleLocalPersistence` passou a expor o diagnostico
  `peopleDocumentMetadataLocalAdapterPreparationDiagnostic`, registrando
  `adapterPrepared=true`, `internalServiceConnected=true`,
  `externalRouteCreated=false` e `localReadCutoverAllowedNow=false`. O
  diagnostico de schema foi atualizado para refletir que o adapter esta
  preparado, mas ainda sem ativacao, sem backfill executado, sem reconciliacao
  verde e sem rota externa. Nao houve BFF/frontend, upload local, delete local
  nem alteracao funcional no `school-management-service`. A contagem regressiva
  da Fase 75 chega a 0.
- A primeira subfase da Fase 76 estendeu o ciclo opt-in de
  backfill/reconciliacao do `people-service` para
  `people_documento_read_model`. O sync JDBC passou a ler do monolito via join
  entre `pessoa_documento`, `documento`, `tipo_documento` e `pessoa`, mantendo
  `id_pessoa_documento` como chave idempotente e de reconciliacao. A
  reconciliacao compara pessoa/documento/tipo, codigo/descricao do tipo,
  `numero_documento`, `caminho_arquivo`, `observacao`, `data_upload` e
  `id_escola`, e o relatorio bloqueia quando a origem trouxer o mesmo
  `id_documento` repetido em mais de um vinculo, com motivo
  `document-metadata-duplicate-document-id-in-source`. O health
  `peopleLocalPersistence` passou a expor
  `peopleDocumentBackfillReconciliationDiagnostic`, registrando
  `backfillAllowedNow=true`, `reconciliationAllowedNow=true` e
  `localReadCutoverAllowedNow=false`. Nao houve ativacao de leitura local,
  BFF/frontend, rota externa, escrita local, upload local, delete local ou
  cutover. A contagem regressiva da Fase 76 chega a 0.
- A primeira subfase da Fase 77 conectou `PeopleDocumentMetadataLocalReadService`
  ao guard interno do `people-service`, preservando fallback obrigatorio e sem
  criar rota REST. `PeopleLocalReadCutoverGuard` passou a expor a operacao
  `documentMetadataLocalRead`, source candidata `people_documento_read_model` e
  metrica dedicada de roteamento. O actuator `peopleLocalPersistence` passou a
  publicar `peopleDocumentLocalReadActivationEligibilityDiagnostic`, mostrando
  fase/status, precondicoes do guard, source selecionada e rollback. Nenhum
  contrato externo foi alterado, `PessoaQueryService` nao foi conectado e a
  contagem regressiva da Fase 77 chega a 0.
- A primeira subfase da Fase 78 fechou o diagnostico do consumidor interno
  minimo de `pessoa_documento`. A conclusao foi que o `people-service` ainda nao
  possui fluxo interno documental real alem da observabilidade e do proprio
  adapter local, enquanto upload/listagem/cleanup continuam no
  `school-management-service`. O actuator `peopleLocalPersistence` passou a
  expor `peopleDocumentInternalUsageCandidateDiagnostic`, registrando que nao ha
  consumidor interno seguro neste ponto sem abrir rota, mexer em
  `consultarCadastro` ou criar acoplamento artificial. A fase termina sem
  conexao funcional nova e com a contagem regressiva da Fase 78 em 0.
- A primeira subfase da Fase 79 abriu o diagnostico minimo de `funcionario` no
  `people-service`. O actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioScopeDiagnostic`, registrando que o primeiro recorte seguro
  e somente um resumo interno read-only por escola, enquanto elegibilidade de
  professor, autenticacao, cargo, usuario e writes de funcionario permanecem no
  monolito. A fase termina sem rota nova, sem persistencia local e sem alteracao
  funcional no `school-management-service`, com a contagem regressiva da Fase 79
  em 0.
- A primeira subfase da Fase 80 preparou a fronteira interna minima de resumo de
  `funcionario` no `people-service`, com DTO, port e service read-only ainda sem
  adapter local e sem rota. O actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalSummaryContractDiagnostic`, registrando que o
  contrato interno cobre apenas `id_funcionario`, `id_pessoa`, `id_escola`,
  `nome_completo`, `cargo_descricao` e `ativo`, preservando no monolito as
  regras de professor, autenticacao e qualquer write de funcionario. A fase
  termina sem alteracao funcional no `school-management-service`, com a
  contagem regressiva da Fase 80 em 0.
- A primeira subfase da Fase 81 preparou o adapter local minimo do resumo de
  `funcionario` no `people-service`, com migration V6 do read model e adapter
  JDBC isolado para `people_funcionario_read_model`, ainda sem backfill,
  reconciliacao, rota externa ou cutover. O actuator `peopleLocalPersistence`
  passou a expor `peopleFuncionarioInternalSummaryAdapterPreparationDiagnostic`,
  deixando explicito que professor, autenticacao e writes de funcionario
  continuam preservados no monolito. A fase termina sem alteracao funcional no
  `school-management-service`, com a contagem regressiva da Fase 81 em 0.
- A primeira subfase da Fase 82 preparou o backfill e a reconciliacao minima de
  `funcionario_internal_summary` no `people-service`, incluindo a tabela
  `people_funcionario_read_model` no pipeline local de sincronizacao JDBC e no
  coordenador de backfill, ainda sem ativacao de leitura local. O actuator
  `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalSummaryBackfillReconciliationDiagnostic`,
  registrando fonte, alvo, chave de reconciliacao e bloqueadores de
  consistencia enquanto RH, professor e autenticacao continuam preservados no
  monolito. A fase termina sem alteracao funcional no
  `school-management-service`, com a contagem regressiva da Fase 82 em 0.
- A primeira subfase da Fase 83 preparou a elegibilidade de leitura local
  interna de `funcionario_internal_summary` no `people-service`, conectando o
  `PeopleFuncionarioInternalSummaryService` ao guard de leitura local com
  fallback obrigatorio para `monolith_internal_rh` e metricas dedicadas, ainda
  sem rota externa e sem consumidor novo. O actuator `peopleLocalPersistence`
  passou a expor
  `peopleFuncionarioInternalSummaryLocalReadActivationEligibilityDiagnostic`,
  deixando explicito quando o read model pode ser considerado verde para uso
  interno controlado. A fase termina sem alteracao funcional oficial no
  `school-management-service`, com a contagem regressiva da Fase 83 em 0.
- A primeira subfase da Fase 84 fechou o diagnostico de consumidor interno de
  `funcionario_internal_summary` no `people-service`, confirmando que ainda nao
  existe fluxo nativo que justifique conectar esse read local fora da propria
  preparacao tecnica. O actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalUsageCandidateDiagnostic`, registrando a ausencia de
  consumidor seguro sem abrir nova rota ou ampliar escopo para RH/autenticacao.
  A fase termina sem alteracao funcional oficial no
  `school-management-service`, com a contagem regressiva da Fase 84 em 0.
- A primeira subfase da Fase 85 consolidou o fechamento formal do bloco de
  `funcionario_internal_summary` no `people-service`, registrando como fechadas
  neste estagio as capacidades de contrato, adapter, schema, backfill,
  reconciliacao, guard e diagnostico de ausencia de consumidor interno real. O
  actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioScopeClosureDiagnostic`, deixando explicito que a evolucao
  seguinte deve sair para outra familia backend sem reabrir esse bloco agora. A
  fase termina sem alteracao funcional oficial no
  `school-management-service`, com a contagem regressiva da Fase 85 em 0.
- A primeira subfase da Fase 86 consolidou o fechamento formal do bloco de
  metadados de `pessoa_documento` no `people-service`, registrando como
  fechadas neste estagio as capacidades de diagnostico de escopo, contrato
  interno, candidato local de leitura, schema, adapter JDBC,
  backfill/reconciliacao, guard e diagnostico de ausencia de consumidor interno
  real. O actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentScopeClosureDiagnostic`, deixando explicito que a evolucao
  seguinte deve sair para outra familia backend sem reabrir esse bloco agora. A
  fase termina sem alteracao funcional oficial no
  `school-management-service`, com a contagem regressiva da Fase 86 em 0.
- A primeira subfase da Fase 87 abriu a nova macrofase backend no
  `school-management-service` pelo menor consumidor real de documentos. O
  diagnostico formalizou em actuator dedicado que
  `DocumentoAlunoService.listarPorAluno` e o primeiro recorte seguro para futura
  fronteira com o `people-service`, porque permanece no agregado de aluno,
  reaproveita a resolucao `alunoId -> pessoaId` e nao exige mover upload,
  exclusao ou o fluxo generico `/api/documentos`. A fase termina sem alteracao
  funcional oficial e com a contagem regressiva da Fase 87 em 2 subfases
  restantes estimadas.
- A primeira subfase da Fase 92 criou exclusivamente no `people-service` a
  fronteira interna minima de contato de pessoa (`email` e `telefone`) sobre o
  read model local ja existente de `pessoa`, sem rota externa e sem tocar o
  legado. Foram criados `PeopleContactLocalReadPort`,
  `PessoaContatoLocalReadResponse`, `PeopleContactLocalReadService` e
  `JdbcPeopleContactLocalReadAdapter`, com diagnostico novo no actuator
  `peopleContactLocalReadPreparationDiagnostic`. A contagem regressiva da Fase
  92 passa a 1 subfase restante estimada.
- A primeira subfase da Fase 93 fechou o diagnostico do consumidor interno de
  contato exclusivamente no `people-service`, formalizando que ainda nao existe
  consumidor interno seguro para essa fronteira sem ampliar `PessoaQueryService`
  ou mudar rotas externas. Foi criado o planner
  `PeopleContactInternalUsageCandidatePlanner` e o actuator
  `peopleLocalPersistence` passou a expor
  `peopleContactInternalUsageCandidateDiagnostic`. A contagem regressiva da
  Fase 93 chega a 0.
- A primeira subfase da Fase 94 consolidou o fechamento formal do bloco de
  contato no `people-service`, registrando como fechadas neste estagio as
  capacidades de contrato, service, adapter e diagnostico de ausencia de
  consumidor interno real. O actuator `peopleLocalPersistence` passou a expor
  `peopleContactScopeClosureDiagnostic`, deixando explicito que a evolucao
  seguinte deve sair para outra familia backend sem reabrir esse bloco agora.
  A contagem regressiva da Fase 94 chega a 0.
- A primeira subfase da Fase 95 abriu a nova familia de `professor` no
  `people-service` apenas por diagnostico, sem criar rota, migration,
  persistencia local ou cutover. O planner
  `PeopleProfessorScopeDiagnosticPlanner` e o actuator
  `peopleProfessorScopeDiagnostic` formalizaram que o menor passo seguro agora
  e um contrato interno minimo read-only de resumo de professor por escola,
  preservando no monolito as dependencias atuais de `funcionario`, `pessoa`,
  autenticacao e alocacao academica. A contagem regressiva da Fase 95 passa a
  2 subfases restantes estimadas.
- A segunda subfase da Fase 95 materializou esse contrato interno minimo de
  `professor` no codigo novo do `people-service`, ainda sem adapter, sem rota e
  sem persistencia local. Foram criados `PessoaProfessorInternalSummaryResponse`,
  `PeopleProfessorInternalSummaryPort`,
  `PeopleProfessorInternalSummaryService` e
  `PeopleProfessorInternalSummaryContractPlanner`, enquanto o actuator passou a
  expor `peopleProfessorInternalSummaryContractDiagnostic`. A contagem
  regressiva da Fase 95 passa a 1 subfase restante estimada.
- A terceira subfase da Fase 95 fechou o bloco inicial de `professor` com o
  diagnostico formal do primeiro adapter/local read, ainda sem cria-lo. O
  planner `PeopleProfessorInternalSummaryAdapterPreparationPlanner` e o
  actuator `peopleProfessorInternalSummaryAdapterPreparationDiagnostic`
  registraram que a familia terminou esta macrofase apenas com fronteira
  interna preparada, sem migration, sem adapter JDBC, sem rota e sem
  persistencia propria. A contagem regressiva da Fase 95 chega a 0.
- A primeira subfase da Fase 96 abriu a nova familia de vinculos base de
  `aluno` e `responsavel` no `people-service`, sem criar rota, migration ou
  persistencia adicional. O planner
  `PeopleStudentResponsibleLinkScopeDiagnosticPlanner` e o actuator
  `peopleStudentResponsibleLinkScopeDiagnostic` formalizaram que os lookups
  locais `alunoId -> pessoaId` e `responsavelId -> pessoaId` ja existem no
  codigo novo, mas ainda sem fechamento explicito como macrofase propria. A
  contagem regressiva da Fase 96 passa a 1 subfase restante estimada.
- A segunda subfase da Fase 96 fechou formalmente essa familia de vinculos base
  de `aluno` e `responsavel` no `people-service`. O planner
  `PeopleStudentResponsibleLinkScopeClosurePlanner` e o actuator
  `peopleStudentResponsibleLinkScopeClosureDiagnostic` registraram que a
  familia termina esta macrofase apenas como fronteira interna preparada, sem
  abrir `parentesco`, `status_aluno`, rota externa ou persistencia propria
  adicional. A contagem regressiva da Fase 96 chega a 0.
- A primeira subfase da Fase 97 abriu a familia restante de catalogos base de
  `aluno/responsavel` no `people-service` pelo menor recorte concreto. A
  migration opt-in
  `V7__create_people_student_responsible_catalog_read_model.sql` adicionou as
  tabelas locais `status_aluno` e `parentesco`; o ciclo JDBC de sync foi
  estendido para esses dois catalogos; e `PessoaCatalogoPort` com
  `JdbcPessoaCatalogoAdapter` passaram a expor `listarStatusAluno()` e
  `listarParentescos()`. A entrega permaneceu estritamente backend-only, sem
  rota externa, sem BFF e sem alteracao no `school-management-service`. A
  contagem regressiva da Fase 97 passa a 1 subfase restante estimada.
- A primeira subfase da Fase 98 retomou a familia de `professor` pelo primeiro
  recorte concreto ainda faltante no `people-service`. A migration opt-in
  `V8__create_people_professor_read_model.sql` adicionou a tabela local
  `people_professor_read_model`; o ciclo JDBC de sync foi estendido para
  reconciliar esse resumo por `id_professor` a partir de `professor`, `pessoa`
  e `funcionario`; e o adapter `JdbcPessoaProfessorResumoAdapter` passou a
  implementar `PessoaProfessorResumoPort` no schema local. Migration state,
  sync coordinator, health e testes foram atualizados sem criar rota externa,
  sem BFF e sem alteracao no `school-management-service`. A contagem
  regressiva da Fase 98 passa a 1 subfase restante estimada.
- A segunda subfase da Fase 98 conectou o primeiro consumidor interno real do
  resumo de `professor` no `people-service`. `PessoaProfessorResumoService`
  passou a consultar `PeopleReadSourcePolicy` antes de acessar
  `PessoaProfessorResumoPort`; a policy passou a reconhecer a operacao
  `professorResumo`, liberando `people_professor_read_model` apenas com
  backfill/reconciliacao verdes e fallback para `monolith_internal_rh` quando
  a leitura local nao estiver elegivel; e os testes foram estendidos para
  cobrir guard, liberacao e metricas. A contagem regressiva da Fase 98 chega a
  0.
- A primeira subfase da Fase 99 fechou o menor recorte remanescente de leitura
  interna local de `responsavel` no `people-service`. `ResponsavelPessoaService`
  passou a consultar `PeopleReadSourcePolicy` antes de acessar
  `ResponsavelPessoaPort`; a policy passou a reconhecer a operacao
  `responsavelVinculo`, liberando o bloco
  `people_read_model_student_responsible` apenas com backfill/reconciliacao
  verdes; e health/testes foram estendidos para cobrir bloqueio, adapter
  ausente, caminho liberado e metricas internas. A contagem regressiva da Fase
  99 chega a 0.
- A primeira subfase da Fase 100 fechou a simetria do bloco base de `aluno` no
  `people-service`. `AlunoPessoaService` passou a consultar
  `PeopleReadSourcePolicy` antes de acessar `AlunoPessoaPort`; a policy passou
  a reconhecer a operacao `alunoVinculo`, liberando o bloco
  `people_read_model_student_responsible` apenas com backfill/reconciliacao
  verdes; e health/testes foram estendidos para cobrir bloqueio, adapter
  ausente, caminho liberado e metricas internas. A contagem regressiva da Fase
  100 chega a 0.
- A primeira subfase da Fase 101 reabriu de forma controlada o bloco de
  `contato` no `people-service`. `PessoaContatoService` passou a consultar
  `PeopleReadSourcePolicy` antes de acessar `PessoaContatoPort`; a policy
  passou a reconhecer a operacao `contato`, liberando a leitura local sobre o
  read model de `pessoa` apenas com backfill/reconciliacao verdes; e
  health/testes foram estendidos para cobrir bloqueio, adapter ausente,
  caminho liberado e metricas internas. A contagem regressiva da Fase 101
  chega a 0.
- A primeira subfase da Fase 102 fechou a camada de aplicacao dos catalogos
  base de `aluno/responsavel` no `people-service`. Foi criado
  `PessoaAlunoResponsavelCatalogoService`, reaproveitando `PessoaCatalogoPort`
  para expor internamente `listarStatusAluno()` e `listarParentescos()` com
  guard de elegibilidade e metricas; `PeopleReadSourcePolicy` passou a
  reconhecer explicitamente essas duas operacoes como catalogos locais do
  `people_read_model_catalog`; e os testes foram estendidos para cobrir
  bloqueio, adapter ausente, falha local e sucesso. A contagem regressiva da
  Fase 102 chega a 0.

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

- A Fase 88 retomou a evolucao exclusivamente no codigo novo do
  `people-service`, sem qualquer alteracao no `school-management-service`,
  formalizando o contrato do primeiro consumidor futuro de metadados de
  documento por aluno (`documento_aluno_listar_por_aluno`) e publicando esse
  diagnostico no actuator `peopleLocalPersistence`.
- O novo diagnostico deixa explicito que `PeopleDocumentMetadataLocalReadService`
  e reutilizavel pelo lado novo, mas que a conexao real ainda depende de uma
  estrategia interna de resolucao `alunoId -> pessoaId`, mantendo fallback
  obrigatorio para `monolith_proxy` e sem abrir rota nova nesta etapa.
- Contagem da macrofase Fase 88: 1 subfase restante estimada para decidir a
  estrategia de conexao desse consumidor inteiramente no `people-service`,
  ainda sem tocar o legado.
- A Fase 89 fechou essa subfase restante exclusivamente no `people-service`,
  criando a fronteira interna `PeopleStudentPessoaLocalReadPort`, o service
  `PeopleStudentPessoaLocalReadService` e o adapter
  `JdbcPeopleStudentPessoaLocalReadAdapter` para resolver `alunoId -> pessoaId`
  sobre o read model local ja existente.
- O actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentAlunoConsumerConnectionStrategyDiagnostic`, deixando explicito
  que a resolucao local necessaria ao futuro consumidor de documentos esta
  preparada, mas ainda sem conexao a fluxo real e sem qualquer alteracao no
  `school-management-service`.
- Contagem da macrofase Fase 89: 0 subfases restantes estimadas. O bloco do
  consumidor futuro de documento por aluno fica fechado no codigo novo, pronto
  para eventual reutilizacao futura.
- A Fase 90 abriu o recorte simetrico de criacao para responsavel no
  `people-service`, formalizando o contrato do futuro consumidor
  `documento_responsavel_listar_por_responsavel` e publicando esse estado no
  actuator `peopleLocalPersistence`, ainda sem rota nova e sem qualquer toque
  no `school-management-service`.
- A Fase 91 fechou a subfase restante desse bloco, criando a fronteira interna
  `PeopleResponsiblePessoaLocalReadPort`, o service
  `PeopleResponsiblePessoaLocalReadService` e o adapter
  `JdbcPeopleResponsiblePessoaLocalReadAdapter` para resolver
  `responsavelId -> pessoaId` sobre o read model local.
- O actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentResponsavelConsumerConnectionStrategyDiagnostic`, deixando
  explicito que a resolucao local necessaria ao futuro consumidor de documentos
  de responsavel esta preparada, mas ainda sem conexao a fluxo real e sem
  qualquer alteracao no `school-management-service`.
- Contagem da macrofase Fase 91: 0 subfases restantes estimadas. O bloco do
  consumidor futuro de documento por responsavel fica fechado no codigo novo,
  pronto para eventual reutilizacao futura.

## Diretriz para documentacao futura

- Nao criar um arquivo novo por fase de implementacao.
- Atualizar este consolidado somente quando houver decisao tecnica relevante ou fechamento de um bloco grande de trabalho.
- Para documentacao de usuario, criar futuramente manuais por perfil padrao, quando os perfis estiverem definidos.
- Para mudancas de codigo, priorizar implementacao e validacao; documentacao deve ser pontual e util.

### Fase 103

- O `people-service` passou a expor no contrato interno as consultas locais de
  `status_aluno` e `parentesco`, ligando `PessoaQueryUseCase`,
  `PessoaQueryService` e `PessoaInternalQueryController` aos dois catalogos ja
  preparados na camada nova.
- Foram abertas as rotas internas
  `GET /internal/v1/pessoas/catalogos/status-aluno` e
  `GET /internal/v1/pessoas/catalogos/parentescos`, sem mudanca em BFF,
  frontend ou legado.
- A validacao foi ampliada com testes de servico e integracao HTTP para
  confirmar resposta do read model local nesses dois endpoints.

### Fase 104

- O `people-service` passou a expor no contrato interno a leitura local de
  endereco por pessoa, reaproveitando o bloco ja implementado em
  `PessoaEnderecoService`.
- Foram abertas as rotas internas
  `GET /internal/v1/pessoas/{id}/endereco-principal` e
  `GET /internal/v1/pessoas/{id}/enderecos`, sem alteracao em BFF, frontend ou
  legado.
- A validacao foi ampliada com testes de servico e integracao HTTP cobrindo o
  contrato interno desse recorte.

### Fase 105

- O `people-service` passou a expor no contrato interno a leitura local de
  contato por pessoa, reaproveitando `PessoaContatoService`.
- Foi aberta a rota interna `GET /internal/v1/pessoas/{id}/contato`, sem
  alteracao em BFF, frontend ou legado.
- A validacao foi ampliada com testes de servico e integracao HTTP desse
  contrato interno.

### Fase 106

- O `people-service` passou a expor no contrato interno a leitura de metadata
  de documentos, reaproveitando `PessoaDocumentoMetadataService`.
- Foram abertas as rotas internas `GET /internal/v1/pessoas/{id}/documentos` e
  `GET /internal/v1/documentos/{documentoId}`, sem alteracao em BFF, frontend
  ou legado.
- A validacao foi ampliada com testes de servico e integracao HTTP desse
  contrato interno.

### Fase 107

- O `people-service` passou a expor no contrato interno o resumo de
  funcionario, reaproveitando `PessoaFuncionarioResumoService`.
- Foram abertas as rotas internas `GET /internal/v1/funcionarios/{funcionarioId}`
  e `GET /internal/v1/funcionarios`, sem alteracao em BFF, frontend ou legado.
- A validacao foi ampliada com testes de servico e integracao HTTP desse
  contrato interno.

### Fase 108

- O `people-service` passou a expor no contrato interno o resumo de professor,
  reaproveitando `PessoaProfessorResumoService`.
- Foram abertas as rotas internas `GET /internal/v1/professores/{professorId}`
  e `GET /internal/v1/professores`, sem alteracao em BFF, frontend ou legado.
- A validacao foi ampliada com testes de servico e integracao HTTP desse
  contrato interno.
