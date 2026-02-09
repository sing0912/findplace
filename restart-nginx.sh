#!/usr/bin/env bash
# nginx만 재시작 (프론트엔드 정적 파일 반영)

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

COMPOSE_CMD="docker compose"
command -v docker &>/dev/null || COMPOSE_CMD="podman-compose"

echo "[nginx] 재시작 중..."
$COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml restart nginx
echo "[nginx] 완료!"
