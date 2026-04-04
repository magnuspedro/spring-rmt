#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

require_docker_compose() {
  if ! docker compose version >/dev/null 2>&1; then
    echo "Missing required Docker Compose plugin (docker compose)."
    exit 1
  fi
}

wait_for_localstack() {
  require_cmd curl
  local container="${LOCALSTACK_DOCKER_NAME-localstack_cloud}"
  echo "==> Waiting for LocalStack to be healthy..."

  local max_attempts=60
  local attempt=0

  while [ $attempt -lt $max_attempts ]; do
    if docker inspect "$container" --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
      echo "==> LocalStack is healthy"
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 1
  done

  echo "ERROR: LocalStack did not become healthy in time"
  return 1
}

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
  require_cmd mvn
  echo "==> Building all modules..."
  mvn -B -ntp clean package -DskipTests -f "$SCRIPT_DIR/pom.xml"
}

images() {
  require_cmd docker
  echo "==> Building Docker images..."
  docker build -t magnus/detection "$SCRIPT_DIR/detection-and-refactoring"
  docker build -t magnus/manager   "$SCRIPT_DIR/project-sync-bff"
  docker build -t magnus/metrics   "$SCRIPT_DIR/metrics-calculator"
}

infra() {
  require_cmd docker
  require_docker_compose
  require_cmd tflocal
  echo "==> Starting infrastructure..."
  docker compose -f "$SCRIPT_DIR/infra/local/docker-compose.yml" up -d

  wait_for_localstack

  echo "==> Provisioning AWS resources..."
  tflocal -chdir="$SCRIPT_DIR/infra" apply -auto-approve
}

infra_full() {
  require_cmd docker
  require_docker_compose
  require_cmd tflocal
  echo "==> Starting full environment..."
  docker compose -f "$SCRIPT_DIR/infra/local/docker-compose-full.yml" up -d

  wait_for_localstack

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
