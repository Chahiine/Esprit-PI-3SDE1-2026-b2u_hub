$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Arret de la stack B2U-HUB..." -ForegroundColor Cyan
docker compose -f docker-compose.full.yml down
Write-Host "Termine." -ForegroundColor Green
