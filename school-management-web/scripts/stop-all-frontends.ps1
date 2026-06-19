param(
  [switch]$ClearLogs
)

$ErrorActionPreference = 'Stop'

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$logsRoot = Join-Path $workspaceRoot '.logs\frontends'
$pidFile = Join-Path $logsRoot 'frontend-processes.json'

function Get-TrackedProcesses {
  if (-not (Test-Path $pidFile)) {
    return @()
  }

  $content = Get-Content -Path $pidFile -Raw
  if ([string]::IsNullOrWhiteSpace($content)) {
    return @()
  }

  $parsed = $content | ConvertFrom-Json
  if ($parsed -is [System.Collections.IEnumerable] -and -not ($parsed -is [string])) {
    return @($parsed)
  }

  return @($parsed)
}

function Stop-TrackedProcess {
  param(
    $ProcessInfo
  )

  $existing = Get-Process -Id $ProcessInfo.pid -ErrorAction SilentlyContinue

  if ($null -eq $existing) {
    return [PSCustomObject]@{
      Name = $ProcessInfo.name
      Port = $ProcessInfo.port
      Pid = $ProcessInfo.pid
      Status = 'missing'
    }
  }

  Stop-Process -Id $ProcessInfo.pid -Force

  return [PSCustomObject]@{
    Name = $ProcessInfo.name
    Port = $ProcessInfo.port
    Pid = $ProcessInfo.pid
    Status = 'stopped'
  }
}

$tracked = Get-TrackedProcesses

if ($tracked.Count -eq 0) {
  Write-Output 'Nenhum frontend rastreado para encerrar.'

  if ($ClearLogs -and (Test-Path $logsRoot)) {
    Remove-Item -LiteralPath $logsRoot -Recurse -Force
    Write-Output "Logs removidos: $logsRoot"
  }

  exit 0
}

$results = foreach ($item in $tracked) {
  Stop-TrackedProcess -ProcessInfo $item
}

if (Test-Path $pidFile) {
  Remove-Item -LiteralPath $pidFile -Force
}

if ($ClearLogs -and (Test-Path $logsRoot)) {
  Remove-Item -LiteralPath $logsRoot -Recurse -Force
}

Write-Output 'Resultado da parada dos frontends:'
$results | ForEach-Object {
  Write-Output "- $($_.Name) -> porta $($_.Port) (pid=$($_.Pid)) status=$($_.Status)"
}

if ($ClearLogs) {
  Write-Output "Logs removidos: $logsRoot"
}
