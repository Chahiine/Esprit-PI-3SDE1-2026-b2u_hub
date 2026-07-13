# Demarre toute la stack B2U-HUB sur Docker Desktop
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== B2U-HUB — Stack complete Docker Desktop ===" -ForegroundColor Cyan
Write-Host "Build des images + demarrage (peut prendre 5-15 min la 1ere fois)..." -ForegroundColor Yellow

docker compose -f docker-compose.full.yml up -d --build

Write-Host ""
Write-Host "Attente demarrage (90s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 90

Write-Host ""
Write-Host "=== APPLICATION ===" -ForegroundColor Green
Write-Host "  Frontend Docker : http://localhost:4201"
Write-Host "  Missions        : http://localhost:4201/missions/consulter"
Write-Host "  IA Matching     : http://localhost:4201/missions/etudiant/matching"
Write-Host "  Backend API     : http://localhost:8082/api/missions"
Write-Host "  PostgreSQL app  : localhost:5433 (user postgres / 0000)"
Write-Host ""
Write-Host "=== DEVOPS ===" -ForegroundColor Green
Write-Host "  Jenkins         : http://localhost:8080"
Write-Host "  SonarQube       : http://localhost:9000"
Write-Host "  Prometheus      : http://localhost:9090"
Write-Host "  Grafana         : http://localhost:3000  (admin / admin)"
Write-Host ""
Write-Host "=== MONITORING ===" -ForegroundColor Green
Write-Host "  Metriques       : http://localhost:8082/actuator/prometheus"
Write-Host "  Dashboard       : Grafana > B2U-HUB Backend Monitoring"
Write-Host ""
Write-Host "Arret : .\stop-full-stack.ps1" -ForegroundColor Yellow
