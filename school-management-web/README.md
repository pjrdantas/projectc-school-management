# school-management-web

Frontend Angular do projeto de gestao escolar.

## Estrutura

- `host`: aplicacao principal, com login, menu e telas funcionais do sistema.
- `microfrontend`: aplicacao remota usada como base para federacao.

## Stack

- Angular 20
- Angular Material
- Native Federation
- TypeScript
- RxJS

## Funcionalidades atuais do host

- login;
- home/menu;
- alunos;
- responsaveis;
- periodos letivos;
- series;
- turnos/turmas;
- matriculas;
- documentos, historico e transferencia dentro do ciclo ja desenvolvido;
- usuarios;
- perfis;
- permissoes;
- rota para microfrontend remoto.

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

## Executar o host

```bash
cd host
npm install
npm start
```

## Executar o microfrontend

```bash
cd microfrontend
npm install
npm start
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
- Dados de negocio usam o backend como fonte oficial; caches de tela ficam apenas em memoria durante a sessao.
- A URL da API esta centralizada em `core/config/api.config.ts`; uma melhoria futura e trocar esse valor por configuracao por ambiente quando houver necessidade de empacotamento/deploy fora do ambiente local.
- Documentos e scripts antigos possuem copias organizadas em `../docs/historico` e nao devem orientar novas implementacoes.
