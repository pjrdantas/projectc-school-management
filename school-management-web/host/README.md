# school-management-web/host

Aplicacao Angular principal do sistema de gestao escolar.

## Papel no projeto

O host concentra as telas operacionais atuais:

- autenticacao/login;
- home/menu;
- alunos;
- responsaveis;
- periodos letivos, series, turnos e turmas;
- matriculas;
- documentos, historico e transferencia;
- usuarios, perfis e permissoes tecnicas;
- integracao com o microfrontend remoto.

A matriz granular de permissoes por rota/tela/endpoint sera implementada no final do projeto, apos estabilizacao das funcionalidades principais.

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

```bash
npm install
npm start
```

A aplicacao fica disponivel em:

```text
http://localhost:4200
```

## Build

```bash
npm run build
```

## Observacoes

- Este projeto usa Angular 20, Angular Material e Native Federation.
- Dados de negocio devem vir do backend; nao usar seeds locais como fonte funcional.
- Documentos/scripts antigos ficam em `../../docs/historico` apenas para consulta.
