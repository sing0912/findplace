#!/bin/bash
# ============================================================
# FindPlace - SSL 인증서 갱신 스크립트
# crontab에 등록하여 자동 갱신
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 인증서 갱신
docker run --rm \
    -v "$PROJECT_ROOT/docker/certbot/conf:/etc/letsencrypt" \
    -v "$PROJECT_ROOT/docker/certbot/www:/var/www/certbot" \
    certbot/certbot renew --quiet

# Nginx 재시작
docker-compose -f "$PROJECT_ROOT/docker-compose.yml" restart nginx

echo "[$(date)] SSL 인증서 갱신 체크 완료"
