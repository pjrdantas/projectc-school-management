# Fase 48B - Fronteiras iniciais de servicos e contratos internos

## Objetivo

Definir fronteiras iniciais de servicos de dominio e contratos internos antes de qualquer extracao de codigo do monolito.

Esta fase e documental. Ela nao cria microservicos, nao cria BFFs, nao altera controllers, nao altera banco e nao introduz Kafka, MongoDB ou Redis.

## Premissas

- O monolito `school-management-service` continua sendo a implementacao oficial.
- Os BFFs desenhados na Fase 48A devem consumir contratos de dominio estaveis no futuro.
- As fronteiras devem seguir responsabilidade funcional e ciclo de mudanca, nao apenas pastas ou tabelas.
- Todo contrato futuro deve preservar contexto multi-escola por `escolaId`.
- A extracao so deve acontecer quando o contrato estiver estavel e houver ganho operacional claro.

## Baseline observado

O backend atual ja possui organizacao por dominios internos:

- `seguranca`
- `institucional`
- `compartilhado`
- `aluno`
- `responsavel`
- `catalogo`
- `matricula`
- `documento`
- `transferencia`
- `historico`
- `professor`
- `frequencia`
- `avaliacao`
- `planejamento`
- `ia`
- `dashboard`
- `rh`

Tambem ja existem pontos transversais relevantes:

- `EscolaTenantService` como resolvedor de escola padrao.
- `PessoaFoundationService` como base compartilhada para criacao/manutencao de pessoa, endereco e tipos de pessoa.
- Uso recorrente de `escolaId` e `escolaNome` em contratos ja escopados.
- Relacionamentos fortes entre entidades de dominio por JPA, especialmente em alunos, matriculas, aulas, frequencias, avaliacoes, historico e IA.

## Fronteiras candidatas

### 1. Servico de identidade, acesso e contexto escolar

Responsabilidade:

- Autenticacao.
- Sessao.
- Usuarios.
- Perfis.
- Permissoes.
- Contexto de escola ativa.
- Vinculo futuro entre usuario e multiplas escolas.

Pacotes atuais relacionados:

- `seguranca`
- `institucional`

Contratos internos candidatos:

- `AutenticacaoPort`
  - autenticar credenciais;
  - renovar sessao;
  - encerrar sessao.
- `UsuarioContextoPort`
  - obter usuario autenticado;
  - listar perfis e permissoes;
  - obter escolas vinculadas;
  - resolver escola ativa.
- `EscolaContextoPort`
  - obter escola padrao;
  - validar acesso do usuario a escola;
  - trocar escola ativa no futuro.

Dono dos dados:

- `usuario`
- `perfil`
- `permissao`
- `usuario_perfil`
- `perfil_permissao`
- `sessao_autenticacao`
- `escola`
- vinculos usuario-escola futuros.

Consumidores futuros:

- Todos os BFFs.
- Todos os servicos de dominio que exigirem escopo por escola.

Nao extrair ainda porque:

- Ainda nao ha troca real de escola ativa.
- O contrato de usuario com multiplas escolas ainda precisa ser implementado.
- A autorizacao fina por permissao ainda nao foi revisada como fase propria.

### 2. Servico de pessoas e vinculos cadastrais

Responsabilidade:

- Pessoa base.
- Enderecos.
- Alunos.
- Responsaveis.
- Vinculo aluno-responsavel.
- Consulta cadastral.
- Funcionarios como base para professor.

Pacotes atuais relacionados:

- `compartilhado.pessoa`
- `compartilhado.endereco`
- `aluno`
- `responsavel`
- `rh`

Contratos internos candidatos:

- `PessoaPort`
  - criar ou atualizar pessoa;
  - manter endereco;
  - consultar catalogos de pessoa.
- `AlunoPort`
  - criar aluno;
  - atualizar aluno;
  - buscar aluno por id e escola;
  - listar alunos por filtros;
  - solicitar ou executar exclusao controlada.
- `ResponsavelPort`
  - criar responsavel;
  - atualizar responsavel;
  - vincular responsavel ao aluno;
  - desvincular responsavel do aluno;
  - listar responsaveis do aluno.
- `FuncionarioPort`
  - listar funcionarios elegiveis;
  - validar funcionario ativo.

Dono dos dados:

- pessoa e endereco;
- aluno e status de aluno;
- responsavel, parentesco e vinculos;
- funcionario e cargo.

Consumidores futuros:

- `bff-secretaria`
- `bff-professor`
- servico de matriculas;
- servico pedagogico;
- servico de historico.

Nao extrair ainda porque:

- Aluno hoje possui dependencias diretas com responsaveis, documentos, historicos e transferencias durante exclusao.
- `PessoaFoundationService` ainda e uma fundacao compartilhada dentro do mesmo processo.
- O limite entre pessoa, aluno, responsavel e funcionario precisa de portas explicitas antes de virar rede.

### 3. Servico academico de catalogos e estrutura escolar

Responsabilidade:

- Periodos letivos.
- Series.
- Turnos.
- Disciplinas.
- Turmas.
- Vinculo turma-disciplina.
- Catalogos academicos usados por matricula, professor, planejamento e dashboards.

Pacotes atuais relacionados:

- `catalogo`

Contratos internos candidatos:

- `CatalogoAcademicoPort`
  - listar periodos letivos por escola;
  - listar series por escola;
  - listar turnos por escola;
  - listar disciplinas por escola;
  - listar turmas por escola;
  - validar turma, serie, periodo e disciplina por escola.
- `EstruturaTurmaPort`
  - obter turma com serie, periodo e turno;
  - obter disciplinas vinculadas a turma;
  - validar capacidade quando aplicavel.

Dono dos dados:

- periodo letivo;
- serie;
- turno;
- disciplina;
- turma;
- turma-disciplina.

Consumidores futuros:

- `bff-secretaria`
- `bff-professor`
- `bff-diretor`
- servico de matriculas;
- servico pedagogico;
- servico de planejamento/IA;
- servico de dashboards.

Pode ser candidato a extracao precoce porque:

- E majoritariamente referencial.
- Tem poucos comandos transacionais complexos.
- E lido por varios dominios.

Risco:

- Se extraido cedo demais, pode gerar muitas chamadas sincronas para telas simples. Cache futuro pode ajudar, mas nao deve ser introduzido antes de necessidade concreta.

### 4. Servico de secretaria, matriculas, documentos e transferencias

Responsabilidade:

- Matriculas.
- Rematriculas.
- Etapas de matricula.
- Documentos exigidos e entregues.
- Documentos do aluno.
- Transferencias.
- Escolas de origem.
- Dossie operacional do aluno para secretaria.

Pacotes atuais relacionados:

- `matricula`
- `documento`
- `transferencia`
- parte de `aluno`
- parte de `historico`

Contratos internos candidatos:

- `MatriculaPort`
  - criar matricula;
  - consultar matricula;
  - listar por aluno, turma, periodo e status;
  - avancar etapas;
  - validar rematricula;
  - concluir dados academicos.
- `DocumentoAlunoPort`
  - listar documentos do aluno;
  - registrar documento;
  - atualizar documento;
  - remover documento;
  - consultar pendencias documentais.
- `TransferenciaPort`
  - criar transferencia;
  - listar transferencias por aluno;
  - validar escola de origem;
  - consultar status.
- `DossieSecretariaPort`
  - compor visao operacional de aluno para secretaria;
  - expor pendencias e acoes permitidas.

Dono dos dados:

- matricula;
- etapa de matricula;
- tipo/status de matricula;
- documentos exigidos e entregues;
- documento;
- transferencia;
- escola de origem.

Consumidores futuros:

- `bff-secretaria`
- `bff-diretor`
- servico de dashboards;
- portal responsavel/aluno futuro.

Nao extrair ainda porque:

- Ha dependencias fortes com aluno, turma, periodo letivo, documentos e historico.
- A rematricula e a conclusao academica ainda pertencem a um fluxo operacional integrado.
- Antes da extracao, os comandos precisam ser agrupados em contratos de fluxo e nao em acesso direto a repositorios.

### 5. Servico pedagogico de professores, aulas, frequencias e avaliacoes

Responsabilidade:

- Professores.
- Vinculo professor-turma-disciplina.
- Aulas.
- Frequencia do professor.
- Frequencia dos alunos.
- Avaliacoes.
- Notas.
- Diario de turma.

Pacotes atuais relacionados:

- `professor`
- `frequencia`
- `avaliacao`
- parte de `matricula`
- parte de `catalogo`

Contratos internos candidatos:

- `ProfessorPort`
  - criar professor a partir de funcionario elegivel;
  - consultar professor;
  - listar professores por escola;
  - manter vinculos com turma-disciplina.
- `DiarioAulaPort`
  - criar aula;
  - consultar aula;
  - registrar frequencia do professor;
  - registrar frequencia de alunos;
  - listar aulas por turma, disciplina e professor.
- `AvaliacaoPort`
  - criar avaliacao;
  - consultar avaliacao;
  - registrar notas;
  - listar notas por matricula;
  - validar turma, disciplina e periodo avaliativo.

Dono dos dados:

- professor;
- professor-turma-disciplina;
- aula;
- frequencias;
- avaliacao;
- nota.

Consumidores futuros:

- `bff-professor`
- `bff-diretor`
- servico de planejamento/IA;
- servico de dashboards;
- servico de historico/boletim.

Nao extrair ainda porque:

- A consistencia entre aula, matricula, turma, frequencia e avaliacao ainda depende de entidades compartilhadas no mesmo banco.
- O servico de historico/boletim consome notas e frequencias diretamente.
- O contrato de diario de turma precisa ser estabilizado antes de virar fronteira remota.

### 6. Servico de planejamento e IA pedagogica

Responsabilidade:

- Planejamento bimestral.
- Aulas previstas.
- Avaliacoes previstas.
- Status de planejamento.
- Interacoes de IA.
- Conteudos gerados.
- Versoes.
- Aprovacao e publicacao em biblioteca pedagogica.
- Reuso de conteudo aprovado.

Pacotes atuais relacionados:

- `planejamento`
- `ia`
- parte de `professor`
- parte de `catalogo`

Contratos internos candidatos:

- `PlanejamentoPedagogicoPort`
  - criar planejamento;
  - atualizar planejamento;
  - listar por professor, turma, disciplina, periodo e status;
  - alterar status;
  - manter aulas previstas;
  - manter avaliacoes previstas.
- `ConteudoIAPort`
  - solicitar conteudo;
  - registrar interacao;
  - listar conteudos de planejamento;
  - versionar conteudo;
  - aprovar versao;
  - publicar na biblioteca.
- `BibliotecaPedagogicaPort`
  - listar conteudos aprovados;
  - filtrar por escola, disciplina, serie, tipo e tema;
  - reutilizar conteudo em planejamento.

Dono dos dados:

- planejamento bimestral;
- aulas e avaliacoes previstas;
- status de planejamento;
- interacoes de IA;
- conteudos de IA;
- versoes;
- biblioteca pedagogica.

Consumidores futuros:

- `bff-professor`
- `bff-diretor`
- servico de dashboards;
- possivel worker de IA futuro.

Pode evoluir para servico separado depois porque:

- Tem ciclo de mudanca proprio.
- Pode precisar de armazenamento documental no futuro para respostas completas de IA.
- Pode gerar eventos de aprovacao e publicacao.

Nao extrair ainda porque:

- O provedor real de IA ainda nao e obrigatorio.
- Os contratos ainda foram estabilizados recentemente.
- A biblioteca pedagogica depende de politica multi-escola ainda simples.

### 7. Servico de historico, boletins e vida escolar

Responsabilidade:

- Boletim.
- Historico escolar.
- Componentes curriculares de historico.
- Geracao de historico a partir de boletim.
- Vida escolar consolidada do aluno.

Pacotes atuais relacionados:

- `historico`
- parte de `avaliacao`
- parte de `frequencia`
- parte de `matricula`
- parte de `aluno`

Contratos internos candidatos:

- `BoletimPort`
  - consultar boletim por matricula;
  - compor notas e frequencias;
  - consolidar resultado academico.
- `HistoricoEscolarPort`
  - criar historico;
  - atualizar historico;
  - listar por aluno;
  - gerar historico a partir de boletim;
  - remover historico com validacao de escola.
- `VidaEscolarPort`
  - compor linha do tempo escolar do aluno;
  - expor matriculas, boletins, transferencias e historicos.

Dono dos dados:

- boletim;
- itens de boletim;
- historico escolar;
- componentes curriculares de historico.

Consumidores futuros:

- `bff-secretaria`
- `bff-diretor`
- portal responsavel/aluno futuro.

Nao extrair ainda porque:

- Boletim depende de notas, frequencias, matricula, turma e periodo.
- Historico ainda e muito proximo da secretaria e da conclusao academica.
- A vida escolar consolidada pode ser inicialmente um contrato interno do monolito.

### 8. Servico de dashboards e analytics

Responsabilidade:

- Dashboard academico.
- Dashboard secretaria.
- Dashboard diretor.
- Dashboard professor.
- Alertas.
- Configuracoes de dashboard.
- Snapshots e comparativos.

Pacotes atuais relacionados:

- `dashboard`

Contratos internos candidatos:

- `DashboardOperacionalPort`
  - obter dashboard academico;
  - obter dashboard secretaria;
  - obter dashboard professor;
  - obter dashboard diretor.
- `AlertaEscolarPort`
  - listar alertas por escola e perfil;
  - priorizar alertas;
  - vincular alerta a rota operacional.
- `SnapshotDashboardPort`
  - gerar snapshot;
  - listar snapshots;
  - comparar indicadores historicos.
- `DashboardConfiguracaoPort`
  - listar dashboards e widgets;
  - manter configuracoes por usuario.

Dono dos dados:

- dashboard;
- widgets;
- configuracoes de usuario;
- snapshots;
- indicadores persistidos.

Consumidores futuros:

- `bff-diretor`
- `bff-admin-shell`
- `bff-professor`
- `bff-secretaria`

Pode ser bom candidato a extracao piloto no futuro porque:

- Ja tem fronteira funcional clara.
- A carga e mais leitura/agregacao do que transacao.
- Foi indicado no roadmap como candidato para extracao piloto de microfrontend.

Risco:

- Atualmente agrega dados de muitos dominios. Antes de extrair, precisa trocar acesso direto a repositorios por contratos internos ou eventos/snapshots.

### 9. Servico de notificacoes

Status:

- Futuro. Ainda nao ha pacote operacional dedicado observado nesta fase.

Responsabilidade futura:

- Comunicados.
- Avisos para responsaveis.
- Notificacoes de pendencia documental.
- Alertas de frequencia, nota, planejamento e matricula.

Contratos internos candidatos:

- `NotificacaoPort`
  - registrar notificacao;
  - listar notificacoes por usuario ou papel;
  - marcar leitura;
  - emitir aviso para canal externo futuro.

Nao implementar agora porque:

- Nao ha necessidade funcional concreta validada nesta fase.
- Depende de decisoes de portal externo, canais e LGPD.

## Matriz BFF x servicos candidatos

| BFF | Servicos candidatos principais |
| --- | --- |
| `bff-admin-shell` | identidade/acesso/contexto escolar, dashboards/configuracao |
| `bff-secretaria` | pessoas/vinculos, catalogos academicos, secretaria/matriculas/documentos/transferencias, historico/vida escolar, dashboards |
| `bff-professor` | identidade/contexto, catalogos academicos, pedagogico, planejamento/IA, dashboards |
| `bff-diretor` | dashboards/analytics, catalogos academicos, secretaria, pedagogico, planejamento/IA, historico |
| `bff-portal-responsavel` | identidade externa futura, pessoas/vinculos, secretaria/documentos, historico/vida escolar, notificacoes |

## Contrato interno minimo recomendado

Antes de extrair qualquer servico, cada fronteira deve ter portas internas com:

- contexto explicito de escola;
- DTOs de entrada e saida que nao exponham entidades JPA;
- erros de dominio mapeaveis para HTTP;
- operacoes de leitura separadas de comandos;
- paginacao padronizada para listas;
- ids opacos e estaveis;
- testes de contrato no monolito.

Exemplo de contexto:

```java
public record EscolaContexto(
        UUID escolaId,
        String escolaNome,
        UUID usuarioId,
        Set<String> perfis,
        Set<String> permissoes
) {
}
```

Exemplo de porta:

```java
public interface MatriculaPort {
    MatriculaResumoOutput buscarPorId(EscolaContexto contexto, UUID matriculaId);
    Page<MatriculaResumoOutput> listar(EscolaContexto contexto, MatriculaFiltro filtro, Pageable pageable);
    MatriculaResumoOutput criar(EscolaContexto contexto, CriarMatriculaCommand command);
}
```

## Eventos candidatos sem Kafka imediato

Os eventos abaixo devem ser tratados primeiro como eventos de dominio internos ou registros de auditoria. Kafka so deve entrar quando houver servicos separados ou processamento assincrono real.

| Evento candidato | Produtor natural | Consumidores futuros |
| --- | --- | --- |
| `school.enrollment.created` | matriculas | dashboards, notificacoes, portal |
| `school.enrollment.completed` | matriculas | historico, dashboards, notificacoes |
| `school.document.pending` | documentos/matriculas | secretaria, notificacoes, dashboards |
| `school.transfer.requested` | transferencias | secretaria, dashboards, notificacoes |
| `school.lesson.completed` | pedagogico | dashboards, historico |
| `school.attendance.recorded` | pedagogico | dashboards, historico, notificacoes |
| `school.assessment.published` | pedagogico | dashboards, portal |
| `school.grade.recorded` | pedagogico | historico, dashboards, portal |
| `school.planning.approved` | planejamento | dashboards, biblioteca, notificacoes |
| `school.ai-content.approved` | planejamento/IA | biblioteca, dashboards |
| `school.dashboard.snapshot.requested` | dashboards | worker futuro de analytics |

## Ordem de estabilizacao recomendada

1. Consolidar contratos internos de contexto escolar e usuario.
2. Criar portas internas para catalogos academicos, por serem dependencia ampla.
3. Criar portas internas para pessoas/alunos/responsaveis sem extrair banco.
4. Criar contrato de dossie de secretaria no monolito.
5. Criar contrato de diario pedagogico no monolito.
6. Criar contrato de workspace de planejamento/IA no monolito.
7. Isolar dashboards para consumir contratos internos ou snapshots.
8. Somente depois avaliar extracao piloto de um dominio com baixo risco.

## Criterios para extrair um servico

Um servico so deve ser extraido quando:

- possui contrato interno estavel;
- nao depende de entidades JPA de outro dominio;
- tem dono claro de dados;
- possui testes de contrato suficientes;
- suporta contexto multi-escola sem depender de estado global implicito;
- tem carga, escala, ciclo de deploy ou isolamento que justifique o custo;
- tem plano de observabilidade e rollback.

## Anti-objetivos desta fase

- Nao criar novo projeto Spring.
- Nao mover pacotes.
- Nao criar clients HTTP internos.
- Nao criar fila ou topicos reais.
- Nao introduzir bancos adicionais.
- Nao alterar frontend ou rotas.
- Nao transformar DTOs atuais em contratos finais sem fase especifica.

## Criterios para considerar a Fase 48B concluida

- Fronteiras candidatas documentadas.
- Pacotes atuais relacionados mapeados.
- Dono de dados e consumidores futuros registrados.
- Contratos internos candidatos propostos.
- Eventos candidatos listados sem implementacao de Kafka.
- Criterios de extracao definidos.

## Proxima fase sugerida

Fase 48C - contratos internos de contexto escolar e catalogos academicos no monolito.

Objetivo da proxima fase:

- Criar ou consolidar portas internas pequenas para contexto escolar e catalogos academicos.
- Manter tudo no `school-management-service`.
- Nao criar BFF real.
- Nao extrair microservicos.
- Validar backend com `.\mvnw.cmd test` se houver alteracao de codigo.
