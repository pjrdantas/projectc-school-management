# Fase 45N - Validacao de historico escolar do aluno

## Objetivo

Validar o fluxo de historico escolar dentro da edicao de aluno, cobrindo
componentes curriculares, disciplina cadastrada, cadastro, listagem, exclusao e
ausencia de identificadores tecnicos na interface.

## Escopo implementado

- Backend:
  - `HistoricoEscolarRequest` passou a receber `alunoId`.
  - `HistoricoEscolarResponse` passou a retornar `alunoId` para contrato interno.
  - Historico escolar passou a validar existencia do aluno antes de salvar.
  - Adicionado `GET /api/historicos-escolares/alunos/{alunoId}`.
  - Listagem por aluno usa vinculo real por `id_aluno`, nao filtro por nome.
- Frontend do microfrontend academico:
  - Cadastro de historico passa a enviar o aluno atual.
  - Aba Historico Escolar passa a listar historicos pelo endpoint do aluno.
  - Adicionada exclusao de historico escolar com modal de confirmacao bloqueado.
  - Mantida exibicao sem UUID para o usuario final.
- Testes:
  - Teste de integracao de historico escolar passou a criar aluno controlado.
  - Teste cobre criacao, busca, listagem por aluno, listagem paginada,
    atualizacao, exclusao e validacoes de componentes.

## Decisoes

- Sem alteracao Maven, Flyway ou schema.
- O `alunoId` fica no contrato tecnico entre frontend e backend, mas nao e
  exibido na tela.
- A listagem por aluno evita associacao incorreta por nome do aluno.
- A exclusao foi mantida com confirmacao explicita por ser acao destrutiva.

## Validacao

- `.\mvnw.cmd -Dtest=HistoricoEscolarControllerIntegrationTest test` em
  `school-management-service`.
- `.\mvnw.cmd test` em `school-management-service`.
- `npm run build` em `school-management-web/microfrontend`.
- Validacao no navegador em
  `http://localhost:4200/students/92b00000-0000-0000-0000-000000000001/edit`:
  - aba Historico Escolar carregada para `Aluno Teste 01`;
  - combo de disciplina cadastrada preenchido;
  - componente curricular `Matematica I` adicionado;
  - historico temporario `Ensino Fundamental Validacao UI` salvo e exibido;
  - listagem exibida sem UUID visivel;
  - historico temporario excluido pela UI;
  - API confirmou 0 registros temporarios `Validacao UI historico 45N`
    restantes.

## Proxima fase sugerida

Considerar a etapa de validacoes funcionais basicas concluida. A proxima fase
recomendada e revisao final de PR/MVP: conferir `git status`, revisar diff,
executar uma navegacao exploratoria curta nos fluxos principais e abrir PR de
`develop` para `master` quando estiver pronto.
