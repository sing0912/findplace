#!/bin/bash
# ============================================================
# PetPro Backend & Frontend 재시작 스크립트
# 인프라(DB, Redis, MinIO)는 유지하고 앱만 재시작
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

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ============================================================
# 프로세스 종료 함수
# ============================================================

kill_backend() {
    print_info "Backend 프로세스 종료 중..."

    # bootRun 프로세스 찾기
    local pids=$(pgrep -f "bootRun|petpro-backend" 2>/dev/null || true)

    if [ -n "$pids" ]; then
        echo "$pids" | xargs kill -9 2>/dev/null || true
        print_success "Backend 프로세스 종료됨"
    else
        print_warning "실행 중인 Backend 프로세스 없음"
    fi

    # 포트 8080 사용 중인 프로세스 확인
    local port_pid=$(lsof -ti:8080 2>/dev/null || true)
    if [ -n "$port_pid" ]; then
        print_info "포트 8080 사용 중인 프로세스 종료..."
        echo "$port_pid" | xargs kill -9 2>/dev/null || true
    fi
}

kill_frontend() {
    print_info "Frontend 프로세스 종료 중..."

    # react-scripts 프로세스 찾기
    local pids=$(pgrep -f "react-scripts|node.*frontend" 2>/dev/null || true)

    if [ -n "$pids" ]; then
        echo "$pids" | xargs kill -9 2>/dev/null || true
        print_success "Frontend 프로세스 종료됨"
    else
        print_warning "실행 중인 Frontend 프로세스 없음"
    fi

    # 포트 3000 사용 중인 프로세스 확인
    local port_pid=$(lsof -ti:3000 2>/dev/null || true)
    if [ -n "$port_pid" ]; then
        print_info "포트 3000 사용 중인 프로세스 종료..."
        echo "$port_pid" | xargs kill -9 2>/dev/null || true
    fi
}

# ============================================================
# 시작 함수
# ============================================================

start_backend() {
    print_info "Backend 시작 중... (백그라운드)"
    cd "$PROJECT_ROOT/backend"

    # 로그 파일 경로
    local log_file="$PROJECT_ROOT/logs/backend.log"
    mkdir -p "$PROJECT_ROOT/logs"

    # 백그라운드로 실행
    nohup ./gradlew bootRun > "$log_file" 2>&1 &
    local pid=$!

    print_success "Backend 시작됨 (PID: $pid)"
    print_info "로그: tail -f $log_file"
}

start_frontend() {
    print_info "Frontend 시작 중... (백그라운드)"
    cd "$PROJECT_ROOT/frontend"

    # 로그 파일 경로
    local log_file="$PROJECT_ROOT/logs/frontend.log"
    mkdir -p "$PROJECT_ROOT/logs"

    # 백그라운드로 실행
    nohup npm start > "$log_file" 2>&1 &
    local pid=$!

    print_success "Frontend 시작됨 (PID: $pid)"
    print_info "로그: tail -f $log_file"
}

# ============================================================
# 상태 확인
# ============================================================

check_status() {
    echo ""
    print_info "=== 애플리케이션 상태 ==="

    # Backend 상태
    if lsof -ti:8080 >/dev/null 2>&1; then
        echo -e "Backend (8080):  ${GREEN}실행 중${NC}"
    else
        echo -e "Backend (8080):  ${RED}중지됨${NC}"
    fi

    # Frontend 상태
    if lsof -ti:3000 >/dev/null 2>&1; then
        echo -e "Frontend (3000): ${GREEN}실행 중${NC}"
    else
        echo -e "Frontend (3000): ${RED}중지됨${NC}"
    fi
}

# ============================================================
# 메인
# ============================================================

show_help() {
    echo "사용법: $0 [command]"
    echo ""
    echo "명령어:"
    echo "  all       Backend + Frontend 모두 재시작 (기본값)"
    echo "  backend   Backend만 재시작"
    echo "  frontend  Frontend만 재시작"
    echo "  stop      Backend + Frontend 모두 중지"
    echo "  status    상태 확인"
    echo ""
    echo "예시:"
    echo "  $0           # 전체 재시작"
    echo "  $0 backend   # Backend만 재시작"
    echo "  $0 stop      # 전체 중지"
}

main() {
    local command=${1:-all}

    case "$command" in
        all)
            kill_backend
            kill_frontend
            sleep 2
            start_backend
            start_frontend
            sleep 3
            check_status
            echo ""
            print_success "재시작 완료!"
            print_info "Backend 준비까지 10-15초 소요됩니다."
            ;;
        backend)
            kill_backend
            sleep 2
            start_backend
            ;;
        frontend)
            kill_frontend
            sleep 1
            start_frontend
            ;;
        stop)
            kill_backend
            kill_frontend
            print_success "모든 애플리케이션 중지됨"
            ;;
        status)
            check_status
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "알 수 없는 명령: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
