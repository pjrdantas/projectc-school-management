# Fase 44 - Planejamento do epico pedagogico no microfrontend

## Objetivo

Retomar a evolucao funcional apos a consolidacao do dashboard/Home e organizar as proximas entregas em fases pequenas, iniciando pelo frontend dos fluxos pedagogicos que ja possuem suporte no backend.

## Contexto atual

A arquitetura federada esta estabilizada:

- `school-management-web/host` permanece responsavel por autenticacao, sessao, menu, permissoes e roteamento federado.
- `school-management-web/microfrontend` concentra as funcionalidades escolares.
- A Home atual consome o pacote agregado de dashboard via `GET /api/dashboard/frontend`.

O backend ja possui dominios e endpoints para:

- professores;
- vinculo professor/turma/disciplina;
- aulas;
- frequencia de professor;
- frequencia de alunos;
- avaliacoes;
- notas;
- dashboards de secretaria, diretor e professor.

O microfrontend ainda nao possui telas operacionais para professores, aulas, frequencia e avaliacoes/notas.

## Principio de execucao

As proximas entregas devem ser feitas etapa por etapa, sem misturar refatoracoes amplas com uma tela funcional.

Ao final de cada etapa, registrar:

- o que foi implementado;
- se a mudanca foi frontend, backend ou ambos;
- quais validacoes foram executadas;
- qual deve ser a proxima fase.

## Fase 44A - Frontend de professores

### Objetivo

Criar a primeira area operacional do epico pedagogico: cadastro, listagem, detalhe e manutencao de professores no microfrontend.

### Escopo

- Criar dominio frontend `professor` no microfrontend.
- Criar lista de professores.
- Criar cadastro/edicao em modal ou tela conforme padrao atual.
- Criar detalhe do professor.
- Integrar com `GET /api/professores`, `POST /api/professores` e `GET /api/professores/{id}`.
- Expor rotas federadas no `federation.config.js`.
- Registrar rotas no contrato do host em `SHELL_REMOTE_ROUTES`.
- Adicionar item de menu por perfil.

### Perfis sugeridos

- `ADMIN`: acesso total.
- `DIRETOR`: consulta e acompanhamento.
- `SECRETARIA`: cadastro/manutencao operacional, se fizer sentido no uso real.
- `PROFESSOR`: nao deve gerenciar cadastro de professores nesta fase.

### Validacao

- `npm run build` em `school-management-web/microfrontend`.
- `npm run build` em `school-management-web/host`, se o contrato de rotas/menu for alterado.

### Proxima fase apos concluir

Avancar para a Fase 44B, vinculando professores a turmas e disciplinas.

## Fase 44B - Vinculo professor, turma e disciplina

### Objetivo

Permitir que a escola defina quais professores atuam em quais turmas e disciplinas.

### Escopo

- Criar UI para listar vinculos de um professor.
- Criar UI para adicionar vinculo professor/turma/disciplina.
- Integrar com:
  - `POST /api/professores/{id}/turmas-disciplinas`;
  - `GET /api/professores/{id}/turmas-disciplinas`;
  - `GET /api/turmas/{turmaId}/professores`.
- Reaproveitar listas existentes de turmas e disciplinas.
- Preferir `select matNativeControl` em modal federada se `mat-select` voltar a apresentar problema de overlay.

### Validacao

- `npm run build` no microfrontend.
- `npm run build` no host se houver menu/rota nova.

### Proxima fase apos concluir

Avancar para a Fase 44C, criando o frontend de aulas.

## Fase 44C - Frontend de aulas

### Objetivo

Criar a rotina inicial para planejar/registrar aulas por professor, turma e disciplina.

### Escopo

- Criar lista de aulas com filtros por professor, turma, disciplina e data.
- Criar cadastro de aula.
- Criar detalhe da aula.
- Integrar com `GET /api/aulas`, `POST /api/aulas` e `GET /api/aulas/{id}`.
- Expor rotas federadas e item de menu.

### Perfis sugeridos

- `ADMIN`: acesso total.
- `DIRETOR`: consulta.
- `SECRETARIA`: consulta operacional.
- `PROFESSOR`: consulta e registro das suas aulas quando o vinculo usuario/professor estiver resolvido.

### Validacao

- `npm run build` no microfrontend.
- `npm run build` no host se houver menu/rota nova.

### Proxima fase apos concluir

Avancar para a Fase 44D, registrando frequencia do professor e frequencia dos alunos.

## Fase 44D - Frequencia em aula

### Objetivo

Registrar a execucao real das aulas, incluindo frequencia do professor e frequencia dos alunos.

### Escopo

- Na tela de detalhe da aula, adicionar registro de frequencia do professor.
- Na tela de detalhe da aula, listar alunos da turma/matriculas elegiveis.
- Registrar frequencia dos alunos.
- Integrar com:
  - `POST /api/aulas/{id}/frequencia-professor`;
  - `GET /api/aulas/{id}/frequencia-professor`;
  - `POST /api/aulas/{id}/frequencias-alunos`;
  - `GET /api/aulas/{id}/frequencias-alunos`.

### Validacao

- `npm run build` no microfrontend.
- `npm run build` no host se houver menu/rota nova.

### Proxima fase apos concluir

Avancar para a Fase 44E, criando avaliacoes e lancamento de notas.

## Fase 44E - Avaliacoes e notas

### Objetivo

Permitir o acompanhamento inicial de desempenho academico por avaliacao e por aluno.

### Escopo

- Criar lista de avaliacoes.
- Criar cadastro de avaliacao por turma/disciplina.
- Criar tela ou secao para lancamento de notas.
- Integrar com:
  - `GET /api/avaliacoes`;
  - `POST /api/avaliacoes`;
  - `GET /api/avaliacoes/{id}`;
  - `POST /api/avaliacoes/{id}/notas`;
  - `GET /api/avaliacoes/{id}/notas`;
  - `GET /api/matriculas/{matriculaId}/notas`.

### Validacao

- `npm run build` no microfrontend.
- `npm run build` no host se houver menu/rota nova.

### Proxima fase apos concluir

Avancar para a Fase 44F, tornando o dashboard do professor funcional com dados reais do usuario logado.

## Fase 44F - Dashboard do professor com vinculo real

### Objetivo

Resolver o ponto pendente da Home do professor: identificar o `professorId` correspondente ao usuario autenticado.

### Escopo

- Definir se o vinculo usuario/professor ja existe no schema atual ou se precisa ser criado.
- Se exigir backend, implementar de forma controlada e com migration Flyway incremental justificada.
- Ajustar o contrato de autenticacao ou endpoint auxiliar para o microfrontend descobrir o professor do usuario.
- Consumir `GET /api/dashboard/frontend?publicoCodigo=PROFESSOR&professorId={professorId}`.
- Trocar a mensagem pendente da Home do professor por indicadores reais.

### Validacao

- Se tocar backend: `.\mvnw.cmd test` em `school-management-service`.
- Se tocar frontend: `npm run build` no microfrontend.
- Se tocar host/contrato: `npm run build` no host.

### Proxima fase apos concluir

Avaliar se a proxima frente deve ser personalizacao de dashboards por usuario ou telas administrativas de configuracao de dashboards.

## Fora deste bloco

Nao entram neste planejamento imediato:

- matriz completa de permissoes granulares por endpoint/tela;
- financeiro;
- notificacoes multicanal;
- relatorios gerenciais avancados fora dos dashboards iniciais;
- refatoracoes amplas de nomenclatura ou estrutura sem necessidade funcional.

## Proxima decisao

A proxima fase recomendada e iniciar pela Fase 44A - Frontend de professores.
