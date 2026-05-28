# projectc-school-management

Projeto de gestao escolar com backend Spring Boot, frontend Angular e documentacao de produto/arquitetura mantida no proprio repositorio.

## Estrutura atual

- `docs/16-roteiro-continuidade-frontend-backend.md`: roteiro ativo de continuidade.
- `docs/17-status-atual-do-projeto.md`: fotografia tecnica atual do projeto.
- `docs/18-url-para-testes.md`: checklist de testes HTTP manuais.
- `docs/19-proximo-epico-aulas-professores-notas-dashboards.md`: direcionamento do proximo epico funcional.
- `docs/20-base-dados-oficial-scriptdb.md`: documentacao da base oficial.
- `docs/v2/scriptdb.sql`: dump SQL oficial da base `gestao_escolar`.
- `docs/historico`: documentos e scripts antigos preservados apenas para consulta historica.
- `school-management-service`: API backend.
- `school-management-web/host`: aplicacao Angular principal.
- `school-management-web/microfrontend`: aplicacao Angular remota usada como base de microfrontend federado.

## Status atual

O MVP atual foi considerado aceito pelo cliente dentro dos parametros desejados. O projeto ja possui fluxo funcional cobrindo:

- autenticacao com login JWT, refresh e logout;
- administracao tecnica de usuarios, perfis e permissoes;
- cadastro de alunos;
- cadastro de responsaveis;
- vinculo entre aluno e responsavel;
- consulta cadastral consolidada;
- cadastro academico com periodos letivos, series, turnos e turmas;
- matricula de aluno em turma/periodo letivo, incluindo status/cancelamento conforme fluxo aceito;
- documentos de alunos;
- historico escolar;
- transferencia de alunos.

A matriz granular de permissoes por rota/tela/endpoint sera implementada no final do projeto, apos estabilizacao das funcionalidades principais.

## Base de dados oficial

A base oficial atual e `gestao_escolar`, representada por:

```text
docs/v2/scriptdb.sql
```

Use este arquivo como fonte para nomes de tabelas, colunas, constraints, relacionamentos e dados de apoio. A documentacao da decisao esta em:

```text
docs/20-base-dados-oficial-scriptdb.md
```

Scripts SQL e documentos que nao devem orientar a modelagem atual foram movidos para:

```text
docs/historico
```

## Backend

Local: `school-management-service`

Stack principal:

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Security;
- JWT;
- Spring Data JPA;
- PostgreSQL;
- Flyway;
- Springdoc OpenAPI.

Execucao local:

```bash
cd school-management-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

No Windows, tambem pode usar:

```bash
cd school-management-service
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Credenciais locais:

- login: `admin`
- senha: `admin123`

Endpoint de login:

```http
POST http://localhost:8080/api/auth/login
```

```json
{
  "login": "admin",
  "senha": "admin123"
}
```

## Frontend

Local: `school-management-web/host`

Execucao local:

```bash
cd school-management-web/host
npm install
npm start
```

Aplicacao remota:

```bash
cd school-management-web/microfrontend
npm install
npm start
```

Por padrao, os services Angular consomem o backend em `http://localhost:8080`.

## Proximo epico funcional

O proximo epico deve focar:

- aulas e planejamento de aulas;
- professores e relacao professor/turma/disciplina;
- alunos em contexto de aula;
- notas e acompanhamento de evolucao;
- comportamento de alunos e professores;
- dashboards de aulas, matriculas e evolucao dos alunos.

## Validacao

Backend:

```bash
cd school-management-service
./mvnw test
```

Frontend host:

```bash
cd school-management-web/host
npm run build
```
