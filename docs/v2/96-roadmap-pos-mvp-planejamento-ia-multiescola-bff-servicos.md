# Roadmap pos-MVP - BFF, servicos por dominio e plataforma distribuida

## Objetivo arquitetural

Evoluir o backend atual para uma arquitetura distribuida orientada a dominios,
sem interromper o sistema existente. A arquitetura-alvo inclui:

- BFF orquestrador como unica fachada dos frontends;
- servicos Spring Boot implantaveis separadamente por dominio;
- PostgreSQL como fonte oficial dos dados transacionais;
- Kafka para integracao assincrona por eventos;
- MongoDB para conteudo flexivel, historico rico e projecoes documentais;
- Redis para cache, contexto curto, idempotencia, locks e rate limiting;
- isolamento por escola em APIs, eventos, persistencia e observabilidade;
- migracao incremental pelo padrao strangler, mantendo o monolito operacional.

Kafka, MongoDB e Redis fazem parte da arquitetura-alvo. Isso nao significa que
todo servico deva usar as tres tecnologias. Cada uso precisa ter propriedade,
consistencia, retencao e comportamento de falha definidos.

## Diagnostico da baseline em 19 de junho de 2026

### Runtime e infraestrutura

- Existe um unico runtime Spring Boot: `school-management-service`.
- Java 21 e Spring Boot 3.5.14.
- Uma unica conexao PostgreSQL para `gestao_escolar`.
- JPA, Flyway e 73 entidades/tabelas mapeadas no mesmo runtime.
- 43 controllers e 171 operacoes HTTP declaradas.
- Nao existem dependencias de Kafka, MongoDB ou Redis no `pom.xml` atual.
- Os unicos processamentos agendados locais sao limpeza de sessoes e geracao de
  snapshots de dashboard.
- A unica chamada HTTP externa identificada e o ViaCEP por `RestClient`.
- Arquivos de documentos sao armazenados localmente por uma porta de storage.
- O frontend chama diretamente o monolito e propaga apenas o bearer token.
- O token atual e opaco, persistido por hash em `sessao_autenticacao`; ele nao e
  um JWT autocontido apesar da nomenclatura residual de configuracao.

### Modulos internos encontrados

O monolito ja possui agrupamentos uteis, mas eles nao sao bounded contexts
isolados. Existem imports diretos, relacionamentos JPA e consultas cruzadas
entre `aluno`, `avaliacao`, `catalogo`, `dashboard`, `documento`, `frequencia`,
`historico`, `ia`, `institucional`, `matricula`, `planejamento`, `professor`,
`responsavel`, `rh`, `seguranca` e `transferencia`.

Os acoplamentos mais relevantes para a extracao sao:

- dashboard consulta quase todos os dominios transacionais diretamente;
- matricula depende de aluno, catalogo, documento, frequencia, avaliacao e
  historico;
- professor/aula depende de catalogo, pessoa, RH, matricula, frequencia,
  planejamento e seguranca;
- planejamento e avaliacao compartilham entidades de professor/turma;
- IA referencia planejamento, professor, catalogo, seguranca e escola;
- handlers compartilhados conhecem excecoes de quase todos os modulos;
- entidades JPA possuem relacionamentos entre pacotes que futuramente serao
  servicos diferentes.

Consequencia: mover pacotes para novos executaveis nao e suficiente. Antes de
cada extracao, relacionamentos JPA externos devem virar IDs de referencia e
portas HTTP/eventos com contratos explicitos.

## Principios obrigatorios

1. Dependencias apontam para dentro: `interfaces/infra -> application -> domain`.
2. `domain` nao depende de Spring, JPA, Kafka, MongoDB, Redis ou HTTP.
3. `application` orquestra casos de uso e declara portas de entrada e saida.
4. `infra` implementa persistencia, mensageria, cache, clientes e seguranca.
5. `interfaces` adapta REST, validacao e tratamento de erros.
6. Entidade de dominio nao e entidade JPA nem DTO REST.
7. Nenhum servico consulta o banco de outro servico.
8. Nenhum servico compartilha entidades JPA ou repositorios Spring Data.
9. Referencias externas ao agregado sao IDs, nunca relacionamentos ORM remotos.
10. Eventos de integracao sao imutaveis, versionados e diferentes dos eventos
    internos de dominio.
11. Publicacao Kafka transacional usa outbox; consumidores sao idempotentes.
12. Nao usar transacao distribuida. Fluxos longos usam saga/orquestracao e
    compensacao explicita.
13. Toda API e evento carrega `correlationId`, `usuarioId` e `escolaId` quando
    aplicavel.
14. O BFF nao acessa bancos nem concentra regra de dominio.
15. Redis e cache descartavel, nunca fonte oficial.
16. MongoDB nao substitui PostgreSQL em invariantes transacionais.
17. Contratos externos sao versionados e retrocompativeis durante a migracao.

## Estrutura DDD obrigatoria por servico

```text
<nome-do-servico>/
|-- pom.xml
`-- src/
    |-- main/
    |   |-- java/br/com/escola/<servico>/
    |   |   |-- domain/
    |   |   |   |-- model/
    |   |   |   |-- valueobject/
    |   |   |   |-- aggregate/
    |   |   |   |-- service/
    |   |   |   |-- event/
    |   |   |   `-- repository/
    |   |   |-- application/
    |   |   |   |-- usecase/
    |   |   |   |-- service/
    |   |   |   |-- dto/
    |   |   |   |-- mapper/
    |   |   |   `-- port/
    |   |   |       |-- in/
    |   |   |       `-- out/
    |   |   |-- infra/
    |   |   |   |-- config/
    |   |   |   |-- database/
    |   |   |   |-- repository/
    |   |   |   |-- messaging/
    |   |   |   |-- webclient/
    |   |   |   |-- cache/
    |   |   |   |-- observability/
    |   |   |   `-- security/
    |   |   `-- interfaces/
    |   |       |-- rest/
    |   |       |-- request/
    |   |       |-- response/
    |   |       |-- exception/
    |   |       `-- advice/
    |   `-- resources/
    |       |-- application.yml
    |       |-- logback-spring.xml
    |       `-- db/migration/
    `-- test/
        |-- java/
        `-- resources/
```

O BFF segue as mesmas direcoes de dependencia, mas seu dominio e de composicao
de experiencias, nao uma copia dos dominios escolares.

## Arvore-alvo do repositorio

O estado final desejado e um monorepo backend com multiplos runtimes Spring
Boot independentes, cada um com seu proprio `pom.xml`, `src/`, contrato e
persistencia. O frontend continua separado e o monolito atual permanece durante
o strangler ate que os fluxos sejam extraidos de forma segura.

```text
projectc-school-management/
|-- pom.xml
|-- README.md
|-- docs/
|   `-- v2/
|-- scripts/
|-- platform/
|   |-- compose/
|   |-- docker/
|   |-- observability/
|   `-- runtime/
|-- school-management-bff/
|   |-- pom.xml
|   `-- src/
|-- identity-access-service/
|   |-- pom.xml
|   `-- src/
|-- institutional-tenant-service/
|   |-- pom.xml
|   `-- src/
|-- academic-catalog-service/
|   |-- pom.xml
|   `-- src/
|-- people-service/
|   |-- pom.xml
|   `-- src/
|-- enrollment-document-service/
|   |-- pom.xml
|   `-- src/
|-- pedagogical-service/
|   |-- pom.xml
|   `-- src/
|-- planning-ai-service/
|   |-- pom.xml
|   `-- src/
|-- dashboard-query-service/
|   |-- pom.xml
|   `-- src/
|-- school-management-service/
|   |-- pom.xml
|   `-- src/
`-- school-management-web/
```

### Papel de cada pasta raiz

- `pom.xml`: parent Maven do monorepo, concentrando versoes, plugins e modulos.
- `docs/v2`: fonte da verdade arquitetural e historica da migracao.
- `scripts`: automacoes de suporte, sem conter regra de negocio.
- `platform`: infraestrutura local e operacional do monorepo.
- `school-management-bff`: fachada unica dos frontends e orquestrador curto.
- `identity-access-service`: autenticacao, usuarios, perfis, permissoes e sessoes.
- `institutional-tenant-service`: escolas, vinculos usuario-escola e tenant
  ativo.
- `academic-catalog-service`: catalogo academico reutilizavel.
- `people-service`: pessoas e papeis institucionais base.
- `enrollment-document-service`: matriculas, documentos e transferencia.
- `pedagogical-service`: execucao academica oficial.
- `planning-ai-service`: planejamento docente e IA.
- `dashboard-query-service`: modelos de leitura, indicadores e snapshots.
- `school-management-service`: monolito legado em esvaziamento progressivo.
- `school-management-web`: frontend, sem ser o foco da evolucao atual.

## Arvore-alvo de plataforma

`platform/` existe para evitar espalhar detalhes operacionais por cada modulo.
Ela nao concentra regra de negocio e nao substitui a configuracao interna dos
servicos.

```text
platform/
|-- compose/
|   |-- docker-compose.yml
|   `-- overrides/
|-- docker/
|   |-- bff/
|   |-- academic-catalog/
|   |-- kafka/
|   |-- postgres/
|   `-- redis/
|-- observability/
|   |-- prometheus/
|   |-- grafana/
|   `-- dashboards/
`-- runtime/
    |-- logs/
    |-- reports/
    `-- temp/
```

Uso esperado:

- `compose/`: sobe dependencias e, quando necessario, runtimes locais.
- `docker/`: Dockerfiles e assets de empacotamento por servico.
- `observability/`: configuracoes de Prometheus, Grafana e dashboards.
- `runtime/`: artefatos efemeros locais, como logs e relatorios de migracao;
  nao faz parte do contrato de codigo de negocio.

## Topologia alvo e propriedade de dados

### `school-management-bff`

Responsavel por autenticar a requisicao, resolver e propagar contexto, compor
respostas, executar orquestracoes curtas e aplicar timeout, retry seletivo,
circuit breaker e rate limit. Nao possui tabelas de negocio.

Durante o strangler, ele roteia endpoints ainda nao extraidos para o monolito e
endpoints migrados para os novos servicos sem alterar imediatamente os
frontends.

### `identity-access-service`

Responsabilidades: autenticacao, usuarios, perfis, permissoes, tokens e sessoes.

Tabelas de propriedade:

- `usuario`, `perfil`, `permissao`;
- `usuario_perfil`, `perfil_permissao`;
- `sessao_autenticacao`.

### `institutional-tenant-service`

Responsabilidades: escolas, vinculos usuario-escola, escola ativa e autorizacao
de acesso ao tenant. A tabela atual `escola` inicia este contexto; o vinculo
usuario-escola precisa deixar de ser uma relacao implicita de uma unica escola.

Tabelas de propriedade:

- `escola`;
- futura `usuario_escola`, com migracao explicita a partir do modelo atual.

### `academic-catalog-service`

Responsabilidades: estrutura academica reutilizada pelos demais dominios.

Tabelas de propriedade:

- `nivel_ensino`, `periodo_letivo`, `periodo_avaliativo`;
- `serie`, `turno`, `turma`;
- `disciplina`, `turma_disciplina`.

### `people-service`

Responsabilidades: cadastro base de pessoas e seus papeis institucionais. Nao
possui matriculas, aulas ou autenticacao.

Tabelas de propriedade:

- `pessoa`, `tipo_pessoa`, `pessoa_tipo_pessoa`;
- `endereco`, `tipo_endereco`, `pessoa_endereco`;
- `pessoa_documento`, `tipo_documento`;
- `aluno`, `status_aluno`, `responsavel`, `parentesco`, `aluno_responsavel`;
- `professor`, `funcionario`, `cargo`.

`professor.usuarioId` e uma referencia externa ao identity service; nao deve
permanecer como relacionamento JPA entre bancos.

### `enrollment-document-service`

Responsabilidades: matricula, etapas, documentos administrativos, transferencia
e ciclo administrativo do aluno.

Tabelas de propriedade:

- `matricula`, `tipo_matricula`, `status_matricula`;
- `matricula_etapa`, `etapa_matricula_modelo`, `status_etapa_matricula`;
- `matricula_documento_entregue`, `matricula_documento_exigido`;
- `documento`;
- `transferencia_aluno`, `tipo_transferencia`, `status_transferencia`;
- `aluno_historico_evento`, `tipo_evento_aluno`;
- `solicitacao_exclusao_aluno`.

O binario do documento deve migrar do filesystem local para storage de objetos;
PostgreSQL mantem metadados e a referencia do objeto.

### `pedagogical-service`

Responsabilidades: alocacao docente, diario de aula, frequencia, avaliacao,
notas, boletim e historico academico oficial.

Tabelas de propriedade:

- `professor_turma_disciplina`;
- `aula`, `frequencia_aluno`, `frequencia_professor`, `situacao_frequencia`;
- `avaliacao`, `tipo_avaliacao`, `nota_aluno`;
- `historico_escolar`, `historico_escolar_item`.

### `planning-ai-service`

Responsabilidades: planejamento docente, geracao de IA, versoes, aprovacao e
biblioteca pedagogica.

Tabelas PostgreSQL de propriedade:

- `planejamento_bimestral`, `planejamento_bimestral_aula`;
- `planejamento_bimestral_avaliacao`, `status_planejamento`;
- `planejamento_aula`, `planejamento_professor`;
- `planejamento_ia_interacao`, `planejamento_ia_conteudo_gerado`;
- `planejamento_ia_conteudo_versao`, `biblioteca_conteudo_pedagogico`;
- `tipo_conteudo_ia`, `status_conteudo_ia`.

MongoDB armazena prompts e respostas completos, payloads de provedores, conteudo
flexivel e trilha rica de versoes. PostgreSQL mantem identidade, status,
aprovacao, autoria e referencias transacionais.

### `dashboard-query-service`

Responsabilidades: modelos de leitura, indicadores, configuracoes, alertas e
snapshots. Nao consulta bancos transacionais de outros servicos.

Tabelas PostgreSQL de propriedade:

- `publico_dashboard`, `dashboard`, `dashboard_widget`;
- `dashboard_usuario_configuracao`, `dashboard_indicador_snapshot`.

MongoDB pode manter snapshots historicos detalhados e projecoes por publico.
Redis armazena respostas agregadas de curta duracao. As projecoes sao
alimentadas por eventos Kafka.

## Mapa final de runtimes

| Pasta raiz | Runtime final | Responsabilidade principal | Banco oficial | Integracoes esperadas |
| --- | --- | --- | --- | --- |
| `school-management-bff` | sim | fachada, contexto, composicao e strangler | nenhum banco de negocio | HTTP interno, Redis opcional para concerns tecnicos |
| `identity-access-service` | sim | identidade e acesso | PostgreSQL proprio | HTTP interno, Kafka |
| `institutional-tenant-service` | sim | tenant e escola ativa | PostgreSQL proprio | HTTP interno, Kafka |
| `academic-catalog-service` | sim | catalogo academico | PostgreSQL proprio | HTTP interno, Kafka, Redis |
| `people-service` | sim | pessoa, aluno, responsavel, professor, funcionario | PostgreSQL proprio | HTTP interno, Kafka |
| `enrollment-document-service` | sim | matricula, documentos e transferencia | PostgreSQL proprio | HTTP interno, Kafka, object storage |
| `pedagogical-service` | sim | aula, frequencia, avaliacao, boletim e historico | PostgreSQL proprio | HTTP interno, Kafka, Redis opcional |
| `planning-ai-service` | sim | planejamento docente e IA | PostgreSQL proprio | MongoDB, Kafka, provedores IA |
| `dashboard-query-service` | sim | leitura agregada e snapshots | PostgreSQL proprio | Kafka, Redis, MongoDB opcional |
| `school-management-service` | transitorio | monolito legado durante o strangler | `gestao_escolar` atual | atende BFF enquanto houver rotas nao extraidas |
| `school-management-web` | fora do backend | frontend | n/a | consome o BFF quando o corte externo for concluido |

## Convencao final para cada servico

Toda nova extracao deve nascer diretamente na estrutura DDD padrao, sem criar
pacotes temporarios do tipo `controller/service/repository` na raiz do modulo.
O formato esperado e:

```text
<servico>/
|-- pom.xml
`-- src/
    |-- main/
    |   |-- java/br/com/escola/<servico>/
    |   |   |-- domain/
    |   |   |-- application/
    |   |   |-- infra/
    |   |   `-- interfaces/
    |   `-- resources/
    |       |-- application.yml
    |       |-- logback-spring.xml
    |       `-- db/migration/
    `-- test/
        |-- java/
        `-- resources/
```

Regras de leitura dessa arvore:

- `domain`: regra de negocio pura, sem Spring, HTTP, JPA, Kafka, Redis ou
  MongoDB.
- `application`: casos de uso, DTOs e portas.
- `infra`: implementacoes tecnicas de banco, mensageria, cache, clientes e
  seguranca.
- `interfaces`: REST, requests, responses e tratamento de erro.
- `resources/db/migration`: migrations locais do servico, nunca compartilhadas
  entre servicos.

## Estado final esperado do monolito

`school-management-service` nao desaparece no inicio da migracao. O plano e:

1. ele continua operacional enquanto o BFF faz strangler rota a rota;
2. dominios extraidos deixam de escrever e ler diretamente por ele;
3. restam apenas fluxos ainda nao migrados e, eventualmente, adaptadores de
   compatibilidade temporarios;
4. quando o esvaziamento for suficiente, o monolito pode ser aposentado ou
   reduzido a um modulo residual estritamente necessario.

Portanto, a arvore final do repositorio ainda pode conter o monolito por um
tempo, mas ele deixa de ser o centro do sistema.

## Mapa das APIs atuais para os servicos-alvo

O BFF preserva inicialmente as URLs publicas existentes. A tabela define o
proprietario futuro de todas as familias REST encontradas no monolito.

| APIs atuais | Proprietario alvo | Observacao de migracao |
| --- | --- | --- |
| `/api/auth`, `/api/usuarios`, `/api/perfis`, `/api/permissoes` | identity-access | BFF continua sendo a fachada externa |
| `/api/periodos-letivos`, `/api/series`, `/api/turnos`, `/api/turmas`, `/api/disciplinas`, `/api/catalogos/academicos` | academic-catalog | primeiro corte pelo strangler |
| `/api/alunos`, `/api/responsaveis`, `/api/alunos/{id}/responsaveis`, `/api/pessoas/catalogos`, consultas cadastrais | people | exclusao coordenada com enrollment |
| CRUD de `/api/professores` | people | alocacoes nao pertencem ao people service |
| alocacoes de professor e `/api/turmas/{id}/professores` | pedagogical | referencia professor e turma somente por ID |
| `/api/matriculas`, seus catalogos, etapas e documentos | enrollment-document | validacoes externas por portas |
| `/api/documentos`, `/api/documentos-alunos` | enrollment-document | binario migra para object storage |
| `/api/transferencias`, `/api/escolas-origem` | enrollment-document | fluxo longo modelado como saga |
| `/api/aulas`, `/api/avaliacoes`, notas e frequencias | pedagogical | eventos alimentam dashboard |
| `/api/matriculas/{id}/boletim`, `/api/historicos-escolares` | pedagogical | registro academico oficial |
| `/api/planejamentos-bimestrais`, `/api/planejamentos-ia` | planning-ai | MongoDB para payload flexivel, nao para status oficial |
| todas as familias `/api/dashboard/**` | dashboard-query | substituir fan-out ao vivo por projecoes |
| consulta de CEP | people | adapter externo com timeout/circuit breaker |

Controllers que hoje misturam responsabilidades devem ser divididos por caso de
uso no servico proprietario. Compatibilidade de URL fica no BFF, nao em um
modulo compartilhado entre servicos.

## Contratos sincronos internos iniciais

Os endpoints internos usam `/internal/v1`, autenticacao service-to-service e
nao sao expostos diretamente aos frontends.

| Consumidor | Provedor | Contrato minimo |
| --- | --- | --- |
| BFF | identity | validar sessao e obter identidade/permissoes |
| BFF | institutional | resolver escola ativa e validar acesso |
| enrollment | people | validar aluno/responsavel por escola |
| enrollment | academic-catalog | validar turma, periodo e disponibilidade |
| pedagogical | people | validar professor/aluno ativos |
| pedagogical | academic-catalog | consultar turma-disciplina e periodo |
| planning-ai | pedagogical | validar alocacao docente |
| planning-ai | academic-catalog | consultar metadados academicos |

Consultas que toleram consistencia eventual devem preferir projecoes locais
alimentadas por Kafka. Chamadas HTTP ficam reservadas para validacoes que exigem
resposta imediata.

## Orquestracoes do BFF

| Experiencia | Composicao |
| --- | --- |
| login/sessao | identity + institutional |
| contexto do shell | identity + institutional + configuracao de dashboard |
| ficha do aluno | people + enrollment + pedagogical + documentos |
| matricula | people + academic-catalog + enrollment |
| diario/avaliacao | pedagogical + academic-catalog + people |
| planejamento com IA | planning-ai + pedagogical + academic-catalog |
| dashboard | dashboard-query, sem fan-out transacional em tempo real |

Orquestracao de escrita com varias etapas nao fica escondida em controller. Ela
deve ser um caso de uso de aplicacao com estados, idempotencia e compensacoes.

## Catalogo inicial de eventos Kafka

Padrao de topico: `school.<dominio>.<evento>.v1`. Envelope obrigatorio:
`eventId`, `eventType`, `eventVersion`, `occurredAt`, `correlationId`,
`causationId`, `usuarioId`, `escolaId` e `payload`.

| Produtor | Eventos iniciais | Consumidores principais |
| --- | --- | --- |
| identity | `user-created`, `user-access-changed`, `session-revoked` | institutional, BFF/cache |
| institutional | `school-created`, `user-school-linked`, `active-school-changed` | todos os contextos |
| academic-catalog | `term-changed`, `class-changed`, `subject-changed`, `class-subject-changed` | enrollment, pedagogical, planning, dashboard |
| people | `student-changed`, `responsible-changed`, `teacher-changed` | enrollment, pedagogical, dashboard |
| enrollment | `enrollment-created`, `enrollment-status-changed`, `document-received`, `transfer-completed` | pedagogical, dashboard |
| pedagogical | `lesson-completed`, `attendance-recorded`, `assessment-published`, `grade-recorded`, `record-closed` | planning, dashboard |
| planning-ai | `planning-status-changed`, `ai-content-approved`, `library-content-published` | dashboard |

Cada servico produtor possui `outbox_event` no proprio PostgreSQL. Cada
consumidor possui controle de inbox/idempotencia. Falhas usam retry com backoff
e DLT; reprocessamento deve ser operacionalmente rastreavel.

## Uso concreto de Redis

- BFF: rate limit, idempotencia de comandos e cache curto de composicoes;
- identity/institutional: cache curto de sessao, permissoes e escola ativa;
- academic-catalog: cache de catalogos por escola com invalidacao por evento;
- planning-ai: lock de geracao e rate limit por usuario/escola/provedor;
- dashboard-query: cache de indicadores e snapshots recentes.

Chaves incluem ambiente, servico, escola e versao. Todo cache possui TTL e
estrategia para indisponibilidade do Redis; falha de cache nao pode corromper o
dado oficial.

## Observabilidade, seguranca e resiliencia

- OpenTelemetry para traces, metricas e propagacao W3C Trace Context;
- logs JSON com `traceId`, `correlationId`, `usuarioId`, `escolaId` e servico;
- Actuator com health, readiness, liveness e metricas Prometheus;
- timeouts obrigatorios em toda chamada remota;
- retry apenas para operacoes idempotentes e falhas transitorias;
- circuit breaker e bulkhead nos clientes do BFF e servicos;
- autenticacao externa no BFF e credencial service-to-service internamente;
- `escolaId` validado a partir do contexto autenticado, nunca confiado apenas
  porque veio em body ou query string;
- segredos somente por variaveis/secret store, sem defaults reais versionados.

## Estrategia de migracao incremental

### Fase 51A - Diagnostico e arquitetura-alvo

Estado: concluida por este documento.

Entregas:

- inventario do monolito e seus acoplamentos;
- mapa de servicos e propriedade das 73 tabelas;
- contratos sincronos, eventos e responsabilidades de dados;
- uso de Kafka, MongoDB e Redis;
- estrategia strangler e dominio piloto.

### Fase 51B - Fundacao da plataforma distribuida

Estado: implementacao concluida.

Criar sem migrar regra de negocio:

- parent Maven multi-modulo ou estrutura equivalente de build;
- template DDD verificavel por testes de arquitetura;
- Docker Compose local com PostgreSQL, Kafka, MongoDB e Redis;
- convencoes de configuracao, erros, contexto e observabilidade;
- contratos de envelope Kafka, outbox e idempotencia;
- pipeline de testes unitarios, integracao e Testcontainers.

Nao criar todos os servicos vazios. Criar apenas os modulos de fundacao usados
pelo BFF e pelo primeiro servico piloto.

Entregue:

- parent Maven agregando apenas BFF e catalogo, sem acoplar o build do monolito;
- `school-management-bff` e `academic-catalog-service` executaveis em Java 21 e
  Spring Boot 3.5.14;
- quatro camadas DDD e regras ArchUnit impedindo dependencias para fora;
- contexto reativo inicial com `X-Correlation-Id` no BFF;
- portas de outbox e idempotencia no catalogo, sem publicar evento fora de uma
  transacao de negocio;
- contratos JSON Schema para contexto, erro HTTP e envelope Kafka;
- Compose com PostgreSQL, Kafka em KRaft, MongoDB e Redis;
- profile Maven `integration` com Testcontainers PostgreSQL;
- Actuator, Prometheus e bridge OpenTelemetry nos dois runtimes.

Validacao local:

- build agregado e 7 testes de unidade/arquitetura aprovados;
- 99 testes do monolito aprovados;
- Compose e contratos JSON validados estaticamente;
- manifests das quatro imagens validados no registry;
- Testcontainers nao executado nesta maquina porque o servico do Docker Desktop
  estava desabilitado. O profile permanece explicito para CI ou engine OCI
  funcional e nao bloqueia o build padrao.

### Fase 51C - BFF inicial e contexto distribuido

Estado: concluida para a rota piloto de leitura de disciplinas.

- criar `school-management-bff`;
- manter URLs externas atuais por proxy/roteamento strangler;
- centralizar validacao da sessao e propagacao de contexto;
- propagar `Authorization`, `correlationId`, `usuarioId` e `escolaId`;
- configurar timeout, circuit breaker, logs e metricas;
- manter todas as rotas inicialmente apontando para o monolito;
- validar que os frontends operam sem mudanca funcional.

Entregue:

- `GET /api/disciplinas` no BFF preservando o contrato atual;
- porta e caso de uso na camada de aplicacao, com WebClient apenas em `infra`;
- propagacao explicita de `Authorization` e `X-Correlation-Id`;
- descarte de headers de usuario/escola fornecidos pelo cliente;
- timeout de conexao/resposta configuravel;
- circuit breaker Resilience4j configuravel e exposto em metricas Micrometer;
- erros `401`, rejeicoes downstream e indisponibilidade padronizados;
- feature flag `DISCIPLINAS_PROXY_ENABLED` para rollback imediato;
- testes unitarios, de arquitetura, contrato, timeout e fluxo HTTP completo com
  monolito simulado, sem Docker/WSL;
- nenhum frontend, tabela ou endpoint de escrita alterado.

Decisao de seguranca:

- enquanto identity/tenant ainda pertencem ao monolito, o BFF nao confia em
  `X-Usuario-Id` ou `X-Escola-Id` enviados externamente;
- o token opaco e repassado ao monolito, que continua sendo a autoridade da
  sessao e do contexto escolar nesta fase;
- identidade e tenant so serao propagados como headers internos depois de
  validacao criptografica ou introspeccao confiavel na fase de identidade.

### Fase 51D - Piloto `academic-catalog-service`

O catalogo academico e o piloto recomendado porque possui fronteira funcional
clara, APIs existentes, testes de integracao e escopo de escola ja iniciado.

Estado: setima subfase de roteamento read-only do BFF concluida, com cutover
desabilitado por padrao.

Entregue nesta subfase:

- modelo de dominio independente para periodo letivo, serie, turno, turma,
  disciplina e turma-disciplina;
- portas de repositorio no dominio e adaptadores JPA na infraestrutura;
- migrations proprias para banco PostgreSQL vazio, incluindo os catalogos
  globais de nivel de ensino e turno;
- `id_escola` obrigatorio nos agregados escolares, sem duplicar a entidade
  escola neste servico;
- chaves estrangeiras compostas que impedem relacionamentos entre dados de
  escolas diferentes;
- testes unitarios, de arquitetura e de integracao PostgreSQL com duas escolas.

Entregue na segunda subfase:

- caso de uso de consultas separado dos adapters REST e JPA;
- endpoints `GET /internal/v1` para niveis de ensino, turnos, periodos letivos,
  series, turmas, disciplinas e disciplinas da turma;
- autenticacao service-to-service por token configurado exclusivamente por
  `CATALOG_INTERNAL_API_TOKEN`, sem segredo versionado;
- contexto obrigatorio com `X-Correlation-Id`, `X-Usuario-Id` e `X-Escola-Id`;
- respostas internas proprias, sem depender de DTOs ou nomes de escola do
  monolito;
- erros no contrato HTTP v1 e testes REST/PostgreSQL comprovando que uma escola
  nao consulta recursos da outra.

Ao final da segunda subfase, permaneciam fora: escrita no novo servico,
publicacao Kafka, cache Redis, copia de dados e roteamento do BFF.

Entregue na terceira subfase:

- comandos internos de criacao para periodo letivo, serie, disciplina, turma e
  vinculo turma-disciplina;
- `Idempotency-Key` obrigatoria, delimitada por escola e protegida contra reuso
  com outro comando ou payload;
- idempotencia oficial persistida no PostgreSQL com lock transacional por chave,
  substituindo o `SETNX` Redis que nao poderia acompanhar rollback do agregado;
- eventos `term-created`, `grade-created`, `subject-created`, `class-created` e
  `class-subject-created` versionados e persistidos como `PENDENTE` na outbox;
- agregado, registro de idempotencia e outbox confirmados na mesma transacao;
- testes de rollback, replay, conflito de chave e referencia entre escolas.

Atualizacao, exclusao, publisher Kafka, cache Redis, copia de dados e roteamento
do BFF continuam fora desta subfase.

Entregue na quarta subfase:

- publisher transacional da outbox com reivindicacao concorrente por
  `FOR UPDATE SKIP LOCKED` e protecao contra atualizacao por worker obsoleto;
- publicacao dos cinco envelopes versionados no topico
  `school.catalog.events.v1`, preservando `correlationId`, `usuarioId` e
  `escolaId`;
- confirmacao de `PUBLICADO` e incremento de tentativas somente depois da
  confirmacao do broker, com topico, particao e offset registrados;
- retry com backoff exponencial, liberacao de locks expirados e envio para DLT
  ao esgotar as tentativas;
- metricas de itens reivindicados, publicados, reenfileirados e enviados para
  DLT, alem da duracao de cada ciclo;
- feature flag desabilitada por padrao, mantendo o monolito e as rotas atuais
  sem cutover;
- testes unitarios do fluxo de publicacao e testes Testcontainers com
  PostgreSQL e Kafka para os caminhos de sucesso e DLT.

Atualizacao, exclusao, cache Redis, copia de dados e roteamento do BFF continuam
fora desta subfase.

Entregue na quinta subfase:

- snapshot descartavel das leituras do catalogo por escola, mantendo o
  PostgreSQL como fonte oficial;
- chave `ambiente:academic-catalog:escola:catalog-read:v1`, sem compartilhamento
  de dados entre tenants e sem varredura por prefixo;
- TTL configuravel e feature flag desabilitada por padrao antes do cutover;
- invalidacao local depois de comandos novos e invalidacao entre instancias ao
  consumir os cinco eventos versionados do catalogo no Kafka;
- comportamento fail-open em leitura, escrita e invalidacao: falha no Redis
  gera metrica/log e a consulta continua pelo PostgreSQL;
- metricas de hit, miss, escrita, invalidacao e fail-open;
- testes unitarios e Testcontainers validando cache hit/miss, eventos,
  isolamento por escola, TTL e indisponibilidade real do Redis.

Atualizacao, exclusao, copia de dados e roteamento do BFF continuam fora desta
subfase.

Entregue na sexta subfase:

- executor opt-in com conexoes independentes para o PostgreSQL do monolito e do
  catalogo, sem acesso cruzado permanente entre os servicos;
- dry-run como modo padrao e apply somente por configuracao explicita;
- snapshot de origem em transacao read-only `REPEATABLE READ`;
- carga em ordem de dependencias com IDs preservados e upsert, permitindo
  repeticao sem duplicar registros;
- derivacao e validacao de `id_escola` de `turma_disciplina` a partir das duas
  referencias do monolito;
- bloqueio do apply quando a origem possui referencias ou campos obrigatorios
  incompativeis com as restricoes multi-escola do destino;
- reconciliacao de todos os campos por ID, com contagens por escola e listas de
  IDs ausentes, inesperados e divergentes;
- relatorio JSON escrito atomicamente e invalidacao dos snapshots Redis das
  escolas migradas;
- Testcontainers com dois PostgreSQL comprovando duas escolas, repeticao da
  carga e deteccao de divergencia.

Atualizacao, exclusao e roteamento do BFF continuam fora desta subfase. Nenhuma
execucao contra dados reais e automatica: as features de migracao e apply ficam
desabilitadas por padrao.

Entregue na setima subfase:

- BFF expandido de rota piloto unica para as leituras externas do catalogo:
  periodos letivos, series, turnos, turmas, disciplinas, turma-disciplinas e
  catalogos globais;
- roteamento rota a rota para o `academic-catalog-service`, mantendo monolito
  como destino padrao;
- gate de cutover baseado em relatorio JSON reconciliado, com leitura local do
  arquivo e recusa silenciosa ao servico novo quando o relatorio estiver
  ausente, invalido ou divergente;
- resolucao segura de `usuarioId` e `escolaId` no monolito por
  `GET /api/auth/contexto-atual`, sem confiar em headers forjados pelo cliente;
- propagacao service-to-service de `X-Correlation-Id`, `X-Usuario-Id`,
  `X-Escola-Id` e `CATALOG_INTERNAL_API_TOKEN` apenas depois da resolucao do
  contexto autenticado;
- fallback automatico para o monolito quando o catalogo novo estiver
  indisponivel, preservando rollback imediato tambem por feature flag;
- testes unitarios, de arquitetura e de integracao HTTP do BFF, alem de teste
  de integracao do novo endpoint de contexto no monolito.

Escritas permanecem no monolito. O cutover real continua bloqueado ate existir
execucao de migracao real reconciliada e arquivo de relatorio montado no BFF.

Sequencia:

1. caracterizar os contratos REST atuais;
2. criar o servico com a estrutura DDD obrigatoria;
3. separar dominio de entidades JPA e DTOs;
4. criar PostgreSQL/migrations de propriedade do servico;
5. aplicar tenant em todos os agregados e consultas;
6. implementar outbox e eventos de catalogo no Kafka;
7. implementar cache Redis de leituras de catalogo;
8. migrar dados com reconciliacao e contagens verificaveis;
9. rotear `/api/periodos-letivos`, `/api/series`, `/api/turnos`, `/api/turmas`,
   `/api/disciplinas` e vinculos pelo BFF;
10. manter rollback por rota para o monolito durante estabilizacao;
11. remover escrita do monolito somente depois da reconciliacao.

MongoDB nao participa deste piloto porque o catalogo nao possui dado documental
que justifique sua utilizacao.

### Fase 52 - Identidade e tenant

Extrair `identity-access-service` e `institutional-tenant-service`, implementar
vinculo usuario-escola e tornar o contexto autenticado a autoridade de tenant.

### Fase 53 - Pessoas

Extrair `people-service`, remover relacionamentos JPA com seguranca e publicar
eventos de aluno, responsavel e professor.

### Fase 54 - Matricula e documentos

Extrair `enrollment-document-service`, introduzir storage de objetos e saga de
matricula/rematricula/transferencia.

Entregue na primeira subfase da Fase 54:

- o menor recorte backend/backend de menor risco foi aberto em
  `transferencia`, sem tocar BFF, sem storage externo e sem migracao de schema;
- `TransferenciaAlunoService` deixou de depender diretamente de
  `AlunoJpaRepository`, `TransferenciaAlunoJpaRepository`, `JdbcTemplate` e DTOs
  do adaptador web, passando a orquestrar apenas um contrato interno proprio;
- foram criados DTOs internos de transferencia e escola de origem, alem da
  nova porta `TransferenciaAlunoGateway`, com implementacao local em
  `TransferenciaAlunoPersistenceGateway` para encapsular lookup de aluno,
  persistencia da transferencia e resolucao dos catalogos de tipo/status;
- `TransferenciaAlunoController` e `EscolaOrigemController` passaram a fazer
  apenas o mapeamento entre contrato externo e contrato interno, preservando as
  mesmas rotas publicas `/api/transferencias` e `/api/escolas-origem`;
- com isso, a Fase 54 comeca pelo ponto mais simples do bloco
  matricula/documentos para reduzir dependencia direta de repositorios/SQL de
  outro modulo antes de atacar matricula, historico e storage de arquivos.

Proxima subfase pratica:

- aplicar o mesmo padrao de fronteira interna ao write principal de
  `matricula`, reduzindo o acoplamento remanescente entre caso de uso e
  materializacao JPA compartilhada do aluno;
- manter o escopo no backend atual, sem cutover de BFF, sem saga distribuida e
  sem persistencia propria fora do monolito nesta etapa.

Entregue na segunda subfase da Fase 54:

- foi aplicado esse mesmo padrao minimo ao write principal de `matricula`, sem
  alterar rotas externas, sem migracao de schema e sem mudar a modelagem fisica
  de `MatriculaEntity` nesta etapa;
- foi criada a porta interna `AlunoMatriculaPort`, implementada localmente por
  `AlunoMatriculaService`, para encapsular existencia e materializacao escopada
  de aluno por `alunoId` e `escolaId`;
- `AlunoConsultaPersistenceGateway` deixou de depender diretamente de
  `AlunoJpaRepository` e passou a delegar essa verificacao a essa fronteira
  interna;
- `MatriculaPersistenceGateway` deixou de materializar `AlunoEntity`
  diretamente por `EntityManager.getReference(AlunoEntity.class, ...)` e passou
  a obter o aluno via `AlunoMatriculaPort`, preservando o comportamento da
  escrita principal de matricula no backend atual;
- com isso, a Fase 54 avancou no desacoplamento contrato-a-contrato do bloco de
  matricula antes de discutir remocao de relacionamento ORM, storage de
  documentos ou saga distribuida.

Proxima subfase pratica:

- aplicar o mesmo criterio ao recorte de `documento` ou ao fluxo complementar
  de `historico`, escolhendo o menor ponto ainda acoplado diretamente a
  repositorios/entidades de outro modulo;
- manter a evolucao ainda no backend/backend local, sem BFF e sem storage
  externo nesta etapa.

Entregue na terceira subfase da Fase 54:

- foi aplicado o mesmo criterio ao menor ponto restante de `documento`, sem
  alterar controller, casos de uso, storage local nem contrato REST externo;
- `DocumentoPersistenceGateway` deixou de consultar `AlunoJpaRepository`
  diretamente para resolver o vinculo `ALUNO -> pessoaId`;
- esse gateway passou a reutilizar a fronteira interna `AlunoMatriculaPort`,
  ja criada na subfase anterior, para materializar o aluno escopado por escola
  antes de gravar ou listar documentos do aluno;
- o recorte foi mantido propositalmente minimo: o fluxo de `RESPONSAVEL` e os
  demais pontos do gateway continuam inalterados nesta etapa para evitar
  refatoracao ampla do bloco de documentos;
- com isso, a Fase 54 avanca no desacoplamento incremental do recorte
  matricula/documentos reaproveitando fronteiras internas existentes, sem abrir
  storage externo nem cutover de BFF.

Proxima subfase pratica:

- aplicar o mesmo padrao ao proximo ponto minimo remanescente, priorizando o
  fluxo de `responsavel` em `documento` ou o primeiro acoplamento direto de
  `historico`, conforme o menor risco de implementacao;
- manter a fase restrita ao backend atual, sem migracao de schema e sem
  distribuicao fisica nesta etapa.

Entregue na quarta subfase da Fase 54:

- foi aplicado o mesmo padrao ao fluxo de `responsavel` dentro de
  `documento`, fechando o gateway documental nos dois vinculos atualmente
  expostos sem alterar controllers, casos de uso ou storage;
- foi criada a porta interna `ResponsavelDocumentoPort`, implementada por
  `ResponsavelDocumentoService`, para encapsular a busca escopada de
  `ResponsavelEntity` por `responsavelId` e `escolaId`;
- `DocumentoPersistenceGateway` deixou de consultar
  `ResponsavelJpaRepository` diretamente para resolver
  `RESPONSAVEL -> pessoaId`, passando a usar essa nova fronteira interna;
- com isso, o recorte de `documento` ficou alinhado ao mesmo criterio usado em
  `transferencia` e `matricula`: nenhuma dependencia direta restante do gateway
  em repositorio JPA de outro modulo para os vinculos de aluno e responsavel;
- a subfase permaneceu backend/backend e incremental, sem migracao fisica, sem
  storage externo e sem mudanca de contrato REST.

Proxima subfase pratica:

- iniciar o primeiro recorte minimo de `historico`, escolhendo o menor ponto
  que ainda materializa ou consulta diretamente entidade/repositorio de outro
  modulo;
- manter a fase no backend atual, sem BFF e sem refatoracao ampla do bloco de
  historico/boletim nesta etapa.

Entregue na quinta subfase da Fase 54:

- foi iniciado o primeiro recorte minimo de `historico` pelo menor acoplamento
  direto remanescente: a consulta e materializacao de aluno dentro de
  `HistoricoEscolarServiceImpl`;
- `HistoricoEscolarServiceImpl` deixou de depender diretamente de
  `AlunoJpaRepository` para validar existencia de aluno por escola e para
  carregar `AlunoEntity` no create/update do historico;
- o service passou a reutilizar a fronteira interna `AlunoMatriculaPort`, ja
  estabelecida nas subfases anteriores, mantendo inalterados os contratos REST,
  o mapper de historico e o recorte de `boletim`;
- com isso, a Fase 54 fecha o bloco minimo de desacoplamento interno de
  matricula/documentos/historico no backend atual sem abrir refatoracao ampla
  do modulo historico nem mexer em BFF, storage ou distribuicao fisica.

Proxima subfase pratica:

- encerrar oficialmente o bloco interno da Fase 54 revisando se restou algum
  acesso direto relevante entre `matricula`, `documento` e `historico` para
  aluno/responsavel, e, se o recorte estiver coberto, preparar a macrofase
  seguinte;
- manter o escopo backend/backend, sem ampliar para cutover externo nesta
  etapa.

Entregue na sexta subfase da Fase 54:

- a revisao final do bloco interno identificou um residuo do mesmo padrao ainda
  em `transferencia`: a validacao de existencia de aluno dentro de
  `TransferenciaAlunoPersistenceGateway`;
- esse gateway deixou de depender diretamente de `AlunoJpaRepository` e passou
  a reutilizar `AlunoMatriculaPort` com escopo de escola para a validacao de
  aluno, preservando o contrato publico existente e sem alterar o write da
  transferencia;
- com isso, o bloco minimo de desacoplamento interno de `transferencia`,
  `matricula`, `documento` e `historico` fica formalmente fechado no backend
  atual, restando como proximos passos apenas recortes estruturais maiores
  fora deste mesmo escopo minimo.

Proxima subfase pratica:

- iniciar a macrofase seguinte pelo primeiro recorte minimo de leitura ou
  persistencia propria em dominio ainda nao iniciado, sem reabrir este bloco
  interno de matricula/documentos/historico;
- manter o escopo backend/backend, sem cutover externo de BFF nesta etapa.

### Fase 55 - Pedagogico

Extrair `pedagogical-service` com alocacao, aula, frequencia, avaliacao, notas,
boletim e historico.

Entregue na primeira subfase da Fase 55:

- a macrofase pedagogica foi iniciada pelo menor recorte de leitura
  consolidada ainda acoplado fora do proprio modulo: o resumo academico da
  matricula em `/api/matriculas/{id}/academico`;
- `MatriculaAcademicoResumoService` deixou de depender diretamente de
  `NotaAlunoJpaRepository`, `FrequenciaAlunoJpaRepository` e das entidades de
  `avaliacao` e `frequencia`;
- foi criada a fronteira interna `RendimentoAcademicoPort`, implementada por
  `RendimentoAcademicoService`, com DTOs internos proprios para notas e
  frequencias academicas por matricula;
- com isso, a Fase 55 abre o desacoplamento do bloco pedagogico pelo ponto de
  menor risco, sem alterar contrato REST, sem mexer em `boletim` e sem abrir
  persistencia propria nesta etapa.

Proxima subfase pratica:

- aplicar o mesmo criterio ao primeiro recorte minimo de `boletim`,
  reaproveitando essa nova fronteira interna onde fizer sentido e evitando
  refatoracao ampla do modulo pedagogico;
- manter o escopo backend/backend, sem BFF e sem cutover externo nesta etapa.

Entregue na segunda subfase da Fase 55:

- o mesmo padrao minimo foi aplicado ao primeiro recorte de `boletim`, ainda
  sem mexer na persistencia de fechamento nem no contrato HTTP externo;
- `BoletimService` deixou de depender diretamente de
  `NotaAlunoJpaRepository`, `FrequenciaAlunoJpaRepository` e das entidades de
  `avaliacao`/`frequencia` para calcular o boletim em memoria;
- o calculo do boletim passou a reutilizar `RendimentoAcademicoPort`, criado na
  subfase anterior, preservando o comportamento do endpoint e o fluxo de
  fechamento persistido;
- com isso, a Fase 55 avanca no bloco pedagogico por leitura consolidada antes
  de atacar recortes mais estruturais de historico oficial ou persistencia
  propria.

Proxima subfase pratica:

- fechar o primeiro bloco interno pedagogico revisando `historico`/`boletim`
  apos a introducao de `RendimentoAcademicoPort`, e, se o recorte estiver
  coberto, identificar o proximo ponto minimo ainda acoplado dentro da
  macrofase;
- manter o escopo backend/backend, sem BFF e sem cutover externo nesta etapa.

Entregue na terceira subfase da Fase 55:

- foi fechado o primeiro bloco interno pedagogico com a revisao final de
  `historico` e `boletim` apos a introducao de `RendimentoAcademicoPort`;
- `BoletimService` e `HistoricoEscolarServiceImpl` deixaram de resolver escola
  padrao via `EscolaTenantService` e passaram a reutilizar `EscolaContextoPort`,
  alinhando o contexto escolar desses fluxos ao mesmo padrao interno ja usado em
  leituras consolidadas e gateways auxiliares;
- com isso, o recorte minimo de fronteiras internas em resumo academico,
  boletim e historico fica formalmente fechado sem alterar contratos REST, sem
  BFF e sem persistencia propria nesta etapa;
- os acoplamentos remanescentes no modulo de historico passam a ser mais
  estruturais, ligados a mapeamento ORM e persistencia oficial, e nao mais ao
  mesmo padrao minimo de consulta interna tratado neste bloco.

Proxima subfase pratica:

- iniciar o proximo recorte minimo da macrofase 55 em um ponto estrutural
  ainda controlado, preferencialmente na geracao oficial de `historico` a
  partir de `boletim` ou em outra fronteira interna de persistencia
  pedagogica, sem abrir persistencia propria nem cutover externo;
- manter o escopo backend/backend, sem BFF nesta etapa.

Entregue na quarta subfase da Fase 55:

- foi iniciado o proximo recorte estrutural controlado exatamente na geracao
  oficial de `historico` a partir de `boletim`;
- `HistoricoEscolarServiceImpl` deixou de consultar diretamente
  `BoletimJpaRepository`, `BoletimItemJpaRepository`, `BoletimEntity` e
  `BoletimItemEntity` para esse fluxo;
- foi criada a fronteira interna `BoletimHistoricoPort`, implementada por
  `BoletimHistoricoService`, com DTOs internos proprios para o resumo do
  boletim fechado e dos itens usados na geracao oficial do historico;
- com isso, a Fase 55 avancou do bloco interno de leitura para o primeiro
  recorte estrutural de persistencia oficial sem alterar rota externa, sem BFF
  e sem persistencia propria nesta etapa.

Proxima subfase pratica:

- consolidar esse recorte estrutural de geracao oficial de `historico`,
  revisando se ainda resta no mesmo fluxo algum acoplamento minimo que possa
  sair para fronteira interna antes de atacar mudancas maiores de modelo;
- manter o escopo backend/backend, sem cutover externo nesta etapa.

Entregue na quinta subfase da Fase 55:

- a montagem estrutural do `HistoricoEscolar` gerado por boletim fechado foi
  extraida de `HistoricoEscolarServiceImpl` para a fabrica interna
  `HistoricoEscolarGeracaoFactory`;
- a nova fabrica passou a concentrar a composicao do cabecalho oficial do
  historico, a montagem dos `HistoricoEscolarItem` e a observacao padrao de
  geracao, deixando o service focado na orquestracao do fluxo;
- com isso, o recorte estrutural iniciado na subfase anterior ficou mais
  coeso e isolado em fronteiras internas do proprio modulo, sem alterar rota
  externa, sem BFF e sem ampliar persistencia propria.

Proxima subfase pratica:

- executar a subfase minima restante para fechar esse bloco estrutural de
  `historico`, revisando se ainda ha algum vazamento relevante de detalhe de
  persistencia no contrato interno da geracao oficial antes de considerar o
  recorte encerrado;
- manter o escopo backend/backend, sem cutover externo nesta etapa.

Entregue na sexta subfase da Fase 55:

- o contrato interno `BoletimHistoricoItemResumo` deixou de carregar
  `PeriodoLetivoEntity`, `SerieEntity` e `DisciplinaEntity`, passando a expor
  apenas os respectivos IDs estruturais;
- a resolucao JPA dessas referencias ficou encapsulada em
  `HistoricoEscolarGeracaoFactory` por `EntityManager.getReference(...)`,
  mantendo o detalhe de persistencia apenas no ponto de montagem final do
  `HistoricoEscolar`;
- com isso, o bloco estrutural minimo de geracao oficial de `historico` por
  `boletim` fica formalmente encerrado no backend atual, sem alterar rota
  externa, sem BFF e sem abrir persistencia propria adicional nesta macrofase.

Proxima subfase pratica:

- encerrar oficialmente a Fase 55 e iniciar a primeira subfase da Fase 56 com
  diagnostico do menor recorte interno de `planejamento e IA`, escolhendo a
  fronteira backend/backend de menor risco antes de qualquer extração física;
- manter o escopo incremental e backend-only.

### Fase 56 - Planejamento e IA

Extrair `planning-ai-service`, ativar MongoDB para payloads flexiveis, Kafka para
publicacao e Redis para locks/rate limit.

Entregue na primeira subfase da Fase 56:

- foi aberto o primeiro recorte backend/backend de menor risco no bloco de
  `planejamento e IA`, exatamente na dependencia de `PlanejamentoIAService`
  sobre o fluxo de `planejamento bimestral`;
- `PlanejamentoIAService` deixou de consultar diretamente
  `PlanejamentoBimestralJpaRepository` para validar e resumir o planejamento
  usado na geracao de conteudo, passando a consumir a nova fronteira interna
  `PlanejamentoIAPort`;
- foi criado `PlanejamentoIAPlanejamentoService` com DTO interno proprio
  `PlanejamentoIAResumo`, mantendo a resolucao JPA do `PlanejamentoBimestral`
  apenas como referencia no ponto de persistencia da interacao e do conteudo
  gerado;
- com isso, a Fase 56 inicia pelo menor acoplamento direto entre IA e
  planejamento, sem alterar rota externa, sem BFF e sem extracao fisica nesta
  etapa.

Proxima subfase pratica:

- aplicar o mesmo criterio no bloco restante de `PlanejamentoIAService`,
  escolhendo o proximo menor acoplamento direto a repositorios/entidades de
  `planejamento` ou catalogos auxiliares antes de discutir Mongo, Kafka ou
  Redis;
- manter o escopo backend/backend e incremental.

Entregue na segunda subfase da Fase 56:

- o bloco de publicacao de conteudo na biblioteca pedagogica saiu de
  `PlanejamentoIAService` para a fabrica interna
  `PlanejamentoIABibliotecaFactory`;
- foi criado o DTO interno `PlanejamentoIABibliotecaPublicacaoResumo` para
  transportar apenas os dados minimos da publicacao, encapsulando no ponto
  interno apropriado a travessia de entidades de `planejamento`, `professor` e
  `catalogo`;
- com isso, `PlanejamentoIAService` fica mais concentrado na orquestracao do
  fluxo de IA, enquanto a montagem da publicacao reutilizavel da biblioteca
  passa a ficar isolada em uma fronteira interna do proprio modulo.

Proxima subfase pratica:

- executar a subfase minima restante do primeiro bloco de `planejamento e IA`,
  revisando se ainda resta em `PlanejamentoIAService` algum acoplamento direto
  relevante a entidades de `planejamento` apenas para mapeamento ou contexto;
- manter o escopo backend/backend, sem Mongo, Kafka, Redis ou BFF nesta etapa.

Entregue na terceira subfase da Fase 56:

- o contexto escolar de resposta em `PlanejamentoIAService` deixou de depender
  da travessia de `PlanejamentoBimestral` apenas para recuperar escola;
- `toInteracaoResponse` e `toConteudoResponse` passaram a usar diretamente
  `EscolaContextoPort` como autoridade interna do contexto escolar atual,
  eliminando o uso residual do planejamento apenas para esse mapeamento;
- com isso, o primeiro bloco interno de `planejamento e IA` fica formalmente
  fechado no backend atual, ainda sem Mongo, Kafka, Redis, BFF ou extracao
  fisica.

Proxima subfase pratica:

- iniciar o proximo bloco interno da Fase 56 escolhendo o menor recorte
  remanescente de `PlanejamentoIAService` ou da biblioteca pedagogica que ainda
  faça consulta direta relevante a repositorios auxiliares antes de qualquer
  debate sobre infraestrutura externa;
- manter o escopo backend/backend e incremental.

Entregue na quarta subfase da Fase 56:

- o bloco de persistencia e consulta da biblioteca pedagogica saiu de
  `PlanejamentoIAService` para a nova porta interna
  `PlanejamentoIABibliotecaPort`, implementada por
  `PlanejamentoIABibliotecaService`;
- foi criado o DTO interno `PlanejamentoIABibliotecaResumo` para materializar o
  contrato backend/backend minimo da biblioteca, sem expor `PlanejamentoIAService`
  diretamente aos detalhes de `BibliotecaConteudoPedagogicoJpaRepository`;
- com isso, `PlanejamentoIAService` deixa de consultar e publicar diretamente
  na biblioteca pedagogica, preservando o mesmo contrato REST externo e o mesmo
  comportamento funcional;
- Mongo, Kafka, Redis, BFF e extracao fisica continuam fora desta subfase.

Proxima subfase pratica:

- executar a subfase minima restante de Fase 56 para revisar se o bloco de
  `planejamento e IA` agora ficou restrito a dependencias internas do proprio
  modulo, fechando formalmente a macrofase antes de abrir outro dominio;
- manter o escopo backend/backend, sem ampliar para infraestrutura externa ou
  refatoracao ampla.

Entregue na quinta subfase da Fase 56:

- a montagem das entidades de escrita de IA saiu de `PlanejamentoIAService`
  para a fabrica interna `PlanejamentoIAEscritaFactory`;
- com isso, `PlanejamentoIAService` deixou de depender diretamente de
  `PlanejamentoBimestralEntity` e `EntityManager` apenas para amarrar a escrita
  de interacoes, conteudos gerados e versoes ao planejamento;
- o bloco de `planejamento e IA` ficou fechado no backend atual com
  orquestracao principal apoiada apenas por portas e fabricas internas do
  proprio modulo, sem alterar contrato REST externo, sem BFF e sem
  infraestrutura distribuida nesta etapa.

Proxima subfase pratica:

- encerrar oficialmente a Fase 56 e iniciar a proxima macrofase backend pelo
  menor dominio remanescente fora desse bloco, sem reabrir `planejamento e IA`
  nesta mesma etapa;
- manter o escopo incremental e backend/backend.

Encerramento oficial da Fase 56:

- a macrofase backend de `planejamento e IA` fica encerrada no monolito atual;
- os recortes aplicados nesta fase deixaram `PlanejamentoIAService` apoiado por
  portas e fabricas internas do proprio modulo para leitura de planejamento,
  publicacao/listagem da biblioteca e montagem das escritas;
- nao houve alteracao de rotas externas, nao houve cutover de BFF e nao houve
  abertura de persistencia distribuida adicional nesta etapa;
- qualquer nova evolucao de `planejamento e IA` passa a depender de decisao
  explicita de nova macrofase, e nao de continuidade automatica desta.

Proxima fase pratica:

- iniciar a primeira subfase da Fase 57 com diagnostico pontual do menor
  recorte backend/backend de `dashboard`, separando consulta consolidada,
  dependencias cruzadas e possibilidade de fronteira interna minima antes de
  qualquer uso de Kafka, MongoDB, Redis ou extracao fisica;
- manter o mesmo criterio incremental, sem BFF e sem refatoracao ampla.

### Fase 57 - Dashboard orientado a eventos

Extrair `dashboard-query-service`, substituir consultas cruzadas por projecoes
Kafka, usar MongoDB para historico detalhado e Redis para respostas recentes.

Entregue na primeira subfase da Fase 57:

- foi executado o diagnostico pontual do menor recorte backend/backend de
  `dashboard` no monolito atual, ainda sem Kafka, MongoDB, Redis, BFF ou
  extracao fisica;
- o menor ponto de entrada identificado e `DashboardAcademicoService`, porque
  ele ja materializa um resumo consolidado reutilizado por
  `DashboardSecretariaService` e `DashboardDiretorService`, com contrato de
  resposta estavel e rollback interno simples;
- esse servico concentra dependencias cruzadas reais de `matricula`,
  `catalogo` e `historico` por meio de `MatriculaJpaRepository`,
  `CatalogoAcademicoPort`, `BoletimJpaRepository` e
  `HistoricoEscolarJpaRepository`, tornando-se o melhor candidato para abrir a
  primeira fronteira interna da macrofase;
- `DashboardProfessorService` e `DashboardDiretorService` permanecem mais
  acoplados e com risco maior nesta etapa, porque agregam contagens por
  `aula`, `avaliacao`, `nota`, `planejamento`, `aluno` e `professor`, alem de
  iteracoes em memoria sobre consultas amplas;
- `DashboardSnapshotGeradorService` foi mantido fora como primeiro recorte
  pratico, porque ele depende da estabilizacao dos resumos de dashboard antes de
  virar orquestrador de projecoes ou snapshots desacoplados.

Impactos e consistencia mapeados:

- nenhuma rota externa precisa mudar na primeira fronteira interna, porque o
  consumo e hoje exclusivamente entre servicos do proprio modulo `dashboard`;
- a consistencia continua sincrona e baseada na leitura do PostgreSQL do
  monolito nesta etapa, sem outbox, sem eventos e sem cache distribuido;
- o rollback minimo e trivial: a futura troca pode permanecer limitada ao
  chamador interno, com retorno direto ao `DashboardAcademicoService` atual sem
  migracao de dados.

Proxima subfase pratica:

- criar a primeira fronteira interna explicita da Fase 57 em torno de
  `DashboardAcademicoService`, definindo uma porta backend/backend e um DTO
  interno proprio para o resumo academico consumido por secretaria e diretoria;
- manter o escopo no backend atual, sem alterar contratos REST, sem mexer em
  snapshots, sem BFF e sem infraestrutura distribuida.

Entregue na segunda subfase da Fase 57:

- foi criada a porta interna `DashboardAcademicoPort` e o DTO interno
  `DashboardAcademicoResumo`, com tipos internos proprios para
  `matriculasPorStatus` e `turmasComVagas`;
- `DashboardAcademicoService` passou a implementar essa fronteira interna e a
  separar explicitamente o resumo backend/backend da resposta REST externa;
- `DashboardSecretariaService` e `DashboardDiretorService` deixaram de depender
  diretamente da implementacao concreta de `DashboardAcademicoService` e
  passaram a consumir o contrato interno minimo do resumo academico;
- os endpoints externos de dashboard permaneceram inalterados e o bloco de
  snapshots continuou fora desta subfase.

Proxima subfase pratica:

- aplicar o mesmo criterio no proximo consumidor interno de menor risco do
  bloco de dashboard, avaliando se `DashboardSnapshotGeradorService` deve passar
  a consumir a mesma fronteira interna de resumos antes de qualquer diagnostico
  maior em `DashboardDiretorService` ou `DashboardProfessorService`;
- manter o escopo backend/backend e incremental, sem BFF nem infraestrutura
  distribuida.

Entregue na terceira subfase da Fase 57:

- `DashboardSnapshotGeradorService` passou a consumir `DashboardAcademicoPort`
  no fluxo de geracao de snapshots do publico `ACADEMICO`, reutilizando a mesma
  fronteira interna de resumo criada na subfase anterior;
- com isso, o primeiro consumidor interno adicional do bloco de dashboard deixa
  de depender diretamente da implementacao concreta de
  `DashboardAcademicoService` para esse recorte;
- a geracao de snapshots de `SECRETARIA`, `DIRETOR` e `PROFESSOR` permaneceu
  inalterada nesta etapa, preservando o criterio de menor risco e evitando
  abrir recortes maiores antes da hora;
- contratos REST, persistencia de snapshots e comportamento funcional externo
  permaneceram inalterados.

Proxima subfase pratica:

- aplicar o mesmo criterio de fronteira interna ao proximo resumo de menor
  risco dentro do bloco de dashboard, avaliando se `DashboardSecretariaService`
  deve virar contrato interno reutilizavel antes de qualquer diagnostico mais
  profundo em `DashboardDiretorService` ou `DashboardProfessorService`;
- manter o escopo backend/backend e incremental, sem BFF nem infraestrutura
  distribuida.

Entregue na quarta subfase da Fase 57:

- foi criada a porta interna `DashboardSecretariaPort` e o DTO interno
  `DashboardSecretariaResumo`, reaproveitando os tipos internos ja existentes
  para `matriculasPorStatus` e `turmasComVagas`;
- `DashboardSecretariaService` passou a implementar essa nova fronteira e a
  separar explicitamente o resumo backend/backend da resposta REST externa da
  secretaria;
- `DashboardDiretorService` e o fluxo `SECRETARIA` de
  `DashboardSnapshotGeradorService` deixaram de depender da implementacao
  concreta de `DashboardSecretariaService` e passaram a consumir o contrato
  interno minimo;
- os endpoints externos de dashboard e o comportamento funcional dos snapshots
  permaneceram inalterados, mantendo o recorte no backend atual e o rollback
  interno simples.

Proxima subfase pratica:

- aplicar o mesmo padrao ao proximo recorte minimo restante do bloco,
  avaliando se `DashboardDiretorService` deve expor um resumo interno proprio
  antes de qualquer diagnostico mais profundo sobre `DashboardProfessorService`;
- manter o escopo backend/backend e incremental, sem BFF nem infraestrutura
  distribuida.

Entregue na quinta subfase da Fase 57:

- foi criada a porta interna `DashboardDiretorPort` e o DTO interno
  `DashboardDiretorResumo`, reaproveitando os tipos internos ja existentes para
  `matriculasPorStatus` e `turmasComVagas`;
- `DashboardDiretorService` passou a implementar essa fronteira e a separar o
  resumo backend/backend da resposta REST externa da diretoria;
- o fluxo `DIRETOR` de `DashboardSnapshotGeradorService` deixou de depender da
  implementacao concreta de `DashboardDiretorService` e passou a consumir o
  contrato interno minimo;
- os endpoints externos de dashboard, os snapshots e o comportamento funcional
  permaneceram inalterados, fechando o primeiro ciclo minimo de fronteiras
  internas do bloco `dashboard` sem avancar ainda para `DashboardProfessorService`.

Proxima subfase pratica:

- fechar oficialmente este primeiro ciclo interno da Fase 57 com um diagnostico
  objetivo do recorte remanescente, confirmando se `DashboardProfessorService`
  deve ser o proximo candidato ou se a macrofase ja pode migrar para preparacao
  de projecoes/eventos em uma etapa separada;
- manter o escopo backend/backend e incremental, sem BFF nem infraestrutura
  distribuida nesta subfase de fechamento.

Entregue na sexta subfase da Fase 57:

- foi executado o fechamento formal do primeiro ciclo interno da macrofase,
  confirmando que os resumos de `DashboardAcademicoService`,
  `DashboardSecretariaService` e `DashboardDiretorService` ja possuem portas e
  DTOs internos proprios consumidos pelo gerador de snapshots sem dependencia
  concreta desses tres servicos;
- o recorte remanescente foi diagnosticado como qualitativamente diferente:
  `DashboardProfessorService` continua mais acoplado por depender de consultas
  especificas de professor, alocacoes, aulas, frequencias, avaliacoes, notas e
  planejamentos, enquanto `DashboardFrontendService` e `DashboardAlertaService`
  ainda compoem respostas externas diretamente a partir dos services concretos
  de dashboard;
- por esse motivo, a Fase 57 fica encerrada sem ampliar escopo para nova
  refatoracao interna nesta subfase: o bloco minimo backend/backend de resumos
  reutilizaveis foi fechado, e qualquer passo adicional ja entra em um recorte
  maior do dominio de consultas/agregacoes;
- nao houve alteracao de rotas externas, nao houve mudanca de persistencia e
  nao houve introducao de Kafka, MongoDB, Redis ou BFF nesta etapa de
  encerramento formal.

Proxima fase pratica:

- iniciar a proxima macrofase de `dashboard` pelo diagnostico pontual do menor
  recorte remanescente apos o bloco de resumos internos, com duas opcoes
  explicitas a validar: `DashboardProfessorService` como novo candidato de
  fronteira interna ou `DashboardFrontendService`/`DashboardAlertaService` como
  primeiro ponto de composicao a separar;
- manter o mesmo criterio incremental: sem cutover externo, sem projecoes por
  eventos ainda e sem infraestrutura distribuida antes do diagnostico objetivo.

Entregue na primeira subfase da macrofase seguinte de `dashboard`:

- foi executado o diagnostico comparativo do menor recorte remanescente apos o
  fechamento do bloco de resumos internos;
- `DashboardProfessorService` foi descartado como proximo passo imediato porque
  continua acoplado a consultas especificas por professor, alocacoes, aulas,
  frequencias, avaliacoes, notas e planejamentos, o que elevaria o risco e o
  tamanho da fronteira logo na abertura da nova macrofase;
- `DashboardFrontendService` tambem nao foi escolhido como primeiro candidato,
  porque ele compoe resumo, alertas, configuracao administrativa,
  configuracao por usuario e historico de snapshots em uma mesma resposta,
  tornando o recorte maior do que o necessario para reiniciar a evolucao
  incremental;
- o menor candidato seguro identificado passa a ser `DashboardAlertaService`,
  que hoje ainda depende diretamente dos services concretos de `academico`,
  `secretaria`, `diretor` e `professor`, mas pode reutilizar as fronteiras
  internas ja abertas no bloco anterior sem mexer ainda na composicao maior do
  frontend.

Impactos e consistencia mapeados:

- nenhuma rota externa precisa mudar nesse proximo recorte, porque a troca
  permanece confinada ao backend atual e aos consumidores internos do modulo de
  dashboard;
- a consistencia continua sincrona e baseada em leitura do PostgreSQL do
  monolito, sem eventos, sem cache distribuido e sem mudanca de persistencia;
- o rollback segue simples, pois o recorte pode ficar limitado a
  `DashboardAlertaService` e aos contratos internos ja existentes para
  academico, secretaria e diretor, mantendo `DashboardProfessorService`
  concreto nesta primeira etapa.

Proxima subfase pratica:

- aplicar a mesma fronteira interna minima no primeiro ponto de composicao da
  nova macrofase, fazendo `DashboardAlertaService` consumir as portas internas
  ja existentes de `academico`, `secretaria` e `diretor`, enquanto o caminho
  de `professor` permanece concreto por ora;
- manter o escopo backend/backend e incremental, sem BFF, sem eventos e sem
  refatoracao ampla de `DashboardFrontendService` nesta etapa.

Entregue na segunda subfase da macrofase seguinte de `dashboard`:

- `DashboardAlertaService` passou a consumir `DashboardAcademicoPort`,
  `DashboardSecretariaPort` e `DashboardDiretorPort` para os publicos
  `ACADEMICO`, `SECRETARIA` e `DIRETOR`, reaproveitando as fronteiras internas
  abertas no bloco anterior;
- o caminho de `PROFESSOR` permaneceu dependente de `DashboardProfessorService`
  concreto, preservando o criterio de menor risco e evitando abrir nessa etapa
  o recorte mais pesado de consultas especificas por professor;
- os endpoints externos de alertas e a composicao maior do frontend
  permaneceram inalterados, com a mudanca confinada ao backend atual e ao
  primeiro ponto de composicao escolhido para a nova macrofase;
- o rollback continua simples, porque a alteracao ficou restrita a
  `DashboardAlertaService` e aos contratos internos ja existentes, sem schema,
  sem migracao de dados e sem infraestrutura distribuida.

Proxima subfase pratica:

- fechar formalmente este bloco minimo da nova macrofase, confirmando se ainda
  resta algum consumidor de composicao comparavel antes de decidir entre abrir
  `DashboardFrontendService` ou iniciar um recorte proprio para
  `DashboardProfessorService`;
- manter o escopo backend/backend e incremental, sem BFF, sem eventos e sem
  refatoracao ampla nesta etapa de fechamento.

### Fase 58 - Desativacao do monolito

Somente quando todas as rotas tiverem proprietario, reconciliacao, observabilidade
e rollback testado. Remover gradualmente migrations e codigo ja transferidos.

## Criterios de aceite por extracao

- arquitetura DDD validada automaticamente;
- nenhuma dependencia de entidade/repositorio de outro servico;
- contrato OpenAPI e eventos versionados;
- tenant testado com pelo menos duas escolas;
- testes unitarios, integracao e contrato aprovados;
- migration e reconciliacao de dados reproduziveis;
- outbox/inbox, retry e DLT validados quando houver Kafka;
- comportamento com Redis/Mongo/Kafka indisponiveis definido e testado;
- logs, metricas, traces e health checks disponiveis;
- BFF com rollback de rota documentado;
- monolito continua operacional ate o corte definitivo.

## Proxima fase pratica

Continuar a Fase 51D com a expansao da escrita controlada do catalogo via BFF,
rota por rota, com o mesmo gate de relatorio reconciliado, `Idempotency-Key`,
metricas e health dedicados. O proximo corte de menor risco e
`POST /api/disciplinas`, ainda sem fallback automatico para o monolito depois
que a escrita tenta o `academic-catalog-service`.

Entregue na oitava subfase:

- execucao real da migracao do catalogo do monolito para o PostgreSQL proprio,
  com relatorio reconciliado em arquivo absoluto montado localmente no BFF;
- ajuste do migrador para substituir os seeds globais de `nivel_ensino` e
  `turno` quando o destino ainda esta vazio de dados escolares, resolvendo o
  primeiro bloqueio operacional real por colisao de IDs;
- ativacao controlada no BFF para `GET /api/disciplinas` e
  `GET /api/turnos/{id}`;
- validacao ponta a ponta com token real do monolito, metricas HTTP e fallback
  comprovado ao derrubar o `academic-catalog-service`, sem mudar escritas.

Entregue na nona subfase:

- ampliacao do cutover read-only para `GET /api/disciplinas/{id}`,
  `GET /api/periodos-letivos` e `GET /api/periodos-letivos/{id}`;
- validacao operacional das tres rotas ampliadas com resposta `200` via BFF e
  trafego confirmado no `academic-catalog-service`;
- fallback comprovado tambem nessas rotas apos parada deliberada do catalogo,
  mantendo o monolito como rollback imediato;
- `platform/runtime/` tratado como artefato local e ignorado pelo Git.

Entregue na decima subfase:

- ampliacao do cutover read-only para `GET /api/series`,
  `GET /api/series/{id}`, `GET /api/turmas` e `GET /api/turmas/{id}`;
- validacao operacional das quatro rotas ampliadas com resposta `200` via BFF,
  trafego confirmado no `academic-catalog-service` e gate preservado pelo
  mesmo relatorio reconciliado da migracao real;
- fallback comprovado tambem nessas rotas apos parada deliberada do catalogo,
  mantendo rollback imediato para o monolito sem alterar escritas.

Entregue na decima-primeira subfase:

- ampliacao do cutover read-only para `GET /api/turmas/{turmaId}/disciplinas`,
  `GET /api/turnos`, `GET /api/academico/catalogos/turnos` e
  `GET /api/academico/catalogos/niveis-ensino`;
- validacao operacional das quatro rotas restantes com resposta `200` via BFF,
  trafego confirmado no `academic-catalog-service` e gate preservado pelo
  mesmo relatorio reconciliado da migracao real;
- fallback comprovado tambem nessas rotas apos parada deliberada do catalogo,
  mantendo rollback imediato para o monolito sem alterar escritas.

Entregue na decima-segunda subfase:

- decisao de cutover no BFF com motivo explicito por rota, em vez de booleano
  opaco;
- metricas `bff.catalog.read.route.total` por rota, alvo, motivo e resultado;
- metricas `bff.catalog.read.catalog.error.total` para falhas do servico novo e
  `bff.catalog.read.fallback.total` para fallback efetivo ao monolito;
- health dedicado em `/actuator/health/catalogReadCutover`, expondo o estado do
  gate do relatorio reconciliado;
- validacao operacional das metricas novas e do health, incluindo falha real do
  `academic-catalog-service` com fallback e incrementos coerentes no Prometheus.

Entregue na decima-terceira subfase:

- primeira escrita controlada do catalogo no BFF em `POST /api/periodos-letivos`;
- flag propria de write cutover por rota, separada do bloco read-only;
- gate de escrita reaproveitando o mesmo relatorio reconciliado da migracao
  real, antes de liberar qualquer envio ao `academic-catalog-service`;
- resolucao de `usuarioId`, `escolaId` e `escolaNome` pelo contexto autenticado
  do monolito, com rejeicao explicita quando o `escolaId` informado diverge do
  tenant autenticado;
- `Idempotency-Key` repassado pelo cliente quando presente ou gerado no BFF
  quando ausente;
- metricas `bff_catalog_write_route_total` e `bff_catalog_write_error_total`,
  mais health dedicado em `/actuator/health/catalogWriteCutover`;
- validacao automatizada cobrindo roteamento ao catalogo, retorno ao monolito
  quando a flag de escrita esta desabilitada e ausencia de fallback automatico
  ao monolito quando a tentativa de escrita no servico novo falha.

Entregue na decima-quarta subfase:

- expansao da escrita controlada do catalogo no BFF para `POST /api/disciplinas`;
- reaproveitamento do mesmo gate por relatorio reconciliado, `Idempotency-Key`,
  metricas e health de write cutover ja introduzidos na subfase anterior;
- roteamento direto ao `academic-catalog-service` apenas para payload compativel
  com o contrato novo de disciplina (`status` ausente ou `ATIVA`);
- decisao explicita de manter o monolito como destino direto quando o payload
  pede `status` nao compativel com o contrato atual do servico novo, evitando
  degradar comportamento existente enquanto a escrita ainda esta em rollout
  controlado;
- ausencia de fallback automatico para o monolito depois que a escrita tenta o
  `academic-catalog-service`;
- validacao automatizada cobrindo roteamento ao catalogo, retorno direto ao
  monolito por flag desabilitada ou `status` nao compativel, e erro do catalogo
  sem retry cruzado de escrita.

Entregue na decima-quinta subfase:

- diagnostico contratual de `POST /api/series`, confirmando a diferenca entre o
  contrato externo atual do monolito (`nivelEnsino` textual) e o contrato
  interno do `academic-catalog-service` (`nivelEnsinoId` UUID);
- adaptacao minima e isolada no BFF para resolver o `nivelEnsino` informado em
  um `nivelEnsinoId` do catalogo novo antes de tentar a escrita;
- expansao da escrita controlada do catalogo no BFF para `POST /api/series`,
  mantendo o mesmo gate por relatorio reconciliado, `Idempotency-Key`,
  metricas e health de write cutover;
- roteamento direto ao `academic-catalog-service` apenas quando `ordem` e
  `nivelEnsino` estiverem compativeis com o contrato novo;
- destino direto ao monolito quando o `nivelEnsino` vier ausente ou nao puder
  ser resolvido no catalogo novo, preservando o comportamento atual sem exigir
  refatoracao ampla do contrato externo;
- ausencia de fallback automatico para o monolito depois que a escrita tenta o
  `academic-catalog-service`;
- validacao automatizada cobrindo roteamento ao catalogo, retorno direto ao
  monolito por flag desabilitada ou `nivelEnsino` nao resolvido, e erro do
  catalogo sem retry cruzado de escrita.

Entregue na decima-sexta subfase:

- diagnostico contratual de `POST /api/turmas`, confirmando a diferenca entre o
  contrato externo atual do monolito (`turno` textual e `status` externo) e o
  contrato interno do `academic-catalog-service` (`turnoId` UUID e `ativo`
  interno);
- adaptacao minima e isolada no BFF para resolver o `turno` informado em um
  `turnoId` do catalogo novo antes de tentar a escrita;
- expansao da escrita controlada do catalogo no BFF para `POST /api/turmas`,
  mantendo o mesmo gate por relatorio reconciliado, `Idempotency-Key`,
  metricas e health de write cutover;
- roteamento direto ao `academic-catalog-service` apenas quando `capacidade`,
  `periodoLetivoId`, `serieId`, `turno` e `status` estiverem compativeis com o
  contrato novo;
- destino direto ao monolito quando o `turno` vier ausente, nao puder ser
  resolvido no catalogo novo ou o `status` nao for compativel, preservando o
  comportamento atual sem exigir refatoracao ampla do contrato externo;
- ausencia de fallback automatico para o monolito depois que a escrita tenta o
  `academic-catalog-service`;
- validacao automatizada cobrindo roteamento ao catalogo, retorno direto ao
  monolito por flag desabilitada, `turno` nao resolvido ou `status`
  incompativel, e erro do catalogo sem retry cruzado de escrita.

Proximo passo pratico: expor o contrato interno minimo do dominio de
professores por um adaptador dedicado de baixo risco, comecando por `criar
professor` e `alocar professor em turma-disciplina`, sem cutover no BFF ainda e
sem acoplar esse passo ao `academic-catalog-service`.

Entregue na decima-setima subfase:

- diagnostico contratual de `POST /api/turmas/{turmaId}/disciplinas`,
  confirmando compatibilidade direta entre o contrato externo atual do
  monolito e o contrato interno do `academic-catalog-service` para o payload de
  vinculo (`disciplinaId`, `cargaHoraria`);
- expansao da escrita controlada do catalogo no BFF para
  `POST /api/turmas/{turmaId}/disciplinas`, preservando o mesmo gate por
  relatorio reconciliado, `Idempotency-Key`, metricas e health de write
  cutover;
- roteamento direto ao `academic-catalog-service` quando a feature flag da rota
  estiver habilitada, sem necessidade de adaptacao ampla de request/response;
- retorno direto ao monolito quando a flag da rota estiver desabilitada;
- ausencia de fallback automatico para o monolito depois que a escrita tenta o
  `academic-catalog-service`;
- validacao automatizada cobrindo roteamento ao catalogo, retorno direto ao
  monolito por flag desabilitada e erro do catalogo sem retry cruzado de
  escrita.

Entregue na decima-oitava subfase:

- diagnostico contratual de `POST /api/professores`, confirmando dependencia do
  monolito em RH/pessoas via `funcionarioId`, consulta de funcionario por
  escola e regras de elegibilidade/atividade antes da criacao do professor;
- diagnostico contratual de `POST /api/professores/{id}/turmas-disciplinas`,
  confirmando dependencia do agregado academico preexistente via
  `turmaDisciplinaId` e das validacoes de duplicidade/alocacao por escola;
- confirmacao de que o `academic-catalog-service` nao expoe endpoints, modelo
  interno ou persistencia para professores e alocacoes, portanto nao existe
  destino seguro para expandir o cutover do BFF nesta frente agora;
- decisao de nao aplicar refatoracao ampla nem adicionar rota write no BFF sem
  antes definir um contrato interno proprio para o dominio de professores.

Entregue na decima-nona subfase:

- definicao do contrato interno minimo do dominio de professores no
  `school-management-service`, com DTOs internos de criacao de professor,
  consulta/resumo, alocacao professor-turma-disciplina e resumo de alocacao;
- introducao da porta interna `ProfessorAcademicoPort`, delimitando a
  interface backend/backend necessaria para futura extracao incremental do
  dominio;
- adaptacao minima do `ProfessorService` para implementar a nova porta interna
  sem alterar os endpoints externos atuais de `POST /api/professores` e
  `POST /api/professores/{id}/turmas-disciplinas`;
- validacao automatizada do contrato interno cobrindo criacao de professor,
  alocacao e bloqueio de duplicidade, mantendo a suite completa do backend
  verde.

Entregue na vigesima subfase:

- exposicao desse contrato interno de professores por um adaptador web
  dedicado e de baixo risco no `school-management-service`, sem alterar os
  endpoints externos atuais nem iniciar cutover no BFF;
- criacao de endpoints internos autenticados e com escopo explicito por
  `X-Escola-Id` para `POST /internal/professores`, `GET /internal/professores/{id}`,
  `POST /internal/professores/{id}/turmas-disciplinas` e
  `GET /internal/professores/{id}/turmas-disciplinas`;
- reaproveitamento da porta `ProfessorAcademicoPort` como fronteira
  backend/backend do dominio, preservando o monolito atual como implementacao
  unica nesta etapa;
- validacao automatizada do adaptador interno cobrindo criacao de professor,
  consulta por id, alocacao professor-turma-disciplina e listagem de alocacoes
  por escola, mantendo a suite completa do backend verde.

Proximo passo pratico: introduzir o cliente backend/backend desse adaptador
interno e consumi-lo de forma controlada no fluxo de professores, primeiro sem
mudar rotas externas do BFF, com metricas e rollback simples para preparar a
extracao incremental do dominio.

Entregue na vigesima-primeira subfase:

- introducao do cliente backend/backend `ProfessorInternalApiClient`, consumindo
  `POST /internal/professores`, `GET /internal/professores/{id}`,
  `POST /internal/professores/{id}/turmas-disciplinas` e
  `GET /internal/professores/{id}/turmas-disciplinas` com propagacao do bearer
  atual e do escopo por `X-Escola-Id`;
- criacao do `ProfessorFluxoOrquestradorService` para usar esse cliente interno
  nas operacoes externas de criacao, consulta por id, alocacao e listagem de
  alocacoes, preservando `ProfessorService` como implementacao local padrao da
  porta interna;
- protecao do consumo por feature flag
  `professor.internal-client.enabled`, com `base-url` configuravel, fallback
  local simples por `RestClientException` e metricas Micrometer para
  requisicoes/fallbacks;
- validacao automatizada com testes unitarios do orquestrador e suite completa
  do backend verde, sem alterar rotas externas do BFF nem mover o dominio para
  outro runtime.

Proximo passo pratico: habilitar esse cliente em um teste operacional
controlado, com configuracao explicita da `base-url` e verificacao ponta a ponta
do fluxo autenticado, antes de qualquer extracao fisica do dominio de
professores.

Entregue na vigesima-segunda subfase:

- validacao operacional controlada do `ProfessorInternalApiClient` com
  `professor.internal-client.enabled=true`, `fallback-local-on-error=false`,
  `base-url` explicita e servidor de teste em porta aleatoria;
- teste ponta a ponta com autenticacao real via `/api/auth/login`, propagacao do
  bearer atual para o adaptador interno e execucao HTTP completa de
  `POST /api/professores`, `GET /api/professores/{id}`,
  `POST /api/professores/{id}/turmas-disciplinas` e
  `GET /api/professores/{id}/turmas-disciplinas`;
- comprovacao automatizada de uso do cliente interno sem fallback, com leitura
  das metricas Micrometer de requisicoes e fallbacks dentro do teste;
- endurecimento do `ProfessorInternalApiClient` para resolver a `base-url` em
  tempo de uso, permitindo cenarios com `local.server.port` e mantendo a
  configuracao externa do cliente.

Proximo passo pratico: adicionar observabilidade operacional explicita para esse
consumo interno de professores, com health/readiness dedicado ou diagnostico
equivalente do cliente interno, antes de qualquer extracao fisica do dominio.

Entregue na vigesima-terceira subfase:

- exposicao do componente dedicado
  `/actuator/health/professorInternalClient` no `school-management-service`,
  sem mudar rotas externas de negocio nem abrir um novo runtime;
- diagnostico do componente cobrindo configuracao resolvida do cliente interno
  (`enabled`, `fallbackLocalOnError`, `baseUrlScheme`, `baseUrlHost`) e
  agregando os sinais operacionais das metricas ja publicadas
  (`professor.internal.client.requests` e
  `professor.internal.client.fallbacks`);
- liberacao anonima apenas de `/actuator/health/**` e `/actuator/info` para
  verificacao operacional, mantendo o restante da API protegido;
- validacao automatizada tanto da logica do `HealthIndicator` quanto da
  exposicao real do actuator em ambiente Spring Boot com porta aleatoria, sem
  remover o teste operacional autenticado do fluxo de professores via cliente
  interno.

Proxima subfase pratica e de menor risco:

- usar a fronteira interna de professores ja observada para preparar a primeira
  separacao fisica incremental do dominio, preferencialmente iniciando por
  leitura shadow/read-only em runtime proprio e nao por escrita;
- se a extracao fisica ainda nao estiver segura, introduzir antes um contrato
  interno minimo de consulta/elegibilidade de funcionario para reduzir o
  acoplamento atual entre professor e RH/pessoa sem ampliar o BFF.

Entregue na vigesima-quarta subfase:

- introducao do contrato interno minimo de funcionario para o fluxo de
  professores, com a porta `FuncionarioProfessorPort` e DTO interno proprio,
  ainda dentro do `school-management-service`;
- implementacao local em RH para busca de funcionario por escola e listagem de
  funcionarios elegiveis para cadastro de professor, sem expor repositórios ou
  entidades de RH diretamente ao dominio de professores;
- adaptacao minima do `ProfessorService`, que deixou de depender diretamente de
  `FuncionarioJpaRepository` e `FuncionarioEntity`, passando a consumir o resumo
  interno de funcionario e a materializar apenas uma referencia JPA de
  `PessoaEntity` no momento da escrita do professor;
- exposicao dos endpoints internos autenticados
  `GET /internal/funcionarios/{id}/professor` e
  `GET /internal/funcionarios/professor-elegiveis`, preparando a futura
  extracao incremental do dominio sem alterar rotas externas nem abrir cutover
  no BFF;
- validacao automatizada cobrindo os novos endpoints internos de funcionario e
  a preservacao do endpoint externo `GET /api/professores/funcionarios-elegiveis`.

Proxima subfase pratica e de menor risco:

- usar as duas fronteiras internas agora explicitas (`ProfessorAcademicoPort` e
  `FuncionarioProfessorPort`) para preparar a primeira separacao fisica
  incremental do dominio de professores, preferencialmente iniciando por
  leitura shadow/read-only em runtime proprio;
- manter a criacao e a alocacao de professor no monolito nesta etapa, usando o
  novo runtime apenas como consumidor observavel dos contratos internos, sem
  mudar rotas no BFF.

Entregue na terceira subfase da Fase 53:

- ampliacao controlada da fronteira interna de pessoas ja existente
  (`PessoaCadastroPort`) com busca por `pessoaId` e `escolaId`, sem criar novo
  runtime, sem nova persistencia e sem alterar rotas externas;
- remocao da dependencia direta de `EntityManager` em `ProfessorService`, que
  passou a resolver a pessoa do funcionario exclusivamente pela porta interna
  de pessoas antes do write local do professor;
- preservacao do fluxo de criacao de professor no monolito atual, mantendo a
  consistencia do write local e reduzindo o acoplamento interno restante entre
  professor e infraestrutura JPA compartilhada;
- validacao automatizada pela suite do backend, sem cutover no BFF e sem
  ampliar o escopo para separacao fisica adicional nesta etapa.

Proxima subfase pratica e de menor risco:

- reaplicar o mesmo padrao de fronteira interna no recorte restante de
  professores/RH, priorizando a elegibilidade e a verificacao de professor ja
  cadastrado sem expor consulta cruzada de `ProfessorJpaRepository` fora do
  limite minimo necessario;
- manter a fase restrita ao backend/backend do monolito, sem mover rotas
  externas nem abrir persistencia propria adicional.

Entregue na quarta subfase da Fase 53:

- introducao da porta interna minima `ProfessorPessoaPort`, dedicada a
  responder se uma `pessoa` ja possui professor cadastrado em determinada
  escola, sem expor repositorio JPA de professor fora do limite do dominio;
- implementacao local dessa consulta no proprio dominio de professor,
  preservando o monolito como runtime unico e sem abrir persistencia propria;
- adaptacao de `FuncionarioProfessorService`, que deixou de depender
  diretamente de `ProfessorJpaRepository` para calcular elegibilidade de
  funcionario no fluxo de criacao de professor;
- validacao automatizada pela suite do backend, mantendo o contrato externo e
  os endpoints internos existentes sem alteracao.

Proxima subfase pratica e de menor risco:

- executar a subfase minima restante para fechar o bloco interno de Fase 53,
  revisando se ainda existe dependencia concreta cruzada entre professor, RH e
  pessoas que precise da mesma fronteira interna;
- se o bloco estiver coberto, encerrar oficialmente a macrofase 53 e preparar
  a transicao para a proxima frente backend sem ampliar escopo para BFF.

Entregue na quinta subfase da Fase 53:

- substituicao do DTO web `ProfessorFuncionarioElegivelResponse` por um DTO
  interno proprio no contrato `ProfessorAcademicoPort`, removendo o ultimo
  vazamento de tipo de adaptador externo na fronteira backend/backend;
- exposicao explicita de `GET /internal/professores/funcionarios-elegiveis`
  no adaptador interno de professores, alinhando a rota real ao
  `ProfessorInternalApiClient` e ao health operacional ja publicado;
- adaptacao minima do orquestrador e do cliente interno para converter o
  contrato interno de funcionarios elegiveis para o DTO externo apenas na API
  publica `/api/professores/funcionarios-elegiveis`;
- validacao automatizada cobrindo a nova rota interna, o consumo operacional
  do cliente interno e a preservacao da suite completa do backend verde.

Proximo passo pratico:

- considerar o bloco interno da Fase 53 fechado no `school-management-service`
  e iniciar a proxima macrofase backend a partir desse estado, sem reabrir
  esse recorte salvo se surgir necessidade concreta na extracao fisica seguinte;
- a partir daqui, a evolucao sugerida volta a mirar a frente seguinte de
  desacoplamento, e nao mais o ajuste fino interno deste bloco.

Entregue na vigesima-quinta subfase:

- criacao do modulo `academic-professor-service` no monorepo como primeiro
  runtime proprio do dominio de professores em modo shadow/read-only;
- exposicao da API interna do novo runtime em `/internal/v1`, protegida por
  token interno e contexto obrigatorio (`X-Correlation-Id`, `X-Usuario-Id`,
  `X-Escola-Id`), sem banco proprio e sem escrita;
- consumo HTTP, a partir do runtime novo, dos contratos internos ja
  estabilizados no monolito para leitura de professor por id, listagem de
  alocacoes e listagem de funcionarios elegiveis;
- manutencao do `Authorization` apenas como header propagado ao monolito para a
  leitura shadow, sem alterar o contrato externo atual nem abrir cutover no
  BFF;
- validacao automatizada do runtime shadow com `MockWebServer`, cobrindo
  roteamento interno, propagacao de headers, protecao por token interno e
  mapeamento de 404 do monolito.

Entregue na vigesima-sexta subfase:

- expansao do contrato interno do monolito com `GET /internal/professores` e
  `GET /internal/professores/turmas/{turmaId}`, reaproveitando a fronteira
  `ProfessorAcademicoPort` para separar leitura geral e leitura por turma sem
  refatoracao ampla;
- adaptacao do `ProfessorService` e do cliente interno do proprio monolito para
  suportar essas leituras como parte do contrato backend/backend ja estabilizado;
- expansao do `academic-professor-service` com `GET /internal/v1/professores`
  e `GET /internal/v1/turmas/{turmaId}/professores`, mantendo o runtime novo
  apenas como consumidor shadow/read-only dos contratos internos do monolito;
- validacao automatizada dos dois lados: teste de integracao do adaptador
  interno no `school-management-service` e teste com `MockWebServer` no
  `academic-professor-service`, cobrindo roteamento, headers e respostas das
  novas leituras;
- preservacao integral do escopo: nenhuma escrita movida, nenhum banco novo,
  nenhuma alteracao de rota externa no BFF e nenhum cutover para frontend.

Entregue na vigesima-setima subfase:

- extensao do `ProfessorFluxoOrquestradorService` para aplicar o cliente
  interno observavel tambem nas leituras `GET /api/professores` e
  `GET /api/turmas/{turmaId}/professores`, mantendo a mesma feature flag,
  metricas por operacao e fallback local imediato ja usados em criacao,
  consulta por id e listagem de alocacoes;
- alinhamento do `TurmaProfessorController` ao mesmo orquestrador, evitando
  caminho local paralelo e preservando observabilidade consistente nas duas
  rotas read-only alvo;
- ampliacao dos testes unitarios do orquestrador para cobrir sucesso remoto em
  listagem geral e fallback em listagem por turma;
- ampliacao do teste operacional autenticado do backend para comprovar, em
  runtime, o consumo observavel das duas leituras shadow e o incremento das
  metricas correspondentes sem acionar fallback;
- preservacao integral do escopo: nenhuma escrita movida, nenhuma alteracao de
  contrato externo, nenhum cutover no BFF e nenhum banco novo.

Entregue na vigesima-oitava subfase:

- ampliacao do `ProfessorInternalClientHealthIndicator` para expor diagnostico
  operacional explicito das rotas shadow de leitura de professores no backend
  atual;
- publicacao, no health dedicado `professorInternalClient`, de um mapa por
  operacao contendo rota externa, rota interna correspondente e contadores
  separados de sucesso interno, erro interno, fallback local, execucao local
  por feature desabilitada e total de fallbacks;
- cobertura explicita das leituras `GET /api/professores`,
  `GET /api/professores/{id}`,
  `GET /api/professores/{id}/turmas-disciplinas` e
  `GET /api/turmas/{turmaId}/professores`, sem alterar o comportamento
  funcional dessas rotas;
- validacao automatizada por teste unitario do indicador, teste de endpoint do
  actuator e preservacao do teste operacional autenticado do cliente interno;
- preservacao integral do escopo: nenhuma escrita movida, nenhum contrato
  externo alterado, nenhum cutover no BFF e nenhum banco novo.

Entregue na vigesima-nona subfase:

- instrumentacao do `MonolithProfessorReadClient` no
  `academic-professor-service` com metricas por operacao e resultado, cobrindo
  sucesso, `not_found` e erro ao consumir o monolito;
- criacao do `HealthIndicator` dedicado `professorShadowMonolith`, expondo no
  actuator do runtime shadow um mapa por rota `/internal/v1` com a rota
  correspondente do monolito, contadores por resultado e total de falhas por
  operacao;
- exposicao explicita dos sinais de dependencia remota no health do shadow,
  incluindo base URL, host, porta e timeouts do cliente do monolito;
- validacao automatizada por teste unitario do indicador, teste de endpoint do
  actuator do runtime shadow e preservacao dos testes de integracao ja
  existentes das leituras shadow;
- preservacao integral do escopo: nenhuma escrita movida, nenhum contrato
  externo alterado, nenhum cutover no BFF e nenhum banco novo.

Entregue na trigesima subfase:

- criacao de um smoke operacional controlado do `academic-professor-service`
  em porta aleatoria, exercitando em runtime real da aplicacao shadow os
  cenarios de sucesso, `not_found` e erro remoto;
- consulta automatizada ao health dedicado `professorShadowMonolith` ao final
  do smoke para comprovar os contadores por rota e os sinais agregados da
  dependencia remota;
- preservacao dos testes anteriores do runtime shadow, mantendo o contrato
  funcional de `/internal/v1` inalterado;
- preservacao integral do escopo: nenhuma escrita movida, nenhum contrato
  externo alterado, nenhum cutover no BFF e nenhum banco novo.

Entregue na trigesima primeira subfase:

- preparacao de um perfil controlado `professor-shadow-operational` no
  `school-management-service`, usando H2 e seed minimo exclusivo de smoke para
  disponibilizar autenticacao, contexto escolar e um professor real sem exigir
  PostgreSQL externo;
- criacao do script operacional
  `scripts/operational/professor-shadow-operational-smoke.ps1`, responsavel por
  subir `school-management-service` e `academic-professor-service` em portas
  controladas, autenticar no monolito, executar leituras externas e shadow e
  consolidar no mesmo relatorio os healths
  `/actuator/health/professorInternalClient` e
  `/actuator/health/professorShadowMonolith`;
- preservacao do escopo: nenhuma escrita movida, nenhum contrato externo
  alterado, nenhum cutover no BFF e nenhuma dependencia nova de infraestrutura.

Entregue na trigesima segunda subfase:

- execucao real do smoke operacional controlado por
  `scripts/operational/professor-shadow-operational-smoke.ps1`, com subida
  local dos dois runtimes em portas livres e geracao do relatorio operacional
  `target/professor-shadow-operational-smoke/report.json`;
- endurecimento minimo dos contratos internos para o fluxo ponta a ponta:
  compatibilidade no `academic-professor-service` com as rotas internas atuais
  do monolito sob `/internal/**`, aplicacao do interceptor interno tambem nessas
  rotas e repasse de `X-Internal-Token`, `X-Correlation-Id` e
  `X-Usuario-Id` pelo cliente interno do `school-management-service`;
- comprovacao operacional real de leitura em `GET /api/professores` e
  `GET /api/professores/{id}` no monolito, mais `GET /internal/v1/professores`
  e `GET /internal/v1/professores/{id}` no runtime shadow, sem fallback e com
  healths dos dois lados em `UP`;
- preservacao do escopo: nenhuma escrita movida, nenhum contrato externo
  alterado, nenhum cutover no BFF e nenhuma dependencia nova de infraestrutura.

Proxima subfase pratica e de menor risco:

- ampliar o smoke operacional real para cobrir `listarFuncionariosElegiveis` e
  `listarPorTurma`, consolidando no mesmo relatorio os contadores e sinais
  dessas duas rotas no `school-management-service` e no
  `academic-professor-service`;
- manter criacao, alocacao e qualquer escrita ainda no monolito ate que essa
  cobertura read-only adicional esteja comprovada em execucao real.

Entregue na trigesima terceira subfase:

- ampliacao do smoke operacional real para cobrir
  `GET /api/professores/funcionarios-elegiveis`,
  `GET /api/turmas/{turmaId}/professores`,
  `GET /internal/v1/professores/funcionarios-elegiveis` e
  `GET /internal/v1/turmas/{turmaId}/professores`, consolidando no mesmo
  `target/professor-shadow-operational-smoke/report.json` os contadores
  positivos dessas rotas e os healths dos dois lados;
- endurecimento minimo do perfil `professor-shadow-operational` no
  `school-management-service`, com seed controlado das dependencias minimas de
  catalogo (`nivel_ensino`, `turno`, `serie`, `periodo_letivo`, `turma`,
  `disciplina`, `turma_disciplina`, `professor_turma_disciplina`) e de um
  funcionario elegivel sem vinculo com professor, apenas para viabilizar o
  cenario operacional real em H2 sem depender de `data.sql` de teste;
- comprovacao operacional real de que o monolito e o runtime shadow respondem
  essas leituras adicionais com `status` funcional, `professorShadowMonolith`
  e `professorInternalClient` em `UP` e ausencia de fallback no backend
  principal;
- preservacao do escopo: nenhuma escrita movida, nenhum contrato externo
  alterado, nenhum cutover no BFF e nenhuma dependencia nova de infraestrutura.

Proxima subfase pratica e de menor risco:

- ampliar o smoke operacional real para cobrir `listarAlocacoes`
  (`GET /api/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/professores/{id}/turmas-disciplinas`), fechando o bloco
  read-only do runtime shadow de professores com o mesmo padrao de relatorio,
  metricas e health operacional;
- manter criacao, alocacao e qualquer escrita ainda no monolito ate que essa
  ultima leitura read-only esteja comprovada em execucao real.

Entregue na trigesima quarta subfase:

- ampliacao final do smoke operacional real para cobrir
  `GET /api/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/professores/{id}/turmas-disciplinas`, consolidando no
  mesmo `target/professor-shadow-operational-smoke/report.json` os contadores
  reais de `listarAlocacoes` junto de `listar`, `buscarPorId`,
  `listarPorTurma` e `listarFuncionariosElegiveis`;
- comprovacao operacional real de que o monolito e o runtime shadow respondem
  todo o bloco read-only de professores com `status` funcional,
  `professorShadowMonolith` e `professorInternalClient` em `UP` e ausencia de
  fallback no backend principal;
- preservacao do escopo: nenhuma escrita movida, nenhum contrato externo
  alterado, nenhum cutover no BFF e nenhuma persistencia propria introduzida no
  runtime shadow.

Proxima subfase pratica e de menor risco:

- iniciar o diagnostico operacional minimo da primeira escrita shadow de
  professores, com foco em `POST /api/professores`: revisar contrato, seed
  operacional, metricas, health e ponto exato de observabilidade antes de
  qualquer tentativa de cutover externo;
- manter criacao, alocacao e qualquer escrita ainda no monolito ate que esse
  passo de escrita shadow esteja comprovado com risco controlado e sem ampliar
  escopo para persistencia propria ou BFF.

Entregue na trigesima quinta subfase:

- implementacao do menor passo de escrita shadow de professores com risco
  controlado: `POST /internal/v1/professores` no
  `academic-professor-service`, atuando apenas como proxy do contrato interno
  `POST /internal/professores` do monolito, sem banco proprio e sem BFF;
- preservacao do contexto obrigatorio e da seguranca interna
  (`Authorization`, `X-Correlation-Id`, `X-Usuario-Id`, `X-Escola-Id` e
  `X-Internal-Token`) tambem para essa primeira escrita shadow;
- ampliacao dos healths dedicados `professorShadowMonolith` e
  `professorInternalClient` para incluir a operacao `criar` no diagnostico por
  rota e nos contadores operacionais;
- ampliacao do smoke operacional real em
  `scripts/operational/professor-shadow-operational-smoke.ps1` para executar
  `POST /api/professores`, validar a consulta do professor criado pelo runtime
  shadow e comprovar no `report.json` healths em `UP`, contador de `criar` nos
  dois lados e ausencia de fallback no backend principal;
- preservacao do escopo: nenhuma persistencia propria no runtime shadow,
  nenhuma mudanca de rotas no BFF e nenhuma alteracao de contrato externo para
  frontend.

Proxima subfase pratica e de menor risco:

- aplicar o mesmo padrao minimo ao segundo write interno de professores:
  `POST /api/professores/{id}/turmas-disciplinas`, com proxy shadow sem
  persistencia propria, observabilidade dedicada e smoke operacional
  controlado;
- manter criacao, alocacao e qualquer escrita ainda fora do BFF ate esse bloco
  backend/backend estar comprovado ponta a ponta com risco controlado.

Entregue na trigesima sexta subfase:

- implementacao do mesmo padrao minimo para o segundo write interno de
  professores:
  `POST /internal/v1/professores/{id}/turmas-disciplinas` no
  `academic-professor-service`, atuando apenas como proxy do contrato interno
  `POST /internal/professores/{id}/turmas-disciplinas` do monolito, sem banco
  proprio e sem BFF;
- ampliacao dos healths dedicados `professorShadowMonolith` e
  `professorInternalClient` para incluir a operacao
  `vincularTurmaDisciplina` no diagnostico por rota e nos contadores
  operacionais;
- ampliacao do smoke operacional real em
  `scripts/operational/professor-shadow-operational-smoke.ps1` para criar um
  professor, aloca-lo via `POST /api/professores/{id}/turmas-disciplinas`,
  validar a leitura da alocacao criada pelo runtime shadow e comprovar no
  `report.json` healths em `UP`, contadores de `vincularTurmaDisciplina` nos
  dois lados e ausencia de fallback no backend principal;
- preservacao do escopo: nenhuma persistencia propria no runtime shadow,
  nenhuma mudanca de rotas no BFF e nenhuma alteracao de contrato externo para
  frontend.

Proxima subfase pratica e de menor risco:

- revisar se ainda resta algum write interno de professores fora desse bloco
  minimo backend/backend; se nao restar, encerrar oficialmente a Fase 51D,
  preparar a PR para `Master` e iniciar a proxima fase em um chat novo;
- manter o combinado de parar antes da proxima fase para o fechamento da 51D.

Entregue na trigesima setima subfase:

- abertura da primeira persistencia propria controlada do
  `academic-professor-service`, ainda sem BFF e sem cutover externo, com
  migrations locais para `professor_shadow`, `professor_alocacao_shadow` e
  estados de sincronizacao por escola, professor e turma;
- criacao de um migrador opt-in de professores do monolito para a copia local,
  com runner controlado por propriedade, dry-run/aplicacao e relatorio JSON
  reconciliado por escola;
- validacao automatizada da migracao cobrindo repeticao segura, reconciliacao
  e deteccao de divergencias de destino;
- preservacao do escopo: monolito segue como autoridade funcional e nenhuma
  rota externa do BFF foi alterada.

Proxima subfase pratica e de menor risco:

- automatizar o registro local dos writes shadow ja fechados
  (`POST /api/professores` e
  `POST /api/professores/{id}/turmas-disciplinas`) imediatamente apos o
  sucesso do proxy para o monolito, com metricas, health e rollback simples;
- evitar qualquer ampliacao para BFF ou cutover externo enquanto a copia local
  ainda nao estiver observavel e validada.

Entregue na trigesima oitava subfase:

- automacao controlada da persistencia local dos writes shadow de professores
  no `academic-professor-service`, registrando a criacao de professor e a
  alocacao professor-turma-disciplina depois do sucesso do proxy ao monolito;
- validacoes explicitas de divergencia de escola, identidade e alocacao na
  copia local, com tratamento configuravel por `failOnError`;
- metricas dedicadas `professor.shadow.local.persistence.*` e actuator
  `professorShadowPersistence` para diagnosticar sucesso, divergencia, erro e
  volume persistido localmente;
- preservacao do escopo: o monolito continua sendo a autoridade da escrita e a
  copia local permanece apenas como reflexo controlado para o proximo passo de
  leitura.

Proxima subfase pratica e de menor risco:

- aplicar o mesmo criterio incremental na leitura local do runtime shadow,
  priorizando rotas que possam depender de um estado de sincronizacao completo
  antes de responder localmente;
- manter fallback controlado para o monolito enquanto a sincronizacao local nao
  estiver comprovadamente completa por escola, professor ou turma.

Entregue na trigesima nona subfase:

- primeiro cutover controlado de leitura dentro do proprio
  `academic-professor-service`, ainda sem BFF: `GET /internal/v1/professores`,
  `GET /internal/v1/professores/{id}/turmas-disciplinas` e
  `GET /internal/v1/turmas/{turmaId}/professores` passaram a preferir a copia
  local quando o respectivo estado de sincronizacao estiver completo;
- fallback explicito para o monolito quando a feature estiver desabilitada ou
  o sync state estiver incompleto, com motivo operacional registrado em
  `professor.shadow.local.read.requests`;
- ampliacao do actuator `professorShadowPersistence` para expor a estrategia de
  leitura, contadores locais/fallback e resumo dos sync states usados por cada
  rota;
- preservacao do escopo: `listarFuncionariosElegiveis` continua remoto e nenhum
  contrato externo do BFF foi alterado.

Proxima subfase pratica e de menor risco:

- fechar o bloco restante do primeiro cutover controlado de leitura com
  `GET /internal/v1/professores/{id}`, adotando uma decisao binaria local sem
  fallback quando a propriedade de cutover estiver ativa;
- manter rollback simples por propriedade, sem abrir qualquer redirecionamento
  externo no BFF.

Entregue na quadragesima subfase:

- fechamento do primeiro cutover controlado de leitura por id no
  `academic-professor-service`: quando
  `professor.shadow.local-persistence.buscar-por-id-cutover-enabled=true`,
  `GET /internal/v1/professores/{id}` responde diretamente da copia local e
  retorna `404 RESOURCE_NOT_FOUND` sem consultar o monolito quando o registro
  nao existir localmente;
- validacao automatizada de nao-acesso remoto, rollback por propriedade e
  diagnostico no actuator `professorShadowPersistence`, incluindo a estrategia
  `local_record_presence_required_no_fallback`;
- fechamento objetivo da Fase 51D no dominio de professores: writes shadow
  observaveis, persistencia propria controlada, leituras locais incrementais e
  nenhuma alteracao de rotas externas no BFF.

Proxima fase pratica:

- iniciar a Fase 52 com diagnostico pontual de identidade e tenant no recorte
  backend/backend, mapeando contratos internos minimos, dependencias no
  monolito e impactos sobre sessao, autorizacao e vinculo usuario-escola;
- manter o mesmo criterio incremental: sem cutover externo, sem refatoracao
  ampla e sem extracao fisica do dominio antes do diagnostico objetivo.

Entregue na primeira subfase da Fase 52:

- diagnostico objetivo do estado atual de identidade e tenant no monolito:
  `AuthService` ainda concentra login, refresh, logout, validacao do access
  token opaco e resolucao do contexto autenticado a partir de
  `sessao_autenticacao`, enquanto `EscolaTenantService` segue como autoridade
  local de tenant;
- confirmacao de que o tenant ativo continua implicito em `usuario.id_escola`
  com fallback para a escola padrao `00000000-0000-0000-0000-000000000047`;
  ainda nao existe `usuario_escola`, troca explicita de escola ativa ou uma
  fronteira separada para autorizacao de acesso por tenant;
- confirmacao de que o BFF atual depende do contrato externo
  `GET /api/auth/contexto-atual` no monolito para resolver `usuarioId`,
  `escolaId` e `escolaNome`, portanto qualquer extracao prematura de identity ou
  tenant quebraria o strangler atual;
- mapeamento do acoplamento remanescente: o monolito ainda possui dezenas de
  consumidores diretos de `EscolaTenantService` e `EscolaContextoPort`, o que
  torna arriscado tentar extracao fisica antes de estabilizar uma fronteira
  interna minima;
- definicao do menor contrato seguro para a proxima subfase:
  1. resolver sessao/autenticacao por token opaco;
  2. resolver tenant ativo e autorizacao de acesso por usuario;
  3. expor contexto autenticado interno com `usuarioId`, `escolaId`,
     `escolaNome`, perfis e permissoes sem depender diretamente das entidades
     JPA de seguranca nos consumidores.

Proxima subfase pratica:

- introduzir, ainda dentro do `school-management-service`, a fronteira interna
  minima de identidade/tenant por portas e DTOs proprios, sem criar runtime
  novo e sem alterar o contrato externo do BFF;
- manter `AuthController` e `GET /api/auth/contexto-atual` inalterados nesta
  etapa, apenas fazendo-os delegar para a nova fronteira interna;
- preservar rollback trivial, porque a implementacao continuara no mesmo
  runtime e nas mesmas tabelas (`usuario`, `usuario_perfil`, `perfil`,
  `perfil_permissao`, `permissao`, `sessao_autenticacao` e `escola`).

Entregue na segunda subfase da Fase 52:

- a fronteira interna minima de identidade/tenant foi introduzida dentro do
  `school-management-service` por meio da porta `IdentidadeTenantPort`, dos
  DTOs internos `SessaoAutenticadaResumo` e `ContextoAutenticadoResumo` e da
  implementacao `IdentidadeTenantService`, ainda no mesmo runtime e sem
  alteracao de schema;
- `AuthService` deixou de concentrar a regra de autenticacao e contexto e
  passou a atuar como fachada de compatibilidade para o contrato externo atual,
  apenas delegando para a nova fronteira interna e montando `AuthResponse` e
  `AuthContextResponse`;
- o contrato externo usado pelo BFF foi preservado sem mudanca de rota ou
  payload: `POST /api/auth/login`, `POST /api/auth/refresh`,
  `POST /api/auth/logout` e `GET /api/auth/contexto-atual` continuam estaveis;
- o rollback desta subfase permanece trivial, porque a logica continua nas
  mesmas tabelas e no mesmo runtime, sem cutover externo, sem nova migration e
  sem dependencia operacional adicional;
- a separacao interna reduz o acoplamento imediato entre consumidores futuros e
  `AuthService`, criando o ponto minimo para evoluir depois a autoridade de
  tenant e o vinculo usuario-escola sem refatoracao ampla nesta etapa.

Proxima subfase pratica:

- revisar os consumidores internos mais proximos do contexto autenticado para
  decidir o primeiro candidato seguro a passar a depender da nova fronteira
  `IdentidadeTenantPort`, sem mudar rotas externas nem iniciar extracao fisica;
- diagnosticar o menor passo seguro para explicitar a autoridade de tenant
  alem de `usuario.id_escola`, separando o que continua local do que precisara
  de `usuario_escola` e selecao de escola ativa nas proximas subfases da Fase
  52.

Entregue na terceira subfase da Fase 52:

- o primeiro consumidor interno da nova fronteira `IdentidadeTenantPort` foi
  aplicado na cadeia de seguranca do proprio monolito, que era o ponto mais
  proximo e de menor risco para deixar de depender da fachada `AuthService`;
- `JwtAuthenticationFilter` e `SecurityBeansConfig` passaram a consumir a porta
  interna diretamente apenas para resolucao do principal autenticado por access
  token, sem alterar a autenticacao externa nem o contrato REST de
  `/api/auth/**`;
- para evitar que a nova fronteira continuasse vazando entidade JPA de
  seguranca, foi introduzido o DTO interno `PrincipalAutenticadoResumo`, com
  `usuarioId`, `username` e permissoes, removendo da borda a dependencia
  imediata de `UsuarioEntity` e do hash de senha;
- `AuthService` ficou ainda mais restrito ao papel de fachada do contrato
  externo de autenticacao/contexto, enquanto o consumo interno de seguranca
  passou a ocorrer pela porta nova;
- o rollback continua trivial, porque a troca aconteceu apenas dentro do mesmo
  runtime e manteve as mesmas tabelas, o mesmo filtro HTTP e o mesmo fluxo de
  bearer token opaco.

Proxima subfase pratica:

- diagnosticar o menor passo seguro para explicitar a autoridade de tenant alem
  de `usuario.id_escola`, separando o que ainda pode continuar local do que
  exigira `usuario_escola` e selecao explicita de escola ativa;
- manter o recorte no backend/backend, sem abrir rota externa nova no BFF e
  sem iniciar extracao fisica antes desse diagnostico controlado.

Entregue na quarta subfase da Fase 52:

- a autoridade de tenant deixou de permanecer apenas implicita em
  `EscolaTenantService` e passou a existir como contrato interno explicito por
  meio da porta `TenantAtivoPort` e dos DTOs `TenantAtivoResumo` e
  `OrigemTenantAtivo`;
- o fluxo atual do monolito agora diferencia, de forma declarada e testavel, a
  origem da escola ativa em tres casos: escola vinda da sessao autenticada,
  escola vinculada em `usuario.id_escola` e fallback para a escola padrao;
- `IdentidadeTenantService` passou a depender dessa nova fronteira para
  resolver tenant na criacao de sessao, no refresh e na leitura do contexto
  autenticado, sem alterar o contrato externo `/api/auth/**`;
- o resultado pratico desta subfase nao e a introducao de `usuario_escola`
  ainda, e sim a criacao do ponto minimo que separa claramente o comportamento
  atual legado da futura evolucao para escola ativa explicita;
- o rollback continua trivial porque o runtime, o schema e as rotas externas
  permanecem inalterados.

Proxima subfase pratica:

- fechar a macrofase preparatoria de identidade/tenant com o desenho do menor
  passo para introduzir vinculo explicito `usuario_escola` e selecao de escola
  ativa sem cutover externo imediato;
- manter o trabalho no backend/backend, ainda sem extracao fisica dos servicos
  `identity-access-service` e `institutional-tenant-service`.

Entregue na quinta subfase da Fase 52:

- foi aplicado o primeiro passo real e aditivo para sair de `usuario.id_escola`
  como unica fonte estrutural de vinculo usuario-escola: a nova tabela
  `usuario_escola` passou a existir por migration propria, com backfill inicial
  a partir do estado atual de `usuario.id_escola`;
- a fundacao foi mantida sem cutover de leitura: o tenant ativo continua sendo
  resolvido pelo comportamento legado ja explicitado em `TenantAtivoPort`,
  enquanto `usuario_escola` passa a ser preenchida e preservada para a proxima
  etapa;
- foi criado o contrato interno `UsuarioEscolaPort`, com implementacao
  `UsuarioEscolaService`, para garantir o vinculo explicito e listar as escolas
  do usuario sem depender de SQL espalhado pelos casos de uso;
- o write de usuario no backend passou a sincronizar esse vinculo de forma
  aditiva e de baixo risco: `UsuarioInteractor` agora garante em
  `usuario_escola` a escola corrente do usuario no create e no update, sem
  remover vinculos anteriores e sem alterar o contrato externo de usuarios;
- o rollback continua simples, porque a etapa apenas adiciona tabela,
  repositorio e sincronizacao complementar, sem trocar a autoridade atual de
  leitura do tenant nem exigir mudanca no BFF.

Proxima subfase pratica:

- usar essa fundacao para desenhar e implementar o menor fluxo interno de
  selecao explicita de escola ativa, ainda sem expor rota externa nova no BFF;
- definir como `sessao_autenticacao.id_escola` passa de reflexo do legado para
  resultado de uma escolha validada contra `usuario_escola`.

Entregue na sexta subfase da Fase 52:

- foi implementado o primeiro fluxo interno real de selecao explicita de escola
  ativa, ainda sem criar rota externa no BFF e sem alterar o contrato externo
  de `/api/auth/**`;
- a nova rota interna autenticada `GET /internal/auth/escolas` lista as escolas
  disponiveis para a sessao atual a partir de `usuario_escola`, marcando qual
  delas esta ativa naquele momento;
- a nova rota interna autenticada `POST /internal/auth/escola-ativa` permite
  trocar a escola ativa da sessao, validando se o usuario possui vinculo com a
  escola informada antes de persistir a troca em `sessao_autenticacao.id_escola`;
- com isso, `sessao_autenticacao.id_escola` deixa de ser apenas reflexo do
  legado e passa a suportar uma escolha validada contra `usuario_escola`, sem
  mudar ainda o login externo nem exigir cutover do BFF;
- o rollback continua simples, porque a selecao explicita ficou restrita a
  endpoints internos do mesmo runtime e a atualizacao pontual da sessao opaca.

Proxima subfase pratica:

- consolidar o fechamento da macrofase 52, definindo se o login continua com
  selecao implicita por default ou se a proxima etapa deve aceitar escolha de
  escola no fluxo autenticado sem romper compatibilidade externa;
- preparar o ponto minimo para a futura extracao fisica de
  `identity-access-service` e `institutional-tenant-service`, agora que sessao,
  vinculo usuario-escola e escolha de tenant ja possuem fronteiras internas
  separadas.

Entregue na setima subfase da Fase 52:

- o bloco de identidade e tenant foi fechado com a definicao compativel do
  login multiescola: `POST /api/auth/login` passou a aceitar `escolaId`
  opcional, sem quebrar o payload ja consumido pelos clientes atuais;
- quando `escolaId` nao e informado, o comportamento continua implicito e
  retrocompativel, reaproveitando a resolucao legada da escola ativa por
  default;
- quando `escolaId` e informado, o backend valida o vinculo do usuario contra
  `usuario_escola` antes de abrir a sessao, e a sessao autenticada ja nasce com
  essa escola ativa persistida em `sessao_autenticacao.id_escola`;
- a troca explicita iniciada na sexta subfase e a escolha opcional no login
  agora cobrem os dois pontos minimos do ciclo de sessao sem exigir mudanca no
  BFF nem no frontend nesta etapa;
- com isso, a macrofase 52 fica encerrada no backend atual com fronteiras
  internas explicitas para sessao, vinculo usuario-escola, tenant ativo e
  selecao de escola, reduzindo o risco da futura extracao fisica de
  `identity-access-service` e `institutional-tenant-service`.

Proxima fase pratica:

- iniciar a preparacao fisica minima dos modulos `identity-access-service` e
  `institutional-tenant-service`, reaproveitando os contratos internos ja
  estabilizados no monolito;
- manter o recorte backend/backend, sem cutover de BFF e sem migracao ampla de
  persistencia nesta primeira etapa da extracao fisica.

Entregue na primeira subfase da Fase 53:

- foi iniciado o desacoplamento de `people-service` pelo menor ponto ainda
  acoplado a seguranca no monolito: o relacionamento JPA direto entre
  `professor` e `usuario`;
- `ProfessorEntity` deixou de depender de `UsuarioEntity` como `@ManyToOne` e
  passou a tratar `id_usuario` apenas como referencia externa por UUID,
  preservando a mesma coluna fisica e sem exigir migracao de schema;
- `ProfessorJpaRepository` e a resolucao de `professorId` na autenticacao foram
  ajustados para usar essa referencia simples, mantendo o comportamento externo
  de login e contexto autenticado;
- com isso, a Fase 53 comeca atacando exatamente o objetivo de remover
  relacionamentos JPA entre people e seguranca, sem abrir runtime novo cedo
  demais e sem ampliar escopo para eventos ou BFF nesta etapa.

Proxima subfase pratica:

- aplicar o mesmo criterio de desacoplamento nas fronteiras de pessoa usadas por
  aluno, responsavel, professor e funcionario, definindo o primeiro contrato
  interno de `people-service` que permita preparar a extracao fisica sem
  refatoracao ampla;
- somente depois disso avaliar a abertura do modulo `people-service` em modo
  shadow/backend-backend.

Entregue na segunda subfase da Fase 53:

- foi criado o primeiro contrato interno explicito do futuro `people-service`
  no proprio monolito, encapsulando o nucleo compartilhado de cadastro base de
  pessoa e endereco usado por aluno e responsavel;
- a nova porta interna `PessoaCadastroPort` passou a expor criacao/atualizacao
  de pessoa com tipo e endereco, consulta por CPF e catalogos de tipos, com a
  implementacao atual ainda delegada a `PessoaFoundationService`;
- `AlunoPersistenceGateway`, `ResponsavelPersistenceGateway` e
  `PessoaCatalogoController` deixaram de depender diretamente da implementacao
  concreta, passando a consumir a fronteira interna do dominio de people;
- com isso, a Fase 53 avanca do desacoplamento ORM com seguranca para o
  desacoplamento contratual do cadastro base de pessoas, sem abrir runtime novo
  ainda e sem alterar contratos externos.

Proxima subfase pratica:

- aplicar o mesmo padrao de fronteira interna no recorte de funcionario e
  professor, definindo o menor contrato de people para elegibilidade e consulta
  cadastral antes da extracao fisica do `people-service`;
- manter o foco backend/backend e evitar publicar eventos antes de fechar essas
  fronteiras internas minimas.
