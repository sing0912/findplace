#!/usr/bin/env bash
# ============================================================
# PetPro 서버 배포 스크립트
# GitHub Release에서 최신 아티팩트를 다운로드하여 배포합니다.
#
# 사용법: ./deploy.sh
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ----------------------------------------
# 색상 정의
# ----------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()   { echo -e "${GREEN}[배포]${NC} $1"; }
warn()  { echo -e "${YELLOW}[경고]${NC} $1"; }
error() { echo -e "${RED}[오류]${NC} $1"; exit 1; }

# ----------------------------------------
# .env 로드
# ----------------------------------------
if [ ! -f .env ]; then
    error ".env 파일이 없습니다. cp .env.example .env 후 값을 설정하세요."
fi

set -a
source .env
set +a

# ----------------------------------------
# 필수 변수 검증
# ----------------------------------------
: "${GITHUB_REPO:?'.env에 GITHUB_REPO가 설정되지 않았습니다 (예: sing0912/findplace)'}"
: "${DB_PASSWORD:?'.env에 DB_PASSWORD가 설정되지 않았습니다'}"
: "${REDIS_PASSWORD:?'.env에 REDIS_PASSWORD가 설정되지 않았습니다'}"
: "${JWT_SECRET:?'.env에 JWT_SECRET가 설정되지 않았습니다'}"

# ----------------------------------------
# Compose 명령어 감지
# ----------------------------------------
COMPOSE_CMD="docker compose"
if ! command -v docker &> /dev/null; then
    if command -v podman-compose &> /dev/null; then
        COMPOSE_CMD="podman-compose"
    else
        error "docker 또는 podman-compose가 설치되어 있지 않습니다."
    fi
fi

# 필수 서비스만 시작 (모니터링/Slave 제외로 메모리 절약)
ESSENTIAL_SERVICES="postgres-master postgres-coupon mysql-log-master redis minio nginx"

# ----------------------------------------
# 최신 Release 정보 조회
# ----------------------------------------
log "최신 Release 정보 조회 중..."

RELEASE_INFO=$(curl -sfL "https://api.github.com/repos/${GITHUB_REPO}/releases/latest") \
    || error "Release 정보를 가져올 수 없습니다. GITHUB_REPO=${GITHUB_REPO} 확인하세요."

TAG_NAME=$(echo "$RELEASE_INFO" | grep -m1 '"tag_name"' | sed 's/.*: "\(.*\)".*/\1/')
log "최신 Release: ${TAG_NAME}"

# ----------------------------------------
# 아티팩트 다운로드
# ----------------------------------------
DOWNLOAD_BASE="https://github.com/${GITHUB_REPO}/releases/download/${TAG_NAME}"

log "backend.jar 다운로드 중..."
curl -sfL -o backend.jar "${DOWNLOAD_BASE}/backend.jar" \
    || error "backend.jar 다운로드 실패"

log "frontend.tar.gz 다운로드 중..."
curl -sfL -o frontend.tar.gz "${DOWNLOAD_BASE}/frontend.tar.gz" \
    || error "frontend.tar.gz 다운로드 실패"

# ----------------------------------------
# 프론트엔드 정적 파일 배포
# ----------------------------------------
log "프론트엔드 정적 파일 배포 중..."
mkdir -p frontend/build
rm -rf frontend/build/*
tar -xzf frontend.tar.gz -C frontend/build

# ----------------------------------------
# 기존 백엔드 프로세스 종료
# ----------------------------------------
if pgrep -f "java.*backend.jar" > /dev/null 2>&1; then
    log "기존 백엔드 프로세스 종료 중..."
    pkill -f "java.*backend.jar" || true
    sleep 2
fi

# ----------------------------------------
# 불필요한 컨테이너 중지 (메모리 확보)
# ----------------------------------------
log "불필요한 컨테이너 중지 중..."
for c in petpro-grafana petpro-prometheus petpro-loki petpro-promtail petpro-tempo petpro-postgres-slave1 petpro-postgres-slave2 petpro-mysql-log-slave; do
    docker stop "$c" 2>/dev/null && docker rm "$c" 2>/dev/null || true
done

# ----------------------------------------
# Docker 인프라 시작 (필수 서비스만)
# ----------------------------------------
log "필수 Docker 서비스 시작 중... (${ESSENTIAL_SERVICES})"
$COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml up -d $ESSENTIAL_SERVICES

# ----------------------------------------
# Docker 인프라 준비 대기
# ----------------------------------------
log "인프라 준비 대기 중 (40초)..."
sleep 40

# ----------------------------------------
# 쿠폰 DB 자동 생성 (없는 경우)
# ----------------------------------------
if ! docker exec petpro-postgres-coupon psql -U coupon -d petpro_coupon -c "SELECT 1" &>/dev/null; then
    log "쿠폰 DB(petpro_coupon) 생성 중..."
    docker exec petpro-postgres-coupon psql -U coupon -d postgres -c "CREATE DATABASE petpro_coupon OWNER coupon;" \
        && log "쿠폰 DB 생성 완료" \
        || warn "쿠폰 DB 생성 실패 - 수동 확인 필요"
fi

# ----------------------------------------
# 백엔드 실행 (메모리 제한)
# ----------------------------------------
log "백엔드 시작 중..."
mkdir -p logs

APP_ENV="${APP_ENV:-prod}"

nohup java -Xms128m -Xmx384m -jar backend.jar \
    --spring.profiles.active="$APP_ENV" \
    > logs/backend.log 2>&1 &

BACKEND_PID=$!
log "백엔드 PID: ${BACKEND_PID}"

# ----------------------------------------
# 백엔드 헬스체크 대기
# ----------------------------------------
log "백엔드 헬스체크 대기 중 (최대 3분)..."

for i in $(seq 1 90); do
    if curl -sf http://localhost:${APP_PORT:-8080}/api/health > /dev/null 2>&1; then
        log "백엔드 헬스체크 통과!"
        break
    fi
    if [ "$i" -eq 90 ]; then
        warn "백엔드 헬스체크 타임아웃 (3분). logs/backend.log를 확인하세요."
    fi
    sleep 2
done

# ----------------------------------------
# nginx 재시작 (새 정적 파일 반영)
# ----------------------------------------
log "nginx 재시작 중..."
$COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml restart nginx

# ----------------------------------------
# 완료
# ----------------------------------------
log "=========================================="
log "배포 완료! (Release: ${TAG_NAME})"
log "=========================================="
log "  백엔드:     http://localhost:${APP_PORT:-8080}"
log "  프론트엔드: https://dev.findplace.co.kr"
log "=========================================="
