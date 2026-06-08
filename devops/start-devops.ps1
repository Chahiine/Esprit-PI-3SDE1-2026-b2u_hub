# Demarre SonarQube + Jenkins (Windows + Docker Desktop)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Write-Host "=== Demarrage stack DevOps (Jenkins + SonarQube) ===" -ForegroundColor Cyan
Set-Location $PSScriptRoot

docker compose -f docker-compose.devops.yml up -d --build

Write-Host ""
Write-Host "Attente demarrage SonarQube (60s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host ""
Write-Host "URLs :" -ForegroundColor Green
Write-Host "  Jenkins   : http://localhost:8080"
Write-Host "  SonarQube : http://localhost:9000  (admin / admin)"
Write-Host ""
Write-Host "Mot de passe initial Jenkins :" -ForegroundColor Yellow
docker exec b2u-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
Write-Host ""
Write-Host "Voir le guide : devops/DEVOPS-PIPELINE.md"
