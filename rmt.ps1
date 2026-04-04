param(
    [ValidateSet("build", "images", "infra", "dev", "all", "help")]
    [string]$Command = "all"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Test-Command {
    param([string]$Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Assert-Command {
    param([string]$Name)
    if (-not (Test-Command $Name)) {
        throw "Missing required command: $Name"
    }
}

function Assert-DockerCompose {
    Assert-Command "docker"
    docker compose version *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "Missing required Docker Compose plugin (docker compose)."
    }
}

function Wait-LocalStack {
    Assert-Command "curl"
    $container = if ($env:LOCALSTACK_DOCKER_NAME) { $env:LOCALSTACK_DOCKER_NAME } else { "localstack_cloud" }
    Write-Host "==> Waiting for LocalStack to be healthy..."

    $maxAttempts = 60
    $attempt = 0

    while ($attempt -lt $maxAttempts) {
        $healthStatus = docker inspect $container --format='{{.State.Health.Status}}' 2>$null
        if ($healthStatus -eq "healthy") {
            Write-Host "==> LocalStack is healthy"
            return
        }
        $attempt++
        Start-Sleep -Seconds 1
    }

    throw "LocalStack did not become healthy in time"
}

function Show-Usage {
    Write-Host @"
Usage: .\rmt.ps1 [command]

Commands:
  build     Build all Maven modules
  images    Build Docker images for all services
  infra     Start infrastructure (LocalStack + Redis) and provision AWS resources via Terraform
  dev       build + infra  (run services locally with mvn spring-boot:run)
  all       build + images + infra  (full Docker environment)  [default]

Examples:
  .\rmt.ps1           # same as .\rmt.ps1 all
  .\rmt.ps1 dev       # build and start infra, then run services manually
  .\rmt.ps1 build     # only compile and install Maven modules
"@
}

function Invoke-Build {
    Assert-Command "mvn"
    Write-Host "==> Building all modules..."
    mvn -B -ntp clean package -DskipTests -f "$ScriptDir\pom.xml"
}

function Invoke-Images {
    Assert-Command "docker"
    Write-Host "==> Building Docker images..."
    docker build -t magnus/detection "$ScriptDir\detection-and-refactoring"
    docker build -t magnus/manager   "$ScriptDir\project-sync-bff"
    docker build -t magnus/metrics   "$ScriptDir\metrics-calculator"
}

function Invoke-Infra {
    Assert-DockerCompose
    Assert-Command "tflocal"
    Write-Host "==> Starting infrastructure..."
    docker compose -f "$ScriptDir\infra\local\docker-compose.yml" up -d

    Wait-LocalStack

    Write-Host "==> Provisioning AWS resources..."
    tflocal -chdir="$ScriptDir\infra" apply -auto-approve
}

function Invoke-InfraFull {
    Assert-DockerCompose
    Assert-Command "tflocal"
    Write-Host "==> Starting full environment..."
    docker compose -f "$ScriptDir\infra\local\docker-compose-full.yml" up -d

    Wait-LocalStack

    Write-Host "==> Provisioning AWS resources..."
    tflocal -chdir="$ScriptDir\infra" apply -auto-approve
}

switch ($Command) {
    "build" {
        Invoke-Build
    }
    "images" {
        Invoke-Images
    }
    "infra" {
        Invoke-Infra
    }
    "dev" {
        Invoke-Build
        Invoke-Infra
        Write-Host ""
        Write-Host "Infrastructure is up. Start each service with:"
        Write-Host "  mvn spring-boot:run -pl project-sync-bff"
        Write-Host "  mvn spring-boot:run -pl detection-and-refactoring"
        Write-Host "  mvn spring-boot:run -pl metrics-calculator"
    }
    "all" {
        Invoke-Build
        Invoke-Images
        Invoke-InfraFull
        Write-Host ""
        Write-Host "Done. UI available at http://localhost:8080"
    }
    "help" {
        Show-Usage
    }
}
