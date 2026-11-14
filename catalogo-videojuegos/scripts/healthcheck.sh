#!/usr/bin/env bash
# Espera hasta 90 s a que la app responda UP en /actuator/health
# Uso: bash scripts/healthcheck.sh [PUERTO]
set -u

PORT="${1:-8081}"
URL="http://localhost:$PORT/actuator/health"

for i in $(seq 1 45); do
  if curl -fs "$URL" | grep -q '"status":"UP"'; then
    echo "Health check OK ($URL) tras $((i * 2)) s"
    exit 0
  fi
  sleep 2
done

echo "Health check FALLO: la app no respondio en $URL" >&2
exit 1
