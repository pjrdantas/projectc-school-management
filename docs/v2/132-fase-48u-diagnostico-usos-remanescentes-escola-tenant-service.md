# Fase 48U - Diagnostico dos usos remanescentes de EscolaTenantService

## Objetivo

Mapear os usos remanescentes de `EscolaTenantService` por dominio, risco e tipo de fluxo, escolhendo um unico candidato seguro para a proxima aplicacao pontual de `EscolaContextoPort`.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend ou nova rota HTTP.

## Usos remanescentes mapeados

| Dominio | Classe | Tipo de fluxo | Risco para troca agora | Decisao |
| --- | --- | --- | --- | --- |
| Catalogo | `CatalogoAcademicoInternalService` | Leitura e validacao interna por escola | Baixo | Proximo candidato |
| Catalogo | `DisciplinaService` | Use case de catalogo | Medio | Manter para fase posterior |
| Catalogo | `PeriodoLetivoPersistenceGateway` | Persistencia de periodo letivo | Medio | Nao tocar nesta fase |
| Catalogo | `SeriePersistenceGateway` | Persistencia de serie | Medio | Nao tocar nesta fase |
| Catalogo | `TurmaPersistenceGateway` | Persistencia de turma | Medio | Nao tocar nesta fase |
| Catalogo | `TurmaDisciplinaService` | Validacao com constante de escola padrao | Medio | Exige analise propria |
| Aluno | `AlunoPersistenceGateway` | Persistencia de aluno | Medio | Nao tocar nesta fase |
| Responsavel | `ResponsavelPersistenceGateway` | Persistencia de responsavel | Medio | Nao tocar nesta fase |
| Responsavel | `AlunoResponsavelVinculoPersistenceGateway` | Persistencia de vinculo | Medio | Nao tocar nesta fase |
| Matricula | `MatriculaFluxoService` | Fluxo transacional | Alto | Manter fora do escopo |
| Matricula | `MatriculaPersistenceGateway` | Persistencia de matricula | Alto | Manter fora do escopo |
| Planejamento | `PlanejamentoBimestralService` | Escrita e validacao de planejamento | Medio | Manter para fase posterior |
| Avaliacao | `AvaliacaoService` | Escrita e validacao de avaliacao | Medio | Manter para fase posterior |
| Professor | `DiarioAulaService` | Escrita e validacao de aula | Medio | Manter para fase posterior |
| Professor | `ProfessorService` | Use case de professor | Medio | Manter para fase posterior |
| Documento | `DocumentoPersistenceGateway` | Persistencia de documento | Medio | Manter para fase posterior |
| Historico | `HistoricoEscolarServiceImpl` | Consulta e geracao historica | Medio | Exige analise propria |
| Historico | `BoletimService` | Consulta academica composta | Medio | Exige analise propria |
| IA | `PlanejamentoIAService` | Composicao com IA pedagogica | Medio | Exige fase propria |
| Pessoa | `PessoaFoundationService` | Fundacao compartilhada de pessoa | Medio | Exige analise propria |
| Seguranca | `AuthService` | Autenticacao | Alto | Nao tocar agora |
| Seguranca | `UsuarioInteractor` | Usuarios e perfis | Alto | Nao tocar agora |

## Classificacao

### Baixo risco

- Fluxos internos de leitura e validacao defensiva.
- Fluxos que ja recebem `escolaId` como parametro explicito.
- Fluxos onde a troca nao altera controller, DTO, response, request ou contrato HTTP.

### Medio risco

- Use cases com criacao, atualizacao ou persistencia.
- Gateways que resolvem entidade `EscolaEntity` para salvar dados.
- Fluxos compartilhados por mais de um dominio.

### Alto risco

- Autenticacao, usuarios e perfis.
- Fluxos transacionais de matricula.
- Pontos que podem alterar autorizacao, identidade ativa ou consistencia de escrita.

## Candidato escolhido para a proxima fase

`CatalogoAcademicoInternalService`.

Motivos:

- Ja e uma implementacao interna de portas de catalogo.
- Ja trabalha com `escolaId` explicito nos contratos internos.
- O uso direto de `EscolaTenantService` esta concentrado em `resolverEscolaId(UUID escolaId)`.
- A troca pode ficar limitada ao ponto de resolucao de escola padrao.
- Nao exige alterar controller, DTO, migration, rota HTTP, frontend ou contrato externo.

## Onde nao mexer na proxima fase

- Gateways de persistencia de catalogo.
- Services transacionais de matricula.
- Seguranca, autenticacao, usuarios e perfis.
- IA pedagogica.
- Documentos, historico e fundacao de pessoa.

## Decisoes

- A evolucao continua incremental dentro do monolito.
- `EscolaTenantService` continua sendo a implementacao atual de `EscolaContextoPort`.
- A proxima fase deve aplicar `EscolaContextoPort` somente em `CatalogoAcademicoInternalService`.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos remanescentes de `EscolaTenantService` foram classificados por dominio e risco. O proximo candidato seguro e `CatalogoAcademicoInternalService`, sem ampliar escopo para persistencia, seguranca ou fluxos transacionais.

## Estado apos Fase 48V

A Fase 48V aplicou `EscolaContextoPort` em `CatalogoAcademicoInternalService`, preservando os contratos internos de catalogo e mantendo fora do escopo os gateways de persistencia, seguranca e fluxos transacionais.

## Proxima fase sugerida

Fase 48V - uso pontual de `EscolaContextoPort` em `CatalogoAcademicoInternalService`.

Objetivo sugerido:

- Trocar a dependencia direta de `EscolaTenantService` por `EscolaContextoPort` apenas em `CatalogoAcademicoInternalService`.
- Preservar `CatalogoAcademicoPort` e `EstruturaTurmaPort`.
- Preservar comportamento de `resolverEscolaId(UUID escolaId)`.
- Validar backend completo com `.\mvnw.cmd test`.

## Proxima fase sugerida apos 48V

Fase 48W - consolidacao de `EscolaContextoPort` nos contratos internos de catalogo.
