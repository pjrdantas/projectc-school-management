# 23 - Fase 3: professor, RH e frequencia

## Objetivo

Criar as entidades e repositories dos dominios `professor`, `rh` e `frequencia`, usando o `scriptdb.sql` oficial e o `model.zip` como referencia de estrutura.

Esta fase nao cria endpoints, services ou regras de negocio novas.

## Entidades criadas

### professor

- `ProfessorEntity` -> `professor`
- `ProfessorTurmaDisciplinaEntity` -> `professor_turma_disciplina`
- `AulaEntity` -> `aula`

Repositories:

- `ProfessorJpaRepository`
- `ProfessorTurmaDisciplinaJpaRepository`
- `AulaJpaRepository`

Observacao: `AulaEntity` possui a coluna `id_planejamento_aula`. Como `PlanejamentoAulaEntity` pertence a fase futura de planejamento, a coluna foi mapeada temporariamente como `UUID planejamentoAulaId`. Na fase de planejamento, esse campo deve ser convertido para relacionamento com `PlanejamentoAulaEntity`.

### rh

- `CargoEntity` -> `cargo`
- `FuncionarioEntity` -> `funcionario`

Repositories:

- `CargoJpaRepository`
- `FuncionarioJpaRepository`

### frequencia

- `SituacaoFrequenciaEntity` -> `situacao_frequencia`
- `FrequenciaAlunoEntity` -> `frequencia_aluno`
- `FrequenciaProfessorEntity` -> `frequencia_professor`

Repositories:

- `SituacaoFrequenciaJpaRepository`
- `FrequenciaAlunoJpaRepository`
- `FrequenciaProfessorJpaRepository`

## Saldo atualizado

Comparando o `model.zip` / `scriptdb.sql` oficial com o backend atual:

| Situacao | Quantidade |
|---|---:|
| Total de tabelas do modelo | 77 |
| Tabelas cobertas por entidades no backend | 52 |
| Tabelas ainda faltantes | 25 |

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

Ordem sugerida para a Fase 4:

1. `avaliacao`
2. complementos de `historico`: `BoletimEntity` e `BoletimItemEntity`

Esses dominios se conectam por notas, periodos avaliativos, boletins e itens de boletim.
