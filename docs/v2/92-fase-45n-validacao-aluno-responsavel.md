# Fase 45N - Validacao de aluno e responsavel

## Objetivo

Validar os fluxos principais de cadastro, edicao e exclusao de aluno e
responsavel usando a massa de testes local.

## Escopo implementado

- Frontend do microfrontend academico.
- Backend do servico escolar.
- Validado no navegador:
  - cadastro de aluno;
  - edicao de aluno;
  - exclusao de aluno com dialogo de confirmacao;
  - cadastro de responsavel;
  - edicao de responsavel;
  - abertura do dialogo de exclusao de responsavel.
- Corrigido o fluxo de exclusao de responsavel:
  - frontend passou a usar `MessageDialogComponent`, com `disableClose` e texto
    explicito de confirmacao;
  - backend passou a remover a pessoa, documentos, vinculos de tipo de pessoa e
    endereco associados ao responsavel quando ele nao possui vinculo com aluno;
  - mantido bloqueio de exclusao quando o responsavel ainda possui aluno
    vinculado.
- Corrigido o fluxo de exclusao de aluno:
  - backend passou a remover a pessoa, documentos, vinculos de tipo de pessoa e
    endereco associados ao aluno excluido;
  - responsaveis exclusivos removidos junto com o aluno tambem limpam seus dados
    de pessoa;
  - responsaveis compartilhados continuam preservados.

## Decisoes

- Sem alteracao Maven, Flyway ou schema.
- As exclusoes limpam apenas os dados associados ao aluno ou responsavel
  excluido, sem limpeza global de enderecos.
- A validacao usou registros temporarios criados pela interface; o registro
  temporario que ficou em `pessoa` antes das correcoes foi removido manualmente
  do banco local.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- `.\mvnw.cmd test` em `school-management-service`.
- Resultado backend: 91 testes, 0 falhas, 0 erros.
- Testes backend cobrem liberacao de CPF da pessoa apos excluir aluno e
  responsavel sem vinculo.
- Validacao no navegador:
  - aluno temporario criado, editado e removido;
  - dialogo de exclusao de aluno exibido com confirmacao;
  - responsavel temporario criado e editado;
  - dialogo de exclusao de responsavel exibido com os botoes `Cancelar` e
    `Excluir responsavel`;
  - exclusao de dados reais cancelada na validacao final.

## Proxima fase sugerida

Continuar a Fase 45N validando matriculas com a massa de testes: nova matricula,
filtros, troca de status e exclusao de matricula.
