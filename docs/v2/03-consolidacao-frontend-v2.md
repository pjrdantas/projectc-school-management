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

Pendencia:

- Validar visualmente as telas de usuarios, perfis e permissoes contra a base real depois da limpeza.

### Alunos e responsaveis

Estado: alinhado ao contrato v2 inicial.

- Services usam `API_BASE_URL`.
- Endpoints principais:
  - `GET/POST/PUT/DELETE /api/alunos`;
  - `GET/POST/PUT/DELETE /api/responsaveis`;
  - `POST/GET/DELETE /api/alunos/{idAluno}/responsaveis/{idResponsavel}`;
  - `GET /enderecos/cep/{cep}`.
- Models contemplam dados pessoais e endereco.

Pendencia:

- Validar no navegador o fluxo aluno -> responsavel -> vinculo.

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

Pendencias:

- Implementar atualizacao real de periodo letivo se a tela precisar editar persistindo no backend. Hoje a edicao de periodo ainda e local.
- Validar no navegador criacao de periodo, serie existente e turma.

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

Pendencia:

- Validar no navegador o fluxo de criacao e mudanca de status de matricula.

### Historico, documentos e disciplinas

Estado: contrato backend validado contra PostgreSQL.

- `student-records` usa endpoints reais existentes no backend:
  - `/api/disciplinas`;
  - `/api/historicos-escolares`;
  - `/api/transferencias`;
  - `/api/documentos-alunos`;
  - `/enderecos/cep/{cep}`.

Pendencia:

- Validar visualmente as telas `student-records` no navegador.

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

Antes de novas funcionalidades, validar no navegador contra backend `8080` e base `gestao_escolar`:

1. login;
2. usuarios/perfis/permissoes;
3. alunos;
4. responsaveis;
5. vinculo aluno-responsavel;
6. periodos e turmas;
7. matricula.
