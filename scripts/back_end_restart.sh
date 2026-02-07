#!/bin/bash
#
# 백엔드 재시작 스크립트
# 사용법: ./scripts/back_end_restart.sh
#
# 옵션:
#   --skip-db    PostgreSQL 시작 건너뛰기
#   --log        로그 출력 모드 (foreground 실행)
#   --db-only    DB만 시작하고 종료
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"
LOG_DIR="$PROJECT_ROOT/logs"

# 옵션 파싱
SKIP_DB=false
LOG_MODE=false
DB_ONLY=false
for arg in "$@"; do
    case $arg in
        --skip-db)
            SKIP_DB=true
            ;;
        --log)
            LOG_MODE=true
            ;;
        --db-only)
            DB_ONLY=true
            ;;
    esac
done

# 로그 디렉토리 생성
mkdir -p "$LOG_DIR"

echo "=========================================="
echo "  PetPro 백엔드 재시작"
echo "=========================================="
echo ""

# 컨테이너 런타임 감지 (podman 우선)
detect_container_runtime() {
    if command -v podman &> /dev/null; then
        echo "podman"
    elif command -v docker &> /dev/null; then
        echo "docker"
    else
        echo ""
    fi
}

CONTAINER_CMD=$(detect_container_runtime)

# PostgreSQL 연결 확인 함수 (pg_isready 없이도 동작)
check_postgres() {
    local host=$1
    local port=$2

    # pg_isready가 있으면 사용
    if command -v pg_isready &> /dev/null; then
        pg_isready -h "$host" -p "$port" -q 2>/dev/null
        return $?
    fi

    # nc (netcat)로 포트 확인
    if command -v nc &> /dev/null; then
        nc -z "$host" "$port" 2>/dev/null
        return $?
    fi

    # 최후의 방법: /dev/tcp 사용
    (echo > /dev/tcp/"$host"/"$port") 2>/dev/null
    return $?
}

# PostgreSQL 컨테이너 시작 함수
start_postgres_containers() {
    local cmd=$1

    echo "🔄 $cmd 으로 PostgreSQL 시작 중..."

    # Master DB 컨테이너 확인 및 시작
    if $cmd ps -a --format '{{.Names}}' 2>/dev/null | grep -q 'petpro-postgres-master'; then
        echo "   기존 master 컨테이너 시작..."
        $cmd start petpro-postgres-master 2>&1 || true
    else
        echo "   새 master 컨테이너 생성..."
        $cmd run -d --name petpro-postgres-master \
            -e POSTGRES_DB=petpro \
            -e POSTGRES_USER=petpro \
            -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
            -p 5432:5432 \
            postgres:16-alpine 2>&1
    fi

    # Coupon DB 컨테이너 확인 및 시작
    if $cmd ps -a --format '{{.Names}}' 2>/dev/null | grep -q 'petpro-postgres-coupon'; then
        echo "   기존 coupon 컨테이너 시작..."
        $cmd start petpro-postgres-coupon 2>&1 || true
    else
        echo "   새 coupon 컨테이너 생성..."
        $cmd run -d --name petpro-postgres-coupon \
            -e POSTGRES_DB=petpro_coupon \
            -e POSTGRES_USER=coupon \
            -e POSTGRES_PASSWORD="${COUPON_DB_PASSWORD}" \
            -p 5435:5432 \
            postgres:16-alpine 2>&1
    fi
}

# PostgreSQL 상태 확인 및 시작
if [ "$SKIP_DB" = false ]; then
    echo "📦 PostgreSQL 상태 확인..."

    # PostgreSQL 연결 테스트
    if ! check_postgres localhost 5432; then
        echo "⚠️  PostgreSQL이 실행되지 않고 있습니다."

        if [ -n "$CONTAINER_CMD" ]; then
            start_postgres_containers "$CONTAINER_CMD"

            echo "⏳ PostgreSQL 연결 대기 중..."
            for i in {1..30}; do
                if check_postgres localhost 5432; then
                    echo ""
                    echo "✅ PostgreSQL 연결 성공!"
                    break
                fi
                sleep 2
                echo -n "."
            done
            echo ""
        else
            echo "❌ 컨테이너 런타임을 찾을 수 없습니다."
            echo "   podman 또는 docker를 설치해주세요."
            exit 1
        fi

        if ! check_postgres localhost 5432; then
            echo "❌ PostgreSQL에 연결할 수 없습니다."
            echo ""
            echo "   컨테이너 상태 확인:"
            $CONTAINER_CMD ps -a 2>/dev/null | grep postgres || true
            echo ""
            echo "   수동으로 시작하려면:"
            echo "   $CONTAINER_CMD start petpro-postgres-master petpro-postgres-coupon"
            exit 1
        fi
    else
        echo "✅ PostgreSQL 실행 중"
    fi
    echo ""
fi

# DB만 시작하는 옵션
if [ "$DB_ONLY" = true ]; then
    echo "✅ DB만 시작 완료"
    exit 0
fi

# 기존 백엔드 프로세스 종료
echo "📍 기존 백엔드 프로세스 확인..."
PIDS=$(lsof -t -i:8080 2>/dev/null || true)
if [ -n "$PIDS" ]; then
    echo "🛑 포트 8080 프로세스 종료: $PIDS"
    kill -15 $PIDS 2>/dev/null || true
    sleep 2
    # 강제 종료
    PIDS=$(lsof -t -i:8080 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        kill -9 $PIDS 2>/dev/null || true
        sleep 1
    fi
    echo "✅ 기존 프로세스 종료 완료"
else
    echo "✅ 실행 중인 프로세스 없음"
fi
echo ""

# 백엔드 디렉토리로 이동
cd "$BACKEND_DIR"

# 빌드 (옵션)
# echo "🔨 백엔드 빌드 중..."
# ./gradlew build -x test --quiet

# 백엔드 시작
echo "🚀 백엔드 시작 중..."
if [ "$LOG_MODE" = true ]; then
    echo "   (로그 모드 - Ctrl+C로 종료)"
    echo ""
    ./gradlew bootRun
else
    LOG_FILE="$LOG_DIR/backend_$(date +%Y%m%d_%H%M%S).log"
    nohup ./gradlew bootRun > "$LOG_FILE" 2>&1 &
    BACKEND_PID=$!
    echo "   PID: $BACKEND_PID"
    echo "   로그: $LOG_FILE"

    # 시작 확인 대기
    echo ""
    echo "⏳ 서버 시작 대기 중..."
    for i in {1..30}; do
        # 포트가 열려있는지 확인 (서버가 응답하면 성공)
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
        if [ "$HTTP_CODE" != "000" ]; then
            echo ""
            echo "✅ 백엔드 시작 완료!"
            break
        fi
        sleep 2
        echo -n "."
    done

    # 최종 상태 확인
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
    if [ "$HTTP_CODE" = "000" ]; then
        echo ""
        echo "⚠️  서버가 아직 시작되지 않았을 수 있습니다."
        echo "   로그를 확인해주세요: tail -f $LOG_FILE"
    fi
fi

echo ""
echo "=========================================="
echo "  백엔드 정보"
echo "=========================================="
echo "  URL: http://localhost:8080"
echo "  API Docs: http://localhost:8080/swagger-ui.html"
echo "  Health: http://localhost:8080/health"
echo ""
echo "  로그 확인: tail -f $LOG_DIR/backend_*.log"
echo "  프로세스 종료: kill \$(lsof -t -i:8080)"
echo "=========================================="
