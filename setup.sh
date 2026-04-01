#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Building all modules..."
mvn clean install -f "$SCRIPT_DIR/pom.xml"

echo "Building Docker images..."
docker build -t magnus/detection "$SCRIPT_DIR/detection-and-refactoring"
docker build -t magnus/manager "$SCRIPT_DIR/project-sync-bff"
docker build -t magnus/metrics "$SCRIPT_DIR/metrics-calculator"

echo "Done. Run ./run_local_full.sh to start the local environment."
