# school-management-web

Frontend Angular do projeto de gestao escolar.

## Estrutura

- `host`: shell principal, com login, menu, autenticacao, autorizacao e administracao de usuarios, perfis e permissoes.
- `mfe-matriculas`: remoto do dominio de matriculas.
- `mfe-catalogo-academico`: remoto do dominio de catalogo academico.
- `mfe-dashboard`: remoto do dominio de dashboards.
- `mfe-planejamento-ia`: remoto do dominio de planejamento e IA.
- `mfe-professores`: remoto do dominio de professores.
- `mfe-aulas-avaliacoes`: remoto do dominio de aulas e avaliacoes.
- `mfe-responsaveis`: remoto do dominio de responsaveis.
- `mfe-alunos`: remoto do dominio de alunos.

## Stack

- Angular 20
- Angular Material
- Native Federation
- TypeScript
- RxJS

## Direcao arquitetural

O host permanece como casca de seguranca e administracao tecnica:

- login, sessao e token;
- layout/menu;
- guards e regras de autorizacao;
- usuarios, perfis e permissoes;
- carregamento dos remotos federados.

As funcionalidades escolares foram separadas em remotos federados por dominio:

- catalogo academico;
- responsaveis;
- alunos;
- matriculas;
- historico/disciplinas dentro do remoto de catalogo academico;
- documentos usados por aluno/responsavel dentro dos remotos de alunos e responsaveis;
- dashboards;
- planejamento/IA;
- professores;
- aulas e avaliacoes.

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

Suba o backend em `http://localhost:8080`, depois os remotos que voce vai usar e por ultimo o host. Use `localhost`, pois o Angular dev-server pode publicar em IPv6 (`::1`) e nao responder a probes em `127.0.0.1`.

Para subir tudo sem excecao:

```powershell
cd C:\Projeto\git\projectc-school-management\school-management-web
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-frontends.ps1
```

O script sobe:

- `host`
- `mfe-matriculas`
- `mfe-catalogo-academico`
- `mfe-dashboard`
- `mfe-planejamento-ia`
- `mfe-professores`
- `mfe-aulas-avaliacoes`
- `mfe-responsaveis`
- `mfe-alunos`

Se algum projeto estiver sem `node_modules`, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-frontends.ps1 -InstallMissing
```

Para encerrar tudo que foi iniciado pelo launcher:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-all-frontends.ps1
```

Para encerrar e limpar tambem os logs/PIDs locais:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-all-frontends.ps1 -ClearLogs
```

```bash
cd mfe-dashboard
npm install
npm start
```

O host consome o manifesto ativo em `host/public/federation.manifest.json`, que hoje aponta para:

```text
mfe-matriculas -> http://localhost:4202/remoteEntry.json
mfe-catalogo-academico -> http://localhost:4203/remoteEntry.json
mfe-dashboard -> http://localhost:4204/remoteEntry.json
mfe-planejamento-ia -> http://localhost:4205/remoteEntry.json
mfe-professores -> http://localhost:4206/remoteEntry.json
mfe-aulas-avaliacoes -> http://localhost:4207/remoteEntry.json
mfe-responsaveis -> http://localhost:4208/remoteEntry.json
mfe-alunos -> http://localhost:4209/remoteEntry.json
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

Build de um remoto de dominio:

```bash
cd mfe-dashboard
npm run build
```

## Observacoes tecnicas

- A sessao usa `localStorage` para tokens e dados do usuario autenticado.
- O host publica o contrato de shell para os remotos na chave `school-management.shell.context.v1` e no evento `school-management:shell-context-changed`.
- As rotas e menus federados do MVP sao definidos pelo contrato declarativo do shell no host.
- A rota marcador `/microfrontend`, o expose `./Component` e o carregamento dinamico antigo por `AplicativosService` foram removidos.
- O remoto legado `microfrontend` foi descontinuado e removido do workspace.
- As rotas federadas ativas agora sao atendidas pelos remotos `mfe-*` do manifesto do host.
- Dados de negocio usam o backend como fonte oficial; caches de tela ficam apenas em memoria durante a sessao.
- A URL da API esta centralizada em `core/config/api.config.ts`; uma melhoria futura e trocar esse valor por configuracao por ambiente quando houver necessidade de empacotamento/deploy fora do ambiente local.
- Documentos e scripts antigos possuem copias organizadas em `../docs/historico` e nao devem orientar novas implementacoes.
