$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Building all modules..."
mvn clean install -f "$ScriptDir\pom.xml"

Write-Host "Building Docker images..."
docker build -t magnus/detection "$ScriptDir\detection-and-refactoring"
docker build -t magnus/manager "$ScriptDir\project-sync-bff"
docker build -t magnus/metrics "$ScriptDir\metrics-calculator"

Write-Host "Starting Docker services..."
docker compose -f "$ScriptDir\infra\local\docker-compose-full.yml" up -d

Write-Host "Applying Terraform configuration..."
tflocal -chdir="$ScriptDir\infra" apply -auto-approve
