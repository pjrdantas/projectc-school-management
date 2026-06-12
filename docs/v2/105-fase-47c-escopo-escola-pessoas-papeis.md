# Fase 47C - Escopo por escola em pessoas e papeis

## Objetivo

Aplicar a segunda subfase de escopo por escola no nucleo de pessoas e papeis,
sem alterar fluxos de UI ou iniciar isolamento completo de todos os dominios.

## Escopo implementado

- Adicionado vinculo obrigatorio de `pessoa` com `escola`.
- Mantidos `aluno`, `responsavel`, `professor` e `funcionario` herdando escola
  por relacionamento com `pessoa`.
- Adicionado `escolaId` opcional nos contratos de criacao/atualizacao de aluno
  e responsavel.
- Adicionados `escolaId` e `escolaNome` nas respostas de aluno, responsavel,
  professor e funcionario elegivel para professor.
- Quando `escolaId` nao e informado, o backend usa a escola padrao criada na
  Fase 47B.
- Listagens, buscas, validacoes de CPF e exclusoes principais de aluno e
  responsavel passam a operar dentro da escola padrao nesta subfase.
- Listagem, busca, criacao e alocacao de professores passam a validar escola
  pela pessoa do funcionario/professor e pela turma da alocacao.
- Login e refresh passam a resolver `professorId` dentro da escola ativa da
  sessao.
- Criada migration `V5__escopo_escola_pessoas_papeis.sql`.

## Decisoes tecnicas

- A subfase ainda nao implementa troca dinamica de escola pelo usuario.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.
- `aluno`, `responsavel`, `professor` e `funcionario` nao receberam
  `id_escola` direto para evitar duplicidade de estado com `pessoa`.
- A unicidade de CPF passa a ser por escola.

## Fora do escopo

- Escopo por escola em matriculas, documentos, historico, avaliacoes,
  dashboards e planejamento.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=AlunoControllerIntegrationTest,ResponsavelControllerIntegrationTest,AlunoResponsavelVinculoControllerIntegrationTest,ProfessorControllerIntegrationTest,AuthControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47C - proxima subfase de escopo por escola em matriculas e documentos:

- matriculas;
- vinculos aluno-responsavel;
- documentos ligados a pessoas/alunos;
- pontos de consulta que ainda usam aluno ou responsavel sem filtro por escola.
