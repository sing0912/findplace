#!/bin/bash
#
# 로컬 개발 환경 백엔드 재시작 스크립트 (Gradle bootRun 방식)
# 사용법: ./scripts/local-backend-restart.sh
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

PORT="${APP_PORT:-8080}"

echo "=========================================="
echo "  PetPro 백엔드 재시작 (로컬 개발)"
echo "=========================================="

# .env 로드
if [ ! -f .env ]; then
    echo "[오류] .env 파일이 없습니다."
    exit 1
fi
set -a && source .env && set +a

# 기존 프로세스 종료
echo "[1/3] 기존 프로세스 종료 중..."
PID=$(lsof -ti:"$PORT" 2>/dev/null)
if [ -n "$PID" ]; then
    kill "$PID" 2>/dev/null
    sleep 2
    # 아직 살아있으면 강제 종료
    if lsof -ti:"$PORT" > /dev/null 2>&1; then
        kill -9 "$(lsof -ti:"$PORT")" 2>/dev/null
        sleep 1
    fi
    echo "  포트 $PORT 해제 완료 (PID: $PID)"
else
    echo "  포트 $PORT 사용 중인 프로세스 없음"
fi

# Gradle bootRun 실행
echo "[2/3] Gradle bootRun 시작 중..."
mkdir -p "$PROJECT_ROOT/logs"
cd "$PROJECT_ROOT/backend"
nohup ./gradlew bootRun > "$PROJECT_ROOT/logs/local-backend.log" 2>&1 &
BOOT_PID=$!
echo "  PID: $BOOT_PID"

# 헬스체크
echo "[3/3] 헬스체크 대기 중 (최대 2분)..."
for i in $(seq 1 60); do
    if curl -sf "http://localhost:$PORT/health" > /dev/null 2>&1; then
        echo ""
        echo "=========================================="
        echo "  백엔드 시작 완료!"
        echo "  URL:     http://localhost:$PORT"
        echo "  Swagger: http://localhost:$PORT/swagger-ui.html"
        echo "  로그:    tail -f logs/local-backend.log"
        echo "=========================================="
        exit 0
    fi
    printf "."
    sleep 2
done

echo ""
echo "[경고] 헬스체크 타임아웃. 로그를 확인하세요:"
echo "  tail -f $PROJECT_ROOT/logs/local-backend.log"
exit 1
