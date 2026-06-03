# Fase 5 - Planejamento e IA

Data: 2026-06-03

## Escopo executado

Nesta fase foram estruturadas as tabelas dos dominios `planejamento` e `ia` que existiam no modelo enviado e ainda nao estavam cobertas pela estrutura atual do projeto.

Tambem foram ajustados os vinculos que nas fases anteriores ficaram temporariamente como `UUID`, porque as entidades de destino ainda nao existiam naquele momento.

## Entidades de planejamento adicionadas

As entidades abaixo foram mantidas no pacote:

`br.com.escola.planejamento.adapter.out.persistence.entity`

- `StatusPlanejamentoEntity` -> `status_planejamento`
- `PlanejamentoProfessorEntity` -> `planejamento_professor`
- `PlanejamentoAulaEntity` -> `planejamento_aula`
- `PlanejamentoBimestralEntity` -> `planejamento_bimestral`
- `PlanejamentoBimestralAulaEntity` -> `planejamento_bimestral_aula`
- `PlanejamentoBimestralAvaliacaoEntity` -> `planejamento_bimestral_avaliacao`

## Repositorios de planejamento adicionados

Pacote:

`br.com.escola.planejamento.adapter.out.persistence.repository`

- `StatusPlanejamentoJpaRepository`
- `PlanejamentoProfessorJpaRepository`
- `PlanejamentoAulaJpaRepository`
- `PlanejamentoBimestralJpaRepository`
- `PlanejamentoBimestralAulaJpaRepository`
- `PlanejamentoBimestralAvaliacaoJpaRepository`

## Entidades de IA adicionadas

Pacote:

`br.com.escola.ia.adapter.out.persistence.entity`

- `TipoConteudoIAEntity` -> `tipo_conteudo_ia`
- `StatusConteudoIAEntity` -> `status_conteudo_ia`
- `PlanejamentoIAInteracaoEntity` -> `planejamento_ia_interacao`
- `PlanejamentoIAConteudoGeradoEntity` -> `planejamento_ia_conteudo_gerado`
- `PlanejamentoIAConteudoVersaoEntity` -> `planejamento_ia_conteudo_versao`
- `BibliotecaConteudoPedagogicoEntity` -> `biblioteca_conteudo_pedagogico`

## Repositorios de IA adicionados

Pacote:

`br.com.escola.ia.adapter.out.persistence.repository`

- `TipoConteudoIAJpaRepository`
- `StatusConteudoIAJpaRepository`
- `PlanejamentoIAInteracaoJpaRepository`
- `PlanejamentoIAConteudoGeradoJpaRepository`
- `PlanejamentoIAConteudoVersaoJpaRepository`
- `BibliotecaConteudoPedagogicoJpaRepository`

## Ajustes de relacionamento

Foram substituidos os campos temporarios abaixo por relacionamentos JPA:

- `AulaEntity.planejamentoAulaId` passou a ser `PlanejamentoAulaEntity planejamentoAula`
- `AvaliacaoEntity.planejamentoBimestralAvaliacaoId` passou a ser `PlanejamentoBimestralAvaliacaoEntity planejamentoBimestralAvaliacao`

Esses ajustes fecham as referencias entre professor, avaliacao e planejamento.

## Validacao executada

Comando:

```powershell
.\mvnw.cmd test
```

Resultado:

- Build: sucesso
- Testes: 31
- Falhas: 0
- Erros: 0
- Repositorios JPA encontrados pelo Spring: 66

## Cobertura contra o modelo de 77 tabelas

Resultado apos a Fase 5:

| Indicador | Quantidade |
| --- | ---: |
| Tabelas no modelo enviado | 77 |
| Tabelas cobertas por entidades no projeto | 70 |
| Tabelas ainda faltantes | 7 |
| Repositorios JPA no projeto | 66 |

Tabelas ainda faltantes:

- `dashboard`
- `dashboard_indicador_snapshot`
- `dashboard_usuario_configuracao`
- `dashboard_widget`
- `publico_dashboard`
- `flyway_audit_marker`
- `flyway_schema_history`

## Proxima fase sugerida

A Fase 6 deve cobrir o dominio `dashboard`, com as 5 tabelas transacionais restantes:

- `dashboard`
- `dashboard_widget`
- `dashboard_indicador_snapshot`
- `dashboard_usuario_configuracao`
- `publico_dashboard`

As tabelas `flyway_audit_marker` e `flyway_schema_history` sao tecnicas e podem ser tratadas separadamente, sem virar dominio de negocio, salvo se for decidido criar um pacote tecnico especifico para auditoria/migracao.
