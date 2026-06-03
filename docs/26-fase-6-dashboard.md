# Fase 6 - Dashboard

Data: 2026-06-03

## Escopo executado

Nesta fase foram estruturadas as tabelas transacionais restantes do dominio `dashboard`, mantendo o padrao de pacotes atual do projeto:

`br.com.escola.dashboard.adapter.out.persistence`

As tabelas tecnicas do Flyway ficaram fora do dominio de negocio.

## Entidades adicionadas

Pacote:

`br.com.escola.dashboard.adapter.out.persistence.entity`

- `PublicoDashboardEntity` -> `publico_dashboard`
- `DashboardEntity` -> `dashboard`
- `DashboardWidgetEntity` -> `dashboard_widget`
- `DashboardIndicadorSnapshotEntity` -> `dashboard_indicador_snapshot`
- `DashboardUsuarioConfiguracaoEntity` -> `dashboard_usuario_configuracao`

## Repositorios adicionados

Pacote:

`br.com.escola.dashboard.adapter.out.persistence.repository`

- `PublicoDashboardJpaRepository`
- `DashboardJpaRepository`
- `DashboardWidgetJpaRepository`
- `DashboardIndicadorSnapshotJpaRepository`
- `DashboardUsuarioConfiguracaoJpaRepository`

## Observacao sobre unicidade

O modelo Java gerado marcava algumas colunas como `unique=true` individualmente, mas o `scriptdb.sql` define restricoes compostas para:

- `dashboard_widget`: `id_dashboard`, `codigo`
- `dashboard_usuario_configuracao`: `id_usuario`, `id_dashboard_widget`
- `dashboard_indicador_snapshot`: `id_publico_dashboard`, `codigo_indicador`, `referencia_data`

Por isso, as entidades foram criadas sem forcar unicidade individual nessas colunas. Os repositorios refletem as consultas pelos campos compostos mais importantes.

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
- Repositorios JPA encontrados pelo Spring: 71

## Cobertura contra o modelo de 77 tabelas

Resultado apos a Fase 6:

| Indicador | Quantidade |
| --- | ---: |
| Tabelas no modelo enviado | 77 |
| Tabelas cobertas por entidades no projeto | 75 |
| Tabelas ainda faltantes | 2 |
| Repositorios JPA no projeto | 71 |

Tabelas ainda faltantes:

- `flyway_audit_marker`
- `flyway_schema_history`

## Conclusao

Todas as tabelas transacionais de negocio do modelo enviado foram estruturadas em dominios do projeto.

As duas tabelas restantes sao tecnicas de controle/migracao do Flyway. Elas podem permanecer fora da estrutura de dominio, ou serem tratadas em uma fase separada de infraestrutura caso seja necessario mapear auditoria tecnica de migracoes.
