$ErrorActionPreference = "Stop"
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    docker compose up -d --wait postgres
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL startup failed." }
    Write-Host "PostgreSQL ready. Run ResolveLogApplication in IntelliJ."
} finally { Pop-Location }

