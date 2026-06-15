# Fase 49G - Diagnostico atualizado dos usos remanescentes de EscolaTenantService

## Objetivo

Reclassificar os usos remanescentes de `EscolaTenantService` apos as aplicacoes pontuais de `EscolaContextoPort` em catalogo interno, dashboards, matricula leitura, planejamento bimestral, diario de aula e avaliacoes.

Esta fase e documental e de verificacao. Ela nao cria BFF, microservico, fila, banco adicional, novo componente frontend, nova rota HTTP ou migration.

## Usos remanescentes mapeados

| Dominio | Classe | Tipo de fluxo | Risco para troca agora | Decisao |
| --- | --- | --- | --- | --- |
| Catalogo | `DisciplinaService` | Use case de catalogo com criacao, atualizacao, exclusao e leitura | Medio | Proximo candidato para diagnostico pontual |
| Catalogo | `PeriodoLetivoPersistenceGateway` | Gateway de persistencia de periodo letivo | Medio | Manter para fase posterior |
| Catalogo | `SeriePersistenceGateway` | Gateway de persistencia de serie | Medio | Manter para fase posterior |
| Catalogo | `TurmaPersistenceGateway` | Gateway de persistencia de turma | Medio | Manter para fase posterior |
| Catalogo | `TurmaDisciplinaService` | Validacao com constante de escola padrao | Medio | Exige analise propria |
| Aluno | `AlunoPersistenceGateway` | Persistencia de aluno | Medio a alto | Manter para fase posterior |
| Responsavel | `ResponsavelPersistenceGateway` | Persistencia de responsavel | Medio a alto | Manter para fase posterior |
| Responsavel | `AlunoResponsavelVinculoPersistenceGateway` | Persistencia de vinculo aluno-responsavel | Medio a alto | Manter para fase posterior |
| Matricula | `MatriculaFluxoService` | Fluxo transacional de matricula | Alto | Manter fora do escopo |
| Matricula | `MatriculaPersistenceGateway` | Persistencia de matricula | Alto | Manter fora do escopo |
| Documento | `DocumentoPersistenceGateway` | Persistencia e vinculos documentais | Medio a alto | Exige analise propria |
| Historico | `HistoricoEscolarServiceImpl` | Consulta e geracao historica | Medio | Exige analise propria |
| Historico | `BoletimService` | Consulta academica composta | Medio | Exige analise propria |
| IA | `PlanejamentoIAService` | Composicao com IA pedagogica | Medio | Exige fase propria |
| Pessoa | `PessoaFoundationService` | Fundacao compartilhada de pessoa | Medio a alto | Exige analise propria |
| Professor | `ProfessorService` | Use case de professor e alocacoes | Medio | Manter para fase posterior |
| Seguranca | `AuthService` | Autenticacao | Alto | Nao tocar agora |
| Seguranca | `UsuarioInteractor` | Usuarios e perfis | Alto | Nao tocar agora |

## Classificacao atualizada

### Baixo risco

- Nenhum uso remanescente foi classificado como baixo risco direto para troca imediata sem diagnostico proprio.
- Os fluxos simples de leitura ja foram tratados nas fases anteriores.

### Medio risco

- Use cases de catalogo e professor.
- Gateways de persistencia de catalogo.
- Consultas historicas e composicoes academicas.
- Composicao pedagogica com IA.

### Medio a alto risco

- Gateways que persistem aluno, responsavel, vinculos e documentos.
- Fundacao compartilhada de pessoa.

### Alto risco

- Autenticacao, usuarios e perfis.
- Fluxos transacionais de matricula.
- Pontos que podem alterar identidade ativa, autorizacao ou consistencia de escrita.

## Candidato escolhido para a proxima fase

`DisciplinaService`.

Motivos:

- O fluxo esta limitado ao catalogo de disciplinas.
- O uso direto de `EscolaTenantService` esta concentrado em resolucao de escola padrao e no metodo privado `resolverEscola(UUID escolaId)`.
- O service ja trabalha com `escolaId` explicito quando o request informa escola.
- A troca futura pode preservar controllers, DTOs, requests, responses, repositories e migrations.
- O risco e menor que gateways de aluno, responsavel, documento, matricula e seguranca.

## Onde nao mexer na proxima fase

- Gateways de persistencia de aluno, responsavel, matricula e documento.
- `MatriculaFluxoService` e `MatriculaPersistenceGateway`.
- `AuthService` e `UsuarioInteractor`.
- Historico, boletim e IA pedagogica.
- `PessoaFoundationService`.
- BFF, microservicos, filas, bancos adicionais e novos componentes frontend.

## Decisoes

- A proxima fase deve diagnosticar `DisciplinaService` antes de qualquer troca.
- Nao aplicar `EscolaContextoPort` em massa nos usos remanescentes.
- Nao alterar fluxos transacionais ou de seguranca nesta etapa.
- O monolito continua sendo o componente runtime unico.
- Ainda nao ha justificativa para criar BFF, microservico, Kafka, MongoDB, Redis ou novo componente frontend.

## Validacao esperada

- `.\mvnw.cmd test`

## Resultado

Os usos remanescentes de `EscolaTenantService` foram reclassificados apos o ciclo de `EscolaContextoPort` em planejamento, diario de aula e avaliacoes. O proximo passo seguro e diagnosticar `DisciplinaService` de forma pontual antes de qualquer alteracao de codigo.

## Proxima fase sugerida

Fase 49H - diagnostico pontual de `DisciplinaService` antes de aplicar `EscolaContextoPort`.

Objetivo sugerido:

- Mapear os usos de escola em criacao, atualizacao, exclusao, busca e listagem de disciplinas.
- Confirmar como preservar `resolverEscola(UUID escolaId)`.
- Confirmar se a troca pode ficar limitada ao ponto de resolucao de escola padrao.
- Validar backend completo com `.\mvnw.cmd test`.
