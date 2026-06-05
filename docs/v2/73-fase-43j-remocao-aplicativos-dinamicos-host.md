# Fase 43J - Remocao do carregamento dinamico antigo de aplicativos

## Objetivo

Remover do host o mecanismo antigo de aplicativos dinamicos, que ficou sem uso apos a centralizacao das rotas e menus no contrato declarativo do shell.

## Contexto

O `AplicativosService` retornava sempre uma lista vazia e nao havia mais renderizacao de aplicativos dinamicos no menu. As rotas federadas funcionais ja estavam registradas em `SHELL_REMOTE_ROUTES`, e os itens de menu vinham de `SHELL_BUSINESS_MENU` e `SHELL_ACCESS_MENU`.

## Alteracoes realizadas

- Removido `host/src/app/services/aplicativos.service.ts`.
- Removido `host/src/app/models/aplicativos-response.model.ts`.
- Removido do `Menu`:
  - injecao de `AplicativosService`;
  - import de `AplicativosResponse`;
  - import de `loadRemoteModule` para registro dinamico;
  - `MatSnackBarModule` usado apenas no fluxo dinamico antigo;
  - registro dinamico de rotas;
  - fallback por `remoteEntry`;
  - metodos `navegarApp` e `getIconeApp`.
- Removidas as pastas vazias `host/src/app/models` e `host/src/app/services`.

## Itens preservados

- Rotas federadas estaticas continuam em `SHELL_REMOTE_ROUTES`.
- Itens de menu de negocio continuam em `SHELL_BUSINESS_MENU`.
- Itens de menu de acesso continuam em `SHELL_ACCESS_MENU`.
- Login, logout, refresh token, usuarios, perfis e permissoes continuam no host.
- O contrato `school-management.shell.context.v1` continua publicado pelo host.

## Validacao

Validacoes previstas:

```bash
cd school-management-web/host
npm run build
```

Tambem deve ser validado por busca que nao restam referencias a:

- `AplicativosService`;
- `AplicativosResponse`;
- `aplicativosAtivos`;
- `registrarRotasMicrofrontend`;
- `loadRemoteModule({ remoteEntry, exposedModule })`.

## Proxima fase sugerida

Auditar a documentacao e os READMEs para fechar a etapa de arquitetura federada antes de voltar para funcionalidades novas de negocio ou dashboard.
