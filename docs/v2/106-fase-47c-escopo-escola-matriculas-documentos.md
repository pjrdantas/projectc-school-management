# Fase 47C - Escopo por escola em matriculas e documentos

## Objetivo

Aplicar a terceira subfase de escopo por escola em matriculas, documentos e
vinculos aluno-responsavel, sem alterar fluxo de UI ou separar servicos.

## Escopo implementado

- Matriculas passam a herdar escola por `turma`, `periodo_letivo` e `aluno`.
- Consultas, listagens, atualizacao de status, exclusao, rematricula e resumo
  academico de matricula passam a operar na escola padrao.
- Validacoes de criacao de matricula passam a conferir aluno, turma e periodo
  letivo dentro da escola padrao.
- Validacao de duplicidade de matricula por aluno e periodo letivo passa a ser
  feita dentro da escola.
- Contagem de vagas por turma passa a considerar a escola da turma.
- Documentos passam a ser escopados pela escola da pessoa vinculada via
  `pessoa_documento`.
- Cadastro, listagem, busca e exclusao de documentos passam a validar aluno ou
  responsavel dentro da escola padrao.
- Vinculos aluno-responsavel passam a validar duplicidade, listagem e exclusao
  dentro da escola padrao.
- Adicionados `escolaId` e `escolaNome` nas respostas de matricula e documento.

## Decisoes tecnicas

- `matricula` e `documento` nao receberam `id_escola` direto nesta subfase.
- A escola da matricula e inferida por `turma` e pelo `aluno`.
- A escola do documento e inferida pela pessoa vinculada em `pessoa_documento`.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.

## Fora do escopo

- Escopo por escola em aulas, frequencias e avaliacoes.
- Escopo por escola em historico, dashboards, planejamento e IA.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=MatriculaControllerIntegrationTest,MatriculaAcademicoControllerIntegrationTest,DocumentoAlunoControllerIntegrationTest,AlunoResponsavelVinculoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47C - proxima subfase de escopo por escola em aulas, frequencias e
avaliacoes:

- aulas;
- frequencias;
- avaliacoes;
- notas;
- consultas que partem de professor, turma, aluno ou matricula nesses dominios.
