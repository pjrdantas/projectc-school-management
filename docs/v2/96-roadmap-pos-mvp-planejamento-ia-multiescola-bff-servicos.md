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

## Regra operacional para fases internas, shadow e cutover

Antes de introduzir complexidade operacional, a fase precisa ser classificada
corretamente.

Quando a fase estiver restrita ao backend atual/monolito, sem novo runtime
recebendo trafego, sem BFF redirecionando rota, sem migracao fisica de banco,
sem alteracao de contrato externo e sem troca real de caminho da aplicacao, nao
criar cutover, modo shadow, feature flag operacional, smoke distribuido ou
infraestrutura adicional.

Para essas fases internas, seguir apenas este fluxo:

1. diagnosticar o menor acoplamento;
2. implementar a fronteira interna;
3. preservar o contrato REST existente;
4. preservar comportamento funcional;
5. criar ou ajustar testes necessarios;
6. executar testes;
7. registrar objetivamente a entrega no roadmap.

Cutover so deve ser introduzido quando uma rota real passar a ser redirecionada
para outro runtime, por exemplo:

- BFF deixando de chamar o monolito e passando a chamar um servico novo;
- frontend passando a consumir o BFF no lugar do monolito;
- escrita oficial saindo do monolito e indo para outro servico;
- leitura oficial deixando de vir do monolito e passando a vir de servico
  extraido.

Shadow so deve ser introduzido quando ja existir um novo runtime executando em
paralelo para leitura, comparacao, observabilidade ou validacao de
comportamento, sem assumir trafego oficial.

Mesmo nesses casos, `shadow` e modo operacional e nao deve aparecer como nome
de classe, controller, service, use case, porta, DTO, package ou teste de
negocio. Esse vocabulario deve ficar em documentacao, roadmap, configuracao,
profile, metrica, log e plano operacional.

Resumo da regra:

- fase interna no monolito: desenvolver, testar e registrar;
- novo servico read-only sem trafego oficial: health/smoke simples; shadow
  apenas se houver comparacao real;
- rota migrada para BFF/servico novo: cutover obrigatorio com feature flag e
  rollback;
- escrita migrada para servico novo: cutover obrigatorio com reconciliacao,
  idempotencia e rollback planejado;
- producao final: plano operacional de cutover obrigatorio.

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

Insumo funcional adicional do MVP inicial:

- a pasta `projetos-historico-diario` passa a ser referencia oficial para duas
  entregas futuras deste dominio: `diario de classe` e nova experiencia de
  `historico escolar`;
- o `diario de classe` deve atender preferencialmente professor, coordenacao
  pedagogica e direcao, com consulta restrita para secretaria apos assinatura e
  checagem, sem permitir alteracao de informacoes ja validadas nas etapas
  anteriores;
- a tela nova de `historico escolar` deve substituir a experiencia atual,
  preservando o valor documental do dominio e ampliando o fluxo para cadastro
  inicial incompleto, pendencias, certificado evolutivo e importacao de PDF
  como pre-preenchimento revisavel;
- os documentos desse pacote tambem sugerem ajustes incrementais de banco e de
  contrato backend que devem ser tratados como insumo de futuras macrofases, e
  nao como mudanca imediata dentro do bloco atualmente encerrado de `boletim`.

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

Novo insumo de API/UX a considerar nas proximas macrofases:

- futura familia `/api/diarios-classe` como parte do dominio pedagogico, com
  contrato proprio de carregamento mensal, salvamento do dia corrente,
  bloqueio apos assinatura e workflow de checagem por papeis;
- substituicao evolutiva do contrato de `/api/historicos-escolares` para
  suportar cadastro inicial com pendencias, edicao progressiva, bloqueio formal
  e importacao de PDF;
- a padronizacao visual das novas telas de diario e historico deve orientar a
  futura macrofase de consolidacao de layout do frontend, sem alterar a frente
  backend atual nesta etapa.

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

Entregue na terceira subfase da macrofase seguinte de `dashboard`:

- foi executado o fechamento formal deste bloco minimo de composicao interna,
  confirmando que `DashboardAlertaService` esgota o proximo recorte seguro de
  baixo risco depois do bloco anterior de resumos internos;
- nao foi identificado outro consumidor de composicao comparavel entre esse
  ponto e o proximo salto de escopo: o que resta no modulo passa
  essencialmente por `DashboardFrontendService`, que agrega resumo, alertas,
  configuracao administrativa, configuracao por usuario e historico de
  snapshots, ou por `DashboardProfessorService`, que segue concentrando
  consultas especificas mais pesadas por professor;
- por esse motivo, este bloco fica encerrado sem nova refatoracao nesta
  subfase: o backend atual ja separou os resumos reutilizaveis e o primeiro
  consumidor de composicao de menor risco, mantendo rollback simples e nenhum
  impacto em rotas externas;
- nao houve alteracao de schema, nao houve migracao de dados e nao houve uso de
  Kafka, MongoDB, Redis, BFF ou outra infraestrutura distribuida neste
  fechamento formal.

Proxima fase pratica:

- iniciar a proxima macrofase de `dashboard` com diagnostico pontual para
  decidir explicitamente entre dois caminhos remanescentes:
  `DashboardFrontendService` como proximo recorte de composicao externa ou
  `DashboardProfessorService` como proximo recorte de dominio mais pesado;
- manter o mesmo criterio incremental: sem cutover externo e sem infraestrutura
  distribuida antes desse novo diagnostico objetivo.

Entregue na primeira subfase da macrofase seguinte:

- foi executado o diagnostico comparativo entre `DashboardFrontendService` e
  `DashboardProfessorService` como proximos recortes remanescentes do modulo de
  dashboard;
- `DashboardProfessorService` foi mantido fora do proximo passo imediato
  porque continua concentrando consultas especificas de professor, alocacoes,
  aulas, frequencias, avaliacoes, notas e planejamentos, o que caracteriza um
  recorte de dominio mais pesado e com maior risco de ampliacao de escopo;
- `DashboardFrontendService` foi escolhido como proximo recorte minimo seguro,
  porque ele opera como camada de composicao sobre servicos ja existentes, nao
  possui consultas JPA proprias, ja depende do `DashboardAlertaService`
  estabilizado e permite reaproveitar de forma incremental as fronteiras
  internas abertas nos blocos anteriores;
- a decisao preserva o criterio de menor risco: primeiro separar mais uma
  composicao backend/backend antes de entrar no recorte mais pesado de
  `DashboardProfessorService`.

Impactos e consistencia mapeados:

- nenhuma rota externa precisa mudar no proximo passo, porque a evolucao pode
  permanecer confinada ao `DashboardFrontendService` e aos contratos internos
  ja existentes do proprio modulo;
- a consistencia continua sincrona e baseada em leitura do PostgreSQL do
  monolito, sem eventos, sem cache distribuido e sem mudanca de persistencia;
- o rollback segue simples, porque o proximo recorte pode ficar limitado a
  trocas de dependencia e mapeamento interno do pacote agregado de dashboard
  frontend.

Proxima subfase pratica:

- aplicar a mesma fronteira interna minima em `DashboardFrontendService`,
  substituindo primeiro os consumos concretos de `academico`, `secretaria` e
  `diretor` pelas portas internas ja existentes, mantendo `professor`,
  configuracao e snapshots conforme estao nesta etapa;
- manter o escopo backend/backend e incremental, sem BFF, sem eventos e sem
  refatoracao ampla do recorte de professor.

Entregue na segunda subfase da macrofase seguinte:

- `DashboardFrontendService` passou a consumir `DashboardAcademicoPort`,
  `DashboardSecretariaPort` e `DashboardDiretorPort` nos caminhos
  `ACADEMICO`, `SECRETARIA` e `DIRETOR`, reaproveitando as fronteiras internas
  abertas nas macrofases anteriores;
- o payload externo do pacote agregado de dashboard frontend foi preservado por
  mapeamento interno dos resumos backend/backend para os DTOs REST ja
  existentes, sem alteracao de rota, sem mudanca de schema e sem impacto em
  configuracao, historico ou alertas;
- o caminho `PROFESSOR` permaneceu dependente de `DashboardProfessorService`,
  mantendo o criterio de menor risco e evitando abrir ainda o recorte mais
  pesado de consultas especificas por professor;
- o rollback continua simples, porque a mudanca ficou confinada ao service de
  composicao e aos contratos internos ja existentes do mesmo modulo.

Proxima subfase pratica:

- fechar formalmente este bloco minimo da macrofase atual, confirmando se
  `DashboardFrontendService` esgota o ultimo recorte seguro antes de qualquer
  entrada em `DashboardProfessorService`;
- manter o escopo backend/backend e incremental, sem BFF, sem eventos e sem
  refatoracao ampla de dominio nesta etapa de fechamento.

Entregue na terceira subfase da macrofase seguinte:

- foi executado o fechamento formal deste bloco minimo de `dashboard`,
  confirmando por testes do agregador que os caminhos `ACADEMICO`,
  `SECRETARIA` e `DIRETOR` ja consomem exclusivamente as portas internas
  abertas no proprio modulo;
- com isso, o unico caminho ainda concreto dentro de
  `DashboardFrontendService` passou a ser `PROFESSOR`, que depende de
  `DashboardProfessorService` e concentra um recorte de dominio mais pesado do
  que a composicao frontend/backend encerrada nesta macro-subfase;
- configuracao administrativa, configuracao por usuario e historico de
  snapshots permaneceram como componentes transversais do pacote agregado e nao
  caracterizam, nesta etapa, um novo recorte seguro equivalente ao que foi
  fechado para `academico`, `secretaria` e `diretor`;
- por esse motivo, `DashboardFrontendService` esgota o ultimo recorte seguro de
  composicao minima antes de qualquer entrada em `DashboardProfessorService`, e
  este bloco fica encerrado sem alterar BFF, sem eventos e sem nova
  persistencia.

Proxima subfase pratica:

- iniciar uma nova macro-subfase de diagnostico comparativo para decidir se o
  proximo passo backend/backend do modulo `dashboard` deve abrir
  `DashboardProfessorService` como recorte mais pesado ou se ainda existe algum
  corte estrutural mais seguro fora dele;
- manter o mesmo criterio incremental, sem cutover externo, sem refatoracao
  ampla e sem introduzir runtime distribuido novo nesta etapa.

Entregue na primeira subfase da macrofase seguinte:

- foi executado o diagnostico comparativo do bloco remanescente de `dashboard`
  e ficou confirmado que nao existe, fora de `DashboardProfessorService`, um
  recorte backend/backend menor com ganho equivalente;
- `DashboardConfiguracaoAdminService`, `DashboardUsuarioConfiguracaoService` e
  `DashboardIndicadorSnapshotService` foram descartados como proximo passo
  porque sao componentes transversais de configuracao e historico, com CRUD e
  persistencia proprios, sem a mesma concentracao de consultas cruzadas que
  motivou as fronteiras internas anteriores;
- `DashboardProfessorService` foi confirmado como o proximo candidato real
  porque permanece sem porta interna propria e segue consumido diretamente por
  quatro pontos do modulo: controller REST, `DashboardFrontendService`,
  `DashboardAlertaService` e `DashboardSnapshotGeradorService`;
- o risco principal nao esta no contrato externo atual, mas no fato de o
  resumo do professor ainda sair como DTO web direto de um service que conhece
  repositorios de `professor`, `aula`, `frequencia`, `avaliacao`, `nota` e
  `planejamento`, o que amplia o acoplamento backend/backend no proprio modulo;
- por isso, o menor proximo passo seguro nao e quebrar consultas por agregado
  ou tentar extracao fisica imediata, e sim criar primeiro uma fronteira
  interna explicita para o resumo de professor.

Impactos e consistencia mapeados:

- nenhuma rota externa do dashboard precisa mudar na proxima subfase, porque o
  recorte pode ficar restrito a contrato interno, troca de dependencia e
  mapeamento local dentro do proprio modulo;
- a consistencia continua sincrona e baseada na mesma leitura do PostgreSQL do
  monolito, sem eventos, sem cache distribuido e sem nova persistencia;
- o rollback permanece simples, pois a implementacao concreta de
  `DashboardProfessorService` pode continuar a mesma enquanto os consumidores
  backend/backend passam gradualmente a depender da nova porta.

Proxima subfase pratica:

- introduzir `DashboardProfessorPort` e um DTO interno proprio para o resumo de
  professor, mantendo `DashboardProfessorService` como implementacao concreta
  inicial da nova fronteira;
- aplicar primeiro essa troca de dependencia em `DashboardAlertaService` e
  `DashboardSnapshotGeradorService`, que sao os consumidores backend/backend de
  menor risco, deixando controller REST e `DashboardFrontendService` para a
  subfase seguinte;
- manter o escopo backend/backend, sem cutover externo, sem eventos e sem
  refatoracao ampla das consultas do professor nesta etapa.

Entregue na segunda subfase da macrofase seguinte:

- foi introduzida a fronteira interna `DashboardProfessorPort`, com DTO proprio
  de resumo de professor, mantendo `DashboardProfessorService` como
  implementacao concreta inicial sem alterar o contrato REST ja exposto;
- `DashboardAlertaService` e `DashboardSnapshotGeradorService` deixaram de
  depender diretamente de `DashboardProfessorService` e passaram a consumir a
  nova porta interna, reduzindo o acoplamento backend/backend nos dois
  consumidores de menor risco definidos no diagnostico anterior;
- o controller REST de professor e `DashboardFrontendService` permaneceram
  intencionalmente inalterados nesta etapa, preservando o critério de menor
  risco antes de abrir a troca nos pontos ainda ligados ao payload externo;
- o rollback continua simples, porque a mudanca ficou confinada ao contrato
  interno de professor, ao mapeamento local do proprio service e a dois
  consumidores internos do modulo.

Proxima subfase pratica:

- aplicar a mesma fronteira interna restante em `DashboardFrontendService` e,
  se fizer sentido manter o mesmo criterio incremental, avaliar tambem a troca
  do controller REST apenas como adaptador externo do service concreto;
- manter o escopo backend/backend e incremental, sem BFF, sem eventos e sem
  refatoracao ampla das consultas internas do professor.

Entregue na terceira subfase da macrofase seguinte:

- `DashboardFrontendService` deixou de depender diretamente de
  `DashboardProfessorService` no caminho `PROFESSOR` e passou a consumir
  `DashboardProfessorPort`, concluindo a aplicacao da nova fronteira interna
  nos consumidores backend/backend do pacote agregado;
- o payload externo do dashboard de professor foi preservado por mapeamento
  local de `DashboardProfessorResumo` para `DashboardProfessorResponse`, sem
  alteracao de rota, sem mudanca de schema e sem impacto nos demais publicos;
- o controller REST de professor permaneceu como unico consumidor direto do
  service concreto nesta etapa, o que mantem o fechamento do bloco dentro do
  criterio de menor risco e sem abrir refatoracao externa adicional;
- com isso, o bloco minimo de fronteira interna de professor fica encerrado sem
  BFF, sem eventos e sem mudanca de persistencia.

Proxima subfase pratica:

- fechar formalmente este bloco, confirmando que o controller REST de professor
  pode permanecer como adaptador externo simples nesta macrofase e que nao
  resta outro consumo backend/backend fora da nova porta;
- manter o escopo incremental, sem cutover externo e sem abrir refatoracao
  ampla do dominio de professor nesta etapa de fechamento.

Entregue na quarta subfase da macrofase seguinte:

- foi executado o fechamento formal deste bloco de `dashboard` orientado ao
  resumo de professor;
- ficou confirmado que os consumos backend/backend do modulo passaram a ficar
  concentrados em `DashboardProfessorPort`, hoje reutilizado por
  `DashboardAlertaService`, `DashboardSnapshotGeradorService` e
  `DashboardFrontendService`;
- `DashboardProfessorController` permaneceu como unico consumidor direto de
  `DashboardProfessorService`, atuando apenas como adaptador externo do payload
  REST existente, sem caracterizar novo acoplamento interno a ser tratado nesta
  macrofase;
- por isso, o bloco tecnico fica encerrado sem alterar rota externa, sem BFF,
  sem eventos e sem nova persistencia, preservando rollback simples pelo
  proprio wiring interno do modulo.

Proxima subfase pratica:

- iniciar a proxima macrofase de diagnostico pontual no modulo `dashboard`,
  decidindo se o proximo passo incremental deve abrir o controller REST de
  professor como adaptador da nova porta ou se a frente seguinte deve migrar
  para outro recorte backend com melhor relacao risco/ganho;
- manter o criterio de menor risco, sem cutover externo e sem refatoracao
  ampla de dominio antes desse novo diagnostico.

Entregue na subfase seguinte:

- o diagnostico comparativo final foi executado e concluiu que nao vale abrir
  nova macrofase em `dashboard`, porque o uso restante de
  `DashboardProfessorController` ja e apenas de adaptacao REST externa sobre a
  porta interna estabilizada `DashboardProfessorPort`;
- a comparacao com `PlanejamentoBimestralService` e
  `HistoricoEscolarServiceImpl` mostrou que esses blocos ja avancaram mais no
  criterio de fronteiras internas, tenant explicito e consumo por portas,
  ficando com menor retorno arquitetural imediato para a proxima etapa;
- o melhor proximo alvo incremental passa a ser `MatriculaFluxoService`, que
  ainda concentra write transacional relevante com dependencias diretas de
  `matricula`, `documento`, `catalogo` e `historico`, alem de impacto de
  consistencia local e rollback mais sensivel;
- por isso, a proxima macrofase backend deve sair do modulo `dashboard` e abrir
  o primeiro recorte minimo de `matricula`, sem alterar rotas externas no BFF
  e sem iniciar persistencia propria fora do monolito nesta etapa.

Proxima subfase pratica:

- abrir diagnostico pontual do menor write/controlador interno de `matricula`
  com menor risco de separacao, priorizando contrato interno, dependencias
  cruzadas, consistencia transacional e rollback local;
- usar `MatriculaFluxoService` como ponto de partida para escolher a primeira
  fronteira interna de write da nova macrofase, evitando refatoracao ampla do
  modulo inteiro.

Entregue na subfase seguinte:

- foi aberto o primeiro recorte minimo de write da nova macrofase exatamente no
  ponto de menor risco identificado em `MatriculaFluxoService`: a atualizacao
  de status de etapa da matricula;
- foi criado o contrato interno `MatriculaEtapaPort` com o DTO proprio
  `AtualizarMatriculaEtapaStatusSolicitacao`, separando esse write do payload
  REST externo e deixando explicita a primeira fronteira interna do bloco;
- `MatriculaFluxoService` permaneceu como implementacao local unica nesta
  etapa, preservando o mesmo runtime, as mesmas tabelas e o mesmo comportamento
  transacional de `matricula_etapa` e seus catalogos de status;
- `MatriculaController` passou a atuar apenas como adaptador do request externo
  para esse contrato interno minimo no write de etapa, sem mudar rota publica,
  sem abrir cliente HTTP interno e sem ampliar escopo para os demais writes de
  `documento`, `historico` ou `rematricula`.

Proxima subfase pratica:

- aplicar o mesmo criterio ao proximo write de `MatriculaFluxoService` com
  maior ganho arquitetural ainda controlado, priorizando `registrarDocumentoEntregue`
  por ser o primeiro ponto que explicita a dependencia cruzada com `documento`
  e a transicao local de status da matricula;
- manter a evolucao no backend atual, sem BFF, sem persistencia propria e sem
  endpoint interno HTTP nesta etapa seguinte.

Entregue na subfase seguinte:

- foi aplicado o mesmo criterio ao write `registrarDocumentoEntregue`, mantendo
  a evolucao da macrofase de `matricula` no menor passo ainda seguro e sem
  abrir refatoracao ampla do service inteiro;
- foi criado o contrato interno `MatriculaDocumentoEntreguePort` com o DTO
  proprio `RegistrarMatriculaDocumentoEntregueSolicitacao`, separando o write
  do payload REST externo e deixando explicita a fronteira do primeiro ponto de
  cruzamento entre `matricula` e `documento`;
- `MatriculaFluxoService` permaneceu como implementacao local unica, mantendo a
  mesma persistencia em `matricula_documento_entregue`, a validacao de
  `documento` por escola e a atualizacao local de status da matricula quando os
  obrigatorios ficam completos;
- `MatriculaController` passou a atuar apenas como adaptador do request externo
  para esse contrato interno minimo tambem nesse write, sem criar endpoint
  interno HTTP, sem cliente backend/backend e sem tocar ainda os blocos de
  `historico` ou `rematricula`.

Proxima subfase pratica:

- fechar formalmente este primeiro bloco minimo da macrofase de `matricula`,
  revisando se os writes mais seguros de `MatriculaFluxoService` ja ficaram
  cobertos por fronteiras internas antes de decidir se o proximo passo deve
  abrir `concluirAcademicamente` ou se o bloco atual pode ser encerrado;
- manter o criterio incremental, ainda sem BFF, sem persistencia propria e sem
  endpoint interno HTTP nesta etapa de fechamento.

Entregue na subfase seguinte:

- foi executado o fechamento formal desse primeiro bloco minimo da macrofase de
  `matricula`, sem ampliar o escopo tecnico para os writes mais pesados do
  service;
- a revisao confirmou que os dois writes de menor risco e melhor retorno
  imediato de `MatriculaFluxoService` (`atualizarStatusEtapa` e
  `registrarDocumentoEntregue`) ja contam com fronteiras internas explicitas e
  controller atuando apenas como adaptador de contrato externo;
- tambem ficou confirmado que os proximos candidatos naturais,
  `concluirAcademicamente` e `rematricular`, ja elevam o risco arquitetural por
  dependerem mais fortemente de `boletim`, `historico`, capacidade de turma e
  regras academicas de progressao, nao cabendo mais neste primeiro bloco
  minimo;
- com isso, o bloco tecnico atual de `matricula` fica encerrado mantendo o
  mesmo runtime, as mesmas tabelas, rollback simples e nenhuma abertura de
  endpoint interno HTTP ou persistencia propria adicional nesta etapa.

Proxima subfase pratica:

- iniciar o proximo bloco da macrofase de `matricula` por diagnostico pontual
  de `concluirAcademicamente`, separando contrato interno, dependencias em
  `boletim/historico`, impactos de consistencia e criterio de rollback antes de
  qualquer troca concreta;
- manter o recorte backend/backend e incremental, ainda sem BFF e sem
  persistencia propria fora do monolito nessa proxima abertura.

Entregue na subfase seguinte:

- foi executado o diagnostico pontual de `concluirAcademicamente` como abertura
  do proximo bloco da macrofase de `matricula`;
- o fluxo foi classificado como estruturalmente mais pesado do que os writes do
  primeiro bloco minimo, porque depende de `BoletimJpaRepository`,
  `BoletimItemJpaRepository`, validacao de pertencimento do boletim a
  matricula, verificacao de itens pendentes e decisao do status final
  `CONCLUIDA` ou `EFETIVADA` a partir do resultado academico;
- o diagnostico tambem confirmou que ja existe no modulo de `historico` a porta
  `BoletimHistoricoPort`, hoje usada para geracao de historico escolar, mas
  ainda sem um contrato explicito e dedicado para a necessidade especifica de
  conclusao academica da matricula;
- por isso, o menor proximo passo seguro nao e trocar todo o write de uma vez,
  e sim abrir primeiro a fronteira interna do resumo academico de boletim que
  `concluirAcademicamente` realmente consome, preservando rollback simples e
  evitando misturar nessa mesma etapa `historico` completo ou `rematricula`.

Proxima subfase pratica:

- implementar a primeira fronteira interna minima para `concluirAcademicamente`,
  separando o contrato de leitura academica de boletim consumido por
  `MatriculaFluxoService`, preferencialmente reaproveitando ou evoluindo a base
  ja existente em `BoletimHistoricoPort` sem refatoracao ampla do modulo de
  `historico`;
- manter a alteracao no backend atual, sem endpoint interno HTTP, sem BFF e sem
  persistencia propria adicional nesta etapa.

Entregue na subfase seguinte:

- foi implementada a primeira fronteira interna minima para
  `concluirAcademicamente` pela evolucao controlada de `BoletimHistoricoPort`;
- foi criado o resumo dedicado `BoletimConclusaoAcademicaResumo`, contendo so o
  necessario para o desfecho academico da matricula: `boletimId`,
  `matriculaId` e os resultados dos itens fechados;
- `BoletimHistoricoService` passou a expor essa leitura especializada e
  `MatriculaFluxoService` deixou de consultar diretamente `BoletimJpaRepository`
  e `BoletimItemJpaRepository`, passando a depender da porta interna do modulo
  de `historico` para validar pertencimento do boletim, itens pendentes e
  resultado final;
- a etapa preservou o mesmo runtime, as mesmas tabelas e o mesmo contrato REST
  externo de `POST /api/matriculas/{id}/conclusao-academica`, sem criar
  endpoint interno HTTP, sem BFF e sem abrir ainda o recorte completo de
  `historico` ou `rematricula`.

Proxima subfase pratica:

- fechar formalmente este segundo bloco minimo da macrofase de `matricula`,
  confirmando que `concluirAcademicamente` ja depende da fronteira interna
  apropriada e decidir se o proximo passo deve abrir `rematricular` ou se ainda
  resta algum ajuste pontual pequeno dentro do mesmo recorte;
- manter o criterio incremental e backend/backend, sem persistencia propria
  adicional nesta etapa de fechamento.

Entregue na subfase seguinte:

- o bloco de `concluirAcademicamente` ficou formalmente encerrado como recorte
  minimo ja apoiado em fronteira interna propria, sem dependencia direta de
  repositorios de `boletim` no fluxo de conclusao academica;
- a decisao seguinte foi abrir `rematricular` pelo menor ponto de risco:
  primeiro a consulta de elegibilidade, antes de mexer na escrita operacional
  `POST /api/matriculas/{id}/rematricula`;
- foi criada a fronteira interna `MatriculaRematriculaPort`, implementada por
  `MatriculaRematriculaService`, com o resumo dedicado
  `MatriculaRematriculaElegibilidadeResumo` para encapsular a leitura da
  matricula base, turma destino, capacidade, serie posterior e duplicidade no
  periodo letivo de destino;
- `MatriculaFluxoService` deixou de concentrar diretamente essa regra de
  elegibilidade, passando a apenas adaptar a resposta externa do endpoint
  `GET /api/matriculas/{id}/rematricula/elegibilidade`, enquanto a criacao
  final continua local e inalterada em `CriarMatriculaUseCase`.

Proxima subfase pratica:

- aplicar o mesmo padrao de fronteira interna ao write operacional de
  `rematricular`, reduzindo o acoplamento restante de `MatriculaFluxoService`
  com a carga da matricula base antes da chamada final a `CriarMatriculaUseCase`;
- manter o recorte no backend atual, sem BFF, sem endpoint interno HTTP novo e
  sem persistencia propria adicional nesta etapa.

Entregue na subfase seguinte:

- foi aplicado o mesmo padrao de fronteira interna ao write operacional minimo
  de `rematricular`, sem mudar o contrato externo de
  `POST /api/matriculas/{id}/rematricula` e sem mexer ainda no
  `CriarMatriculaUseCase`;
- `MatriculaRematriculaPort` passou a expor tambem o resumo
  `MatriculaRematriculaBaseResumo`, contendo apenas `matriculaBaseId`,
  `alunoId` e `statusBase`, que sao os unicos dados realmente consumidos pelo
  fluxo operacional antes da criacao da nova matricula;
- `MatriculaFluxoService` deixou de carregar diretamente a matricula base em
  `rematricular`, passando a validar o status `CONCLUIDA` e resolver o
  `alunoId` pela porta interna de `rematricula`, enquanto a criacao final
  permanece centralizada e inalterada em `CriarMatriculaUseCase`.

Proxima subfase pratica:

- fechar formalmente este bloco de `rematricula`, revisando se o fluxo externo
  ja ficou reduzido ao consumo das fronteiras internas apropriadas e se ainda
  resta algum ajuste pontual pequeno antes de encerrar a macrofase atual;
- manter o criterio incremental, backend/backend e sem persistencia propria
  adicional nessa etapa de fechamento.

Entregue na subfase seguinte:

- foi executado o fechamento formal do bloco de `rematricula` apos a revisao
  final do fluxo externo;
- a revisao confirmou que, dentro desse recorte, `MatriculaFluxoService`
  permaneceu apenas como adaptador/orquestrador fino dos endpoints externos de
  rematricula, sem voltar a carregar diretamente a matricula base ou a turma de
  destino para elegibilidade e write minimo;
- `MatriculaRematriculaPort` ficou consolidada como a fronteira interna do
  bloco, concentrando a leitura de elegibilidade e da base minima necessaria,
  enquanto `CriarMatriculaUseCase` segue como implementacao central da criacao
  final;
- por isso, a macrofase atual de `matricula` fica encerrada neste ponto sem
  abrir novo endpoint interno HTTP, sem persistencia propria adicional e sem
  ampliar a mudanca para outro write mais pesado na mesma etapa.

Proxima subfase pratica:

- iniciar a proxima macrofase backend por diagnostico pontual do proximo fluxo
  concentrado ainda remanescente no monolito, escolhendo um recorte minimo com
  risco comparavel ao que foi aplicado em `matricula`;
- manter a linha incremental, backend/backend e sem BFF nessa abertura.

Entregue na subfase seguinte:

- foi executado o diagnostico comparativo da primeira macrofase backend apos o
  fechamento de `matricula`, sem reabrir esse bloco e sem ampliar escopo para
  BFF ou persistencia propria adicional;
- a comparacao entre candidatos mais pesados, como `AlunoPersistenceGateway`, e
  candidatos estruturais menores confirmou que o proximo recorte incremental
  mais seguro esta em `BoletimService`, dentro do bloco pedagogico;
- o diagnostico mostrou que o calculo de rendimento ja foi desacoplado por
  `RendimentoAcademicoPort`, mas `BoletimService` ainda cruza diretamente os
  dominios de `matricula` e `catalogo` ao carregar `MatriculaEntity` por
  `MatriculaJpaRepository`, navegar seus dados para compor resposta e resolver
  `DisciplinaEntity` por `DisciplinaJpaRepository` no fechamento persistido;
- por criterio de menor risco, o primeiro recorte pratico recomendado nao e
  mexer no fechamento inteiro de uma vez nem abrir exclusao/coordenacao de
  aluno, e sim separar primeiro o resumo interno de matricula que `boletim`
  realmente consome, preservando o mesmo contrato externo de
  `/api/matriculas/{id}/boletim` e `/fechamento`.

Proxima subfase pratica:

- iniciar o novo bloco pedagogico por uma fronteira interna minima de
  `matricula` para `boletim`, extraindo do `BoletimService` o resumo de
  matricula usado na consulta e no fechamento do boletim, sem tocar ainda na
  resolucao estrutural de `disciplina` nem na persistencia oficial do
  fechamento;
- manter o recorte backend/backend, incremental e sem BFF nessa implementacao
  inicial.

Entregue na subfase seguinte:

- foi criada a fronteira interna minima `MatriculaBoletimPort`, implementada
  por `MatriculaBoletimService`, para encapsular a leitura escopada da
  matricula usada por `BoletimService`;
- o novo resumo `MatriculaBoletimResumo` passou a concentrar apenas os dados
  realmente consumidos por `boletim`: `matriculaId`, identificacao do aluno,
  turma, periodo letivo e escola;
- `BoletimService` deixou de consultar `MatriculaJpaRepository` diretamente
  para a consulta do boletim calculado, para o fechamento e para a listagem de
  fechamentos, passando a depender dessa fronteira interna de leitura;
- para manter o recorte estritamente minimo, a persistencia oficial do
  fechamento continuou local no proprio fluxo de `boletim`, com
  `MatriculaEntity` restrita ao ponto de referencia JPA do novo
  `BoletimEntity`, sem mexer ainda na resolucao estrutural de `disciplina`.

Proxima subfase pratica:

- aplicar o mesmo criterio ao proximo detalhe estrutural minimo do bloco de
  `boletim`, avaliando se a resolucao de `disciplina` no fechamento persistido
  pode sair para uma fronteira interna dedicada antes de qualquer mudanca maior
  de modelo;
- manter a evolucao backend/backend, sem BFF e sem persistencia propria
  adicional nesta etapa.

Entregue na subfase seguinte:

- o detalhe estrutural minimo remanescente de `disciplina` em `boletim` foi
  extraido para a fronteira interna `DisciplinaBoletimPort`, com o resumo
  `DisciplinaBoletimResumo` implementado em `CatalogoAcademicoInternalService`;
- `BoletimService` deixou de consultar `DisciplinaJpaRepository` diretamente no
  fechamento persistido e passou a usar resumo interno por escola, mantendo
  `DisciplinaEntity` apenas como referencia JPA local para gravar
  `boletim_item`;
- o recorte continuou sem alterar rotas externas no BFF, sem persistencia
  propria nova e sem refatoracao ampla.

Proxima subfase pratica:

- revisar o bloco restante de `boletim` para fechamento formal deste ciclo e
  confirmar se nao resta leitura ou resolucao estrutural direta fora do padrao
  interno ja aplicado;
- se a revisao confirmar o bloco limpo, preparar a transicao para o proximo
  recorte backend/backend sem ampliar escopo nesta mesma fase.

Entregue na subfase final deste bloco:

- a revisao formal do recorte de `boletim` confirmou que nao restou leitura
  direta por repositorio de `matricula` ou `disciplina` em `BoletimService`,
  ficando o fluxo restrito aos contratos internos `MatriculaBoletimPort`,
  `RendimentoAcademicoPort` e `DisciplinaBoletimPort`;
- os unicos vinculos JPA remanescentes ficaram limitados aos pontos locais de
  persistencia oficial (`MatriculaEntity` para `BoletimEntity` e
  `DisciplinaEntity` para `boletim_item`), sem reabrir acoplamento de leitura;
- com isso, o primeiro ciclo de cutover controlado backend/backend do bloco de
  `boletim` fica formalmente encerrado no backend atual, sem alterar rota
  externa, sem BFF e sem persistencia propria adicional nesta macrofase.

Proxima fase sugerida:

- iniciar o proximo diagnostico backend/backend fora de `boletim`, priorizando
  um novo recorte minimo com baixo risco transacional e baixo acoplamento de
  escrita antes de qualquer movimento maior de persistencia propria.

Entregue na abertura da macrofase seguinte:

- foi executado o diagnostico comparativo inicial entre as duas novas frentes
  do pacote `projetos-historico-diario`: `diario de classe` e substituicao da
  tela de `historico escolar`;
- `diario de classe` foi classificado como recorte mais pesado neste momento,
  porque exige agregacao mensal propria, regras de dia util, bloqueio apos
  assinatura, checagem por coordenacao/direcao e novas estruturas de
  persistencia sobre o bloco pedagogico atual de `aula`, `frequencia`,
  `planejamento` e `avaliacao`;
- a nova experiencia de `historico escolar` foi classificada como o menor
  primeiro recorte seguro da macrofase, porque pode iniciar por contrato de
  leitura e carregamento da tela dentro do dominio de `historico`, que ja possui
  endpoint e base oficial ativos;
- por esse motivo, a primeira subfase pratica recomendada passa a ser abrir o
  carregamento de `historico escolar` em modo cadastro/edicao com contexto de
  matricula, pendencias e dados documentais, sem substituir ainda o salvamento
  atual, sem importacao de PDF nesta etapa e sem mover ainda para o workflow
  mais pesado de `diario de classe`.
- essa primeira subfase pratica passa a ficar entregue de forma aditiva no
  `school-management-service`, com os endpoints
  `GET /api/historicos-escolares/novo` e
  `GET /api/historicos-escolares/{id}/carregamento` alimentando a nova tela sem
  alterar o contrato externo atual de criacao/edicao, sem trocar o fluxo de
  persistencia vigente e sem introduzir importacao de PDF neste recorte.
- a subfase seguinte recomendada para esse mesmo bloco tambem fica entregue no
  menor recorte possivel: os campos documentais basicos ja presentes no
  contrato atual de `historico escolar` passam a ser persistidos de fato no
  agregado, reduzindo o uso de fallback em modo edicao sem alterar o BFF, sem
  abrir nova rota externa e sem introduzir ainda as estruturas mais amplas de
  cabecalho, periodos, pendencias ou importacao de PDF propostas no pacote
  `projetos-historico-diario`.
- o fechamento desse bloco minimo tambem fica entregue: o agregado
  `historico_escolar` passa a persistir o contexto minimo de workflow da nova
  tela (`id_matricula`, `status`, `bloqueado`, serie atual/origem, escola de
  origem e data de transferencia) com enriquecimento automatico no fluxo atual,
  permitindo que a leitura em edicao reutilize primeiro esse snapshot antes de
  buscar fallback dinamico no restante do monolito.
- o primeiro bloco backend de `diario de classe` tambem fica entregue no
  `school-management-service`, ainda sem BFF e sem frontend: a leitura mensal
  consolida dados de alunos, frequencias, planejamento, observacoes,
  avaliacoes, assinatura e bloqueio; a escrita controlada cria o lancamento
  persistido, vincula a aula do dia, grava frequencias obrigatorias para todos
  os alunos ativos e bloqueia nova escrita para a mesma alocacao/data apos a
  assinatura do professor.
- a subfase final do diario fecha a regra minima de governanca operacional:
  `PUT /api/diarios-classe/{idDiarioClasse}` so aceita lancamento no dia
  corrente e em dia util. A checagem por coordenacao/direcao e a governanca de
  reabertura/correcao ficam explicitamente fora deste MVP inicial e devem ser
  diagnosticadas antes de qualquer ampliacao.
- o diagnostico controlado do workflow de checagem do diario conclui que a
  primeira ampliacao segura nao deve ser endpoint publico nem migration de
  status ainda. O prototipo funcional nao especifica contrato de checagem, e o
  monolito atual ainda nao possui uma fronteira interna que transforme o
  usuario autenticado em autoridade pedagogica verificavel por escola,
  funcionario ativo e cargo (`COORDENADOR` ou `DIRETOR`).
- a subfase pratica seguinte introduziu essa fronteira interna de autoridade
  pedagogica no `school-management-service`, com DTO/porta propria e
  implementacao local sobre seguranca, pessoa/RH e tenant. O contrato resolve o
  access token em usuario, escola, funcionario ativo e cargo permitido
  (`COORDENADOR` ou `DIRETOR`), ainda sem endpoint publico, sem BFF, sem
  frontend e sem nova migration.
- a proxima ampliacao segura e modelar os campos/transicoes internas de
  checagem do diario sobre essa fronteira, mantendo o workflow sem cutover
  externo ate que rollback, consistencia e estados minimos estejam fechados.
- essa ampliacao interna foi materializada no `school-management-service` com
  metadados auditaveis em `diario_classe_lancamento` para checagem por
  coordenacao e direcao, extensao controlada dos status
  (`CHECADO_COORDENACAO`, `CHECADO_DIRECAO`) e servico transacional interno que
  aplica a sequencia `BLOQUEADO -> CHECADO_COORDENACAO -> CHECADO_DIRECAO`
  usando `AutoridadePedagogicaPort`. A subfase ainda nao cria endpoint publico,
  nao altera BFF e nao toca frontend.
- o proximo recorte seguro passa a ser expor o primeiro adaptador controlado
  para acionar essas transicoes, mantendo o mesmo contrato interno, sem permitir
  payload que burle cargo, escola ou ordem de checagem.
- o adaptador backend controlado foi aberto em `/api/diarios-classe/lancamentos/{idLancamento}/checagens/coordenacao`
  e `/api/diarios-classe/lancamentos/{idLancamento}/checagens/direcao`. As
  rotas exigem Bearer token, nao aceitam escola/cargo no payload e delegam a
  validacao para `DiarioClasseChecagemService` + `AutoridadePedagogicaPort`,
  preservando a ordem obrigatoria da transicao e mantendo BFF/frontend fora do
  escopo.
- com isso, o bloco backend de checagem do diario fica pronto para fechamento
  formal antes de qualquer evolucao de BFF ou tela.
- o fechamento formal confirma que o recorte backend de checagem do diario esta
  encerrado: existem fronteira de autoridade pedagogica, metadados persistidos,
  transicoes internas, migration, endpoints backend controlados e testes
  automatizados cobrindo a sequencia minima. Nao houve BFF, frontend ou
  permissao de cliente informar escola/cargo/autoridade.
- a proxima decisao de macrofase deve escolher entre: (1) iniciar o recorte
  BFF/frontend do diario usando os endpoints ja controlados; ou (2) manter a
  frente exclusivamente backend e abrir nova familia funcional, preservando o
  diario como bloco backend fechado.
- a decisao tomada para a macrofase seguinte foi manter a frente exclusivamente
  backend e retomar a nova experiencia de `historico escolar`, porque o diario
  ja esta fechado no recorte backend e o proximo risco ainda deve ser reduzido
  antes de qualquer BFF/frontend.
- a primeira subfase dessa macrofase abriu a fronteira interna de pendencias do
  novo historico escolar no `school-management-service`: o calculo de
  pendencias de cadastro e edicao saiu de `HistoricoEscolarServiceImpl` e passou
  para `HistoricoEscolarPendenciaPort`, com implementacao local propria. O
  contrato HTTP permanece igual, sem migration, sem BFF e sem frontend.
- essa separacao deixa preparada a proxima evolucao controlada: decidir se as
  pendencias continuam apenas calculadas ou se passam a ter persistencia propria
  minima em `historico_escolar_pendencia`. Contagem da macrofase novo historico
  escolar: 2 subfases restantes estimadas.
- a persistencia propria minima das pendencias foi aplicada de forma aditiva no
  `school-management-service`: foi criada a tabela
  `historico_escolar_pendencia`, a entidade JPA correspondente e o snapshot das
  pendencias abertas passou a ser sincronizado nos fluxos atuais de criacao,
  atualizacao e geracao por boletim. A leitura de carregamento reutiliza as
  pendencias persistidas quando existirem e mantém fallback calculado para
  historicos legados, sem rota nova, sem BFF e sem frontend.
- rollback dessa subfase permanece controlado: como o contrato externo nao foi
  alterado, basta desativar o uso da tabela e voltar ao calculo em memoria,
  mantendo a migration aditiva como dado de auditoria. Contagem da macrofase
  novo historico escolar: 1 subfase restante estimada.
- o fechamento formal da macrofase confirma que o bloco backend do novo
  historico escolar fica encerrado neste recorte: existem carregamento de
  cadastro/edicao, contexto minimo persistido, fronteira interna de pendencias,
  snapshot persistido das pendencias abertas, fallback calculado para dados
  legados e testes automatizados cobrindo o fluxo principal. Nao houve
  substituicao de tela, BFF, frontend, importacao de PDF ou ampliacao para
  estruturas completas de cabecalho/periodos/estudos realizados.
- a macrofase novo historico escolar passa a contagem 0 dentro do backend-only.
  A proxima decisao deve escolher entre iniciar um novo bloco funcional
  backend-only ou planejar, em macrofase separada, o recorte de BFF/frontend
  para integrar a nova experiencia visual.

### Fase 58 - Documentos e storage controlado

Objetivo: iniciar o proximo bloco backend-only do futuro
`enrollment-document-service` pelo menor ponto de risco em documentos, preparando
a troca futura do filesystem local para storage de objetos sem alterar rotas
externas, BFF, frontend ou persistencia transacional.

Entregue na primeira subfase da Fase 58:

- o storage local de documentos deixou de ter a raiz fixa diretamente na
  implementacao e passou a usar configuracao propria em
  `documento.storage.local.root`, preservando o default atual
  `uploads/documentos`;
- `LocalDocumentoArquivoStorage` passou a normalizar a raiz configurada e os
  caminhos por entidade antes da gravacao, mantendo a porta
  `DocumentoArquivoStorage` como contrato interno unico para upload;
- o recorte nao muda o contrato HTTP de `/api/documentos` ou
  `/api/documentos-alunos`, nao altera BFF/frontend e nao introduz storage
  externo ou runtime novo;
- rollback permanece simples: remover a propriedade customizada ou voltar ao
  default local preserva o comportamento anterior enquanto os metadados seguem
  no PostgreSQL.

Contagem da macrofase documentos/storage: 2 subfases restantes estimadas:
diagnostico do contrato minimo para object storage e fechamento formal do bloco
antes de qualquer cutover.

Entregue na segunda subfase da Fase 58:

- a porta `DocumentoArquivoStorage` deixou de devolver uma `String` crua e
  passou a devolver `DocumentoArquivoReferencia`, com tipo de storage, chave
  interna e localizacao de persistencia;
- o storage local agora gera uma chave relativa estavel por entidade e mantem
  `caminhoPersistencia()` compativel com o valor historicamente salvo em
  `documento.caminho_arquivo`;
- o contrato minimo para um futuro adapter de object storage fica separado da
  persistencia transacional: metadados de documento continuam no PostgreSQL e o
  adapter de arquivo passa a ser responsavel por traduzir chave/localizacao;
- nao houve criacao de bucket, dependencia de cloud, migration, BFF, frontend
  ou alteracao de rota externa.

Contagem da macrofase documentos/storage: 1 subfase restante estimada:
fechamento formal do bloco e decisao sobre quando iniciar object storage real
em macrofase propria.

Fechamento formal da Fase 58:

- o bloco backend-only de documentos/storage fica encerrado neste recorte: a
  raiz local de arquivos e configuravel, o storage local normaliza diretorios,
  a porta `DocumentoArquivoStorage` devolve uma referencia estruturada e o
  fluxo atual continua persistindo `documento.caminho_arquivo` de forma
  compativel;
- o recorte nao introduziu storage externo, bucket, dependencia de cloud,
  migration, BFF, frontend, rota nova ou alteracao dos contratos HTTP atuais;
- o rollback permanece controlado pelo default local `uploads/documentos` e
  pela compatibilidade de `DocumentoArquivoReferencia.caminhoPersistencia()`
  com o valor legado;
- a evolucao para object storage real deve ser tratada como macrofase propria,
  com adapter novo, configuracao operacional, estrategia de migracao dos
  arquivos existentes, validacao de leitura/gravacao e plano de rollback.

Contagem da macrofase documentos/storage: 0. A proxima decisao deve escolher
entre iniciar uma macrofase backend-only de object storage real ou abrir outro
bloco funcional sem ampliar BFF/frontend.

### Fase 59 - Object storage real controlado

Objetivo: iniciar a migracao tecnica controlada do armazenamento de binarios de
documentos para storage S3-compatible, mantendo `LOCAL` como backend padrao,
sem alterar rotas externas, sem BFF/frontend e sem migrar arquivos existentes
antes de validacao operacional.

Entregue na primeira subfase da Fase 59:

- foi adicionada a dependencia `software.amazon.awssdk:s3`, reaproveitando o
  BOM da AWS SDK ja declarado no `school-management-service`;
- `documento.storage.backend` passou a selecionar o backend de arquivos:
  `local` por default e `s3` somente quando explicitamente configurado;
- o storage local ficou condicionado a `documento.storage.backend=local` com
  `matchIfMissing=true`, preservando o comportamento atual e o rollback
  imediato;
- foi criado o adapter `S3DocumentoArquivoStorage`, condicionado a
  `documento.storage.backend=s3`, com configuracao de bucket, prefixo, regiao,
  endpoint S3-compatible, credenciais estaticas opcionais e path-style access;
- o adapter S3 grava por `PutObject`, gera chave por entidade e retorna
  `DocumentoArquivoReferencia` com `OBJECT_STORAGE` e localizacao persistivel
  `s3://bucket/chave`;
- nao houve criacao de bucket, runtime MinIO/S3, migration, BFF, frontend ou
  mudanca de contrato HTTP.

Contagem da macrofase object storage real controlado: 2 subfases restantes
estimadas: validacao operacional controlada com storage S3-compatible e
fechamento/migracao minima dos arquivos existentes somente se a validacao for
segura.

Entregue na segunda subfase da Fase 59:

- foi criada uma validacao operacional backend-only para o modo
  `documento.storage.backend=s3`, subindo o contexto Spring real do
  `school-management-service` com o adapter S3 ativo;
- o teste executa o fluxo HTTP multipart de `/api/documentos-alunos`, cria o
  aluno, passa pelo controller, service, use case e `S3DocumentoArquivoStorage`
  e confirma que o metadado persistido permanece como `s3://bucket/chave`;
- o `S3Client` foi substituido por mock no limite externo para evitar iniciar
  MinIO/S3, criar bucket, depender de credenciais reais ou alterar runtime local
  nesta subfase;
- a validacao confirma que o rollback por `documento.storage.backend=local`
  continua preservado e que o modo S3 permanece opt-in, sem BFF/frontend,
  migration ou mudanca de contrato HTTP.

Contagem da macrofase object storage real controlado: 1 subfase restante
estimada: decidir entre validacao com runtime S3-compatible real em escopo
opt-in ou fechamento formal sem migrar arquivos existentes.

Fechamento formal da Fase 59:

- a validacao com runtime S3-compatible real nao foi iniciada neste bloco para
  evitar acoplar a fase a Docker/MinIO, bucket, credenciais, rede ou ambiente
  externo antes de haver decisao operacional explicita;
- o recorte fica encerrado com o backend preparado para alternar entre
  `local` e `s3` por configuracao, com `local` como default e rollback
  imediato por `documento.storage.backend=local`;
- o modo `s3` permanece opt-in, coberto por teste unitario do adapter e por
  teste operacional com contexto Spring real e boundary externo mockado;
- nenhuma rota externa, BFF, frontend, migration, contrato HTTP ou migracao de
  arquivos existentes foi alterada;
- a proxima evolucao de storage deve ser uma macrofase propria, somente quando
  houver decisao de ambiente S3-compatible real, estrategia de migracao de
  arquivos legados, reconciliacao e rollback operacional.

Contagem da macrofase object storage real controlado: 0. O bloco fica fechado
sem cutover de arquivos existentes e sem dependencia operacional externa.

### Fase 60 - Diagnostico de abertura do `people-service`

Objetivo: iniciar a proxima macrofase backend-only apos o fechamento de
documentos/storage, avaliando o menor recorte seguro para preparar a extracao
fisica futura do `people-service` sem alterar BFF, frontend, rotas externas ou
persistencia fora do monolito nesta primeira subfase.

Entregue na primeira subfase da Fase 60:

- o modulo fisico `people-service` ainda nao existe no monorepo, enquanto
  `academic-catalog-service` e `academic-professor-service` ja possuem runtime
  proprio;
- o monolito ja possui fronteiras internas iniciais para pessoas:
  `PessoaCadastroPort` encapsula cadastro base de pessoa/endereco/tipos para
  aluno e responsavel, e `FuncionarioProfessorPort` encapsula elegibilidade de
  funcionario para professor;
- o primeiro risco identificado e que `PessoaCadastroPort` ainda expoe
  `PessoaEntity` em operacoes internas, mantendo acoplamento JPA direto entre
  people e consumidores como aluno, responsavel, professor e funcionario;
- abrir o runtime fisico começando por writes de aluno, responsavel,
  funcionario ou professor seria arriscado agora, porque esses fluxos ainda
  dependem de transacoes locais, relacionamentos JPA e consistencia imediata
  com matricula, diario, historico, documentos, identidade e tenant;
- o menor recorte seguro para a proxima subfase e separar um contrato interno
  entity-free de consulta cadastral e catalogos de pessoa, reaproveitando o
  que hoje aparece em `/api/consulta-cadastral` e `/api/pessoas/catalogos`,
  sem mudar as rotas externas e sem mover escrita;
- a futura abertura fisica do `people-service` deve iniciar em modo
  backend/backend read-only ou shadow, consumindo esse contrato entity-free e
  mantendo o monolito como autoridade de escrita ate haver reconciliacao e
  rollback operacional.

Dependencias e impactos mapeados:

- dados centrais: `pessoa`, `pessoa_tipo_pessoa`, `tipo_pessoa`,
  `pessoa_endereco`, `endereco`, `tipo_endereco`, alem dos agregados que
  referenciam pessoa (`aluno`, `responsavel`, `professor`, `funcionario`);
- consistencia: CPF, escola ativa, vinculo pessoa-tipo, endereco principal e
  referencias cruzadas com matricula, documentos, diario, historico,
  planejamento/IA e seguranca;
- rollback minimo: manter `school-management-service` como runtime e banco
  autoritativos, introduzindo apenas contrato interno sem entidade JPA na borda;
- migracao: nenhuma migration nesta subfase; qualquer banco proprio do
  `people-service` deve ser precedido por inventario de dados, backfill
  reproduzivel e reconciliacao por escola/CPF.

Contagem da macrofase `people-service`: 3 subfases restantes estimadas:
criar contrato entity-free de consulta/catalogos, aplicar o primeiro consumo
interno de baixo risco e, depois, decidir se o modulo fisico pode abrir em modo
shadow read-only.

Entregue na segunda subfase da Fase 60:

- foi criado o contrato interno `PessoaConsultaPort`, separado de
  `PessoaCadastroPort`, para concentrar leitura cadastral e catalogos de pessoa
  sem expor `PessoaEntity` na borda;
- foram adicionados DTOs internos entity-free para catalogos, pagina de
  consulta cadastral, aluno com responsaveis e resumo de responsavel;
- a implementacao `PessoaConsultaService` passou a concentrar a consulta
  cadastral hoje usada por `/api/consulta-cadastral` e os catalogos usados por
  `/api/pessoas/catalogos`, mantendo o mesmo runtime e banco do monolito;
- `ConsultarCadastroAlunoResponsavelUseCase` passou a depender da nova porta
  de pessoa e apenas mapear o resultado para os DTOs externos atuais de
  responsavel;
- `PessoaCatalogoController` passou a consumir `PessoaConsultaPort`, e
  `PessoaCadastroPort` ficou restrita ao contrato de cadastro/escrita;
- o gateway antigo de consulta cadastral em `responsavel` foi removido para
  evitar duas fontes de leitura paralelas;
- nao houve nova rota, BFF, frontend, migration, runtime fisico de
  `people-service`, mudanca de payload externo ou movimentacao de escrita.

Contagem da macrofase `people-service`: 2 subfases restantes estimadas:
aplicar o primeiro consumo interno adicional de baixo risco sobre
`PessoaConsultaPort` e, depois, decidir se o modulo fisico pode abrir em modo
shadow read-only.

Entregue na terceira subfase da Fase 60:

- `PessoaConsultaPort` passou a expor tambem a consulta entity-free de pessoa
  por `pessoaId` e `escolaId`, retornando `PessoaResumo` sem vazar
  `PessoaEntity` para consumidores;
- `PessoaConsultaService` implementou essa leitura sobre o banco atual do
  monolito, mantendo `school-management-service` como runtime e fonte
  autoritativa;
- `FuncionarioProfessorService`, usado pelo contrato interno de funcionarios
  elegiveis para professor, passou a montar os dados de pessoa via
  `PessoaConsultaPort` em vez de ler nome/escola diretamente de
  `FuncionarioEntity.getPessoa()`;
- a criacao de professor continuou usando `PessoaCadastroPort` porque o
  relacionamento JPA atual ainda exige `PessoaEntity` enquanto nao houver nova
  persistencia propria;
- nao houve nova rota, BFF, frontend, migration, runtime fisico de
  `people-service`, alteracao de payload externo ou movimentacao de escrita.

Contagem da macrofase `people-service`: 1 subfase restante estimada: decidir se
o modulo fisico pode abrir em modo shadow/read-only ou se ainda e necessario
mais um recorte interno antes da extracao fisica.

Fechamento formal da Fase 60:

- a abertura fisica do `people-service` fica autorizada apenas em modo
  backend/backend shadow/read-only, consumindo os contratos entity-free ja
  estabilizados em `PessoaConsultaPort`;
- o primeiro recorte fisico deve ser restrito a leitura de catalogos de pessoa,
  consulta cadastral e resumo de pessoa por `pessoaId` + `escolaId`, sem mover
  escrita e sem assumir autoridade sobre `pessoa`, `endereco`, aluno,
  responsavel, funcionario ou professor;
- ainda nao e seguro abrir cutover de escrita nem persistencia propria
  autoritativa, porque `PessoaCadastroPort`, aluno, responsavel, professor,
  funcionario, endereco e vinculos JPA continuam dependentes do schema e das
  transacoes locais do `school-management-service`;
- o modulo fisico futuro deve iniciar sem BFF/frontend, sem rota externa nova,
  sem migration de dados e sem banco proprio obrigatorio; se houver schema
  local, ele deve ser opt-in e precedido por backfill/reconciliacao;
- rollback da proxima macrofase deve ser trivial: desabilitar o runtime shadow
  e manter o monolito como unica fonte de leitura/escrita.

Contagem da macrofase `people-service`: 0. A proxima macrofase sugerida e abrir
o modulo fisico `people-service` em shadow/read-only backend/backend, iniciando
por scaffold minimo, health e proxy/cliente interno para os contratos de
consulta ja estabilizados, sem BFF, frontend, escrita ou persistencia propria
autoritativa.

### Fase 61 - Abertura fisica do `people-service` shadow/read-only

Objetivo: iniciar o modulo fisico `people-service` pelo menor recorte seguro de
leitura backend/backend, mantendo o monolito como fonte autoritativa e
reaproveitando os contratos entity-free ja estabilizados em `PessoaConsultaPort`.
Esta fase nao abre rota externa no BFF, nao altera frontend, nao move escrita e
nao cria persistencia propria autoritativa.

Entregue na primeira subfase da Fase 61:

- o monorepo passou a incluir o modulo Maven `people-service`, com runtime
  Spring Boot proprio, actuator/health e configuracao isolada em
  `people.shadow.*`;
- o monolito passou a expor o adaptador interno read-only
  `GET /internal/pessoas/**`, delegando para `PessoaConsultaPort` e retornando
  DTOs internos sem `PessoaEntity`;
- o `people-service` passou a expor rotas internas compativeis em
  `/internal/v1/pessoas/**` e `/internal/pessoas/**` para catalogos de pessoa,
  catalogos de endereco, resumo de pessoa por `pessoaId` + `escolaId` e
  consulta cadastral;
- todas as leituras do novo runtime ainda sao proxy shadow para o monolito via
  cliente HTTP interno, com repasse de `Authorization`, `X-Correlation-Id`,
  `X-Usuario-Id` e `X-Escola-Id`;
- foi adicionada observabilidade minima com health indicator
  `peopleShadowMonolith` e metricas `people.shadow.monolith.requests` /
  `people.shadow.monolith.failures` por operacao;
- nao houve BFF/frontend, migration, schema proprio, Flyway/JPA no novo modulo,
  rota externa nova, cutover de escrita ou autoridade local de dados.

Contagem da macrofase `people-service` fisico: 2 subfases restantes estimadas:
realizar smoke operacional/read-only do runtime e, depois, fechar formalmente a
macrofase decidindo se a proxima evolucao pode iniciar diagnostico de
persistencia propria controlada ou se ainda exige mais hardening do proxy.

Proxima subfase pratica:

- consolidar smoke operacional do `people-service` fisico em modo shadow,
  validando health/metricas e compatibilidade das rotas internas sem tocar BFF;
- manter escrita, persistencia propria e cutover externo fora do escopo ate o
  fechamento formal desta macrofase.

Entregue na segunda subfase da Fase 61:

- o smoke operacional do `people-service` fisico foi automatizado em modo
  read-only, sem depender de monolito real em execucao e sem iniciar runtime
  externo fora dos testes;
- o health indicator `peopleShadowMonolith` passou a ter cobertura dedicada
  para diagnostico por rota, totais de requests, totais de falhas e estado
  `OUT_OF_SERVICE` quando a `base-url` configurada e invalida;
- foi adicionado um teste operacional com porta aleatoria do runtime
  `people-service`, `MockWebServer` como monolito simulado e chamadas reais para
  as rotas internas de catalogo, busca por id e consulta cadastral;
- o smoke comprova os tres sinais minimos esperados para operacao shadow:
  sucesso, `not_found` e erro downstream, refletidos no endpoint
  `/actuator/health/peopleShadowMonolith`;
- o recorte permanece sem BFF/frontend, sem write, sem migration, sem banco
  proprio, sem cutover externo e sem autoridade local de dados.

Contagem da macrofase `people-service` fisico: 1 subfase restante estimada:
fechar formalmente a macrofase, confirmando se o proxy read-only esta maduro
para iniciar diagnostico de persistencia propria controlada ou se ainda exige
hardening adicional antes de qualquer persistencia.

Proxima subfase pratica:

- fechar formalmente a Fase 61, revisando as rotas internas, os sinais
  operacionais e os limites que ainda impedem escrita ou persistencia propria;
- manter a decisao como backend/backend, sem abrir BFF/frontend e sem iniciar
  schema autoritativo nesta macrofase.

Fechamento formal da Fase 61:

- o runtime fisico `people-service` fica oficialmente aberto apenas como proxy
  backend/backend shadow/read-only para os contratos entity-free ja
  estabilizados em `PessoaConsultaPort`;
- o recorte operacional fechado cobre catalogos de pessoa, catalogos de
  endereco, consulta cadastral e resumo de pessoa por `pessoaId` + `escolaId`,
  com rotas internas compativeis e health/metricas dedicados;
- o smoke automatizado confirma que o runtime responde, repassa headers ao
  monolito, registra sucesso, `not_found` e erro downstream, e expoe esses
  sinais em `/actuator/health/peopleShadowMonolith`;
- a escrita continua bloqueada para o `people-service`, porque cadastro base,
  endereco, aluno, responsavel, funcionario, professor e vinculos JPA ainda
  dependem das transacoes locais e do schema autoritativo do
  `school-management-service`;
- ainda nao ha BFF/frontend, rota externa nova, migration, banco proprio,
  schema autoritativo, cutover de leitura externa ou cutover de escrita;
- rollback permanece trivial: remover/desligar o runtime shadow e manter o
  monolito como unica fonte de leitura e escrita.

Contagem da macrofase `people-service` fisico: 0. A proxima macrofase sugerida
e diagnosticar a primeira persistencia propria controlada do `people-service`,
ainda sem cutover externo e sem mover escrita, separando quais dados poderiam
compor um read model local, qual migration/backfill minimo seria necessario e
qual rollback impediria perda de consistencia.

Proxima fase pratica:

- iniciar a Fase 62 como diagnostico de persistencia propria controlada do
  `people-service`, sem criar schema autoritativo nem aplicar migration antes
  de mapear dados, dependencias, consistencia e rollback;
- manter a frente restrita a backend/backend, sem BFF/frontend, sem write e sem
  cutover de rotas externas.

### Fase 62 - Diagnostico de persistencia propria controlada do `people-service`

Objetivo: decidir o menor recorte seguro para iniciar persistencia propria
controlada no `people-service`, sem aplicar migration, sem criar schema
autoritativo, sem mover escrita, sem BFF/frontend e sem cutover externo. A fase
parte do runtime fisico read-only fechado na Fase 61 e do contrato entity-free
`PessoaConsultaPort`.

Entregue na primeira subfase da Fase 62:

- o primeiro recorte seguro foi definido como read model local opcional,
  alimentado por backfill controlado a partir do monolito, e nao como escrita
  autoritativa de `pessoa`;
- as tabelas candidatas ao read model inicial sao `pessoa`, `tipo_pessoa`,
  `pessoa_tipo_pessoa`, `endereco`, `tipo_endereco` e `pessoa_endereco`, porque
  elas sustentam os contratos de catalogos, consulta cadastral e resumo de
  pessoa ja expostos pelo proxy read-only;
- `aluno`, `responsavel`, `funcionario`, `professor`, `aluno_responsavel`,
  `pessoa_documento`, historico, matricula, diario, avaliacao, IA e documentos
  ficam fora do primeiro schema local, porque ainda dependem de transacoes e
  joins do `school-management-service`;
- `PessoaCadastroPort` continua bloqueando qualquer cutover de escrita, pois
  ainda expoe `PessoaEntity` e e consumida por aluno, responsavel e professor
  em fluxos transacionais locais;
- a migration minima futura deve ser opt-in e criar apenas estrutura local
  espelhada/read-only para os dados centrais de pessoa/endereco/tipos, mantendo
  IDs originais e metadados necessarios para reconciliacao;
- o backfill minimo deve ser idempotente por `id_pessoa`, `id_endereco`,
  `id_pessoa_tipo_pessoa` e `id_pessoa_endereco`, com filtro por escola e
  relatorio de divergencias por escola/CPF/tipo/endereco principal;
- o rollback permanece desligar o uso local e voltar todo o runtime para proxy
  direto ao monolito; nenhuma escrita do `people-service` pode ser habilitada
  antes de reconciliacao verde e contrato de rollback testado.

Impactos e dependencias mapeados:

- consistencia principal: unicidade `id_escola + cpf`, pessoa ativa, escola da
  pessoa, tipos vinculados e endereco principal;
- dependencia de tenant: `pessoa.id_escola` ja existe no monolito, mas o
  `people-service` ainda nao deve assumir autoridade sobre tenant;
- dependencia de catalogos: `tipo_pessoa` e `tipo_endereco` devem ser copiados
  primeiro para preservar integridade do read model;
- dependencias transacionais fora do recorte: criacao/edicao de aluno,
  responsavel, professor e funcionario continuam no monolito;
- observabilidade exigida para a proxima etapa: health separado para
  persistencia local, contadores de backfill, totais reconciliados e divergencias
  por tabela.

Contagem da macrofase persistencia controlada do `people-service`: 3 subfases
restantes estimadas: preparar fundacao opt-in de persistencia local sem uso em
runtime, implementar backfill/reconciliacao sem cutover e, depois, decidir se
alguma leitura interna pode usar o read model local com fallback para o monolito.

Entregue na segunda subfase da Fase 62:

- o `people-service` recebeu a fundacao opt-in de persistencia local read-only
  apenas como contrato interno, configuracao e observabilidade, sem adicionar
  JPA, Flyway, datasource, migration, schema fisico ou adapter local;
- as flags `people.shadow.local-persistence.enabled`,
  `migration-enabled`, `read-model-cutover-enabled` e `fail-on-error` ficam
  desligadas por padrao, preservando o proxy read-only para o monolito como
  unico caminho em runtime;
- o health dedicado `peopleLocalPersistence` documenta as tabelas candidatas do
  read model, as rotas shadow que poderiam ser atendidas futuramente, as tabelas
  excluidas do primeiro recorte, a estrategia de rollback e os contadores
  esperados de backfill, divergencia e falha;
- qualquer ativacao prematura da persistencia local ou do cutover de leitura
  reporta `OUT_OF_SERVICE`, evitando uso acidental antes de schema, backfill,
  reconciliacao e fallback estarem implementados;
- nao houve alteracao de rota interna, contrato externo, BFF/frontend, escrita
  autoritativa ou fonte de verdade do monolito.

Contagem da macrofase persistencia controlada do `people-service`: 2 subfases
restantes estimadas: implementar backfill/reconciliacao sem cutover e, depois,
decidir se alguma leitura interna pode usar o read model local com fallback para
o monolito.

Entregue na terceira subfase da Fase 62:

- o `people-service` recebeu o primeiro esqueleto controlado de
  backfill/reconciliacao para o read model de pessoas, ainda sem schema local,
  sem adapter de banco e sem execucao autoritativa;
- foram adicionadas as flags
  `people.shadow.local-persistence.backfill-enabled`,
  `reconciliation-enabled` e `backfill-batch-size`, todas inertes por padrao,
  para permitir um ciclo explicito de planejamento operacional;
- o coordenador de ciclo gera relatorio apenas em modo `planned_only`, com as
  seis tabelas candidatas, chaves idempotentes, origem `monolith_proxy`,
  destino candidato `people_read_model_candidate`, escrita desligada e cutover
  desligado;
- um runner de startup executa o ciclo somente quando backfill ou reconciliacao
  forem explicitamente habilitados por configuracao; com as flags padrao, nada
  e executado em runtime;
- o health `peopleLocalPersistence` passou a expor o plano de backfill,
  tamanho de lote, flags de reconciliacao e contadores de ciclos/tabelas
  planejadas, mantendo o monolito como unica fonte efetiva.

Contagem da macrofase persistencia controlada do `people-service`: 1 subfase
restante estimada: decidir se alguma leitura interna pode usar o read model
local com fallback para o monolito, sem abrir escrita.

Entregue na quarta subfase da Fase 62:

- o `people-service` recebeu uma guarda interna de cutover controlado de
  leitura, chamada antes das consultas shadow atuais apenas para decidir e
  medir a origem selecionada, sem trocar o `PessoaReadPort` efetivo;
- a flag `people.shadow.local-persistence.read-model-fallback-enabled` foi
  adicionada ligada por padrao, tornando o fallback para `monolith_proxy`
  obrigatorio para qualquer tentativa futura de leitura local;
- cada operacao candidata (`listarTiposPessoa`, `listarTiposEndereco`,
  `buscarPorId` e `consultarCadastro`) agora possui decisao observavel com
  origem candidata, origem selecionada, elegibilidade, motivo de bloqueio e
  garantia de escrita desligada;
- mesmo com `read-model-cutover-enabled=true`, a leitura local permanece
  inelegivel enquanto nao houver persistencia local habilitada, reconciliacao
  verde, ausencia de falhas/divergencias e adapter local implementado;
- o health `peopleLocalPersistence` passou a expor o plano de roteamento de
  leitura e retorna `OUT_OF_SERVICE` quando o cutover e solicitado sem cumprir
  as pre-condicoes, preservando o monolito como origem selecionada.

Contagem da macrofase persistencia controlada do `people-service`: 0 subfases
restantes. A macrofase fica fechada sem mover escrita, sem BFF/frontend, sem
schema local fisico e sem cutover real de leitura.

Proxima subfase pratica:

- iniciar a proxima macrofase backend com o menor passo fisico seguro para o
  `people-service`: diagnosticar se ja cabe criar schema local read-only real
  para `tipo_pessoa`/`tipo_endereco` ou se ainda e melhor hardening de contrato
  antes de qualquer migration;
- manter proibido mover escrita, alterar BFF/frontend ou habilitar leitura local
  sem adapter, reconciliacao verde e rollback testado.

### Fase 63 - Schema local read-only controlado do `people-service`

Objetivo: iniciar a proxima macrofase backend do `people-service` apos o
fechamento da Fase 62, decidindo o menor schema fisico seguro para sair do
diagnostico/health e preparar persistencia local real sem mover escrita, sem
BFF/frontend e sem cutover de leitura.

Entregue na primeira subfase da Fase 63:

- o menor recorte fisico seguro foi definido como catalogo local read-only de
  `tipo_pessoa` e `tipo_endereco`, porque sao tabelas globais simples,
  identificadas por UUID, com `codigo` unico e sem dependencia de tenant ou
  transacao de cadastro;
- `pessoa`, `pessoa_tipo_pessoa`, `endereco`, `pessoa_endereco`,
  `pessoa_documento`, `aluno`, `responsavel`, `funcionario`, `professor` e
  demais tabelas transacionais continuam fora da primeira migration fisica,
  porque ainda dependem de backfill por escola, joins e contratos de escrita do
  monolito;
- o `people-service` recebeu um plano interno de schema de catalogo no health
  `peopleLocalPersistence`, expondo tabelas candidatas, colunas, chaves,
  origem de seed, bloqueios, rollback e recomendacao da proxima subfase;
- a decisao desta subfase foi nao adicionar ainda datasource, Flyway, JPA,
  migration fisica, adapter local ou cutover, preservando o runtime apenas em
  proxy shadow para o monolito;
- a proxima migration deve ser opt-in/read-only, criar somente
  `tipo_pessoa`/`tipo_endereco`, manter IDs originais do monolito, usar
  `codigo` como chave natural de reconciliacao e continuar com fallback
  obrigatorio para `monolith_proxy`.

Contagem da macrofase schema local read-only do `people-service`: 3 subfases
restantes estimadas: adicionar dependencias/configuracao opt-in e migration
fisica dos catalogos, implementar backfill/reconciliacao desses catalogos sem
cutover e, depois, avaliar adapter local de leitura com fallback obrigatorio.

Entregue na segunda subfase da Fase 63:

- o `people-service` recebeu a preparacao fisica opt-in do schema local
  read-only para `tipo_pessoa` e `tipo_endereco`, com Flyway e driver
  PostgreSQL adicionados sem datasource automatico e sem JPA;
- a migration `V1__create_people_catalog_read_model.sql` cria somente os dois
  catalogos, preservando IDs originais, `codigo` como chave natural unica e sem
  qualquer tabela transacional;
- foi adicionado um runner manual de schema migration, condicionado por
  `people.shadow.local-persistence.migration-enabled`; com a flag desligada,
  nenhuma conexao local e aberta e nenhuma migration e executada;
- a configuracao opt-in exige URL explicita em
  `people.shadow.local-persistence.schema-migration.*`, reportando bloqueio se
  a migration for habilitada sem destino configurado e respeitando
  `fail-on-error`;
- o health `peopleLocalPersistence` passou a expor o estado da migration fisica
  e contadores de sucesso/falha, mantendo `PessoaReadPort` no
  `monolith_proxy` e `read-model-cutover` proibido.

Contagem da macrofase schema local read-only do `people-service`: 2 subfases
restantes estimadas: implementar backfill/reconciliacao dos catalogos sem
cutover e, depois, avaliar adapter local de leitura com fallback obrigatorio.

Entregue na terceira subfase da Fase 63:

- o `people-service` recebeu backfill/reconciliacao opt-in real dos catalogos
  `tipo_pessoa` e `tipo_endereco`, copiando dados do monolito por JDBC
  configurado explicitamente para o schema local ja preparado;
- a execucao continua inerte por padrao e so roda quando
  `people.shadow.local-persistence.backfill-enabled` ou
  `people.shadow.local-persistence.reconciliation-enabled` forem habilitadas,
  exigindo origem em
  `people.shadow.local-persistence.catalog-backfill.source-*` e destino em
  `people.shadow.local-persistence.schema-migration.*`;
- o adapter de infraestrutura faz upsert idempotente por UUID, preserva
  `codigo` como chave natural de reconciliacao e valida divergencias por
  codigo, UUID e descricao, sem incluir tabelas transacionais;
- o health `peopleLocalPersistence` passou a expor o ultimo relatorio
  `catalogBackfill`, totais de origem/destino, registros copiados,
  divergencias e status agregado;
- o `PessoaReadPort` permanece em `monolith_proxy`; nao houve BFF/frontend,
  escrita externa, adapter local de leitura nem habilitacao de
  `read-model-cutover`.

Contagem da macrofase schema local read-only do `people-service`: 1 subfase
restante estimada: avaliar adapter local de leitura dos catalogos com fallback
obrigatorio para o monolito, somente depois de backfill/reconciliacao verde.

Entregue na quarta subfase da Fase 63:

- o `people-service` recebeu adapter local de leitura apenas para os catalogos
  `tipo_pessoa` e `tipo_endereco`, usando o schema local configurado em
  `people.shadow.local-persistence.schema-migration.*`, sem datasource
  automatico e sem JPA;
- a guarda de leitura passou a permitir fonte local somente para
  `listarTiposPessoa` e `listarTiposEndereco`, e apenas quando
  `read-model-cutover-enabled=true`, `read-model-fallback-enabled=true`,
  persistencia local, backfill e reconciliacao estiverem habilitados, o ultimo
  `catalogBackfill` estiver `completed` e nao houver divergencias/falhas;
- `buscarPorId` e `consultarCadastro` continuam inelegiveis para leitura local
  e permanecem em `monolith_proxy`;
- qualquer falha do adapter local dos catalogos registra fallback e retorna ao
  monolito, mantendo o fallback obrigatorio como contrato operacional;
- o health `peopleLocalPersistence` passou a expor as decisoes de roteamento
  para catalogos, leituras locais/fallbacks e o plano final da Fase 63;
- nao houve escrita, BFF/frontend, tabela transacional, mudanca de payload
  externo ou cutover amplo.

Contagem da macrofase schema local read-only do `people-service`: 0. A Fase 63
fica fechada com schema local dos catalogos, backfill/reconciliacao opt-in e
adapter local de leitura controlado apenas para `tipo_pessoa` e
`tipo_endereco`, sempre com fallback obrigatorio para o monolito.

Proxima macrofase sugerida:

- iniciar diagnostico da proxima fatia minima do `people-service`, avaliando se
  cabe avancar para dados cadastrais transacionais (`pessoa`,
  `pessoa_tipo_pessoa`, `endereco`, `pessoa_endereco`) ou se ainda e melhor
  endurecer operacao/observabilidade dos catalogos locais antes de ampliar o
  escopo.

### Fase 64 - Diagnostico da proxima fatia transacional do `people-service`

Objetivo: decidir o menor recorte transacional seguro apos o fechamento dos
catalogos locais da Fase 63, sem criar migration fisica, sem backfill real, sem
alterar BFF/frontend, sem mover escrita e sem habilitar cutover de leitura para
rotas transacionais.

Entregue na primeira subfase da Fase 64:

- o `people-service` passou a expor no health `peopleLocalPersistence` o plano
  `transactionalReadModelExpansionPlan`, separando candidato minimo,
  dependencias, bloqueios, rollback e proximo passo;
- a decisao foi nao migrar de uma vez `pessoa`, `pessoa_tipo_pessoa`,
  `endereco` e `pessoa_endereco`, porque isso misturaria identidade, papeis,
  endereco, consulta cadastral, PII e derivacao de escopo escolar no mesmo
  passo;
- o menor recorte candidato ficou definido como `pessoa_identity_read_model`,
  composto por `pessoa` e `pessoa_tipo_pessoa`, limitado inicialmente a
  preparar a rota `buscarPorId`;
- `endereco` e `pessoa_endereco` ficam fora da primeira fatia transacional e
  devem aguardar contrato proprio da consulta cadastral e reconciliacao da
  fatia de identidade;
- nenhuma migration, backfill, adapter local transacional, escrita, BFF/frontend
  ou cutover foi criado nesta subfase;
- antes da proxima subfase pratica, o contrato deve definir politica de PII,
  escopo escolar sem o `people-service` assumir tenant, ordem de backfill
  idempotente e reconciliacao por ID/CPF/papel.

Contagem da macrofase Fase 64: 3 subfases restantes estimadas: preparar schema
fisico opt-in de `pessoa`/`pessoa_tipo_pessoa`, implementar backfill/
reconciliacao da fatia de identidade e, depois, avaliar leitura local de
`buscarPorId` com fallback obrigatorio para o monolito.

Proxima subfase pratica:

- preparar a migration opt-in somente de `pessoa` e `pessoa_tipo_pessoa`,
  preservando IDs originais e sem incluir `endereco`, `pessoa_endereco`,
  `aluno`, `responsavel`, `funcionario`, `professor` ou documentos;
- manter `buscarPorId` e `consultarCadastro` no monolito ate haver backfill,
  reconciliacao verde e rollback testado.

Entregue na segunda subfase da Fase 64:

- o `people-service` recebeu a migration
  `V2__create_people_identity_read_model.sql`, executada apenas pelo runner
  opt-in ja existente quando
  `people.shadow.local-persistence.migration-enabled` e habilitado com destino
  explicito em `people.shadow.local-persistence.schema-migration.*`;
- a migration cria somente `pessoa` e `pessoa_tipo_pessoa`, preservando IDs
  originais do monolito, `id_escola` como identificador copiado de escopo,
  PII necessaria ao read model de identidade e vinculo com o catalogo local
  `tipo_pessoa`;
- `endereco`, `pessoa_endereco`, `aluno`, `responsavel`, `funcionario`,
  `professor`, documentos e qualquer tabela de escrita continuam fora do schema
  desta fatia;
- o plano `transactionalReadModelExpansionPlan` passou a reportar
  `pessoa_identity_schema_prepared_opt_in`, liberando apenas migration opt-in e
  mantendo backfill, leitura local transacional, escrita, BFF/frontend e cutover
  bloqueados;
- o rollback operacional segue por flag: desabilitar
  `people.shadow.local-persistence.migration-enabled`, manter
  `read-model-cutover-enabled` desligado e deixar o schema de identidade sem uso
  ate backfill/reconciliacao ficarem verdes.

Contagem da macrofase Fase 64: 2 subfases restantes estimadas: implementar
backfill/reconciliacao da fatia de identidade e, depois, avaliar leitura local
de `buscarPorId` com fallback obrigatorio para o monolito.

Proxima subfase pratica:

- implementar backfill/reconciliacao opt-in somente para `pessoa` e
  `pessoa_tipo_pessoa`, respeitando a ordem `pessoa` antes de
  `pessoa_tipo_pessoa`, comparando por ID/CPF/papel e mantendo cutover
  transacional desligado.

Entregue na terceira subfase da Fase 64:

- o ciclo opt-in de backfill/reconciliacao do `people-service` deixou de ser
  limitado aos catalogos e passou a sincronizar, na mesma execucao controlada,
  `tipo_pessoa`, `tipo_endereco`, `pessoa` e `pessoa_tipo_pessoa`;
- a ordem operacional fica preservada: catalogos primeiro, depois `pessoa` e
  por fim `pessoa_tipo_pessoa`, evitando quebra de chave estrangeira no read
  model local;
- `pessoa` e `pessoa_tipo_pessoa` sao copiados por JDBC a partir do monolito
  para o schema local preparado na subfase anterior, com upsert idempotente por
  ID original e reconciliacao por ID/CPF/campos de identidade/papel;
- o health passou a expor tambem `localReadModelBackfill`, mantendo
  `catalogBackfill` por compatibilidade, e o plano
  `transactionalReadModelExpansionPlan` passou a reportar
  `pessoa_identity_backfill_reconciliation_prepared_opt_in`;
- nenhuma rota externa, BFF/frontend, escrita, adapter local transacional ou
  cutover de `buscarPorId` foi habilitado nesta subfase; o monolito continua
  sendo a fonte de leitura transacional.

Contagem da macrofase Fase 64: 1 subfase restante estimada: avaliar leitura
local de `buscarPorId` com fallback obrigatorio para o monolito, somente se o
read model de identidade estiver verde e sem ampliar para `consultarCadastro`.

Proxima subfase pratica:

- implementar a avaliacao controlada de leitura local para `buscarPorId`, com
  adapter local restrito a `pessoa`/`pessoa_tipo_pessoa`, fallback obrigatorio
  para o monolito e sem alterar rota externa no BFF.

Entregue na quarta subfase da Fase 64:

- o `people-service` recebeu a primeira leitura local controlada da fatia de
  identidade, restrita a `buscarPorId`, atras de `PeopleLocalReadCutoverGuard`
  e dependente do read model local verde;
- foi criada a porta `PeopleIdentityLocalReadPort` e o adapter JDBC local
  `JdbcPeopleIdentityLocalReadAdapter`, consultando `pessoa` por `id_pessoa` e
  `id_escola` no schema local, sem acessar `endereco`, `pessoa_endereco`,
  `aluno`, `responsavel`, `funcionario` ou `professor`;
- o read model de `pessoa` passou a copiar `escola_nome` como campo
  desnormalizado de leitura para preservar o contrato de
  `PessoaResumoResponse` sem tornar o `people-service` autoridade de escola;
- `PessoaQueryService.buscarPessoaPorId` tenta a leitura local somente quando o
  gate libera `buscarPorId`; em ausencia local ou erro, o fallback obrigatorio
  para o monolito permanece ativo e observado por metrica propria;
- `consultarCadastro`, escritas, rotas externas, BFF/frontend e cutover amplo
  continuam fora do escopo desta macrofase.

Contagem da macrofase Fase 64: 0. A Fase 64 fica fechada com o primeiro
cutover controlado de leitura local de identidade preparado e protegido por
fallback obrigatorio para o monolito.

Proxima fase pratica:

- iniciar a proxima macrofase do `people-service` por diagnostico, decidindo se
  o proximo recorte seguro deve ser endurecimento operacional da leitura local
  ja criada ou expansao controlada para `consultarCadastro` com
  `endereco`/`pessoa_endereco`;
- manter o recorte backend/backend, sem alterar BFF/frontend, sem escrita local
  e sem remover o monolito como fallback.

### Fase 65 - Diagnostico do read model de endereco do `people-service`

Objetivo: iniciar a macrofase seguinte do `people-service` apos o fechamento da
leitura local de identidade, decidindo se `consultarCadastro` pode evoluir para
read model local controlado sem assumir escrita, sem alterar BFF/frontend e sem
remover o monolito como fallback.

Entregue na primeira subfase da Fase 65:

- o diagnostico interno passou a classificar o proximo recorte minimo como
  `pessoa_address_read_model`, formado por `endereco` e `pessoa_endereco`, para
  preparar futuramente `consultarCadastro`;
- `pessoa` e `pessoa_tipo_pessoa` ficaram marcadas como fatia ja preparada para
  `buscarPorId`, atras de fallback obrigatorio e reconciliacao verde;
- `endereco` e `pessoa_endereco` foram marcadas como candidatas da proxima
  fatia, mas ainda sem autorizacao para migration, backfill ou leitura local;
- a proxima subfase precisa mapear o contrato exato de resposta de
  `consultarCadastro` antes de criar schema, porque a consulta envolve vinculo
  pessoa-endereco, tipo de endereco e campos pessoais sensiveis;
- rollback permanece simples: manter `consultarCadastro` no proxy do monolito,
  manter `read-model-cutover` desligado e desabilitar a fundacao local se houver
  risco operacional.

Contagem da macrofase Fase 65: 3 subfases restantes estimadas: mapear contrato
de `consultarCadastro`, preparar schema opt-in de endereco e depois avaliar
backfill/reconciliacao antes de qualquer leitura local.

Proxima subfase pratica:

- mapear o contrato de `consultarCadastro` contra o payload atual do monolito,
  separando campos de `pessoa`, `pessoa_tipo_pessoa`, `endereco`,
  `pessoa_endereco` e `tipo_endereco`;
- manter o recorte em diagnostico backend/backend, sem migration nova, sem
  adapter local de `consultarCadastro`, sem BFF/frontend e sem escrita.

Entregue na segunda subfase da Fase 65:

- o contrato real de `consultarCadastro` foi mapeado diretamente no monolito e
  no espelho do `people-service`;
- a rota atual `GET /internal/pessoas/consulta-cadastral` recebe filtros
  `nomeAluno`, `cpfAluno`, `nomeResponsavel`, `cpfResponsavel`, `page` e
  `size`, normaliza strings em branco para `null`, limita `size` a 100 e ordena
  a pagina por nome do aluno;
- o payload atual retorna somente `content`, `totalElements`, `page` e `size`;
  cada item de `content` contem aluno com `idAluno`, `nomeCompleto`, `cpf`,
  `email`, `telefone`, `dataNascimento`, `createdAt` e lista de responsaveis
  com `id`, `nomeCompleto`, `cpf`, `email`, `telefone`, `createdAt`;
- a consulta real usa `aluno`, `responsavel` e `aluno_responsavel`; apesar do
  nome da macrofase ter iniciado pela hipotese de endereco, `endereco`,
  `pessoa_endereco` e `tipo_endereco` nao fazem parte do payload atual de
  `consultarCadastro` e ficam explicitamente fora do proximo schema;
- o planner interno passou a classificar o proximo recorte minimo como
  `pessoa_student_responsible_read_model`, ainda sem autorizar migration,
  backfill, adapter local, BFF/frontend, escrita ou cutover.

Contagem da macrofase Fase 65: 2 subfases restantes estimadas: preparar schema
opt-in para `aluno`, `responsavel` e `aluno_responsavel`, e depois avaliar
backfill/reconciliacao antes de qualquer leitura local.

Proxima subfase pratica:

- preparar o schema opt-in do read model de `consultarCadastro` com
  `aluno`, `responsavel` e `aluno_responsavel`, preservando IDs do monolito e
  mantendo `consultarCadastro` no proxy do monolito;
- nao incluir `endereco`, `pessoa_endereco` ou `tipo_endereco` enquanto o
  contrato externo dessa consulta nao expuser esses campos.

Entregue na terceira subfase da Fase 65:

- foi criada a migration opt-in `V3__create_people_student_responsible_read_model.sql`
  no `people-service`, preparando as tabelas locais `aluno`, `responsavel` e
  `aluno_responsavel` para o read model minimo de `consultarCadastro`;
- o schema preserva os IDs do monolito (`id_aluno`, `id_responsavel` e
  `id_aluno_responsavel`), os campos hoje retornados pela consulta e a
  referencia opcional `id_pessoa` para compatibilidade com a evolucao atual do
  monolito;
- `endereco`, `pessoa_endereco` e `tipo_endereco` continuam fora desse recorte,
  porque nao aparecem no payload real de `consultarCadastro`;
- o health e o planner do `people-service` passaram a reportar a fatia
  `pessoa_student_responsible_read_model` com migration permitida somente por
  opt-in, mantendo backfill, adapter local, BFF/frontend, escrita e cutover
  bloqueados;
- rollback permanece por desligamento de
  `people.shadow.local-persistence.migration-enabled`, preservando
  `consultarCadastro` no proxy do monolito.

Contagem da macrofase Fase 65: 1 subfase restante estimada: implementar
backfill/reconciliacao opt-in de `aluno`, `responsavel` e
`aluno_responsavel`, ainda sem leitura local ou cutover.

Proxima subfase pratica:

- implementar backfill/reconciliacao opt-in para `aluno`, `responsavel` e
  `aluno_responsavel`, comparando o read model local com o monolito e mantendo
  `consultarCadastro` no proxy ate divergencia zero;
- nao criar adapter local, nao alterar BFF/frontend, nao habilitar escrita local
  e nao incluir endereco nesta macrofase sem mudanca explicita de contrato.

Entregue na quarta subfase da Fase 65:

- o ciclo controlado de backfill/reconciliacao do `people-service` foi expandido
  para incluir `aluno`, `responsavel` e `aluno_responsavel`, alem das tabelas ja
  existentes de catalogo e identidade;
- o adapter JDBC passou a copiar e reconciliar a fatia
  `people_read_model_student_responsible` a partir do monolito, preservando
  `id_aluno`, `id_responsavel` e `id_aluno_responsavel` e comparando os campos
  efetivamente usados pelo contrato atual de `consultarCadastro`;
- `consultarCadastro` continua no proxy do monolito porque ainda nao existe
  adapter local para essa leitura e o guard de cutover segue retornando
  `local-read-adapter-not-configured` para essa rota;
- a operacao continua opt-in pelas flags atuais de backfill/reconciliacao, sem
  escrita local, sem BFF/frontend, sem rota externa nova, sem endereco e sem
  cutover;
- rollback permanece por desligamento de
  `people.shadow.local-persistence.backfill-enabled`,
  `people.shadow.local-persistence.reconciliation-enabled` e
  `people.shadow.local-persistence.enabled`.

Contagem da macrofase Fase 65: 0 subfases restantes. O bloco de schema,
backfill e reconciliacao opt-in do read model minimo de `consultarCadastro`
fica fechado sem leitura local e sem cutover.

Proxima fase pratica:

- iniciar a proxima macrofase backend do `people-service` com diagnostico do
  menor recorte remanescente: ou adapter local controlado de `consultarCadastro`
  ainda sem BFF/cutover externo, ou outra fronteira de pessoas com menor risco,
  decidindo pelo codigo atual e mantendo o monolito como fallback obrigatorio.

### Fase 66 - Adapter local controlado de `consultarCadastro` no `people-service`

Objetivo: avaliar e preparar, de forma incremental, se `consultarCadastro` pode
ganhar adapter local read-only no `people-service` usando o read model de
`aluno`, `responsavel` e `aluno_responsavel` ja migrado/reconciliado, ainda sem
BFF/frontend, sem escrita local, sem rotas externas novas e sem cutover externo.

Entregue na primeira subfase da Fase 66:

- o diagnostico do menor recorte remanescente confirmou que o proximo passo
  seguro e preparar um adapter local read-only de `consultarCadastro`, porque o
  schema, backfill e reconciliacao opt-in da fatia `aluno`/`responsavel`/
  `aluno_responsavel` ja ficaram fechados na Fase 65;
- `PessoaQueryService` ainda registra a decisao de roteamento de
  `consultarCadastro` e delega a leitura para o `PessoaReadPort` do monolito;
- o guard de cutover permanece bloqueando essa rota com
  `local-read-adapter-not-configured`, mesmo com read model verde, preservando
  fallback obrigatorio e impedindo leitura local prematura;
- o planner transacional do `people-service` passou a reportar no health a
  fatia `pessoa_student_responsible_local_read_adapter`, com migration e
  backfill ja permitidos por opt-in, mas `localReadCutoverAllowedNow=false`;
- `endereco`, `pessoa_endereco`, BFF/frontend, escrita local e alteracao de
  contrato externo continuam fora desta macrofase.

Contagem da macrofase Fase 66: 2 subfases restantes estimadas: implementar o
adapter local read-only de `consultarCadastro` ainda sem cutover, e depois
avaliar elegibilidade controlada da rota com fallback obrigatorio.

Entregue na segunda subfase da Fase 66:

- foi criado o contrato interno `PeopleStudentResponsibleLocalReadPort` para
  isolar a leitura local de `consultarCadastro` sem expor nova rota externa;
- foi implementado o adapter JDBC read-only sobre `aluno`, `responsavel` e
  `aluno_responsavel`, reproduzindo filtros por aluno/responsavel, paginacao,
  limite de pagina, ordenacao por nome do aluno e agregacao de responsaveis por
  aluno;
- `PessoaQueryService` permaneceu delegando `consultarCadastro` ao monolito, e
  o adapter local ficou preparado, mas sem roteamento nem cutover nesta subfase;
- o planner transacional passou a reportar
  `consultar_cadastro_local_adapter_prepared_without_routing`, mantendo
  `localReadCutoverAllowedNow=false` e indicando que a proxima decisao deve
  avaliar roteamento controlado com fallback obrigatorio;
- nao houve BFF/frontend, escrita local, endereco, mudanca de contrato externo
  ou cutover externo.

Contagem da macrofase Fase 66: 1 subfase restante estimada: avaliar a
elegibilidade controlada de roteamento de `consultarCadastro` com fallback
obrigatorio, mantendo rollback imediato para o monolito.

Proxima subfase pratica:

- avaliar e, se seguro, conectar `consultarCadastro` ao adapter local somente
  atras do guard de cutover, mantendo fallback obrigatorio para o monolito e
  bloqueio operacional quando a reconciliacao/read model nao estiver verde;
- manter o escopo backend/backend, sem BFF/frontend, sem escrita local e sem
  ampliar o read model para `endereco` ou outros vinculos fora do payload atual.

Entregue na terceira subfase da Fase 66:

- `consultarCadastro` foi conectado ao adapter local do `people-service` apenas
  atras do guard de cutover, com fallback obrigatorio para o monolito;
- a leitura local so fica elegivel quando o read model, o backfill e a
  reconciliacao estao verdes; quando o guard bloqueia ou a leitura local falha,
  a rota permanece no proxy do monolito;
- o health `peopleLocalPersistence` passou a expor
  `guardedReadCutoverClosure`, consolidando fonte selecionada, fallback,
  criterios verdes, metricas e rollback da rota;
- nao houve BFF/frontend, escrita local, rota externa nova ou ampliacao para
  `endereco`;
- a macrofase Fase 66 fica fechada com 0 subfases restantes.

### Fase 67 - Diagnostico profundo de `endereco` no `people-service`

Objetivo: mapear o recorte real de `endereco` antes de qualquer schema, backfill
ou cutover local, separando endereco persistido, vinculo pessoa-endereco,
endereco principal, limpeza de orfaos e consulta externa ViaCEP.

Entregue na primeira subfase da Fase 67:

- o planner transacional do `people-service` passou a reportar
  `address_read_model_deep_diagnostic_closed_no_schema`, com
  `migrationAllowedNow=false`, `backfillAllowedNow=false` e
  `localReadCutoverAllowedNow=false`;
- o health `peopleLocalPersistence` aprofundou `nextBlockedSliceDiagnostic`,
  deixando explicitos consumidores reais de escrita:
  `PessoaFoundationService.criarPessoaComTipoEEndereco`,
  `PessoaFoundationService.atualizarPessoaEEndereco`, criacao/atualizacao de
  aluno e criacao/atualizacao de responsavel;
- foram mapeados os consumidores de limpeza e consistencia:
  `PessoaEnderecoJpaRepository.deleteByPessoaId`,
  `PessoaEnderecoJpaRepository.countByEnderecoId`, `AlunoPersistenceGateway` e
  `ResponsavelPersistenceGateway`;
- a consulta ViaCEP foi separada do read model persistido: `EnderecoCepController`
  e `ViaCepService` continuam sendo adapter externo, nao fonte local de dados;
- a decisao tecnica foi bloquear schema/backfill/cutover de `endereco` ate
  existir contrato interno entity-free, regra explicita de endereco principal,
  estrategia de limpeza de endereco orfao e chave de reconciliacao por
  `pessoa_endereco`;
- nao houve BFF/frontend, escrita local, migration, backfill ou cutover.

Contagem da macrofase Fase 67: 1 subfase restante estimada: criar o contrato
interno entity-free de endereco no monolito/people boundary, ainda sem schema
local nem rota externa.

Entregue na segunda subfase da Fase 67:

- foi introduzido o contrato interno `PessoaEnderecoPort`, sem expor entidades
  JPA, cobrindo busca do endereco principal por pessoa e remocao dos vinculos de
  endereco com limpeza de endereco orfao;
- foi criado o DTO interno `PessoaEnderecoResumo`, usado como fronteira
  entity-free para `id_pessoa_endereco`, `id_endereco`, campos de endereco,
  tipo de endereco e flag `principal`;
- `PessoaFoundationService` passou a implementar tambem essa porta, mantendo a
  autoridade de escrita e limpeza no monolito atual;
- `AlunoPersistenceGateway` e `ResponsavelPersistenceGateway` deixaram de
  consultar `PessoaEnderecoJpaRepository`, `EnderecoJpaRepository`,
  `PessoaEnderecoEntity` e `EnderecoEntity` diretamente para leitura do endereco
  principal e limpeza de vinculos/orfaos;
- o health `peopleLocalPersistence` passou a expor `preparedInternalContract`
  com `PessoaEnderecoPort`, `PessoaEnderecoResumo` e `jpaEntityExposure=false`;
- ViaCEP continua separado como adapter externo e nao foi transformado em fonte
  do read model;
- nao houve migration local, backfill, BFF/frontend, rota externa nova, escrita
  local ou cutover.

Contagem da macrofase Fase 67: 0 subfases restantes. A Fase 67 fica fechada com
contrato interno de endereco preparado e schema local ainda bloqueado.

Proxima fase pratica:

- iniciar diagnostico de schema/backfill local de `endereco` e
  `pessoa_endereco` no `people-service`, agora partindo do contrato interno ja
  estabilizado;
- definir colunas minimas, chave de reconciliacao por `pessoa_endereco`, regra
  de endereco principal e estrategia de rollback;
- manter `endereco` no monolito como autoridade, sem BFF/frontend, sem escrita
  local e sem cutover.

### Fase 68 - Diagnostico de schema/backfill local de `endereco` no `people-service`

Objetivo: fechar o menor desenho seguro para uma futura persistencia local
read-only de `endereco` e `pessoa_endereco`, sem criar migration, sem executar
backfill e sem habilitar leitura local.

Entregue na primeira subfase da Fase 68:

- o planner transacional do `people-service` passou a reportar
  `address_schema_backfill_diagnostic_closed_no_migration`, com
  `migrationAllowedNow=false`, `backfillAllowedNow=false` e
  `localReadCutoverAllowedNow=false`;
- a proxima fatia pratica ficou limitada a
  `address_schema_migration_opt_in_no_backfill`, ou seja, preparar migration
  opt-in de schema em fase posterior, ainda sem carga de dados e sem cutover;
- o health `peopleLocalPersistence` passou a expor
  `addressSchemaBackfillDiagnostic`, separando explicitamente tabelas candidatas
  (`endereco`, `pessoa_endereco`), tabelas de referencia (`pessoa`,
  `tipo_endereco`), colunas minimas, chave de reconciliacao por
  `pessoa_endereco.id_pessoa_endereco` e checks secundarios;
- a regra de endereco principal ficou formalizada: somente `principal=true`
  pode ser exposto pelo contrato interno atual, e multiplos principais para a
  mesma pessoa bloqueiam reconciliacao verde;
- ViaCEP continua classificado como adapter externo de consulta, sem virar
  autoridade do read model persistido;
- o rollback minimo ficou definido por desligar migration, backfill e cutover
  local, mantendo `endereco` no proxy do monolito e mantendo
  `consultarCadastro` independente de endereco local;
- nao houve migration local, backfill, BFF/frontend, rota externa nova, escrita
  local ou cutover.

Contagem da macrofase Fase 68: 2 subfases restantes estimadas: preparar schema
opt-in sem backfill; depois avaliar backfill/reconciliacao de endereco sem
cutover externo.

Proxima fase pratica:

- preparar a migration opt-in de schema local para `endereco` e
  `pessoa_endereco` no `people-service`, preservando IDs do monolito e sem
  backfill automatico;
- manter as flags de migration/backfill/cutover desligadas por padrao e manter
  `endereco` no monolito como autoridade;
- nao alterar BFF/frontend, rotas externas ou escrita local.

Entregue na segunda subfase da Fase 68:

- foi criada a migration opt-in `V4__create_people_address_read_model.sql` no
  `people-service`, adicionando as tabelas read-only candidatas `endereco` e
  `pessoa_endereco` ao schema local de people;
- a migration preserva os IDs originais do monolito (`id_endereco` e
  `id_pessoa_endereco`) e mantém as dependencias para `pessoa` e
  `tipo_endereco`, sem criar escrita local nem alterar rotas externas;
- o runner de schema passou a reportar `endereco` e `pessoa_endereco` no
  conjunto de tabelas migraveis quando
  `people.shadow.local-persistence.migration-enabled=true`, mas a flag continua
  desligada por padrao;
- o planner transacional passou a reportar
  `address_schema_migration_opt_in_prepared_no_backfill`, liberando apenas a
  migration opt-in e mantendo `backfillAllowedNow=false` e
  `localReadCutoverAllowedNow=false`;
- o health `peopleLocalPersistence` passou a indicar
  `schema_migration_opt_in_prepared_backfill_still_blocked`, com referencia
  explicita a `V4__create_people_address_read_model.sql`,
  `enabledByDefault=false` e `automaticBackfill=false`;
- nao houve backfill, reconciliacao, adapter de leitura local de endereco,
  BFF/frontend, rota externa nova, escrita local ou cutover.

Contagem da macrofase Fase 68: 1 subfase restante estimada: diagnosticar e
preparar o backfill/reconciliacao de `endereco` e `pessoa_endereco`, ainda sem
cutover externo.

Proxima fase pratica:

- diagnosticar e preparar o menor backfill/reconciliacao opt-in para
  `endereco` e `pessoa_endereco`, usando `pessoa_endereco.id_pessoa_endereco`
  como chave principal de reconciliacao;
- bloquear qualquer leitura local quando houver multiplos enderecos principais
  por pessoa ou divergencia em campos normalizados de endereco;
- manter `endereco` no monolito como autoridade, sem BFF/frontend, sem escrita
  local e sem cutover.

Entregue na terceira subfase da Fase 68:

- o backfill/reconciliacao opt-in do read model local do `people-service` foi
  estendido para `endereco` e `pessoa_endereco`, mantendo os IDs originais do
  monolito como chaves idempotentes (`id_endereco` e
  `id_pessoa_endereco`);
- a sincronizacao continua desligada por padrao e so executa quando
  `people.shadow.local-persistence.backfill-enabled` e/ou
  `people.shadow.local-persistence.reconciliation-enabled` forem habilitadas;
- a reconciliacao de `endereco` compara campos normalizados de endereco e a de
  `pessoa_endereco` usa `id_pessoa_endereco`, `id_pessoa`, `id_endereco`,
  `id_tipo_endereco` e a flag `principal`;
- a regra de consistencia bloqueia `pessoa_endereco` quando a origem possuir
  mais de um endereco principal para a mesma pessoa, reportando
  `address-principal-rule-violated`;
- o planner passou a reportar
  `address_backfill_reconciliation_prepared_no_read_cutover`, com migration e
  backfill permitidos apenas em modo controlado e
  `localReadCutoverAllowedNow=false`;
- o health `peopleLocalPersistence` passou a indicar
  `backfill_reconciliation_prepared_read_cutover_still_blocked`, incluindo
  diagnostico de `backfillReconciliation`, alvo `people_read_model_address`,
  blockers de green reconciliation e rollback por desligamento das flags;
- nao houve leitura local de endereco, adapter de consulta local para rotas de
  negocio, BFF/frontend, rota externa nova, escrita local ou cutover.

Contagem da macrofase Fase 68: 0 subfases restantes. O bloco de diagnostico,
schema opt-in e backfill/reconciliacao opt-in de endereco esta fechado sem
cutover.

Proxima fase pratica:

- iniciar uma macrofase separada para diagnosticar o contrato de leitura local
  de endereco, sem assumir cutover automatico e sem misturar com escrita local;
- qualquer leitura local futura de endereco deve continuar atras de guard,
  reconciliacao verde e fallback obrigatorio para o monolito;
- manter sem BFF/frontend, sem escrita local e sem alteracao de rotas externas
  ate decisao explicita da proxima macrofase.

Fechamento formal da Fase 68:

- a Fase 68 fica oficialmente encerrada com tres entregas controladas:
  diagnostico de schema/backfill, migration opt-in e backfill/reconciliacao
  opt-in de `endereco`/`pessoa_endereco`;
- o recorte termina sem leitura local de endereco, sem adapter de rota de
  negocio, sem BFF/frontend, sem escrita local e sem cutover;
- a decisao de evoluir para leitura local de endereco foi separada para nova
  macrofase, exigindo guard, reconciliacao verde, fallback obrigatorio e
  rollback por flags antes de qualquer uso operacional.

### Fase 69 - Diagnostico do contrato de leitura local de endereco no `people-service`

Objetivo: iniciar uma nova macrofase backend-only para definir o contrato de
leitura local de endereco no `people-service`, partindo do schema e
backfill/reconciliacao opt-in ja preparados na Fase 68, ainda sem adapter
operacional, sem cutover, sem BFF/frontend, sem escrita local e sem alteracao de
rotas externas.

Entregue na primeira subfase da Fase 69:

- o planner transacional passou a reportar
  `address_local_read_contract_diagnostic_started_no_cutover`, com proxima
  etapa `define_address_local_read_contract_before_adapter` e fatia minima
  `address_local_read_contract_diagnostic_no_route_change`;
- `endereco` e `pessoa_endereco` continuam com migration/backfill permitidos
  apenas em modo opt-in, mas `localReadAllowed=false` ate existir contrato de
  leitura local explicito e validado;
- o health `peopleLocalPersistence` passou a expor
  `addressLocalReadContractDiagnostic`, separando origem candidata
  `people_read_model_address`, fallback obrigatorio para `monolith_proxy`,
  operacoes candidatas internas (`buscarEnderecoPrincipalPorPessoa` e
  `listarEnderecosPorPessoa`), payload interno minimo e pre-condicoes do guard;
- o payload interno minimo ficou restrito a identificadores de
  `pessoa_endereco`, `pessoa`, `endereco`, tipo de endereco, flag `principal` e
  campos persistidos de endereco, sem incluir ViaCEP como autoridade do read
  model;
- ficaram explicitamente fora do recorte: nova rota interna REST, alteracao de
  payload de `consultarCadastro`, BFF/frontend, escrita local, cutover de
  endereco e qualquer leitura local sem fallback;
- o rollback da macrofase fica limitado a manter endereco no proxy do monolito,
  desligar cutover de read model e bloquear qualquer adapter quando houver
  violacao da regra de endereco principal ou divergencia de reconciliacao.

Contagem da macrofase Fase 69: 2 subfases restantes estimadas: definir porta e
DTO internos de leitura local de endereco sem rota; depois avaliar adapter local
controlado ainda atras de guard e fallback.

Proxima fase pratica:

- criar a porta e os DTOs internos para leitura local de endereco no
  `people-service`, sem rota REST nova e sem conectar `consultarCadastro`;
- manter o contrato limitado a `people_read_model_address`, com fallback
  obrigatorio para o monolito e bloqueio quando a reconciliacao de endereco nao
  estiver verde;
- nao alterar BFF/frontend, escrita local, rotas externas ou payloads atuais.

Entregue na segunda subfase da Fase 69:

- foi criado o contrato interno `PeopleAddressLocalReadPort` no
  `people-service`, ainda sem implementacao JDBC, sem injecao em use case e sem
  rota REST;
- foi criado o DTO interno `PessoaEnderecoLocalReadResponse`, limitado ao
  payload definido na primeira subfase: identificadores de `pessoa_endereco`,
  pessoa, endereco, tipo de endereco, flag `principal` e campos persistidos de
  endereco;
- o planner passou a reportar
  `address_local_read_port_contract_prepared_no_adapter`, mantendo
  `localReadCutoverAllowedNow=false` e indicando como proxima etapa
  `evaluate_address_local_read_adapter_behind_guard`;
- o health `peopleLocalPersistence` passou a expor os artefatos preparados em
  `addressLocalReadContractDiagnostic.preparedArtifacts`, com
  `routeCreated=false`, `adapterCreated=false` e `queryServiceConnected=false`;
- nao houve adapter operacional, rota interna nova, alteracao de
  `consultarCadastro`, BFF/frontend, escrita local ou cutover.

Contagem da macrofase Fase 69: 1 subfase restante estimada: avaliar o adapter
local de leitura de endereco atras de guard e fallback, ainda sem rota externa e
sem conectar payloads existentes.

Proxima fase pratica:

- avaliar e, se seguro, criar o primeiro adapter local JDBC de leitura de
  endereco implementando `PeopleAddressLocalReadPort`, ainda sem rota REST nova
  e sem conectar `consultarCadastro`;
- o adapter deve ler apenas `people_read_model_address`, respeitar endereco
  principal, bloquear quando houver divergencia/violacao de reconciliacao e
  manter fallback obrigatorio para o monolito;
- nao alterar BFF/frontend, escrita local, rotas externas ou payloads atuais.

Entregue na terceira subfase da Fase 69:

- foi criado o adapter `JdbcPeopleAddressLocalReadAdapter`, implementando
  `PeopleAddressLocalReadPort` sobre o read model local de endereco, ainda sem
  injecao em use case, sem rota REST e sem conectar `consultarCadastro`;
- o adapter le apenas `pessoa`, `pessoa_endereco`, `endereco` e
  `tipo_endereco`, aplica escopo por escola via `pessoa.id_escola` e mantem o
  payload limitado ao DTO interno `PessoaEnderecoLocalReadResponse`;
- `buscarEnderecoPrincipalPorPessoa` retorna vazio quando nao ha endereco
  principal no escopo da escola e bloqueia a leitura local com
  `address-principal-rule-violated` quando houver mais de um principal para a
  mesma pessoa;
- `listarEnderecosPorPessoa` usa ordenacao deterministica com principal
  primeiro, sem expor nova rota externa e sem transformar ViaCEP em autoridade
  do read model;
- o planner passou a reportar
  `address_local_read_adapter_prepared_no_route_no_cutover`, mantendo
  `localReadCutoverAllowedNow=false` e indicando fechamento formal da Fase 69
  antes de qualquer decisao de cutover de leitura de endereco;
- o health `peopleLocalPersistence` passou a indicar
  `addressLocalReadContractDiagnostic.status=adapter_prepared_no_route_no_cutover`,
  com `adapterCreated=true`, `queryServiceConnected=false`,
  `localReadAdapterConnected=false`, fallback obrigatorio para o monolito e
  bloqueio de BFF/frontend, escrita local, rota externa e cutover.

Contagem da macrofase Fase 69: 0 subfases restantes. O contrato interno e o
adapter local JDBC de leitura de endereco estao preparados sem uso operacional,
sem rota REST, sem BFF/frontend, sem escrita local e sem cutover.

Proxima fase pratica:

- fechar formalmente a Fase 69 apos commit/push da terceira subfase;
- decidir em fase separada se o proximo recorte sera diagnosticar cutover
  guardado de leitura local de endereco ou iniciar outra familia backend;
- qualquer cutover futuro de endereco deve exigir guard, reconciliacao verde,
  fallback obrigatorio para o monolito, rollback por flags e nenhuma mudanca de
  BFF/frontend sem decisao explicita.

Fechamento formal da Fase 69:

- a Fase 69 fica oficialmente encerrada com tres entregas controladas:
  diagnostico do contrato de leitura local de endereco, criacao de porta/DTO
  internos e adapter JDBC local preparado sobre `people_read_model_address`;
- o recorte termina sem rota REST nova, sem alteracao de `consultarCadastro`,
  sem BFF/frontend, sem escrita local, sem leitura operacional de endereco e
  sem cutover;
- o adapter `JdbcPeopleAddressLocalReadAdapter` permanece apenas como artefato
  preparado, com `queryServiceConnected=false`, fallback obrigatorio para o
  monolito e bloqueio quando houver violacao de endereco principal ou
  reconciliacao nao verde;
- a decisao de ativar leitura local de endereco fica separada para uma nova
  fase, exigindo diagnostico explicito de guard, reconciliacao verde,
  observabilidade, fallback e rollback por flags antes de qualquer uso
  operacional.

Proxima fase pratica:

- iniciar uma fase separada de decisao sobre o proximo recorte backend:
  diagnosticar cutover guardado de leitura local de endereco ou escolher outra
  familia de API;
- se o recorte escolhido for endereco, manter a primeira subfase apenas como
  diagnostico de elegibilidade do cutover, sem BFF/frontend, sem escrita local,
  sem rota externa nova e sem conectar payloads existentes;
- preservar o monolito como fallback obrigatorio ate que guard, reconciliacao,
  metricas e rollback estejam comprovados.

### Fase 70 - Diagnostico de elegibilidade do cutover de leitura local de endereco no `people-service`

Objetivo: iniciar uma fase backend-only para diagnosticar se a leitura local de
endereco pode evoluir para cutover guardado em fase futura, partindo do adapter
JDBC preparado na Fase 69, ainda sem conectar o adapter ao `PessoaQueryService`,
sem rota REST nova, sem BFF/frontend, sem escrita local e sem cutover.

Entregue na primeira subfase da Fase 70:

- o planner transacional passou a reportar
  `address_local_read_cutover_eligibility_diagnostic_started`, com proxima
  etapa `define_address_read_cutover_guard_criteria_no_route_change` e fatia
  minima `address_read_cutover_eligibility_diagnostic_no_connection`;
- `endereco` e `pessoa_endereco` continuam com migration/backfill permitidos
  apenas em modo controlado, mas `localReadAllowed=false` e
  `localReadCutoverAllowedNow=false`;
- o health `peopleLocalPersistence` passou a expor
  `addressLocalReadCutoverEligibilityDiagnostic`, marcando
  `adapterPrepared=true`, `queryServiceConnected=false`, `routeCreated=false`,
  `fallbackRequired=true`, `bffFrontendChangeAllowedNow=false` e
  `writeCutoverAllowedNow=false`;
- foram explicitados os criterios minimos de guard para qualquer ativacao
  futura: flags de persistencia local ligadas, fallback habilitado, backfill
  concluido, divergencias/falhas zeradas, nenhuma violacao de endereco
  principal, nenhuma divergencia de campos normalizados e nenhuma referencia
  ausente de pessoa/endereco;
- ficaram como blockers antes de qualquer conexao operacional: ausencia de
  operacao de roteamento especifica para endereco, ausencia de metricas
  especificas de leitura local de endereco, ausencia de contrato de rota
  selecionado, `consultarCadastro` sem campos de endereco e autoridade de
  escrita ainda no monolito;
- ficaram explicitamente fora do recorte: conectar
  `JdbcPeopleAddressLocalReadAdapter` ao `PessoaQueryService`, criar rota REST
  de endereco, alterar payload de `consultarCadastro`, BFF/frontend, escrita
  local e cutover.

Contagem da macrofase Fase 70: 2 subfases restantes estimadas: definir a
operacao interna/metricas de roteamento de leitura de endereco; depois avaliar
uma conexao controlada do adapter apenas atras de guard, se os criterios
estiverem verdes.

Proxima fase pratica:

- definir a operacao interna de roteamento de leitura local de endereco e as
  metricas necessarias para observabilidade, ainda sem conectar o adapter ao
  fluxo operacional;
- manter o cutover bloqueado ate existir guard especifico de endereco,
  reconciliacao verde, fallback obrigatorio e rollback por flags;
- nao alterar BFF/frontend, escrita local, rotas externas ou payloads atuais.

Entregue na segunda subfase da Fase 70:

- foi definida a operacao interna de roteamento `addressLocalRead` no
  `PeopleLocalReadCutoverGuard`, apontando para
  `internal-operation:PeopleAddressLocalReadPort` e candidato
  `people_read_model_address`;
- a operacao de endereco ficou isolada do conjunto global `avaliarTodas()`,
  para nao alterar o comportamento ja estabilizado de catalogo, identidade e
  `consultarCadastro`;
- `addressLocalRead` registra decisao de roteamento geral em
  `people.shadow.local.persistence.read.routing.decisions{operation=addressLocalRead}`
  e decisao especifica em
  `people.shadow.local.persistence.address.read.routing.decisions`;
- mesmo com read model verde, a operacao de endereco continua selecionando
  `monolith_proxy` e bloqueando leitura local com
  `address-local-read-connection-disabled` ate uma fase futura decidir a
  conexao do adapter;
- o health `peopleLocalPersistence` passou a expor a decisao de
  `addressLocalRead`, os nomes das metricas de roteamento e o contador
  `addressReadRoutingDecisionsTotal`, mantendo `queryServiceConnected=false`,
  `routeCreated=false`, `localReadCutoverAllowedNow=false` e
  `fallbackRequired=true`;
- nao houve conexao do `JdbcPeopleAddressLocalReadAdapter` ao
  `PessoaQueryService`, rota REST nova, BFF/frontend, escrita local, alteracao
  de payload ou cutover.

Contagem da macrofase Fase 70: 1 subfase restante estimada: avaliar uma conexao
controlada do adapter apenas atras de guard, sem rota externa e mantendo
fallback obrigatorio para o monolito.

Proxima fase pratica:

- avaliar se e seguro conectar o adapter de endereco ao fluxo interno apenas
  atras do guard `addressLocalRead`, ainda sem rota REST nova e sem alterar
  `consultarCadastro`;
- manter fallback obrigatorio para o monolito e bloquear a conexao quando
  backfill/reconciliacao nao estiverem verdes ou houver violacao da regra de
  endereco principal;
- nao alterar BFF/frontend, escrita local, rotas externas ou payloads atuais.

Entregue na terceira subfase da Fase 70:

- `addressLocalRead` passou a ficar elegivel para `people_read_model_address`
  quando o guard ja estiver verde: cutover de leitura habilitado, fallback
  ligado, persistencia local/backfill/reconciliacao ligados, relatorio local
  concluido, divergencias zeradas e falhas zeradas;
- foi criado o servico interno `PeopleAddressLocalReadService`, conectando
  `PeopleAddressLocalReadPort` ao guard de cutover de endereco sem expor rota
  REST, sem conectar ao `PessoaQueryService`, sem alterar `consultarCadastro`
  e sem mudar payload externo;
- o servico interno so consulta o adapter local quando `addressLocalRead` esta
  elegivel; quando o guard bloqueia, quando o endereco nao e encontrado ou
  quando o adapter falha, o resultado fica vazio para manter o fallback
  operacional fora desse recorte;
- a observabilidade passou a registrar leituras internas de endereco em
  `people.shadow.local.persistence.address.reads` e o health passou a expor
  `localAddressReadsTotal`, `internalGuardedServiceConnected=true`,
  `adapter_connected_to_internal_guard_no_route`, `queryServiceConnected=false`
  e `routeCreated=false`;
- o planner transacional passou a marcar o recorte como
  `address_local_read_internal_guard_connection_prepared_no_route`, com proxima
  etapa `close_phase_70_and_plan_address_write_authority_diagnostic`;
- nao houve rota REST nova, BFF/frontend, escrita local, conexao de endereco ao
  `consultarCadastro`, alteracao de payload externo ou cutover de escrita.

Contagem da macrofase Fase 70: 0 subfases restantes. O recorte de leitura local
interna de endereco fica fechado em modo guardado, sem exposicao externa.

Proxima fase pratica:

- fechar formalmente a Fase 70 e iniciar o diagnostico de autoridade de escrita
  de endereco no `people-service`, separando criacao/atualizacao de endereco,
  limpeza de vinculos/orfaos, uso de ViaCEP e rollback;
- manter o monolito como autoridade de escrita ate existir contrato, migracao,
  reconciliacao e plano de rollback especificos para escrita;
- nao abrir PR para `Master` ainda, porque o criterio combinado e
  `people-service` 100% desacoplado do monolito.

Fechamento formal da Fase 70:

- o recorte de leitura local interna de endereco fica encerrado com guard
  dedicado, adapter JDBC preparado, servico interno guardado e observabilidade
  propria;
- o fechamento manteve `queryServiceConnected=false`, `routeCreated=false`,
  `writeCutoverAllowedNow=false`, `bffFrontendChangeAllowedNow=false` e nenhum
  payload externo alterado;
- a decisao de evoluir para escrita local de endereco foi separada para nova
  fase, porque leitura local guardada nao torna o read model autoridade de
  escrita.

### Fase 71 - Diagnostico de autoridade de escrita de endereco no `people-service`

Objetivo: iniciar uma fase backend-only para mapear o menor recorte futuro de
escrita de endereco que poderia sair do monolito, partindo da leitura local
interna fechada na Fase 70, sem criar rota REST, sem BFF/frontend, sem escrever
nas tabelas locais e sem remover o caminho atual do monolito.

Entregue na primeira subfase da Fase 71:

- foi criado o planner `PeopleAddressWriteAuthorityPlanner` e o contrato
  `PeopleAddressWriteAuthorityPlan`, expondo no health
  `peopleLocalPersistence.addressWriteAuthorityDiagnostic` o estado
  `diagnostic_started_no_write_cutover`;
- o diagnostico separa quatro operacoes candidatas: criar pessoa com endereco
  principal, atualizar endereco principal da pessoa, remover vinculos/endereco
  orfao e consulta ViaCEP para enriquecimento de entrada;
- cada operacao continua com `allowedNow=false`, porque a escrita de endereco
  ainda esta acoplada a transacao de pessoa/aluno/responsavel no monolito,
  limpeza de orfaos exige salvaguarda por contagem de referencias e ViaCEP nao
  e autoridade de persistencia;
- o health passou a registrar autoridades atuais no monolito
  (`PessoaFoundationService`, `PessoaEnderecoPort`, repositorios JPA de
  endereco e `ViaCepService`), contratos exigidos antes de qualquer write
  routing, blockers de consistencia, rollback e out-of-scope;
- ficaram fora do recorte: criar rota REST de escrita de endereco, alterar BFF
  ou frontend, escrever nas tabelas locais, remover o caminho do monolito e
  alterar payload de `consultarCadastro`.

Contagem da macrofase Fase 71: 2 subfases restantes estimadas: definir o
contrato de comando de escrita de endereco sem entidades JPA; depois avaliar
um piloto backend/backend shadow de comando sem persistir localmente.

Proxima fase pratica:

- definir `PeopleAddressWritePort`/DTOs internos para comando de endereco,
  incluindo idempotencia, regra de endereco principal, rollback e contrato de
  fallback para o monolito;
- manter `writeCutoverAllowedNow=false`, sem rota REST nova, sem BFF/frontend,
  sem escrita local e sem alterar os fluxos atuais de aluno/responsavel.

Entregue na segunda subfase da Fase 71:

- foram criados os contratos internos `PeopleAddressWritePort`,
  `PessoaEnderecoWriteCommand`, `PessoaEnderecoCleanupCommand` e
  `PessoaEnderecoWriteResult`, sem adapter, sem bean operacional e sem chamada
  por use case;
- o contrato de comando de escrita de endereco carrega `commandId`, `pessoaId`,
  `escolaId`, tipo de endereco, flag `principal`, campos persistidos de
  endereco, `idempotencyKey` e `requestedBy`;
- o contrato de cleanup separa a intencao de remover vinculos/endereco orfao
  por pessoa, tambem com `idempotencyKey`, mantendo a salvaguarda de endereco
  compartilhado como requisito antes de qualquer execucao real;
- o resultado explicita `selectedSource`, `persistedLocally=false`,
  `fallbackRequired=true`, status e avisos, para permitir piloto shadow futuro
  sem persistir localmente;
- o health `peopleLocalPersistence.addressWriteAuthorityDiagnostic` passou a
  expor `preparedCommandArtifacts` e o estado
  `command_contract_defined_no_write_cutover`, mantendo
  `writeCutoverAllowedNow=false`, `adapterCreated=false`,
  `routeCreated=false` e `localPersistenceConnected=false`;
- nao houve rota REST nova, BFF/frontend, escrita local, implementacao JDBC,
  remocao do caminho do monolito ou alteracao de fluxos atuais.

Contagem da macrofase Fase 71: 1 subfase restante estimada: avaliar um piloto
backend/backend shadow de comando de endereco sem persistencia local, apenas
observavel e com fallback obrigatorio para o monolito.

Proxima fase pratica:

- criar um servico shadow interno para receber `PeopleAddressWritePort`/DTOs e
  registrar decisao/metricas de comando sem executar persistencia local;
- manter o monolito como unica autoridade de escrita e bloquear qualquer rota,
  BFF/frontend, adapter JDBC de escrita ou alteracao dos fluxos atuais.

Entregue na terceira subfase da Fase 71:

- foi criado o servico interno `PeopleAddressWriteShadowService`, implementando
  `PeopleAddressWritePort` apenas para receber comandos de escrita/cleanup de
  endereco e registrar a decisao shadow, sem chamar monolito, sem adapter JDBC e
  sem persistir nas tabelas locais;
- os comandos passam por uma validacao minima de seguranca operacional
  (`commandId`, `pessoaId`, `escolaId` e `idempotencyKey`) antes de qualquer
  registro de decisao, evitando piloto sem chave de idempotencia;
- o resultado de ambos os comandos permanece explicito:
  `selectedSource=monolith_proxy`, `persistedLocally=false`,
  `fallbackRequired=true` e status
  `shadow_command_received_no_local_persistence`;
- foi adicionada a metrica
  `people.shadow.local.persistence.address.write.shadow.commands`, etiquetada
  por operacao, resultado e fonte selecionada, para comprovar que o comando foi
  recebido sem simular persistencia;
- o health `peopleLocalPersistence.addressWriteAuthorityDiagnostic` passou a
  expor `PeopleAddressWriteShadowService` em `preparedCommandArtifacts` e
  `shadowCommandExecution`, mantendo `writeCutoverAllowedNow=false`,
  `routeCreated=false` e `localPersistenceConnected=false`;
- nao houve rota REST nova, BFF/frontend, escrita local, migration, chamada ao
  monolito ou alteracao dos fluxos atuais de aluno/responsavel.

Contagem da macrofase Fase 71: 0 subfases restantes estimadas. O recorte de
diagnostico, contrato e piloto shadow interno de autoridade de escrita de
endereco fica fechado sem cutover.

Proxima fase pratica:

- iniciar diagnostico pontual de um adapter backend/backend de escrita para o
  monolito atras de guard, ainda sem rota externa e sem persistencia local, para
  decidir se os comandos shadow podem virar chamada controlada ao caminho atual
  do `school-management-service`;
- manter o rollback por desligamento do adapter e o monolito como unica
  autoridade de escrita ate existir reconciliacao verde de write/read model.

### Fase 72 - Diagnostico do adapter de escrita de endereco para o monolito

Objetivo: avaliar se os comandos shadow de endereco do `people-service` podem
evoluir para uma chamada backend/backend ao monolito, sem criar rota externa,
sem BFF/frontend, sem escrita local e sem transformar o read model em autoridade
de escrita.

Entregue na primeira subfase da Fase 72:

- foi criado o diagnostico `PeopleAddressWriteMonolithAdapterPlanner` e o
  contrato `PeopleAddressWriteMonolithAdapterPlan`, expondo no health
  `peopleLocalPersistence.addressWriteMonolithAdapterDiagnostic` o estado
  `monolith_http_write_contract_missing_adapter_blocked`;
- o diagnostico confirmou que o `school-management-service` ainda possui apenas
  portas internas Java para escrita/cleanup de endereco
  (`PessoaFoundationService` e `PessoaEnderecoPort`), sem contrato HTTP interno
  equivalente para o `people-service` chamar;
- foram mapeadas duas operacoes candidatas para um futuro adapter:
  `create-or-update-principal-address` e
  `cleanup-person-address-links-and-orphans`, ambas com
  `adapterAllowedNow=false`;
- ficaram definidos os contratos HTTP internos minimos que precisariam existir
  antes de qualquer adapter no `people-service`:
  `PUT /internal/pessoas/{pessoaId}/endereco-principal` e
  `DELETE /internal/pessoas/{pessoaId}/enderecos`, com `Idempotency-Key` e
  propagacao de `X-Correlation-Id`, `X-Usuario-Id` e `X-Escola-Id`;
- o health tambem passou a expor o estado atual do `people-service`:
  `PeopleAddressWriteShadowService` existe, mas
  `monolithWriteClientCreated=false`, `localPersistenceConnected=false` e
  `routeCreated=false`;
- nao houve implementacao de cliente HTTP de escrita, rota REST, BFF/frontend,
  migration, persistencia local, chamada ao monolito ou alteracao dos fluxos
  atuais de aluno/responsavel.

Contagem da macrofase Fase 72: 2 subfases restantes estimadas: primeiro definir
o contrato HTTP interno minimo no monolito sem adapter no `people-service`;
depois avaliar a conexao do adapter atras de guard, ainda sem persistencia local.

Proxima fase pratica:

- criar no `school-management-service` apenas o contrato HTTP interno minimo de
  escrita/cleanup de endereco, delegando para as autoridades atuais
  (`PessoaFoundationService`/`PessoaEnderecoPort`), com idempotencia,
  contexto interno e testes;
- manter o `people-service` apenas em modo shadow/diagnostico nesta subfase,
  sem implementar cliente de escrita nem alterar rotas externas.

Entregue na segunda subfase da Fase 72:

- o `school-management-service` passou a expor os contratos HTTP internos
  minimos para escrita/cleanup de endereco:
  `PUT /internal/pessoas/{pessoaId}/endereco-principal` e
  `DELETE /internal/pessoas/{pessoaId}/enderecos`;
- os endpoints internos exigem `X-Escola-Id`, `X-Usuario-Id`,
  `X-Correlation-Id` e `Idempotency-Key`, devolvendo resposta com `commandId`,
  `pessoaId`, ids de endereco quando aplicavel, fonte selecionada e flags de
  persistencia/fallback;
- a escrita continua delegada ao monolito por `PessoaEnderecoPort` e
  `PessoaFoundationService`, que agora tambem expõem
  `atualizarEnderecoPrincipalDaPessoa` sem transferir autoridade para o
  `people-service`;
- o diagnostico do `people-service` foi atualizado para
  `monolith_http_write_contract_defined_adapter_not_connected`, com
  `monolithHttpWriteContractAvailable=true`,
  `adapterImplementationAllowedNow=true`, `writeCutoverAllowedNow=false` e
  `localPersistenceAllowedNow=false`;
- nao houve cliente HTTP de escrita no `people-service`, rota externa,
  BFF/frontend, migration, persistencia local, escrita local ou alteracao dos
  fluxos atuais de aluno/responsavel.

Contagem da macrofase Fase 72: 1 subfase restante estimada: avaliar e, se
seguro, implementar o adapter backend/backend do `people-service` para esses
contratos internos, sempre atras de guard, sem rota externa e sem persistencia
local.

Proxima fase pratica:

- criar o cliente/adaptador de escrita do `people-service` para chamar os
  contratos internos do monolito somente atras de guard e mantendo fallback
  obrigatorio para o comando shadow;
- preservar `writeCutoverAllowedNow=false`, nao criar rota externa e nao
  transformar o read model de endereco em autoridade de escrita.

Entregue na terceira subfase da Fase 72:

- foi criado o adapter `MonolithPessoaAddressWriteClient` no `people-service`,
  implementando `PeopleAddressWritePort` para chamar os contratos internos do
  monolito:
  `PUT /internal/pessoas/{pessoaId}/endereco-principal` e
  `DELETE /internal/pessoas/{pessoaId}/enderecos`;
- o adapter propaga `X-Escola-Id`, `X-Usuario-Id`, `X-Correlation-Id` e
  `Idempotency-Key`, mapeia a resposta do monolito para
  `PessoaEnderecoWriteResult` e registra metricas
  `people.shadow.monolith.address.write.requests` e
  `people.shadow.monolith.address.write.failures`;
- o bean do adapter fica condicionado a
  `people.shadow.monolith.address-write-adapter-enabled=true`, que permanece
  `false` por padrao. Assim, o `PeopleAddressWriteShadowService` continua sendo
  o caminho operacional de comando shadow;
- em falha HTTP/indisponibilidade do monolito, o adapter retorna resultado com
  `fallbackRequired=true`, `selectedSource=monolith_proxy` e sem persistir
  localmente;
- o health `peopleLocalPersistence.addressWriteMonolithAdapterDiagnostic`
  passou para `monolith_write_adapter_prepared_guard_disabled_no_cutover`,
  indicando adapter implementado, guard desligado, sem rota externa, sem
  persistencia local e sem cutover de escrita.

Contagem da macrofase Fase 72: 0 subfases restantes estimadas. O bloco de
diagnostico e preparacao do adapter backend/backend de escrita de endereco esta
fechado sem ativar o adapter por padrao e sem retirar a autoridade de escrita do
monolito.

### Fase 73 - Fechamento do recorte pessoa/endereco antes da proxima familia

Objetivo: registrar explicitamente o que ja ficou fechado no `people-service`
para pessoa/endereco, o que continua bloqueado para ativacao operacional e qual
e a proxima familia minima que pode ser diagnosticada sem reabrir esse escopo.

Entregue na primeira subfase da Fase 73:

- foi criado no actuator `peopleLocalPersistence` o diagnostico
  `peopleAddressScopeClosureDiagnostic`, consolidando que o bloco atual ja
  fechou leitura local de catalogo/identidade, `consultarCadastro` guardado com
  fallback obrigatorio, contrato interno de leitura de endereco e preparacao do
  adapter backend/backend de escrita sem cutover;
- o diagnostico deixa explicito que nao ha necessidade de ativacao agora:
  `readScopeClosed=true`, `writeScopePreparedWithoutCutover=true`,
  `activationRequiredNow=false` e `safeToStartNextFamilyDiagnostic=true`;
- os bloqueios restantes ficaram formalizados apenas como criterio para fases
  futuras de ativacao, sem reabrir escopo nesta etapa: criacao de
  pessoa-com-endereco ainda acoplada a transacao do monolito, modelo local sem
  autoridade de escrita e guarda
  `people.shadow.monolith.address-write-adapter-enabled` mantida desligada por
  padrao;
- a proxima familia candidata minima ficou registrada como `pessoa_documento`,
  antes de `funcionario`, enquanto `professor` permanece fora deste recorte por
  ja estar tratado no `academic-professor-service`.

Contagem da macrofase Fase 73: 2 subfases restantes estimadas: primeiro
diagnosticar o contrato minimo de `pessoa_documento` no `people-service`; depois
decidir a primeira preparacao pratica dessa nova familia sem reabrir
endereco/pessoa.

Entregue na segunda subfase da Fase 73:

- foi criado no actuator `peopleLocalPersistence` o diagnostico
  `peopleDocumentScopeDiagnostic`, separando o menor recorte seguro de
  `pessoa_documento` sem reabrir `pessoa/endereco` e sem antecipar
  `cutover/shadow` operacional;
- o diagnostico registrou que o primeiro recorte preferido e leitura interna
  read-only de metadados por pessoa, mantendo upload, exclusao e cleanup no
  monolito: `diagnosticReadyNow=true`,
  `internalContractSeparationAllowedNow=true`,
  `localPersistenceAllowedNow=false`,
  `externalRouteChangeAllowedNow=false` e
  `fallbackToCurrentMonolithRequired=true`;
- as dependencias minimas no monolito ficaram explicitadas em
  `DocumentoController`, `DocumentoAlunoController`,
  `DocumentoPersistenceGateway`, `DocumentoJpaRepository` e nos cleanups de
  `AlunoPersistenceGateway`/`ResponsavelPersistenceGateway`, deixando claro que
  `pessoa_documento` depende tambem de `documento`, `tipo_documento` e do join
  com `pessoa.id_escola`;
- os impactos de consistencia e PII ficaram formalizados: exclusao exige
  remover vinculos antes de apagar documentos orfaos, qualquer leitura futura
  precisa reconciliar `pessoa_documento` com `documento`, e campos como
  `numeroDocumento` e `caminhoArquivo` nao devem ampliar exposicao nesta
  subfase;
- a migracao minima futura ficou restrita a eventual read model de metadados e
  vinculos, sem storage binario, sem escrita local autoritativa, sem BFF e sem
  nova rota externa.

Contagem da macrofase Fase 73: 1 subfase restante estimada: preparar a primeira
implementacao pratica do contrato interno read-only de metadados de
`pessoa_documento`, ainda sem rota externa, sem BFF e sem mover escrita do
monolito.

Entregue na terceira subfase da Fase 73:

- foram criados no `people-service` os artefatos internos minimos de leitura
  read-only de metadados de documento: `PessoaDocumentoMetadataLocalReadResponse`,
  `PeopleDocumentMetadataLocalReadPort` e
  `PeopleDocumentMetadataLocalReadService`, sem rota externa, sem entidade JPA
  e sem adapter JDBC conectado nesta subfase;
- o actuator `peopleLocalPersistence` passou a expor o diagnostico
  `peopleDocumentInternalMetadataReadContractDiagnostic`, registrando
  `contractPrepared=true`, `internalServicePrepared=true`,
  `adapterCreated=false`, `localPersistenceConnected=false`,
  `externalRouteCreated=false` e `fallbackRequired=true`;
- o contrato interno foi limitado aos metadados minimos
  (`id_pessoa_documento`, `id_pessoa`, `id_documento`, `id_tipo_documento`,
  `tipo_documento_codigo`, `tipo_documento_descricao`, `numero_documento`,
  `caminho_arquivo`, `observacao`, `data_upload`) e aos consumidores internos
  `listarDocumentosPorPessoa` e `buscarDocumentoPorId`;
- o servico interno foi preparado para fallback seguro quando nao houver adapter
  local, registrando metrica de leitura de metadados sem ativar cutover, sem
  alterar BFF/frontend e sem mover upload, exclusao, cleanup ou storage binario
  do monolito.

Contagem da macrofase Fase 73: 0 subfases restantes estimadas. O bloco
`pessoa/endereco` foi fechado e a primeira fronteira interna de
`pessoa_documento` ficou preparada sem rota externa, sem persistencia local e
sem reabrir os recortes anteriores.

Proxima fase pratica:

- iniciar a proxima macrofase backend com o diagnostico do adapter/local read
  candidate de metadados de `pessoa_documento`, decidindo se vale criar schema
  minimo proprio ou se o proximo recorte mais seguro passa a ser `funcionario`;
- manter fora do escopo qualquer migracao de upload, escrita autoritativa,
  cleanup documental, BFF ou frontend.

### Fase 74 - Diagnostico do candidato local de metadados de pessoa_documento

Objetivo: decidir, com base em schema minimo, reconciliacao e ownership, se a
familia `pessoa_documento` ainda comporta o proximo recorte backend seguro ou
se deve ceder lugar a `funcionario`.

Entregue na primeira subfase da Fase 74:

- foi criado no actuator `peopleLocalPersistence` o diagnostico
  `peopleDocumentLocalReadCandidateDiagnostic`, abrindo formalmente a nova
  macrofase para o candidato local de leitura de metadados de
  `pessoa_documento`;
- o diagnostico registrou a decisao de seguir na familia
  `pessoa_documento` antes de `funcionario`:
  `continueWithDocumentFamilyNow=true`,
  `switchToFuncionarioNow=false`,
  `schemaDiagnosticAllowedNow=true`,
  `adapterDiagnosticAllowedNow=true` e
  `localReadCutoverAllowedNow=false`;
- ficaram separados como fontes minimas do candidato local os joins entre
  `pessoa_documento`, `documento`, `tipo_documento` e `pessoa`, com chave de
  reconciliacao primaria `pessoa_documento.id_pessoa_documento` e checagens
  secundarias por `id_pessoa`, `id_documento`, `id_tipo_documento`,
  `data_upload` e `id_escola`;
- o diagnostico formalizou que o read model candidato ainda depende de regras
  de ownership por escola via `pessoa.id_escola`, de normalizacao da coluna
  `caminho_arquivo` e da manutencao do monolito como autoridade para upload e
  delete;
- nao houve migration, adapter JDBC novo, rota externa, BFF/frontend, escrita
  local, cleanup local nem alteracao funcional no monolito.

Contagem da macrofase Fase 74: 1 subfase restante estimada: preparar o
diagnostico do schema fisico minimo de metadados de `pessoa_documento` antes de
qualquer adapter local.

Entregue na segunda subfase da Fase 74:

- foi criado no actuator `peopleLocalPersistence` o diagnostico
  `peopleDocumentMetadataSchemaDiagnostic`, formalizando o schema fisico
  minimo, a estrategia de backfill e as regras de reconciliacao do candidato
  local de metadados de `pessoa_documento`;
- o diagnostico registrou
  `migrationAllowedNow=true`,
  `backfillAllowedNow=true`,
  `localReadAdapterAllowedNow=false` e
  `localReadCutoverAllowedNow=false`, deixando explicito que a fase fecha o
  planejamento de schema sem liberar adapter local nem cutover;
- o read model candidato ficou consolidado em uma tabela minima
  `people_documento_read_model`, com chave primaria de reconciliacao
  `id_pessoa_documento` e colunas estritamente necessarias para metadata e
  ownership: ids de pessoa/documento/tipo, codigo/descricao do tipo,
  `numero_documento`, `caminho_arquivo`, `observacao`, `data_upload`,
  `id_escola` e `created_at`;
- a estrategia minima futura ficou amarrada a migration opt-in
  `V5__create_people_document_metadata_read_model.sql` e backfill
  `monolith_jdbc -> people_documento_read_model`, ambos desligados por padrao;
- os blockers antes de qualquer adapter ficaram registrados: drift de
  `tipo_documento`, definicao de normalizacao de `caminho_arquivo`, exclusao de
  documentos orfaos fora do read model e divergencia de ownership por
  `pessoa.id_escola`;
- nao houve migration executavel nova, adapter JDBC, rota externa, BFF,
  frontend, escrita local, upload, delete ou cleanup fora do monolito.

Contagem da macrofase Fase 74: 0 subfases restantes estimadas. A familia
`pessoa_documento` ficou fechada no nivel de diagnostico e preparacao de
fronteira/schema, ainda sem adapter local e sem rota externa.

Proxima fase pratica:

- iniciar a proxima macrofase backend decidindo entre:
  diagnosticar a primeira preparacao real do adapter local de metadados de
  `pessoa_documento`; ou
  encerrar essa familia como suficientemente planejada por agora e abrir o
  diagnostico minimo de `funcionario`;
- manter fora do escopo upload, escrita autoritativa, cleanup documental,
  storage binario, BFF e frontend.

### Fase 75 - Preparacao do adapter local de metadados de pessoa_documento

Objetivo: materializar o primeiro adapter JDBC local de metadados de
`pessoa_documento` no `people-service`, com migration opt-in e sem ativar rota,
cutover, backfill ou reconciliacao operacional.

Entregue na primeira subfase da Fase 75:

- foi criado o adapter `JdbcPeopleDocumentMetadataLocalReadAdapter`, ligado ao
  contrato interno `PeopleDocumentMetadataLocalReadPort`, usando leitura direta
  sobre `people_documento_read_model` filtrada por `id_escola`;
- a migration opt-in
  `V5__create_people_document_metadata_read_model.sql` passou a existir no
  pacote de read model do `people-service`, formalizando a estrutura minima
  fisica antes de qualquer execucao operacional;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentMetadataLocalAdapterPreparationDiagnostic`, registrando
  `adapterPrepared=true`, `internalServiceConnected=true`,
  `externalRouteCreated=false` e `localReadCutoverAllowedNow=false`;
- o diagnostico de schema `peopleDocumentMetadataSchemaDiagnostic` foi
  atualizado para refletir que o adapter JDBC ja esta preparado, mas continua
  sem ativacao, sem backfill e sem reconciliacao verde;
- nao houve rota externa nova, nao houve uso do adapter por query publica, nao
  houve BFF/frontend, nao houve upload local, delete local, backfill executado,
  reconciliacao executada ou alteracao funcional no `school-management-service`.

Contagem da macrofase Fase 75: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- preparar o recorte minimo de backfill e reconciliacao de
  `people_documento_read_model`, ainda sem ativar leitura local real; ou
- se quiser reduzir o risco antes de entrar em dados de documento, abrir o
  diagnostico minimo de `funcionario`.

### Fase 76 - Preparacao do backfill/reconciliacao de pessoa_documento

Objetivo: estender o ciclo opt-in de backfill/reconciliacao do `people-service`
para `people_documento_read_model`, sem ativar leitura local real e sem alterar
rotas externas.

Entregue na primeira subfase da Fase 76:

- o ciclo JDBC opt-in de sincronizacao do `people-service` foi estendido para
  `people_documento_read_model`, usando fonte `monolith_jdbc` com join entre
  `pessoa_documento`, `documento`, `tipo_documento` e `pessoa`;
- o backfill continua desligado por padrao e so executa quando
  `people.shadow.local-persistence.backfill-enabled` e/ou
  `people.shadow.local-persistence.reconciliation-enabled` forem habilitadas;
- a reconciliacao usa `people_documento_read_model.id_pessoa_documento` como
  chave principal e compara `id_pessoa`, `id_documento`, `id_tipo_documento`,
  codigo/descricao do tipo, `numero_documento`, `caminho_arquivo`,
  `observacao`, `data_upload` e `id_escola`;
- a regra de consistencia bloqueia o relatorio quando a origem trouxer o mesmo
  `id_documento` repetido em mais de um vinculo, reportando
  `document-metadata-duplicate-document-id-in-source`;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentBackfillReconciliationDiagnostic`, registrando
  `backfillAllowedNow=true`, `reconciliationAllowedNow=true` e
  `localReadCutoverAllowedNow=false`;
- nao houve ativacao de leitura local de documento, BFF/frontend, rota externa,
  escrita local, upload local, delete local ou cutover.

Contagem da macrofase Fase 76: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- avaliar a elegibilidade de ativacao interna da leitura local de metadados de
  `pessoa_documento`, ainda mantendo fallback obrigatorio e sem rota externa; ou
- se quiser segurar documentos por agora, abrir o diagnostico minimo de
  `funcionario`.

### Fase 77 - Elegibilidade de ativacao interna da leitura local de pessoa_documento

Objetivo: aplicar o mesmo padrao interno de guard/fallback ao servico de
metadados de `pessoa_documento`, sem criar rota nova, sem ligar
`consultarCadastro` e sem mudar qualquer contrato externo.

Entregue na primeira subfase da Fase 77:

- `PeopleDocumentMetadataLocalReadService` passou a consultar o guard interno
  antes de usar o adapter JDBC local, retornando vazio/lista vazia quando o
  guard bloquear e preservando fallback obrigatorio;
- `PeopleLocalReadCutoverGuard` passou a expor a operacao interna
  `documentMetadataLocalRead`, com decisao observavel, source candidata
  `people_documento_read_model` e metrica dedicada de roteamento;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentLocalReadActivationEligibilityDiagnostic`, consolidando fase,
  status, precondicoes do guard, source selecionada e rollback;
- a elegibilidade fica dinamica: quando o estado local esta verde, o health
  reporta o recorte como internamente elegivel; quando nao esta verde, o
  fallback para `monolith_proxy` continua sendo o comportamento esperado;
- nao houve rota REST nova, alteracao em `PessoaQueryService`, mudanca de BFF,
  frontend, escrita local, upload local ou cutover externo.

Contagem da macrofase Fase 77: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- avaliar se existe algum uso interno minimo e seguro para consumir
  `PeopleDocumentMetadataLocalReadService` sem alterar rotas externas e sem
  tocar `consultarCadastro`; ou
- se preferir manter documentos estacionados neste ponto, abrir o diagnostico
  minimo de `funcionario`.

### Fase 78 - Diagnostico do consumidor interno minimo de pessoa_documento

Objetivo: decidir se existe algum consumidor interno real e seguro para usar
`PeopleDocumentMetadataLocalReadService` agora, sem criar rota nova, sem tocar
`consultarCadastro` e sem acoplamento artificial ao monolito.

Entregue na primeira subfase da Fase 78:

- o diagnostico formal mostrou que o `people-service` ainda nao possui fluxo
  interno nativo de documento alem da propria observabilidade e da validacao do
  adapter local;
- os fluxos reais de upload/listagem/cleanup documental continuam no
  `school-management-service`, entao forcar um consumidor agora criaria rota
  nova, alteracao indevida de contrato ou acoplamento artificial;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentInternalUsageCandidateDiagnostic`, registrando que nao existe
  consumidor interno seguro neste ponto e que o fallback continua obrigatorio;
- a decisao desta fase foi nao conectar `PessoaQueryService`, nao criar rota
  interna adicional e nao inventar um uso tecnico sem necessidade funcional.

Contagem da macrofase Fase 78: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- abrir o diagnostico minimo de `funcionario` como proxima familia backend; ou
- manter `pessoa_documento` estacionado ate existir um consumidor interno real
  justificado por fase futura.

### Fase 79 - Diagnostico minimo de funcionario

Objetivo: mapear o menor recorte seguro de `funcionario` para o
`people-service`, ainda sem rota externa, sem persistencia local, sem tocar
autenticacao e sem mexer no fluxo funcional de professor.

Entregue na primeira subfase da Fase 79:

- foi formalizado o diagnostico de `funcionario` no actuator
  `peopleLocalPersistence`, deixando explicito que o primeiro recorte seguro e
  apenas um resumo interno read-only por escola;
- o diagnostico registrou como dependencias atuais do monolito o contrato
  interno de elegibilidade de professor (`GET /internal/funcionarios/{id}/professor`
  e `GET /internal/funcionarios/professor-elegiveis`), o
  `FuncionarioProfessorService` e o uso indireto de professor/autenticacao em
  `IdentidadeTenantService`;
- tambem ficou explicito que write de funcionario, cargo, usuario, autenticacao
  e qualquer alteracao no cadastro de professor permanecem fora de escopo nesta
  macrofase;
- a recomendacao objetiva da fase ficou registrada como
  `funcionario_internal_summary_read_only`, sem criar rota nova, sem BFF e sem
  cutover.

Contagem da macrofase Fase 79: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- preparar o contrato interno minimo de resumo read-only de `funcionario` no
  `people-service`, ainda sem persistencia local e sem rota externa; ou
- se preferir segurar `funcionario`, abrir o proximo diagnostico backend de
  outra familia ainda acoplada.

### Fase 80 - Contrato interno minimo de resumo de funcionario

Objetivo: preparar a fronteira interna minima de leitura para `funcionario` no
`people-service`, ainda sem adapter local, sem persistencia propria, sem rota
externa e sem deslocar para este servico a regra de elegibilidade de professor.

Entregue na primeira subfase da Fase 80:

- foram criados os artefatos internos minimos do contrato de resumo de
  `funcionario`: DTO de resposta, port de leitura e service interno read-only,
  todos sem adapter conectado nesta etapa;
- o payload minimo ficou reduzido a `id_funcionario`, `id_pessoa`, `id_escola`,
  `nome_completo`, `cargo_descricao` e `ativo`, evitando puxar regra de
  professor, autenticacao ou entidades JPA de RH para dentro do contrato;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalSummaryContractDiagnostic`, registrando que o
  contrato e o service interno estao preparados, mas que adapter, persistencia
  local e rota continuam desligados;
- a fase preserva o `school-management-service` como fonte funcional unica para
  RH interno, professor e autenticacao, sem qualquer mudanca de comportamento.

Contagem da macrofase Fase 80: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- preparar o adapter local minimo do resumo interno de `funcionario`, ainda sem
  rota externa e sem write; ou
- se o risco aumentar, parar em diagnostico adicional antes de qualquer conexao
  com persistencia local.

### Fase 81 - Preparacao do adapter local minimo de funcionario

Objetivo: deixar pronto o adapter JDBC minimo do resumo interno de
`funcionario` no `people-service`, incluindo schema opt-in, sem ativar rota,
sem backfill/reconciliacao e sem deslocar para este servico a regra funcional
de professor ou autenticacao.

Entregue na primeira subfase da Fase 81:

- foi criado o adapter JDBC `JdbcPeopleFuncionarioInternalSummaryAdapter`,
  lendo o read model `people_funcionario_read_model` apenas por `id_escola` e
  `id_funcionario`, com fallback funcional ainda preservado no RH interno do
  monolito;
- foi adicionada a migration opt-in
  `V6__create_people_funcionario_internal_summary_read_model.sql`, com schema
  minimo alinhado ao contrato interno ja fechado na Fase 80;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalSummaryAdapterPreparationDiagnostic`, deixando
  explicito que adapter e schema estao preparados, mas backfill, reconciliacao,
  cutover, rota externa e writes continuam fora de escopo;
- a fase continua sem qualquer alteracao funcional no fluxo de professor,
  autenticacao, BFF ou frontend.

Contagem da macrofase Fase 81: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- preparar o backfill/reconciliacao minimo de `funcionario_internal_summary`,
  ainda sem rota externa e sem ativacao de leitura local; ou
- se aparecer dependencia oculta em RH/autenticacao, abrir antes um diagnostico
  complementar estritamente interno.

### Fase 82 - Backfill e reconciliacao minima de funcionario

Objetivo: incluir `funcionario_internal_summary` no pipeline local de backfill e
reconciliacao do `people-service`, ainda sem ativar leitura local, sem rota
externa e sem deslocar regras de professor/autenticacao.

Entregue na primeira subfase da Fase 82:

- o pipeline `JdbcPeopleCatalogReadModelSyncAdapter` passou a sincronizar
  `people_funcionario_read_model` a partir de `funcionario + pessoa + cargo`,
  usando `id_funcionario` como chave de reconciliacao e preservando `id_escola`
  derivado da `pessoa`;
- o coordenador de backfill local passou a considerar
  `people_funcionario_read_model` nas tabelas planejadas, sem ativar qualquer
  leitura local oficial;
- foi formalizado no actuator `peopleLocalPersistence` o diagnostico
  `peopleFuncionarioInternalSummaryBackfillReconciliationDiagnostic`,
  registrando fonte, alvo, chave de reconciliacao, bloqueadores de consistencia
  e rollback;
- a fase segue sem BFF, sem frontend, sem rota nova, sem cutover e sem mudar o
  comportamento funcional de RH, professor ou autenticacao no monolito.

Contagem da macrofase Fase 82: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- preparar a elegibilidade de ativacao da leitura local de
  `funcionario_internal_summary`, ainda bloqueada ate backfill/reconciliacao
  verde e sem rota externa; ou
- se aparecer risco de consistencia em RH/autenticacao, abrir um diagnostico
  interno complementar antes de qualquer ativacao.

### Fase 83 - Elegibilidade de leitura local interna de funcionario

Objetivo: preparar a ativacao elegivel da leitura local interna de
`funcionario_internal_summary`, ainda sem rota externa, sem BFF e sem mudar a
autoridade funcional de RH/autenticacao no monolito.

Entregue na primeira subfase da Fase 83:

- o `PeopleLocalReadCutoverGuard` passou a reconhecer a operacao interna
  `funcionarioInternalSummaryLocalRead`, com decisao dedicada e fallback
  obrigatorio para `monolith_internal_rh`;
- o `PeopleFuncionarioInternalSummaryService` foi conectado ao guard de leitura
  local, registrando metricas de sucesso e fallback, mas sem criar consumidor
  novo nem expor rota externa;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalSummaryLocalReadActivationEligibilityDiagnostic`,
  deixando explicito quando o read model esta verde para eventual uso interno
  controlado;
- a fase segue sem BFF, sem frontend, sem cutover externo, sem write cutover e
  sem alterar comportamento funcional oficial de RH, professor ou autenticacao.

Contagem da macrofase Fase 83: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- diagnosticar se existe um primeiro consumidor interno real e seguro para
  `funcionario_internal_summary`, sem abrir rota nova; ou
- se nao houver consumidor justificavel agora, fechar formalmente o bloco e
  manter o service apenas preparado e protegido por guard.

### Fase 84 - Diagnostico de consumidor interno de funcionario

Objetivo: confirmar se existe algum primeiro consumidor interno real e seguro
para `funcionario_internal_summary` no `people-service`, sem criar rota nova e
sem ampliar escopo para RH/autenticacao.

Entregue na primeira subfase da Fase 84:

- foi formalizado o diagnostico de uso interno minimo de
  `funcionario_internal_summary`, verificando que o `people-service` ainda nao
  possui fluxo nativo que justifique consumir esse read local fora da propria
  preparacao tecnica;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioInternalUsageCandidateDiagnostic`, deixando explicito que
  nao ha consumidor seguro a conectar agora sem abrir superficie fora da fase;
- a recomendacao da fase passa a ser fechar o bloco de funcionario preparado,
  sem inventar consumidor artificial e sem mudar rota, BFF, frontend ou writes.

Contagem da macrofase Fase 84: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- fechar formalmente o bloco backend/backend de `funcionario_internal_summary`
  como preparado e sem consumidor real nesta etapa; ou
- iniciar outro recorte backend menor fora da familia de funcionario.

### Fase 85 - Fechamento formal do bloco de funcionario

Objetivo: encerrar formalmente o bloco backend/backend de
`funcionario_internal_summary` como preparado neste estagio, sem reabrir rota,
sem forcar consumidor artificial e sem mudar autoridade do monolito.

Entregue na primeira subfase da Fase 85:

- foi consolidado o fechamento formal do bloco de
  `funcionario_internal_summary`, registrando contrato, adapter, read model,
  backfill/reconciliacao e guard de leitura local como capacidades prontas;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleFuncionarioScopeClosureDiagnostic`, deixando explicito que o bloco pode
  ser considerado encerrado neste estagio sem ativacao adicional;
- a fase conclui que a proxima evolucao deve sair para outra familia backend,
  sem reabrir `funcionario_internal_summary` nesta mesma linha.

Contagem da macrofase Fase 85: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- iniciar um novo recorte backend minimo fora da familia de funcionario; ou
- se houver necessidade objetiva futura, reabrir funcionario apenas com novo
  consumidor real ou nova fronteira tecnica justificada.

### Fase 86 - Fechamento formal do bloco de pessoa_documento

Objetivo: encerrar formalmente o bloco backend/backend de
`people_documento_read_model` como preparado neste estagio, sem reabrir rota,
sem forcar consumidor artificial e sem mover upload, listagem ou cleanup
documental para fora do monolito.

Entregue na primeira subfase da Fase 86:

- foi consolidado o fechamento formal do bloco de metadados de
  `pessoa_documento`, registrando como capacidades prontas o diagnostico de
  escopo, o contrato interno, o candidato local de leitura, o schema,
  o adapter JDBC, o backfill/reconciliacao, o guard de leitura local e o
  diagnostico de ausencia de consumidor interno real;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentScopeClosureDiagnostic`, deixando explicito que o bloco pode
  ser considerado encerrado neste estagio sem ativacao adicional;
- a fase conclui que a proxima evolucao deve sair para outra familia backend,
  sem reabrir `pessoa_documento` nesta mesma linha.

Contagem da macrofase Fase 86: 0 subfases restantes estimadas.

Proxima fase pratica sugerida:

- iniciar um novo recorte backend minimo fora das familias `funcionario` e
  `pessoa_documento`; ou
- se houver necessidade objetiva futura, reabrir `pessoa_documento` apenas com
  novo consumidor real ou nova fronteira tecnica justificada.

### Fase 87 - Diagnostico do primeiro consumidor real de documento no monolito

Objetivo: mapear no `school-management-service` o menor consumidor real que
poderia usar futuramente o contrato interno de metadados de documento vindo do
`people-service`, sem alterar rotas externas, sem tocar upload e sem mexer em
cleanup/exclusao nesta etapa.

Entregue na primeira subfase da Fase 87:

- foi formalizado no monolito o diagnostico do primeiro consumidor real de
  `pessoa_documento`, registrando que `DocumentoAlunoService.listarPorAluno` e
  o menor recorte seguro para uma futura fronteira interna;
- o diagnostico deixou explicito que `GET /api/documentos-alunos/alunos/{alunoId}`
  e menor que `buscarPorId` e menor que o fluxo generico `/api/documentos`,
  porque permanece em um unico agregado funcional, reutiliza a resolucao
  `alunoId -> pessoaId` e nao exige mover upload ou exclusao;
- o actuator `documentoPeopleConsumerDiagnostic` passou a expor as dependencias
  atuais do `DocumentoGateway`, os impactos de consistencia e a recomendacao de
  preparar primeiro um contrato interno de consumo sem ativar leitura local.

Contagem da macrofase Fase 87: 2 subfases restantes estimadas: primeiro
preparar o contrato interno/adapter de consumo de metadados para
`DocumentoAlunoService.listarPorAluno`; depois avaliar a conexao controlada com
fallback obrigatorio antes de considerar outros fluxos de documento.

Proxima fase pratica sugerida:

- preparar no `school-management-service` o contrato interno minimo de consumo
  de metadados de documento para `DocumentoAlunoService.listarPorAluno`, ainda
  sem trocar a autoridade atual do `DocumentoGateway`; ou
- se o recorte de aluno precisar ser segurado, manter `documento` somente em
  diagnostico e escolher outra familia backend menor.

### Fase 88 - Contrato do consumidor futuro de documento por aluno no `people-service`

Objetivo: preparar exclusivamente no `people-service` o contrato do primeiro
consumidor futuro de metadados de documento por aluno, sem criar rota, sem BFF,
sem frontend e sem tocar o legado `school-management-service`.

Entregue na primeira subfase da Fase 88:

- foi formalizado no `people-service` o contrato do consumidor futuro
  `documento_aluno_listar_por_aluno`, reaproveitando como operacao-alvo
  `listarDocumentosPorPessoa` do read model local ja preparado;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentAlunoConsumerContractDiagnostic`, deixando explicito que o
  contrato esta preparado mas ainda nao deve ser conectado a fluxo real;
- a fase registra como fronteiras preservadas: nenhuma rota nova, nenhuma
  mudanca em BFF/frontend, nenhuma migracao de binario e nenhuma alteracao no
  `school-management-service`.

Contagem da macrofase Fase 88: 1 subfase restante estimada: preparar a
estrategia de conexao desse consumidor inteiramente no `people-service`,
validando dependencias `alunoId -> pessoaId` sem tocar o legado.

Proxima fase pratica sugerida:

- preparar a estrategia de conexao do consumidor futuro
  `documento_aluno_listar_por_aluno` inteiramente no `people-service`, ainda
  sem conectar fluxo real e sem alterar o `school-management-service`; ou
- se aparecer dependencia estrutural escondida de `aluno -> pessoa`, fechar a
  fase como diagnostico suficiente e trocar para outra familia nova.

### Fase 89 - Estrategia de conexao do consumidor futuro de documento por aluno

Objetivo: fechar exclusivamente no `people-service` a estrategia interna de
resolucao `alunoId -> pessoaId` necessaria para o futuro consumidor de
metadados de documento por aluno, sem criar rota, sem ligar fluxo real e sem
tocar o legado `school-management-service`.

Entregue na primeira subfase da Fase 89:

- foi criada a fronteira interna minima `PeopleStudentPessoaLocalReadPort` com
  service e adapter JDBC locais para resolver `alunoId -> pessoaId` a partir do
  read model do proprio `people-service`;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentAlunoConsumerConnectionStrategyDiagnostic`, registrando que a
  estrategia local esta pronta para futura conexao mas continua desligada de
  qualquer fluxo real;
- a fase preserva o escopo novo-only: nenhuma rota nova, nenhuma mudanca de
  `consultarCadastro`, nenhuma alteracao em BFF/frontend e nenhuma mudanca no
  `school-management-service`.

Contagem da macrofase Fase 89: 0 subfases restantes estimadas. O bloco do
consumidor futuro de documento por aluno fica fechado como preparado no codigo
novo, ainda sem conexao a fluxo real.

Proxima fase pratica sugerida:

- fazer o fechamento formal desse bloco de consumidor futuro de documento por
  aluno e escolher a proxima familia exclusivamente dentro do codigo novo; ou
- se surgir um consumidor real em outro servico novo, reabrir esse recorte
  apenas para integrar a fronteira ja preparada, sem tocar o legado.

### Fase 90 - Contrato do consumidor futuro de documento por responsavel

Objetivo: preparar exclusivamente no `people-service` o contrato do primeiro
consumidor futuro de metadados de documento por responsavel, sem criar rota,
sem BFF, sem frontend e sem tocar o legado `school-management-service`.

Entregue na primeira subfase da Fase 90:

- foi formalizado no `people-service` o contrato do consumidor futuro
  `documento_responsavel_listar_por_responsavel`, reaproveitando como
  operacao-alvo `listarDocumentosPorPessoa` do read model local ja preparado;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentResponsavelConsumerContractDiagnostic`, deixando explicito que
  o contrato esta preparado mas ainda nao deve ser conectado a fluxo real;
- a fase preserva o mesmo escopo novo-only: nenhuma rota nova, nenhuma mudanca
  em BFF/frontend e nenhuma alteracao no `school-management-service`.

Contagem da macrofase Fase 90: 1 subfase restante estimada: preparar a
estrategia de conexao desse consumidor inteiramente no `people-service`,
validando dependencias `responsavelId -> pessoaId` sem tocar o legado.

Proxima fase pratica sugerida:

- preparar a estrategia de conexao do consumidor futuro
  `documento_responsavel_listar_por_responsavel` inteiramente no
  `people-service`, ainda sem conectar fluxo real e sem alterar o
  `school-management-service`; ou
- se aparecer dependencia estrutural escondida de `responsavel -> pessoa`,
  fechar a fase como diagnostico suficiente e trocar para outra familia nova.

### Fase 91 - Estrategia de conexao do consumidor futuro de documento por responsavel

Objetivo: fechar exclusivamente no `people-service` a estrategia interna de
resolucao `responsavelId -> pessoaId` necessaria para o futuro consumidor de
metadados de documento por responsavel, sem criar rota, sem ligar fluxo real e
sem tocar o legado `school-management-service`.

Entregue na primeira subfase da Fase 91:

- foi criada a fronteira interna minima `PeopleResponsiblePessoaLocalReadPort`
  com service e adapter JDBC locais para resolver `responsavelId -> pessoaId` a
  partir do read model do proprio `people-service`;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleDocumentResponsavelConsumerConnectionStrategyDiagnostic`,
  registrando que a estrategia local esta pronta para futura conexao mas
  continua desligada de qualquer fluxo real;
- a fase preserva o escopo novo-only: nenhuma rota nova, nenhuma mudanca de
  `consultarCadastro`, nenhuma alteracao em BFF/frontend e nenhuma mudanca no
  `school-management-service`.

Contagem da macrofase Fase 91: 0 subfases restantes estimadas. O bloco do
consumidor futuro de documento por responsavel fica fechado como preparado no
codigo novo, ainda sem conexao a fluxo real.

Proxima fase pratica sugerida:

- fazer o fechamento formal desse bloco de consumidor futuro de documento por
  responsavel e escolher a proxima familia exclusivamente dentro do codigo
  novo; ou
- se surgir um consumidor real em outro servico novo, reabrir esse recorte
  apenas para integrar a fronteira ja preparada, sem tocar o legado.

### Fase 92 - Preparacao da leitura interna de contato no `people-service`

Objetivo: criar exclusivamente no `people-service` a fronteira interna minima
de contato de pessoa (`email` e `telefone`), reutilizando o read model local ja
existente de `pessoa`, sem criar rota, sem BFF, sem frontend e sem tocar o
legado `school-management-service`.

Entregue na primeira subfase da Fase 92:

- foi criado o contrato interno `PeopleContactLocalReadPort` com response
  proprio `PessoaContatoLocalReadResponse`, sem expor entidade JPA;
- foi criado o `PeopleContactLocalReadService` com fallback local seguro quando
  o adapter nao estiver disponivel ou falhar;
- foi criado o `JdbcPeopleContactLocalReadAdapter`, lendo `email` e `telefone`
  diretamente da tabela local `pessoa`, que ja faz parte do read model do
  `people-service`;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleContactLocalReadPreparationDiagnostic`, deixando explicito que a
  fronteira interna de contato esta pronta mas continua sem rota externa e sem
  consumidor real conectado;
- a fase preserva o escopo novo-only: nenhuma mudanca de `consultarCadastro`,
  nenhuma alteracao em BFF/frontend e nenhuma alteracao no
  `school-management-service`.

Contagem da macrofase Fase 92: 1 subfase restante estimada: diagnosticar o
primeiro consumidor interno legitimo desse contrato de contato ainda dentro do
codigo novo, sem abrir rota externa e sem tocar o legado.

Proxima fase pratica sugerida:

- diagnosticar o primeiro consumidor interno legitimo de
  `PeopleContactLocalReadPort` exclusivamente no codigo novo, mantendo a
  fronteira pronta mas ainda sem conexao funcional real; ou
- se nao houver consumidor novo justificavel agora, fechar a fase como bloco
  preparado e seguir para outra familia nova de desacoplamento.

### Fase 93 - Diagnostico do consumidor interno de contato no `people-service`

Objetivo: diagnosticar exclusivamente no `people-service` se existe consumidor
interno legitimo para a nova fronteira de contato (`email` e `telefone`), sem
forcar conexao artificial, sem criar rota, sem BFF, sem frontend e sem tocar o
legado `school-management-service`.

Entregue na primeira subfase da Fase 93:

- foi criado o planner `PeopleContactInternalUsageCandidatePlanner`,
  formalizando que ainda nao existe consumidor interno seguro para o contrato
  de contato sem ampliar escopo de `PessoaQueryService` ou de rotas externas;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleContactInternalUsageCandidateDiagnostic`, deixando explicito que o
  bloco de contato segue preparado, mas ainda sem conexao funcional real;
- a fase preserva o escopo novo-only: nenhuma mudanca em `consultarCadastro`,
  nenhuma rota nova, nenhuma alteracao em BFF/frontend e nenhuma alteracao no
  `school-management-service`.

Contagem da macrofase Fase 93: 0 subfases restantes estimadas. O bloco de
contato fica fechado como fronteira preparada no codigo novo, ainda sem
consumidor real conectado.

Proxima fase pratica sugerida:

- fazer o fechamento formal do bloco de contato e escolher a proxima familia
  exclusivamente dentro do codigo novo; ou
- se surgir um consumidor real em outro servico novo, reabrir esse recorte
  apenas para integrar a fronteira ja preparada, sem tocar o legado.

### Fase 94 - Fechamento formal do bloco de contato

Objetivo: registrar explicitamente no `people-service` que o bloco de contato
(`email` e `telefone`) ficou preparado e fechado neste estagio, sem forcar
conexao a fluxo real, sem rota externa e sem tocar o legado
`school-management-service`.

Entregue na primeira subfase da Fase 94:

- foi criado o planner `PeopleContactScopeClosurePlanner`, consolidando que o
  bloco de contato ja possui contrato, service, adapter e diagnostico de
  ausencia de consumidor interno real;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleContactScopeClosureDiagnostic`, deixando explicito que a evolucao
  seguinte deve sair para outra familia backend sem reabrir esse bloco agora;
- a fase preserva o escopo novo-only: nenhuma mudanca em `consultarCadastro`,
  nenhuma rota nova, nenhuma alteracao em BFF/frontend e nenhuma alteracao no
  `school-management-service`.

Contagem da macrofase Fase 94: 0 subfases restantes estimadas. O bloco de
contato fica formalmente fechado como fronteira preparada no codigo novo.

Proxima fase pratica sugerida:

- iniciar uma nova familia backend exclusivamente dentro do codigo novo do
  `people-service`, sem reabrir `endereco`, `pessoa_documento`, `funcionario`
  ou `contato`; ou
- se surgir um consumidor real em outro servico novo, reabrir somente a
  fronteira necessaria para integracao controlada.

### Fase 95 - Diagnostico da familia de professor no `people-service`

Objetivo: abrir a proxima familia backend exclusivamente no codigo novo do
`people-service`, escolhendo `professor` como o menor recorte restante com
valor arquitetural sem reabrir blocos ja fechados e sem tocar o
`school-management-service`.

Entregue na primeira subfase da Fase 95:

- foi criado o planner `PeopleProfessorScopeDiagnosticPlanner`, formalizando
  que o primeiro passo seguro para `professor` deve ser um contrato interno
  minimo read-only de resumo por escola;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleProfessorScopeDiagnostic`, deixando explicito que `professor` ainda
  depende de `funcionario`, `pessoa` e relacoes academicas hoje mantidas no
  monolito, sem autorizar rota externa, persistencia local ou write cutover
  nesta etapa;
- a fase preserva o foco novo-only: nenhuma rota nova, nenhuma migration,
  nenhuma mudanca em BFF/frontend e nenhuma alteracao no legado.

Entregue na segunda subfase da Fase 95:

- foram criados no `people-service` o DTO
  `PessoaProfessorInternalSummaryResponse`, a porta
  `PeopleProfessorInternalSummaryPort`, o service
  `PeopleProfessorInternalSummaryService` e o planner
  `PeopleProfessorInternalSummaryContractPlanner`, materializando a fronteira
  interna minima de resumo de `professor` sem criar adapter, rota ou
  persistencia local;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleProfessorInternalSummaryContractDiagnostic`, deixando explicito que o
  contrato existe e que a proxima subfase, se seguir, deve decidir apenas a
  preparacao do adapter/local read, ainda sem ampliar para alocacao academica,
  write ou cutover;
- a fase continua novo-only e backend-only: nenhuma alteracao em
  `school-management-service`, nenhuma mudanca em BFF/frontend e nenhuma
  migration de `professor`.

Entregue na terceira subfase da Fase 95:

- foi criado o planner `PeopleProfessorInternalSummaryAdapterPreparationPlanner`
  para registrar explicitamente o menor proximo passo possivel da familia:
  diagnosticar o primeiro adapter/local read de `professor` sem cria-lo nesta
  fase e sem ativar qualquer persistencia propria;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleProfessorInternalSummaryAdapterPreparationDiagnostic`, deixando
  explicito que o contrato interno esta pronto, mas que schema, adapter real,
  local read e qualquer conexao externa continuam fora deste bloco;
- a familia de `professor` foi mantida estritamente em preparacao interna no
  codigo novo, sem rota, sem migration, sem BFF/frontend e sem alteracao no
  legado.

Contagem da macrofase Fase 95: 0 subfases restantes estimadas. O bloco inicial
de `professor` fica fechado como fronteira interna preparada, ainda sem
persistencia propria.

Proxima fase pratica sugerida:

- encerrar formalmente esta familia de `professor` como bloco inicial concluido
  e iniciar a proxima familia backend do `people-service` ainda nao tratada; ou
- se houver justificativa forte de continuidade em `professor`, abrir uma nova
  macrofase separada para schema/adapter local, sem misturar com este bloco
  inicial.

### Fase 96 - Diagnostico da familia de vinculos base de aluno e responsavel

Objetivo: abrir a proxima familia backend do `people-service` pelo recorte
minimo ainda sem macrofase propria, formalizando os vinculos base
`aluno -> pessoa`, `responsavel -> pessoa` e a dependencia de
`aluno_responsavel`, ainda sem rota, sem migration e sem alterar o legado.

Entregue na primeira subfase da Fase 96:

- foi criado o planner `PeopleStudentResponsibleLinkScopeDiagnosticPlanner`,
  registrando que os contratos locais de lookup de `aluno` e `responsavel` ja
  existem no codigo novo, mas ainda sem fechamento explicito como familia
  propria;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleStudentResponsibleLinkScopeDiagnostic`, deixando explicito que
  `parentesco`, `status_aluno` e qualquer write de `aluno`, `responsavel` ou
  `aluno_responsavel` continuam fora desta macrofase;
- a fase preserva o foco novo-only: nenhuma rota nova, nenhuma migration, sem
  BFF/frontend e sem qualquer alteracao no `school-management-service`.

Entregue na segunda subfase da Fase 96:

- foi criado o planner `PeopleStudentResponsibleLinkScopeClosurePlanner`,
  formalizando o fechamento dessa familia como bloco interno ja preparado no
  codigo novo, reutilizando os lookups e services existentes de `aluno` e
  `responsavel`;
- o actuator `peopleLocalPersistence` passou a expor
  `peopleStudentResponsibleLinkScopeClosureDiagnostic`, deixando explicito que
  a familia fica fechada neste estagio sem abrir `parentesco`,
  `status_aluno`, rota externa ou persistencia propria adicional;
- o escopo permaneceu estritamente backend-only e novo-only: nenhuma migration,
  nenhuma alteracao em BFF/frontend e nenhuma mudanca no legado.

Contagem da macrofase Fase 96: 0 subfases restantes estimadas. O bloco de
vinculos base `aluno/responsavel` fica formalmente fechado como fronteira
preparada no codigo novo.

Proxima fase pratica sugerida:

- iniciar a proxima familia backend ainda nao tratada no `people-service`,
  preferencialmente pelos catalogos/vinculos de `parentesco` ou `status_aluno`,
  porque ficaram explicitamente fora deste bloco minimo; ou
- se nao houver necessidade imediata nesses catalogos, abrir outra familia nova
  ainda nao coberta sem reabrir `aluno/responsavel`.

### Fase 97 - Read model local dos catalogos `status_aluno` e `parentesco`

Objetivo: abrir a proxima familia backend do `people-service` pelo menor
recorte concreto restante de `aluno/responsavel`, adicionando os catalogos
`status_aluno` e `parentesco` ao read model local e ao contrato interno ja
existente de catalogos, sem criar rota nova, sem BFF e sem tocar o
`school-management-service`.

Entregue na primeira subfase da Fase 97:

- foi criada a migration opt-in
  `V7__create_people_student_responsible_catalog_read_model.sql`, adicionando
  as tabelas locais `status_aluno` e `parentesco` com reconciliacao por
  `id_status_aluno` e `id_parentesco`;
- o ciclo JDBC de sincronizacao do `people-service` passou a incluir esses dois
  catalogos no mesmo read model de catalogos ja existente, preservando a
  estrategia atual de backfill/reconciliacao sem rota externa;
- o contrato interno `PessoaCatalogoPort` e o adapter local
  `JdbcPessoaCatalogoAdapter` passaram a expor `listarStatusAluno()` e
  `listarParentescos()`, deixando a nova familia preparada no codigo novo para
  consumo controlado futuro;
- health, estado de migration, coordenacao de sync e testes do modulo foram
  ajustados para refletir as duas novas tabelas, sem alterar qualquer contrato
  HTTP atual e sem mover autoridade funcional do monolito.

Contagem da macrofase Fase 97: 1 subfase restante estimada.

Proxima fase pratica sugerida:

- fechar formalmente esta familia de catalogos base de `aluno/responsavel` se
  nao houver consumidor interno imediato; ou
- se houver necessidade concreta no proprio `people-service`, conectar um
  primeiro consumidor interno controlado desses catalogos sem abrir rota
  externa.

### Fase 98 - Schema e adapter local de resumo de `professor`

Objetivo: retomar a familia de `professor` pelo primeiro passo concreto ainda
faltante no codigo novo do `people-service`, adicionando schema local,
backfill/reconciliacao e adapter JDBC do resumo de professor, sem criar rota
externa e sem tocar o `school-management-service`.

Entregue na primeira subfase da Fase 98:

- foi criada a migration opt-in `V8__create_people_professor_read_model.sql`,
  adicionando a tabela local `people_professor_read_model`;
- o ciclo JDBC de sincronizacao do `people-service` passou a incluir o resumo
  de `professor`, reconciliado por `id_professor`, com join de origem entre
  `professor`, `pessoa` e `funcionario`;
- foi criado o adapter local `JdbcPessoaProfessorResumoAdapter`, ligado ao
  contrato `PessoaProfessorResumoPort`, permitindo
  `buscarProfessorPorId()` e `listarProfessoresPorEscola()` no schema local;
- migration state, sync coordinator, health e testes do modulo foram estendidos
  para refletir a nova tabela, mantendo a entrega backend-only, sem BFF, sem
  rota nova e sem alteracao do legado.

Contagem da macrofase Fase 98: 1 subfase restante estimada.

Proxima fase pratica sugerida:

- fechar formalmente esta familia de `professor` como preparada com schema e
  adapter local; ou
- se houver consumidor interno imediato, conectar esse resumo local ao primeiro
  fluxo controlado dentro do proprio `people-service`, ainda sem rota externa.

Entregue na segunda subfase da Fase 98:

- `PessoaProfessorResumoService` passou a aplicar o mesmo guard interno ja
  usado nas leituras locais de `endereco`, `documento` e `funcionario`,
  consultando `PeopleReadSourcePolicy` antes de acessar
  `PessoaProfessorResumoPort`;
- `PeopleReadSourcePolicy` passou a reconhecer explicitamente a operacao
  `professorResumo`, liberando `people_professor_read_model` apenas quando o
  estado local estiver verde e mantendo fallback para `monolith_internal_rh`
  quando a leitura local nao estiver elegivel;
- testes de policy e service foram estendidos para cobrir o bloqueio do guard,
  a liberacao da leitura local e as metricas de roteamento/leitura do resumo de
  `professor`, sem criar rota nova e sem tocar o legado.

Contagem da macrofase Fase 98: 0 subfases restantes estimadas. O bloco de
`professor` fica fechado com schema local, sync, adapter e primeiro consumidor
interno protegido pelo mesmo criterio de elegibilidade das demais leituras
locais do `people-service`.

Proxima fase pratica sugerida:

- iniciar a proxima macrofase backend pelo menor recorte ainda faltante da
  familia de `professor` ou por outro agregado remanescente do `people-service`,
  sempre preservando o criterio de primeiro contrato interno, depois adapter
  local e so entao consumidor interno.

### Fase 99 - Elegibilidade interna do vinculo base de `responsavel`

Objetivo: fechar o menor recorte remanescente de leitura interna local ja
preparada no `people-service`, conectando o lookup de vinculo base de
`responsavel` ao mesmo guard operacional das demais leituras locais, sem criar
schema novo, sem rota externa e sem tocar no `school-management-service`.

Entregue na primeira subfase da Fase 99:

- `ResponsavelPessoaService` passou a consultar `PeopleReadSourcePolicy` antes
  de acessar `ResponsavelPessoaPort`, bloqueando a leitura local quando a base
  local nao estiver elegivel e preservando fallback quando necessario;
- `PeopleReadSourcePolicy` passou a reconhecer explicitamente a operacao
  `responsavelVinculo`, tratando esse lookup como leitura local do bloco
  `people_read_model_student_responsible` apenas quando o estado de
  backfill/reconciliacao estiver verde;
- health e testes do modulo foram estendidos para expor a decisao interna de
  `responsavel`, suas metricas de roteamento e o comportamento do service sob
  bloqueio, adapter ausente e caminho liberado.

Contagem da macrofase Fase 99: 0 subfases restantes estimadas. O bloco do
vinculo base de `responsavel` fica fechado com guard interno, observabilidade e
consumo alinhado ao padrao atual do `people-service`.

Proxima fase pratica sugerida:

- iniciar o proximo recorte minimo ainda sem guard interno simetrico, como o
  vinculo base de `aluno`, ou escolher outro agregado remanescente do
  `people-service` que ja tenha adapter local preparado e ainda nao esteja
  conectado ao mesmo criterio de elegibilidade.

### Fase 100 - Elegibilidade interna do vinculo base de `aluno`

Objetivo: fechar a simetria do bloco base `aluno/responsavel` no
`people-service`, conectando o lookup de vinculo base de `aluno` ao mesmo guard
interno das demais leituras locais, sem criar schema novo, sem rota externa e
sem tocar no `school-management-service`.

Entregue na primeira subfase da Fase 100:

- `AlunoPessoaService` passou a consultar `PeopleReadSourcePolicy` antes de
  acessar `AlunoPessoaPort`, bloqueando o caminho local quando o estado do read
  model nao estiver elegivel e preservando fallback quando necessario;
- `PeopleReadSourcePolicy` passou a reconhecer explicitamente a operacao
  `alunoVinculo`, tratando esse lookup como leitura local do bloco
  `people_read_model_student_responsible` apenas quando o backfill e a
  reconciliacao estiverem verdes;
- health e testes do modulo foram estendidos para expor a decisao interna de
  `aluno`, suas metricas de roteamento e o comportamento do service sob
  bloqueio, adapter ausente e caminho liberado.

Contagem da macrofase Fase 100: 0 subfases restantes estimadas. O bloco base
de `aluno` fica alinhado ao mesmo criterio de elegibilidade e observabilidade
ja aplicado a `responsavel`, `funcionario`, `professor`, `documento` e
`endereco`.

Proxima fase pratica sugerida:

- escolher o proximo agregado do `people-service` que ja tenha adapter local ou
  read model preparado, mas ainda nao esteja fechado com o mesmo padrao de
  guard interno e observabilidade.

### Fase 101 - Reabertura controlada do bloco de `contato`

Objetivo: reaproveitar a fronteira de `contato` (`email` e `telefone`) ja
preparada no `people-service`, agora fechando esse bloco com o mesmo criterio
de elegibilidade interna, metricas e health das demais leituras locais, sem
criar rota externa, sem BFF/frontend e sem tocar o
`school-management-service`.

Entregue na primeira subfase da Fase 101:

- `PessoaContatoService` passou a consultar `PeopleReadSourcePolicy` antes de
  acessar `PessoaContatoPort`, bloqueando o caminho local quando a base local
  nao estiver elegivel e preservando fallback quando necessario;
- `PeopleReadSourcePolicy` passou a reconhecer explicitamente a operacao
  `contato`, tratando esse lookup como leitura local sobre o read model de
  `pessoa` apenas quando o backfill e a reconciliacao estiverem verdes;
- health e testes do modulo foram estendidos para expor a decisao interna de
  `contato`, suas metricas de roteamento e o comportamento do service sob
  bloqueio, adapter ausente e caminho liberado.

Contagem da macrofase Fase 101: 0 subfases restantes estimadas. O bloco de
`contato`, antes apenas preparado, fica reaberto e fechado com o mesmo padrao
operacional das leituras locais atuais do `people-service`.

Proxima fase pratica sugerida:

- escolher a proxima familia ou agregado do `people-service` que ainda nao
  tenha sido fechado com guard interno, observabilidade e consumo controlado,
  sem reabrir o legado.

### Fase 102 - Fronteira interna dos catalogos base de `aluno/responsavel`

Objetivo: fechar no `people-service` a camada de aplicacao dos catalogos
`status_aluno` e `parentesco`, que ja possuem read model e adapter local, sem
criar rota externa nova, sem BFF/frontend e sem depender de contrato interno
inexistente no `school-management-service`.

Entregue na primeira subfase da Fase 102:

- foi criado `PessoaAlunoResponsavelCatalogoService`, reaproveitando
  `PessoaCatalogoPort` para expor internamente `listarStatusAluno()` e
  `listarParentescos()` com guard de elegibilidade, fallback local e metricas;
- `PeopleReadSourcePolicy` passou a reconhecer explicitamente as operacoes
  `listarStatusAluno` e `listarParentescos` como catalogos locais do
  `people_read_model_catalog`, alinhando essas decisoes ao mesmo criterio de
  backfill/reconciliacao verde dos demais catalogos;
- testes do modulo foram estendidos para cobrir bloqueio do guard, adapter
  ausente, falha local e sucesso, mantendo o bloco como fronteira interna do
  codigo novo, ainda sem contrato HTTP novo.

Contagem da macrofase Fase 102: 0 subfases restantes estimadas. O bloco de
catalogos base de `aluno/responsavel` fica fechado na camada de aplicacao do
`people-service`, sem inflar interface externa e sem tocar o legado.

Proxima fase pratica sugerida:

- escolher o proximo agregado que ainda precise de fechamento na camada de
  aplicacao ou observabilidade, preservando o criterio de recortes pequenos,
  portas especificas e nenhum retorno ao legado.

### Fase futura - Desativacao do monolito

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

Criar no `school-management-service` apenas o contrato HTTP interno minimo de
escrita/cleanup de endereco, delegando para as autoridades atuais
(`PessoaFoundationService`/`PessoaEnderecoPort`), com idempotencia, contexto
interno e testes. Manter o `people-service` apenas em modo shadow/diagnostico
nesta subfase, sem implementar cliente de escrita nem alterar rotas externas.

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

### Fase 103 - Contrato interno visivel dos catalogos locais de `aluno/responsavel`

Entregue nesta fase:

- o contrato interno de consulta de `people-service` passou a expor tambem
  `listarStatusAluno()` e `listarParentescos()` no mesmo `PessoaQueryUseCase`,
  sem alterar BFF, frontend ou qualquer rota externa;
- `PessoaInternalQueryController` passou a publicar
  `GET /internal/v1/pessoas/catalogos/status-aluno` e
  `GET /internal/v1/pessoas/catalogos/parentescos`, usando apenas o codigo novo;
- `PessoaQueryService` conectou esses endpoints ao
  `PessoaAlunoResponsavelCatalogoService`, preservando a separacao entre
  contrato interno, politica de elegibilidade e adapter local;
- os testes do `people-service` passaram a validar tanto o servico de consulta
  quanto as duas novas rotas internas usando o read model local.

Contagem da macrofase Fase 103: 0 subfases restantes estimadas. Este recorte
pratico ficou fechado.

### Fase 104 - Contrato interno visivel de leitura de endereco

Entregue nesta fase:

- o `people-service` passou a expor no contrato interno as leituras locais de
  endereco ja prontas no read model;
- `PessoaQueryUseCase` e `PessoaQueryService` passaram a cobrir
  `buscarEnderecoPrincipalPorPessoa` e `listarEnderecosPorPessoa`;
- `PessoaInternalQueryController` passou a publicar
  `GET /internal/v1/pessoas/{id}/endereco-principal` e
  `GET /internal/v1/pessoas/{id}/enderecos`, ainda sem BFF e sem frontend.

Contagem da macrofase Fase 104: 0 subfases restantes estimadas. O bloco interno
de leitura de endereco ficou fechado.

### Fase 105 - Contrato interno visivel de leitura de contato

Entregue nesta fase:

- o `people-service` passou a expor no contrato interno a leitura local de
  contato por pessoa;
- `PessoaQueryUseCase`, `PessoaQueryService` e
  `PessoaInternalQueryController` passaram a cobrir
  `GET /internal/v1/pessoas/{id}/contato`;
- a fase permaneceu restrita ao codigo novo, sem BFF, sem frontend e sem tocar
  no legado.

Contagem da macrofase Fase 105: 0 subfases restantes estimadas. O bloco interno
de leitura de contato ficou fechado.

### Fase 106 - Contrato interno visivel de documento metadata

Entregue nesta fase:

- o `people-service` passou a expor no contrato interno a leitura de metadata
  de documentos por pessoa e por `documentoId`;
- `PessoaQueryUseCase`, `PessoaQueryService` e
  `PessoaInternalQueryController` passaram a cobrir
  `GET /internal/v1/pessoas/{id}/documentos` e
  `GET /internal/v1/documentos/{documentoId}`;
- a fase permaneceu restrita ao codigo novo, sem BFF, sem frontend e sem tocar
  no legado.

Contagem da macrofase Fase 106: 0 subfases restantes estimadas. O bloco interno
de documento metadata ficou fechado.

### Fase 107 - Contrato interno visivel de funcionario resumo

Entregue nesta fase:

- o `people-service` passou a expor no contrato interno o bloco de resumo de
  funcionario por id e a listagem de ativos da escola;
- `PessoaQueryUseCase`, `PessoaQueryService` e
  `PessoaInternalQueryController` passaram a cobrir
  `GET /internal/v1/funcionarios/{funcionarioId}` e
  `GET /internal/v1/funcionarios`;
- a fase permaneceu restrita ao codigo novo, sem BFF, sem frontend e sem tocar
  no legado.

Contagem da macrofase Fase 107: 0 subfases restantes estimadas. O bloco interno
de funcionario resumo ficou fechado.

### Fase 108 - Contrato interno visivel de professor resumo

Entregue nesta fase:

- o `people-service` passou a expor no contrato interno o bloco de resumo de
  professor por id e a listagem por escola;
- `PessoaQueryUseCase`, `PessoaQueryService` e
  `PessoaInternalQueryController` passaram a cobrir
  `GET /internal/v1/professores/{professorId}` e
  `GET /internal/v1/professores`;
- a fase permaneceu restrita ao codigo novo, sem BFF, sem frontend e sem tocar
  no legado.

Contagem da macrofase Fase 108: 0 subfases restantes estimadas. O bloco interno
de professor resumo ficou fechado.

### Fase 109 - Primeiro consumo oficial de professor via `school-management-bff`

Entregue nesta fase:

- o `school-management-bff` passou a publicar `GET /api/professores` e
  `GET /api/professores/{professorId}` consumindo oficialmente o
  `people-service`, sem alterar frontend e sem tocar no legado;
- foi criado um fluxo enxuto de proxy para professor, reaproveitando a
  resolucao de contexto autenticado no monolito e encaminhando a chamada para
  `GET /internal/v1/professores` e
  `GET /internal/v1/professores/{professorId}` no `people-service`;
- a entrega ficou restrita ao codigo novo do BFF e do `people-service`, sem
  introduzir novo mecanismo operacional paralelo para esse recorte.

Contagem regressiva do `people-service`: 2 fases reais restantes estimadas
para fechar o primeiro bloco oficial minimo.

### Fase 110 - Segundo consumo oficial minimo via `school-management-bff`

Entregue nesta fase:

- o `school-management-bff` passou a publicar `GET /api/funcionarios` e
  `GET /api/funcionarios/{funcionarioId}` consumindo oficialmente o
  `people-service`, sem alterar frontend e sem tocar no legado;
- foi aplicado o mesmo fluxo enxuto ja validado para professor, reaproveitando
  a resolucao de contexto autenticado no monolito e encaminhando a chamada para
  `GET /internal/v1/funcionarios` e
  `GET /internal/v1/funcionarios/{funcionarioId}` no `people-service`;
- a entrega permaneceu restrita ao codigo novo do BFF e do `people-service`,
  sem ampliar escopo para outros recortes.

Contagem regressiva do `people-service`: 1 fase real restante estimada para
fechar o primeiro bloco oficial minimo.

### Fase 111 - Fechamento formal do primeiro bloco oficial minimo de `people-service`

Entregue nesta fase:

- foi confirmada a cobertura oficial completa do recorte minimo combinado no
  `school-management-bff`, com rotas externas de listagem e detalhe por id para
  `professores` e `funcionarios`;
- a validacao automatizada foi reforcada para garantir explicitamente os
  caminhos `GET /api/professores/{professorId}` e
  `GET /api/funcionarios/{funcionarioId}`, alem das listagens ja fechadas;
- com isso, o primeiro bloco oficial minimo do `people-service` ficou
  formalmente encerrado sem ampliar escopo para frontend, endereco, contato ou
  outras familias.

Contagem regressiva do `people-service`: 0 fases reais restantes neste primeiro
bloco oficial minimo. Macrofase encerrada.

### Fase 112 - Segundo bloco oficial de leitura de `people-service` via BFF

Entregue nesta fase:

- o `school-management-bff` passou a publicar oficialmente
  `GET /api/consulta-cadastral`,
  `GET /api/pessoas/catalogos/tipos-pessoa` e
  `GET /api/pessoas/catalogos/tipos-endereco` consumindo o `people-service`;
- a chamada externa preservou os contratos ja existentes do monolito e passou a
  resolver contexto autenticado no monolito antes de encaminhar para
  `GET /internal/v1/pessoas/consulta-cadastral`,
  `GET /internal/v1/pessoas/catalogos/tipos-pessoa` e
  `GET /internal/v1/pessoas/catalogos/tipos-endereco`;
- a protecao de bearer no BFF foi ampliada para essas rotas oficiais, mantendo
  o mesmo padrao aplicado aos demais consumos novos de people.

Contagem regressiva do `people-service`: o segundo bloco oficial de leitura foi
iniciado e ficou parcialmente fechado nesta fase.

### Fase 113 - Fechamento formal do contrato de leitura atual de `people-service` via BFF

Entregue nesta fase:

- o `school-management-bff` passou a publicar oficialmente tambem
  `GET /api/pessoas/catalogos/status-aluno`,
  `GET /api/pessoas/catalogos/parentescos`,
  `GET /api/pessoas/{pessoaId}`,
  `GET /api/pessoas/{pessoaId}/endereco-principal`,
  `GET /api/pessoas/{pessoaId}/enderecos`,
  `GET /api/pessoas/{pessoaId}/contato`,
  `GET /api/pessoas/{pessoaId}/documentos` e
  `GET /api/documentos/{documentoId}`, todos consumindo o `people-service`;
- com isso, todo endpoint de leitura atualmente implementado no
  `PessoaInternalQueryController` do `people-service` ficou exposto de forma
  oficial pelo BFF, sem tocar frontend e sem alterar o legado;
- a protecao de bearer no BFF foi ampliada para todas essas rotas e a
  validacao automatizada foi reforcada com testes de controller, proxy e
  filtro de autenticacao.

Contagem regressiva do `people-service`: 0 fases restantes neste recorte atual
de leitura oficial. O contrato de leitura atualmente implementado no
`people-service` ficou fechado.

### Fase 114 - Abertura da nova macrofase de `responsaveis` pelo vinculo com `aluno`

Entregue nesta fase:

- o `people-service` permanece formalmente encerrado no recorte anterior; esta
  fase nao reabre aquela macrofase e passa a ser tratada como abertura de uma
  nova macrofase backend para a futura familia/servico de `responsaveis`;
- o menor recorte seguro para abrir essa nova macrofase foi formalizado como
  `GET /api/alunos/{alunoId}/responsaveis`, porque ele reutiliza o vinculo
  oficial ja existente com `aluno`, preserva o contrato externo atual e evita
  abrir nesta etapa a listagem ampla `GET /api/responsaveis`;
- o `people-service` recebeu o DTO
  `PessoaResponsavelVinculadoResponse`, a migration
  `V9__extend_people_student_responsible_link_read_model.sql`, a extensao do
  read model local de `aluno_responsavel` com `id_parentesco`,
  `responsavel_financeiro`, `responsavel_pedagogico` e
  `autorizado_retirar`, alem do adapter JDBC local e do fallback controlado
  para o monolito;
- como passo transitorio de implementacao no codigo novo atual, o contrato
  interno hospedado hoje no `people-service` passou a expor
  `GET /internal/v1/alunos/{alunoId}/responsaveis`, e o
  `school-management-bff` passou a oficializar a mesma leitura em
  `GET /api/alunos/{alunoId}/responsaveis`;
- a macrofase ficou delimitada de forma exata em 2 fases totais:
  a Fase 114, agora concluida, para o vinculo `aluno -> responsaveis`, e uma
  unica fase restante para decidir e, se aprovado, oficializar o detalhe
  minimo de `responsavel` por id; `GET /api/responsaveis` continua fora desse
  fechamento por depender de contrato de listagem mais amplo e semantica de
  filtro ainda nao migrada.

Contagem regressiva da nova macrofase de `responsaveis`: 1 fase restante
exata. O `people-service` continua fechado; o que resta e apenas concluir ou
encerrar esta nova macrofase independente.

### Fase 115 - Abertura fisica do `enrollment-document-service` por `transferencia`

Entregue nesta fase:

- o `people-service` permanece encerrado; a nova frente backend passa a ser o
  `enrollment-document-service`, ja previsto no roadmap como servico de
  `matriculas, documentos e transferencia`;
- a macrofase foi delimitada em 3 fases totais, escolhendo nesta primeira o
  menor recorte seguro para abertura fisica do runtime novo:
  `transferencia_aluno` e `escolas-origem`, sem escrita migrada oficial, sem
  BFF e sem frontend;
- foi criado o modulo fisico `enrollment-document-service` no monorepo, com
  estrutura em camadas, `application.yml`, validacao de headers internos,
  tratamento de erros proprio e cliente HTTP para consumir o monolito;
- o novo runtime passou a expor o contrato interno
  `POST/GET /internal/v1/transferencias`,
  `GET /internal/v1/transferencias/alunos/{alunoId}` e
  `POST/GET /internal/v1/escolas-origem`, preservando o monolito como unica
  autoridade funcional nesta etapa;
- o `school-management-service` passou a expor os adaptadores internos
  `POST/GET /internal/transferencias`,
  `GET /internal/transferencias/alunos/{alunoId}` e
  `POST/GET /internal/escolas-origem`, reaproveitando o
  `TransferenciaAlunoService` existente e separando o contrato backend/backend
  sem refatoracao ampla;
- os primeiros candidatos para oficializacao futura no
  `school-management-bff` ficam identificados como as leituras
  `GET /api/transferencias/{id}` e `GET /api/escolas-origem/{id}`, por serem o
  menor passo read-only com resposta direta e baixo risco de composicao;
- a validacao ficou restrita aos modulos tocados, cobrindo o runtime novo e o
  adaptador interno do monolito com testes automatizados.

Contagem regressiva do `enrollment-document-service`: 2 fases restantes para
fechar o primeiro bloco oficial do servico.

### Fase 116 - Primeiro bloco oficial de leitura do `enrollment-document-service` via BFF

Entregue nesta fase:

- o `school-management-bff` passou a publicar oficialmente
  `GET /api/transferencias/{id}` e `GET /api/escolas-origem/{id}` consumindo o
  `enrollment-document-service`, preservando os contratos externos atuais e sem
  tocar frontend;
- o BFF recebeu cliente HTTP, portas, use cases, proxy services e controllers
  dedicados para esse primeiro bloco minimo, reaproveitando a mesma resolucao
  de contexto autenticado ja usada nas leituras do `people-service`;
- o `enrollment-document-service` permaneceu como consumidor do monolito por
  contrato interno, sem migrar escrita e sem alterar a autoridade funcional do
  legado nesta etapa;
- a validacao ficou restrita ao modulo tocado, com testes de controller e de
  integracao do BFF cobrindo a propagacao de bearer, correlation ID e headers
  internos para o novo servico.

Contagem regressiva do `enrollment-document-service`: 1 fase restante para
fechar este primeiro bloco oficial minimo.

### Fase 117 - Fechamento formal do primeiro bloco oficial minimo de `enrollment-document-service`

Entregue nesta fase:

- o `school-management-bff` passou a publicar oficialmente tambem
  `GET /api/escolas-origem` e
  `GET /api/transferencias/alunos/{alunoId}` consumindo o
  `enrollment-document-service`;
- com isso, o primeiro bloco oficial minimo de leitura atualmente suportado no
  `enrollment-document-service` ficou exposto de forma oficial pelo BFF, sem
  tocar frontend e sem reabrir escrita;
- a validacao do modulo tocado foi ampliada com testes de controller e
  integracao cobrindo essas duas leituras restantes, preservando propagacao de
  bearer, correlation ID e headers internos.

Contagem regressiva do `enrollment-document-service`: 0 fases restantes neste
primeiro bloco oficial minimo.

### Fase 118 - Abertura do bloco oficial minimo de `documento` por aluno no `enrollment-document-service`

Entregue nesta fase:

- foi aberta a proxima macrofase do `enrollment-document-service` pelo menor
  recorte seguro fora de `transferencia`: a leitura oficial
  `GET /api/documentos-alunos/alunos/{alunoId}`;
- o `school-management-service` passou a expor o contrato interno
  `GET /internal/documentos-alunos/alunos/{alunoId}` reaproveitando o
  `DocumentoAlunoService`, sem refatoracao ampla e sem mover escrita;
- o `enrollment-document-service` passou a consumir esse contrato e a expor
  `GET /internal/v1/documentos-alunos/alunos/{alunoId}` como nova fronteira
  backend/backend do recorte;
- o `school-management-bff` passou a oficializar
  `GET /api/documentos-alunos/alunos/{alunoId}` consumindo o
  `enrollment-document-service`, com propagacao de bearer, correlation ID e
  contexto interno obrigatorio;
- esta nova macrofase fica delimitada em 2 fases totais: a Fase 118, agora
  concluida, para a listagem de documentos por aluno, e 1 unica fase restante
  para decidir e, se aprovado, oficializar o detalhe minimo
  `GET /api/documentos-alunos/{id}`.

Contagem regressiva do bloco `documento` por aluno no
`enrollment-document-service`: 1 fase restante.

### Fase 119 - Fechamento formal do bloco oficial minimo de `documento` por aluno no `enrollment-document-service`

Entregue nesta fase:

- o `school-management-bff` passou a publicar oficialmente tambem
  `GET /api/documentos-alunos/{id}` consumindo o
  `enrollment-document-service`;
- o `school-management-service` passou a expor o detalhe interno
  `GET /internal/documentos-alunos/{id}` reaproveitando o
  `DocumentoAlunoService`, sem alterar upload, escrita ou exclusao;
- o `enrollment-document-service` passou a consumir esse detalhe interno e a
  expor `GET /internal/v1/documentos-alunos/{id}` no mesmo recorte
  backend/backend de documento por aluno;
- com isso, o bloco oficial minimo de `documento` por aluno no
  `enrollment-document-service` ficou fechado no BFF sem tocar frontend.

Contagem regressiva do bloco `documento` por aluno no
`enrollment-document-service`: 0 fases restantes.

### Fase 120 - Leitura oficial minima de `matricula` com `etapas` no `enrollment-document-service`

Entregue nesta fase:

- foi aberta a proxima frente funcional do `enrollment-document-service` em
  `matricula`, usando o menor recorte read-only ja operacional no legado:
  `GET /api/matriculas` com seus filtros atuais;
- o `school-management-service` passou a expor o contrato interno
  `GET /internal/matriculas`, com DTO interno proprio para `matricula` e
  `etapas`, reaproveitando `ConsultarMatriculasUseCase` sem alterar o
  controller publico nem abrir escrita;
- o `enrollment-document-service` passou a consumir esse contrato do monolito e
  a publicar `GET /internal/v1/matriculas`, preservando bearer, correlation ID
  e contexto interno obrigatorio;
- o `school-management-bff` passou a oficializar `GET /api/matriculas`
  consumindo o `enrollment-document-service`, mantendo o mesmo formato externo
  de filtros (`alunoId`, `turmaId`, `periodoLetivoId`, `status`) e o payload de
  `matricula` com `etapas`;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `enrollment-document-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `enrollment-document-service`: 2
fases restantes no escopo atual, ficando como proximas frentes `documentos
administrativos` e depois `escrita/storage/cutover`.

### Fase 121 - Leitura oficial minima de `documentos administrativos` no `enrollment-document-service`

Entregue nesta fase:

- foi oficializado no `school-management-bff` o recorte minimo restante de
  `documentos administrativos` sem colisao com o bloco de `pessoa_documento`:
  `GET /api/documentos` por `entidadeTipo` e `entidadeId`;
- o `school-management-service` passou a expor `GET /internal/documentos`
  reaproveitando `ListarDocumentosPorEntidadeUseCase`, com DTO interno proprio e
  sem alterar upload, exclusao, storage ou o detalhe publico
  `/api/documentos/{id}` hoje ainda associado ao contrato de `people-service`;
- o `enrollment-document-service` passou a consumir esse contrato interno do
  monolito e a publicar `GET /internal/v1/documentos`, mantendo o runtime novo
  como fronteira read-only para o bloco generico de documentos;
- o `school-management-bff` passou a publicar `GET /api/documentos` consumindo o
  `enrollment-document-service`, preservando bearer, correlation ID, contexto
  interno obrigatorio e convivendo sem conflito com `GET /api/documentos/{id}`
  ainda servido pelo `people-service`;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `enrollment-document-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `enrollment-document-service`: 1
fase restante no escopo atual, dedicada a `escrita/storage/cutover`.

### Fase 122 - Fechamento operacional minimo de escrita do `enrollment-document-service`

Entregue nesta fase:

- foi fechado o recorte minimo de escrita oficial ja pronto no
  `enrollment-document-service`, ligando no `school-management-bff` os writes
  `POST /api/escolas-origem` e `POST /api/transferencias`;
- o BFF passou a resolver contexto autenticado no monolito e a encaminhar esses
  writes para `POST /internal/v1/escolas-origem` e
  `POST /internal/v1/transferencias` do `enrollment-document-service`,
  preservando bearer, correlation ID e headers internos obrigatorios, sem
  fallback automatico cruzado apos tentar o servico novo;
- a protecao de bearer foi ampliada para essas duas rotas de escrita oficiais
  do bloco `transferencia`, consolidando o primeiro cutover operacional minimo
  de escrita do dominio novo no BFF;
- o bloco de documento/binario permaneceu explicitamente fora deste fechamento:
  `GET /api/documentos/{id}` segue pertencendo ao contrato de `people-service`,
  e uploads/exclusoes de documentos continuam no monolito ate existir um recorte
  isolado sem colisao de ownership entre `people-service` e
  `enrollment-document-service`;
- com isso, o `enrollment-document-service` fica encerrado no escopo atual
  planejado: leituras oficiais de `transferencia`, `matricula`, `documento por
  aluno`, leitura generica de `documentos administrativos` e escrita oficial
  minima de `escolas-origem`/`transferencias`.

Contagem regressiva funcional estimada do `enrollment-document-service`: 0
fases restantes no escopo atual planejado.

### Fase 123 - Abertura fisica do `pedagogical-service` com primeiro recorte oficial de `boletim`

Entregue nesta fase:

- o `enrollment-document-service` foi mantido como encerrado no escopo atual e
  a proxima macrofase backend passou a ser o `pedagogical-service`, ja previsto
  no roadmap como servico de execucao academica oficial;
- a macrofase foi delimitada de forma fechada em 10 fases totais, escolhendo
  nesta primeira o menor recorte integravel e read-only ja operacional no
  legado: `GET /api/matriculas/{matriculaId}/boletim`;
- o `school-management-service` passou a expor o contrato interno
  `GET /internal/boletins/matriculas/{matriculaId}`, reaproveitando o
  `BoletimService` existente e sem abrir escrita, fechamento, historico,
  avaliacao ou frequencia nesta etapa;
- foi criado o modulo fisico `pedagogical-service` no monorepo, com estrutura
  em camadas, validacao de headers internos, tratamento de erro proprio e
  cliente HTTP para consumir o monolito por esse contrato interno;
- o `pedagogical-service` passou a publicar
  `GET /internal/v1/matriculas/{matriculaId}/boletim`, separando o contrato
  backend/backend do primeiro recorte de leitura academica oficial de
  `boletim`;
- o `school-management-bff` passou a oficializar
  `GET /api/matriculas/{matriculaId}/boletim` consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o payload externo atual sem tocar frontend;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 9 fases
restantes no escopo atual planejado.

### Fase 124 - Fechamento do primeiro bloco minimo de `boletim` no `pedagogical-service`

Entregue nesta fase:

- o primeiro recorte oficial de `boletim` aberto na Fase 123 foi fechado pelo
  read-only restante de menor risco do mesmo contrato publico:
  `GET /api/matriculas/{matriculaId}/boletim/fechamentos`;
- o `school-management-service` passou a expor
  `GET /internal/boletins/matriculas/{matriculaId}/fechamentos`,
  reaproveitando `BoletimService.listarFechamentos(...)` sem abrir ainda
  escrita migrada ou historico;
- o `pedagogical-service` passou a publicar
  `GET /internal/v1/matriculas/{matriculaId}/boletim/fechamentos`,
  mantendo o monolito como autoridade funcional e separando o contrato
  backend/backend do bloco de fechamentos;
- o `school-management-bff` passou a oficializar
  `GET /api/matriculas/{matriculaId}/boletim/fechamentos` consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o payload externo atual;
- com isso, o primeiro bloco minimo oficial de leitura de `boletim` fica
  fechado no `pedagogical-service` sem tocar frontend e sem abrir ainda
  `POST /fechamento`;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 8 fases
restantes no escopo atual planejado.

### Fase 125 - Primeiro recorte read-only de `historico escolar` no `pedagogical-service`

Entregue nesta fase:

- a macrofase de `historico escolar` foi iniciada pelo menor recorte read-only
  explicitamente recomendado no roadmap: carregamento da nova tela em modo
  cadastro e edicao, sem tocar salvamento, importacao de PDF ou workflow mais
  pesado;
- o `school-management-service` passou a expor
  `GET /internal/historicos-escolares/novo` e
  `GET /internal/historicos-escolares/{id}/carregamento`, reaproveitando
  `HistoricoEscolarService.carregarNovo(...)` e
  `HistoricoEscolarService.carregarParaEdicao(...)`;
- o `pedagogical-service` passou a publicar
  `GET /internal/v1/historicos-escolares/novo` e
  `GET /internal/v1/historicos-escolares/{id}/carregamento`, mantendo o
  monolito como autoridade funcional e isolando o contrato backend/backend da
  nova experiencia de leitura de `historico escolar`;
- o `school-management-bff` passou a oficializar
  `GET /api/historicos-escolares/novo` e
  `GET /api/historicos-escolares/{id}/carregamento` consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o payload externo atual;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, query params,
  payload e propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 7 fases
restantes no escopo atual planejado.

### Fase 126 - Escrita minima de `historico escolar` no `pedagogical-service`

Entregue nesta fase:

- o bloco de `historico escolar` avancou para a primeira escrita minima segura,
  preservando o contrato externo atual de `POST /api/historicos-escolares` e
  `PUT /api/historicos-escolares/{id}` sem abrir importacao de PDF, diario de
  classe ou refatoracao ampla;
- o `school-management-service` passou a expor
  `POST /internal/historicos-escolares` e
  `PUT /internal/historicos-escolares/{id}`, reaproveitando
  `HistoricoEscolarService.criar(...)` e `atualizar(...)`;
- o `pedagogical-service` passou a publicar
  `POST /internal/v1/historicos-escolares` e
  `PUT /internal/v1/historicos-escolares/{id}` como proxy controlado do
  monolito, mantendo o legado como autoridade funcional;
- o `school-management-bff` passou a oficializar esses dois writes consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o payload externo atual;
- o carregamento operacional da fase anterior foi preservado intacto;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 6 fases
restantes no escopo atual planejado.

### Fase 127 - Bloco minimo de `aulas` read/write no `pedagogical-service`

Entregue nesta fase:

- o dominio de `aulas` foi aberto pelo menor recorte operacional seguro antes
  de `diario de classe`, `avaliacoes` e `frequencias`: criacao, listagem e
  detalhamento por id, preservando os contratos externos atuais
  `POST /api/aulas`, `GET /api/aulas` e `GET /api/aulas/{id}`;
- o `school-management-service` passou a expor
  `POST /internal/aulas`, `GET /internal/aulas` e `GET /internal/aulas/{id}`,
  reaproveitando `DiarioAulaService` sem migrar ainda frequencia de professor
  ou aluno;
- o `pedagogical-service` passou a publicar
  `POST /internal/v1/aulas`, `GET /internal/v1/aulas` e
  `GET /internal/v1/aulas/{id}`, mantendo o monolito como autoridade
  funcional e separando o contrato backend/backend do primeiro bloco de aula;
- o `school-management-bff` passou a oficializar essas tres rotas consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno,
  filtros de consulta e o payload externo atual;
- o bloco de frequencias permaneceu explicitamente fora desta fase para nao
  colidir com as fases posteriores de `diario de classe`, `avaliacoes`, `notas`
  e `frequencias`;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, query params,
  payload e propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 5 fases
restantes no escopo atual planejado.

### Fase 128 - Primeiro recorte read-only de `diario de classe` no `pedagogical-service`

Entregue nesta fase:

- o bloco de `diario de classe` foi iniciado pelo menor recorte oficial e
  read-only recomendado antes de escrita e checagens: o carregamento mensal
  consolidado de `GET /api/diarios-classe`;
- o `school-management-service` passou a expor
  `GET /internal/diarios-classe`, reaproveitando
  `DiarioClasseConsultaService.carregar(...)` sem abrir ainda
  `PUT /api/diarios-classe/{idDiarioClasse}` nem as rotas de checagem por
  coordenacao ou direcao;
- o `pedagogical-service` passou a publicar
  `GET /internal/v1/diarios-classe`, mantendo o monolito como autoridade
  funcional e preservando exatamente o payload consolidado do diario;
- o `school-management-bff` passou a oficializar
  `GET /api/diarios-classe` consumindo o `pedagogical-service`, preservando
  bearer, correlation ID, contexto interno e todos os query params atuais do
  contrato publico;
- o bloco de escrita do diario e as checagens ficaram explicitamente fora desta
  fase para nao colidir com as fases posteriores de governanca operacional;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, query params,
  payload e propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 4 fases
restantes no escopo atual planejado.

### Fase 129 - Escrita minima de `diario de classe` no `pedagogical-service`

Entregue nesta fase:

- o bloco de `diario de classe` avancou para a primeira escrita minima segura,
  preservando o contrato externo atual de
  `PUT /api/diarios-classe/{idDiarioClasse}` sem abrir ainda as checagens por
  coordenacao ou direcao;
- o `school-management-service` passou a expor
  `PUT /internal/diarios-classe/{idDiarioClasse}`, reaproveitando
  `DiarioClasseConsultaService.salvar(...)` sem alterar o workflow de checagem;
- o `pedagogical-service` passou a publicar
  `PUT /internal/v1/diarios-classe/{idDiarioClasse}` como proxy controlado do
  monolito, mantendo o legado como autoridade funcional e preservando o payload
  externo atual;
- o `school-management-bff` passou a oficializar esse write consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o corpo JSON atual do lancamento;
- as rotas de checagem
  `/api/diarios-classe/lancamentos/{idLancamento}/checagens/coordenacao` e
  `/api/diarios-classe/lancamentos/{idLancamento}/checagens/direcao`
  permaneceram explicitamente fora desta fase;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 3 fases
restantes no escopo atual planejado.

### Fase 130 - Bloco minimo de `avaliacoes` read/write no `pedagogical-service`

Entregue nesta fase:

- o dominio de `avaliacoes` foi aberto pelo menor recorte operacional seguro
  antes de `notas` e `frequencias`: criacao, listagem e detalhamento por id,
  preservando os contratos externos atuais `POST /api/avaliacoes`,
  `GET /api/avaliacoes` e `GET /api/avaliacoes/{id}`;
- o `school-management-service` passou a expor
  `POST /internal/avaliacoes`, `GET /internal/avaliacoes` e
  `GET /internal/avaliacoes/{id}`, reaproveitando `AvaliacaoService` sem
  migrar ainda o lancamento e a consulta de `notas` por avaliacao;
- o `pedagogical-service` passou a publicar
  `POST /internal/v1/avaliacoes`, `GET /internal/v1/avaliacoes` e
  `GET /internal/v1/avaliacoes/{id}`, mantendo o monolito como autoridade
  funcional e separando o contrato backend/backend do primeiro bloco de
  avaliacao;
- o `school-management-bff` passou a oficializar essas tres rotas consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno,
  filtros de consulta e o payload externo atual;
- as rotas `POST /api/avaliacoes/{id}/notas` e `GET /api/avaliacoes/{id}/notas`
  permaneceram explicitamente fora desta fase para compor a fase seguinte de
  `notas e frequencias`;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, query params,
  payload e propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 2 fases
restantes no escopo atual planejado.

### Fase 131 - Bloco minimo de `notas` no `pedagogical-service`

Entregue nesta fase:

- o dominio de `notas` foi aberto pelo menor recorte operacional seguro ainda
  acoplado a `avaliacoes`: lancamento por avaliacao, listagem por avaliacao e
  consulta por matricula, preservando os contratos externos atuais
  `POST /api/avaliacoes/{id}/notas`, `GET /api/avaliacoes/{id}/notas` e
  `GET /api/matriculas/{matriculaId}/notas`;
- o `school-management-service` passou a expor
  `POST /internal/avaliacoes/{id}/notas`,
  `GET /internal/avaliacoes/{id}/notas` e
  `GET /internal/matriculas/{matriculaId}/notas`, reaproveitando
  `AvaliacaoService` sem migrar ainda o bloco final de `frequencias`;
- o `pedagogical-service` passou a publicar
  `POST /internal/v1/avaliacoes/{id}/notas`,
  `GET /internal/v1/avaliacoes/{id}/notas` e
  `GET /internal/v1/matriculas/{matriculaId}/notas`, mantendo o monolito como
  autoridade funcional e separando o contrato backend/backend do primeiro bloco
  de notas;
- o `school-management-bff` passou a oficializar essas tres rotas consumindo o
  `pedagogical-service`, preservando bearer, correlation ID, contexto interno e
  o payload externo atual;
- as rotas finais de `frequencias`
  `POST /api/aulas/{id}/frequencia-professor`,
  `GET /api/aulas/{id}/frequencia-professor`,
  `POST /api/aulas/{id}/frequencias-alunos` e
  `GET /api/aulas/{id}/frequencias-alunos` permaneceram explicitamente para a
  fase final de encerramento funcional do micro-servico;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, query params,
  payload e propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 1 fase
restante no escopo atual planejado.

### Fase 132 - Fechamento funcional de `frequencias` no `pedagogical-service`

Entregue nesta fase:

- o bloco final de `frequencias` foi migrado no menor recorte operacional
  remanescente do dominio pedagogico, preservando os contratos externos atuais
  `POST /api/aulas/{id}/frequencia-professor`,
  `GET /api/aulas/{id}/frequencia-professor`,
  `POST /api/aulas/{id}/frequencias-alunos` e
  `GET /api/aulas/{id}/frequencias-alunos`;
- o `school-management-service` passou a expor
  `POST /internal/aulas/{id}/frequencia-professor`,
  `GET /internal/aulas/{id}/frequencia-professor`,
  `POST /internal/aulas/{id}/frequencias-alunos` e
  `GET /internal/aulas/{id}/frequencias-alunos`, reaproveitando
  `DiarioAulaService` como autoridade funcional do legado;
- o `pedagogical-service` passou a publicar
  `POST /internal/v1/aulas/{id}/frequencia-professor`,
  `GET /internal/v1/aulas/{id}/frequencia-professor`,
  `POST /internal/v1/aulas/{id}/frequencias-alunos` e
  `GET /internal/v1/aulas/{id}/frequencias-alunos`, completando a fronteira
  backend/backend do dominio pedagogico planejado;
- o `school-management-bff` passou a oficializar essas quatro rotas consumindo
  o `pedagogical-service`, preservando bearer, correlation ID, contexto interno
  e os payloads externos atuais;
- com isso, o `pedagogical-service` encerra o escopo funcional planejado nas
  10 fases fechadas para este micro-servico, sem abrir novas rotas fora do
  roadmap atual;
- a validacao ficou restrita aos modulos tocados, com testes automatizados no
  monolito, no `pedagogical-service` e no BFF cobrindo rota, payload e
  propagacao de headers.

Contagem regressiva funcional estimada do `pedagogical-service`: 0 fases
restantes no escopo atual planejado.

Definicao objetiva da proxima macrofase backend:

- com o `pedagogical-service` funcionalmente encerrado em 10 fases, a proxima
  frente sugerida pelo roadmap passa a ser a extracao fisica da familia
  `identity-access-service` e `institutional-tenant-service`, porque o backend
  atual ja fechou as fronteiras internas minimas de sessao, vinculo
  usuario-escola, tenant ativo e selecao de escola no fluxo autenticado;
- para manter contagem fechada e evitar abertura simultanea difusa, a definicao
  inicial passa a tratar `identity-access-service` como proximo micro-servico
  lider da macrofase, com `institutional-tenant-service` como servico irmao da
  mesma frente;
- quantidade fechada inicial desta macrofase: 6 fases totais;
- distribuicao proposta das 6 fases:
  1. abertura fisica minima do `identity-access-service` consumindo contratos
     internos ja estabilizados de autenticacao e sessao;
  2. abertura fisica minima do `institutional-tenant-service` consumindo os
     contratos internos ja estabilizados de escola ativa e vinculo
     usuario-escola;
  3. oficializacao controlada do bloco minimo de autenticacao/sessao via BFF ou
     consumidores internos, sem romper compatibilidade externa;
  4. oficializacao controlada do bloco minimo de tenant/vinculo de escola;
  5. endurecimento operacional do ciclo autenticado multiescola, mantendo
     fallback e rollback simples;
  6. fechamento formal do primeiro bloco oficial de identidade e tenant no
     codigo novo, com contagem zerada dessa macrofase.

### Fase 133 - Abertura fisica minima do `identity-access-service`

Entregue nesta fase:

- foi criado o modulo fisico `identity-access-service` no monorepo, com
  estrutura Spring Boot minima, `application.yml`, validacao de API interna,
  tratamento de erro proprio e cliente HTTP dedicado para o monolito;
- o menor recorte seguro escolhido para abrir o runtime novo foi o contrato
  interno ja estabilizado de sessao multiescola:
  `GET /internal/auth/escolas` e `POST /internal/auth/escola-ativa`;
- o `identity-access-service` passou a publicar
  `GET /internal/v1/auth/escolas` e
  `POST /internal/v1/auth/escola-ativa`, mantendo o monolito como autoridade
  funcional nesta primeira etapa;
- a fase permaneceu estritamente backend/backend: nao houve cutover de BFF,
  nao houve migracao de persistencia e ficaram explicitamente fora desta etapa
  `login`, `refresh`, `logout` e qualquer mudanca de contrato externo;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl identity-access-service test`, cobrindo rota interna, propagacao do
  bearer e protecao por token interno.

Contagem regressiva da macrofase inicial de identidade e tenant: 5 fases
restantes no escopo fechado atual.

### Fase 134 - Abertura fisica minima do `institutional-tenant-service`

Entregue nesta fase:

- foi criado o modulo fisico `institutional-tenant-service` no monorepo, com
  estrutura Spring Boot minima, `application.yml`, validacao de API interna,
  tratamento de erro proprio e cliente HTTP dedicado para o monolito;
- o menor recorte seguro escolhido para abrir o servico irmao foi a leitura
  autenticada das escolas disponiveis da sessao ja exposta no contrato interno
  `GET /internal/auth/escolas`;
- o `institutional-tenant-service` passou a publicar
  `GET /internal/v1/tenant/escolas` e
  `GET /internal/v1/tenant/ativa`, mantendo o monolito como autoridade
  funcional e derivando no codigo novo apenas a leitura do tenant ativo atual;
- a fase permaneceu estritamente backend/backend: nao houve cutover de BFF,
  nao houve migracao de persistencia, nao houve escrita migrada e a troca de
  escola ativa continuou fora deste recorte inicial do servico institucional;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl institutional-tenant-service test`, cobrindo rota interna,
  propagacao do bearer, derivacao do tenant ativo e protecao por token
  interno.

Contagem regressiva da macrofase inicial de identidade e tenant: 4 fases
restantes no escopo fechado atual.

### Fase 135 - Oficializacao inicial do bloco minimo de sessao via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar o primeiro bloco publico do
  ciclo autenticado multiescola consumindo o `identity-access-service`;
- foram publicadas no BFF as rotas `GET /api/auth/escolas` e
  `POST /api/auth/escola-ativa`, reutilizando os contratos internos ja
  estabilizados do `identity-access-service`;
- para preservar compatibilidade e reduzir risco, a fase nao migrou ainda
  `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout` nem
  `GET /api/auth/contexto-atual`; o BFF continua usando `contexto-atual` no
  monolito apenas para montar `X-Usuario-Id` e `X-Escola-Id` na chamada
  interna ao servico novo;
- foi criado cliente HTTP dedicado do BFF para o `identity-access-service`,
  com token interno proprio, e o filtro de bearer passou a proteger tambem as
  duas novas rotas oficiais de sessao;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff -Dtest=AuthSessionProxyIntegrationTest test`,
  cobrindo a resolucao de contexto, a propagacao de bearer/correlation ID e o
  proxy oficial para listagem e troca de escola ativa.

Contagem regressiva da macrofase inicial de identidade e tenant: 3 fases
restantes no escopo fechado atual.

### Fase 136 - Oficializacao inicial do tenant ativo via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a leitura publica minima do
  tenant ativo consumindo o `institutional-tenant-service`;
- foi publicada no BFF a rota `GET /api/auth/tenant/ativa`, reutilizando o
  contrato interno ja estabilizado `GET /internal/v1/tenant/ativa`;
- para preservar o menor recorte seguro, a fase nao abriu uma segunda rota
  publica para listar escolas por tenant, porque a lista de escolas da sessao
  ja ficou oficializada em `GET /api/auth/escolas` na fase anterior;
- o BFF continua usando `GET /api/auth/contexto-atual` no monolito apenas para
  montar `X-Usuario-Id` e `X-Escola-Id` na chamada interna ao servico novo;
- foi criado cliente HTTP dedicado do BFF para o
  `institutional-tenant-service`, com token interno proprio, e o filtro de
  bearer passou a proteger tambem a nova rota oficial de tenant ativo;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff -Dtest=InstitutionalTenantReadProxyIntegrationTest test`,
  cobrindo a resolucao de contexto, a propagacao de bearer/correlation ID e o
  proxy oficial para a leitura do tenant ativo.

Contagem regressiva da macrofase inicial de identidade e tenant: 2 fases
restantes no escopo fechado atual.

### Fase 137 - Endurecimento operacional do ciclo autenticado multiescola

Entregue nesta fase:

- o `school-management-bff` recebeu cutover operacional proprio para o bloco
  identity/tenant ja oficializado, cobrindo
  `GET /api/auth/escolas`, `POST /api/auth/escola-ativa` e
  `GET /api/auth/tenant/ativa`;
- foram introduzidas flags por rota e fallback simples para o monolito em caso
  de indisponibilidade dos servicos novos, permitindo rollback operacional sem
  tocar frontend nem contratos publicos;
- o fallback de sessao multiescola foi ligado aos contratos internos estaveis
  do monolito `GET /internal/auth/escolas` e
  `POST /internal/auth/escola-ativa`;
- o fallback da leitura de tenant ativo foi ligado ao contrato publico estavel
  `GET /api/auth/contexto-atual`, convertendo a resposta do monolito para o
  payload minimo de tenant ativo;
- foram adicionadas metricas Micrometer e health indicator dedicados ao bloco
  identity/tenant para observar roteamento direto, sucesso em servico novo,
  falha e fallback para o monolito;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=AuthSessionProxyIntegrationTest,InstitutionalTenantReadProxyIntegrationTest,AuthSessionMonolithIntegrationTest,AuthSessionFallbackIntegrationTest" test`,
  cobrindo operacao nominal, rollback por flag e fallback automatico.

Contagem regressiva da macrofase inicial de identidade e tenant: 1 fase
restante no escopo fechado atual.

### Fase 138 - Fechamento formal do primeiro bloco oficial de identity/tenant

Entregue nesta fase:

- foi encerrado formalmente o primeiro bloco oficial de identity/tenant no
  codigo novo, mantendo o recorte limitado ao `school-management-bff`,
  `identity-access-service` e `institutional-tenant-service`;
- ficam consolidadas como rotas publicas oficiais do bloco multiescola no BFF:
  `GET /api/auth/escolas`, `POST /api/auth/escola-ativa` e
  `GET /api/auth/tenant/ativa`;
- fica consolidado tambem o modelo operacional minimo do bloco: flags por rota,
  fallback simples para o monolito, metricas dedicadas e health indicator
  `identityTenantCutover`;
- para evitar reabertura ambigua de escopo, o fechamento registra
  explicitamente que continuam fora desta macrofase:
  `POST /api/auth/login`, `POST /api/auth/refresh`,
  `POST /api/auth/logout` e a retirada da dependencia do BFF de
  `GET /api/auth/contexto-atual`;
- foram adicionados testes unitarios especificos para o decider e para o health
  indicator do bloco identity/tenant, reforcando a cobertura da parte
  operacional sem abrir novas rotas;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=AuthSessionProxyIntegrationTest,InstitutionalTenantReadProxyIntegrationTest,AuthSessionMonolithIntegrationTest,AuthSessionFallbackIntegrationTest,IdentityTenantCutoverDeciderTest,IdentityTenantCutoverHealthIndicatorTest" test`.

Contagem regressiva da macrofase inicial de identidade e tenant: 0 fases
restantes no escopo fechado atual.

Fechamento da macrofase:

- a macrofase inicial de identity/tenant fica encerrada no plano funcional
  fechado de 6 fases;
- o proximo recorte futuro sugerido para esta frente, se ela for reaberta, e a
  migracao controlada de `login`/`refresh`/`logout` e a remocao da dependencia
  de `GET /api/auth/contexto-atual` no BFF.

### Fase 139 - Abertura fisica inicial do `planning-ai-service`

Entregue nesta fase:

- foi definida a contagem fechada de 10 fases para o desenvolvimento inicial
  completo do `planning-ai-service`, para evitar reabertura difusa de escopo;
- o servico fisico `planning-ai-service` foi criado no monorepo como novo
  runtime Spring Boot, com contrato interno, token interno, cliente HTTP
  dedicado para o monolito e tratamento de erro proprio;
- o menor recorte seguro escolhido para a fase 1 foi a leitura da biblioteca
  pedagogica de IA, porque ja existe um contrato estavel no monolito em
  `GET /api/biblioteca-conteudos-pedagogicos` e nao exige abrir escrita nem
  persistencia propria neste primeiro passo;
- o novo servico passou a expor
  `GET /internal/v1/biblioteca-conteudos-pedagogicos`, preservando os filtros
  `professorId`, `disciplinaId`, `tipoConteudo` e `tema`;
- o tratamento de `tipoConteudo` invalido foi preservado no novo servico como
  `404 RESOURCE_NOT_FOUND`, mantendo o comportamento funcional ja endurecido no
  monolito;
- a primeira rota publica sugerida para oficializacao futura no
  `school-management-bff` fica objetivamente definida como
  `GET /api/biblioteca-conteudos-pedagogicos`, reaproveitando este contrato
  interno novo sem abrir ainda geracao, aprovacao, versoes ou publicacao;
- esta fase permaneceu propositalmente sem BFF, sem persistencia propria, sem
  MongoDB/Kafka/Redis e sem qualquer escrita migrada;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo rota interna, protecao por token
  interno, propagacao de bearer e query string e preservacao de `404` para
  `tipoConteudo` invalido.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 9 fases
restantes no escopo fechado atual.

### Fase 140 - Oficializacao inicial da biblioteca pedagogica via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a primeira rota publica do
  `planning-ai-service`, exatamente em
  `GET /api/biblioteca-conteudos-pedagogicos`;
- a rota publica reutiliza o contrato interno ja aberto na fase anterior em
  `GET /internal/v1/biblioteca-conteudos-pedagogicos`, preservando os filtros
  `professorId`, `disciplinaId`, `tipoConteudo` e `tema`;
- o BFF resolve o contexto autenticado atual no monolito apenas para montar os
  headers internos `X-Usuario-Id` e `X-Escola-Id` exigidos pelo
  `planning-ai-service`, sem abrir ainda nenhuma mudanca em geracao, versoes,
  aprovacao ou publicacao de conteudo;
- foi criado cliente HTTP dedicado do BFF para o `planning-ai-service`, com
  token interno proprio e feature flag especifica para esta leitura oficial;
- para preservar o menor recorte seguro, esta fase nao introduziu cutover,
  fallback, escrita migrada, persistencia propria nem novas rotas publicas do
  bloco de planejamento e IA;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=BibliotecaConteudoPedagogicoReadControllerTest,PlanningAiBibliotecaReadProxyIntegrationTest" test`,
  cobrindo contrato da rota publica, propagacao de bearer/correlation ID,
  resolucao de contexto autenticado e chamada interna ao servico novo.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 8 fases
restantes no escopo fechado atual.

### Fase 141 - Leitura interna inicial de interacoes de planejamento IA

Entregue nesta fase:

- o `planning-ai-service` abriu o proximo recorte read-only interno do bloco em
  `GET /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`;
- a nova rota reutiliza o contrato atual do monolito em
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`,
  preservando o payload oficial de interacoes de IA do planejamento;
- foi separado no codigo novo o DTO proprio de interacao de planejamento IA,
  sem acoplar essa leitura ao contrato da biblioteca pedagogica;
- o comportamento de erro para planejamento inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional do monolito;
- para manter o recorte minimo seguro, esta fase permaneceu sem BFF, sem
  escrita migrada, sem geracao de conteudo, sem versoes, sem aprovacao e sem
  publicacao na biblioteca;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo contrato interno da nova rota,
  propagacao de bearer e preservacao de `404` para planejamento inexistente.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 7 fases
restantes no escopo fechado atual.

### Fase 142 - Oficializacao inicial das interacoes de planejamento IA via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a rota publica
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`;
- a rota publica reutiliza o contrato interno ja aberto na fase anterior em
  `GET /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`,
  preservando o payload oficial de interacoes de IA do planejamento;
- o BFF resolve o contexto autenticado atual no monolito apenas para montar os
  headers internos `X-Usuario-Id` e `X-Escola-Id` exigidos pelo
  `planning-ai-service`;
- foi criado no BFF um proxy dedicado para as interacoes de planejamento IA,
  reutilizando o mesmo client properties e a mesma feature flag de leitura do
  `planning-ai-service`;
- para manter o menor recorte seguro, esta fase nao abriu geracao, versoes,
  aprovacao, publicacao, escrita migrada, cutover ou fallback;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaInteracaoReadControllerTest,PlanningAiInteracaoReadProxyIntegrationTest" test`,
  cobrindo contrato da rota publica, propagacao de bearer/correlation ID,
  resolucao de contexto autenticado e chamada interna ao servico novo.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 6 fases
restantes no escopo fechado atual.

### Fase 143 - Leitura interna inicial de conteudos gerados por planejamento IA

Entregue nesta fase:

- o `planning-ai-service` abriu o proximo recorte read-only interno do bloco em
  `GET /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`;
- a nova rota reutiliza o contrato atual do monolito em
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`,
  preservando o payload oficial de conteudos gerados por planejamento IA;
- foi separado no codigo novo o DTO proprio de conteudo gerado, sem acoplar
  essa leitura ao contrato das interacoes ou da biblioteca pedagogica;
- o comportamento de erro para planejamento inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional do monolito;
- para manter o recorte minimo seguro, esta fase permaneceu sem BFF, sem
  escrita migrada, sem geracao nova, sem versoes, sem aprovacao e sem
  publicacao na biblioteca;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo contrato interno da nova rota,
  propagacao de bearer e preservacao de `404` para planejamento inexistente.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 5 fases
restantes no escopo fechado atual.

### Fase 144 - Oficializacao inicial dos conteudos de planejamento IA via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a rota publica
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`;
- a rota publica reutiliza o contrato interno ja aberto na fase anterior em
  `GET /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`,
  preservando o payload oficial de conteudos gerados por planejamento IA;
- o BFF resolve o contexto autenticado atual no monolito apenas para montar os
  headers internos `X-Usuario-Id` e `X-Escola-Id` exigidos pelo
  `planning-ai-service`;
- foi criado no BFF um proxy dedicado para os conteudos de planejamento IA,
  reutilizando o mesmo client properties e a mesma feature flag de leitura do
  `planning-ai-service`;
- para manter o menor recorte seguro, esta fase nao abriu geracao, versoes,
  aprovacao, publicacao, escrita migrada, cutover ou fallback;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoReadControllerTest,PlanningAiConteudoReadProxyIntegrationTest" test`,
  cobrindo contrato da rota publica, propagacao de bearer/correlation ID,
  resolucao de contexto autenticado e chamada interna ao servico novo.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 4 fases
restantes no escopo fechado atual.

### Fase 145 - Leitura interna inicial de conteudo de planejamento IA por ID

Entregue nesta fase:

- o `planning-ai-service` abriu o proximo recorte read-only interno do bloco em
  `GET /internal/v1/ia/conteudos/{conteudoId}`;
- a nova rota reutiliza o contrato atual do monolito em
  `GET /api/ia/conteudos/{conteudoId}`, preservando o payload oficial de
  detalhe do conteudo gerado;
- foi reaproveitado o DTO proprio de conteudo gerado ja aberto na fase
  anterior, evitando duplicacao de contrato no codigo novo;
- o comportamento de erro para conteudo inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional do monolito;
- para manter o recorte minimo seguro, esta fase permaneceu sem BFF, sem
  escrita migrada, sem geracao nova, sem versoes, sem aprovacao e sem
  publicacao na biblioteca;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo contrato interno da nova rota,
  propagacao de bearer e preservacao de `404` para conteudo inexistente.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 3 fases
restantes no escopo fechado atual.

### Fase 146 - Oficializacao inicial do detalhe de conteudo IA via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a rota publica
  `GET /api/ia/conteudos/{conteudoId}`;
- a rota publica reutiliza o contrato interno ja aberto na fase anterior em
  `GET /internal/v1/ia/conteudos/{conteudoId}`, preservando o payload oficial
  de detalhe do conteudo gerado;
- o BFF resolve o contexto autenticado atual no monolito apenas para montar os
  headers internos `X-Usuario-Id` e `X-Escola-Id` exigidos pelo
  `planning-ai-service`;
- foi criado no BFF um proxy dedicado para o detalhe de conteudo IA,
  reutilizando o mesmo client properties e a mesma feature flag de leitura do
  `planning-ai-service`;
- para manter o menor recorte seguro, esta fase nao abriu geracao, versoes,
  aprovacao, publicacao, escrita migrada, cutover ou fallback;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoDetailReadControllerTest,PlanningAiConteudoDetailReadProxyIntegrationTest" test`,
  cobrindo contrato da rota publica, propagacao de bearer/correlation ID,
  resolucao de contexto autenticado e chamada interna ao servico novo.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 2 fases
restantes no escopo fechado atual.

### Fase 147 - Leitura interna inicial das versoes de conteudo IA

Entregue nesta fase:

- o `planning-ai-service` abriu o proximo recorte read-only interno do bloco em
  `GET /internal/v1/ia/conteudos/{conteudoId}/versoes`;
- a nova rota reutiliza o contrato atual do monolito em
  `GET /api/ia/conteudos/{conteudoId}/versoes`, preservando o payload oficial
  de versoes do conteudo gerado;
- foi criado DTO proprio de versao de conteudo IA no codigo novo, isolando esse
  contrato do detalhe principal do conteudo;
- o comportamento de erro para conteudo inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional do monolito;
- para manter o recorte minimo seguro, esta fase permaneceu sem BFF, sem
  escrita migrada, sem criacao de versao, sem aprovacao e sem publicacao;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo contrato interno da nova rota,
  propagacao de bearer e preservacao de `404` para conteudo inexistente.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 1 fase
restante no escopo fechado atual.

### Fase 148 - Oficializacao inicial das versoes de conteudo IA via BFF

Entregue nesta fase:

- o `school-management-bff` passou a oficializar a rota publica
  `GET /api/ia/conteudos/{conteudoId}/versoes`;
- a rota publica reutiliza o contrato interno ja aberto na fase anterior em
  `GET /internal/v1/ia/conteudos/{conteudoId}/versoes`, preservando o payload
  oficial de versoes do conteudo gerado;
- o BFF resolve o contexto autenticado atual no monolito apenas para montar os
  headers internos `X-Usuario-Id` e `X-Escola-Id` exigidos pelo
  `planning-ai-service`;
- foi criado no BFF um proxy dedicado para as versoes de conteudo IA,
  reutilizando o mesmo client properties e a mesma feature flag de leitura do
  `planning-ai-service`;
- para manter o menor recorte seguro, esta fase nao abriu geracao, criacao de
  versao, aprovacao, publicacao, escrita migrada, cutover ou fallback;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoVersaoReadControllerTest,PlanningAiConteudoVersaoReadProxyIntegrationTest" test`,
  cobrindo contrato da rota publica, propagacao de bearer/correlation ID,
  resolucao de contexto autenticado e chamada interna ao servico novo.

Contagem regressiva da macrofase inicial de `planning-ai-service`: 0 fases
restantes no escopo fechado atual.

Fechamento da macrofase:

- a macrofase inicial do `planning-ai-service` fica encerrada no plano
  funcional fechado de 10 fases;
- ficam oficializadas no `school-management-bff` as leituras publicas
  `GET /api/biblioteca-conteudos-pedagogicos`,
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`,
  `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`,
  `GET /api/ia/conteudos/{conteudoId}` e
  `GET /api/ia/conteudos/{conteudoId}/versoes`;
- permanecem explicitamente fora deste ciclo inicial qualquer geracao,
  criacao de versao, aprovacao, publicacao na biblioteca, persistencia propria
  e adocao de MongoDB/Kafka/Redis.

### Fase 149 - Abertura interna inicial da geracao de conteudo IA

Entregue nesta fase:

- foi aberto o novo ciclo fechado de 8 fases para a escrita minima do
  `planning-ai-service`, cobrindo geracao, criacao de versao, aprovacao e
  publicacao, sempre em pares `servico interno -> BFF`;
- a primeira fase desse novo ciclo abriu no `planning-ai-service` o contrato
  interno `POST /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`;
- a nova rota reutiliza o contrato atual do monolito em
  `POST /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`,
  preservando o payload oficial de conteudo gerado e o contrato de entrada de
  geracao;
- foi criado DTO proprio de request para geracao de conteudo IA no codigo novo,
  com as mesmas validacoes basicas de `promptProfessor`, `tipoConteudo`,
  `titulo` e `reutilizavel`;
- o comportamento de erro para planejamento inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional do monolito;
- para manter o menor recorte seguro, esta fase permaneceu sem BFF, sem
  criacao publica, sem criacao de versao, sem aprovacao e sem publicacao;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service test`, cobrindo o contrato interno de geracao,
  propagacao de bearer e preservacao de `404` para planejamento inexistente.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
7 fases restantes no escopo fechado atual.

### Fase 150 - Oficializacao inicial da geracao de conteudo IA no BFF

Entregue nesta fase:

- foi oficializado no `school-management-bff` o contrato publico
  `POST /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`;
- a nova rota publica ficou desacoplada por porta propria de escrita e cliente
  HTTP dedicado para o `planning-ai-service`, sem reabrir contratos read-only
  ja entregues;
- o BFF passou a resolver o contexto autenticado atual pelo `AuthContextPort` e
  a propagar `Authorization`, `X-Internal-Token`, `X-Correlation-Id`,
  `X-Usuario-Id` e `X-Escola-Id` para o contrato interno
  `POST /internal/v1/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`;
- foi adicionada feature flag propria
  `features.planning-ai-write-proxy-enabled`, separando a oficializacao de
  escrita da flag ja existente de leitura;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoWriteControllerTest,PlanningAiConteudoWriteProxyIntegrationTest" test`,
  cobrindo contrato publico e integracao ponta a ponta do proxy;
- permanecem explicitamente fora desta fase criacao de versao, aprovacao,
  publicacao na biblioteca, persistencia propria e qualquer refatoracao ampla.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
6 fases restantes no escopo fechado atual.

### Fase 151 - Abertura interna inicial da criacao de versao de conteudo IA

Entregue nesta fase:

- foi aberto no `planning-ai-service` o contrato interno
  `POST /internal/v1/ia/conteudos/{conteudoId}/versoes`;
- a nova rota reutiliza o contrato atual do monolito em
  `POST /api/ia/conteudos/{conteudoId}/versoes`, preservando o payload oficial
  de versao criada e o contrato de entrada de edicao;
- foi criado DTO proprio de request para criacao de versao no codigo novo, com
  as mesmas validacoes basicas de `conteudo` e `motivoAlteracao`;
- o comportamento de erro para conteudo inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional atual;
- para manter o menor recorte seguro, esta fase permaneceu sem BFF, sem
  oficializacao publica, sem aprovacao e sem publicacao na biblioteca;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service "-Dtest=PlanningAiInternalControllerIntegrationTest" test`,
  cobrindo criacao interna de versao, propagacao de bearer, encaminhamento do
  payload e preservacao de `404`.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
5 fases restantes no escopo fechado atual.

### Fase 152 - Oficializacao inicial da criacao de versao de conteudo IA no BFF

Entregue nesta fase:

- foi oficializado no `school-management-bff` o contrato publico
  `POST /api/ia/conteudos/{conteudoId}/versoes`;
- a nova rota publica ficou desacoplada por porta propria de escrita de versao
  e cliente HTTP dedicado para o `planning-ai-service`, sem reabrir os contratos
  read-only nem misturar com aprovacao/publicacao;
- o BFF passou a resolver o contexto autenticado atual pelo `AuthContextPort` e
  a propagar `Authorization`, `X-Internal-Token`, `X-Correlation-Id`,
  `X-Usuario-Id` e `X-Escola-Id` para o contrato interno
  `POST /internal/v1/ia/conteudos/{conteudoId}/versoes`;
- a oficializacao reutiliza a mesma flag de escrita
  `features.planning-ai-write-proxy-enabled`, mantendo separado o eixo de
  escrita da flag ja existente de leitura;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoVersaoWriteControllerTest,PlanningAiConteudoVersaoWriteProxyIntegrationTest" test`,
  cobrindo contrato publico e integracao ponta a ponta do proxy;
- permanecem explicitamente fora desta fase aprovacao de versao, publicacao na
  biblioteca, persistencia propria e qualquer refatoracao ampla.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
4 fases restantes no escopo fechado atual.

### Fase 153 - Abertura interna inicial da aprovacao de versao de conteudo IA

Entregue nesta fase:

- foi aberto no `planning-ai-service` o contrato interno
  `PATCH /internal/v1/ia/conteudos/{conteudoId}/aprovar-versao`;
- a nova rota reutiliza o contrato atual do monolito em
  `PATCH /api/ia/conteudos/{conteudoId}/aprovar-versao`, preservando o payload
  oficial do conteudo aprovado e o contrato de entrada de aprovacao;
- foi criado DTO proprio de request para aprovacao de versao no codigo novo,
  com as mesmas validacoes basicas de `numeroVersao` e `publicarBiblioteca`;
- o client HTTP do `planning-ai-service` foi ajustado para uma request factory
  compativel com `PATCH`, evitando acoplamento ao comportamento limitado da
  implementacao anterior baseada em `SimpleClientHttpRequestFactory`;
- o comportamento de erro para conteudo inexistente foi preservado como
  `404 RESOURCE_NOT_FOUND`, mantendo o contrato funcional atual;
- para manter o menor recorte seguro, esta fase permaneceu sem BFF e sem abrir
  ainda o recorte de publicacao dedicada na biblioteca;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl planning-ai-service "-Dtest=PlanningAiInternalControllerIntegrationTest" test`,
  cobrindo aprovacao interna, propagacao de bearer, encaminhamento do payload,
  suporte a `PATCH` e preservacao de `404`.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
3 fases restantes no escopo fechado atual.

### Fase 154 - Oficializacao inicial da aprovacao de versao de conteudo IA no BFF

Entregue nesta fase:

- foi oficializado no `school-management-bff` o contrato publico
  `PATCH /api/ia/conteudos/{conteudoId}/aprovar-versao`;
- a nova rota publica ficou desacoplada por porta propria de aprovacao e client
  HTTP dedicado para o `planning-ai-service`, sem misturar esse recorte com a
  publicacao dedicada na biblioteca;
- o BFF passou a resolver o contexto autenticado atual pelo `AuthContextPort` e
  a propagar `Authorization`, `X-Internal-Token`, `X-Correlation-Id`,
  `X-Usuario-Id` e `X-Escola-Id` para o contrato interno
  `PATCH /internal/v1/ia/conteudos/{conteudoId}/aprovar-versao`;
- a oficializacao reutiliza a mesma flag de escrita
  `features.planning-ai-write-proxy-enabled`, mantendo separado o eixo de
  escrita da flag ja existente de leitura;
- a validacao ficou restrita ao modulo tocado com
  `mvn -pl school-management-bff "-Dtest=PlanejamentoIaConteudoVersaoApproveControllerTest,PlanningAiConteudoVersaoApproveProxyIntegrationTest" test`,
  cobrindo contrato publico e integracao ponta a ponta do proxy `PATCH`;
- permanece explicitamente fora desta fase a abertura do contrato dedicado de
  publicacao na biblioteca e qualquer refatoracao ampla.

Contagem regressiva do novo ciclo de escrita minima do `planning-ai-service`:
2 fases restantes no escopo fechado atual.
