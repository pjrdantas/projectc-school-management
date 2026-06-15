# Fase 48F - Consolidacao dos contratos internos usados

## Objetivo

Consolidar o estado atual dos contratos internos criados nas Fases 48C, 48D e 48E, deixando claro onde eles ja sao usados, quais limites ainda existem e quando sera necessario criar BFFs, servicos separados ou novos componentes runtime.

Esta fase e documental. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Contratos internos existentes

### `EscolaContextoPort`

Pacote:

- `br.com.escola.institucional.application.port`

DTO principal:

- `EscolaContexto`

Implementacao atual:

- `EscolaTenantService`

Responsabilidade atual:

- Resolver contexto da escola padrao.
- Resolver contexto de escola a partir de usuario.
- Validar se um usuario pode acessar uma escola.

Uso atual:

- `DashboardAcademicoService`
- `DashboardSecretariaService`
- `DashboardDiretorService`
- `DashboardProfessorService`
- `DashboardIndicadorSnapshotService`
- `MatriculaAcademicoResumoService`
- `AlunoConsultaPersistenceGateway`
- `PeriodoLetivoConsultaPersistenceGateway`
- `TurmaConsultaPersistenceGateway`

Limites atuais:

- Ainda nao ha troca dinamica de escola ativa.
- Ainda nao ha usuario com multiplas escolas.
- O contexto de perfis e permissoes e preparado no DTO, mas ainda nao e o eixo de autorizacao dos fluxos de dominio.
- Ainda ha fluxos fora da area de dashboards que usam a entidade de escola diretamente por necessidade transacional.

### `CatalogoAcademicoPort`

Pacote:

- `br.com.escola.catalogo.application.port.internal`

DTOs principais:

- `PeriodoLetivoResumo`
- `SerieResumo`
- `TurnoResumo`
- `DisciplinaResumo`
- `TurmaResumo`

Implementacao atual:

- `CatalogoAcademicoInternalService`

Responsabilidade atual:

- Listar e validar catalogos academicos por escola.
- Expor visoes internas de leitura sem vazar entidades JPA.
- Manter a escola como parametro explicito.

Uso atual:

- `DashboardAcademicoService`

Limites atuais:

- Ainda nao substitui use cases de criacao e edicao de catalogos.
- Ainda nao e usado pelos fluxos de matricula, aula, avaliacao ou planejamento para todas as validacoes.
- Ainda nao possui cache. Cache so deve ser discutido quando houver necessidade concreta.

### `EstruturaTurmaPort`

Pacote:

- `br.com.escola.catalogo.application.port.internal`

DTOs principais:

- `TurmaResumo`
- `TurmaDisciplinaResumo`

Implementacao atual:

- `CatalogoAcademicoInternalService`

Responsabilidade atual:

- Obter turma por escola.
- Listar disciplinas de turma.
- Validar se turma possui disciplina.
- Validar se turma pertence a periodo letivo.

Uso atual:

- `PlanejamentoBimestralService`
- `DiarioAulaService`
- `AvaliacaoService`

Limites atuais:

- Ainda nao substitui validacoes diretas de repositories nos fluxos transacionais.
- Ainda nao e usado por todos os fluxos que manipulam turma-disciplina.

## Matriz de uso atual

| Contrato interno | Implementacao | Consumidor atual | Tipo de uso |
| --- | --- | --- | --- |
| `EscolaContextoPort` | `EscolaTenantService` | `DashboardAcademicoService` | Resolver contexto escolar padrao para agregacao academica |
| `EscolaContextoPort` | `EscolaTenantService` | `DashboardSecretariaService` | Resolver contexto escolar padrao para agregacao operacional da secretaria |
| `EscolaContextoPort` | `EscolaTenantService` | `DashboardDiretorService` | Resolver contexto escolar padrao para agregacao executiva do diretor |
| `EscolaContextoPort` | `EscolaTenantService` | `DashboardProfessorService` | Resolver contexto escolar padrao para agregacao operacional do professor |
| `EscolaContextoPort` | `EscolaTenantService` | `DashboardIndicadorSnapshotService` | Resolver contexto escolar padrao para listagem, historico e persistencia de snapshots |
| `EscolaContextoPort` | `EscolaTenantService` | `MatriculaAcademicoResumoService` | Resolver contexto escolar padrao para consulta academica de matricula |
| `EscolaContextoPort` | `EscolaTenantService` | `AlunoConsultaPersistenceGateway` | Resolver contexto escolar padrao para validacao de aluno em matricula |
| `EscolaContextoPort` | `EscolaTenantService` | `PeriodoLetivoConsultaPersistenceGateway` | Resolver contexto escolar padrao para validacao de periodo letivo em matricula |
| `EscolaContextoPort` | `EscolaTenantService` | `TurmaConsultaPersistenceGateway` | Resolver contexto escolar padrao para consultas auxiliares de turma em matricula |
| `CatalogoAcademicoPort` | `CatalogoAcademicoInternalService` | `DashboardAcademicoService` | Listar turmas por escola |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `PlanejamentoBimestralService` | Validar turma-disciplina da alocacao antes de criar ou atualizar planejamento |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `DiarioAulaService` | Validar turma-disciplina da alocacao antes de criar aula |
| `EstruturaTurmaPort` | `CatalogoAcademicoInternalService` | `AvaliacaoService` | Validar turma-disciplina da alocacao antes de criar avaliacao |

## Decisoes consolidadas

- Os contratos internos continuam dentro do monolito.
- Os contratos internos devem ser usados primeiro em pontos de baixo risco.
- O uso inicial deve priorizar leitura, agregacao e validacao defensiva.
- Nenhum contrato interno deve expor entidades JPA.
- O parametro `escolaId` deve permanecer explicito nos contratos internos.
- O comportamento HTTP publico deve ser preservado quando um fluxo passar a usar uma porta interna.

## Onde ainda nao mexer

Ainda nao e recomendado refatorar em massa:

- matriculas;
- documentos;
- historico/boletins;
- diario de aula;
- avaliacoes/notas;
- IA pedagogica;
- dashboards transacionais ou com filtros especificos ainda nao analisados.

Motivo:

- Esses fluxos tem acoplamentos transacionais e queries especificas.
- O ganho de trocar tudo agora nao compensa o risco.
- As portas internas ainda estao em amadurecimento.

## Quando sera necessario criar novos componentes

Ainda nao e necessario criar BFF, microservico ou componente runtime separado.

Sera necessario avisar e planejar criacao de novo componente quando pelo menos uma destas condicoes estiver presente:

- Um BFF tiver contrato de experiencia fechado e precisar compor mais de um dominio para reduzir chamadas do frontend.
- Um dominio tiver contrato interno estavel e for consumido por varios fluxos.
- Houver necessidade real de deploy independente.
- Houver necessidade real de escala separada.
- Houver isolamento operacional ou de dados que justifique o custo.
- Houver processamento assincrono concreto que justifique fila.
- Houver uma fase explicita de extracao aprovada.

## Proximos candidatos seguros de uso interno

### Candidato 1 - CatalogoAcademicoInternalService

Possivel uso:

- Usar `EscolaContextoPort` em `CatalogoAcademicoInternalService`, preservando `CatalogoAcademicoPort` e `EstruturaTurmaPort`.

Risco:

- Baixo. O uso direto de `EscolaTenantService` esta concentrado no fallback de escola padrao em um service interno de leitura e validacao.

Validacao esperada:

- `.\mvnw.cmd test`.

## Estado apos Fase 48I

A Fase 48I consolidou os usos pontuais de `EstruturaTurmaPort` em planejamento bimestral, diario de aula e avaliacoes. A porta continua dentro do monolito e ainda nao exige BFF, microservico ou novo componente runtime.

## Estado apos Fase 48M

A Fase 48M consolidou os usos de `EscolaContextoPort` nos dashboards academico, secretaria e diretor. O proximo candidato seguro passa a ser o dashboard professor, ainda sem justificar BFF, microservico ou novo componente runtime.

## Estado apos Fase 48N

A Fase 48N aplicou `EscolaContextoPort` em `DashboardProfessorService`, mantendo o contrato HTTP do dashboard professor e os filtros por professor e escola. Os snapshots de indicadores passam a ser o proximo candidato de consolidacao, ainda dentro do monolito.

## Estado apos Fase 48O

A Fase 48O aplicou `EscolaContextoPort` em `DashboardIndicadorSnapshotService`, mantendo listagem, historico, criacao, atualizacao e exclusao de snapshots sem alterar contratos HTTP. A area de dashboards fica pronta para uma consolidacao documental especifica antes de novos usos fora desse dominio.

## Estado apos Fase 48P

A Fase 48P consolidou a matriz de consumidores de `EscolaContextoPort` na area de dashboards e snapshots, confirmando que os services de dashboard nao dependem mais diretamente de `EscolaTenantService`. O proximo candidato seguro passa a ser um fluxo de leitura fora de dashboards: `MatriculaAcademicoResumoService`.

## Estado apos Fase 48Q

A Fase 48Q aplicou `EscolaContextoPort` em `MatriculaAcademicoResumoService`, preservando o contrato HTTP do resumo academico da matricula e os filtros por matricula e escola. A evolucao fora de dashboards deve continuar por consultas de leitura antes de fluxos transacionais.

## Estado apos Fase 48R

A Fase 48R consolidou o uso de `EscolaContextoPort` no resumo academico da matricula e mapeou os proximos candidatos de leitura em matricula. Os gateways auxiliares de consulta de aluno, periodo letivo e turma sao os proximos pontos seguros antes de tocar services transacionais de matricula.

## Estado apos Fase 48S

A Fase 48S aplicou `EscolaContextoPort` nos gateways auxiliares de consulta de matricula, preservando as interfaces de gateway e o comportamento externo. A proxima fase deve consolidar esses usos antes de avaliar novos candidatos fora de matricula.

## Estado apos Fase 48T

A Fase 48T consolidou os usos de `EscolaContextoPort` em matricula leitura e gateways auxiliares. `MatriculaFluxoService` e `MatriculaPersistenceGateway` permanecem fora do escopo por serem transacionais. O proximo passo seguro e diagnosticar os usos remanescentes de `EscolaTenantService` por dominio e risco.

## Estado apos Fase 48U

A Fase 48U diagnosticou os usos remanescentes de `EscolaTenantService` por dominio, risco e tipo de fluxo. O proximo candidato seguro escolhido foi `CatalogoAcademicoInternalService`, por ser um service interno de leitura e validacao com `escolaId` explicito nos contratos.

## Criterios de conclusao da Fase 48F

- Contratos internos existentes listados.
- Implementacoes atuais identificadas.
- Consumidores atuais identificados.
- Limites e lacunas documentados.
- Criterios para criar novos componentes explicitados.
- Proximo candidato seguro indicado.
