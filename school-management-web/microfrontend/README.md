# school-management-web/microfrontend

Aplicacao Angular remota usada como base de microfrontend federado no projeto de gestao escolar.

## Papel no projeto

Este app funciona como remoto para evolucoes federadas. A aplicacao principal continua sendo o `host`.

## Desenvolvimento local

```bash
npm install
npm start
```

A aplicacao remota fica disponivel na porta configurada pelo projeto Angular/Native Federation.

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
