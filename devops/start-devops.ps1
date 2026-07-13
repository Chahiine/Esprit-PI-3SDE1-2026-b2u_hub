# Demarre Jenkins + SonarQube (Windows + Docker Desktop)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== Demarrage Jenkins + SonarQube + Prometheus + Grafana ===" -ForegroundColor Cyan
Write-Host "Astuce : stack complete -> .\start-full-stack.ps1" -ForegroundColor DarkGray
docker compose -f docker-compose.devops.yml up -d --build

Write-Host ""
Write-Host "Attente SonarQube (60s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host ""
Write-Host "URLs :" -ForegroundColor Green
Write-Host "  Jenkins    : http://localhost:8080"
Write-Host "  SonarQube  : http://localhost:9000  (admin / votre mot de passe)"
Write-Host "  Prometheus : http://localhost:9090"
Write-Host "  Grafana    : http://localhost:3000  (admin / admin)"
Write-Host "  Dashboard  : B2U-HUB > B2U-HUB Backend Monitoring"
Write-Host ""
Write-Host "Metriques backend : http://localhost:8081/actuator/prometheus"
Write-Host "  (Lancez le backend IntelliJ ou Docker 8082 avant la demo monitoring)"
Write-Host ""
Write-Host "Mot de passe initial Jenkins :" -ForegroundColor Yellow
docker exec b2u-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
Write-Host ""
Write-Host "Verifier Docker dans Jenkins :" -ForegroundColor Yellow
$dockerVer = docker exec b2u-jenkins docker --version 2>&1
Write-Host $dockerVer
if ($dockerVer -match "not found") {
    Write-Host "Docker manquant -> lancez : .\rebuild-jenkins.ps1" -ForegroundColor Red
}
Write-Host ""
Write-Host "Pipeline a coller : devops/jenkins-pipeline.groovy"
