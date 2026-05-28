# Consolidacao frontend v2

Data: 2026-05-26

## Objetivo

Reduzir risco tecnico antes de continuar a reforma do frontend.

O frontend estava com muitas alteracoes acumuladas porque varios modulos foram adaptados em paralelo para a base v2. Este checkpoint registra o estado por area, remove sinais de codigo transitorio e define o que pode ser considerado estavel neste momento.

## Estado por area

### Autenticacao e administracao

Estado: em uso.

- Login usa `POST /api/auth/login`.
- Refresh/logout usam `/api/auth/refresh` e `/api/auth/logout`.
- `AuthStateService` e `authInterceptor` sao o caminho atual para token Bearer.
- As rotas antigas de usuarios foram substituidas por `usuarios.component`.
- Os arquivos antigos de usuarios em `auth/pages/users/list`, `new` e `detail` estao removidos no working tree.

Validacao de negocio:

- Fluxo considerado aceito pelo cliente dentro dos parametros esperados para o MVP atual.
- A matriz completa de permissoes por funcionalidade sera implementada no final do projeto, nao como pendencia deste checkpoint.

### Alunos e responsaveis

Estado: alinhado ao contrato v2 inicial.

- Services usam `API_BASE_URL`.
- Endpoints principais:
  - `GET/POST/PUT/DELETE /api/alunos`;
  - `GET/POST/PUT/DELETE /api/responsaveis`;
  - `POST/GET/DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`;
  - `GET /enderecos/cep/{cep}`.
- Models contemplam dados pessoais e endereco.

Validacao de negocio:

- Fluxo aluno -> responsavel -> vinculo considerado aceito pelo cliente dentro dos parametros esperados para o MVP atual.

### Academico minimo

Estado: saneado neste checkpoint.

- `AcademicService` nao usa mais UUIDs artificiais fixos.
- O metodo legado `hydrateSeedData` foi removido.
- Telas de periodos e turmas chamam `syncFromApi`.
- Endpoints reais usados:
  - `GET /api/periodos-letivos`;
  - `GET /api/series`;
  - `GET /api/turmas`;
  - `POST /api/periodos-letivos`;
  - `POST/PUT /api/turmas`.
- O dialogo de turma passou a exigir `serieId`.

Status de produto:

- O funcionamento atual de periodo, serie e turma esta aceito pelo cliente dentro dos parametros esperados para o MVP atual.
- Ajustes adicionais de edicao persistida de periodo letivo so devem ser tratados se voltarem como necessidade explicita de produto.

### Matriculas

Estado: parcialmente alinhado ao contrato v2.

- Modelo de matricula contempla:
  - `serieId`;
  - `serieNome`;
  - `tipoMatricula`;
  - `dataMatricula`;
  - `observacao`;
  - `etapas`.
- Status oficiais usados na tela:
  - `SOLICITADA`;
  - `EM_ANDAMENTO`;
  - `AGUARDANDO_DOCUMENTOS`;
  - `AGUARDANDO_HISTORICO_ESCOLAR`;
  - `EFETIVADA`;
  - `CANCELADA`;
  - `INDEFERIDA`;
  - `CONCLUIDA`.
- Para vagas e bloqueio de aluno ja matriculado, o frontend considera `EFETIVADA` como matricula ativa.

Validacao de negocio:

- Fluxo de criacao, status e cancelamento de matricula considerado aceito pelo cliente dentro dos parametros esperados para o MVP atual.
- Nao ha pendencia aberta de decisao sobre matricula/status/cancelamento neste checkpoint.

### Historico, documentos e disciplinas

Estado: contrato backend validado contra PostgreSQL.

- `student-records` usa endpoints reais existentes no backend:
  - `/api/disciplinas`;
  - `/api/historicos-escolares`;
  - `/api/transferencias`;
  - `/api/documentos-alunos`;
  - `/enderecos/cep/{cep}`.

Status de produto:

- Historico, documentos e transferencia pertencem ao escopo ja trabalhado neste ciclo e nao devem ser tratados como proximo epico.
- Ajustes futuros nessa area devem nascer de necessidades novas ou refinamentos explicitamente solicitados.

## Limpezas executadas

- Removido o metodo legado `hydrateSeedData`.
- Removido comentario transitorio `/* unchanged */`.
- Varredura em `src/app` nao encontrou mais:
  - `TODO`;
  - `FIXME`;
  - `mock`;
  - `fake`;
  - UUIDs artificiais `10000000...` ou `20000000...`.

## Validacao executada

Comando:

```powershell
npm run build
```

Resultado:

```text
Application bundle generation complete.
```

## Proximo passo recomendado

Com o MVP atual aceito pelo cliente, o proximo passo deixa de ser validacao pendente do ciclo anterior e passa a ser o planejamento do novo epico funcional:

1. aulas e planejamento de aulas;
2. professores e relacao professor/turma/disciplina;
3. alunos em contexto de aula;
4. lancamento e acompanhamento de notas;
5. comportamento de alunos e professores;
6. dashboards de aulas, matriculas e evolucao dos alunos.

Permissoes granulares devem ficar para o final do projeto, depois que as funcionalidades principais estiverem estabilizadas.
