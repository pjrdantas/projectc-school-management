# 24 - Fase 4: avaliacao e historico complementar

## Objetivo

Criar as entidades e repositories dos dominios `avaliacao` e complementos de `historico`, usando o `scriptdb.sql` oficial e o `model.zip` como referencia de estrutura.

Esta fase nao cria endpoints, services ou regras de negocio novas.

## Entidades criadas

### avaliacao

- `TipoAvaliacaoEntity` -> `tipo_avaliacao`
- `PeriodoAvaliativoEntity` -> `periodo_avaliativo`
- `AvaliacaoEntity` -> `avaliacao`
- `NotaAlunoEntity` -> `nota_aluno`

Repositories:

- `TipoAvaliacaoJpaRepository`
- `PeriodoAvaliativoJpaRepository`
- `AvaliacaoJpaRepository`
- `NotaAlunoJpaRepository`

Observacao: `AvaliacaoEntity` possui a coluna `id_planejamento_bimestral_avaliacao`. Como `PlanejamentoBimestralAvaliacaoEntity` pertence a fase futura de planejamento, a coluna foi mapeada temporariamente como `UUID planejamentoBimestralAvaliacaoId`. Na fase de planejamento, esse campo deve ser convertido para relacionamento com `PlanejamentoBimestralAvaliacaoEntity`.

### historico

- `BoletimEntity` -> `boletim`
- `BoletimItemEntity` -> `boletim_item`

Repositories:

- `BoletimJpaRepository`
- `BoletimItemJpaRepository`

Observacao: `BoletimItemEntity` referencia `DisciplinaEntity`, que ainda existe no dominio `historico`. A migracao de `DisciplinaEntity` para `catalogo` continua como ponto de refatoracao separado.

## Saldo atualizado

Comparando o `model.zip` / `scriptdb.sql` oficial com o backend atual:

| Situacao | Quantidade |
|---|---:|
| Total de tabelas do modelo | 77 |
| Tabelas cobertas por entidades no backend | 58 |
| Tabelas ainda faltantes | 19 |

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

Ordem sugerida para a Fase 5:

1. `planejamento`
2. `ia`

Esses dominios se conectam diretamente porque conteudos gerados por IA dependem dos planejamentos e suas versoes.
