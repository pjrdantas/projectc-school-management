param(
    [int]$MonolithPort = 18080,
    [int]$ShadowPort = 18092,
    [string]$InternalToken = "shadow-token",
    [string]$ReportPath = ""
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent (Split-Path -Parent $scriptDir)
$artifactsDir = Join-Path $repoRoot "target\professor-shadow-operational-smoke"

if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $artifactsDir "report.json"
}

New-Item -ItemType Directory -Force -Path $artifactsDir | Out-Null

$monolithStdout = Join-Path $artifactsDir "school-management-service.stdout.log"
$monolithStderr = Join-Path $artifactsDir "school-management-service.stderr.log"
$shadowStdout = Join-Path $artifactsDir "academic-professor-service.stdout.log"
$shadowStderr = Join-Path $artifactsDir "academic-professor-service.stderr.log"

function Start-BackgroundPowerShell {
    param(
        [string]$Command,
        [string]$StdoutPath,
        [string]$StderrPath
    )

    Start-Process `
        -FilePath "powershell" `
        -ArgumentList @("-NoLogo", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $Command) `
        -RedirectStandardOutput $StdoutPath `
        -RedirectStandardError $StderrPath `
        -PassThru `
        -WindowStyle Hidden
}

function Wait-Healthy {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5
            if ($response.status -eq "UP") {
                return $response
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    throw "Timeout aguardando health UP em $Url"
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )

    $params = @{
        Uri = $Url
        Method = $Method
        Headers = $Headers
        TimeoutSec = 20
    }

    if ($null -ne $Body) {
        $params["ContentType"] = "application/json"
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }

    try {
        $response = Invoke-WebRequest @params
    } catch {
        $exceptionResponse = $_.Exception.Response
        if ($null -eq $exceptionResponse) {
            throw
        }

        $reader = New-Object System.IO.StreamReader($exceptionResponse.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Dispose()

        $response = [pscustomobject]@{
            StatusCode = [int]$exceptionResponse.StatusCode
            Content = $content
        }
    }
    if ($response.StatusCode -ne $ExpectedStatus) {
        throw "Resposta inesperada em ${Url}: esperado $ExpectedStatus, obtido $($response.StatusCode). Corpo: $($response.Content)"
    }

    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }

    $response.Content | ConvertFrom-Json
}

$monolithProcess = $null
$shadowProcess = $null

try {
    $monolithCommand = @"
Set-Location '$repoRoot\school-management-service'
`$env:SERVER_PORT = '$MonolithPort'
`$env:PROFESSOR_SHADOW_BASE_URL = 'http://localhost:$ShadowPort'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=professor-shadow-operational'
"@

    $shadowCommand = @"
Set-Location '$repoRoot'
`$env:SERVER_PORT = '$ShadowPort'
`$env:MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS = 'always'
`$env:PROFESSOR_SHADOW_INTERNAL_API_TOKEN = '$InternalToken'
`$env:PROFESSOR_SHADOW_MONOLITH_BASE_URL = 'http://localhost:$MonolithPort'
Set-Location '$repoRoot\academic-professor-service'
mvn spring-boot:run
"@

    $monolithProcess = Start-BackgroundPowerShell -Command $monolithCommand -StdoutPath $monolithStdout -StderrPath $monolithStderr
    Wait-Healthy -Url "http://localhost:$MonolithPort/actuator/health" | Out-Null

    $shadowProcess = Start-BackgroundPowerShell -Command $shadowCommand -StdoutPath $shadowStdout -StderrPath $shadowStderr
    Wait-Healthy -Url "http://localhost:$ShadowPort/actuator/health" | Out-Null

    $login = Invoke-Json -Method Post -Url "http://localhost:$MonolithPort/api/auth/login" -Body @{
        login = "professor-shadow-smoke"
        senha = "senha123"
    }

    $accessToken = $login.accessToken
    if ([string]::IsNullOrWhiteSpace($accessToken)) {
        throw "Login do smoke nao retornou accessToken."
    }

    $authHeaders = @{
        Authorization = "Bearer $accessToken"
    }

    $contexto = Invoke-Json -Method Get -Url "http://localhost:$MonolithPort/api/auth/contexto-atual" -Headers $authHeaders
    $escolaId = $contexto.escolaId
    $usuarioId = $contexto.usuarioId

    $professoresMonolith = Invoke-Json -Method Get -Url "http://localhost:$MonolithPort/api/professores" -Headers $authHeaders
    if (@($professoresMonolith).Count -lt 1) {
        throw "O monolito nao retornou professores no smoke operacional."
    }

    $professorId = @($professoresMonolith)[0].id
    $professorMonolith = Invoke-Json -Method Get -Url "http://localhost:$MonolithPort/api/professores/$professorId" -Headers $authHeaders

    $shadowHeaders = @{
        Authorization = "Bearer $accessToken"
        "X-Internal-Token" = $InternalToken
        "X-Correlation-Id" = "professor-shadow-operational-smoke"
        "X-Usuario-Id" = "$usuarioId"
        "X-Escola-Id" = "$escolaId"
    }

    $professoresShadow = Invoke-Json -Method Get -Url "http://localhost:$ShadowPort/internal/v1/professores" -Headers $shadowHeaders
    $professorShadow = Invoke-Json -Method Get -Url "http://localhost:$ShadowPort/internal/v1/professores/$professorId" -Headers $shadowHeaders
    $notFoundId = [guid]::NewGuid()
    Invoke-Json -Method Get -Url "http://localhost:$ShadowPort/internal/v1/professores/$notFoundId" -Headers $shadowHeaders -ExpectedStatus 404 | Out-Null

    $monolithHealth = Invoke-Json -Method Get -Url "http://localhost:$MonolithPort/actuator/health/professorInternalClient"
    $shadowHealth = Invoke-Json -Method Get -Url "http://localhost:$ShadowPort/actuator/health/professorShadowMonolith"

    $report = [ordered]@{
        generatedAt = (Get-Date).ToString("o")
        ports = @{
            schoolManagementService = $MonolithPort
            academicProfessorService = $ShadowPort
        }
        seededUser = @{
            username = "professor-shadow-smoke"
            escolaId = $escolaId
            usuarioId = $usuarioId
        }
        smoke = @{
            monolithListCount = @($professoresMonolith).Count
            shadowListCount = @($professoresShadow).Count
            professorId = $professorId
            monolithProfessorNome = $professorMonolith.nomeCompleto
            shadowProfessorNome = $professorShadow.nomeCompleto
            shadowNotFoundId = $notFoundId
        }
        health = @{
            schoolManagementProfessorInternalClient = $monolithHealth
            academicProfessorShadowMonolith = $shadowHealth
        }
        logs = @{
            schoolManagementStdout = $monolithStdout
            schoolManagementStderr = $monolithStderr
            academicProfessorStdout = $shadowStdout
            academicProfessorStderr = $shadowStderr
        }
    }

    $reportJson = $report | ConvertTo-Json -Depth 20
    Set-Content -Path $ReportPath -Value $reportJson -Encoding UTF8
    Write-Output $reportJson
}
finally {
    if ($shadowProcess -and -not $shadowProcess.HasExited) {
        Stop-Process -Id $shadowProcess.Id -Force
    }
    if ($monolithProcess -and -not $monolithProcess.HasExited) {
        Stop-Process -Id $monolithProcess.Id -Force
    }
}
