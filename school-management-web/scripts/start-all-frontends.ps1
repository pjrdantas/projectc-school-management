param(
  [switch]$InstallMissing
)

$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$logsRoot = Join-Path $workspaceRoot '.logs\frontends'
$pidFile = Join-Path $logsRoot 'frontend-processes.json'

$apps = @(
  @{ Name = 'host'; Port = 4200; Path = 'host' },
  @{ Name = 'mfe-matriculas'; Port = 4202; Path = 'mfe-matriculas' },
  @{ Name = 'mfe-catalogo-academico'; Port = 4203; Path = 'mfe-catalogo-academico' },
  @{ Name = 'mfe-dashboard'; Port = 4204; Path = 'mfe-dashboard' },
  @{ Name = 'mfe-planejamento-ia'; Port = 4205; Path = 'mfe-planejamento-ia' },
  @{ Name = 'mfe-professores'; Port = 4206; Path = 'mfe-professores' },
  @{ Name = 'mfe-aulas-avaliacoes'; Port = 4207; Path = 'mfe-aulas-avaliacoes' },
  @{ Name = 'mfe-responsaveis'; Port = 4208; Path = 'mfe-responsaveis' },
  @{ Name = 'mfe-alunos'; Port = 4209; Path = 'mfe-alunos' }
)

function Test-PortInUse {
  param(
    [int]$Port
  )

  $listener = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
  return $null -ne $listener
}

function Start-FrontendProcess {
  param(
    [hashtable]$App
  )

  $appRoot = Join-Path $workspaceRoot $App.Path

  if (-not (Test-Path $appRoot)) {
    throw "Diretorio nao encontrado para $($App.Name): $appRoot"
  }

  $packageJson = Join-Path $appRoot 'package.json'
  if (-not (Test-Path $packageJson)) {
    throw "package.json nao encontrado para $($App.Name): $packageJson"
  }

  $nodeModules = Join-Path $appRoot 'node_modules'
  if (-not (Test-Path $nodeModules)) {
    if (-not $InstallMissing) {
      throw "node_modules ausente em $($App.Name). Rode npm install ou use -InstallMissing."
    }

    Write-Output "Instalando dependencias de $($App.Name)..."
    npm install --prefix $appRoot | Out-Host
  }

  $stdoutLog = Join-Path $logsRoot "$($App.Name).out.log"
  $stderrLog = Join-Path $logsRoot "$($App.Name).err.log"

  if (Test-PortInUse -Port $App.Port) {
    throw "A porta $($App.Port) ja esta em uso. Nao foi possivel iniciar $($App.Name)."
  }

  Write-Output "Iniciando $($App.Name) em http://localhost:$($App.Port)..."

  $command = @"
Set-Location '$appRoot'
npm start
"@

  $process = Start-Process -FilePath 'powershell' `
    -ArgumentList '-NoLogo', '-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $command `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -PassThru

  return [PSCustomObject]@{
    name = $App.Name
    port = $App.Port
    path = $appRoot
    pid = $process.Id
    stdoutLog = $stdoutLog
    stderrLog = $stderrLog
  }
}

New-Item -ItemType Directory -Path $logsRoot -Force | Out-Null

$started = @()

try {
  foreach ($app in $apps) {
    $started += Start-FrontendProcess -App $app
  }
}
catch {
  foreach ($item in $started) {
    try {
      Stop-Process -Id $item.pid -Force -ErrorAction SilentlyContinue
    }
    catch {
    }
  }

  throw
}

$started | ConvertTo-Json | Set-Content -Path $pidFile

Write-Output ''
Write-Output 'Frontends iniciados:'
$started | ForEach-Object {
  Write-Output "- $($_.name) -> http://localhost:$($_.port) (pid=$($_.pid))"
}
Write-Output ''
Write-Output "Logs: $logsRoot"
Write-Output "PIDs:  $pidFile"
