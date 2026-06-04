# Consolidado - Fases 1 a 7 de entidades e dominios

Data: 2026-06-03

## Objetivo

Consolidar o trabalho executado para estruturar, no backend, as entidades e repositórios baseados no modelo enviado e no `scriptdb.sql`.

Este documento resume as fases 1 a 7, as decisões tomadas e o saldo final da cobertura das tabelas.

## Fonte de verdade

Foram usados como referência:

- `C:/Users/pjr_d/OneDrive/Documentos/model.zip`
- `C:/Users/pjr_d/OneDrive/Documentos/scriptdb.sql`
- `docs/v2/scriptdb.sql`
- `docs/v2/modelo_normalizado_escolar_v3_documentos_ia_dashboards.sql`

A diretriz técnica mantida foi ajustar o backend à base v2, sem alterar a modelagem oficial do banco.

## Resultado final

| Indicador | Quantidade |
| --- | ---: |
| Tabelas no modelo enviado | 77 |
| Tabelas no `scriptdb.sql` | 77 |
| Tabelas transacionais/de negócio estruturadas | 75 |
| Tabelas técnicas não mapeadas como domínio | 2 |
| Repositórios JPA encontrados pelo Spring após a última validação | 71 |

Tabelas técnicas mantidas fora dos domínios escolares:

- `flyway_audit_marker`
- `flyway_schema_history`

## Fase 1 - Auditoria

Documento: `docs/21-auditoria-fase-1-modelo-dominios.md`

Resultado:

- Confirmado que `model.zip` e `scriptdb.sql` possuem 77 tabelas cada.
- Confirmado que não havia divergência entre as tabelas do ZIP e do SQL.
- Identificado o saldo inicial:
  - 36 tabelas já cobertas.
  - 41 tabelas faltantes.

## Fase 2 - Apoio e vínculos simples

Documento: `docs/22-fase-2-entidades-apoio-vinculos.md`

Domínios tratados:

- `aluno`
- `seguranca`
- `catalogo`
- `matricula`

Principais entregas:

- Eventos e solicitações de exclusão de aluno.
- Tabelas de vínculo de segurança.
- Vínculo `turma_disciplina`.
- Documentos exigidos e entregues em matrícula.

Saldo após a fase:

- 44 tabelas cobertas.
- 33 tabelas faltantes.

## Fase 3 - Professor, RH e frequência

Documento: `docs/23-fase-3-professor-rh-frequencia.md`

Domínios tratados:

- `professor`
- `rh`
- `frequencia`

Principais entregas:

- Professor, professor por turma/disciplina e aula.
- Cargo e funcionário.
- Frequência de aluno, frequência de professor e situação de frequência.

Saldo após a fase:

- 52 tabelas cobertas.
- 25 tabelas faltantes.

## Fase 4 - Avaliação e histórico complementar

Documento: `docs/24-fase-4-avaliacao-historico-complementar.md`

Domínios tratados:

- `avaliacao`
- complemento de `historico`

Principais entregas:

- Tipo de avaliação, período avaliativo, avaliação e nota do aluno.
- Boletim e item de boletim.

Saldo após a fase:

- 58 tabelas cobertas.
- 19 tabelas faltantes.

## Fase 5 - Planejamento e IA

Documento: `docs/25-fase-5-planejamento-ia.md`

Domínios tratados:

- `planejamento`
- `ia`

Principais entregas:

- Planejamento de professor, aula, planejamento bimestral, aulas e avaliações bimestrais.
- Conteúdo gerado por IA, versões, interações, tipo/status de conteúdo e biblioteca pedagógica.
- Conversão de campos temporários `UUID` para relacionamentos JPA:
  - `AulaEntity.planejamentoAula`
  - `AvaliacaoEntity.planejamentoBimestralAvaliacao`

Saldo após a fase:

- 70 tabelas cobertas.
- 7 tabelas faltantes.

## Fase 6 - Dashboard

Documento: `docs/26-fase-6-dashboard.md`

Domínio tratado:

- `dashboard`

Principais entregas:

- Público de dashboard.
- Dashboard.
- Widgets.
- Snapshots de indicadores.
- Configuração de dashboard por usuário.

Decisão relevante:

- As restrições compostas do SQL foram respeitadas conceitualmente nos repositórios, sem transformar colunas individuais em `unique=true` quando a unicidade real é composta.

Saldo após a fase:

- 75 tabelas cobertas.
- 2 tabelas faltantes, ambas técnicas do Flyway.

## Fase 7 - Consolidação arquitetural

Documento: `docs/v2/27-fase-7-consolidacao-arquitetural.md`

Principais entregas:

- `DisciplinaEntity` movida de `historico` para `catalogo`.
- `DisciplinaJpaRepository` movido de `historico` para `catalogo`.
- Atualização dos consumidores:
  - `TurmaDisciplinaEntity`
  - `BibliotecaConteudoPedagogicoEntity`
  - `BoletimItemEntity`
  - `DisciplinaService`
  - `DisciplinaMapper`

Decisão relevante:

- O endpoint e os DTOs existentes de disciplina foram preservados para não ampliar o escopo da fase.

## Decisões arquiteturais consolidadas

### Flyway não virou domínio

As tabelas `flyway_audit_marker` e `flyway_schema_history` permanecem fora dos domínios escolares.

Motivo:

- `flyway_schema_history` é tabela interna do Flyway.
- `flyway_audit_marker` é técnica e relacionada à migração/auditoria.
- Nenhuma representa entidade transacional de negócio escolar.

### Estrutura criada não implementa fluxo de negócio completo

As fases 1 a 7 criaram a base estrutural JPA.

Não foram criados, salvo ajustes pontuais, novos:

- endpoints;
- services;
- DTOs;
- regras de negócio;
- casos de uso completos.

Essa separação foi intencional para estabilizar primeiro a estrutura de persistência.

### Pacotes foram aproximados dos domínios reais

A estrutura criada segue a organização por domínio:

- `aluno`
- `responsavel`
- `matricula`
- `catalogo`
- `historico`
- `avaliacao`
- `frequencia`
- `professor`
- `rh`
- `planejamento`
- `ia`
- `transferencia`
- `seguranca`
- `dashboard`
- `documento`
- `compartilhado`

## Validação

Ao final das fases implementadas, a validação executada foi:

```powershell
.\mvnw.cmd test
```

Último resultado registrado:

- Build: sucesso.
- Testes: 31.
- Falhas: 0.
- Erros: 0.
- Repositórios JPA encontrados pelo Spring: 71.

## Próximas frentes recomendadas

Com a estrutura de persistência estabilizada, os próximos trabalhos úteis são:

1. Revisar duplicidades conceituais e responsabilidades entre domínios.
2. Escolher um fluxo MVP e implementar de ponta a ponta com controller, service, DTO e testes.
3. Criar testes `@DataJpaTest` para os relacionamentos mais críticos.
4. Reavaliar, somente se houver necessidade real, uma camada técnica de infraestrutura para auditoria de migrações.

## Conclusão

As fases 1 a 7 concluíram a estruturação das tabelas transacionais de negócio do modelo enviado.

O saldo restante é exclusivamente técnico e não deve ser tratado como domínio escolar.
