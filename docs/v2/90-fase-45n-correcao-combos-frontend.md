# Fase 45N - Correcao de combos bloqueados no frontend

## Objetivo

Corrigir combos que ficavam bloqueados ou sem comportamento correto no
microfrontend, principalmente em aluno, documentos, transferencia, responsaveis
e matricula.

## Escopo implementado

- Frontend do microfrontend academico.
- Substituicao dos `mat-select` restantes por `select matNativeControl` em:
  - cadastro e edicao de aluno: sexo e status;
  - painel de documentos: tipo do novo documento;
  - historico escolar: disciplina cadastrada;
  - transferencia de aluno: tipo e status da transferencia;
  - matricula: turma da nova matricula, filtros de turma/status e status da
    consulta operacional.
- Inclusao de label no seletor de status da consulta operacional de matriculas.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- A troca segue o padrao ja adotado nas telas academicas para evitar problemas
  de overlay do Angular Material no microfrontend federado.
- Os `mat-option` que restaram pertencem a `mat-autocomplete`, nao a
  `mat-select`.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- Validacao no navegador:
  - aluno novo: sexo e status renderizados como select nativo;
  - aluno editado: sexo, status e disciplina cadastrada renderizados como select
    nativo;
  - aba transferencia: tipo e status da transferencia renderizados como select
    nativo;
  - aba documentos: tipo do novo documento renderizado e selecionavel como
    select nativo;
  - responsavel editado: tipo do novo documento renderizado e selecionavel como
    select nativo;
  - matricula: turma, filtro de turma e filtro de status renderizados como
    select nativo, com selecao testada.

## Proxima fase sugerida

Continuar a revisao funcional da Fase 45N usando a massa de testes, seguindo
para o fluxo de cadastro/edicao/exclusao de aluno e responsavel apos confirmar
que os combos corrigidos estao adequados visualmente.
