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
- `catalogo/models` e `catalogo/services` permanecem temporariamente no host por dependencia da tela de matricula.
- `responsavel/models` e `responsavel/services` permanecem temporariamente no host por dependencia das telas de aluno.

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
- Dados de negocio devem vir do backend; nao usar seeds locais como fonte funcional.
- Documentos/scripts antigos ficam em `../../docs/historico` apenas para consulta.
