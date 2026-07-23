# projectc-school-management

Plataforma distribuida de gestao escolar com backend Spring Boot, BFF e
frontends Angular. A direcao tecnica vigente esta em `docs/v2`.

## Backend ativo

O `pom.xml` da raiz agrega exclusivamente os modulos operacionais:

- `school-management-bff`
- `identity-access-service`
- `institutional-tenant-service`
- `academic-catalog-service`
- `academic-professor-service`
- `people-service`
- `responsibles-service`
- `enrollment-document-service`
- `pedagogical-service`
- `planning-ai-service`
- `dashboard-query-service`

O `school-management-service` foi descomissionado. Os contratos operacionais
pertencem ao BFF e aos servicos modulares listados acima; o modulo legado nao
faz parte do checkout nem da topologia da plataforma.

## Build

Validacao agregada dos servicos ativos:

```powershell
mvn.cmd clean test
```

Validacao de um unico modulo:

```powershell
mvn.cmd -pl <modulo> clean test
```

Infraestrutura local compartilhada:

```powershell
docker compose -f platform/compose.yaml up -d
```

O Compose inicia apenas PostgreSQL, Kafka, MongoDB e Redis. Cada servico deve
ser iniciado pelo proprio modulo com suas credenciais internas e conexoes
configuradas por variaveis de ambiente.

O ponto de entrada planejado para todos os contratos externos e o
`school-management-bff`, na porta local `8081`. Enquanto a D16 estiver aberta,
nao se deve declarar operacao integral sem o modulo residual.

## Frontend

O host e os microfrontends ficam em `school-management-web`. O backend padrao
para desenvolvimento local e o BFF:

```text
http://localhost:8081
```

As instrucoes especificas de cada frontend permanecem no respectivo diretorio.

## Banco e documentacao

- `platform/compose.yaml`: infraestrutura compartilhada local.
- `platform/postgres/init`: criacao dos bancos dos servicos.
- `docs/v2`: arquitetura, roadmap e historico tecnico vigentes.
- `docs/historico`: material antigo sem autoridade sobre a implementacao atual.
- `projetos-historico-diario`: prototipos e referencias fora da topologia ativa.

Migrations Flyway de cada servico pertencem ao proprio modulo. Os scripts em
`scripts/test-data` sao dados auxiliares e nunca devem ser promovidos para
migration de runtime.
