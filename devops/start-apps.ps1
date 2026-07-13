$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== B2U-HUB — Application Docker seule ===" -ForegroundColor Cyan
docker compose -f docker-compose.apps.yml up -d --build

Write-Host ""
Write-Host "Frontend : http://localhost:4201" -ForegroundColor Green
Write-Host "Backend  : http://localhost:8082" -ForegroundColor Green
Write-Host "Postgres : localhost:5433" -ForegroundColor Green
