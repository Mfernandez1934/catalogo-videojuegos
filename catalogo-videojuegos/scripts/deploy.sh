#!/usr/bin/env bash
# Deploy al ambiente simulado de produccion (carpeta local) - Linux / macOS
# Uso: bash scripts/deploy.sh [CARPETA_PROD] [PUERTO]
set -euo pipefail

PROD_DIR="${1:-$HOME/prod/catalogo}"
PORT="${2:-8081}"
JAR_ORIGEN="web/target/catalogo-web.jar"
PID_FILE="$PROD_DIR/app.pid"

if [ ! -f "$JAR_ORIGEN" ]; then
  echo "No se encontro $JAR_ORIGEN. Ejecutar antes: mvn package" >&2
  exit 1
fi

mkdir -p "$PROD_DIR"

# 1. Detener la version anterior (si esta corriendo)
if [ -f "$PID_FILE" ]; then
  PID_ANTERIOR="$(cat "$PID_FILE")"
  if kill -0 "$PID_ANTERIOR" 2>/dev/null; then
    echo "Deteniendo version anterior (PID $PID_ANTERIOR)..."
    kill "$PID_ANTERIOR" || true
    for _ in $(seq 1 30); do
      kill -0 "$PID_ANTERIOR" 2>/dev/null || break
      sleep 1
    done
    kill -9 "$PID_ANTERIOR" 2>/dev/null || true
  fi
  rm -f "$PID_FILE"
fi

# 2. Copiar el nuevo jar a la carpeta de produccion
cp "$JAR_ORIGEN" "$PROD_DIR/catalogo-web.jar"

# 3. Iniciar la nueva version en segundo plano
cd "$PROD_DIR"
nohup java -jar catalogo-web.jar --server.port="$PORT" > app.log 2>&1 &
echo $! > "$PID_FILE"

echo "Version desplegada en $PROD_DIR (PID $(cat "$PID_FILE"), puerto $PORT)"
