#!/bin/bash
# ============================================================
# FindPlace - 서버 설정 자동화 스크립트
# 사용법: ./scripts/server_setup.sh <도메인> <이메일>
# 예시: ./scripts/server_setup.sh dev.findplace.co.kr admin@findplace.co.kr
# ============================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 스크립트 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 인자 확인
if [ $# -lt 2 ]; then
    echo -e "${RED}사용법: $0 <도메인> <이메일>${NC}"
    echo -e "예시: $0 dev.findplace.co.kr admin@findplace.co.kr"
    exit 1
fi

DOMAIN="$1"
EMAIL="$2"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   FindPlace 서버 설정${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "도메인: ${GREEN}$DOMAIN${NC}"
echo -e "이메일: ${GREEN}$EMAIL${NC}"
echo -e "프로젝트: ${GREEN}$PROJECT_ROOT${NC}"
echo ""

# 1. 디렉토리 생성
echo -e "${YELLOW}[1/7] 디렉토리 생성 중...${NC}"
mkdir -p "$PROJECT_ROOT/docker/certbot/conf"
mkdir -p "$PROJECT_ROOT/docker/certbot/www/.well-known/acme-challenge"

# 2. 기존 SSL 설정 파일 비활성화
echo -e "${YELLOW}[2/7] 기존 SSL 설정 비활성화...${NC}"
for f in "$PROJECT_ROOT/docker/nginx/conf.d"/*.conf; do
    if [ -f "$f" ] && [ "$(basename "$f")" != "default.conf" ]; then
        mv "$f" "${f}.disabled" 2>/dev/null || true
    fi
done

# 3. HTTP 전용 Nginx 설정 생성
echo -e "${YELLOW}[3/7] HTTP 전용 Nginx 설정 생성...${NC}"
TEMPLATE_DIR="$PROJECT_ROOT/docker/nginx/conf.d/templates"
if [ -f "$TEMPLATE_DIR/http.conf.template" ]; then
    sed "s/{{DOMAIN}}/$DOMAIN/g" "$TEMPLATE_DIR/http.conf.template" > "$PROJECT_ROOT/docker/nginx/conf.d/default.conf"
else
    cat > "$PROJECT_ROOT/docker/nginx/conf.d/default.conf" << HTTPEOF
upstream backend {
    server host.docker.internal:8080;
}

upstream frontend {
    server host.docker.internal:3000;
}

server {
    listen 80;
    server_name $DOMAIN;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /health {
        return 200 'OK';
        add_header Content-Type text/plain;
    }

    location /api {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /ws {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
    }

    location /files {
        rewrite ^/files/(.*) /\$1 break;
        proxy_pass http://minio:9000;
    }

    location / {
        proxy_pass http://frontend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
    }
}
HTTPEOF
fi

# 4. Nginx 컨테이너 시작 (HTTP)
echo -e "${YELLOW}[4/7] Nginx 컨테이너 시작 (HTTP)...${NC}"
cd "$PROJECT_ROOT"
docker-compose rm -f nginx 2>/dev/null || true
docker-compose up -d nginx

# Nginx가 시작될 때까지 대기
sleep 3

# Nginx 상태 확인
if ! docker ps | grep -q findplace-nginx; then
    echo -e "${RED}Nginx 시작 실패. 로그 확인:${NC}"
    docker-compose logs --tail=20 nginx
    exit 1
fi

echo -e "${GREEN}Nginx 시작 완료${NC}"

# 5. SSL 인증서 발급
echo -e "${YELLOW}[5/7] SSL 인증서 발급 중...${NC}"

# Nginx 중지 (standalone 모드 사용)
docker-compose stop nginx

# certbot 실행
docker run --rm \
    -v "$PROJECT_ROOT/docker/certbot/conf:/etc/letsencrypt" \
    -p 80:80 \
    certbot/certbot certonly \
    --standalone \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    --force-renewal \
    -d "$DOMAIN"

# 인증서 확인
if [ ! -d "$PROJECT_ROOT/docker/certbot/conf/live/$DOMAIN" ]; then
    echo -e "${RED}SSL 인증서 발급 실패${NC}"
    exit 1
fi

# 심볼릭 링크를 실제 파일로 복사 (Docker 볼륨 호환성)
echo -e "${YELLOW}[6/7] SSL 인증서 파일 처리...${NC}"
CERT_DIR="$PROJECT_ROOT/docker/certbot/conf/live/$DOMAIN"
ARCHIVE_DIR="$PROJECT_ROOT/docker/certbot/conf/archive/$DOMAIN"

if [ -L "$CERT_DIR/fullchain.pem" ]; then
    # 심볼릭 링크인 경우 실제 파일로 복사
    cp -L "$CERT_DIR/fullchain.pem" "$CERT_DIR/fullchain.pem.tmp"
    cp -L "$CERT_DIR/privkey.pem" "$CERT_DIR/privkey.pem.tmp"
    cp -L "$CERT_DIR/cert.pem" "$CERT_DIR/cert.pem.tmp"
    cp -L "$CERT_DIR/chain.pem" "$CERT_DIR/chain.pem.tmp"

    rm "$CERT_DIR/fullchain.pem" "$CERT_DIR/privkey.pem" "$CERT_DIR/cert.pem" "$CERT_DIR/chain.pem"

    mv "$CERT_DIR/fullchain.pem.tmp" "$CERT_DIR/fullchain.pem"
    mv "$CERT_DIR/privkey.pem.tmp" "$CERT_DIR/privkey.pem"
    mv "$CERT_DIR/cert.pem.tmp" "$CERT_DIR/cert.pem"
    mv "$CERT_DIR/chain.pem.tmp" "$CERT_DIR/chain.pem"
fi

echo -e "${GREEN}SSL 인증서 발급 완료${NC}"

# 7. SSL Nginx 설정 적용
echo -e "${YELLOW}[7/7] SSL Nginx 설정 적용...${NC}"
if [ -f "$TEMPLATE_DIR/ssl.conf.template" ]; then
    sed "s/{{DOMAIN}}/$DOMAIN/g" "$TEMPLATE_DIR/ssl.conf.template" > "$PROJECT_ROOT/docker/nginx/conf.d/default.conf"
else
    cat > "$PROJECT_ROOT/docker/nginx/conf.d/default.conf" << SSLEOF
upstream backend {
    server host.docker.internal:8080;
}

upstream frontend {
    server host.docker.internal:3000;
}

server {
    listen 80;
    server_name $DOMAIN;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl;
    http2 on;
    server_name $DOMAIN;

    ssl_certificate /etc/letsencrypt/live/$DOMAIN/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$DOMAIN/privkey.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_session_tickets off;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000" always;

    location /health {
        return 200 'OK';
        add_header Content-Type text/plain;
    }

    location /api {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 300s;
    }

    location /ws {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_read_timeout 86400s;
    }

    location /files {
        rewrite ^/files/(.*) /\$1 break;
        proxy_pass http://minio:9000;
    }

    location / {
        proxy_pass http://frontend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
    }
}
SSLEOF
fi

# Nginx 재시작
docker-compose up -d nginx

# 최종 확인
sleep 3
if docker ps | grep -q findplace-nginx; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}   설정 완료!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "HTTP:  ${BLUE}http://$DOMAIN${NC}"
    echo -e "HTTPS: ${BLUE}https://$DOMAIN${NC}"
    echo ""
    echo -e "${YELLOW}인증서 자동 갱신 설정 (crontab -e):${NC}"
    echo "0 3 * * * $PROJECT_ROOT/scripts/renew-ssl.sh >> /var/log/ssl-renew.log 2>&1"
    echo ""
else
    echo -e "${RED}Nginx 시작 실패. 로그 확인:${NC}"
    docker-compose logs --tail=20 nginx
    exit 1
fi
