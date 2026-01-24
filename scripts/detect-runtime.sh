#!/bin/bash
# ============================================================
# Container Runtime Detection Script
# Docker / Podman 자동 감지
# ============================================================

# Podman 머신 상태 확인 및 시작 (macOS)
ensure_podman_machine() {
    # macOS가 아니면 스킵
    if [ "$(uname)" != "Darwin" ]; then
        return 0
    fi

    # Podman 머신 상태 확인
    local machine_status=$(podman machine list --format "{{.Name}}:{{.Running}}" 2>/dev/null | head -1)

    if [ -z "$machine_status" ]; then
        echo "Podman 머신이 없습니다. 'podman machine init'으로 생성하세요." >&2
        return 1
    fi

    local machine_name=$(echo "$machine_status" | cut -d: -f1)
    local is_running=$(echo "$machine_status" | cut -d: -f2)

    if [ "$is_running" != "true" ]; then
        echo "Podman 머신 시작 중... ($machine_name)" >&2
        podman machine start "$machine_name" >&2

        # 시작 대기
        local retry=0
        while [ $retry -lt 30 ]; do
            if podman info &>/dev/null; then
                echo "Podman 머신이 시작되었습니다." >&2
                return 0
            fi
            sleep 1
            retry=$((retry + 1))
        done

        echo "Podman 머신 시작 실패" >&2
        return 1
    fi

    return 0
}

detect_container_runtime() {
    # 1. 환경변수로 명시적 설정 확인
    if [ -n "$CONTAINER_RUNTIME" ]; then
        echo "$CONTAINER_RUNTIME"
        return
    fi

    # 2. Podman 확인 (우선순위)
    if command -v podman &> /dev/null; then
        # macOS에서 Podman 머신 확인
        if [ "$(uname)" == "Darwin" ]; then
            ensure_podman_machine || {
                # Podman 머신 시작 실패 시 Docker로 fallback
                if command -v docker &> /dev/null; then
                    echo "docker"
                    return
                fi
            }
        fi
        echo "podman"
        return
    fi

    # 3. Docker 확인
    if command -v docker &> /dev/null; then
        echo "docker"
        return
    fi

    # 4. 없으면 에러
    echo "none"
}

detect_compose_runtime() {
    local runtime=$(detect_container_runtime)

    case "$runtime" in
        podman)
            # podman-compose 확인
            if command -v podman-compose &> /dev/null; then
                echo "podman-compose"
            # podman compose (v4+) 확인
            elif podman compose version &> /dev/null; then
                echo "podman compose"
            else
                echo "none"
            fi
            ;;
        docker)
            # docker compose (v2) 확인
            if docker compose version &> /dev/null; then
                echo "docker compose"
            # docker-compose (v1) 확인
            elif command -v docker-compose &> /dev/null; then
                echo "docker-compose"
            else
                echo "none"
            fi
            ;;
        *)
            echo "none"
            ;;
    esac
}

# 직접 실행 시 감지 결과 출력
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    echo "Container Runtime: $(detect_container_runtime)"
    echo "Compose Runtime: $(detect_compose_runtime)"
fi
