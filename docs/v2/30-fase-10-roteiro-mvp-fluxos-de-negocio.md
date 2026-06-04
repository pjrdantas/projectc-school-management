# Fase 10 - Roteiro MVP de fluxos de negocio

Data: 2026-06-03

## Objetivo

Definir a sequencia pragmatica para sair da estrutura JPA criada nas fases anteriores e avancar para fluxos funcionais de produto.

As fases 1 a 7 estruturaram a persistencia. Esta fase define a ordem recomendada para criar:

- services;
- DTOs;
- controllers;
- validacoes;
- testes de caso de uso;
- telas ou integracoes futuras.

## Principio adotado

Implementar primeiro os fluxos que desbloqueiam os demais.

Evitar iniciar por dashboard, IA ou relatorios antes de estabilizar cadastros, matricula, turma, professor, frequencia e avaliacao.

## Sequencia recomendada

## 1. Catalogo academico

Objetivo:

- Garantir que a escola consiga configurar a base operacional minima do ano letivo.

Entidades envolvidas:

- `Escola`
- `NivelEnsino`
- `Serie`
- `Turno`
- `PeriodoLetivo`
- `Turma`
- `Disciplina`
- `TurmaDisciplina`

Entregas esperadas:

- Services de cadastro e consulta.
- Controllers REST para cadastros principais.
- DTOs de entrada e saida.
- Validacoes de campos obrigatorios.
- Consulta de turmas por periodo, serie e turno.
- Vinculo de disciplinas a turmas.

Critério de saida:

- Criar uma turma completa com disciplinas vinculadas.

## 2. Aluno e responsavel

Objetivo:

- Permitir cadastrar o aluno, seus dados de pessoa, endereco e responsaveis.

Entidades envolvidas:

- `Pessoa`
- `TipoPessoa`
- `PessoaTipoPessoa`
- `Endereco`
- `PessoaEndereco`
- `Aluno`
- `StatusAluno`
- `Responsavel`
- `Parentesco`
- `AlunoResponsavel`

Entregas esperadas:

- Cadastro de aluno com pessoa associada.
- Cadastro de responsavel com pessoa associada.
- Vinculo aluno-responsavel.
- Busca de aluno por filtros basicos.
- Endpoint de detalhes do aluno com responsaveis.

Critério de saida:

- Cadastrar aluno e responsavel, vincular ambos e consultar a ficha consolidada do aluno.

## 3. Matricula

Objetivo:

- Transformar aluno cadastrado em aluno matriculado em uma turma e periodo letivo.

Entidades envolvidas:

- `Matricula`
- `MatriculaEtapa`
- `EtapaMatriculaModelo`
- `StatusMatricula`
- `StatusEtapaMatricula`
- `TipoMatricula`
- `MatriculaDocumentoExigido`
- `MatriculaDocumentoEntregue`
- `Documento`
- `PessoaDocumento`

Entregas esperadas:

- Criar matricula para aluno.
- Associar turma, periodo letivo e tipo de matricula.
- Controlar etapas de matricula.
- Listar documentos exigidos.
- Registrar documentos entregues.
- Atualizar status da matricula.

Critério de saida:

- Matricular um aluno em turma ativa e consultar a situacao da matricula com etapas e documentos.

## 4. Professor, RH e alocacao em turma

Objetivo:

- Permitir que professores sejam cadastrados e associados a disciplinas/turmas.

Entidades envolvidas:

- `Pessoa`
- `Funcionario`
- `Cargo`
- `Professor`
- `ProfessorTurmaDisciplina`
- `TurmaDisciplina`

Entregas esperadas:

- Cadastro de funcionario/professor.
- Vinculo professor com turma e disciplina.
- Consulta da grade de aulas/disciplinas por professor.
- Consulta de professores por turma.

Critério de saida:

- Vincular um professor a uma disciplina de uma turma e consultar essa alocacao.

## 5. Aulas, frequencia e avaliacao

Objetivo:

- Registrar a execucao pedagogica minima: aulas, presencas, avaliacoes e notas.

Entidades envolvidas:

- `Aula`
- `FrequenciaAluno`
- `FrequenciaProfessor`
- `SituacaoFrequencia`
- `TipoAvaliacao`
- `PeriodoAvaliativo`
- `Avaliacao`
- `NotaAluno`

Entregas esperadas:

- Criar aula vinculada a professor/turma/disciplina.
- Registrar frequencia dos alunos.
- Registrar frequencia do professor.
- Criar avaliacao.
- Lançar notas.
- Consultar notas e frequencia por matricula.

Critério de saida:

- Para uma turma com alunos matriculados, registrar uma aula, frequencia, avaliacao e notas.

## 6. Historico e boletim

Objetivo:

- Consolidar resultado academico para leitura pelo aluno/responsavel e secretaria.

Entidades envolvidas:

- `Boletim`
- `BoletimItem`
- `HistoricoEscolar`
- `HistoricoEscolarItem`

Entregas esperadas:

- Gerar boletim por matricula e periodo avaliativo.
- Consultar boletim com notas por disciplina.
- Preparar historico escolar a partir dos registros consolidados.

Critério de saida:

- Consultar boletim de um aluno matriculado com notas e dados de disciplina.

## 7. Planejamento e IA

Objetivo:

- Apoiar o professor na organizacao de aulas e planejamentos.

Entidades envolvidas:

- `PlanejamentoProfessor`
- `PlanejamentoAula`
- `PlanejamentoBimestral`
- `PlanejamentoBimestralAula`
- `PlanejamentoBimestralAvaliacao`
- `PlanejamentoIAInteracao`
- `PlanejamentoIAConteudoGerado`
- `PlanejamentoIAConteudoVersao`
- `BibliotecaConteudoPedagogico`

Entregas esperadas:

- Criar planejamento do professor.
- Criar planejamento bimestral.
- Vincular aulas planejadas.
- Registrar conteudo gerado por IA.
- Versionar conteudo aprovado.

Critério de saida:

- Professor criar planejamento bimestral, gerar conteudo auxiliar e aprovar uma versao.

## 8. Dashboard

Objetivo:

- Exibir indicadores apenas depois que os fluxos geradores de dados estiverem funcionando.

Entidades envolvidas:

- `PublicoDashboard`
- `Dashboard`
- `DashboardWidget`
- `DashboardIndicadorSnapshot`
- `DashboardUsuarioConfiguracao`

Entregas esperadas:

- Configurar dashboards por publico.
- Configurar widgets.
- Salvar snapshots de indicadores.
- Permitir configuracao por usuario.

Critério de saida:

- Exibir indicadores reais de matricula, frequencia e avaliacao.

## Padrao minimo por fluxo

Cada fluxo deve entregar:

- Entidades ja mapeadas usadas sem alterar o banco.
- Repository com consultas necessarias ao caso de uso.
- Service com regra de negocio.
- DTOs de request/response.
- Controller REST.
- Teste de service para regra principal.
- Teste de integracao ou controller para o endpoint principal.

## Ordem de commits sugerida

Criar commits pequenos por fluxo:

1. `catalogo`: cadastros e vinculos academicos.
2. `aluno-responsavel`: ficha do aluno.
3. `matricula`: criacao e etapas.
4. `professor-alocacao`: vinculo professor/turma/disciplina.
5. `frequencia-avaliacao`: aula, presenca e nota.
6. `boletim-historico`: consolidacao academica.
7. `planejamento-ia`: apoio pedagogico.
8. `dashboard`: indicadores.

## Proxima fase recomendada

Antes de iniciar os controllers e services, criar testes JPA focados nos relacionamentos mais importantes.

Isso reduz o risco de descobrir problemas de mapeamento apenas quando os fluxos REST forem implementados.
