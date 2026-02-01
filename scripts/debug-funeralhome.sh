#!/bin/bash
# ============================================================
# FindPlace - 장례식장 디버깅 스크립트
# 사용법: ./scripts/debug-funeralhome.sh
# ============================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   FindPlace 장례식장 디버깅${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. 환경변수 확인
echo -e "${YELLOW}[1/6] 환경변수 확인${NC}"
echo ""

# Frontend .env 확인
if [ -f "$PROJECT_ROOT/frontend/.env" ]; then
    echo -e "${GREEN}frontend/.env 파일 존재${NC}"
    if grep -q "REACT_APP_GOOGLE_MAPS_API_KEY" "$PROJECT_ROOT/frontend/.env"; then
        KEY=$(grep "REACT_APP_GOOGLE_MAPS_API_KEY" "$PROJECT_ROOT/frontend/.env" | cut -d'=' -f2)
        if [ -n "$KEY" ] && [ "$KEY" != "" ]; then
            echo -e "  Google Maps API Key: ${GREEN}설정됨${NC} (${KEY:0:10}...)"
        else
            echo -e "  Google Maps API Key: ${RED}값이 비어있음${NC}"
        fi
    else
        echo -e "  Google Maps API Key: ${RED}미설정${NC}"
    fi
else
    echo -e "${RED}frontend/.env 파일 없음${NC}"
fi
echo ""

# 2. 백엔드 로그 확인
echo -e "${YELLOW}[2/6] 백엔드 에러 로그 확인${NC}"
echo ""
if [ -f "/tmp/findplace-backend.log" ]; then
    ERRORS=$(grep -i "error\|exception" /tmp/findplace-backend.log 2>/dev/null | tail -10)
    if [ -n "$ERRORS" ]; then
        echo -e "${RED}최근 에러:${NC}"
        echo "$ERRORS"
    else
        echo -e "${GREEN}최근 에러 없음${NC}"
    fi
else
    echo "백엔드 로그 파일 없음 (/tmp/findplace-backend.log)"
fi
echo ""

# 3. DB 연결 확인 및 데이터 조회
echo -e "${YELLOW}[3/6] 데이터베이스 장례식장 데이터 확인${NC}"
echo ""

# Docker PostgreSQL 컨테이너 확인
if docker ps | grep -q findplace-postgres-master; then
    echo -e "${GREEN}PostgreSQL 컨테이너 실행 중${NC}"

    echo ""
    echo "=== 전체 장례식장 수 ==="
    docker exec findplace-postgres-master psql -U findplace -d findplace -c "SELECT COUNT(*) as total FROM funeral_homes;" 2>/dev/null || echo "쿼리 실패"

    echo ""
    echo "=== 활성화된 장례식장 수 ==="
    docker exec findplace-postgres-master psql -U findplace -d findplace -c "SELECT COUNT(*) as active FROM funeral_homes WHERE is_active = true;" 2>/dev/null || echo "쿼리 실패"

    echo ""
    echo "=== 좌표가 있는 장례식장 수 ==="
    docker exec findplace-postgres-master psql -U findplace -d findplace -c "SELECT COUNT(*) as with_coords FROM funeral_homes WHERE latitude IS NOT NULL AND longitude IS NOT NULL;" 2>/dev/null || echo "쿼리 실패"

    echo ""
    echo "=== 검색 가능한 장례식장 수 (활성 + 좌표 있음) ==="
    docker exec findplace-postgres-master psql -U findplace -d findplace -c "SELECT COUNT(*) as searchable FROM funeral_homes WHERE is_active = true AND latitude IS NOT NULL AND longitude IS NOT NULL;" 2>/dev/null || echo "쿼리 실패"

    echo ""
    echo "=== 샘플 데이터 (상위 5개) ==="
    docker exec findplace-postgres-master psql -U findplace -d findplace -c "SELECT id, name, ROUND(latitude::numeric, 4) as lat, ROUND(longitude::numeric, 4) as lng, is_active FROM funeral_homes LIMIT 5;" 2>/dev/null || echo "쿼리 실패"

else
    echo -e "${RED}PostgreSQL 컨테이너가 실행 중이 아닙니다${NC}"
fi
echo ""

# 4. API 테스트
echo -e "${YELLOW}[4/6] API 엔드포인트 테스트${NC}"
echo ""

# 서울 시청 좌표로 테스트
TEST_LAT=37.5665
TEST_LNG=126.9780
RADIUS=50

echo "테스트 위치: 서울 시청 (${TEST_LAT}, ${TEST_LNG})"
echo "검색 반경: ${RADIUS}km"
echo ""

# 로컬 API 테스트
echo "=== 로컬 API 테스트 (localhost:8080) ==="
API_RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/v1/funeral-homes/nearby?latitude=${TEST_LAT}&longitude=${TEST_LNG}&radius=${RADIUS}&limit=5" 2>/dev/null)
HTTP_CODE=$(echo "$API_RESPONSE" | tail -1)
BODY=$(echo "$API_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "HTTP Status: ${GREEN}200 OK${NC}"
    echo "응답:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo -e "HTTP Status: ${RED}${HTTP_CODE}${NC}"
    echo "응답: $BODY"
fi
echo ""

# 5. 프론트엔드 빌드 환경변수 확인
echo -e "${YELLOW}[5/6] 프론트엔드 빌드 환경변수${NC}"
echo ""
if [ -d "$PROJECT_ROOT/frontend/build" ]; then
    echo "빌드 폴더 존재"
    # 빌드된 JS에서 API 키 확인 (보안상 실제 키는 숨김)
    if grep -r "AIzaSy" "$PROJECT_ROOT/frontend/build/static/js/" 2>/dev/null | head -1 > /dev/null; then
        echo -e "Google Maps API Key: ${GREEN}빌드에 포함됨${NC}"
    else
        echo -e "Google Maps API Key: ${RED}빌드에 포함되지 않음${NC}"
        echo -e "${YELLOW}해결: frontend/.env 설정 후 npm run build 재실행 필요${NC}"
    fi
else
    echo "빌드 폴더 없음 (개발 모드로 실행 중이면 정상)"
fi
echo ""

# 6. 요약 및 권장사항
echo -e "${YELLOW}[6/6] 진단 요약${NC}"
echo ""
echo -e "${BLUE}========================================${NC}"
echo ""

# Google Maps 키 확인
if grep -q "REACT_APP_GOOGLE_MAPS_API_KEY=AIza" "$PROJECT_ROOT/frontend/.env" 2>/dev/null; then
    echo -e "✅ Google Maps API Key 설정됨"
else
    echo -e "❌ Google Maps API Key 미설정"
    echo -e "   ${YELLOW}해결: frontend/.env 에 REACT_APP_GOOGLE_MAPS_API_KEY=your_key 추가${NC}"
fi

# 데이터 확인
SEARCHABLE=$(docker exec findplace-postgres-master psql -U findplace -d findplace -t -c "SELECT COUNT(*) FROM funeral_homes WHERE is_active = true AND latitude IS NOT NULL AND longitude IS NOT NULL;" 2>/dev/null | tr -d ' ')
if [ -n "$SEARCHABLE" ] && [ "$SEARCHABLE" -gt "0" ]; then
    echo -e "✅ 검색 가능한 장례식장: ${SEARCHABLE}개"
else
    echo -e "❌ 검색 가능한 장례식장: 0개"
    echo -e "   ${YELLOW}해결: 데이터 동기화 필요 (관리자 API 또는 배치 작업)${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
