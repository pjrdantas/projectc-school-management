# 20 - Base de Dados Oficial (`scriptdb.sql`)

## Objetivo

Este documento registra a decisao de que o arquivo `docs/v2/scriptdb.sql` e a referencia oficial de banco de dados do projeto neste momento.

Qualquer novo desenvolvimento, documentacao tecnica, exemplo de payload, seed ou ajuste de contrato deve considerar `docs/v2/scriptdb.sql` como base ativa. Materiais anteriores que descrevem outra modelagem foram organizados em `docs/historico` para consulta e nao devem interferir na concepcao atual do projeto.

## Arquivo oficial

```text
docs/v2/scriptdb.sql
```

O arquivo representa a base `gestao_escolar` em uso, incluindo:

- estrutura das tabelas;
- constraints;
- indices;
- relacionamentos;
- dados de apoio/seed existentes no dump;
- historico Flyway gravado na base exportada.

## Como recriar a base local

Exemplo usando PostgreSQL local:

```bash
createdb -U postgres gestao_escolar
psql -U postgres -d gestao_escolar -f docs/v2/scriptdb.sql
```

Se a base ja existir e puder ser recriada do zero:

```bash
dropdb -U postgres gestao_escolar
createdb -U postgres gestao_escolar
psql -U postgres -d gestao_escolar -f docs/v2/scriptdb.sql
```

## Tabelas principais do `scriptdb.sql`

### Pessoas e cadastro escolar

- `aluno`
- `responsavel`
- `aluno_responsavel`

### Academico

- `periodo_letivo`
- `serie`
- `turma`
- `disciplina`

### Matricula

- `matricula`
- `matricula_evento_status`
- `matricula_pendencia_documental`

### Documentos, historico e transferencia

- `documento`
- `documento_vinculo`
- `historico_escolar`
- `historico_escolar_item`
- `escola_origem`
- `transferencia_aluno`

### Acesso

- `usuario`
- `perfil`
- `permissao`
- `usuario_perfil`
- `perfil_permissao`
- `sessao_autenticacao`

### Controle tecnico

- `flyway_schema_history`
- `flyway_audit_marker`

## Regras de alinhamento

1. `docs/v2/scriptdb.sql` e a fonte oficial para nomes de tabelas, colunas, constraints e relacionamentos.
2. Scripts SQL antigos devem ser tratados como historicos; a copia organizada fica em `docs/historico/sql-legado`.
3. Documentos antigos de planejamento ou refatoracao devem ser tratados como historicos; a copia organizada fica em `docs/historico`.
4. Novos documentos ativos devem referenciar este arquivo quando falarem da base de dados.
5. Caso codigo, README ou exemplos entrem em conflito com `scriptdb.sql`, o documento/codigo deve ser revisado ou o material antigo deve ser reclassificado como historico.

## Observacao sobre materiais historicos

A pasta `docs/historico` nao deve ser usada como fonte para novas implementacoes. Ela existe apenas para preservar contexto, decisoes anteriores, scripts legados e planejamento antigo.
