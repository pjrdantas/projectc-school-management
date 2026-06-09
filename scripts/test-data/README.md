# Massa de teste local

Scripts para popular o banco local com dados de teste sem usar Flyway.

## Aplicar

```powershell
.\scripts\test-data\apply-pedagogical-sample.ps1
```

Por padrão, o script usa:

- host: `localhost`
- porta: `5432`
- banco: `gestao_escolar`
- usuário: `postgres`

Para informar senha sem prompt:

```powershell
$env:PGPASSWORD = 'root123'
.\scripts\test-data\apply-pedagogical-sample.ps1
```

## Remover a massa

O arquivo `pedagogical-sample-data.sql` possui um bloco comentado no início para remover somente os dados desta massa, usando os UUIDs fixos do próprio script.

Não mover estes scripts para `school-management-service/src/main/resources/db/migration`.
