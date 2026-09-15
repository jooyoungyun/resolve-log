$ErrorActionPreference = "Stop"
$oldUrl = $env:TEST_DATABASE_URL
$oldUser = $env:TEST_DATABASE_USER
$oldPassword = $env:TEST_DATABASE_PASSWORD
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    docker compose --profile test up -d --wait postgres-test
    if ($LASTEXITCODE -ne 0) { throw "Test PostgreSQL startup failed." }
    $env:TEST_DATABASE_URL = "jdbc:postgresql://localhost:5434/resolve_log_test"
    $env:TEST_DATABASE_USER = "resolve_log_test"
    $env:TEST_DATABASE_PASSWORD = "resolve_log_test"
    & .\backend\mvnw.cmd -f .\backend\pom.xml verify
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL integration tests failed." }
} finally {
    docker compose --profile test stop postgres-test
    $env:TEST_DATABASE_URL = $oldUrl
    $env:TEST_DATABASE_USER = $oldUser
    $env:TEST_DATABASE_PASSWORD = $oldPassword
    Pop-Location
}

