# Historico de Documentacao e Scripts

Esta pasta guarda documentos e scripts preservados apenas para consulta historica.

## Regra de uso

Os arquivos daqui nao sao fonte ativa para novas implementacoes.

A referencia oficial de banco de dados e:

```text
docs/v2/scriptdb.sql
```

A documentacao ativa sobre essa decisao esta em:

```text
docs/20-base-dados-oficial-scriptdb.md
```

## Organizacao

- `planejamento-inicial/`: documentos de MVP, backlog, arquitetura e Jira criados nas fases iniciais do projeto.
- `refatoracao-v2/`: documentos e scripts de apoio da refatoracao v2 que foram superados pela decisao de usar `docs/v2/scriptdb.sql` como base oficial.
- `sql-legado/`: scripts SQL antigos, seeds e ajustes pontuais que nao devem orientar a modelagem atual.

## Orientacao

Se alguma informacao historica parecer necessaria para o produto atual, ela deve ser reavaliada contra `docs/v2/scriptdb.sql` e entao reescrita em um documento ativo fora desta pasta.
