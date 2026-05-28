# school-management-web

Frontend Angular do projeto de gestao escolar.

## Estrutura

- `host`: aplicacao principal, com login, menu e telas do MVP.
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
- turmas;
- matriculas;
- usuarios;
- perfis;
- permissoes;
- rota para microfrontend remoto.

Os services HTTP consomem o backend em:

```text
http://localhost:8080
```

A URL base da API fica centralizada em:

```text
host/src/app/core/config/api.config.ts
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
- A URL da API esta centralizada em `core/config/api.config.ts`; uma melhoria futura e trocar esse valor por configuracao por ambiente.
