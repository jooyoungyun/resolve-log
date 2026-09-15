#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
trap 'docker compose --profile test stop postgres-test' EXIT
docker compose --profile test up -d --wait postgres-test
TEST_DATABASE_URL=jdbc:postgresql://localhost:5434/resolve_log_test \
TEST_DATABASE_USER=resolve_log_test TEST_DATABASE_PASSWORD=resolve_log_test \
./backend/mvnw -f backend/pom.xml verify

