#!/bin/bash
# ============================================================
# FindPlace - 서비스 서버 접속 스크립트
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$PROJECT_ROOT/.env"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# .env 파일 확인
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}오류: .env 파일을 찾을 수 없습니다.${NC}"
    echo "경로: $ENV_FILE"
    exit 1
fi

# .env 파일에서 SERVICE SERVER 정보 추출
# 형식: IP : xxx.xxx.xxx.xxx, ID : xxx, PASSWORD : xxx
SERVER_IP=$(grep "^IP" "$ENV_FILE" | sed 's/IP[[:space:]]*:[[:space:]]*//')
SERVER_ID=$(grep "^ID" "$ENV_FILE" | sed 's/ID[[:space:]]*:[[:space:]]*//')
SERVER_PASSWORD=$(grep "^PASSWORD" "$ENV_FILE" | sed 's/PASSWORD[[:space:]]*:[[:space:]]*//')

# 정보 확인
if [ -z "$SERVER_IP" ] || [ -z "$SERVER_ID" ] || [ -z "$SERVER_PASSWORD" ]; then
    echo -e "${RED}오류: .env 파일에서 서버 정보를 찾을 수 없습니다.${NC}"
    echo "필요한 정보: IP, ID, PASSWORD"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   FindPlace 서비스 서버 접속${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "서버: ${YELLOW}$SERVER_ID@$SERVER_IP${NC}"
echo ""

# sshpass 설치 확인
if ! command -v sshpass &> /dev/null; then
    echo -e "${YELLOW}sshpass가 설치되어 있지 않습니다.${NC}"
    echo ""
    echo "설치 방법:"
    echo "  macOS: brew install hudochenkov/sshpass/sshpass"
    echo "  Ubuntu/Debian: sudo apt-get install sshpass"
    echo ""
    echo -e "${YELLOW}수동으로 접속하시려면 다음 명령어를 사용하세요:${NC}"
    echo -e "  ssh $SERVER_ID@$SERVER_IP"
    echo ""

    # sshpass 없이 일반 SSH로 접속 시도
    read -p "일반 SSH로 접속하시겠습니까? (y/n): " answer
    if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
        ssh "$SERVER_ID@$SERVER_IP"
    fi
    exit 0
fi

# sshpass를 사용하여 SSH 접속
echo -e "${GREEN}서버에 접속 중...${NC}"
sshpass -p "$SERVER_PASSWORD" ssh -o StrictHostKeyChecking=no "$SERVER_ID@$SERVER_IP"
