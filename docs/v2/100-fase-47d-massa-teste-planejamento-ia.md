# Fase 47D - Massa de teste de planejamento com IA

## Objetivo

Gerar uma massa local e controlada para validar os fluxos entregues nas fases de planejamento bimestral, conteudos de IA e biblioteca pedagogica, antes de seguir para novas fases funcionais.

## Arquivo criado

- `docs/sql/seed-planejamento-ia-multiescola.sql`

O script e manual, idempotente e nao faz parte do Flyway. Ele limpa apenas os registros criados pelos UUIDs fixos da propria massa.

## Escopo da massa

- Escola padrao para teste local.
- Catalogos minimos de nivel de ensino, turno, status de planejamento, tipo de avaliacao, tipo de conteudo IA e status de conteudo IA.
- Periodo letivo de 2026 com dois bimestres.
- Serie, disciplina, turma e vinculo turma-disciplina.
- Pessoa, professor e alocacao professor-turma-disciplina.
- Dois planejamentos bimestrais:
  - um planejamento aprovado com aulas, avaliacao, conteudo IA aprovado e item publicado na biblioteca;
  - um planejamento em rascunho com conteudo IA ainda em edicao.
- Interacoes simuladas com IA e historico de versoes de conteudo gerado.

## Uso local

```powershell
$env:PGPASSWORD='root123'
psql -U postgres -d gestao_escolar -f docs/sql/seed-planejamento-ia-multiescola.sql
```

## Limites

- A massa nao deve ser usada como migration.
- O script nao cria usuario de aplicacao; os campos opcionais de auditoria de IA permanecem nulos quando aplicavel.
- O objetivo e apoiar testes manuais e contratos dos endpoints ja implementados, sem introduzir refatoracoes ou novos comportamentos.

## Validacao esperada

- Executar o script em banco local quando for necessario testar as telas/endpoints com dados reais.
- Validar o backend com `.\mvnw.cmd test` em `school-management-service` apos alteracoes relacionadas ao backend.
