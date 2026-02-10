#!/bin/bash
#===============================================================================
# PetPro 방화벽 보안 설정 스크립트
#
# 서버 배포/시작 시 호출하여 방화벽 규칙을 보안 정책에 맞게 설정합니다.
#
# 보안 원칙:
#   - 외부 노출: 80(HTTP), 443(HTTPS) 만 허용
#   - 내부 전용: DB, Redis, MinIO, 모니터링 등은 방화벽에서 차단
#   - docker-compose.yml에서 127.0.0.1 바인딩과 이중 방어
#
# 사용법:
#   ./scripts/secure-firewall.sh          # 방화벽 설정 적용
#   ./scripts/secure-firewall.sh --check  # 현재 상태만 확인
#   ./scripts/secure-firewall.sh --help   # 도움말
#
# 다른 스크립트에서 사용:
#   source scripts/secure-firewall.sh
#   apply_firewall_security    # 방화벽 보안 적용
#   check_firewall_security    # 방화벽 상태 확인
#===============================================================================

# 색상 정의 (이미 정의되어 있으면 건너뜀)
RED="${RED:-\033[0;31m}"
GREEN="${GREEN:-\033[0;32m}"
YELLOW="${YELLOW:-\033[1;33m}"
BLUE="${BLUE:-\033[0;34m}"
NC="${NC:-\033[0m}"

# 외부에 공개해야 하는 포트 (Nginx만)
ALLOWED_EXTERNAL_PORTS=(80 443)

# 내부 전용 포트 (절대 외부에 열면 안 됨)
INTERNAL_ONLY_PORTS=(
    "5432:PostgreSQL-Master"
    "5433:PostgreSQL-Slave1"
    "5434:PostgreSQL-Slave2"
    "5435:PostgreSQL-Coupon"
    "3306:MySQL-Log-Master"
    "3307:MySQL-Log-Slave"
    "6379:Redis"
    "9000:MinIO-API"
    "9001:MinIO-Console"
    "9090:Prometheus"
    "3001:Grafana"
    "3100:Loki"
    "3200:Tempo-HTTP"
    "4317:Tempo-OTLP-gRPC"
    "4318:Tempo-OTLP-HTTP"
    "9411:Tempo-Zipkin"
    "8080:Backend-API"
    "3000:Frontend-Dev"
)

#-------------------------------------------------------------------------------
# 방화벽 유형 감지
#-------------------------------------------------------------------------------
detect_firewall() {
    if command -v firewall-cmd &>/dev/null && systemctl is-active --quiet firewalld 2>/dev/null; then
        echo "firewalld"
    elif command -v ufw &>/dev/null; then
        echo "ufw"
    elif command -v iptables &>/dev/null; then
        echo "iptables"
    else
        echo "none"
    fi
}

#-------------------------------------------------------------------------------
# 방화벽 보안 상태 확인
#-------------------------------------------------------------------------------
check_firewall_security() {
    echo -e "${BLUE}[보안]${NC} 방화벽 포트 보안 점검 중..."

    local fw_type
    fw_type=$(detect_firewall)
    local issues=0

    case "$fw_type" in
        firewalld)
            echo -e "${BLUE}[보안]${NC} 방화벽: firewalld"
            local open_ports
            open_ports=$(firewall-cmd --list-ports 2>/dev/null || echo "")
            local open_services
            open_services=$(firewall-cmd --list-services 2>/dev/null || echo "")

            for entry in "${INTERNAL_ONLY_PORTS[@]}"; do
                local port="${entry%%:*}"
                local name="${entry#*:}"
                if echo "$open_ports" | grep -q "${port}/tcp"; then
                    echo -e "  ${RED}[위험]${NC} ${name} (${port}/tcp) - 외부에 노출됨"
                    issues=$((issues + 1))
                fi
            done

            for port in "${ALLOWED_EXTERNAL_PORTS[@]}"; do
                if echo "$open_ports" | grep -q "${port}/tcp" || echo "$open_services" | grep -qE "^https?$"; then
                    echo -e "  ${GREEN}[정상]${NC} 포트 ${port}/tcp - 외부 허용 (정상)"
                else
                    echo -e "  ${YELLOW}[경고]${NC} 포트 ${port}/tcp - 외부 미허용 (서비스 접속 불가)"
                fi
            done
            ;;
        ufw)
            echo -e "${BLUE}[보안]${NC} 방화벽: ufw"
            local ufw_status
            ufw_status=$(sudo ufw status 2>/dev/null || echo "inactive")

            if echo "$ufw_status" | grep -q "inactive"; then
                echo -e "  ${RED}[위험]${NC} UFW가 비활성 상태입니다"
                issues=$((issues + 1))
            else
                for entry in "${INTERNAL_ONLY_PORTS[@]}"; do
                    local port="${entry%%:*}"
                    local name="${entry#*:}"
                    if echo "$ufw_status" | grep -q "${port}.*ALLOW.*Anywhere"; then
                        echo -e "  ${RED}[위험]${NC} ${name} (${port}) - 외부에 노출됨"
                        issues=$((issues + 1))
                    fi
                done
            fi
            ;;
        iptables)
            echo -e "${BLUE}[보안]${NC} 방화벽: iptables"
            echo -e "  ${YELLOW}[경고]${NC} iptables 규칙은 수동 점검이 필요합니다"
            ;;
        none)
            echo -e "  ${RED}[위험]${NC} 방화벽이 감지되지 않았습니다!"
            issues=$((issues + 1))
            ;;
    esac

    # docker-compose 127.0.0.1 바인딩 확인
    local compose_file
    compose_file="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/docker-compose.yml"
    if [ -f "$compose_file" ]; then
        echo ""
        echo -e "${BLUE}[보안]${NC} Docker 포트 바인딩 점검 중..."
        local unsafe_ports
        unsafe_ports=$(grep -E '^\s+- "[0-9]+:[0-9]+"' "$compose_file" | grep -v '127\.0\.0\.1' | grep -v '#' || true)
        if [ -n "$unsafe_ports" ]; then
            echo -e "  ${RED}[위험]${NC} 0.0.0.0에 바인딩된 포트 발견:"
            echo "$unsafe_ports" | while read -r line; do
                echo -e "    ${RED}→${NC} $line"
            done
            issues=$((issues + 1))
        else
            # 80, 443은 0.0.0.0 허용
            local unsafe_non_web
            unsafe_non_web=$(grep -E '^\s+- "[0-9]+:[0-9]+"' "$compose_file" | grep -v '127\.0\.0\.1' | grep -v '"80:' | grep -v '"443:' | grep -v '#' || true)
            if [ -n "$unsafe_non_web" ]; then
                echo -e "  ${YELLOW}[경고]${NC} Nginx 외 0.0.0.0 바인딩 포트:"
                echo "$unsafe_non_web"
                issues=$((issues + 1))
            else
                echo -e "  ${GREEN}[정상]${NC} 모든 내부 포트가 127.0.0.1에 바인딩됨"
            fi
        fi
    fi

    echo ""
    if [ $issues -eq 0 ]; then
        echo -e "${GREEN}[보안]${NC} 포트 보안 점검 통과 (문제 없음)"
    else
        echo -e "${RED}[보안]${NC} 포트 보안 문제 ${issues}건 발견"
    fi

    return $issues
}

#-------------------------------------------------------------------------------
# 방화벽 보안 적용
#-------------------------------------------------------------------------------
apply_firewall_security() {
    echo -e "${BLUE}[보안]${NC} 방화벽 보안 설정 적용 중..."

    local fw_type
    fw_type=$(detect_firewall)

    case "$fw_type" in
        firewalld)
            _apply_firewalld
            ;;
        ufw)
            _apply_ufw
            ;;
        iptables)
            echo -e "${YELLOW}[경고]${NC} iptables는 자동 설정을 지원하지 않습니다."
            echo -e "${YELLOW}[경고]${NC} 수동으로 80, 443만 허용하고 나머지를 차단해주세요."
            return 1
            ;;
        none)
            echo -e "${YELLOW}[경고]${NC} 방화벽이 설치되어 있지 않습니다."

            # Linux에서만 방화벽 설치 시도
            if [[ "$OSTYPE" == "linux-gnu"* ]]; then
                echo -e "${BLUE}[보안]${NC} firewalld 설치 시도 중..."
                if command -v dnf &>/dev/null; then
                    sudo dnf install -y firewalld && sudo systemctl enable --now firewalld
                elif command -v yum &>/dev/null; then
                    sudo yum install -y firewalld && sudo systemctl enable --now firewalld
                elif command -v apt &>/dev/null; then
                    sudo apt install -y ufw
                    _apply_ufw
                    return $?
                else
                    echo -e "${RED}[오류]${NC} 방화벽을 자동 설치할 수 없습니다."
                    return 1
                fi
                _apply_firewalld
            else
                echo -e "${YELLOW}[경고]${NC} 로컬 개발 환경에서는 방화벽 설정을 건너뜁니다."
            fi
            ;;
    esac
}

#-------------------------------------------------------------------------------
# firewalld 보안 적용
#-------------------------------------------------------------------------------
_apply_firewalld() {
    echo -e "${BLUE}[보안]${NC} firewalld 규칙 적용 중..."

    # 1. 불필요하게 열린 내부 포트 제거
    for entry in "${INTERNAL_ONLY_PORTS[@]}"; do
        local port="${entry%%:*}"
        local name="${entry#*:}"
        if firewall-cmd --query-port="${port}/tcp" &>/dev/null; then
            echo -e "  ${YELLOW}→${NC} ${name} (${port}/tcp) 외부 접근 차단 중..."
            sudo firewall-cmd --permanent --remove-port="${port}/tcp" 2>/dev/null || true
        fi
    done

    # 2. 필수 포트 허용
    for port in "${ALLOWED_EXTERNAL_PORTS[@]}"; do
        if ! firewall-cmd --query-port="${port}/tcp" &>/dev/null; then
            echo -e "  ${GREEN}→${NC} 포트 ${port}/tcp 허용..."
            sudo firewall-cmd --permanent --add-port="${port}/tcp"
        fi
    done

    # 3. 적용
    sudo firewall-cmd --reload
    echo -e "${GREEN}[보안]${NC} firewalld 규칙 적용 완료"

    # 4. 검증
    check_firewall_security
}

#-------------------------------------------------------------------------------
# ufw 보안 적용
#-------------------------------------------------------------------------------
_apply_ufw() {
    echo -e "${BLUE}[보안]${NC} UFW 규칙 적용 중..."

    # 1. 기본 정책: 수신 거부, 발신 허용
    sudo ufw default deny incoming
    sudo ufw default allow outgoing

    # 2. 불필요하게 열린 내부 포트 제거
    for entry in "${INTERNAL_ONLY_PORTS[@]}"; do
        local port="${entry%%:*}"
        local name="${entry#*:}"
        sudo ufw delete allow "${port}/tcp" 2>/dev/null || true
    done

    # 3. 필수 포트 허용
    for port in "${ALLOWED_EXTERNAL_PORTS[@]}"; do
        sudo ufw allow "${port}/tcp"
    done

    # 4. SSH 허용 (잠기지 않도록)
    sudo ufw allow 22/tcp

    # 5. UFW 활성화
    echo "y" | sudo ufw enable 2>/dev/null || true
    echo -e "${GREEN}[보안]${NC} UFW 규칙 적용 완료"

    # 6. 검증
    check_firewall_security
}

#-------------------------------------------------------------------------------
# 직접 실행 시
#-------------------------------------------------------------------------------
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    case "${1:-}" in
        --check|-c)
            check_firewall_security
            ;;
        --help|-h)
            echo "사용법: $0 [옵션]"
            echo ""
            echo "  (없음)    방화벽 보안 설정 적용"
            echo "  --check   현재 방화벽 보안 상태 확인만"
            echo "  --help    이 도움말 표시"
            echo ""
            echo "보안 정책:"
            echo "  허용 포트: ${ALLOWED_EXTERNAL_PORTS[*]} (HTTP/HTTPS만)"
            echo "  차단 포트: DB, Redis, MinIO, 모니터링 등 모든 내부 서비스"
            ;;
        *)
            apply_firewall_security
            ;;
    esac
fi
