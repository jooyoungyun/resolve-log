#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
docker compose up -d --build --wait --wait-timeout 240
printf '%s\n' 'Resolve Log: http://localhost:3300 (default FRONTEND_PORT)'
docker compose ps

