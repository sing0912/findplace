#!/bin/bash

#===============================================================================
# FindPlace 설치 및 실행 스크립트
#
# 사용법:
#   ./setup.sh              # 전체 설치 및 실행 (로컬)
#   ./setup.sh install      # 필수 소프트웨어 설치만
#   ./setup.sh start        # 서비스 시작 (로컬)
#   ./setup.sh start --prod # 서비스 시작 (프로덕션, SSL 포함)
#   ./setup.sh stop         # 서비스 중지
#   ./setup.sh status       # 서비스 상태 확인
#   ./setup.sh restart      # 서비스 재시작 (로컬)
#   ./setup.sh restart --prod # 서비스 재시작 (프로덕션)
#   ./setup.sh clean        # 모든 데이터 삭제 및 초기화
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

# 프로덕션 모드 플래그
PROD_MODE=false

# 사용하는 포트 목록
REQUIRED_PORTS=(80 443 3000 5432 5433 5434 6379 8080 9000 9001)

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
    elif check_command docker-compose; then
        echo "docker-compose"
    elif check_command docker && docker compose version &> /dev/null; then
        echo "docker compose"
    else
        echo ""
    fi
}

#-------------------------------------------------------------------------------
# 포트 사용 확인 및 정리
#-------------------------------------------------------------------------------
kill_port_process() {
    local port=$1
    local pids=$(lsof -ti :$port 2>/dev/null || true)

    if [ -n "$pids" ]; then
        print_warning "포트 $port 사용 중인 프로세스 종료 중..."
        for pid in $pids; do
            kill -9 $pid 2>/dev/null || true
        done
        sleep 1
        print_success "포트 $port 해제됨"
    fi
}

check_and_free_ports() {
    print_info "필수 포트 확인 및 정리 중..."

    for port in "${REQUIRED_PORTS[@]}"; do
        kill_port_process $port
    done

    # 시스템 서비스 중지
    if check_command systemctl; then
        systemctl stop httpd 2>/dev/null || true
        systemctl stop apache2 2>/dev/null || true
        systemctl stop nginx 2>/dev/null || true
    fi

    print_success "포트 정리 완료"
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

    # 패키지 매니저 감지
    if check_command dnf; then
        PKG_MANAGER="dnf"
    elif check_command yum; then
        PKG_MANAGER="yum"
    elif check_command apt; then
        PKG_MANAGER="apt"
    else
        print_error "지원하지 않는 패키지 매니저입니다"
        exit 1
    fi

    # Java 21
    if ! check_command java || ! java -version 2>&1 | grep -q "21"; then
        print_info "Java 21 설치 중..."
        if [ "$PKG_MANAGER" = "apt" ]; then
            sudo apt update
            sudo apt install -y openjdk-21-jdk
        else
            sudo $PKG_MANAGER install -y java-21-openjdk-devel
        fi
    else
        print_success "Java 21 이미 설치됨"
    fi

    # Node.js
    if ! check_command node; then
        print_info "Node.js 설치 중..."
        if [ "$PKG_MANAGER" = "apt" ]; then
            curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
            sudo apt install -y nodejs
        else
            curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
            sudo $PKG_MANAGER install -y nodejs
        fi
    else
        print_success "Node.js 이미 설치됨: $(node -v)"
    fi

    # Docker
    if ! check_command docker; then
        print_info "Docker 설치 중..."
        if [ "$PKG_MANAGER" = "apt" ]; then
            sudo apt install -y docker.io
        else
            sudo $PKG_MANAGER install -y docker
        fi
        sudo systemctl start docker
        sudo systemctl enable docker
        sudo usermod -aG docker $USER 2>/dev/null || true
    else
        print_success "Docker 이미 설치됨"
        # Docker 서비스 시작 확인
        if ! systemctl is-active --quiet docker; then
            print_info "Docker 서비스 시작 중..."
            sudo systemctl start docker
        fi
    fi

    # docker-compose (별도 설치)
    if ! check_command docker-compose; then
        print_info "docker-compose 설치 중..."
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose

        if check_command docker-compose; then
            print_success "docker-compose 설치 완료: $(docker-compose --version)"
        else
            print_error "docker-compose 설치 실패"
            exit 1
        fi
    else
        print_success "docker-compose 이미 설치됨"
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
        sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk 2>/dev/null || true
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
        podman machine init 2>/dev/null || true
        podman machine start 2>/dev/null || true
    else
        print_success "컨테이너 런타임 이미 설치됨"
    fi
}

#-------------------------------------------------------------------------------
# 서비스 시작
#-------------------------------------------------------------------------------
start_services() {
    if [ "$PROD_MODE" = true ]; then
        print_header "FindPlace 서비스 시작 (프로덕션 모드)"
    else
        print_header "FindPlace 서비스 시작 (로컬 모드)"
    fi

    # 컨테이너 런타임 확인
    COMPOSE_CMD=$(get_compose_command)
    if [ -z "$COMPOSE_CMD" ]; then
        print_error "Docker 또는 Podman이 설치되어 있지 않습니다"
        print_info "먼저 ./setup.sh install 을 실행하세요"
        exit 1
    fi

    # 프로덕션 모드 시 SSL 인증서 확인
    if [ "$PROD_MODE" = true ]; then
        if [ ! -f "/etc/letsencrypt/live/dev.findplace.co.kr/fullchain.pem" ]; then
            print_error "SSL 인증서가 없습니다. 먼저 인증서를 발급하세요:"
            print_info "  certbot certonly --standalone -d dev.findplace.co.kr"
            exit 1
        fi
        # certbot 갱신용 디렉토리 생성
        sudo mkdir -p /var/www/certbot
        print_success "SSL 인증서 확인 완료"
    fi

    # 포트 정리
    check_and_free_ports

    # 중복 compose 파일 정리
    if [ -f "$PROJECT_ROOT/compose.yaml" ] && [ -f "$PROJECT_ROOT/docker-compose.yml" ]; then
        print_warning "중복 compose 파일 발견, compose.yaml 삭제..."
        rm -f "$PROJECT_ROOT/compose.yaml"
    fi

    # 기존 컨테이너 정리
    print_info "기존 컨테이너 정리 중..."
    cd "$PROJECT_ROOT"
    $COMPOSE_CMD down --remove-orphans 2>/dev/null || true

    # 1. 인프라 서비스 시작
    print_info "인프라 서비스 시작 중..."
    cd "$PROJECT_ROOT"
    if [ "$PROD_MODE" = true ]; then
        $COMPOSE_CMD -f docker-compose.yml -f docker-compose.prod.yml up -d
    else
        $COMPOSE_CMD up -d
    fi

    # 인프라 준비 대기
    print_info "인프라 준비 대기 중 (40초)..."
    sleep 40

    # 인프라 상태 확인
    check_infrastructure

    # 2. 백엔드 시작
    print_info "백엔드 빌드 및 시작 중..."

    # 기존 백엔드 프로세스 종료
    pkill -f "gradlew.*bootRun" 2>/dev/null || true
    pkill -f "java.*findplace" 2>/dev/null || true
    sleep 2

    cd "$PROJECT_ROOT/backend"
    chmod +x gradlew

    # Gradle 빌드
    print_info "Gradle 빌드 중..."
    ./gradlew build -x test --no-daemon 2>&1 | tail -20

    # 백엔드 실행
    print_info "백엔드 서버 시작 중..."
    nohup ./gradlew bootRun --no-daemon > /tmp/findplace-backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > /tmp/findplace-backend.pid
    cd "$PROJECT_ROOT"

    # 백엔드 준비 대기
    print_info "백엔드 준비 대기 중 (30초)..."
    sleep 30

    # 백엔드 상태 확인
    for i in {1..10}; do
        if curl -s http://localhost:8080/api/actuator/health 2>/dev/null | grep -q "UP"; then
            print_success "백엔드 시작 완료"
            break
        fi
        if [ $i -eq 10 ]; then
            print_warning "백엔드 상태 확인 필요 - 로그: /tmp/findplace-backend.log"
        fi
        sleep 3
    done

    # 3. 프론트엔드 시작
    print_info "프론트엔드 설치 및 시작 중..."

    # 기존 프론트엔드 프로세스 종료
    pkill -f "react-scripts" 2>/dev/null || true
    pkill -f "node.*start" 2>/dev/null || true
    sleep 2

    cd "$PROJECT_ROOT/frontend"
    npm install
    nohup npm start > /tmp/findplace-frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > /tmp/findplace-frontend.pid
    cd "$PROJECT_ROOT"

    # 프론트엔드 준비 대기
    print_info "프론트엔드 준비 대기 중 (15초)..."
    sleep 15

    # 완료 메시지
    print_header "서비스 시작 완료"
    echo ""
    if [ "$PROD_MODE" = true ]; then
        echo -e "  ${GREEN}프론트엔드${NC}: https://dev.findplace.co.kr"
        echo -e "  ${GREEN}백엔드 API${NC}: https://dev.findplace.co.kr/api"
        echo -e "  ${GREEN}Swagger UI${NC}: https://dev.findplace.co.kr/api/swagger-ui.html"
    else
        echo -e "  ${GREEN}프론트엔드${NC}: http://localhost:3000"
        echo -e "  ${GREEN}백엔드 API${NC}: http://localhost:8080/api"
        echo -e "  ${GREEN}Swagger UI${NC}: http://localhost:8080/api/swagger-ui.html"
    fi
    echo -e "  ${GREEN}MinIO Console${NC}: http://localhost:9001 (minioadmin / minioadmin123!)"
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
        rm -f /tmp/findplace-frontend.pid
    fi
    pkill -f "react-scripts" 2>/dev/null || true
    pkill -f "node.*start" 2>/dev/null || true

    # 백엔드 중지
    print_info "백엔드 중지 중..."
    if [ -f /tmp/findplace-backend.pid ]; then
        kill $(cat /tmp/findplace-backend.pid) 2>/dev/null || true
        rm -f /tmp/findplace-backend.pid
    fi
    pkill -f "gradlew.*bootRun" 2>/dev/null || true
    pkill -f "java.*findplace" 2>/dev/null || true

    # 인프라 중지
    print_info "인프라 서비스 중지 중..."
    COMPOSE_CMD=$(get_compose_command)
    if [ -n "$COMPOSE_CMD" ]; then
        cd "$PROJECT_ROOT"
        $COMPOSE_CMD down --remove-orphans 2>/dev/null || true
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
        cd "$PROJECT_ROOT"
        $COMPOSE_CMD ps
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
# 전체 초기화 (데이터 삭제)
#-------------------------------------------------------------------------------
clean_all() {
    print_header "FindPlace 전체 초기화"

    print_warning "모든 데이터가 삭제됩니다!"
    read -p "계속하시겠습니까? (y/N): " confirm

    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        print_info "취소되었습니다"
        exit 0
    fi

    # 서비스 중지
    stop_services

    # Docker 볼륨 삭제
    COMPOSE_CMD=$(get_compose_command)
    if [ -n "$COMPOSE_CMD" ]; then
        cd "$PROJECT_ROOT"
        $COMPOSE_CMD down -v 2>/dev/null || true
    fi

    # 빌드 파일 삭제
    rm -rf "$PROJECT_ROOT/backend/build" 2>/dev/null || true
    rm -rf "$PROJECT_ROOT/frontend/node_modules" 2>/dev/null || true
    rm -rf "$PROJECT_ROOT/frontend/build" 2>/dev/null || true

    # 로그 파일 삭제
    rm -f /tmp/findplace-*.log 2>/dev/null || true
    rm -f /tmp/findplace-*.pid 2>/dev/null || true

    print_success "초기화 완료"
}

#-------------------------------------------------------------------------------
# 메인 로직
#-------------------------------------------------------------------------------
main() {
    # --prod 옵션 확인
    for arg in "$@"; do
        if [ "$arg" = "--prod" ]; then
            PROD_MODE=true
        fi
    done

    # 첫 번째 인자 (명령어) 추출
    CMD="${1:-}"
    if [ "$CMD" = "--prod" ]; then
        CMD="${2:-}"
    fi

    case "$CMD" in
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
        clean)
            clean_all
            ;;
        ""|all)
            install_dependencies
            start_services
            ;;
        *)
            echo "사용법: $0 {install|start|stop|status|restart|clean|all} [--prod]"
            echo ""
            echo "  install  - 필수 소프트웨어 설치"
            echo "  start    - 서비스 시작"
            echo "  stop     - 서비스 중지"
            echo "  status   - 서비스 상태 확인"
            echo "  restart  - 서비스 재시작"
            echo "  clean    - 모든 데이터 삭제 및 초기화"
            echo "  all      - 전체 설치 및 시작 (기본값)"
            echo ""
            echo "옵션:"
            echo "  --prod   - 프로덕션 모드 (SSL/HTTPS 활성화)"
            echo ""
            echo "예시:"
            echo "  $0 start          # 로컬 개발 모드로 시작"
            echo "  $0 start --prod   # 프로덕션 모드로 시작 (SSL)"
            echo "  $0 restart --prod # 프로덕션 모드로 재시작"
            exit 1
            ;;
    esac
}

main "$@"
