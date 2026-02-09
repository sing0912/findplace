#!/bin/bash
#
# 백엔드 재시작 스크립트 (Release JAR 실행 방식)
# 사용법: ./scripts/back_end_restart.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# .env 로드
if [ ! -f .env ]; then
    echo "[오류] .env 파일이 없습니다."
    exit 1
fi
set -a && source .env && set +a

echo "=========================================="
echo "  PetPro 백엔드 재시작"
echo "=========================================="

# 기존 프로세스 종료
echo "[백엔드] 기존 프로세스 종료 중..."
pkill -f "java.*backend.jar" || true
sleep 2

# JAR 파일 확인
if [ ! -f backend.jar ]; then
    echo "[오류] backend.jar가 없습니다. ./deploy.sh를 먼저 실행하세요."
    exit 1
fi

# 백엔드 시작
echo "[백엔드] 시작 중..."
mkdir -p logs
nohup java -Xms128m -Xmx384m -jar backend.jar \
    --spring.profiles.active="${APP_ENV:-prod}" \
    > logs/backend.log 2>&1 &

echo "[백엔드] PID: $!"

# 헬스체크
echo "[백엔드] 헬스체크 대기 중 (최대 3분)..."
for i in $(seq 1 90); do
    if curl -sf http://localhost:${APP_PORT:-8080}/api/health > /dev/null 2>&1; then
        echo "[백엔드] 헬스체크 통과!"
        echo ""
        echo "  URL: http://localhost:${APP_PORT:-8080}"
        echo "  로그: tail -f $PROJECT_ROOT/logs/backend.log"
        exit 0
    fi
    sleep 2
done

echo "[경고] 헬스체크 타임아웃. logs/backend.log를 확인하세요."
