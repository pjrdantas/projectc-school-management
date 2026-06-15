# Fase 48D - Uso pontual dos contratos internos no dashboard academico

## Objetivo

Usar os contratos internos criados na Fase 48C em um fluxo de baixo risco, mantendo tudo dentro do monolito e sem criar BFF real, microservico ou novo componente runtime.

## Escopo implementado

- `DashboardAcademicoService` passou a resolver contexto escolar por `EscolaContextoPort`.
- `DashboardAcademicoService` passou a listar turmas por `CatalogoAcademicoPort`.
- `TurmaResumo` foi ampliado com `escolaId` e `escolaNome` para apoiar consumidores internos que precisam manter escopo multi-escola.
- `CatalogoAcademicoInternalService` passou a preencher os novos campos de escola em `TurmaResumo`.

## Decisoes tecnicas

- O contrato HTTP do dashboard academico nao foi alterado.
- Nenhum controller foi alterado.
- Nenhuma migration foi criada.
- Nenhum BFF, microservico, Kafka, MongoDB ou Redis foi introduzido.
- O uso dos contratos internos ficou restrito a leitura/agregacao de dashboard, reduzindo risco de regressao transacional.

## Fora do escopo

- Criar projeto BFF.
- Criar servicos independentes.
- Refatorar todos os dashboards.
- Refatorar planejamento, aulas ou avaliacoes.
- Alterar frontend.
- Implementar troca dinamica de escola.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=DashboardAcademicoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Frontend:

- Nao houve alteracao de frontend.

## Aviso sobre novos componentes

Ainda nao e necessario criar outro componente, BFF real ou servico separado.

O momento de criar novos componentes deve vir somente quando:

- houver contrato interno estavel consumido por mais de um fluxo;
- houver necessidade real de deploy, escala ou isolamento;
- os BFFs ja tiverem contratos de experiencia fechados;
- houver uma fase explicita para extracao.

## Proxima fase sugerida

Fase 48E - segundo uso pontual dos contratos internos em planejamento ou diario pedagogico.

Objetivo sugerido:

- Usar `EstruturaTurmaPort` em uma validacao pequena de planejamento, aula ou avaliacao.
- Manter a alteracao dentro do monolito.
- Nao criar BFF real.
- Validar backend com `.\mvnw.cmd test`.
