# Fase 46A - Backend de planejamento bimestral

## Objetivo

Criar a API funcional de planejamento bimestral, mantendo o backend monolitico
atual e sem iniciar BFF, IA real, microservicos ou revisao ampla de permissoes.

## Escopo realizado

- Criado controller REST de planejamentos bimestrais.
- Criados DTOs de request/response para:
  - planejamento bimestral;
  - aula prevista;
  - avaliacao prevista;
  - alteracao de status.
- Criado service transacional de planejamento bimestral.
- Criada listagem com filtros por professor, turma, disciplina e periodo
  avaliativo.
- Criada busca por ID.
- Criada criacao e atualizacao do planejamento bimestral.
- Criada inclusao de aulas previstas.
- Criada inclusao de avaliacoes previstas.
- Criada alteracao de status para os codigos cadastrados em
  `status_planejamento`.
- Adicionado tratamento HTTP para erros de planejamento.
- Criado teste de integracao cobrindo o fluxo principal.

## Endpoints adicionados

- `POST /api/planejamentos-bimestrais`
- `GET /api/planejamentos-bimestrais`
- `GET /api/planejamentos-bimestrais/{id}`
- `PUT /api/planejamentos-bimestrais/{id}`
- `POST /api/planejamentos-bimestrais/{id}/aulas-previstas`
- `POST /api/planejamentos-bimestrais/{id}/avaliacoes-previstas`
- `PATCH /api/planejamentos-bimestrais/{id}/status`

## Regras implementadas

- Planejamento bimestral exige alocacao professor/turma/disciplina existente.
- Periodo avaliativo e opcional, mas quando informado deve existir.
- Novo planejamento inicia como `RASCUNHO`.
- Aula prevista nao pode repetir `numeroAula` no mesmo planejamento.
- Avaliacao prevista exige tipo de avaliacao existente.
- Status informado deve existir em `status_planejamento`.
- Ao alterar status para `APROVADO`, o planejamento passa a marcar
  `aprovadoPeloProfessor=true` e registra `dataAprovacao`.

## Ajustes tecnicos pontuais

- Removidos `unique=true` globais do mapeamento JPA de:
  - `PlanejamentoBimestralEntity.temaPrincipal`;
  - `PlanejamentoBimestralAulaEntity.numeroAula`.
- O ajuste alinha o mapeamento JPA ao modelo SQL, que define unicidade por
  contexto do planejamento, nao global.

## Fora do escopo

- Chamada real de IA.
- Frontend.
- BFF.
- Separacao em servicos.
- Kafka, MongoDB ou Redis.
- Regras finais de acesso por professor logado.
- Revisao ampla de permissoes.

## Validacao

Comando executado em `school-management-service`:

```powershell
.\mvnw.cmd test
```

Resultado:

- 93 testes executados.
- 0 falhas.
- 0 erros.

## Proxima fase

Fase 46B - Frontend de planejamento bimestral.
