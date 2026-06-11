# Fase 45N - Validacao de documentos e transferencias do aluno

## Objetivo

Validar os fluxos de documentos e transferencias dentro da edicao de aluno,
usando a massa de testes local e corrigindo somente problemas encontrados na
interface.

## Escopo implementado

- Frontend do microfrontend academico.
- Validado no navegador:
  - edicao do aluno `Aluno Teste 01`;
  - selects nativos de sexo e status do aluno;
  - aba Transferencia;
  - selects nativos de tipo e status de transferencia;
  - cadastro de transferencia temporaria;
  - aba Documentos;
  - combo de tipo de documento;
  - mensagem de obrigatoriedade de numero e arquivo;
  - listagem de documento temporario;
  - exclusao de documento temporario pela interface.
- Corrigida exibicao de arquivo em Documentos:
  - a tela deixou de mostrar caminho tecnico completo;
  - a tela passa a exibir apenas o nome do arquivo;
  - prefixo UUID de arquivos enviados e removido da exibicao para o usuario.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Mantidos `select matNativeControl` nos combos validados.
- O cadastro de documento temporario foi feito pela API local porque o navegador
  interno nao fornece selecao de arquivo local pela UI.
- A exclusao do documento temporario foi validada pela propria interface.
- A API atual de transferencia nao expoe exclusao; a transferencia temporaria
  criada para validacao foi removida manualmente do banco local.
- A limpeza manual ficou restrita a registros com texto `Validacao UI` e ao
  arquivo temporario de upload gerado nesta fase.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- Validacao no navegador em `http://localhost:4200/students/92b00000-0000-0000-0000-000000000001/edit`:
  - login como `admin`;
  - transferencia temporaria cadastrada e exibida;
  - documento temporario criado via API, listado na aba Documentos e excluido
    pela UI;
  - exibicao de documentos confirmada sem caminho tecnico e sem UUID.
- Banco conferido:
  - nenhuma transferencia temporaria `Validacao UI` restante;
  - nenhum documento temporario `DOC-VALIDACAO-UI-45N` restante;
  - nenhum arquivo `projectc-doc-validacao-ui-45n` restante em `uploads`.

## Proxima fase sugerida

Continuar a Fase 45N validando historico escolar do aluno: componentes
curriculares, disciplina cadastrada, cadastro de historico, listagem, exclusao e
ausencia de identificadores tecnicos na interface.
