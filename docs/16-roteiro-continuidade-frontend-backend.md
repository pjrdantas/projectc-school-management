# 16 - Roteiro de Continuidade - Frontend + Backend

## Objetivo

Registrar o ponto atual do projeto e orientar a proxima evolucao do MVP de gestao escolar considerando o estado real do repositorio.

Este documento substitui roteiros antigos que ainda tratavam o backend como inexistente, o CRUD de aluno como local ou a migracao UUID como pendente.

## Estado atual consolidado

### Backend

O backend `school-management-service` ja existe e esta organizado como monolito modular Spring Boot.

Modulos atuais:

- `accesscontrol`: autenticacao, sessoes, usuarios, perfis e permissoes;
- `studentmanagement`: cadastro de alunos;
- `responsavelmanagement`: cadastro de responsaveis, vinculo aluno-responsavel e consulta cadastral;
- `academiccatalog`: periodos letivos e turmas;
- `enrollment`: matriculas;
- `shared`: tratamento global de erros e estruturas comuns.

Principais endpoints:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET|POST|PUT|DELETE /api/usuarios`
- `GET|POST|PUT|DELETE /api/perfis`
- `GET|POST|PUT|DELETE /api/permissoes`
- `GET|POST|PUT|DELETE /api/alunos`
- `GET|POST|PUT|DELETE /api/responsaveis`
- `GET|POST|DELETE /api/alunos/{idAluno}/responsaveis`
- `GET /api/consulta-cadastral`
- `GET|POST /api/periodos-letivos`
- `GET|POST /api/turmas`
- `GET|POST /api/matriculas`

### Banco de dados

O projeto usa PostgreSQL com Flyway.

O schema atual trabalha com `UUID` nos identificadores principais. A migracao `V007__migrate_bigint_to_uuid.sql` ja faz parte do historico Flyway, portanto documentos ativos nao devem orientar novas implementacoes com IDs numericos.

### Frontend

O frontend `school-management-web/host` ja possui rotas e services HTTP para:

- login;
- home/menu;
- alunos;
- responsaveis;
- periodos letivos;
- turmas;
- matriculas;
- usuarios;
- perfis;
- permissoes.

Tambem existe `school-management-web/microfrontend` como aplicacao remota federada.

Os services do host consomem o backend em `http://localhost:8080`.

## Proxima evolucao recomendada

### 1. Consolidar documentacao operacional

Atualizar continuamente:

- `README.md`;
- `school-management-service/README.md`;
- `school-management-web/README.md`;
- `docs/17-status-atual-do-projeto.md`;
- `docs/18-url-para-testes.md`.

### 2. Planejar o proximo epico funcional

O MVP atual foi considerado aceito pelo cliente dentro dos parametros desejados. A proxima evolucao funcional deve focar:

1. aulas e planejamento de aulas;
2. professores e relacao professor/turma/disciplina;
3. alunos em contexto de aula;
4. notas e acompanhamento de evolucao;
5. comportamento de alunos e professores;
6. dashboards de aulas, matriculas e evolucao dos alunos.

Historico, documentos e transferencia nao sao o proximo epico; fazem parte do ciclo funcional ja desenvolvido ate aqui.

### 3. Melhorar experiencia operacional

Prioridade sugerida:

- ampliar padronizacao de mensagens de erro no Angular quando surgirem novos fluxos;
- componente comum para estados de carregamento, vazio e erro;
- manter documentacao atualizada quando o contrato real mudar.

### 4. Fortalecer qualidade tecnica

Prioridade sugerida:

- garantir `mvnw test` verde no backend;
- garantir `npm run build` no host;
- revisar Swagger/OpenAPI quando endpoints mudarem;
- evoluir `API_BASE_URL` centralizado para configuracao por ambiente quando houver necessidade de empacotamento/deploy;
- documentar variaveis de ambiente/configuracao do frontend.

Permissoes granulares por rota, tela e endpoint ficam planejadas para o final do projeto, depois que as funcionalidades principais estiverem estabilizadas.

## Itens fora de contexto removidos da documentacao ativa

Foram retirados da documentacao ativa:

- roteiro antigo de KAN-33, pois o cadastro de aluno ja existe;
- roteiro antigo de KAN-3, pois responsavel/vinculo/consulta cadastral ja existem no backend;
- roteiro de migracao BIGINT para UUID, pois o projeto atual ja opera com UUID;
- script SQL legado baseado em BIGINT.

## Prompt sugerido para continuidade

```text
Contexto: o projeto projectc-school-management ja possui backend Spring Boot, frontend Angular host/microfrontend, autenticacao JWT, CRUDs principais, UUID e Flyway.

Objetivo: evoluir o produto a partir do MVP aceito pelo cliente.

Prioridade:
1) planejar e implementar o epico de aulas, professores, alunos, notas, comportamento e dashboards;
2) manter historico/documentos/transferencia como parte do ciclo ja desenvolvido, refinando apenas mediante demanda explicita;
3) deixar permissoes granulares para o final do projeto;
4) atualizar documentacao quando o contrato real mudar;
5) executar testes/builds relevantes antes de encerrar.
```
