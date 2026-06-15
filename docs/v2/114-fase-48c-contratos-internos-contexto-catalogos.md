# Fase 48C - Contratos internos de contexto escolar e catalogos academicos

## Objetivo

Criar contratos internos pequenos para contexto escolar e catalogos academicos dentro do monolito, preparando consumo futuro por BFFs e servicos sem extrair codigo para outro runtime.

## Escopo implementado

- Criado `EscolaContexto` como DTO interno de contexto escolar.
- Criada porta `EscolaContextoPort`.
- `EscolaTenantService` passa a implementar a porta de contexto escolar.
- Criados DTOs internos de catalogo:
  - `PeriodoLetivoResumo`;
  - `SerieResumo`;
  - `TurnoResumo`;
  - `DisciplinaResumo`;
  - `TurmaResumo`;
  - `TurmaDisciplinaResumo`.
- Criada porta `CatalogoAcademicoPort` para leitura e validacao de catalogos por escola.
- Criada porta `EstruturaTurmaPort` para leitura e validacao de turma e disciplinas vinculadas.
- Criado `CatalogoAcademicoInternalService` como implementacao interna das duas portas de catalogo.

## Decisoes tecnicas

- A implementacao continua no `school-management-service`.
- Nenhum controller existente foi alterado.
- Nenhuma rota HTTP nova foi criada.
- Nenhuma migration foi criada.
- As portas trabalham com DTOs internos e nao expoem entidades JPA.
- Quando `escolaId` nao e informado, os contratos usam a escola padrao resolvida pelo `EscolaTenantService`, preservando o comportamento atual de escola unica.
- Os contratos de catalogo sao inicialmente focados em leitura e validacao, sem substituir os use cases existentes de criacao e edicao.

## Fora do escopo

- BFF real.
- Microservicos.
- Kafka, MongoDB ou Redis.
- Troca dinamica de escola ativa.
- Usuario com multiplas escolas.
- Refatoracao dos fluxos atuais para consumir as novas portas.
- Alteracao de frontend.

## Validacao executada

Backend:

- `.\mvnw.cmd test`

Resultado:

- Build success.
- 95 testes executados.
- 0 falhas.
- 0 erros.

Frontend:

- Nao houve alteracao de frontend.

## Proxima fase sugerida

Fase 48D - uso pontual dos contratos internos em um fluxo de baixo risco.

Objetivo sugerido:

- Escolher um ponto de baixo risco, preferencialmente dashboard ou planejamento, para consumir `CatalogoAcademicoPort` ou `EstruturaTurmaPort`.
- Manter o uso dentro do monolito.
- Nao criar BFF real.
- Validar backend com `.\mvnw.cmd test`.
