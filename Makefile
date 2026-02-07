# ============================================================
# PetPro Makefile
# Docker / Podman 자동 호환
# ============================================================

.PHONY: help init up down restart logs ps clean status \
        db-shell db-shell-slave1 db-shell-slave2 redis-shell \
        replication-status minio-ls \
        backend-start backend-build backend-test \
        frontend-start frontend-build frontend-test frontend-install \
        build test coverage dev \
        machine-start machine-stop machine-status _ensure-runtime

# ============================================================
# 컨테이너 런타임 자동 감지
# ============================================================

# Podman 우선, Docker fallback
CONTAINER_RUNTIME := $(shell command -v podman 2>/dev/null || command -v docker 2>/dev/null)
CONTAINER_CMD := $(notdir $(CONTAINER_RUNTIME))

# Compose 명령 감지
ifeq ($(CONTAINER_CMD),podman)
    # podman-compose 또는 podman compose
    COMPOSE_CHECK := $(shell command -v podman-compose 2>/dev/null)
    ifdef COMPOSE_CHECK
        COMPOSE_CMD := podman-compose
    else
        COMPOSE_CMD := podman compose
    endif
else
    # docker compose (v2) 또는 docker-compose (v1)
    COMPOSE_CHECK := $(shell docker compose version 2>/dev/null)
    ifdef COMPOSE_CHECK
        COMPOSE_CMD := docker compose
    else
        COMPOSE_CMD := docker-compose
    endif
endif

# ============================================================
# 기본 명령어
# ============================================================

help:
	@echo ""
	@echo "╔═══════════════════════════════════════════════════════════╗"
	@echo "║           PetPro - 반려동물 장례 토탈 플랫폼           ║"
	@echo "╚═══════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "런타임: $(CONTAINER_CMD) / $(COMPOSE_CMD)"
	@echo ""
	@echo "인프라 명령어:"
	@echo "  make init              - 초기 설정 (최초 1회)"
	@echo "  make up                - 모든 서비스 시작"
	@echo "  make down              - 모든 서비스 중지"
	@echo "  make restart           - 모든 서비스 재시작"
	@echo "  make logs              - 전체 로그 보기"
	@echo "  make logs-<service>    - 특정 서비스 로그"
	@echo "  make ps                - 컨테이너 상태"
	@echo "  make clean             - 컨테이너 및 볼륨 삭제"
	@echo "  make status            - 전체 상태 확인"
	@echo ""
	@echo "데이터베이스 명령어:"
	@echo "  make db-shell          - PostgreSQL Master 접속"
	@echo "  make db-shell-slave1   - PostgreSQL Slave1 접속"
	@echo "  make db-shell-slave2   - PostgreSQL Slave2 접속"
	@echo "  make redis-shell       - Redis 접속"
	@echo "  make replication-status - Replication 상태"
	@echo ""
	@echo "애플리케이션 명령어:"
	@echo "  make backend-start     - Backend 서버 시작"
	@echo "  make backend-build     - Backend 빌드"
	@echo "  make backend-test      - Backend 테스트"
	@echo "  make frontend-start    - Frontend 서버 시작"
	@echo "  make frontend-build    - Frontend 빌드"
	@echo "  make frontend-test     - Frontend 테스트"
	@echo "  make frontend-install  - Frontend 의존성 설치"
	@echo ""
	@echo "통합 명령어:"
	@echo "  make dev               - 개발 환경 시작 (인프라만)"
	@echo "  make build             - 전체 빌드"
	@echo "  make test              - 전체 테스트"
	@echo "  make coverage          - 테스트 커버리지"
	@echo ""

# ============================================================
# 인프라 명령어
# ============================================================

init:
	@echo "PetPro 초기화 중..."
	@chmod +x scripts/*.sh 2>/dev/null || true
	@chmod +x docker/postgres/*.sh 2>/dev/null || true
	@cp -n .env.example .env 2>/dev/null || true
	@echo ""
	@echo "초기화 완료!"
	@echo ""
	@echo "다음 단계:"
	@echo "  1. .env 파일을 편집하여 설정 변경"
	@echo "  2. 'make up' 실행하여 서비스 시작"

# Podman 머신 자동 시작 (macOS)
_ensure-runtime:
ifeq ($(CONTAINER_CMD),podman)
ifeq ($(shell uname),Darwin)
	@if ! podman info &>/dev/null; then \
		echo "Podman 머신 시작 중..."; \
		podman machine start 2>/dev/null || true; \
		echo "머신 시작 대기 중..."; \
		sleep 10; \
	fi
endif
endif

up: _ensure-runtime
	$(COMPOSE_CMD) up -d
	@echo ""
	@echo "서비스가 시작되었습니다!"
	@echo ""
	@echo "┌──────────────────────┬─────────────────┐"
	@echo "│ 서비스               │ 주소            │"
	@echo "├──────────────────────┼─────────────────┤"
	@echo "│ PostgreSQL Master    │ localhost:5432  │"
	@echo "│ PostgreSQL Slave1    │ localhost:5433  │"
	@echo "│ PostgreSQL Slave2    │ localhost:5434  │"
	@echo "│ Redis                │ localhost:6379  │"
	@echo "│ MinIO API            │ localhost:9000  │"
	@echo "│ MinIO Console        │ localhost:9001  │"
	@echo "│ Nginx                │ localhost:80    │"
	@echo "└──────────────────────┴─────────────────┘"

down:
	$(COMPOSE_CMD) down

restart:
	$(COMPOSE_CMD) restart

logs:
	$(COMPOSE_CMD) logs -f

logs-%:
	$(COMPOSE_CMD) logs -f $*

ps:
	$(COMPOSE_CMD) ps

clean:
	@echo "⚠️  모든 컨테이너와 볼륨을 삭제합니다."
	@read -p "계속하시겠습니까? (y/N): " confirm && \
		[ "$$confirm" = "y" ] || [ "$$confirm" = "Y" ] && \
		$(COMPOSE_CMD) down -v --remove-orphans && \
		echo "삭제 완료!" || echo "취소됨"

status:
	@echo ""
	@echo "=== 컨테이너 상태 ==="
	@$(COMPOSE_CMD) ps
	@echo ""
	@echo "=== Replication 상태 ==="
	@$(CONTAINER_CMD) exec petpro-postgres-master \
		psql -U petpro -d petpro \
		-c "SELECT client_addr, state, sent_lsn, write_lsn FROM pg_stat_replication;" \
		2>/dev/null || echo "PostgreSQL Master가 실행 중이 아닙니다."

# ============================================================
# 데이터베이스 명령어
# ============================================================

db-shell:
	$(CONTAINER_CMD) exec -it petpro-postgres-master psql -U petpro -d petpro

db-shell-slave1:
	$(CONTAINER_CMD) exec -it petpro-postgres-slave1 psql -U petpro -d petpro

db-shell-slave2:
	$(CONTAINER_CMD) exec -it petpro-postgres-slave2 psql -U petpro -d petpro

redis-shell:
	$(CONTAINER_CMD) exec -it petpro-redis redis-cli -a redis123!

minio-ls:
	$(CONTAINER_CMD) exec petpro-minio-init mc ls myminio/

replication-status:
	$(CONTAINER_CMD) exec -it petpro-postgres-master \
		psql -U petpro -d petpro -c "SELECT * FROM pg_stat_replication;"

# ============================================================
# Backend 명령어
# ============================================================

backend-start:
	cd backend && ./gradlew bootRun

backend-build:
	cd backend && ./gradlew build -x test

backend-test:
	cd backend && ./gradlew test

# ============================================================
# Frontend 명령어
# ============================================================

frontend-start:
	cd frontend && npm start

frontend-build:
	cd frontend && npm run build

frontend-test:
	cd frontend && npm test

frontend-install:
	cd frontend && npm install

# ============================================================
# 통합 명령어
# ============================================================

dev: up
	@echo ""
	@echo "개발 환경이 준비되었습니다."
	@echo ""
	@echo "다음 명령으로 애플리케이션을 시작하세요:"
	@echo "  Backend:  make backend-start"
	@echo "  Frontend: make frontend-start"

build: backend-build frontend-build
	@echo ""
	@echo "전체 빌드 완료!"

test: backend-test frontend-test
	@echo ""
	@echo "전체 테스트 완료!"

coverage:
	cd backend && ./gradlew jacocoTestReport
	@echo ""
	@echo "커버리지 리포트: backend/build/reports/jacoco/test/html/index.html"

# ============================================================
# Podman 머신 관리 (macOS 전용)
# ============================================================

machine-start:
ifeq ($(CONTAINER_CMD),podman)
	@echo "Podman 머신 시작 중..."
	@podman machine start 2>/dev/null || echo "이미 실행 중이거나 머신이 없습니다."
else
	@echo "Docker 환경에서는 필요 없습니다."
endif

machine-stop:
ifeq ($(CONTAINER_CMD),podman)
	@echo "Podman 머신 중지 중..."
	@podman machine stop 2>/dev/null || echo "이미 중지되었거나 머신이 없습니다."
else
	@echo "Docker 환경에서는 필요 없습니다."
endif

machine-status:
ifeq ($(CONTAINER_CMD),podman)
	@podman machine list
else
	@echo "Docker 환경입니다."
	@docker info --format "{{.ServerVersion}}" 2>/dev/null || echo "Docker가 실행 중이 아닙니다."
endif
