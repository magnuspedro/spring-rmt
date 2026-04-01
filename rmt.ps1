param(
    [ValidateSet("build", "images", "infra", "dev", "all", "help")]
    [string]$Command = "all"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

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
    Write-Host "==> Building all modules..."
    mvn clean install -f "$ScriptDir\pom.xml"
}

function Invoke-Images {
    Write-Host "==> Building Docker images..."
    docker build -t magnus/detection "$ScriptDir\detection-and-refactoring"
    docker build -t magnus/manager   "$ScriptDir\project-sync-bff"
    docker build -t magnus/metrics   "$ScriptDir\metrics-calculator"
}

function Invoke-Infra {
    Write-Host "==> Starting infrastructure..."
    docker compose -f "$ScriptDir\infra\local\docker-compose.yml" up -d

    Write-Host "==> Provisioning AWS resources..."
    tflocal -chdir="$ScriptDir\infra" apply -auto-approve
}

function Invoke-InfraFull {
    Write-Host "==> Starting full environment..."
    docker compose -f "$ScriptDir\infra\local\docker-compose-full.yml" up -d

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
