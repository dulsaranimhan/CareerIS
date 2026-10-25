#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_PID=""
FRONTEND_PID=""

stop_servers() {
  if [[ -n "$BACKEND_PID" ]]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
  if [[ -n "$FRONTEND_PID" ]]; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
}

trap stop_servers EXIT INT TERM

echo "Starting CareerIS backend at http://localhost:8080 ..."
(cd "$ROOT_DIR/backend" && mvn spring-boot:run) &
BACKEND_PID=$!

echo "Starting CareerIS frontend at http://localhost:5173 ..."
(cd "$ROOT_DIR/frontend" && npm run dev -- --host 127.0.0.1) &
FRONTEND_PID=$!

echo "Both servers are starting. Press Ctrl+C to stop them."
wait "$BACKEND_PID" "$FRONTEND_PID"
