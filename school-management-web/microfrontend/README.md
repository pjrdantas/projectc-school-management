# school-management-web/microfrontend

Aplicacao Angular remota usada como base de microfrontend federado no projeto de gestao escolar.

## Papel no projeto

Este app funciona como remoto federado para as funcionalidades escolares de negocio. A direcao arquitetural vigente e migrar para ca, em fases, dominios como catalogo, aluno, responsavel, documento, historico, matricula e dashboard.

O `host` deve permanecer como shell de seguranca, menu e administracao de usuarios, perfis e permissoes.

Estado atual:

- `catalogo/pages` foi migrado para este microfrontend;
- `CatalogoPeriods`, `CatalogoSeries`, `CatalogoShifts` e `CatalogoClasses` sao expostos via Native Federation;
- `responsavel/pages` foi migrado para este microfrontend;
- `ResponsavelList`, `ResponsavelNew` e `ResponsavelDetail` sao expostos via Native Federation;
- `aluno/pages` foi migrado para este microfrontend;
- `AlunoList`, `AlunoNew` e `AlunoDetail` sao expostos via Native Federation;
- `matricula/pages`, `matricula/models` e `matricula/services` foram migrados para este microfrontend;
- `Matricula` e exposto via Native Federation;
- `historico/pages/disciplines` e o suporte de historico usado por aluno estao neste microfrontend;
- `HistoricoDisciplines` e exposto via Native Federation;
- o service de catalogo usa o contrato de shell para obter API base e token.
- o service de responsavel e o service de documentos usados por responsavel tambem usam o contrato de shell.
- o service de aluno e o painel historico usado por aluno tambem usam o contrato de shell.
- o service de matricula usa o contrato de shell para obter API base e token.
- o service de historico usa o contrato de shell para obter API base e token.

## Desenvolvimento local

```bash
npm install
npm start
```

A aplicacao remota fica disponivel na porta configurada pelo projeto Angular/Native Federation.

No ambiente local atual, o remoto deve publicar:

```text
http://localhost:4201/remoteEntry.json
```

Suba este remoto antes do `../host` quando for validar a federacao completa.

## Contrato com o host

O remoto consome o contexto publicado pelo host em:

```text
localStorage: school-management.shell.context.v1
evento: school-management:shell-context-changed
```

O contrato fornece API base, token de acesso, usuario, perfis e permissoes. O microfrontend nao executa login proprio nem gerencia refresh token.

## Build

```bash
npm run build
```

## Referencias do projeto

- Host principal: `../host`
- Backend: `../../school-management-service`
- Base oficial: `../../docs/v2/scriptdb.sql`
- Documentacao da base oficial: `../../docs/20-base-dados-oficial-scriptdb.md`
- Historico de documentos/scripts antigos: `../../docs/historico`
