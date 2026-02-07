#!/bin/bash
# ============================================================
# PetPro 통합 실행 스크립트
# Docker / Podman 자동 호환
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 런타임 감지 스크립트 로드
source "$SCRIPT_DIR/detect-runtime.sh"

# 전역 변수
CONTAINER_RT=""
COMPOSE_RT=""

# ============================================================
# 유틸리티 함수
# ============================================================

print_banner() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║           PetPro - 반려동물 장례 토탈 플랫폼           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================================
# 런타임 초기화
# ============================================================

init_runtime() {
    CONTAINER_RT=$(detect_container_runtime)
    COMPOSE_RT=$(detect_compose_runtime)

    if [ "$CONTAINER_RT" == "none" ]; then
        print_error "Docker 또는 Podman이 설치되어 있지 않습니다."
        print_info "설치 방법:"
        print_info "  - Docker: https://docs.docker.com/get-docker/"
        print_info "  - Podman: https://podman.io/getting-started/installation"
        exit 1
    fi

    if [ "$COMPOSE_RT" == "none" ]; then
        print_error "docker-compose 또는 podman-compose가 설치되어 있지 않습니다."
        if [ "$CONTAINER_RT" == "podman" ]; then
            print_info "설치: pip install podman-compose"
        else
            print_info "설치: https://docs.docker.com/compose/install/"
        fi
        exit 1
    fi

    print_info "컨테이너 런타임: ${GREEN}$CONTAINER_RT${NC}"
    print_info "Compose 런타임: ${GREEN}$COMPOSE_RT${NC}"
}

# Compose 명령 실행
run_compose() {
    cd "$PROJECT_ROOT"
    $COMPOSE_RT "$@"
}

# Container 명령 실행
run_container() {
    $CONTAINER_RT "$@"
}

# ============================================================
# 명령어 함수
# ============================================================

cmd_init() {
    print_info "PetPro 초기화 중..."

    # 스크립트 실행 권한
    chmod +x "$SCRIPT_DIR"/*.sh
    chmod +x "$PROJECT_ROOT/docker/postgres/"*.sh 2>/dev/null || true

    # .env 파일 생성
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
        print_success ".env 파일이 생성되었습니다."
    else
        print_warning ".env 파일이 이미 존재합니다."
    fi

    # Podman 전용 설정
    if [ "$CONTAINER_RT" == "podman" ]; then
        print_info "Podman 환경 설정 중..."

        # rootless 모드에서 필요한 설정
        if ! grep -q "host.containers.internal" /etc/hosts 2>/dev/null; then
            print_warning "host.containers.internal 설정이 필요할 수 있습니다."
        fi
    fi

    print_success "초기화 완료!"
    echo ""
    print_info "다음 단계:"
    print_info "  1. .env 파일을 편집하여 설정을 변경하세요"
    print_info "  2. './scripts/run.sh up' 명령으로 서비스를 시작하세요"
}

cmd_up() {
    print_info "서비스 시작 중..."
    run_compose up -d "$@"

    echo ""
    print_success "서비스가 시작되었습니다!"
    echo ""
    echo "┌─────────────────────────────────────────┐"
    echo "│ 서비스             │ 주소                │"
    echo "├─────────────────────────────────────────┤"
    echo "│ PostgreSQL Master  │ localhost:5432     │"
    echo "│ PostgreSQL Slave1  │ localhost:5433     │"
    echo "│ PostgreSQL Slave2  │ localhost:5434     │"
    echo "│ Redis              │ localhost:6379     │"
    echo "│ MinIO API          │ localhost:9000     │"
    echo "│ MinIO Console      │ localhost:9001     │"
    echo "│ Nginx              │ localhost:80       │"
    echo "└─────────────────────────────────────────┘"
}

cmd_down() {
    print_info "서비스 중지 중..."
    run_compose down "$@"
    print_success "서비스가 중지되었습니다."
}

cmd_restart() {
    print_info "서비스 재시작 중..."
    run_compose restart "$@"
    print_success "서비스가 재시작되었습니다."
}

cmd_logs() {
    run_compose logs -f "$@"
}

cmd_ps() {
    run_compose ps "$@"
}

cmd_clean() {
    print_warning "모든 컨테이너와 볼륨을 삭제합니다."
    read -p "계속하시겠습니까? (y/N): " confirm
    if [ "$confirm" == "y" ] || [ "$confirm" == "Y" ]; then
        run_compose down -v --remove-orphans
        print_success "모든 컨테이너와 볼륨이 삭제되었습니다."
    else
        print_info "취소되었습니다."
    fi
}

cmd_db_shell() {
    local target=${1:-master}
    local container_name="petpro-postgres-$target"

    print_info "PostgreSQL ($target) 접속 중..."
    run_container exec -it "$container_name" psql -U petpro -d petpro
}

cmd_redis_shell() {
    print_info "Redis 접속 중..."
    run_container exec -it petpro-redis redis-cli -a "${REDIS_PASSWORD}"
}

cmd_replication_status() {
    print_info "Replication 상태 확인 중..."
    run_container exec -it petpro-postgres-master \
        psql -U petpro -d petpro -c "SELECT * FROM pg_stat_replication;"
}

cmd_backend() {
    local action=${1:-start}

    case "$action" in
        start)
            print_info "Backend 서버 시작 중..."
            cd "$PROJECT_ROOT/backend"
            ./gradlew bootRun
            ;;
        build)
            print_info "Backend 빌드 중..."
            cd "$PROJECT_ROOT/backend"
            ./gradlew build -x test
            ;;
        test)
            print_info "Backend 테스트 실행 중..."
            cd "$PROJECT_ROOT/backend"
            ./gradlew test
            ;;
        *)
            print_error "알 수 없는 명령: $action"
            print_info "사용 가능: start, build, test"
            ;;
    esac
}

cmd_frontend() {
    local action=${1:-start}

    case "$action" in
        start)
            print_info "Frontend 서버 시작 중..."
            cd "$PROJECT_ROOT/frontend"
            npm start
            ;;
        build)
            print_info "Frontend 빌드 중..."
            cd "$PROJECT_ROOT/frontend"
            npm run build
            ;;
        test)
            print_info "Frontend 테스트 실행 중..."
            cd "$PROJECT_ROOT/frontend"
            npm test
            ;;
        install)
            print_info "Frontend 의존성 설치 중..."
            cd "$PROJECT_ROOT/frontend"
            npm install
            ;;
        *)
            print_error "알 수 없는 명령: $action"
            print_info "사용 가능: start, build, test, install"
            ;;
    esac
}

cmd_dev() {
    print_info "개발 환경 시작 중..."

    # 인프라 시작
    cmd_up

    echo ""
    print_info "Backend와 Frontend를 별도 터미널에서 실행하세요:"
    print_info "  Backend:  ./scripts/run.sh backend start"
    print_info "  Frontend: ./scripts/run.sh frontend start"
}

cmd_status() {
    echo ""
    print_info "=== 컨테이너 상태 ==="
    run_compose ps

    echo ""
    print_info "=== Replication 상태 ==="
    run_container exec petpro-postgres-master \
        psql -U petpro -d petpro -c "SELECT client_addr, state, sent_lsn, write_lsn FROM pg_stat_replication;" 2>/dev/null || \
        print_warning "PostgreSQL Master가 실행 중이 아닙니다."
}

cmd_help() {
    print_banner
    echo "사용법: $0 <command> [options]"
    echo ""
    echo "인프라 명령어:"
    echo "  init              초기 설정 (최초 1회)"
    echo "  up [services]     서비스 시작"
    echo "  down              서비스 중지"
    echo "  restart           서비스 재시작"
    echo "  logs [service]    로그 보기"
    echo "  ps                컨테이너 상태"
    echo "  clean             컨테이너 및 볼륨 삭제"
    echo "  status            전체 상태 확인"
    echo ""
    echo "데이터베이스 명령어:"
    echo "  db [master|slave1|slave2]  PostgreSQL 쉘 접속"
    echo "  redis                      Redis 쉘 접속"
    echo "  replication                Replication 상태 확인"
    echo ""
    echo "애플리케이션 명령어:"
    echo "  backend <start|build|test>   Backend 관리"
    echo "  frontend <start|build|test>  Frontend 관리"
    echo "  dev                          개발 환경 시작"
    echo ""
    echo "환경변수:"
    echo "  CONTAINER_RUNTIME  컨테이너 런타임 지정 (docker|podman)"
    echo ""
    echo "예시:"
    echo "  $0 init                # 초기 설정"
    echo "  $0 up                  # 모든 서비스 시작"
    echo "  $0 logs postgres-master  # 특정 서비스 로그"
    echo "  $0 db master           # PostgreSQL Master 접속"
    echo "  $0 backend start       # Backend 서버 시작"
}

# ============================================================
# 메인
# ============================================================

main() {
    init_runtime
    echo ""

    local command=${1:-help}
    shift 2>/dev/null || true

    case "$command" in
        init)       cmd_init "$@" ;;
        up)         cmd_up "$@" ;;
        down)       cmd_down "$@" ;;
        restart)    cmd_restart "$@" ;;
        logs)       cmd_logs "$@" ;;
        ps)         cmd_ps "$@" ;;
        clean)      cmd_clean "$@" ;;
        status)     cmd_status "$@" ;;
        db)         cmd_db_shell "$@" ;;
        redis)      cmd_redis_shell "$@" ;;
        replication) cmd_replication_status "$@" ;;
        backend)    cmd_backend "$@" ;;
        frontend)   cmd_frontend "$@" ;;
        dev)        cmd_dev "$@" ;;
        help|--help|-h)
                    cmd_help ;;
        *)
            print_error "알 수 없는 명령: $command"
            echo ""
            cmd_help
            exit 1
            ;;
    esac
}

main "$@"
