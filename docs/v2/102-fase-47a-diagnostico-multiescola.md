# Fase 47A - Diagnóstico multi-escola

## Objetivo

Mapear o impacto de multi-escola no modelo atual antes de criar migrations, filtros ou contexto de tenant.

Esta fase é apenas diagnóstica. Nenhuma alteração de schema, backend ou frontend foi aplicada.

## Estado atual observado

- O sistema ainda opera como escola única.
- O roadmap pós-MVP define que multi-escola deve vir antes de BFF definitivo e separação de serviços.
- O modelo SQL v3 em `docs/v2/modelo_normalizado_escolar_v3_documentos_ia_dashboards.sql` já possui uma tabela `escola`.
- No código atual, a entidade mapeada para `escola` é `EscolaOrigemEntity`, usada no contexto de transferência, não como tenant funcional.
- As principais entidades escolares não carregam `id_escola` nem usam contexto de escola nas consultas.
- Segurança ainda não possui vínculo explícito entre usuário e escola ativa.

## Decisão de diagnóstico

Para a Fase 47B, `escola` deve ser tratada como entidade de tenant/unidade escolar do sistema, e não apenas como escola de origem/destino de transferência.

Antes de qualquer migration, será necessário decidir se:

1. a tabela `escola` atual será reaproveitada e a entidade será renomeada/realocada para um domínio institucional;
2. ou se a escola de transferência será separada conceitualmente da escola tenant.

Recomendação: reaproveitar `escola` como entidade institucional/tenant e ajustar o nome da entidade Java atual, porque o modelo v3 já aponta `transferencia_aluno.id_escola_origem` e `id_escola_destino` para `escola(id_escola)`.

## Classificação das tabelas

### Globais técnicas

Não devem carregar `id_escola` na primeira etapa, pois representam estrutura técnica do sistema:

- `permissao`
- `perfil`, se mantido como perfil técnico padrão
- `perfil_permissao`
- `publico_dashboard`, se representar público técnico do produto

Observação: se a escola puder customizar perfis no futuro, `perfil` precisará de política híbrida: perfis globais padrão e perfis customizados por escola.

### Globais de catálogo controlado

Devem permanecer globais inicialmente, salvo customização explícita por escola:

- `tipo_pessoa`
- `tipo_documento`
- `tipo_endereco`
- `parentesco`
- `status_aluno`
- `tipo_matricula`
- `status_matricula`
- `etapa_matricula_modelo`
- `status_etapa_matricula`
- `tipo_transferencia`
- `status_transferencia`
- `turno`
- `nivel_ensino`
- `tipo_avaliacao`
- `situacao_frequencia`
- `tipo_evento_aluno`
- `status_planejamento`
- `tipo_conteudo_ia`
- `status_conteudo_ia`

Observação: `etapa_matricula_modelo`, `tipo_avaliacao`, `turno`, `serie` e `disciplina` podem virar híbridos se a escola puder customizar nomenclatura, regras ou carga horária.

### Entidade tenant

Deve representar a fronteira funcional de isolamento:

- `escola`

Campos mínimos esperados para a Fase 47B:

- identificador;
- nome;
- código INEP, quando houver;
- CNPJ, quando houver;
- ativo;
- dados de contato;
- vínculo com endereço, se mantido no modelo.

### Tabelas que devem carregar `id_escola` diretamente

Estas entidades são raiz de agregados ou cadastros operacionais e devem receber vínculo direto com escola:

- `pessoa`
- `aluno`
- `responsavel`
- `professor`
- `funcionario`
- `periodo_letivo`
- `serie`, se customizada por escola
- `disciplina`, se customizada por escola
- `turma`
- `documento`
- `matricula_documento_exigido`
- `planejamento_professor`
- `dashboard`
- `dashboard_widget`, se configurável por escola
- `dashboard_usuario_configuracao`
- `dashboard_indicador_snapshot`
- `biblioteca_conteudo_pedagogico`
- `usuario`, se o usuário pertencer a uma escola padrão
- `sessao_autenticacao`, para registrar escola ativa na sessão

Observações:

- `pessoa` como raiz reduz repetição em `aluno`, `responsavel`, `professor` e `funcionario`, mas ainda pode ser necessário manter `id_escola` nos papéis para permitir uma mesma pessoa em mais de uma escola com papéis diferentes.
- `biblioteca_conteudo_pedagogico` deve carregar `id_escola` diretamente para evitar vazamento de material pedagógico entre escolas.
- `dashboard_indicador_snapshot` deve carregar `id_escola` diretamente porque snapshots são dados agregados por contexto.

### Tabelas que podem herdar escola por relacionamento

Estas tabelas podem ser filtradas por escola via entidade pai, desde que os relacionamentos sejam consistentes:

- `pessoa_tipo_pessoa`, via `pessoa`
- `endereco`, se usado apenas por pessoa/escola/documento e não como catálogo compartilhado
- `pessoa_endereco`, via `pessoa`
- `pessoa_documento`, via `pessoa` e `documento`
- `aluno_responsavel`, via `aluno` e `responsavel`
- `turma_disciplina`, via `turma`
- `professor_turma_disciplina`, via `professor` e `turma_disciplina`
- `matricula`, via `aluno` e `turma`
- `matricula_etapa`, via `matricula`
- `matricula_documento_entregue`, via `matricula`
- `transferencia_aluno`, via `aluno`, mas deve validar escola origem/destino
- `planejamento_aula`, via `professor_turma_disciplina`
- `aula`, via `professor_turma_disciplina`
- `frequencia_aluno`, via `aula` e `matricula`
- `frequencia_professor`, via `aula` e `professor`
- `avaliacao`, via `professor_turma_disciplina`, `turma` ou planejamento bimestral
- `nota_aluno`, via `avaliacao` e `matricula`
- `boletim`, via `matricula`
- `boletim_item`, via `boletim`
- `periodo_avaliativo`, via `periodo_letivo`
- `planejamento_bimestral`, via `professor_turma_disciplina`
- `planejamento_bimestral_aula`, via `planejamento_bimestral`
- `planejamento_bimestral_avaliacao`, via `planejamento_bimestral`
- `planejamento_ia_interacao`, via `planejamento_bimestral`
- `planejamento_ia_conteudo_gerado`, via `planejamento_bimestral`
- `planejamento_ia_conteudo_versao`, via `planejamento_ia_conteudo_gerado`
- `historico_escolar_item`, via `historico_escolar`
- `aluno_historico_evento`, via `aluno`
- `solicitacao_exclusao_aluno`, via `aluno`

Observação: herdar escola por relacionamento reduz redundância, mas exige índices e consultas cuidadosas. Em dados auditáveis ou agregados, vale considerar `id_escola` direto mesmo quando a escola também pode ser inferida.

### Tabelas que precisam de decisão específica

#### `historico_escolar`

O modelo v3 já prevê `id_escola`. Deve carregar escola diretamente porque histórico pode representar documento emitido/armazenado e precisa preservar contexto institucional.

#### `documento`

Deve carregar escola diretamente se o armazenamento físico seguir o padrão `/uploads/escola/{idEscola}/...`.

#### `perfil` e `usuario_perfil`

Existem dois caminhos:

- perfis globais técnicos com `usuario_perfil` escopado por escola;
- perfis customizados por escola com `perfil.id_escola` opcional.

Recomendação para 47B: manter `perfil` global e criar vínculo usuário-escola separado antes de permitir customização.

#### `serie` e `disciplina`

Podem ser globais no produto, mas no uso escolar tendem a ser customizadas por escola. Para evitar retrabalho, a recomendação é permitir `id_escola` opcional ou direto quando a Fase 47C chegar nos catálogos acadêmicos.

## Riscos identificados

- A tabela `escola` já existe conceitualmente, mas a entidade Java atual se chama `EscolaOrigemEntity` e está no domínio de transferência.
- Consultas atuais listam dados sem qualquer filtro por escola.
- Constraints únicas atuais não consideram escola, por exemplo turma por período/código e planejamento por alocação/período/tema.
- Usuário autenticado não possui escola ativa.
- Dashboards e snapshots podem misturar dados de escolas diferentes se a agregação multi-escola for adicionada sem filtro obrigatório.
- Biblioteca pedagógica precisa de isolamento claro antes de qualquer reuso amplo de conteúdo.

## Recomendação para a Fase 47B

Implementar o menor modelo base de tenant:

1. Criar/regularizar entidade institucional `Escola`.
2. Criar uma escola padrão para dados existentes.
3. Definir vínculo entre usuário e escola.
4. Definir escola ativa no contexto de sessão/autenticação.
5. Preparar contratos internos para obter `escolaId` atual, sem ainda aplicar filtros em todos os domínios.
6. Não separar banco, BFF ou serviços nesta etapa.

## Ordem sugerida para aplicação posterior

Após 47B, aplicar escopo por escola em subfases pequenas:

1. Catálogos acadêmicos: `periodo_letivo`, `serie`, `disciplina`, `turma`, `turma_disciplina`.
2. Pessoas e papéis: `pessoa`, `aluno`, `responsavel`, `professor`, `funcionario`.
3. Matrículas e documentos.
4. Aulas, frequência, avaliação, boletim e histórico.
5. Planejamento bimestral, IA e biblioteca pedagógica.
6. Dashboards e snapshots.

## Validação

Não houve alteração de código, schema ou frontend.

Validação executada:

- leitura do roadmap pós-MVP;
- leitura do modelo SQL v3;
- mapeamento das entidades JPA atuais relacionadas a escola, planejamento, IA, catálogos, segurança, dashboards e fluxo acadêmico.

## Próxima fase recomendada

Fase 47B - Modelo base de escola/tenant:

- introduzir ou regularizar a entidade `Escola`;
- definir escola padrão para dados atuais;
- definir vínculo mínimo entre usuário e escola;
- definir escola ativa no contexto de autenticação/sessão;
- validar backend com `.\mvnw.cmd test`.
