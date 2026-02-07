#!/bin/bash
# ============================================================
# PetPro 전체 서비스 시작 스크립트
# 인프라 + Backend + Frontend 동시 시작
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║           PetPro - 전체 서비스 시작                    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# 1. 인프라 시작
echo -e "${GREEN}[1/3]${NC} 인프라 시작 중..."
"$SCRIPT_DIR/run.sh" up

# 인프라 안정화 대기
echo -e "${GREEN}[INFO]${NC} 인프라 안정화 대기 (10초)..."
sleep 10

# 2. Backend 시작 (백그라운드)
echo -e "${GREEN}[2/3]${NC} Backend 시작 중..."
cd "$PROJECT_ROOT/backend"
./gradlew bootRun &
BACKEND_PID=$!

# Backend 시작 대기
echo -e "${GREEN}[INFO]${NC} Backend 시작 대기 (30초)..."
sleep 30

# 3. Frontend 시작 (백그라운드)
echo -e "${GREEN}[3/3]${NC} Frontend 시작 중..."
cd "$PROJECT_ROOT/frontend"
npm start &
FRONTEND_PID=$!

echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}모든 서비스가 시작되었습니다!${NC}"
echo ""
echo "서비스 URL:"
echo "  - Frontend:    http://localhost:3000"
echo "  - Backend API: http://localhost:8080/api"
echo "  - Swagger UI:  http://localhost:8080/api/swagger-ui.html"
echo "  - MinIO:       http://localhost:9001"
echo ""
echo "종료하려면 Ctrl+C를 누르세요."
echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"

# 종료 시 cleanup
cleanup() {
    echo ""
    echo "서비스 종료 중..."
    kill $BACKEND_PID 2>/dev/null || true
    kill $FRONTEND_PID 2>/dev/null || true
    "$SCRIPT_DIR/run.sh" down
    echo "모든 서비스가 종료되었습니다."
}

trap cleanup EXIT

# 프로세스 대기
wait
