# projectc-school-management

Projeto de gestao escolar com backend Spring Boot, frontend Angular e documentacao de produto/arquitetura mantida no proprio repositorio.

## Estrutura atual

- `docs/16-roteiro-continuidade-frontend-backend.md`: roteiro ativo de continuidade.
- `docs/17-status-atual-do-projeto.md`: fotografia tecnica atual do projeto.
- `docs/18-url-para-testes.md`: checklist de testes HTTP manuais.
- `docs/19-proximo-epico-aulas-professores-notas-dashboards.md`: direcionamento do proximo epico funcional.
- `docs/20-base-dados-oficial-scriptdb.md`: documentacao da base oficial.
- `docs/v2/scriptdb.sql`: dump SQL oficial da base `gestao_escolar`.
- `docs/historico`: documentos e scripts antigos preservados apenas para consulta historica.
- `school-management-service`: API backend.
- `school-management-web/host`: aplicacao Angular principal.
- `school-management-web/microfrontend`: aplicacao Angular remota usada como base de microfrontend federado.

## Status atual

O MVP atual foi considerado aceito pelo cliente dentro dos parametros desejados. O projeto ja possui fluxo funcional cobrindo:

- autenticacao com login JWT, refresh e logout;
- administracao tecnica de usuarios, perfis e permissoes;
- cadastro de alunos;
- cadastro de responsaveis;
- vinculo entre aluno e responsavel;
- consulta cadastral consolidada;
- cadastro academico com periodos letivos, series, turnos e turmas;
- matricula de aluno em turma/periodo letivo, incluindo status/cancelamento conforme fluxo aceito;
- documentos de alunos;
- historico escolar;
- transferencia de alunos.

A matriz granular de permissoes por rota/tela/endpoint sera implementada no final do projeto, apos estabilizacao das funcionalidades principais.

## Base de dados oficial

A base oficial atual e `gestao_escolar`, representada por:

```text
docs/v2/scriptdb.sql
```

Use este arquivo como fonte para nomes de tabelas, colunas, constraints, relacionamentos e dados de apoio. A documentacao da decisao esta em:

```text
docs/20-base-dados-oficial-scriptdb.md
```

Copias organizadas dos scripts SQL e documentos que nao devem orientar a modelagem atual ficam em:

```text
docs/historico
```

## Backend

Local: `school-management-service`

Stack principal:

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Security;
- JWT;
- Spring Data JPA;
- PostgreSQL;
- Flyway;
- Springdoc OpenAPI.

Execucao local:

```bash
cd school-management-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

No Windows, tambem pode usar:

```bash
cd school-management-service
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Credenciais locais:

- login: `admin`
- senha: `admin123`

Endpoint de login:

```http
POST http://localhost:8080/api/auth/login
```

```json
{
  "login": "admin",
  "senha": "admin123"
}
```

## Plataforma distribuida

A fundacao da arquitetura distribuida e agregada pelo `pom.xml` da raiz e
mantem o monolito com build independente.

Modulos iniciais:

- `school-management-bff`: fachada reativa e contexto de requisicao;
- `academic-catalog-service`: servico piloto estruturado em DDD;
- `platform`: Compose, contratos HTTP/eventos e bootstrap dos bancos locais.

Build unitario da plataforma, sem Docker ou WSL:

```powershell
.\school-management-service\mvnw.cmd -f pom.xml verify
```

Validacao de integracao com Testcontainers, somente quando houver um engine
Docker/OCI funcional:

```powershell
.\school-management-service\mvnw.cmd -f pom.xml -Pintegration verify
```

Infraestrutura local:

```powershell
docker compose -f platform/compose.yaml up -d
```

As consultas do catalogo piloto ficam somente em `/internal/v1/**`. Para iniciar
o servico, defina `CATALOG_INTERNAL_API_TOKEN`; as chamadas internas tambem
exigem `X-Correlation-Id`, `X-Usuario-Id` e `X-Escola-Id`. Nenhum token real e
versionado no repositorio.

Os comandos `POST` internos tambem exigem `Idempotency-Key`. A chave, o recurso
criado e o evento de outbox sao persistidos atomicamente no PostgreSQL; o Kafka
e publicado de forma assincrona quando `CATALOG_OUTBOX_PUBLISHER_ENABLED=true`.
O topico principal padrao e `school.catalog.events.v1`, com retry exponencial e
DLT em `school.catalog.events.v1.DLT`. O publisher permanece desabilitado por
padrao para nao alterar o fluxo operacional antes do cutover.

As leituras podem usar um snapshot Redis isolado por escola quando
`CATALOG_CACHE_ENABLED=true`. A chave inclui ambiente, servico, escola e versao;
o TTL padrao e cinco minutos. Comandos novos e eventos Kafka invalidam o
snapshot. Falhas de leitura, escrita ou invalidacao no Redis operam em modo
fail-open, mantendo o PostgreSQL como fonte oficial. O cache permanece
desabilitado por padrao antes do cutover.

A migracao do catalogo e opt-in e executa em dry-run por padrao. Configure as
credenciais de origem com `CATALOG_MIGRATION_SOURCE_URL`,
`CATALOG_MIGRATION_SOURCE_USERNAME` e `CATALOG_MIGRATION_SOURCE_PASSWORD`, alem
da conexao de destino normal do servico. Use `CATALOG_MIGRATION_ENABLED=true`;
o relatorio sera escrito em `target/catalog-migration-report.json`. Somente
depois de revisar um dry-run, use `CATALOG_MIGRATION_APPLY=true`. O apply e
recusado quando a origem viola as referencias multi-escola do destino, e pode
ser repetido sem duplicar registros. Nenhuma dessas flags e habilitada por
padrao.

O WSL nao faz parte do contrato da aplicacao. Ele pode ser uma dependencia do
Docker Desktop no Windows, mas falhas locais de WSL nao bloqueiam o build
unitario nem a evolucao do codigo.

Roteamento read-only do BFF, mantendo o monolito em `http://localhost:8080` por
padrao:

```powershell
.\school-management-service\mvnw.cmd -f pom.xml -pl school-management-bff spring-boot:run
```

```http
GET http://localhost:8081/api/disciplinas
Authorization: Bearer <access-token-opaco>
X-Correlation-Id: <correlation-id-opcional>
```

Rotas externas cobertas pelo BFF nesta fase:

- `GET /api/disciplinas` e `GET /api/disciplinas/{id}`
- `GET /api/periodos-letivos` e `GET /api/periodos-letivos/{id}`
- `GET /api/series` e `GET /api/series/{id}`
- `GET /api/turnos` e `GET /api/turnos/{id}`
- `GET /api/turmas` e `GET /api/turmas/{id}`
- `GET /api/turmas/{turmaId}/disciplinas`
- `GET /api/academico/catalogos/niveis-ensino`
- `GET /api/academico/catalogos/turnos`

O cutover para o `academic-catalog-service` continua desabilitado por padrao e
so e considerado quando as tres condicoes abaixo forem verdadeiras:

- `CATALOG_READ_CUTOVER_ENABLED=true`
- `CATALOG_READ_CUTOVER_REPORT_PATH` apontando para um relatorio JSON real com
  `applied=true`, `reconciled=true` e sem divergencias
- a flag especifica da rota (`CATALOG_READ_ROUTE_DISCIPLINAS`,
  `CATALOG_READ_ROUTE_TURNO_BY_ID`, etc.) ligada

Quando a rota esta habilitada para cutover, o BFF resolve `usuarioId` e
`escolaId` no monolito por `GET /api/auth/contexto-atual`, repassa esse
contexto ao `academic-catalog-service` com `CATALOG_INTERNAL_API_TOKEN` e faz
fallback automatico para o monolito em indisponibilidade do servico novo.

Observabilidade operacional do cutover read-only:

- health dedicado em `/actuator/health/catalogReadCutover`, com status do gate
  do relatorio reconciliado;
- metricas `bff.catalog.read.route.total` por rota, alvo, motivo e resultado;
- metricas `bff.catalog.read.catalog.error.total` para falhas do catalogo novo;
- metricas `bff.catalog.read.fallback.total` para fallback efetivo ao monolito;
- metricas de circuit breaker do monolito expostas no Prometheus via
  Resilience4j/Micrometer.

Rotas ja validadas operacionalmente em cutover controlado:

- `GET /api/disciplinas`
- `GET /api/disciplinas/{id}`
- `GET /api/turmas/{turmaId}/disciplinas`
- `GET /api/turnos`
- `GET /api/turnos/{id}`
- `GET /api/periodos-letivos`
- `GET /api/periodos-letivos/{id}`
- `GET /api/series`
- `GET /api/series/{id}`
- `GET /api/turmas`
- `GET /api/turmas/{id}`
- `GET /api/academico/catalogos/turnos`
- `GET /api/academico/catalogos/niveis-ensino`

Na migracao real, o `academic-catalog-service` agora aceita substituir os seeds
globais de `nivel_ensino` e `turno` quando o destino ainda nao possui nenhum
dado escolar. Isso evita bloqueio por colisao de IDs no primeiro carregamento
real, sem flexibilizar colisoes de dados escolares ja migrados.

Rollback do roteamento read-only, sem alterar codigo:

```powershell
$env:CATALOG_READ_CUTOVER_ENABLED='false'
```

O frontend continua apontando para o monolito durante esta fase.

## Frontend

Local: `school-management-web/host`

Execucao local:

```bash
cd school-management-web/host
npm install
npm start
```

Aplicacao remota:

```bash
cd school-management-web/microfrontend
npm install
npm start
```

Por padrao, os services Angular consomem o backend em `http://localhost:8080`.

No estado atual, o host e o shell de seguranca/menu/administracao tecnica. As funcionalidades escolares de negocio ficam no microfrontend e sao carregadas por Native Federation a partir do contrato declarativo em:

```text
school-management-web/host/src/app/core/shell/shell-navigation.config.ts
```

A rota marcador `/microfrontend` e o expose `./Component` foram removidos; o remoto publica apenas telas funcionais.

## Proximo epico funcional

O proximo epico deve focar:

- aulas e planejamento de aulas;
- professores e relacao professor/turma/disciplina;
- alunos em contexto de aula;
- notas e acompanhamento de evolucao;
- comportamento de alunos e professores;
- dashboards de aulas, matriculas e evolucao dos alunos.

## Validacao

Backend:

```bash
cd school-management-service
./mvnw test
```

Frontend host:

```bash
cd school-management-web/host
npm run build
```

Frontend microfrontend:

```bash
cd school-management-web/microfrontend
npm run build
```
