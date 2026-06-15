# Fase 48A - Desenho dos BFFs

## Objetivo

Definir o desenho inicial dos BFFs da evolucao pos-MVP a partir das jornadas reais do sistema atual, mantendo o monolito como fonte de verdade nesta fase.

Esta fase e documental e contratual. Ela nao cria aplicacoes BFF, nao extrai servicos, nao altera o banco e nao muda contratos HTTP existentes.

## Premissas

- O sistema continua operando inicialmente para uma escola, mas os contratos ja devem preservar contexto multi-escola.
- O backend atual segue como API de dominio e fonte de verdade para regras, persistencia e validacoes.
- Os BFFs futuros devem compor experiencias de tela, nao substituir regras de dominio.
- Cada BFF deve entregar modelos orientados a jornada e reduzir acoplamento direto entre frontend e granularidade interna dos servicos.
- O contexto autenticado deve carregar, no minimo, `usuarioId`, `escolaId`, `escolaNome`, `perfis`, `permissoes` e, quando existir, `professorId`.

## Baseline atual observado

O repositorio possui hoje:

- `school-management-service`: backend Spring Boot monolitico com endpoints de seguranca, cadastros, matriculas, documentos, historico, aulas, avaliacoes, planejamento, IA pedagogica e dashboards.
- `school-management-web/host`: shell Angular com login, navegacao, seguranca e carregamento remoto das telas de negocio.
- `school-management-web/microfrontend`: microfrontend academico com as telas operacionais atuais.

As rotas atuais do shell e do microfrontend indicam quatro experiencias principais: administracao/shell, secretaria, professor/pedagogico e direcao/dashboard. Um portal externo de responsavel/aluno deve ser tratado como BFF futuro, ainda fora do MVP operacional atual.

## BFFs propostos

### 1. `bff-admin-shell`

Experiencia atendida:

- Entrada autenticada no sistema.
- Resolucao de contexto do usuario.
- Montagem de menus, permissoes, escola atual e configuracoes basicas de shell.
- Administracao de usuarios, perfis, permissoes e configuracoes globais.

Responsabilidades:

- Compor sessao autenticada e contexto multi-escola.
- Entregar navegacao disponivel por perfil/permissao.
- Centralizar contratos de shell para reduzir duplicacao entre microfrontends.
- Apoiar troca futura de escola quando houver usuario vinculado a mais de uma escola.

Endpoints atuais relacionados:

- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/logout`
- `/api/usuarios`
- `/api/perfis`
- `/api/permissoes`
- `/api/dashboard/configuracoes`
- `/api/dashboard/usuarios/{usuarioId}/configuracoes`

Contratos-alvo iniciais:

- `GET /bff/shell/contexto`
  - Retorna usuario autenticado, escola atual, escolas disponiveis, perfis, permissoes e flags de experiencia.
- `GET /bff/shell/menu`
  - Retorna grupos de navegacao filtrados por perfil, permissao e contexto escolar.
- `GET /bff/shell/preferencias`
  - Retorna preferencias do usuario para dashboard e shell.
- `PUT /bff/shell/preferencias`
  - Atualiza preferencias do usuario sem expor detalhes internos dos widgets.

Fora de escopo:

- Autorizacao de dominio dentro do BFF.
- Edicao direta de regras de perfil/permissao fora dos endpoints de seguranca existentes.

### 2. `bff-secretaria`

Experiencia atendida:

- Atendimento administrativo da secretaria.
- Cadastro e manutencao de alunos e responsaveis.
- Matriculas, rematriculas, documentos, transferencias e historico escolar.
- Consulta rapida de dossie do aluno.

Responsabilidades:

- Compor dados cadastrais, matricula atual, responsaveis, documentos, transferencias e historicos em visoes de trabalho.
- Reduzir chamadas sequenciais do frontend nas telas de detalhe do aluno.
- Entregar listas orientadas a triagem da secretaria, com filtros de status e pendencias.
- Preservar escopo por `escolaId` em todos os contratos.

Endpoints atuais relacionados:

- `/api/alunos`
- `/api/alunos/{id}/ficha`
- `/api/responsaveis`
- `/api/alunos/{idAluno}/responsaveis`
- `/api/consulta-cadastral`
- `/api/matriculas`
- `/api/matriculas/{id}/etapas`
- `/api/matriculas/{id}/documentos-entregues`
- `/api/matriculas/{id}/documentos-exigidos`
- `/api/matriculas/{id}/conclusao-academica`
- `/api/matriculas/{id}/rematricula`
- `/api/matriculas/{id}/rematricula/elegibilidade`
- `/api/documentos`
- `/api/documentos-alunos`
- `/api/transferencias`
- `/api/escolas-origem`
- `/api/historicos-escolares`

Contratos-alvo iniciais:

- `GET /bff/secretaria/home`
  - Retorna indicadores de atendimento, pendencias de documento, matriculas em andamento e alertas operacionais.
- `GET /bff/secretaria/alunos`
  - Retorna lista paginada de alunos com matricula atual, status, turma, responsaveis resumidos e pendencias.
- `GET /bff/secretaria/alunos/{alunoId}/dossie`
  - Retorna cadastro, responsaveis, matriculas, documentos, transferencias, historicos e acoes permitidas.
- `GET /bff/secretaria/matriculas/{matriculaId}/workspace`
  - Retorna etapas, documentos exigidos/entregues, elegibilidade de rematricula e proximas acoes.
- `POST /bff/secretaria/matriculas/{matriculaId}/acoes`
  - Orquestra acoes de fluxo usando APIs de dominio existentes.

Fora de escopo:

- Criar regras de matricula no BFF.
- Permitir escrita sem passar pelos servicos de dominio.
- Expor ids tecnicos que nao sejam necessarios para acoes da interface.

### 3. `bff-professor`

Experiencia atendida:

- Area de trabalho do professor.
- Turmas e disciplinas vinculadas.
- Registro de aulas, frequencias, avaliacoes e notas.
- Planejamento bimestral com apoio de IA e biblioteca pedagogica.

Responsabilidades:

- Resolver `professorId` a partir do usuario autenticado quando aplicavel.
- Entregar uma home pedagogica com turmas, proximas aulas, pendencias de frequencia, avaliacoes e planejamentos.
- Compor planejamento, conteudos de IA, aulas previstas e avaliacoes previstas em contrato de trabalho unico.
- Aplicar escopo por escola e vinculos do professor.

Endpoints atuais relacionados:

- `/api/dashboard/professores/{professorId}`
- `/api/professores`
- `/api/professores/{id}/turmas-disciplinas`
- `/api/turmas/{turmaId}/professores`
- `/api/aulas`
- `/api/aulas/{id}/frequencia-professor`
- `/api/aulas/{id}/frequencias-alunos`
- `/api/avaliacoes`
- `/api/avaliacoes/{id}/notas`
- `/api/matriculas/{matriculaId}/notas`
- `/api/planejamentos-bimestrais`
- `/api/planejamentos-bimestrais/{id}/aulas-previstas`
- `/api/planejamentos-bimestrais/{id}/avaliacoes-previstas`
- `/api/planejamentos-bimestrais/{id}/status`
- `/api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`
- `/api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`
- `/api/ia/conteudos/{conteudoId}`
- `/api/ia/conteudos/{conteudoId}/versoes`
- `/api/ia/conteudos/{conteudoId}/aprovar-versao`
- `/api/ia/conteudos/{conteudoId}/publicar-biblioteca`
- `/api/biblioteca-conteudos-pedagogicos`

Contratos-alvo iniciais:

- `GET /bff/professor/home`
  - Retorna turmas vinculadas, aulas recentes, pendencias, avaliacoes abertas, planejamentos ativos e alertas.
- `GET /bff/professor/turmas/{turmaId}/diario`
  - Retorna alunos, aulas, frequencias, avaliacoes e notas relevantes para o diario da turma.
- `GET /bff/professor/planejamentos`
  - Retorna planejamentos filtrados pelo professor, turma, disciplina, bimestre e status.
- `GET /bff/professor/planejamentos/{planejamentoId}/workspace`
  - Retorna planejamento, aulas previstas, avaliacoes previstas, conteudos de IA, interacoes e acoes permitidas.
- `POST /bff/professor/planejamentos/{planejamentoId}/ia/acoes`
  - Encaminha acoes de IA pedagogica para os contratos de dominio adequados.

Fora de escopo:

- O BFF nao deve chamar provedor de IA diretamente enquanto o servico de IA pedagogica for o dono do ciclo de conteudo.
- O BFF nao deve decidir aprovacao, publicacao ou versionamento de conteudo sem usar os contratos de dominio.

### 4. `bff-diretor`

Experiencia atendida:

- Visao executiva da escola.
- Acompanhamento de indicadores academicos, secretaria, professor e alertas.
- Analise de snapshots e tendencias.

Responsabilidades:

- Compor dados de dashboards por perfil de direcao.
- Entregar indicadores agregados com links para detalhes operacionais.
- Preservar filtros por escola, periodo letivo, serie, turma e intervalo.
- Separar visao executiva de operacao diaria da secretaria e do professor.

Endpoints atuais relacionados:

- `/api/dashboard/frontend`
- `/api/dashboard/academico`
- `/api/dashboard/secretaria`
- `/api/dashboard/diretor`
- `/api/dashboard/alertas`
- `/api/dashboard/snapshots`
- `/api/dashboard/configuracoes`

Contratos-alvo iniciais:

- `GET /bff/diretor/painel`
  - Retorna indicadores principais, alertas, tendencias, snapshots recentes e atalhos de investigacao.
- `GET /bff/diretor/indicadores`
  - Retorna series agregadas filtradas por escola, periodo, turma, serie e dimensao.
- `GET /bff/diretor/alertas`
  - Retorna alertas priorizados com origem, severidade, responsavel sugerido e acao de navegacao.
- `GET /bff/diretor/snapshots`
  - Retorna historico comparavel de snapshots com metadados de geracao.

Fora de escopo:

- Substituir consultas analiticas por armazenamento separado nesta fase.
- Criar Data Lake, MongoDB, Redis ou fila de eventos antes das fases especificas.

### 5. `bff-portal-responsavel`

Status:

- Futuro, nao recomendado para implementacao imediata.

Experiencia atendida:

- Portal externo para responsaveis e alunos acompanharem matricula, documentos, boletins, frequencia e comunicados.

Motivo para postergar:

- Exige modelo de identidade externo, consentimento, vinculos familiares, LGPD, politicas de visibilidade e contratos de comunicacao que ainda nao fazem parte do fluxo operacional atual.

Contratos-alvo futuros:

- `GET /bff/portal/contexto`
- `GET /bff/portal/alunos`
- `GET /bff/portal/alunos/{alunoId}/vida-escolar`
- `GET /bff/portal/alunos/{alunoId}/documentos`
- `GET /bff/portal/alunos/{alunoId}/boletim`

## Contexto multi-escola nos BFFs

Todos os BFFs devem receber o contexto de escola a partir da sessao e propagar `escolaId` para os servicos de dominio.

Regras iniciais:

- Respostas agregadas devem informar `escolaId` e `escolaNome` quando o dado representar uma escola especifica.
- Escritas devem validar escola pelo backend de dominio, nao apenas pelo BFF.
- O BFF pode filtrar experiencia e navegacao, mas nao pode ser a unica barreira de autorizacao.
- Quando a troca de escola for implementada, ela deve ocorrer no `bff-admin-shell` e refletir nos demais BFFs via token, sessao ou contexto resolvido.

## Contratos transversais

Envelope minimo recomendado para respostas de workspace:

```json
{
  "contexto": {
    "usuarioId": "uuid",
    "escolaId": "uuid",
    "escolaNome": "Nome da escola",
    "perfis": ["PROFESSOR"],
    "permissoes": ["PLANEJAMENTO_EDITAR"]
  },
  "dados": {},
  "acoesPermitidas": [],
  "alertas": []
}
```

Padroes recomendados:

- `acoesPermitidas` deve orientar UI, mas nao substituir autorizacao backend.
- `alertas` deve ser estruturado com codigo, severidade, mensagem curta e rota de acao.
- Contratos de lista devem manter paginacao explicita.
- Campos tecnicos internos devem ser omitidos quando nao forem necessarios para uma acao.
- IDs necessarios para comando devem continuar estaveis e opacos.

## Anti-objetivos desta fase

- Nao criar projetos BFF.
- Nao extrair microservicos.
- Nao introduzir Kafka, MongoDB ou Redis.
- Nao dividir o microfrontend atual.
- Nao reescrever controladores existentes.
- Nao alterar migrations.
- Nao revisar permissao global alem do necessario para desenhar contratos.

## Criterios para considerar a Fase 48A concluida

- BFFs candidatos definidos por experiencia de usuario.
- Responsabilidades e fora de escopo registrados para cada BFF.
- Endpoints atuais mapeados para cada BFF futuro.
- Contratos-alvo iniciais propostos sem alterar codigo.
- Contexto multi-escola documentado como requisito transversal.

## Proxima fase sugerida

Fase 48B - Fronteiras iniciais de servicos e contratos internos.

Objetivo da proxima fase:

- Desenhar os servicos de dominio que ficariam atras dos BFFs no futuro.
- Separar candidatos a servico por responsabilidade: identidade/escola, secretaria, academico, planejamento/IA e dashboards.
- Definir quais eventos futuros fariam sentido antes de qualquer Kafka real.
- Manter a implementacao ainda no monolito ate haver uma fase explicita de extracao.
