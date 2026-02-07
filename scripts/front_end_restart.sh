#!/bin/bash
#
# 프론트엔드 재시작 스크립트
# 사용법: ./scripts/front_end_restart.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

echo "🔄 프론트엔드 재시작 중..."

# 기존 프로세스 종료
echo "📍 기존 프로세스 확인..."
PIDS=$(lsof -t -i:3000 2>/dev/null || true)
if [ -n "$PIDS" ]; then
    echo "🛑 포트 3000 프로세스 종료: $PIDS"
    kill -9 $PIDS 2>/dev/null || true
    sleep 2
fi

# 프론트엔드 시작
cd "$FRONTEND_DIR"

echo "📦 프론트엔드 시작 (http://localhost:3000)..."
npm start &

echo ""
echo "✅ 프론트엔드 재시작 완료!"
echo "   URL: http://localhost:3000"
echo ""
