# school-management-web

Frontend Angular do projeto de gestao escolar.

## Estrutura

- `host`: shell principal, com login, menu, autenticacao, autorizacao e administracao de usuarios, perfis e permissoes.
- `microfrontend`: aplicacao remota que deve receber gradualmente as funcionalidades escolares de negocio.

## Stack

- Angular 20
- Angular Material
- Native Federation
- TypeScript
- RxJS

## Direcao arquitetural

O host deve permanecer como casca de seguranca e administracao tecnica:

- login, sessao e token;
- layout/menu;
- guards e regras de autorizacao;
- usuarios, perfis e permissoes;
- carregamento dos remotos federados.

As funcionalidades escolares devem ser migradas em fases para o microfrontend:

- catalogo (piloto ja iniciado no microfrontend);
- responsavel (telas ja migradas para o microfrontend);
- aluno;
- documento;
- historico;
- matricula;
- dashboard;
- demais dominios de negocio.

A matriz granular de permissoes por rota/tela/endpoint sera implementada no final do projeto.

## Integracao com backend

Os services HTTP consomem o backend em:

```text
http://localhost:8080
```

A URL base da API fica centralizada em:

```text
host/src/app/core/config/api.config.ts
```

A base oficial do backend e `gestao_escolar`, representada por:

```text
../docs/v2/scriptdb.sql
```

A documentacao ativa da base esta em:

```text
../docs/20-base-dados-oficial-scriptdb.md
```

## Executar o ambiente federado

Suba primeiro o microfrontend remoto e depois o host. Use `localhost`, pois o Angular dev-server pode publicar em IPv6 (`::1`) e nao responder a probes em `127.0.0.1`.

```bash
cd microfrontend
npm install
npm start
```

O remoto deve publicar:

```text
http://localhost:4201/remoteEntry.json
```

Em outro terminal:

```bash
cd host
npm install
npm start
```

O host deve publicar:

```text
http://localhost:4200
```

## Validacao

Build do host:

```bash
cd host
npm run build
```

Build do microfrontend:

```bash
cd microfrontend
npm run build
```

## Observacoes tecnicas

- A sessao usa `localStorage` para tokens e dados do usuario autenticado.
- O host publica o contrato de shell para os remotos na chave `school-management.shell.context.v1` e no evento `school-management:shell-context-changed`.
- As rotas `academic/periods`, `academic/series`, `academic/shifts` e `academic/classes` sao carregadas pelo microfrontend remoto.
- As rotas `responsibles`, `responsibles/new`, `responsibles/:id` e `responsibles/:id/edit` sao carregadas pelo microfrontend remoto.
- Dados de negocio usam o backend como fonte oficial; caches de tela ficam apenas em memoria durante a sessao.
- A URL da API esta centralizada em `core/config/api.config.ts`; uma melhoria futura e trocar esse valor por configuracao por ambiente quando houver necessidade de empacotamento/deploy fora do ambiente local.
- Documentos e scripts antigos possuem copias organizadas em `../docs/historico` e nao devem orientar novas implementacoes.
