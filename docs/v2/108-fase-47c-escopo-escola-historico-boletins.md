# Fase 47C - Escopo por escola em historico e boletins

## Objetivo

Aplicar a quinta subfase de escopo por escola em historico escolar, boletins e
consultas academicas consolidadas, sem alterar fluxo de UI ou separar servicos.

## Escopo implementado

- Boletins passam a validar a matricula dentro da escola padrao antes de
  calcular, fechar ou listar fechamentos.
- Calculo de boletim passa a consumir notas e frequencias filtradas pela escola
  da matricula.
- Fechamento de boletim passa a consultar duplicidade por matricula, periodo e
  escola.
- Geracao de historico por boletim passa a aceitar somente boletins fechados da
  escola padrao.
- Historicos escolares passam a ser criados, atualizados, buscados, listados e
  excluidos apenas para alunos da escola padrao.
- Verificacao de duplicidade de historico gerado por boletim passa a considerar
  aluno, periodo letivo e escola.
- Conclusao academica de matricula passa a validar o boletim fechado dentro da
  escola da matricula.
- Adicionados `escolaId` e `escolaNome` nas respostas de boletim e historico
  escolar.

## Decisoes tecnicas

- `boletim` e `historico_escolar` nao receberam `id_escola` direto nesta
  subfase.
- A escola do boletim e inferida pela matricula e pela turma vinculada.
- A escola do historico escolar e inferida pelo aluno vinculado, que permanece
  obrigatorio no contrato atual.
- O escopo padrao continua sendo a escola
  `00000000-0000-0000-0000-000000000047`.

## Fora do escopo

- Escopo por escola em planejamento e IA.
- Escopo por escola em dashboards.
- Usuario com multiplas escolas.
- Troca de escola ativa pelo frontend.
- Migration para adicionar `id_escola` direto em boletins ou historicos.
- BFF, separacao de servicos, Kafka, MongoDB ou Redis.

## Validacao executada

Backend:

- `.\mvnw.cmd "-Dtest=BoletimControllerIntegrationTest,HistoricoEscolarControllerIntegrationTest,MatriculaAcademicoControllerIntegrationTest" test`
- `.\mvnw.cmd test`

Resultado:

- 95 testes executados;
- 0 falhas;
- 0 erros.

Frontend:

- Nao houve alteracao de frontend nesta fase.

## Proxima fase recomendada

Fase 47C - proxima subfase de escopo por escola em planejamento e IA:

- planejamentos bimestrais;
- interacoes e conteudos gerados por IA;
- biblioteca pedagogica.
