#!/bin/bash
# ============================================================
# FindPlace - SSL 인증서 갱신 스크립트
# crontab에 등록: 0 3 * * * /path/to/scripts/renew-ssl.sh
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# Nginx 중지
docker-compose stop nginx

# 인증서 갱신
docker run --rm \
    -v "$PROJECT_ROOT/docker/certbot/conf:/etc/letsencrypt" \
    -p 80:80 \
    certbot/certbot renew --standalone --quiet

# 심볼릭 링크 처리 (갱신 후 심볼릭 링크가 다시 생성될 수 있음)
for DOMAIN_DIR in "$PROJECT_ROOT/docker/certbot/conf/live"/*/; do
    if [ -d "$DOMAIN_DIR" ]; then
        for pem in fullchain.pem privkey.pem cert.pem chain.pem; do
            if [ -L "$DOMAIN_DIR$pem" ]; then
                cp -L "$DOMAIN_DIR$pem" "$DOMAIN_DIR${pem}.tmp"
                rm "$DOMAIN_DIR$pem"
                mv "$DOMAIN_DIR${pem}.tmp" "$DOMAIN_DIR$pem"
            fi
        done
    fi
done

# Nginx 시작
docker-compose up -d nginx

echo "[$(date)] SSL 인증서 갱신 완료"
