#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<EOF
Usage: ./rmt.sh [command]

Commands:
  build     Build all Maven modules
  images    Build Docker images for all services
  infra     Start infrastructure (LocalStack + Redis) and provision AWS resources via Terraform
  dev       build + infra  (run services locally with mvn spring-boot:run)
  all       build + images + infra  (full Docker environment)  [default]

Examples:
  ./rmt.sh           # same as ./rmt.sh all
  ./rmt.sh dev       # build and start infra, then run services manually
  ./rmt.sh build     # only compile and install Maven modules
EOF
}

build() {
  echo "==> Building all modules..."
  mvn clean install -f "$SCRIPT_DIR/pom.xml"
}

images() {
  echo "==> Building Docker images..."
  docker build -t magnus/detection "$SCRIPT_DIR/detection-and-refactoring"
  docker build -t magnus/manager   "$SCRIPT_DIR/project-sync-bff"
  docker build -t magnus/metrics   "$SCRIPT_DIR/metrics-calculator"
}

infra() {
  echo "==> Starting infrastructure..."
  docker compose -f "$SCRIPT_DIR/infra/local/docker-compose.yml" up -d

  echo "==> Provisioning AWS resources..."
  tflocal -chdir="$SCRIPT_DIR/infra" apply -auto-approve
}

infra_full() {
  echo "==> Starting full environment..."
  docker compose -f "$SCRIPT_DIR/infra/local/docker-compose-full.yml" up -d

  echo "==> Provisioning AWS resources..."
  tflocal -chdir="$SCRIPT_DIR/infra" apply -auto-approve
}

case "${1:-all}" in
  build)
    build
    ;;
  images)
    images
    ;;
  infra)
    infra
    ;;
  dev)
    build
    infra
    echo ""
    echo "Infrastructure is up. Start each service with:"
    echo "  mvn spring-boot:run -pl project-sync-bff"
    echo "  mvn spring-boot:run -pl detection-and-refactoring"
    echo "  mvn spring-boot:run -pl metrics-calculator"
    ;;
  all)
    build
    images
    infra_full
    echo ""
    echo "Done. UI available at http://localhost:8080"
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    echo "Unknown command: ${1}"
    echo ""
    usage
    exit 1
    ;;
esac
