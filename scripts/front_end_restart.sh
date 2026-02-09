#!/bin/bash
#
# 프론트엔드 재시작 스크립트 (nginx 정적 파일 서빙 방식)
# 사용법: ./scripts/front_end_restart.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "=========================================="
echo "  PetPro 프론트엔드 재시작 (nginx)"
echo "=========================================="

# Compose 명령어 감지
COMPOSE_CMD="docker compose"
if ! command -v docker &> /dev/null; then
    if command -v podman-compose &> /dev/null; then
        COMPOSE_CMD="podman-compose"
    else
        echo "[오류] docker 또는 podman-compose가 필요합니다."
        exit 1
    fi
fi

# frontend/build 확인
if [ ! -d frontend/build ]; then
    echo "[오류] frontend/build가 없습니다. ./deploy.sh를 먼저 실행하세요."
    exit 1
fi

# nginx 재시작
echo "[nginx] 재시작 중..."
$COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml restart nginx

echo "[nginx] 완료!"
echo ""
echo "  프론트엔드: https://dev.findplace.co.kr"
