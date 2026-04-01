#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Starting Docker services..."
docker compose -f "$SCRIPT_DIR/local/docker-compose.yml" up -d

echo "Applying Terraform configuration..."
tflocal -chdir="$SCRIPT_DIR" apply -auto-approve
