# 16 - Roteiro de Continuidade - Frontend + Backend

## Objetivo

Registrar o ponto atual do projeto e orientar a proxima evolucao do MVP de gestao escolar considerando o estado real do repositorio.

Este documento substitui roteiros antigos que ainda tratavam o backend como inexistente, o CRUD de aluno como local ou a migracao UUID como pendente.

## Estado atual consolidado

### Backend

O backend `school-management-service` ja existe e esta organizado como monolito modular Spring Boot.

Modulos atuais:

- `accesscontrol`: autenticacao, sessoes, usuarios, perfis e permissoes;
- `studentmanagement`: cadastro de alunos;
- `responsavelmanagement`: cadastro de responsaveis, vinculo aluno-responsavel e consulta cadastral;
- `academiccatalog`: periodos letivos, series, turnos, turmas e catalogos academicos;
- `enrollment`: matriculas, status e catalogos de matricula;
- `studentdocument`: documentos de alunos;
- `schoolhistory`: historicos escolares;
- `transfermanagement`: escolas de origem e transferencias;
- `shared`: tratamento global de erros, documentos compartilhados e estruturas comuns.

Principais endpoints:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET|POST|PUT|DELETE /api/usuarios`
- `GET|POST|PUT|DELETE /api/perfis`
- `GET|POST|PUT|DELETE /api/permissoes`
- `GET|POST|PUT|DELETE /api/alunos`
- `GET|POST|PUT|DELETE /api/responsaveis`
- `GET|POST|DELETE /api/alunos/{idAluno}/responsaveis`
- `GET /api/consulta-cadastral`
- `GET|POST /api/periodos-letivos`
- `GET|POST|PUT /api/series`
- `GET|POST|PUT /api/turnos`
- `GET|POST|PUT /api/turmas`
- `GET|POST|PATCH|DELETE /api/matriculas`
- `GET|POST|DELETE /api/documentos-alunos`
- `GET|POST|PUT|DELETE /api/historicos-escolares`
- `GET|POST /api/escolas-origem`
- `GET|POST /api/transferencias`

### Banco de dados

O projeto usa PostgreSQL. A base oficial atual e `gestao_escolar`, representada por:

```text
docs/v2/scriptdb.sql
```

A documentacao ativa da base oficial fica em:

```text
docs/20-base-dados-oficial-scriptdb.md
```

Documentos e scripts que descrevem modelagens anteriores foram organizados em `docs/historico` e nao devem orientar novas implementacoes.

### Frontend

O frontend `school-management-web` esta organizado em um `host` e remotos federados por dominio.

O `host` concentra:

- login;
- home/menu;
- estado de sessao;
- publicacao do contrato de shell;
- guards;
- usuarios;
- perfis;
- permissoes;
- rotas federadas estaticas.

Os remotos atuais cobrem:

- `mfe-alunos`: alunos;
- `mfe-responsaveis`: responsaveis;
- `mfe-catalogo-academico`: catalogo academico e disciplinas;
- `mfe-matriculas`: matriculas;
- `mfe-professores`: professores;
- `mfe-aulas-avaliacoes`: aulas e avaliacoes;
- `mfe-planejamento-ia`: planejamento, IA e biblioteca pedagogica;
- `mfe-dashboard`: dashboards e snapshots administrativos.

As rotas federadas e menus do MVP sao definidos no contrato declarativo:

```text
school-management-web/host/src/app/core/shell/shell-navigation.config.ts
```

O host publica API base, token, usuario, perfis e permissoes para os remotos por `school-management.shell.context.v1`. O refresh token permanece interno ao host.

## Proxima evolucao recomendada

### 1. Consolidar documentacao operacional

Atualizar continuamente:

- `README.md`;
- `school-management-service/README.md`;
- `school-management-web/README.md`;
- `docs/17-status-atual-do-projeto.md`;
- `docs/18-url-para-testes.md`;
- `docs/20-base-dados-oficial-scriptdb.md`.

### 2. Planejar o proximo epico funcional

O MVP atual foi considerado aceito pelo cliente dentro dos parametros desejados. A proxima evolucao funcional deve focar:

1. consolidacao do frontend federado por dominio, mantendo o host como shell;
2. evolucao incremental de multi-escola no backend e nos contratos internos;
3. ajustes pontuais de UX, contratos e integracao quando surgirem lacunas reais;
4. preparacao arquitetural para fases futuras de BFFs e servicos, sem criar novos runtimes agora.

Historico, documentos e transferencia nao sao o proximo epico; fazem parte do ciclo funcional ja desenvolvido ate aqui.

### 3. Melhorar experiencia operacional

Prioridade sugerida:

- ampliar padronizacao de mensagens de erro no Angular quando surgirem novos fluxos;
- componente comum para estados de carregamento, vazio e erro;
- manter documentacao atualizada quando o contrato real mudar.

### 4. Fortalecer qualidade tecnica

Prioridade sugerida:

- garantir `mvnw test` verde no backend;
- garantir `npm run build` no host;
- garantir `npm run build` no remoto afetado;
- revisar Swagger/OpenAPI quando endpoints mudarem;
- evoluir `API_BASE_URL` centralizado para configuracao por ambiente quando houver necessidade de empacotamento/deploy;
- documentar variaveis de ambiente/configuracao do frontend.

Permissoes granulares por rota, tela e endpoint ficam planejadas para o final do projeto, depois que as funcionalidades principais estiverem estabilizadas.

## Itens fora de contexto retirados da referencia ativa

Nao devem ser usados como referencia ativa:

- roteiro antigo de KAN-33, pois o cadastro de aluno ja existe;
- roteiro antigo de KAN-3, pois responsavel/vinculo/consulta cadastral ja existem no backend;
- roteiro de migracao BIGINT para UUID, pois o projeto atual ja opera com UUID;
- script SQL legado baseado em BIGINT;
- documentos e scripts desalinhados com `docs/v2/scriptdb.sql`, com copias preservadas em `docs/historico`.

## Prompt sugerido para continuidade

```text
Contexto: o projeto projectc-school-management ja possui backend Spring Boot, frontend Angular com host e remotos federados por dominio, autenticacao JWT, CRUDs principais e base oficial documentada em docs/v2/scriptdb.sql.

Objetivo: evoluir o produto a partir do MVP aceito pelo cliente.

Prioridade:
1) planejar e implementar o epico de aulas, professores, alunos, notas, comportamento e dashboards;
2) manter historico/documentos/transferencia como parte do ciclo ja desenvolvido, refinando apenas mediante demanda explicita;
3) deixar permissoes granulares para o final do projeto;
4) atualizar documentacao quando o contrato real mudar;
5) executar testes/builds relevantes antes de encerrar.
```
