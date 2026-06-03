# Fase 7 - Consolidacao arquitetural

Data: 2026-06-03

## Objetivo

Consolidar a estrutura criada nas fases anteriores sem transformar tabelas tecnicas do Flyway em dominio de negocio.

A diretriz usada foi manter o backend aderente a modelagem oficial da base v2 e corrigir inconsistencias de pacote quando isso pudesse ser feito sem mudar tabela, coluna, constraint ou contrato HTTP.

## Ajuste executado

Foi corrigida a localizacao da entidade `disciplina`.

Antes:

- `DisciplinaEntity` em `br.com.escola.historico.adapter.out.persistence.entity`
- `DisciplinaJpaRepository` em `br.com.escola.historico.adapter.out.persistence.repository`

Depois:

- `DisciplinaEntity` em `br.com.escola.catalogo.adapter.out.persistence.entity`
- `DisciplinaJpaRepository` em `br.com.escola.catalogo.adapter.out.persistence.repository`

Justificativa:

- A tabela `disciplina` pertence ao catalogo academico.
- As entidades novas `TurmaDisciplinaEntity` e `BibliotecaConteudoPedagogicoEntity` ja dependiam conceitualmente de disciplina como catalogo.
- A mudanca nao altera a tabela `disciplina`, os nomes de colunas, nem o contrato dos endpoints existentes.

## Referencias atualizadas

Foram atualizados os imports que consumiam `DisciplinaEntity` ou `DisciplinaJpaRepository`:

- `TurmaDisciplinaEntity`
- `BibliotecaConteudoPedagogicoEntity`
- `BoletimItemEntity`
- `DisciplinaService`
- `DisciplinaMapper`

O endpoint e os DTOs atuais de disciplina foram preservados, para evitar ampliar o escopo desta fase.

## Decisao sobre Flyway

As tabelas abaixo permanecem fora da estrutura de dominio:

- `flyway_audit_marker`
- `flyway_schema_history`

Motivo:

- `flyway_schema_history` e uma tabela interna de controle do Flyway.
- `flyway_audit_marker` e uma tabela tecnica relacionada a migracao/auditoria.
- Nenhuma das duas representa uma entidade transacional de negocio escolar.

Caso seja necessario expor auditoria tecnica no futuro, isso deve ser feito em pacote de infraestrutura, nao em dominio de negocio.

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

## Cobertura apos a fase

| Indicador | Quantidade |
| --- | ---: |
| Tabelas no modelo enviado | 77 |
| Tabelas transacionais/de negocio cobertas | 75 |
| Tabelas tecnicas nao mapeadas como dominio | 2 |
| Repositorios JPA no projeto | 71 |

Tabelas tecnicas restantes:

- `flyway_audit_marker`
- `flyway_schema_history`

## Conclusao

A Fase 7 fecha a consolidacao inicial das entidades criadas a partir do modelo enviado.

Todas as tabelas transacionais de negocio estao estruturadas no backend. O saldo restante e tecnico e deve continuar fora dos dominios escolares, salvo decisao futura de criar uma camada especifica de infraestrutura para auditoria de migracoes.
