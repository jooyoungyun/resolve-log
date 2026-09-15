$ErrorActionPreference = "Stop"
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    docker compose up -d --build --wait --wait-timeout 240
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose startup failed. Run: docker compose logs --tail 100" }
    Write-Host "Resolve Log: http://localhost:3300 (default FRONTEND_PORT)"
    docker compose ps
} finally { Pop-Location }

