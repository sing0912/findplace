#!/usr/bin/env bash
# 백엔드만 재시작

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

set -a && source .env && set +a

echo "[백엔드] 기존 프로세스 종료 중..."
pkill -f "java.*backend.jar" || true
sleep 2

echo "[백엔드] 시작 중..."
mkdir -p logs
nohup java -Xms128m -Xmx384m -jar backend.jar \
    --spring.profiles.active="${APP_ENV:-prod}" \
    > logs/backend.log 2>&1 &

echo "[백엔드] PID: $!"
echo "[백엔드] 헬스체크 대기 중..."

for i in $(seq 1 90); do
    if curl -sf http://localhost:${APP_PORT:-8080}/api/health > /dev/null 2>&1; then
        echo "[백엔드] 헬스체크 통과!"
        exit 0
    fi
    sleep 2
done

echo "[백엔드] 헬스체크 타임아웃. logs/backend.log 확인하세요."
