# projectc-school-management

Projeto de gestao escolar com backend Spring Boot, frontend Angular e documentacao de produto/arquitetura mantida no proprio repositorio.

## Estrutura

- `docs`: planejamento, arquitetura, backlog, roteiros atuais e scripts SQL de apoio.
- `school-management-service`: API backend do MVP.
- `school-management-web/host`: aplicacao Angular principal.
- `school-management-web/microfrontend`: aplicacao Angular remota usada como base de microfrontend federado.

## Status atual

O projeto ja possui um fluxo MVP funcional cobrindo:

- autenticacao com login JWT, refresh e logout;
- administracao de usuarios, perfis e permissoes;
- cadastro de alunos;
- cadastro de responsaveis;
- vinculo entre aluno e responsavel;
- consulta cadastral consolidada;
- cadastro de periodos letivos e turmas;
- matricula de aluno em turma/periodo letivo.

IDs principais usam `UUID`. Scripts e exemplos antigos baseados em `BIGINT` foram removidos da documentacao ativa.

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

## Documentos principais

- `docs/00-gestao-escolar-base.md`: visao ampla do produto.
- `docs/01-mvp-gestao-escolar.md`: escopo do MVP.
- `docs/04-arquitetura-inicial.md`: arquitetura recomendada.
- `docs/06-sprint-1-backlog.md`: backlog inicial do MVP.
- `docs/10-convencoes-tecnicas-iniciais.md`: convencoes tecnicas.
- `docs/12-padroes-minimos-de-estrutura-e-responsabilidade.md`: responsabilidades por camada e modulo.
- `docs/15-plano-frontend-angular-material-microfrontend-federado.md`: diretriz do frontend.
- `docs/16-roteiro-continuidade-frontend-backend.md`: continuidade atual.
- `docs/17-status-atual-do-projeto.md`: fotografia tecnica atual do projeto.
- `docs/18-url-para-testes.md`: checklist de testes HTTP.

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
