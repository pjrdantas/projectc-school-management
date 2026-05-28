# 17 - Status Atual do Projeto

## Objetivo

Registrar uma fotografia tecnica do estado atual do projeto `projectc-school-management`.

Use este documento como ponto de partida para proximas conversas e para evitar retomadas com informacoes antigas.

## Estrutura do repositorio

```text
projectc-school-management/
  docs/
  school-management-service/
  school-management-web/
    host/
    microfrontend/
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
| `academiccatalog` | Periodos letivos e turmas |
| `enrollment` | Matriculas |
| `shared` | Excecoes, respostas de erro e componentes comuns |

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
- `GET /api/turmas`
- `GET /api/turmas/{id}`
- `POST /api/turmas`

#### Matriculas

- `GET /api/matriculas`
- `POST /api/matriculas`

Filtros de listagem:

- `alunoId`
- `turmaId`
- `periodoLetivoId`
- `status`

## Banco de dados

O projeto usa Flyway em `school-management-service/src/main/resources/db/migration`.

Migrations existentes:

- `V001__initial_baseline.sql`
- `V002__create_table_aluno.sql`
- `V003__create_table_periodo_letivo.sql`
- `V004__create_table_turma.sql`
- `V005__create_table_matricula.sql`
- `V006__add_column_telefone_to_aluno.sql`
- `V007__migrate_bigint_to_uuid.sql`
- `V008__create_table_responsavel.sql`
- `V009__create_table_aluno_responsavel.sql`
- `V010__create_access_control_tables.sql`
- `V011__add_access_token_columns_to_sessao_autenticacao.sql`
- `V012__seed_default_admin_user.sql`
- `V013__normalize_default_admin_password.sql`
- `V014__rename_usuario_permission_to_admin.sql`
- `V015__ensure_default_admin_access.sql`
- `V016__set_defaults_for_join_table_ids.sql`

Regra atual: exemplos, contratos e novas implementacoes devem usar `UUID` para IDs principais.

## Frontend

Projeto: `school-management-web`

Stack:

- Angular 20;
- Angular Material;
- Native Federation;
- TypeScript;
- RxJS.

### Aplicacoes

- `host`: aplicacao principal.
- `microfrontend`: aplicacao remota.

### Rotas principais do host

- `/login`
- `/home`
- `/students`
- `/students/new`
- `/students/:id`
- `/students/:id/edit`
- `/responsibles`
- `/responsibles/new`
- `/responsibles/:id`
- `/responsibles/:id/edit`
- `/academic/periods`
- `/academic/classes`
- `/enrollment`
- `/auth/users`
- `/auth/profiles`
- `/auth/permissions`
- `/microfrontend`

### Integracoes HTTP atuais

Services Angular consomem a URL base centralizada em `school-management-web/host/src/app/core/config/api.config.ts`, atualmente apontando para `http://localhost:8080`, para:

- autenticacao;
- alunos;
- responsaveis;
- periodos/turmas;
- matriculas;
- usuarios/perfis/permissoes.

Ainda existe uso de `localStorage` para sessao/autenticacao. Dados de negocio como alunos e responsaveis usam o backend como fonte oficial, com cache apenas em memoria durante a sessao.

### Ajustes tecnicos ja consolidados

- URL base da API centralizada em `core/config/api.config.ts`.
- Mensagens de erro dos fluxos principais passam a aproveitar o envelope de erro do backend.
- Cache persistente em `localStorage` removido dos dados de negocio de alunos e responsaveis.
- Regra frontend de permissao centralizada com suporte a permissao `ADMIN`.
- Tela de secretaria/matriculas evoluida com resumo operacional, filtros avancados e consulta sem acoes locais que nao existem no backend.
- Matricula permite cadastro rapido de novo aluno; responsavel e obrigatorio apenas para aluno menor de 18 anos.

## Testes existentes

O backend possui testes de integracao para:

- alunos;
- responsaveis;
- vinculo aluno-responsavel;
- catalogo academico;
- matriculas;
- contexto Spring Boot.

## Proximas prioridades recomendadas

1. Evoluir configuracao da URL da API para ambiente.
2. Revisar permissoes por rota, tela e endpoint.
3. Decidir se matricula tera atualizacao/cancelamento no backend ou apenas consulta/criacao no MVP.
4. Atualizar Swagger/OpenAPI e exemplos de teste sempre que o contrato mudar.
5. Manter este documento sincronizado a cada marco funcional.
