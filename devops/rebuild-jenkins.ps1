# Reconstruit Jenkins avec le client Docker (corrige "docker: not found")
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== Rebuild Jenkins avec Docker CLI ===" -ForegroundColor Cyan
docker compose -f docker-compose.devops.yml build --no-cache jenkins
docker compose -f docker-compose.devops.yml up -d jenkins

Write-Host ""
Write-Host "Verification :" -ForegroundColor Yellow
docker exec b2u-jenkins docker --version
docker exec b2u-jenkins docker ps

Write-Host ""
Write-Host "Si OK, relancez Build Now dans Jenkins." -ForegroundColor Green
