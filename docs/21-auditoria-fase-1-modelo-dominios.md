# 21 - Auditoria Fase 1: modelo de tabelas por dominio

## Objetivo

Registrar a auditoria inicial entre:

- `C:/Users/pjr_d/OneDrive/Documentos/model.zip`;
- `C:/Users/pjr_d/OneDrive/Documentos/scriptdb.sql`;
- entidades atualmente existentes em `school-management-service/src/main/java/br/com/escola`.

Esta fase nao cria entidades novas. Ela apenas identifica o que ja existe, o que falta e em qual dominio cada tabela deve entrar.

## Resultado geral

O `model.zip` possui 77 entidades/tabelas mapeadas.

O script oficial anexado `C:/Users/pjr_d/OneDrive/Documentos/scriptdb.sql` possui 77 tabelas.

Comparacao entre `model.zip` e `scriptdb.sql`:

| Item | Quantidade |
|---|---:|
| Tabelas no `scriptdb.sql` | 77 |
| Tabelas no `model.zip` | 77 |
| Tabelas do script sem entidade no ZIP | 0 |
| Entidades do ZIP sem tabela no script | 0 |

As tabelas tecnicas tambem fazem parte do script:

- `flyway_audit_marker`
- `flyway_schema_history`

Resumo por tabela, comparando o `model.zip` com o backend atual:

| Situacao | Quantidade |
|---|---:|
| Tabelas do modelo que ja possuem tabela mapeada no backend | 36 |
| Tabelas do modelo ainda sem entidade/tabela mapeada no backend | 41 |
| Total no `scriptdb.sql` / `model.zip` | 77 |

## Resumo por dominio

| Dominio | Total no modelo | Ja existe | Falta criar |
|---|---:|---:|---:|
| aluno | 5 | 2 | 3 |
| responsavel | 3 | 3 | 0 |
| matricula | 8 | 6 | 2 |
| catalogo | 8 | 7 | 1 |
| historico | 4 | 2 | 2 |
| avaliacao | 4 | 0 | 4 |
| frequencia | 3 | 0 | 3 |
| professor | 3 | 0 | 3 |
| rh | 2 | 0 | 2 |
| planejamento | 6 | 0 | 6 |
| ia | 6 | 0 | 6 |
| transferencia | 3 | 3 | 0 |
| seguranca | 6 | 4 | 2 |
| dashboard | 5 | 0 | 5 |
| documento | 3 | 3 | 0 |
| compartilhado.pessoa | 3 | 3 | 0 |
| compartilhado.endereco | 3 | 3 | 0 |
| infraestrutura/flyway | 2 | 0 | 2 |

## Tabelas faltantes por dominio

### aluno

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `AlunoHistoricoEventoEntity` | `aluno_historico_evento` | 4 | 3 |
| `SolicitacaoExclusaoAlunoEntity` | `solicitacao_exclusao_aluno` | 8 | 1 |
| `TipoEventoAlunoEntity` | `tipo_evento_aluno` | 3 | 0 |

### matricula

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `MatriculaDocumentoExigidoEntity` | `matricula_documento_exigido` | 4 | 2 |
| `MatriculaDocumentoEntregueEntity` | `matricula_documento_entregue` | 6 | 2 |

### catalogo

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `TurmaDisciplinaEntity` | `turma_disciplina` | 3 | 2 |

### historico

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `BoletimEntity` | `boletim` | 5 | 1 |
| `BoletimItemEntity` | `boletim_item` | 6 | 2 |

### avaliacao

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `TipoAvaliacaoEntity` | `tipo_avaliacao` | 3 | 0 |
| `PeriodoAvaliativoEntity` | `periodo_avaliativo` | 7 | 1 |
| `AvaliacaoEntity` | `avaliacao` | 7 | 3 |
| `NotaAlunoEntity` | `nota_aluno` | 5 | 2 |

### frequencia

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `SituacaoFrequenciaEntity` | `situacao_frequencia` | 3 | 0 |
| `FrequenciaAlunoEntity` | `frequencia_aluno` | 3 | 3 |
| `FrequenciaProfessorEntity` | `frequencia_professor` | 4 | 2 |

### professor

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `ProfessorEntity` | `professor` | 6 | 1 |
| `ProfessorTurmaDisciplinaEntity` | `professor_turma_disciplina` | 5 | 2 |
| `AulaEntity` | `aula` | 8 | 2 |

### rh

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `CargoEntity` | `cargo` | 3 | 0 |
| `FuncionarioEntity` | `funcionario` | 4 | 2 |

### planejamento

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `StatusPlanejamentoEntity` | `status_planejamento` | 3 | 0 |
| `PlanejamentoProfessorEntity` | `planejamento_professor` | 9 | 1 |
| `PlanejamentoAulaEntity` | `planejamento_aula` | 7 | 1 |
| `PlanejamentoBimestralEntity` | `planejamento_bimestral` | 13 | 3 |
| `PlanejamentoBimestralAulaEntity` | `planejamento_bimestral_aula` | 11 | 2 |
| `PlanejamentoBimestralAvaliacaoEntity` | `planejamento_bimestral_avaliacao` | 10 | 2 |

### ia

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `TipoConteudoIAEntity` | `tipo_conteudo_ia` | 3 | 0 |
| `StatusConteudoIAEntity` | `status_conteudo_ia` | 3 | 0 |
| `PlanejamentoIAInteracaoEntity` | `planejamento_ia_interacao` | 8 | 2 |
| `PlanejamentoIAConteudoGeradoEntity` | `planejamento_ia_conteudo_gerado` | 10 | 4 |
| `PlanejamentoIAConteudoVersaoEntity` | `planejamento_ia_conteudo_versao` | 5 | 2 |
| `BibliotecaConteudoPedagogicoEntity` | `biblioteca_conteudo_pedagogico` | 9 | 3 |

### seguranca

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `UsuarioPerfilEntity` | `usuario_perfil` | 2 | 2 |
| `PerfilPermissaoEntity` | `perfil_permissao` | 2 | 2 |

### dashboard

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `PublicoDashboardEntity` | `publico_dashboard` | 3 | 0 |
| `DashboardEntity` | `dashboard` | 6 | 1 |
| `DashboardWidgetEntity` | `dashboard_widget` | 9 | 1 |
| `DashboardUsuarioConfiguracaoEntity` | `dashboard_usuario_configuracao` | 5 | 2 |
| `DashboardIndicadorSnapshotEntity` | `dashboard_indicador_snapshot` | 7 | 1 |

### infraestrutura/flyway

| Entidade do ZIP | Tabela | Colunas | Relacoes |
|---|---|---:|---:|
| `FlywayAuditMarkerEntity` | `flyway_audit_marker` | 3 | 0 |
| `FlywaySchemaHistoryEntity` | `flyway_schema_history` | 10 | 0 |

## Tabelas ja cobertas pelo backend atual

| Dominio | Tabela | Entidade do ZIP | Classe atual |
|---|---|---|---|
| aluno | `aluno` | `AlunoEntity` | `AlunoEntity` |
| aluno | `status_aluno` | `StatusAlunoEntity` | `StatusAlunoEntity` |
| responsavel | `responsavel` | `ResponsavelEntity` | `ResponsavelEntity` |
| responsavel | `parentesco` | `ParentescoEntity` | `ParentescoEntity` |
| responsavel | `aluno_responsavel` | `AlunoResponsavelEntity` | `AlunoResponsavelEntity` |
| matricula | `matricula` | `MatriculaEntity` | `MatriculaEntity` |
| matricula | `matricula_etapa` | `MatriculaEtapaEntity` | `MatriculaEtapaEntity` |
| matricula | `etapa_matricula_modelo` | `EtapaMatriculaModeloEntity` | `EtapaMatriculaModeloEntity` |
| matricula | `status_matricula` | `StatusMatriculaEntity` | `StatusMatriculaEntity` |
| matricula | `status_etapa_matricula` | `StatusEtapaMatriculaEntity` | `StatusEtapaMatriculaEntity` |
| matricula | `tipo_matricula` | `TipoMatriculaEntity` | `TipoMatriculaEntity` |
| catalogo | `nivel_ensino` | `NivelEnsinoEntity` | `NivelEnsinoEntity` |
| catalogo | `serie` | `SerieEntity` | `SerieEntity` |
| catalogo | `turma` | `TurmaEntity` | `TurmaEntity` |
| catalogo | `turno` | `TurnoEntity` | `TurnoEntity` |
| catalogo | `disciplina` | `DisciplinaEntity` | `DisciplinaEntity` |
| catalogo | `periodo_letivo` | `PeriodoLetivoEntity` | `PeriodoLetivoEntity` |
| catalogo | `escola` | `EscolaEntity` | `EscolaOrigemEntity` |
| historico | `historico_escolar` | `HistoricoEscolarEntity` | `HistoricoEscolar` |
| historico | `historico_escolar_item` | `HistoricoEscolarItemEntity` | `HistoricoEscolarItem` |
| transferencia | `transferencia_aluno` | `TransferenciaAlunoEntity` | `TransferenciaAlunoEntity` |
| transferencia | `tipo_transferencia` | `TipoTransferenciaEntity` | `TipoTransferenciaEntity` |
| transferencia | `status_transferencia` | `StatusTransferenciaEntity` | `StatusTransferenciaEntity` |
| seguranca | `usuario` | `UsuarioEntity` | `UsuarioEntity` |
| seguranca | `perfil` | `PerfilEntity` | `PerfilEntity` |
| seguranca | `permissao` | `PermissaoEntity` | `PermissaoEntity` |
| seguranca | `sessao_autenticacao` | `SessaoAutenticacaoEntity` | `SessaoAutenticacaoEntity` |
| documento | `documento` | `DocumentoEntity` | `DocumentoEntity` |
| documento | `tipo_documento` | `TipoDocumentoEntity` | `TipoDocumentoEntity` |
| documento | `pessoa_documento` | `PessoaDocumentoEntity` | `PessoaDocumentoEntity` |
| compartilhado.pessoa | `pessoa` | `PessoaEntity` | `PessoaEntity` |
| compartilhado.pessoa | `tipo_pessoa` | `TipoPessoaEntity` | `TipoPessoaEntity` |
| compartilhado.pessoa | `pessoa_tipo_pessoa` | `PessoaTipoPessoaEntity` | `PessoaTipoPessoaEntity` |
| compartilhado.endereco | `endereco` | `EnderecoEntity` | `EnderecoEntity` |
| compartilhado.endereco | `tipo_endereco` | `TipoEnderecoEntity` | `TipoEnderecoEntity` |
| compartilhado.endereco | `pessoa_endereco` | `PessoaEnderecoEntity` | `PessoaEnderecoEntity` |

## Conflitos e pontos de decisao

1. `escola` esta coberta hoje por `EscolaOrigemEntity` dentro de `transferencia`, mas o modelo pede `EscolaEntity` em `catalogo`.
   - Decisao sugerida: na Fase 2 ou 3, separar `catalogo.adapter.out.persistence.entity.EscolaEntity` e ajustar a transferencia para referenciar escola/origem conforme o modelo oficial.

2. `HistoricoEscolarEntity` e `HistoricoEscolarItemEntity` no ZIP correspondem as tabelas ja existentes, mas as classes atuais se chamam `HistoricoEscolar` e `HistoricoEscolarItem`.
   - Decisao sugerida: renomear as classes para o padrao `*Entity` somente se voce quiser padronizacao total. Nao e obrigatorio para compilar.

3. `docs/20-base-dados-oficial-scriptdb.md` aponta `docs/v2/scriptdb.sql` como base ativa do repositorio, mas o modelo anexado nesta etapa esta baseado em `C:/Users/pjr_d/OneDrive/Documentos/scriptdb.sql`.
   - Decisao sugerida: copiar/substituir o script oficial no repositorio em uma fase propria, para evitar divergencia entre documentacao e base real.

4. As tabelas `flyway_audit_marker` e `flyway_schema_history` sao tecnicas.
   - Decisao sugerida: nao criar dominio de negocio para elas. Se for necessario mapear, usar pacote tecnico separado, nao os dominios escolares.

## Ordem sugerida para as proximas fases

### Fase 2 - cadastros de apoio e vinculos simples

Criar entidades/repositories faltantes sem mexer em fluxo REST:

- `aluno`: `TipoEventoAlunoEntity`, `AlunoHistoricoEventoEntity`, `SolicitacaoExclusaoAlunoEntity`
- `seguranca`: `UsuarioPerfilEntity`, `PerfilPermissaoEntity`
- `catalogo`: `TurmaDisciplinaEntity`
- `matricula`: `MatriculaDocumentoExigidoEntity`, `MatriculaDocumentoEntregueEntity`

### Fase 3 - professor, RH e frequencia

- `professor`
- `rh`
- `frequencia`

### Fase 4 - avaliacao e historico complementar

- `avaliacao`
- `boletim`
- `boletim_item`

### Fase 5 - planejamento e IA

- `planejamento`
- `ia`

### Fase 6 - dashboard

- `dashboard`
- widgets
- snapshots
- configuracoes por usuario

## Criterio para avancar

Ao final de cada fase:

1. Todas as entidades criadas devem estar no pacote de dominio correto.
2. O projeto deve compilar.
3. `mvnw.cmd test` deve passar.
4. Nenhum endpoint existente deve ser removido ou renomeado.
5. A proxima fase so deve iniciar apos validacao do resultado anterior.
