#!/bin/bash
#
# PetPro 앱 재시작 스크립트 (Release JAR + nginx 정적 서빙)
#
# 사용법:
#   ./scripts/restart-app.sh           # 백엔드 + nginx 모두 재시작
#   ./scripts/restart-app.sh backend   # 백엔드만 재시작
#   ./scripts/restart-app.sh frontend  # nginx만 재시작
#   ./scripts/restart-app.sh stop      # 백엔드 중지
#   ./scripts/restart-app.sh status    # 상태 확인
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# 색상
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# .env 로드
if [ -f .env ]; then
    set -a && source .env && set +a
fi

# Compose 명령어
COMPOSE_CMD="docker compose"
command -v docker &>/dev/null || COMPOSE_CMD="podman-compose"

stop_backend() {
    echo "[백엔드] 프로세스 종료 중..."
    pkill -f "java.*backend.jar" || true
    sleep 2
}

start_backend() {
    if [ ! -f backend.jar ]; then
        echo -e "${RED}[오류]${NC} backend.jar가 없습니다. ./deploy.sh를 먼저 실행하세요."
        exit 1
    fi

    echo "[백엔드] 시작 중..."
    mkdir -p logs
    nohup java -Xms128m -Xmx384m -jar backend.jar \
        --spring.profiles.active="${APP_ENV:-prod}" \
        > logs/backend.log 2>&1 &
    echo "[백엔드] PID: $!"

    echo "[백엔드] 헬스체크 대기 중..."
    for i in $(seq 1 90); do
        if curl -sf http://localhost:${APP_PORT:-8080}/api/health > /dev/null 2>&1; then
            echo -e "${GREEN}[백엔드] 헬스체크 통과!${NC}"
            return
        fi
        sleep 2
    done
    echo -e "${YELLOW}[경고] 헬스체크 타임아웃${NC}"
}

restart_nginx() {
    echo "[nginx] 재시작 중..."
    $COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml restart nginx
    echo -e "${GREEN}[nginx] 완료!${NC}"
}

check_status() {
    echo ""
    echo "=== 서비스 상태 ==="

    if pgrep -f "java.*backend.jar" > /dev/null 2>&1; then
        echo -e "백엔드 (8080):  ${GREEN}실행 중${NC}"
    else
        echo -e "백엔드 (8080):  ${RED}중지됨${NC}"
    fi

    if docker ps --format '{{.Names}}' 2>/dev/null | grep -q petpro-nginx; then
        echo -e "nginx (80/443): ${GREEN}실행 중${NC}"
    else
        echo -e "nginx (80/443): ${RED}중지됨${NC}"
    fi
    echo ""
}

show_help() {
    echo "사용법: $0 [command]"
    echo ""
    echo "  all       백엔드 + nginx 모두 재시작 (기본값)"
    echo "  backend   백엔드만 재시작"
    echo "  frontend  nginx만 재시작 (프론트엔드 반영)"
    echo "  stop      백엔드 중지"
    echo "  status    상태 확인"
}

case "${1:-all}" in
    all)
        stop_backend
        start_backend
        restart_nginx
        check_status
        ;;
    backend)
        stop_backend
        start_backend
        ;;
    frontend)
        restart_nginx
        ;;
    stop)
        stop_backend
        echo -e "${GREEN}[완료] 백엔드 중지됨${NC}"
        ;;
    status)
        check_status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "알 수 없는 명령: $1"
        show_help
        exit 1
        ;;
esac
