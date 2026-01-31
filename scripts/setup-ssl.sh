#!/bin/bash
# ============================================================
# FindPlace - Let's Encrypt SSL 인증서 설정 스크립트
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

DOMAIN="findplace.co.kr"
EMAIL="${1:-admin@findplace.co.kr}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   FindPlace SSL 인증서 설정${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "도메인: ${GREEN}$DOMAIN${NC}"
echo -e "이메일: ${GREEN}$EMAIL${NC}"
echo ""

# 1. 필요한 디렉토리 생성
echo -e "${YELLOW}[1/5] 디렉토리 생성 중...${NC}"
mkdir -p "$PROJECT_ROOT/docker/certbot/conf"
mkdir -p "$PROJECT_ROOT/docker/certbot/www"

# 2. Docker Compose에 certbot 볼륨 추가 확인
echo -e "${YELLOW}[2/5] Docker 설정 확인 중...${NC}"

# nginx 컨테이너가 실행 중인지 확인
if ! docker ps | grep -q findplace-nginx; then
    echo -e "${RED}Nginx 컨테이너가 실행 중이 아닙니다.${NC}"
    echo "먼저 docker-compose up -d nginx 를 실행하세요."
    exit 1
fi

# 3. 인증서 발급 (Staging 테스트)
echo -e "${YELLOW}[3/5] SSL 인증서 발급 중...${NC}"
echo ""
echo -e "${YELLOW}참고: 처음에는 --staging 옵션으로 테스트를 권장합니다.${NC}"
echo ""

read -p "테스트 모드로 실행할까요? (y/n, 실제 인증서는 n): " staging_mode

STAGING_FLAG=""
if [ "$staging_mode" = "y" ] || [ "$staging_mode" = "Y" ]; then
    STAGING_FLAG="--staging"
    echo -e "${YELLOW}테스트 모드로 실행합니다.${NC}"
fi

# Certbot 실행
docker run --rm \
    -v "$PROJECT_ROOT/docker/certbot/conf:/etc/letsencrypt" \
    -v "$PROJECT_ROOT/docker/certbot/www:/var/www/certbot" \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN" \
    -d "www.$DOMAIN" \
    $STAGING_FLAG

# 4. SSL 설정 적용
echo -e "${YELLOW}[4/5] SSL 설정 적용 중...${NC}"

# 기존 default.conf 백업
cp "$PROJECT_ROOT/docker/nginx/conf.d/default.conf" \
   "$PROJECT_ROOT/docker/nginx/conf.d/default.conf.backup"

# SSL 설정으로 교체
cp "$PROJECT_ROOT/docker/nginx/conf.d/ssl.conf.template" \
   "$PROJECT_ROOT/docker/nginx/conf.d/default.conf"

# 5. Nginx 재시작
echo -e "${YELLOW}[5/5] Nginx 재시작 중...${NC}"
docker-compose -f "$PROJECT_ROOT/docker-compose.yml" restart nginx

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   SSL 설정 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "사이트 접속: ${BLUE}https://$DOMAIN${NC}"
echo ""
echo -e "${YELLOW}인증서 자동 갱신을 위해 crontab에 추가하세요:${NC}"
echo ""
echo "  sudo crontab -e"
echo "  # 매일 새벽 3시에 인증서 갱신 체크"
echo "  0 3 * * * cd $PROJECT_ROOT && docker run --rm -v $PROJECT_ROOT/docker/certbot/conf:/etc/letsencrypt -v $PROJECT_ROOT/docker/certbot/www:/var/www/certbot certbot/certbot renew --quiet && docker-compose restart nginx"
echo ""
