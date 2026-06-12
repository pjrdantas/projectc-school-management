# Fase 47C - Escopo por escola em aulas, frequencias e avaliacoes

## Objetivo

Aplicar a quarta subfase de escopo por escola em aulas, frequencias,
avaliacoes e notas, sem alterar fluxo de UI ou separar servicos.

## Escopo implementado

- Aulas passam a ser consultadas pela escola da turma vinculada a alocacao do
  professor.
- Criacao de aula passa a validar a alocacao professor-turma-disciplina dentro
  da escola padrao.
- Busca e listagem de aulas passam a operar dentro da escola padrao.
- Registro e listagem de frequencia de professor passam a validar a escola da
  aula.
- Registro e listagem de frequencia de aluno passam a validar a escola da aula
  e da matricula.
- Avaliacoes passam a ser consultadas pela escola da turma vinculada a alocacao
  do professor.
- Criacao de avaliacao passa a validar a alocacao professor-turma-disciplina
  dentro da escola padrao.
- Lancamento e listagem de notas passam a validar a escola da avaliacao e da
  matricula.
- Resumo academico de matricula passa a consumir frequencias e notas escopadas
  pela escola da matricula.
- Adicionados `escolaId` e `escolaNome` nas respostas de aula, frequencia,
  avaliacao e nota.

## Decisoes tecnicas

- `aula`, `frequencia_professor`, `frequencia_aluno`, `avaliacao` e
  `nota_aluno` nao receberam `id_escola` direto nesta subfase.
- A escola e inferida pela turma da alocacao professor-turma-disciplina ou pela
  matricula.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.

## Fora do escopo

- Escopo por escola em historico e boletins.
- Escopo por escola em planejamento e IA.
- Escopo por escola em dashboards.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=AulaControllerIntegrationTest,AvaliacaoControllerIntegrationTest,MatriculaAcademicoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47C - proxima subfase de escopo por escola em historico e boletins:

- historico escolar;
- boletins;
- consultas academicas que consolidam dados de matricula, avaliacao e
  frequencia.
