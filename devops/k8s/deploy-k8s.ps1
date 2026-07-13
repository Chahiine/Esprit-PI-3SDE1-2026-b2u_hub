$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=== Deploiement Kubernetes (Docker Desktop) ===" -ForegroundColor Cyan

Write-Host "Build images Docker..." -ForegroundColor Yellow
docker build -f ../Dockerfile.backend -t b2u-hub-backend:latest ..
docker build -f ../frontend/Dockerfile -t b2u-hub-frontend:latest ../frontend

Write-Host "Application des manifests..." -ForegroundColor Yellow
kubectl apply -f namespace.yaml
kubectl apply -f backend-configmap.yaml
kubectl apply -f postgres.yaml
kubectl apply -f backend.yaml
kubectl apply -f frontend.yaml

Write-Host ""
Write-Host "Attente des pods (60s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60
kubectl get pods -n b2u-hub

Write-Host ""
Write-Host "URLs (Docker Desktop Kubernetes) :" -ForegroundColor Green
Write-Host "  Frontend : http://localhost:30421"
Write-Host "  Backend  : http://localhost:30082/api/missions"
Write-Host ""
Write-Host "Suppression : kubectl delete namespace b2u-hub" -ForegroundColor Yellow
