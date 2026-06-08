# Demarre Jenkins + SonarQube (Windows + Docker Desktop)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== Demarrage Jenkins + SonarQube ===" -ForegroundColor Cyan
docker compose -f docker-compose.devops.yml up -d --build

Write-Host ""
Write-Host "Attente SonarQube (60s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host ""
Write-Host "URLs :" -ForegroundColor Green
Write-Host "  Jenkins   : http://localhost:8080"
Write-Host "  SonarQube : http://localhost:9000  (admin / votre mot de passe)"
Write-Host ""
Write-Host "Mot de passe initial Jenkins :" -ForegroundColor Yellow
docker exec b2u-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
Write-Host ""
Write-Host "Verifier Docker dans Jenkins :" -ForegroundColor Yellow
docker exec b2u-jenkins docker --version 2>$null
Write-Host ""
Write-Host "Pipeline a coller : devops/jenkins-pipeline.groovy"
