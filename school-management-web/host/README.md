# school-management-web/host

Aplicacao Angular principal do sistema de gestao escolar.

## Papel no projeto

O host e a casca principal do frontend. A direcao arquitetural vigente e manter aqui somente seguranca, administracao tecnica e carregamento dos remotos federados:

- autenticacao/login;
- home/menu;
- guards e estado de sessao;
- usuarios, perfis e permissoes;
- integracao com os remotos federados de dominio.

As funcionalidades escolares ja foram destacadas para remotos federados por dominio, preservando contratos REST, autorizacao e rotas publicas.

Estado atual da migracao:

- `academic/periods`, `academic/series`, `academic/shifts`, `academic/classes` e `academic/disciplines` sao carregadas por `mfe-catalogo-academico`.
- `responsibles`, `responsibles/new`, `responsibles/:id` e `responsibles/:id/edit` sao carregadas por `mfe-responsaveis`.
- `students`, `students/new`, `students/:id` e `students/:id/edit` sao carregadas por `mfe-alunos`.
- `enrollment` e carregada por `mfe-matriculas`.
- `dashboard`, `dashboard/config` e `dashboard/snapshots` sao carregadas por `mfe-dashboard`.
- `planning`, `planning/:id` e `planning-library` sao carregadas por `mfe-planejamento-ia`.
- `teachers` e `teachers/:id` sao carregadas por `mfe-professores`.
- `lessons`, `lessons/:id`, `assessments` e `assessments/:id` sao carregadas por `mfe-aulas-avaliacoes`.
- Os pacotes escolares locais de catalogo, aluno, responsavel, matricula, documento e historico foram removidos do host apos migracao das rotas ativas.

Manifesto ativo de remotos:

```text
public/federation.manifest.json
```

## Backend e base oficial

Por padrao, os services apontam para:

```text
http://localhost:8080
```

Configuracao centralizada:

```text
src/app/core/config/api.config.ts
```

A base oficial do backend e:

```text
../../docs/v2/scriptdb.sql
```

Documento de referencia:

```text
../../docs/20-base-dados-oficial-scriptdb.md
```

## Desenvolvimento local

Para validar o fluxo federado completo, suba primeiro os remotos necessarios e depois o host.

```bash
npm install
npm start
```

Ports atuais esperados pelo manifesto:

```text
4202 -> mfe-matriculas
4203 -> mfe-catalogo-academico
4204 -> mfe-dashboard
4205 -> mfe-planejamento-ia
4206 -> mfe-professores
4207 -> mfe-aulas-avaliacoes
4208 -> mfe-responsaveis
4209 -> mfe-alunos
```

A aplicacao fica disponivel em:

```text
http://localhost:4200
```

Use `localhost` na validacao local. Nesta configuracao o dev-server pode escutar em IPv6 (`::1`), o que pode fazer `127.0.0.1` falhar mesmo com o servidor ativo.

## Build

```bash
npm run build
```

## Observacoes

- Este projeto usa Angular 20, Angular Material e Native Federation.
- O contexto de shell e publicado para microfrontends via `school-management.shell.context.v1` no `localStorage` e evento `school-management:shell-context-changed`.
- O contrato inclui API base, token de acesso, usuario, perfis e permissoes. O refresh token permanece interno ao host.
- As rotas federadas estaticas usam o helper unico `loadMfeComponent` em `src/app/app.routes.ts`.
- O contrato declarativo de rotas/menu fica em `src/app/core/shell/shell-navigation.config.ts`.
- O carregamento dinamico antigo por `AplicativosService` foi removido; o MVP usa o contrato declarativo do shell.
- O remoto legado `../microfrontend` foi removido do workspace.
- Nao recriar registro dinamico de remotos sem necessidade funcional validada; novas rotas devem entrar primeiro no contrato declarativo.
- Dados de negocio devem vir do backend; nao usar seeds locais como fonte funcional.
- Documentos/scripts antigos ficam em `../../docs/historico` apenas para consulta.
