# 17 - Status Atual do Projeto

## Objetivo

Registrar uma fotografia tecnica do estado atual do projeto `projectc-school-management`.

Use este documento como ponto de partida para proximas conversas e para evitar retomadas com informacoes antigas.

## Estrutura do repositorio

```text
projectc-school-management/
  docs/
    v2/scriptdb.sql
    historico/
  school-management-service/
  school-management-web/
    host/
    mfe-matriculas/
    mfe-catalogo-academico/
    mfe-dashboard/
    mfe-planejamento-ia/
    mfe-professores/
    mfe-aulas-avaliacoes/
    mfe-responsaveis/
    mfe-alunos/
```

## Backend

Projeto: `school-management-service`

Stack:

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Security;
- JWT;
- Spring Data JPA;
- PostgreSQL;
- Flyway;
- Bean Validation;
- Springdoc OpenAPI.

### Modulos atuais

| Modulo | Responsabilidade |
| --- | --- |
| `accesscontrol` | Login, refresh, logout, usuarios, perfis, permissoes e sessoes |
| `studentmanagement` | Cadastro e manutencao de alunos |
| `responsavelmanagement` | Responsaveis, vinculo aluno-responsavel e consulta cadastral |
| `academiccatalog` | Periodos letivos, series, turnos, turmas e catalogos academicos |
| `enrollment` | Matriculas, status, cancelamento/exclusao e catalogos de matricula |
| `studentdocument` | Documentos vinculados a alunos |
| `schoolhistory` | Historicos escolares |
| `transfermanagement` | Escolas de origem e transferencias de alunos |
| `shared` | Excecoes, respostas de erro, documentos compartilhados e componentes comuns |

### Endpoints principais

#### Autenticacao

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

#### Controle de acesso

- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`
- `GET /api/perfis`
- `GET /api/perfis/{id}`
- `POST /api/perfis`
- `PUT /api/perfis/{id}`
- `DELETE /api/perfis/{id}`
- `GET /api/permissoes`
- `GET /api/permissoes/{id}`
- `POST /api/permissoes`
- `PUT /api/permissoes/{id}`
- `DELETE /api/permissoes/{id}`

#### Alunos

- `GET /api/alunos`
- `GET /api/alunos/{id}`
- `POST /api/alunos`
- `PUT /api/alunos/{id}`
- `DELETE /api/alunos/{id}`

#### Responsaveis

- `GET /api/responsaveis`
- `GET /api/responsaveis/{id}`
- `POST /api/responsaveis`
- `PUT /api/responsaveis/{id}`
- `DELETE /api/responsaveis/{id}`

#### Vinculo aluno-responsavel

- `POST /api/alunos/{idAluno}/responsaveis`
- `POST /api/alunos/{idAluno}/responsaveis/{idResponsavel}`
- `GET /api/alunos/{idAluno}/responsaveis`
- `DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`

#### Consulta cadastral

- `GET /api/consulta-cadastral`

Filtros:

- `nomeAluno`
- `cpfAluno`
- `nomeResponsavel`
- `cpfResponsavel`
- `page`
- `size`

#### Catalogo academico

- `GET /api/periodos-letivos`
- `GET /api/periodos-letivos/{id}`
- `POST /api/periodos-letivos`
- `GET /api/series`
- `GET /api/series/{id}`
- `POST /api/series`
- `PUT /api/series/{id}`
- `GET /api/turnos`
- `GET /api/turnos/{id}`
- `POST /api/turnos`
- `PUT /api/turnos/{id}`
- `GET /api/turmas`
- `GET /api/turmas/{id}`
- `POST /api/turmas`
- `PUT /api/turmas/{id}`
- `GET /api/academico/catalogos/niveis-ensino`
- `GET /api/academico/catalogos/turnos`

#### Matriculas

- `GET /api/matriculas`
- `POST /api/matriculas`
- `PATCH /api/matriculas/{id}/status`
- `DELETE /api/matriculas/{id}`
- `GET /api/matriculas/catalogos/tipos`
- `GET /api/matriculas/catalogos/status`
- `GET /api/matriculas/catalogos/status-etapas`

Filtros de listagem:

- `alunoId`
- `turmaId`
- `periodoLetivoId`
- `status`

#### Documentos, historico e transferencia

- `GET /api/documentos-alunos/{id}`
- `GET /api/documentos-alunos/alunos/{alunoId}`
- `POST /api/documentos-alunos`
- `DELETE /api/documentos-alunos/{id}`
- `GET /api/historicos-escolares`
- `GET /api/historicos-escolares/{id}`
- `POST /api/historicos-escolares`
- `PUT /api/historicos-escolares/{id}`
- `DELETE /api/historicos-escolares/{id}`
- `GET /api/escolas-origem`
- `GET /api/escolas-origem/{id}`
- `POST /api/escolas-origem`
- `GET /api/transferencias/{id}`
- `GET /api/transferencias/alunos/{alunoId}`
- `POST /api/transferencias`

## Banco de dados

A base oficial do projeto e `gestao_escolar`, representada pelo dump:

```text
docs/v2/scriptdb.sql
```

O documento ativo da base oficial e:

```text
docs/20-base-dados-oficial-scriptdb.md
```

Regra atual: exemplos, contratos e novas implementacoes devem usar `docs/v2/scriptdb.sql` como fonte para nomes de tabelas, colunas, constraints e relacionamentos. Scripts SQL e documentos desalinhados devem ser tratados como historicos; copias organizadas ficam em `docs/historico`.

## Frontend

Projeto: `school-management-web`

Stack:

- Angular 20;
- Angular Material;
- Native Federation;
- TypeScript;
- RxJS.

### Aplicacoes

- `host`: shell principal, responsavel por login, sessao, menu, guards, usuarios, perfis, permissoes e roteamento federado.
- `mfe-matriculas`: remoto do dominio de matriculas.
- `mfe-catalogo-academico`: remoto do dominio de catalogo academico e disciplinas/historico operacional.
- `mfe-dashboard`: remoto do dominio de dashboards e snapshots administrativos.
- `mfe-planejamento-ia`: remoto do dominio de planejamento bimestral, IA e biblioteca pedagogica.
- `mfe-professores`: remoto do dominio de professores.
- `mfe-aulas-avaliacoes`: remoto do dominio de aulas e avaliacoes.
- `mfe-responsaveis`: remoto do dominio de responsaveis.
- `mfe-alunos`: remoto do dominio de alunos.

O host publica o contexto de shell em:

```text
localStorage: school-management.shell.context.v1
evento: school-management:shell-context-changed
```

As rotas federadas e itens de menu do MVP ficam centralizados em:

```text
school-management-web/host/src/app/core/shell/shell-navigation.config.ts
```

### Rotas principais do host

- `/login`
- `/home`
- `/dashboard`
- `/dashboard/config`
- `/dashboard/snapshots`
- `/students`
- `/students/new`
- `/students/:id`
- `/students/:id/edit`
- `/responsibles`
- `/responsibles/new`
- `/responsibles/:id`
- `/responsibles/:id/edit`
- `/academic/periods`
- `/academic/series`
- `/academic/shifts`
- `/academic/classes`
- `/academic/disciplines`
- `/teachers`
- `/teachers/:id`
- `/lessons`
- `/lessons/:id`
- `/assessments`
- `/assessments/:id`
- `/planning`
- `/planning/:id`
- `/planning-library`
- `/enrollment`
- `/auth/users`
- `/auth/profiles`
- `/auth/permissions`

### Integracoes HTTP atuais

O host consome a URL base centralizada em `school-management-web/host/src/app/core/config/api.config.ts`, atualmente apontando para `http://localhost:8080`, para:

- autenticacao;
- usuarios/perfis/permissoes.

Os remotos recebem a API base pelo contrato de shell e consomem o backend para:

- alunos;
- responsaveis;
- periodos/series/turnos/turmas;
- matriculas;
- documentos;
- historico;
- disciplinas.
- professores;
- aulas;
- avaliacoes/notas;
- planejamento bimestral;
- IA e biblioteca pedagogica;
- dashboards e snapshots administrativos.

Ainda existe uso de `localStorage` para sessao/autenticacao. Dados de negocio como alunos e responsaveis usam o backend como fonte oficial, com cache apenas em memoria durante a sessao.

### Ajustes tecnicos ja consolidados

- URL base da API centralizada em `core/config/api.config.ts`.
- Mensagens de erro dos fluxos principais passam a aproveitar o envelope de erro do backend.
- Cache persistente em `localStorage` removido dos dados de negocio de alunos e responsaveis.
- Regra frontend de permissao centralizada com suporte a permissao `ADMIN`.
- Tela de secretaria/matriculas evoluida com resumo operacional, filtros avancados e consulta sem acoes locais que nao existem no backend.
- Matricula permite cadastro rapido de novo aluno; responsavel e obrigatorio apenas para aluno menor de 18 anos.
- O fluxo de matricula/status/cancelamento em uso esta aceito pelo cliente dentro dos parametros esperados para o MVP atual.
- Historico, documentos e transferencia pertencem ao escopo ja trabalhado neste ciclo; nao devem ser tratados como proximo epico.
- A matriz completa de permissoes por funcionalidade fica planejada para o final do projeto, apos estabilizacao das funcionalidades finais.
- A rota marcador `/microfrontend`, o expose `./Component`, o remoto legado `microfrontend` e o carregamento dinamico antigo por `AplicativosService` foram removidos; o MVP usa o contrato declarativo do shell com remotos `mfe-*`.

## Testes existentes

O backend possui testes de integracao para:

- alunos;
- responsaveis;
- vinculo aluno-responsavel;
- catalogo academico;
- matriculas;
- contexto Spring Boot.

## Proximas prioridades recomendadas

1. Preservar o host como shell de seguranca e administracao tecnica, evitando recolocar telas de negocio nele.
2. Continuar a evolucao incremental por dominio e por remoto, sem iniciar BFF, microservico ou novo runtime sem fase explicita.
3. Evoluir o backend multi-escola e contratos internos de forma pontual, mantendo `develop` como fonte da verdade.
4. Atualizar Swagger/OpenAPI, exemplos de teste e esta fotografia sempre que o contrato real mudar.
5. Validar `npm run build` no host e no remoto afetado quando houver alteracao no frontend federado.

## Decisoes de produto atualizadas

- Nao existe pendencia de decisao sobre matricula/status/cancelamento para o MVP atual; o comportamento implementado e testado pelo cliente esta aceito.
- Historico, documentos e transferencia nao sao o proximo epico; fazem parte do ciclo funcional ja desenvolvido ate aqui.
- Permissoes granulares por rota, tela e endpoint serao tratadas no final do projeto para evitar retrabalho recorrente a cada nova funcionalidade.
- A proxima frente principal apos esta baseline deixa de ser a extracao inicial do frontend e passa a ser consolidacao incremental: multi-escola, contratos internos e evolucao arquitetural sem abrir BFFs ou microservicos nesta etapa.
