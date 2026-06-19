param(
  [switch]$Detailed
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

function Get-TrackedProcesses {
  if (-not (Test-Path $pidFile)) {
    return @{}
  }

  $content = Get-Content -Path $pidFile -Raw
  if ([string]::IsNullOrWhiteSpace($content)) {
    return @{}
  }

  $parsed = $content | ConvertFrom-Json
  $items = if ($parsed -is [System.Collections.IEnumerable] -and -not ($parsed -is [string])) {
    @($parsed)
  }
  else {
    @($parsed)
  }

  $result = @{}
  foreach ($item in $items) {
    $result[$item.name] = $item
  }

  return $result
}

function Get-FrontendStatus {
  param(
    [hashtable]$App,
    [hashtable]$Tracked
  )

  $trackedInfo = $Tracked[$App.Name]
  $trackedPid = if ($null -ne $trackedInfo) { [int]$trackedInfo.pid } else { $null }
  $trackedProcess = if ($null -ne $trackedPid) { Get-Process -Id $trackedPid -ErrorAction SilentlyContinue } else { $null }
  $listener = Get-NetTCPConnection -State Listen -LocalPort $App.Port -ErrorAction SilentlyContinue | Select-Object -First 1
  $listenerPid = if ($null -ne $listener) { [int]$listener.OwningProcess } else { $null }
  $listenerProcess = if ($null -ne $listenerPid) { Get-Process -Id $listenerPid -ErrorAction SilentlyContinue } else { $null }

  $status = 'stopped'
  $resolvedPid = $null

  if ($null -ne $trackedProcess -and $null -ne $listener -and $trackedPid -eq $listenerPid) {
    $status = 'running-tracked'
    $resolvedPid = $trackedPid
  }
  elseif ($null -ne $trackedProcess) {
    $status = 'running-tracked-no-listener'
    $resolvedPid = $trackedPid
  }
  elseif ($null -ne $listener) {
    $status = 'running-untracked'
    $resolvedPid = $listenerPid
  }
  elseif ($null -ne $trackedInfo) {
    $status = 'tracked-missing'
    $resolvedPid = $trackedPid
  }

  $stdoutLog = Join-Path $logsRoot "$($App.Name).out.log"
  $stderrLog = Join-Path $logsRoot "$($App.Name).err.log"

  return [PSCustomObject]@{
    Name = $App.Name
    Port = $App.Port
    Status = $status
    Pid = $resolvedPid
    ListenerPid = $listenerPid
    ProcessName = if ($null -ne $trackedProcess) { $trackedProcess.ProcessName } elseif ($null -ne $listenerProcess) { $listenerProcess.ProcessName } else { $null }
    Tracked = $null -ne $trackedInfo
    Path = Join-Path $workspaceRoot $App.Path
    StdoutLog = $stdoutLog
    StderrLog = $stderrLog
  }
}

$tracked = Get-TrackedProcesses
$results = foreach ($app in $apps) {
  Get-FrontendStatus -App $app -Tracked $tracked
}

Write-Output 'Status dos frontends:'
$results |
  Select-Object Name, Port, Status, Pid, ProcessName, Tracked |
  Format-Table -AutoSize

if ($Detailed) {
  Write-Output ''
  Write-Output 'Detalhes:'
  $results | ForEach-Object {
    Write-Output "- $($_.Name)"
    Write-Output "  path: $($_.Path)"
    Write-Output "  stdout: $($_.StdoutLog)"
    Write-Output "  stderr: $($_.StderrLog)"
  }
}

Write-Output ''
Write-Output "PIDs rastreados: $pidFile"
Write-Output "Logs: $logsRoot"
