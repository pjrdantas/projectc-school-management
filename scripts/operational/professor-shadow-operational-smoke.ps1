param(
    [int]$MonolithPort = 18080,
    [int]$ShadowPort = 18092,
    [string]$InternalToken = "shadow-token",
    [string]$ReportPath = ""
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Net.Http

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

function Test-PortAvailable {
    param(
        [int]$Port
    )

    $listener = $null
    try {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
        $listener.Start()
        return $true
    } catch {
        return $false
    } finally {
        if ($listener -ne $null) {
            $listener.Stop()
        }
    }
}

function Get-FreePort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    $listener.Start()
    try {
        return $listener.LocalEndpoint.Port
    } finally {
        $listener.Stop()
    }
}

function Resolve-Port {
    param(
        [int]$PreferredPort,
        [int[]]$ReservedPorts = @()
    )

    if ($PreferredPort -gt 0 -and $ReservedPorts -notcontains $PreferredPort -and (Test-PortAvailable -Port $PreferredPort)) {
        return $PreferredPort
    }

    do {
        $candidate = Get-FreePort
    } while ($ReservedPorts -contains $candidate)

    return $candidate
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

    $httpClient = [System.Net.Http.HttpClient]::new()
    $httpClient.Timeout = [TimeSpan]::FromSeconds(20)

    try {
        foreach ($key in $Headers.Keys) {
            [void]$httpClient.DefaultRequestHeaders.TryAddWithoutValidation($key, [string]$Headers[$key])
        }

        $request = [System.Net.Http.HttpRequestMessage]::new(
            [System.Net.Http.HttpMethod]::new($Method),
            $Url)

        if ($null -ne $Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $request.Content = [System.Net.Http.StringContent]::new($jsonBody, [System.Text.Encoding]::UTF8, "application/json")
        }

        $httpResponse = $httpClient.SendAsync($request).GetAwaiter().GetResult()
        $content = $httpResponse.Content.ReadAsStringAsync().GetAwaiter().GetResult()

        $response = [pscustomobject]@{
            StatusCode = [int]$httpResponse.StatusCode
            Content = $content
        }
    }
    finally {
        $httpClient.Dispose()
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
$resolvedMonolithPort = Resolve-Port -PreferredPort $MonolithPort
$resolvedShadowPort = Resolve-Port -PreferredPort $ShadowPort -ReservedPorts @($resolvedMonolithPort)

try {
    $monolithCommand = @"
Set-Location '$repoRoot\school-management-service'
`$env:SERVER_PORT = '$resolvedMonolithPort'
`$env:SPRING_PROFILES_ACTIVE = 'professor-shadow-operational'
`$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:professorShadowOperational;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE'
`$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
`$env:SPRING_DATASOURCE_USERNAME = 'sa'
`$env:SPRING_DATASOURCE_PASSWORD = ''
`$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'create-drop'
`$env:SPRING_FLYWAY_ENABLED = 'false'
`$env:SPRING_SQL_INIT_MODE = 'always'
`$env:MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS = 'always'
`$env:PROFESSOR_SHADOW_BASE_URL = 'http://localhost:$resolvedShadowPort'
`$env:PROFESSOR_INTERNAL_CLIENT_INTERNAL_TOKEN = '$InternalToken'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=professor-shadow-operational'
"@

    $shadowCommand = @"
Set-Location '$repoRoot'
`$env:SERVER_PORT = '$resolvedShadowPort'
`$env:MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS = 'always'
`$env:PROFESSOR_SHADOW_INTERNAL_API_TOKEN = '$InternalToken'
`$env:PROFESSOR_SHADOW_MONOLITH_BASE_URL = 'http://localhost:$resolvedMonolithPort'
Set-Location '$repoRoot\academic-professor-service'
mvn spring-boot:run
"@

    $monolithProcess = Start-BackgroundPowerShell -Command $monolithCommand -StdoutPath $monolithStdout -StderrPath $monolithStderr
    Wait-Healthy -Url "http://localhost:$resolvedMonolithPort/actuator/health" | Out-Null

    $shadowProcess = Start-BackgroundPowerShell -Command $shadowCommand -StdoutPath $shadowStdout -StderrPath $shadowStderr
    Wait-Healthy -Url "http://localhost:$resolvedShadowPort/actuator/health" | Out-Null

    $login = Invoke-Json -Method Post -Url "http://localhost:$resolvedMonolithPort/api/auth/login" -Body @{
        login = "professor-shadow-smoke"
        senha = "senha123"
    }

    $accessToken = $login.accessToken
    if ([string]::IsNullOrWhiteSpace($accessToken)) {
        throw "Login do smoke nao retornou accessToken."
    }

    $authHeaders = @{
        Authorization = "Bearer $accessToken"
        "X-Correlation-Id" = "professor-shadow-operational-smoke"
    }

    $contexto = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/auth/contexto-atual" -Headers $authHeaders
    $escolaId = $contexto.escolaId
    $usuarioId = $contexto.usuarioId
    $authHeaders["X-Usuario-Id"] = "$usuarioId"

    $professoresMonolith = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/professores" -Headers $authHeaders
    if (@($professoresMonolith).Count -lt 1) {
        throw "O monolito nao retornou professores no smoke operacional."
    }

    $professorId = @($professoresMonolith)[0].id
    $professorMonolith = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/professores/$professorId" -Headers $authHeaders
    $alocacoesMonolith = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/professores/$professorId/turmas-disciplinas" -Headers $authHeaders
    if (@($alocacoesMonolith).Count -lt 1) {
        throw "O monolito nao retornou alocacoes do professor no smoke operacional."
    }
    $funcionariosElegiveisMonolith = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/professores/funcionarios-elegiveis" -Headers $authHeaders
    if (@($funcionariosElegiveisMonolith).Count -lt 1) {
        throw "O monolito nao retornou funcionarios elegiveis no smoke operacional."
    }
    $turmaId = "30000000-0000-0000-0000-000000000010"
    $professoresPorTurmaMonolith = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/api/turmas/$turmaId/professores" -Headers $authHeaders
    if (@($professoresPorTurmaMonolith).Count -lt 1) {
        throw "O monolito nao retornou professores por turma no smoke operacional."
    }

    $shadowHeaders = @{
        Authorization = "Bearer $accessToken"
        "X-Internal-Token" = $InternalToken
        "X-Correlation-Id" = "professor-shadow-operational-smoke"
        "X-Usuario-Id" = "$usuarioId"
        "X-Escola-Id" = "$escolaId"
    }

    $professoresShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores" -Headers $shadowHeaders
    $professorShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/$professorId" -Headers $shadowHeaders
    $alocacoesShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/$professorId/turmas-disciplinas" -Headers $shadowHeaders
    if (@($alocacoesShadow).Count -lt 1) {
        throw "O shadow nao retornou alocacoes do professor no smoke operacional."
    }
    $funcionariosElegiveisShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/funcionarios-elegiveis" -Headers $shadowHeaders
    if (@($funcionariosElegiveisShadow).Count -lt 1) {
        throw "O shadow nao retornou funcionarios elegiveis no smoke operacional."
    }
    $professoresPorTurmaShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/turmas/$turmaId/professores" -Headers $shadowHeaders
    if (@($professoresPorTurmaShadow).Count -lt 1) {
        throw "O shadow nao retornou professores por turma no smoke operacional."
    }
    $funcionarioElegivelId = @($funcionariosElegiveisMonolith)[0].funcionarioId
    $professorCriadoMonolith = Invoke-Json -Method Post -Url "http://localhost:$resolvedMonolithPort/api/professores" -Headers $authHeaders -Body @{
        funcionarioId = $funcionarioElegivelId
        registroProfissional = "RP-SHADOW-WRITE"
        formacao = "Licenciatura em Matematica"
        ativo = $true
    } -ExpectedStatus 201
    $professorCriadoId = $professorCriadoMonolith.id
    if ([string]::IsNullOrWhiteSpace($professorCriadoId)) {
        throw "A criacao de professor via shadow nao retornou id no smoke operacional."
    }
    $seedTurmaDisciplinaId = "30000000-0000-0000-0000-000000000012"
    $alocacaoCriadaMonolith = Invoke-Json -Method Post -Url "http://localhost:$resolvedMonolithPort/api/professores/$professorCriadoId/turmas-disciplinas" -Headers $authHeaders -Body @{
        turmaDisciplinaId = $seedTurmaDisciplinaId
        dataInicio = "2041-02-01"
        ativo = $true
    } -ExpectedStatus 201
    $alocacoesProfessorCriadoShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/$professorCriadoId/turmas-disciplinas" -Headers $shadowHeaders
    if (@($alocacoesProfessorCriadoShadow).Count -lt 1) {
        throw "O shadow nao retornou alocacoes do professor criado no smoke operacional."
    }
    $professorCriadoShadow = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/$professorCriadoId" -Headers $shadowHeaders
    $notFoundId = [guid]::NewGuid()
    Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/internal/v1/professores/$notFoundId" -Headers $shadowHeaders -ExpectedStatus 404 | Out-Null

    $monolithHealth = Invoke-Json -Method Get -Url "http://localhost:$resolvedMonolithPort/actuator/health/professorInternalClient"
    $shadowHealth = Invoke-Json -Method Get -Url "http://localhost:$resolvedShadowPort/actuator/health/professorShadowMonolith"

    $report = [ordered]@{
        generatedAt = (Get-Date).ToString("o")
        ports = @{
            schoolManagementService = $resolvedMonolithPort
            academicProfessorService = $resolvedShadowPort
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
            turmaId = $turmaId
            monolithProfessorNome = $professorMonolith.nomeCompleto
            shadowProfessorNome = $professorShadow.nomeCompleto
            createdProfessorId = $professorCriadoId
            createdProfessorNome = $professorCriadoMonolith.nomeCompleto
            createdProfessorShadowNome = $professorCriadoShadow.nomeCompleto
            createdProfessorAlocacaoId = $alocacaoCriadaMonolith.id
            createdProfessorAlocacaoCount = @($alocacoesProfessorCriadoShadow).Count
            monolithAlocacaoCount = @($alocacoesMonolith).Count
            shadowAlocacaoCount = @($alocacoesShadow).Count
            monolithEligibleCount = @($funcionariosElegiveisMonolith).Count
            shadowEligibleCount = @($funcionariosElegiveisShadow).Count
            monolithTurmaProfessorCount = @($professoresPorTurmaMonolith).Count
            shadowTurmaProfessorCount = @($professoresPorTurmaShadow).Count
            turmaDisciplinaId = @($alocacoesMonolith)[0].turmaDisciplinaId
            elegivelFuncionarioId = $funcionarioElegivelId
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
