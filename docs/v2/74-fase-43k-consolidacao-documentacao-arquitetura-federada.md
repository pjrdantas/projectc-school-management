# Fase 43K - Consolidacao da documentacao da arquitetura federada

## Objetivo

Fechar a etapa de refatoracao federada do frontend com documentacao sincronizada ao estado real do repositorio.

## Estado atual do frontend

O frontend esta dividido em:

- `school-management-web/host`: shell principal.
- `school-management-web/microfrontend`: remoto federado com funcionalidades escolares de negocio.

## Responsabilidades do host

O host concentra:

- login;
- logout;
- refresh token;
- estado de sessao;
- publicacao do contrato de shell;
- menu/layout;
- guards;
- usuarios;
- perfis;
- permissoes;
- roteamento federado estatico.

O host nao contem mais pacotes locais de negocio escolar como catalogo, aluno, responsavel, matricula, documento ou historico.

## Responsabilidades do microfrontend

O microfrontend concentra:

- catalogo academico;
- responsaveis;
- alunos;
- documentos usados por aluno/responsavel;
- historico escolar e disciplinas;
- matricula.

## Contrato de shell

O host publica o contexto em:

```text
localStorage: school-management.shell.context.v1
evento: school-management:shell-context-changed
```

O contrato fornece para o microfrontend:

- URL base da API;
- token de acesso;
- usuario autenticado;
- perfis;
- permissoes.

O refresh token permanece interno ao host.

## Contrato declarativo de rotas/menu

As rotas federadas e itens de menu do MVP sao definidos em:

```text
school-management-web/host/src/app/core/shell/shell-navigation.config.ts
```

Esse arquivo centraliza:

- `SHELL_REMOTE_ROUTES`;
- `SHELL_BUSINESS_MENU`;
- `SHELL_ACCESS_MENU`.

Novas rotas federadas estaticas devem entrar primeiro nesse contrato.

## Exposes do microfrontend

O remoto publica:

- `./CatalogoPeriods`;
- `./CatalogoSeries`;
- `./CatalogoShifts`;
- `./CatalogoClasses`;
- `./ResponsavelList`;
- `./ResponsavelNew`;
- `./ResponsavelDetail`;
- `./AlunoList`;
- `./AlunoNew`;
- `./AlunoDetail`;
- `./Matricula`;
- `./HistoricoDisciplines`.

Nao existe mais expose `./Component` nem rota marcador `/microfrontend`.

## Rotas funcionais preservadas no host

- `students`;
- `students/new`;
- `students/:id`;
- `students/:id/edit`;
- `responsibles`;
- `responsibles/new`;
- `responsibles/:id`;
- `responsibles/:id/edit`;
- `academic/periods`;
- `academic/series`;
- `academic/shifts`;
- `academic/classes`;
- `academic/disciplines`;
- `enrollment`;
- `auth/users`;
- `auth/profiles`;
- `auth/permissions`.

## Remocoes consolidadas

- Tela marcador `microfrontend/src/app/pages/home`.
- Expose `./Component`.
- Rota `/microfrontend`.
- `AplicativosService` dinamico antigo.
- `AplicativosResponse`.
- Registro dinamico de rotas por `remoteEntry` no menu.

## Validacao

Validacoes previstas para esta fase:

```bash
cd school-management-web/microfrontend
npm run build
```

```bash
cd school-management-web/host
npm run build
```

Tambem deve ser validado que a documentacao ativa nao trata `/microfrontend`, `./Component` ou `AplicativosService` como mecanismos vigentes.

## Proxima fase sugerida

Retomar a evolucao funcional pelo microfrontend. A proxima frente natural e dashboard operacional/academico, agora consumindo o backend e o contrato de shell estabilizado.
