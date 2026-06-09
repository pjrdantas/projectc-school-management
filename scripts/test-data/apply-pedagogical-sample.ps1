param(
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [string]$Database = "gestao_escolar",
    [string]$Username = "postgres"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptDir "pedagogical-sample-data.sql"

if (-not (Test-Path -LiteralPath $sqlFile)) {
    throw "Arquivo SQL não encontrado: $sqlFile"
}

$psql = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psql) {
    throw "psql não encontrado no PATH. Instale o cliente PostgreSQL ou execute o arquivo SQL manualmente no banco local."
}

& $psql.Source -h $HostName -p $Port -U $Username -d $Database -v ON_ERROR_STOP=1 -f $sqlFile
