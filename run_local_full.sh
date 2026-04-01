#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Starting Docker services..."
docker compose -f "$SCRIPT_DIR/infra/local/docker-compose-full.yml" up -d

echo "Applying Terraform configuration..."
tflocal -chdir="$SCRIPT_DIR/infra" apply -auto-approve
