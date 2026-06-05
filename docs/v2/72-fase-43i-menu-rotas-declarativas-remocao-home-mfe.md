# Fase 43I - Menu/rotas declarativas e remocao da home marcador do microfrontend

## Objetivo

Reduzir duplicacao entre menu e rotas federadas do host, e remover a tela antiga `pages/home` do microfrontend, que existia apenas para indicar que o remoto estava ativo.

## Alteracoes realizadas

- Criado `host/src/app/core/shell/shell-navigation.config.ts`.
- Centralizadas nesse arquivo:
  - rotas federadas estaticas do shell;
  - itens de menu de negocio;
  - itens de menu de acesso.
- `host/src/app/app.routes.ts` passou a gerar as rotas remotas a partir de `SHELL_REMOTE_ROUTES`.
- `host/src/app/menu/pages/menu` passou a renderizar os menus de negocio e acesso a partir de listas declarativas.
- Removidos metodos de navegacao por item que duplicavam rotas no `Menu`.
- Removida a rota `/microfrontend` do host.
- Removido o expose `./Component` do microfrontend.
- Removida a pasta `microfrontend/src/app/pages/home`.
- O `App` do microfrontend passou a renderizar `router-outlet`.
- A rota raiz do microfrontend redireciona para `academic/periods`, uma rota funcional real.

## Rotas preservadas

Foram preservadas as rotas publicas funcionais:

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

## Rota removida

- `microfrontend`

Essa rota carregava somente a tela marcador do remoto e nao representava uma funcionalidade real.

## Validacao

Validacoes previstas:

```bash
cd school-management-web/microfrontend
npm run build
```

```bash
cd school-management-web/host
npm run build
```

Tambem deve ser validado por busca que nao restam referencias a `./Component`, `/microfrontend`, `irMicrofrontend`, `pages/home` ou `app-home` no microfrontend/host, exceto a home real do host.

## Proxima fase sugerida

Revisar o `AplicativosService` dinamico para decidir se ele continua no MVP ou se sera substituido integralmente pelo contrato declarativo do shell.
