# Fase 47C - Escopo por escola em catálogos acadêmicos

## Objetivo

Aplicar a primeira subfase de escopo por escola nos catálogos acadêmicos, sem
isolar todos os domínios do sistema de uma vez.

## Escopo implementado

- Adicionado vínculo com escola em:
  - `periodo_letivo`;
  - `serie`;
  - `disciplina`;
  - `turma`.
- Mantido `turma_disciplina` herdando escola por relacionamento com `turma`.
- Adicionado `escolaId` opcional nos contratos de criação/atualização dos
  catálogos afetados.
- Adicionados `escolaId` e `escolaNome` nas respostas de período letivo, série,
  disciplina e turma.
- Quando `escolaId` não é informado, o backend usa a escola padrão criada na
  Fase 47B.
- Listagens e buscas principais desses catálogos passam a operar na escola
  padrão nesta subfase.
- Validação de turma passa a buscar período letivo e série dentro da mesma
  escola.
- Vínculo turma-disciplina passa a validar disciplina dentro da escola da turma.
- Criada migration `V4__escopo_escola_catalogos_academicos.sql`.
- Ajustados seeds e limpezas de testes para preservar a escola padrão.

## Decisões técnicas

- A subfase ainda não implementa troca dinâmica de escola pelo usuário.
- O escopo padrão é a escola `00000000-0000-0000-0000-000000000047`.
- `turno` e `nivel_ensino` continuam como catálogos globais.
- `turma_disciplina` não recebeu `id_escola` direto porque a escola pode ser
  inferida por `turma`.

## Fora do escopo

- Escopo por escola em pessoas, alunos, responsáveis, professores e matrículas.
- Filtros multi-escola em dashboards, planejamento, IA e documentos.
- Usuário com múltiplas escolas.
- Troca de escola ativa pelo frontend.
- BFF, separação de serviços, Kafka, MongoDB ou Redis.

## Validação executada

Backend:

- `.\mvnw.cmd -Dtest=AcademicCatalogControllerIntegrationTest test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Não houve alteração de frontend nesta fase.

## Próxima fase recomendada

Fase 47C - próxima subfase de escopo por escola em pessoas e papéis:

- `pessoa`;
- `aluno`;
- `responsavel`;
- `professor`;
- `funcionario`, se aplicável no modelo atual.

Essa subfase deve manter a mesma regra: usar escola padrão quando não houver
contexto explícito e evitar filtros globais antes de validar domínio por domínio.
