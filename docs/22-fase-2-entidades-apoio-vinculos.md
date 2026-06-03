# 22 - Fase 2: entidades de apoio e vinculos simples

## Objetivo

Criar as entidades e repositories faltantes da fase 2 sem alterar endpoints, services ou regras de negocio existentes.

## Entidades criadas

### aluno

- `TipoEventoAlunoEntity` -> `tipo_evento_aluno`
- `AlunoHistoricoEventoEntity` -> `aluno_historico_evento`
- `SolicitacaoExclusaoAlunoEntity` -> `solicitacao_exclusao_aluno`

Repositories:

- `TipoEventoAlunoJpaRepository`
- `AlunoHistoricoEventoJpaRepository`
- `SolicitacaoExclusaoAlunoJpaRepository`

### seguranca

- `UsuarioPerfilEntity` -> `usuario_perfil`
- `PerfilPermissaoEntity` -> `perfil_permissao`

Repositories:

- `UsuarioPerfilJpaRepository`
- `PerfilPermissaoJpaRepository`

### catalogo

- `TurmaDisciplinaEntity` -> `turma_disciplina`

Repository:

- `TurmaDisciplinaJpaRepository`

Observacao: `TurmaDisciplinaEntity` referencia a `DisciplinaEntity` existente hoje em `historico`. A mudanca de `DisciplinaEntity` para `catalogo` fica como refatoracao propria, para nao ampliar o escopo desta fase.

### matricula

- `MatriculaDocumentoExigidoEntity` -> `matricula_documento_exigido`
- `MatriculaDocumentoEntregueEntity` -> `matricula_documento_entregue`

Repositories:

- `MatriculaDocumentoExigidoJpaRepository`
- `MatriculaDocumentoEntregueJpaRepository`

## Saldo atualizado

Comparando o `model.zip` / `scriptdb.sql` oficial com o backend atual:

| Situacao | Quantidade |
|---|---:|
| Total de tabelas do modelo | 77 |
| Tabelas cobertas por entidades no backend | 44 |
| Tabelas ainda faltantes | 33 |

## Validacao

Comando executado:

```powershell
.\mvnw.cmd test
```

Resultado:

```text
BUILD SUCCESS
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
```

## Proxima decisao

Ordem sugerida para a Fase 3:

1. `professor`
2. `rh`
3. `frequencia`

Esses dominios estao conectados por relacionamentos diretos:

- `ProfessorEntity` referencia `PessoaEntity`;
- `FuncionarioEntity` referencia `PessoaEntity` e `CargoEntity`;
- `AulaEntity` referencia `ProfessorEntity` e `TurmaEntity`;
- frequencia referencia `AlunoEntity`, `AulaEntity`, `SituacaoFrequenciaEntity` e `ProfessorEntity`.
