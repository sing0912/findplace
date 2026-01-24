#!/bin/bash
# ============================================================
# FindPlace 전체 서비스 중지 스크립트
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${RED}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║           FindPlace - 전체 서비스 중지                    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Java 프로세스 종료 (Spring Boot)
echo -e "${GREEN}[1/3]${NC} Backend 프로세스 종료 중..."
pkill -f "findplace" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true

# Node 프로세스 종료 (React)
echo -e "${GREEN}[2/3]${NC} Frontend 프로세스 종료 중..."
pkill -f "react-scripts" 2>/dev/null || true
pkill -f "node.*frontend" 2>/dev/null || true

# Docker/Podman 컨테이너 중지
echo -e "${GREEN}[3/3]${NC} 인프라 중지 중..."
"$SCRIPT_DIR/run.sh" down

echo ""
echo -e "${GREEN}모든 서비스가 중지되었습니다.${NC}"
