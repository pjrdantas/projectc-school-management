# Plano tecnico de refatoracao backend v2

## Regra principal

A base `gestao_escolar` e o arquivo `docs/v2/modelo_normalizado_escolar_v3_documentos_ia_dashboards.sql` sao a fonte oficial da verdade.

Regras obrigatorias:

1. Nao alterar a modelagem oficial do banco.
2. Nao criar migration para mudar tabela, coluna, constraint, indice ou relacionamento oficial.
3. Ajustar backend e frontend ao banco, nunca o banco ao codigo legado.
4. Todo novo documento tecnico deve ficar em `docs/v2`.
5. As proximas migrations Flyway so podem existir para evolucoes futuras aprovadas explicitamente.
6. O backend deve usar entidades, repositories, services e DTOs compativeis com os nomes reais da base v2.

## Estado atual da base

A base nova `gestao_escolar` ja foi criada com a modelagem v2.

Foram migrados apenas os dados de seguranca solicitados:

- `usuario`
- `usuario_perfil`
- `perfil`
- `perfil_permissao`
- `permissao`
- `sessao_autenticacao`

Os scripts Flyway antigos da base anterior foram removidos. A aplicacao inicia com Flyway sem migrations antigas e baseline limpo em `0`.

## Leitura inicial do backend atual

Pacotes atuais encontrados:

- `accesscontrol`
- `academiccatalog`
- `studentmanagement`
- `responsavelmanagement`
- `enrollment`
- `studentdocument`
- `schoolhistory`
- `transfermanagement`
- `shared`

### Dominios que podem ser reaproveitados com menor ajuste

#### `accesscontrol`

As tabelas v2 preservam a estrutura principal de seguranca:

- `usuario`
- `perfil`
- `permissao`
- `usuario_perfil`
- `perfil_permissao`
- `sessao_autenticacao`

O codigo atual ja usa essas tabelas e colunas principais. Este deve ser o primeiro checkpoint tecnico, validando:

- login;
- refresh;
- logout;
- leitura de perfis;
- leitura de permissoes;
- autorizacao por role/permissao.

### Dominios que precisam ser refatorados antes de uso real

#### `studentmanagement`

O codigo atual trata `aluno` como tabela com dados pessoais diretos, como `nome_completo`, `cpf`, `email`, `telefone`, `data_nascimento` e endereco.

Na base v2, esses dados ficam normalizados:

- `pessoa`
- `pessoa_tipo_pessoa`
- `endereco`
- `pessoa_endereco`
- `aluno`
- `status_aluno`

O backend deve criar/consultar aluno por composicao dessas tabelas, sem adicionar colunas em `aluno`.

#### `responsavelmanagement`

O codigo atual trata `responsavel` como tabela com dados pessoais diretos.

Na base v2, responsavel tambem depende de:

- `pessoa`
- `pessoa_tipo_pessoa`
- `endereco`
- `pessoa_endereco`
- `responsavel`
- `aluno_responsavel`
- `parentesco`

O vinculo `aluno_responsavel` usa `id_parentesco`, nao texto livre de parentesco.

#### `academiccatalog`

Parte dos nomes ainda existe, mas existem diferencas importantes:

- `periodo_letivo` exige `ano`.
- `serie` referencia `nivel_ensino` por `id_nivel_ensino`.
- `turma` referencia `turno` por `id_turno`; nao possui colunas legadas como `turno` texto ou `status`.
- `disciplina`, `turma_disciplina` e `professor_turma_disciplina` fazem parte do fluxo academico v2.

#### `enrollment`

O modelo antigo de matricula usa `status` e `tipo_matricula` como texto.

Na base v2, matricula usa catalogos e fluxo por etapas:

- `tipo_matricula`
- `status_matricula`
- `etapa_matricula_modelo`
- `status_etapa_matricula`
- `matricula`
- `matricula_etapa`
- `matricula_documento_exigido`
- `matricula_documento_entregue`

#### `studentdocument`

O modelo antigo usa `documento_aluno`.

Na base v2, documento e generico:

- `documento`
- `pessoa_documento`
- `matricula_documento_entregue`

Arquivos fisicos devem ficar em storage local configuravel, com metadados no banco.

#### `schoolhistory`

O codigo atual de historico usa colunas documentais antigas que nao batem integralmente com a v2.

Na base v2, o historico deve obedecer:

- `historico_escolar`
- `historico_escolar_item`
- `aluno_historico_evento`

#### `transfermanagement`

O codigo atual possui `escola_origem` e campos textuais de tipo/status.

Na base v2, transferencia usa:

- `transferencia_aluno`
- `tipo_transferencia`
- `status_transferencia`
- `escola`

## Ordem tecnica de execucao

### Checkpoint 1 - seguranca

Objetivo: garantir que o backend sobe contra `gestao_escolar` e autentica usando os usuarios migrados.

Escopo:

- validar entidades `UsuarioEntity`, `PerfilEntity`, `PermissaoEntity`, `SessaoAutenticacaoEntity`;
- validar repositories nativos de perfis/permissoes;
- validar login, refresh e logout;
- ajustar somente codigo se houver incompatibilidade.

Saida esperada:

- login funcional com usuario migrado;
- endpoint protegido respondendo com token valido;
- nenhuma alteracao no banco.

Status em 2026-05-25:

- Backend iniciou contra `jdbc:postgresql://localhost:5432/gestao_escolar`.
- Flyway encontrou somente o baseline `0` em `flyway_schema_history`.
- Login `POST /api/auth/login` validado com `admin/admin123`.
- Login retornou perfil `ADMIN` e permissoes `READ ALL`, `ADMIN`, `READ`, `CREATE`, `UPDATE`, `DELETE`.
- Endpoint protegido `GET /api/usuarios` validado com Bearer token.
- Dados migrados confirmados na base:
  - `perfil`: 4 registros;
  - `perfil_permissao`: 12 registros;
  - `permissao`: 6 registros;
  - `sessao_autenticacao`: 72 registros antes dos novos logins de validacao;
  - `usuario`: 2 registros;
  - `usuario_perfil`: 3 registros.

Pendencia tecnica resolvida:

- A autenticacao usa corretamente codigos de perfil/permissao.
- A resposta de `GET /api/usuarios` foi padronizada para retornar `perfis` com codigos e `perfilNomes` com nomes de exibicao.
- Revalidado com Bearer token gerado por `POST /api/auth/login`.

### Checkpoint 2 - fundacao de pessoas

Objetivo: criar a base de codigo para o modelo normalizado de pessoas.

Escopo:

- entidades JPA para `pessoa`, `tipo_pessoa`, `pessoa_tipo_pessoa`, `endereco`, `tipo_endereco`, `pessoa_endereco`;
- repositories de apoio;
- mappers/DTOs para pessoa e endereco;
- services transacionais para criacao/atualizacao de dados pessoais.

Saida esperada:

- camada de pessoa reutilizavel por aluno, responsavel, professor e funcionario;
- nenhum endpoint legado de aluno/responsavel usando colunas que nao existem mais.

Status em 2026-05-25:

- Criado pacote compartilhado `br.com.escola.shared.person`.
- Criadas entidades JPA alinhadas ao schema oficial:
  - `PessoaEntity` -> `pessoa`;
  - `TipoPessoaEntity` -> `tipo_pessoa`;
  - `PessoaTipoPessoaEntity` -> `pessoa_tipo_pessoa`;
  - `EnderecoEntity` -> `endereco`;
  - `TipoEnderecoEntity` -> `tipo_endereco`;
  - `PessoaEnderecoEntity` -> `pessoa_endereco`.
- Criados repositories Spring Data para as seis tabelas acima.
- Criados DTOs reutilizaveis:
  - `PessoaDados`;
  - `EnderecoDados`;
  - `PessoaCriada`;
  - `CatalogoPessoaResponse`.
- Criado `PessoaFoundationService`, responsavel por:
  - criar pessoa;
  - vincular tipo de pessoa por catalogo;
  - criar endereco;
  - vincular endereco principal;
  - consultar pessoa por CPF;
  - listar tipos de pessoa e tipos de endereco.
- Criado `PessoaCatalogoController` com endpoints read-only:
  - `GET /api/pessoas/catalogos/tipos-pessoa`;
  - `GET /api/pessoas/catalogos/tipos-endereco`.

Validacao executada:

- `mvnw.cmd -DskipTests package`: sucesso.
- Backend iniciado contra `gestao_escolar`: sucesso.
- Login com `admin/admin123`: sucesso.
- `GET /api/pessoas/catalogos/tipos-pessoa`: retornou `ALUNO`, `RESPONSAVEL`, `PROFESSOR`, `FUNCIONARIO`.
- `GET /api/pessoas/catalogos/tipos-endereco`: retornou `RESIDENCIAL`, `COMERCIAL`, `ESCOLAR`.
- Flyway permaneceu apenas com baseline `0`; nenhuma migration nova foi criada e nenhuma alteracao de schema foi feita.

### Checkpoint 3 - aluno e responsavel

Objetivo: refatorar cadastros de aluno e responsavel para a base v2.

Escopo:

- `aluno` como especializacao de `pessoa`;
- `responsavel` como especializacao de `pessoa`;
- vinculo `aluno_responsavel` com catalogo `parentesco`;
- consultas que retornem DTOs agregados sem expor entidades JPA.

Status em 2026-05-25:

- `AlunoEntity` passou a mapear a tabela oficial `aluno` como especializacao de `pessoa`:
  - `id_pessoa`;
  - `id_status_aluno`;
  - `ra`;
  - `rm`;
  - `emancipado`;
  - `data_ingresso`;
  - `data_saida`;
  - `motivo_saida`;
  - `ativo`.
- `ResponsavelEntity` passou a mapear a tabela oficial `responsavel` como especializacao de `pessoa`:
  - `id_pessoa`;
  - `created_at`;
  - `updated_at` ainda sera usado quando o fluxo de atualizacao exigir rastreio direto da especializacao.
- Criados catalogos JPA:
  - `StatusAlunoEntity` -> `status_aluno`;
  - `ParentescoEntity` -> `parentesco`.
- `AlunoPersistenceGateway` passou a:
  - criar dados pessoais em `pessoa`;
  - vincular `tipo_pessoa = ALUNO`;
  - criar/vincular endereco em `endereco` e `pessoa_endereco`;
  - criar especializacao em `aluno`;
  - ler dados agregados de `pessoa`, `endereco` e `status_aluno`.
- `ResponsavelPersistenceGateway` passou a:
  - criar dados pessoais em `pessoa`;
  - vincular `tipo_pessoa = RESPONSAVEL`;
  - criar/vincular endereco em `endereco` e `pessoa_endereco`;
  - criar especializacao em `responsavel`;
  - ler dados agregados de `pessoa` e `endereco`.
- `AlunoResponsavelEntity` passou a usar `id_parentesco`, conforme a base v2.
- `AlunoResponsavelVinculoPersistenceGateway` passou a resolver parentesco por codigo de catalogo.

Validacao executada:

- `mvnw.cmd -DskipTests package`: sucesso.
- Backend iniciado contra `gestao_escolar`: sucesso.
- Login com `admin/admin123`: sucesso.
- Criacao real de aluno via `POST /api/alunos`: sucesso.
- Criacao real de responsavel via `POST /api/responsaveis`: sucesso.
- Vinculo real via `POST /api/alunos/{idAluno}/responsaveis`: sucesso.
- Dados artificiais usados na validacao foram removidos da base ao final.
- Conferido que `flyway_schema_history` permanece somente com baseline `0`.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

Pendencias para proximos checkpoints:

- Revisar endpoints de listagem/consulta consolidada que ainda podem depender de queries antigas ou de tabelas antigas de historico/documentos/transferencia.
- Revisar exclusao de aluno/responsavel para alinhar com exclusao logica e autorizacao administrativa da v2.
- Atualizar testes automatizados, pois os testes H2 atuais ainda representam o modelo legado.

### Checkpoint 4 - academico minimo

Objetivo: alinhar periodo letivo, serie, turma, turno e nivel de ensino.

Escopo:

- catalogos `nivel_ensino` e `turno`;
- `periodo_letivo` com `ano`;
- `serie`;
- `turma`;
- consultas e cadastros basicos.

Status em 2026-05-25:

- Criadas entidades JPA read-only de catalogo:
  - `NivelEnsinoEntity` -> `nivel_ensino`;
  - `TurnoEntity` -> `turno`.
- Criados repositories para consulta dos catalogos por codigo.
- Criado `AcademicoCatalogoController` com endpoints:
  - `GET /api/academico/catalogos/niveis-ensino`;
  - `GET /api/academico/catalogos/turnos`.
- `PeriodoLetivoEntity` passou a mapear os campos oficiais:
  - `ano`;
  - `ativo`.
- O contrato de criacao de periodo letivo aceita `ano`; quando omitido, o backend deriva o ano a partir de `dataInicio`, preservando compatibilidade com chamadas antigas.
- `SerieEntity` passou a gravar `id_nivel_ensino`, resolvendo o campo de entrada `nivelEnsino` pelo codigo oficial do catalogo.
- `TurmaEntity` passou a gravar `id_turno` e `ativo`, resolvendo o campo de entrada `turno` pelo codigo oficial do catalogo.
- O campo legado de entrada `status` de turma foi mantido no contrato e convertido para `ativo`:
  - `INATIVA`, `INATIVO` e `false` gravam `ativo=false`;
  - valores ausentes ou demais valores gravam `ativo=true`.

Validacao executada:

- `mvnw.cmd -DskipTests package`: sucesso.
- Backend iniciado contra `gestao_escolar`: sucesso.
- Login com `admin/admin123`: sucesso.
- `GET /api/academico/catalogos/niveis-ensino`: retornou `EDUCACAO_INFANTIL`, `ENSINO_FUNDAMENTAL`, `ENSINO_MEDIO`.
- `GET /api/academico/catalogos/turnos`: retornou `MANHA`, `TARDE`, `NOITE`, `INTEGRAL`.
- Criacao real de periodo letivo via `POST /api/periodos-letivos`: sucesso, com `ano=2026` derivado de `dataInicio`.
- Criacao real de serie via `POST /api/series`: sucesso, com `nivelEnsino=ENSINO_FUNDAMENTAL`.
- Criacao real de turma via `POST /api/turmas`: sucesso, com `turno=MANHA` e `status=ATIVA`.
- Dados artificiais usados na validacao foram removidos da base ao final.
- Conferido que `flyway_schema_history` permanece somente com baseline `0`.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

### Checkpoint 5 - matricula por etapas

Objetivo: substituir o fluxo antigo de matricula pelo fluxo v2.

Escopo:

- tipo/status de matricula via catalogo;
- criacao de etapas;
- documentos exigidos e entregues;
- regras de efetivacao.

Status em 2026-05-25:

- `MatriculaEntity` passou a mapear a tabela oficial `matricula`:
  - `id_tipo_matricula`;
  - `id_status_matricula`;
  - `data_solicitacao`;
  - `data_efetivacao`;
  - `observacao`;
  - `created_at`.
- Criadas entidades JPA para os catalogos e etapas:
  - `TipoMatriculaEntity` -> `tipo_matricula`;
  - `StatusMatriculaEntity` -> `status_matricula`;
  - `StatusEtapaMatriculaEntity` -> `status_etapa_matricula`;
  - `EtapaMatriculaModeloEntity` -> `etapa_matricula_modelo`;
  - `MatriculaEtapaEntity` -> `matricula_etapa`.
- Criados repositories para os catalogos e etapas.
- Criado `MatriculaCatalogoController` com endpoints:
  - `GET /api/matriculas/catalogos/tipos`;
  - `GET /api/matriculas/catalogos/status`;
  - `GET /api/matriculas/catalogos/status-etapas`.
- A criacao de matricula agora grava tipo e status por catalogo oficial.
- O status inicial de nova matricula passou a ser `SOLICITADA`.
- O contrato antigo aceita aliases de compatibilidade:
  - tipo `NOVA` -> `PRIMEIRA_MATRICULA`;
  - tipo `REMATRICULA` -> `RENOVACAO`;
  - tipo `TRANSFERENCIA` -> `TRANSFERENCIA_ENTRADA`;
  - status `ATIVA` -> `EFETIVADA`.
- Ao criar uma matricula, o backend cria registros em `matricula_etapa`.
- Como a base oficial ainda nao possui registros em `etapa_matricula_modelo`, o backend cria uma etapa transacional minima com descricao `Solicitação de matrícula`, ordem `1` e status `PENDENTE`.
- A contagem de vaga considera matriculas que nao estejam em `CANCELADA` ou `INDEFERIDA`.
- A consulta por status passou a filtrar por `status_matricula.codigo`.
- A resposta de matricula agora retorna a lista `etapas`.

Validacao executada:

- `mvnw.cmd -DskipTests package`: sucesso.
- Backend iniciado contra `gestao_escolar`: sucesso.
- Login com `admin/admin123`: sucesso.
- `GET /api/matriculas/catalogos/tipos`: retornou `PRIMEIRA_MATRICULA`, `TRANSFERENCIA_ENTRADA`, `RENOVACAO`, `TRANSFERENCIA_SAIDA`.
- `GET /api/matriculas/catalogos/status`: retornou `SOLICITADA`, `EM_ANDAMENTO`, `AGUARDANDO_DOCUMENTOS`, `AGUARDANDO_HISTORICO_ESCOLAR`, `EFETIVADA`, `CANCELADA`, `INDEFERIDA`, `CONCLUIDA`.
- `GET /api/matriculas/catalogos/status-etapas`: retornou `PENDENTE`, `EM_ANALISE`, `CONCLUIDA`, `REPROVADA`.
- Criacao real de aluno, periodo letivo, serie e turma temporarios para validacao: sucesso.
- Criacao real de matricula via `POST /api/matriculas`: sucesso, com `status=SOLICITADA`, `tipoMatricula=PRIMEIRA_MATRICULA` e uma etapa `PENDENTE`.
- Atualizacao de status via `PATCH /api/matriculas/{id}/status` para `EFETIVADA`: sucesso.
- Consulta por `GET /api/matriculas?status=EFETIVADA`: sucesso.
- Dados artificiais usados na validacao foram removidos da base ao final.
- Conferido que `flyway_schema_history` permanece somente com baseline `0`.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

### Checkpoint 6 - estabilizacao inicial dos endpoints backend v2

Objetivo: garantir que as telas do frontend possam carregar contra a base oficial mesmo quando ainda nao existirem registros operacionais cadastrados.

Escopo:

- validar endpoints reais contra `gestao_escolar`;
- corrigir contratos que retornavam erro para lista vazia;
- manter `404` apenas para busca por id inexistente ou regra equivalente;
- nao alterar schema e nao criar migration.

Status em 2026-05-26:

- Corrigidos os endpoints de listagem para retornar `200` com lista vazia (`[]`) quando nao houver registros:
  - `GET /api/alunos`;
  - `GET /api/responsaveis`;
  - `GET /api/periodos-letivos`;
  - `GET /api/series`;
  - `GET /api/turmas`.
- Mantido o contrato de erro para buscas pontuais quando o recurso solicitado nao existir.
- O endpoint `GET /api/matriculas` ja seguia o contrato esperado e retornou `200` com lista vazia.
- Dados artificiais remanescentes do diagnostico anterior foram conferidos/removidos da base.

Validacao executada:

- `mvnw.cmd test`: sucesso.
- Resultado da suite: `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`.
- Backend iniciado contra `gestao_escolar` na porta isolada `18080`: sucesso.
- Login real `POST /api/auth/login` com `admin/admin123`: `200`.
- Chamadas reais validadas:
  - `GET /api/usuarios`: `200`, 2 registros;
  - `GET /api/perfis`: `200`, 4 registros;
  - `GET /api/permissoes`: `200`, 6 registros;
  - `GET /api/alunos`: `200`, 0 registros;
  - `GET /api/responsaveis`: `200`, 0 registros;
  - `GET /api/periodos-letivos`: `200`, 0 registros;
  - `GET /api/series`: `200`, 0 registros;
  - `GET /api/turmas`: `200`, 0 registros;
  - `GET /api/matriculas`: `200`, 0 registros;
  - `GET /api/pessoas/catalogos/tipos-pessoa`: `200`, 4 registros;
  - `GET /api/academico/catalogos/niveis-ensino`: `200`, 3 registros;
- `GET /api/matriculas/catalogos/tipos`: `200`, 4 registros.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

### Checkpoint 7 - alinhamento inicial do frontend aos contratos v2

Objetivo: iniciar a reforma do Angular pelos services/models, evitando que as telas chamem contratos antigos ou dados artificiais.

Status em 2026-05-26:

- Conferidos os services/models de alunos e responsaveis contra os endpoints v2.
- Ajustado o contrato academico do frontend:
  - `AcademicPeriod` passou a refletir `ano` e `ativo`;
  - criado `AcademicSeries` para consumir `GET /api/series`;
  - `AcademicClass` passou a refletir `serieId`, `serieNome`, `turno` e `status`;
  - `AcademicClassInput` passou a enviar `serieId`, `turno` e `status`.
- `AcademicService` deixou de buscar UUIDs artificiais fixos e passou a sincronizar por listagens reais:
  - `GET /api/periodos-letivos`;
  - `GET /api/series`;
  - `GET /api/turmas`.
- O dialogo de turma passou a exigir selecao de serie, porque o backend v2 exige `serieId`.
- O modelo de matricula do frontend foi ampliado para os campos v2:
  - `serieId`;
  - `serieNome`;
  - `tipoMatricula`;
  - `dataMatricula`;
  - `observacao`;
  - `etapas`.
- A tela de matricula passou a usar os status oficiais v2:
  - `SOLICITADA`;
  - `EM_ANDAMENTO`;
  - `AGUARDANDO_DOCUMENTOS`;
  - `AGUARDANDO_HISTORICO_ESCOLAR`;
  - `EFETIVADA`;
  - `CANCELADA`;
  - `INDEFERIDA`;
  - `CONCLUIDA`.
- Para contagem de vagas e bloqueio de aluno ja matriculado no periodo, o frontend agora considera `EFETIVADA` como matricula ativa.

Validacao executada:

- `npm run build`: sucesso.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

### Checkpoint 8 - consolidacao tecnica do frontend v2

Objetivo: reduzir o risco de continuar a reforma com muitas alteracoes acumuladas sem rastreabilidade.

Status em 2026-05-26:

- Criado o documento `docs/v2/03-consolidacao-frontend-v2.md`.
- Mapeadas as areas alteradas do frontend:
  - autenticacao e administracao;
  - alunos e responsaveis;
  - academico minimo;
  - matriculas;
  - historico, documentos e disciplinas.
- Removido o metodo legado `hydrateSeedData`.
- Removido comentario transitorio `/* unchanged */`.
- Conferido que as rotas antigas de usuarios apontam para o componente novo `usuarios.component`.
- Conferido que a area `student-records` usa endpoints reais existentes, mas ainda nao foi validada neste checkpoint.

Validacao executada:

- Varredura em `school-management-web/host/src/app` sem ocorrencias de `TODO`, `FIXME`, `mock`, `fake` ou UUIDs artificiais de seed.
- `npm run build`: sucesso.
- Nenhuma migration foi criada e nenhuma alteracao de schema foi feita.

## Validacao por checkpoint

Cada checkpoint deve ter:

1. build backend;
2. teste ou chamada real contra `gestao_escolar`;
3. evidencia de que nao houve alteracao de schema;
4. atualizacao de documento em `docs/v2` quando houver decisao tecnica nova.
