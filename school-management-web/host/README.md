# school-management-web/host

Aplicacao Angular principal do sistema de gestao escolar.

## Papel no projeto

O host e a casca principal do frontend. A direcao arquitetural vigente e manter aqui somente seguranca, administracao tecnica e carregamento dos remotos federados:

- autenticacao/login;
- home/menu;
- guards e estado de sessao;
- usuarios, perfis e permissoes;
- integracao com o microfrontend remoto.

As telas escolares existentes devem ser migradas gradualmente para o microfrontend, preservando contratos REST, autorizacao e rotas publicas ate que cada fase seja validada.

Estado atual da migracao:

- `academic/periods`, `academic/series`, `academic/shifts` e `academic/classes` sao carregadas pelo microfrontend remoto.
- `responsibles`, `responsibles/new`, `responsibles/:id` e `responsibles/:id/edit` sao carregadas pelo microfrontend remoto.
- `students`, `students/new`, `students/:id` e `students/:id/edit` sao carregadas pelo microfrontend remoto.
- `enrollment` e carregada pelo microfrontend remoto.
- `academic/disciplines` e carregada pelo microfrontend remoto.
- Os pacotes escolares locais de catalogo, aluno, responsavel, matricula, documento e historico foram removidos do host apos migracao das rotas ativas.

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

Para validar o fluxo federado completo, suba primeiro `../microfrontend` e depois o host.

```bash
npm install
npm start
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
- O carregamento dinamico de aplicativos federados permanece em `AplicativosService` e `menu/pages/menu`.
- Dados de negocio devem vir do backend; nao usar seeds locais como fonte funcional.
- Documentos/scripts antigos ficam em `../../docs/historico` apenas para consulta.
