# Fase 43H - Auditoria e limpeza do shell host

## Objetivo

Auditar o host apos a migracao das telas escolares para o microfrontend e reduzir residuos locais sem alterar rotas publicas, contrato de shell ou configuracoes de build.

## Alteracoes realizadas

- Consolidado o carregamento dos componentes remotos estaticos em um unico helper `loadMfeComponent`.
- Removidos os helpers repetidos por dominio em `host/src/app/app.routes.ts`.
- Mantidas as rotas publicas existentes:
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
  - `microfrontend`.
- Removido o componente `MessageDialogComponent` do host, pois ficou sem referencias depois da migracao dos fluxos escolares para o microfrontend.
- Mantidos `HasPermissionDirective`, `permission.util`, services de seguranca e `AplicativosService`, pois ainda fazem parte do shell, autorizacao e carregamento federado dinamico.

## Itens preservados

- Login, logout e refresh token continuam no host.
- Usuarios, perfis e permissoes continuam no host.
- O contrato `school-management.shell.context.v1` continua publicado pelo host.
- O carregamento dinamico por `AplicativosService` foi preservado como infraestrutura de shell.
- Nenhuma configuracao Maven, Flyway, backend ou Native Federation foi revertida.

## Validacao

Validacoes previstas:

```bash
cd school-management-web/host
npm run build
```

Tambem deve ser validado por busca que nao restaram referencias para `MessageDialogComponent` nem para os helpers remotos removidos.

## Proxima fase sugerida

Definir o contrato de menu/rotas do shell em uma estrutura declarativa, para reduzir duplicacao entre menu, rotas estaticas federadas e documentacao.
