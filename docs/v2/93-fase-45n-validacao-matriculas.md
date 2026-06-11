# Fase 45N - Validacao de matriculas

## Objetivo

Validar o fluxo operacional de matriculas no microfrontend, usando a massa de
testes local e corrigindo somente problemas encontrados durante a validacao.

## Escopo implementado

- Frontend do microfrontend academico.
- Validado no navegador:
  - consulta geral de matriculas;
  - combo de turma na nova matricula;
  - criacao de matricula com dialogo de confirmacao;
  - filtro por status;
  - alteracao de status da matricula;
  - exclusao de matricula com dialogo de confirmacao.
- Corrigido o combo de status de matricula:
  - removida lista fixa do frontend;
  - status agora sao carregados de `/api/matriculas/catalogos/status`;
  - status inexistente `TRANSFERIDO` deixou de aparecer;
  - status existente `CONCLUIDA` passou a aparecer;
  - o select de status da linha passou a marcar visualmente o status real da
    matricula.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Mantido `select matNativeControl` nos combos da tela de matricula.
- A massa temporaria criada na validacao foi removida pela propria interface.
- Os logs temporarios usados para reiniciar o host foram gerados fora do
  repositorio ao final da fase.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- Validacao no navegador em `http://localhost:4200/enrollment`:
  - login como `admin`;
  - nova matricula criada para `Aluno Teste 01` na `Turma A`;
  - status alterado para `Matrícula efetivada`;
  - filtro por status `Matrícula efetivada` retornou a matricula correta;
  - exclusao da matricula temporaria confirmada pela UI;
  - banco conferido: a matricula temporaria nao permaneceu.

## Proxima fase sugerida

Continuar a Fase 45N validando documentos e transferencias do aluno com a massa
de testes: cadastro, edicao, combos, filtros, exclusao e mensagens de erro.
