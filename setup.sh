#!/bin/bash

#===============================================================================
# FindPlace 설치 및 실행 스크립트
#
# 사용법:
#   ./setup.sh              # 전체 설치 및 실행
#   ./setup.sh install      # 필수 소프트웨어 설치만
#   ./setup.sh start        # 서비스 시작만
#   ./setup.sh stop         # 서비스 중지
#   ./setup.sh status       # 서비스 상태 확인
#===============================================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 프로젝트 루트 디렉토리
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

#-------------------------------------------------------------------------------
# 유틸리티 함수
#-------------------------------------------------------------------------------
print_header() {
    echo ""
    echo -e "${BLUE}================================================================${NC}"
    echo -e "${BLUE} $1${NC}"
    echo -e "${BLUE}================================================================${NC}"
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

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# 명령어 존재 여부 확인
check_command() {
    if command -v "$1" &> /dev/null; then
        return 0
    else
        return 1
    fi
}

# Docker 또는 Podman 확인
get_container_runtime() {
    if check_command podman; then
        echo "podman"
    elif check_command docker; then
        echo "docker"
    else
        echo ""
    fi
}

get_compose_command() {
    if check_command podman-compose; then
        echo "podman-compose"
    elif check_command "docker-compose"; then
        echo "docker-compose"
    elif check_command docker && docker compose version &> /dev/null; then
        echo "docker compose"
    else
        echo ""
    fi
}

#-------------------------------------------------------------------------------
# 필수 소프트웨어 설치
#-------------------------------------------------------------------------------
install_dependencies() {
    print_header "필수 소프트웨어 설치"

    # OS 감지
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        install_linux
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        install_macos
    else
        print_error "지원하지 않는 OS입니다: $OSTYPE"
        exit 1
    fi
}

install_linux() {
    print_info "Linux 환경 감지됨"

    # 패키지 매니저 업데이트
    if check_command apt; then
        sudo apt update

        # Java 21
        if ! check_command java || ! java -version 2>&1 | grep -q "21"; then
            print_info "Java 21 설치 중..."
            sudo apt install -y openjdk-21-jdk
        else
            print_success "Java 21 이미 설치됨"
        fi

        # Node.js
        if ! check_command node; then
            print_info "Node.js 설치 중..."
            curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
            sudo apt install -y nodejs
        else
            print_success "Node.js 이미 설치됨: $(node -v)"
        fi

        # Docker
        if ! check_command docker; then
            print_info "Docker 설치 중..."
            sudo apt install -y docker.io docker-compose
            sudo usermod -aG docker $USER
            print_warning "Docker 그룹 적용을 위해 재로그인이 필요할 수 있습니다"
        else
            print_success "Docker 이미 설치됨"
        fi

    elif check_command yum; then
        # RHEL/CentOS
        print_info "Java 21 설치 중..."
        sudo yum install -y java-21-openjdk-devel

        print_info "Node.js 설치 중..."
        curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
        sudo yum install -y nodejs

        print_info "Docker 설치 중..."
        sudo yum install -y docker docker-compose
        sudo systemctl start docker
        sudo systemctl enable docker
        sudo usermod -aG docker $USER
    else
        print_error "지원하지 않는 패키지 매니저입니다"
        exit 1
    fi
}

install_macos() {
    print_info "macOS 환경 감지됨"

    # Homebrew 확인
    if ! check_command brew; then
        print_info "Homebrew 설치 중..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi

    # Java 21
    if ! check_command java || ! java -version 2>&1 | grep -q "21"; then
        print_info "Java 21 설치 중..."
        brew install openjdk@21
        sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
    else
        print_success "Java 21 이미 설치됨"
    fi

    # Node.js
    if ! check_command node; then
        print_info "Node.js 설치 중..."
        brew install node
    else
        print_success "Node.js 이미 설치됨: $(node -v)"
    fi

    # Docker 또는 Podman
    if ! check_command docker && ! check_command podman; then
        print_info "Podman 설치 중..."
        brew install podman podman-compose
        podman machine init
        podman machine start
    else
        print_success "컨테이너 런타임 이미 설치됨"
    fi
}

#-------------------------------------------------------------------------------
# 서비스 시작
#-------------------------------------------------------------------------------
start_services() {
    print_header "FindPlace 서비스 시작"

    # 컨테이너 런타임 확인
    COMPOSE_CMD=$(get_compose_command)
    if [ -z "$COMPOSE_CMD" ]; then
        print_error "Docker 또는 Podman이 설치되어 있지 않습니다"
        exit 1
    fi

    # 1. 인프라 서비스 시작
    print_info "인프라 서비스 시작 중..."
    cd "$PROJECT_ROOT/infra"
    $COMPOSE_CMD up -d
    cd "$PROJECT_ROOT"

    # 인프라 준비 대기
    print_info "인프라 준비 대기 중 (30초)..."
    sleep 30

    # 인프라 상태 확인
    check_infrastructure

    # 2. 백엔드 시작
    print_info "백엔드 빌드 및 시작 중..."
    cd "$PROJECT_ROOT/backend"
    chmod +x gradlew
    ./gradlew build -x test
    ./gradlew bootRun > /tmp/findplace-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/findplace-backend.pid
    cd "$PROJECT_ROOT"

    # 백엔드 준비 대기
    print_info "백엔드 준비 대기 중 (20초)..."
    sleep 20

    # 백엔드 상태 확인
    if curl -s http://localhost:8080/api/actuator/health | grep -q "UP"; then
        print_success "백엔드 시작 완료"
    else
        print_warning "백엔드 상태 확인 필요 - 로그: /tmp/findplace-backend.log"
    fi

    # 3. 프론트엔드 시작
    print_info "프론트엔드 설치 및 시작 중..."
    cd "$PROJECT_ROOT/frontend"
    npm install
    npm start > /tmp/findplace-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/findplace-frontend.pid
    cd "$PROJECT_ROOT"

    # 완료 메시지
    print_header "서비스 시작 완료"
    echo ""
    echo -e "  ${GREEN}프론트엔드${NC}: http://localhost:3000"
    echo -e "  ${GREEN}백엔드 API${NC}: http://localhost:8080/api"
    echo -e "  ${GREEN}Swagger UI${NC}: http://localhost:8080/api/swagger-ui.html"
    echo -e "  ${GREEN}MinIO Console${NC}: http://localhost:9001"
    echo ""
    echo -e "  로그 확인:"
    echo -e "    백엔드: tail -f /tmp/findplace-backend.log"
    echo -e "    프론트엔드: tail -f /tmp/findplace-frontend.log"
    echo ""
}

#-------------------------------------------------------------------------------
# 인프라 상태 확인
#-------------------------------------------------------------------------------
check_infrastructure() {
    RUNTIME=$(get_container_runtime)

    print_info "인프라 상태 확인 중..."

    # PostgreSQL Master
    if $RUNTIME exec findplace-postgres-master pg_isready -U findplace &> /dev/null; then
        print_success "PostgreSQL Master: 정상"
    else
        print_warning "PostgreSQL Master: 확인 필요"
    fi

    # Redis
    if $RUNTIME exec findplace-redis redis-cli -a "redis123!" ping 2>/dev/null | grep -q "PONG"; then
        print_success "Redis: 정상"
    else
        print_warning "Redis: 확인 필요"
    fi

    # MinIO
    if curl -s http://localhost:9000/minio/health/live &> /dev/null; then
        print_success "MinIO: 정상"
    else
        print_warning "MinIO: 확인 필요"
    fi
}

#-------------------------------------------------------------------------------
# 서비스 중지
#-------------------------------------------------------------------------------
stop_services() {
    print_header "FindPlace 서비스 중지"

    # 프론트엔드 중지
    print_info "프론트엔드 중지 중..."
    if [ -f /tmp/findplace-frontend.pid ]; then
        kill $(cat /tmp/findplace-frontend.pid) 2>/dev/null || true
        rm /tmp/findplace-frontend.pid
    fi
    pkill -f "react-scripts" 2>/dev/null || true

    # 백엔드 중지
    print_info "백엔드 중지 중..."
    if [ -f /tmp/findplace-backend.pid ]; then
        kill $(cat /tmp/findplace-backend.pid) 2>/dev/null || true
        rm /tmp/findplace-backend.pid
    fi
    pkill -f "gradlew.*bootRun" 2>/dev/null || true
    pkill -f "java.*findplace" 2>/dev/null || true

    # 인프라 중지
    print_info "인프라 서비스 중지 중..."
    COMPOSE_CMD=$(get_compose_command)
    if [ -n "$COMPOSE_CMD" ]; then
        cd "$PROJECT_ROOT/infra"
        $COMPOSE_CMD down
        cd "$PROJECT_ROOT"
    fi

    print_success "모든 서비스 중지 완료"
}

#-------------------------------------------------------------------------------
# 서비스 상태 확인
#-------------------------------------------------------------------------------
check_status() {
    print_header "FindPlace 서비스 상태"

    RUNTIME=$(get_container_runtime)
    COMPOSE_CMD=$(get_compose_command)

    # 인프라 상태
    echo ""
    echo -e "${BLUE}[인프라 서비스]${NC}"
    if [ -n "$COMPOSE_CMD" ]; then
        cd "$PROJECT_ROOT/infra"
        $COMPOSE_CMD ps
        cd "$PROJECT_ROOT"
    fi

    # 백엔드 상태
    echo ""
    echo -e "${BLUE}[백엔드]${NC}"
    if curl -s http://localhost:8080/api/actuator/health 2>/dev/null | grep -q "UP"; then
        print_success "백엔드: 실행 중 (http://localhost:8080/api)"
    else
        print_warning "백엔드: 중지됨"
    fi

    # 프론트엔드 상태
    echo ""
    echo -e "${BLUE}[프론트엔드]${NC}"
    if curl -s http://localhost:3000 &>/dev/null; then
        print_success "프론트엔드: 실행 중 (http://localhost:3000)"
    else
        print_warning "프론트엔드: 중지됨"
    fi

    echo ""
}

#-------------------------------------------------------------------------------
# 메인 로직
#-------------------------------------------------------------------------------
main() {
    case "${1:-}" in
        install)
            install_dependencies
            ;;
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        status)
            check_status
            ;;
        restart)
            stop_services
            sleep 3
            start_services
            ;;
        ""|all)
            install_dependencies
            start_services
            ;;
        *)
            echo "사용법: $0 {install|start|stop|status|restart|all}"
            echo ""
            echo "  install  - 필수 소프트웨어 설치"
            echo "  start    - 서비스 시작"
            echo "  stop     - 서비스 중지"
            echo "  status   - 서비스 상태 확인"
            echo "  restart  - 서비스 재시작"
            echo "  all      - 전체 설치 및 시작 (기본값)"
            exit 1
            ;;
    esac
}

main "$@"
