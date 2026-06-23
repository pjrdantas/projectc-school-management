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

### Fase 55 - Pedagogico

Extrair `pedagogical-service` com alocacao, aula, frequencia, avaliacao, notas,
boletim e historico.

### Fase 56 - Planejamento e IA

Extrair `planning-ai-service`, ativar MongoDB para payloads flexiveis, Kafka para
publicacao e Redis para locks/rate limit.

### Fase 57 - Dashboard orientado a eventos

Extrair `dashboard-query-service`, substituir consultas cruzadas por projecoes
Kafka, usar MongoDB para historico detalhado e Redis para respostas recentes.

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
