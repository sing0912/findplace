# FindPlace 스크립트

Docker와 Podman을 자동으로 감지하여 호환되는 실행 스크립트입니다.

## 파일 구조

```
scripts/
├── README.md              # 이 파일
├── detect-runtime.sh      # 컨테이너 런타임 감지
├── run.sh                 # 메인 실행 스크립트
├── start-all.sh           # 전체 서비스 시작
└── stop-all.sh            # 전체 서비스 중지
```

## 사용법

### 기본 명령어 (run.sh)

```bash
# 도움말
./scripts/run.sh help

# 초기 설정 (최초 1회)
./scripts/run.sh init

# 인프라 서비스 시작
./scripts/run.sh up

# 인프라 서비스 중지
./scripts/run.sh down

# 상태 확인
./scripts/run.sh status

# 로그 보기
./scripts/run.sh logs
./scripts/run.sh logs postgres-master
```

### 데이터베이스 접속

```bash
# PostgreSQL Master
./scripts/run.sh db master

# PostgreSQL Slave
./scripts/run.sh db slave1
./scripts/run.sh db slave2

# Redis
./scripts/run.sh redis

# Replication 상태
./scripts/run.sh replication
```

### 애플리케이션 실행

```bash
# Backend
./scripts/run.sh backend start
./scripts/run.sh backend build
./scripts/run.sh backend test

# Frontend
./scripts/run.sh frontend start
./scripts/run.sh frontend build
./scripts/run.sh frontend test
./scripts/run.sh frontend install
```

### 전체 서비스 관리

```bash
# 전체 서비스 시작 (인프라 + Backend + Frontend)
./scripts/start-all.sh

# 전체 서비스 중지
./scripts/stop-all.sh
```

## Makefile 사용

대부분의 명령어는 Makefile로도 실행 가능합니다:

```bash
make help          # 도움말
make init          # 초기 설정
make up            # 인프라 시작
make down          # 인프라 중지
make dev           # 개발 환경 시작
make backend-start # Backend 시작
make frontend-start # Frontend 시작
```

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `CONTAINER_RUNTIME` | 컨테이너 런타임 강제 지정 | 자동 감지 |

```bash
# Docker 강제 사용
CONTAINER_RUNTIME=docker ./scripts/run.sh up

# Podman 강제 사용
CONTAINER_RUNTIME=podman ./scripts/run.sh up
```

## 런타임 지원

### Docker
- Docker Engine 20.10+
- Docker Compose V2 (권장) 또는 docker-compose V1

### Podman
- Podman 4.0+
- podman-compose 또는 podman compose

## 문제 해결

### Podman에서 host.docker.internal 사용

```bash
# /etc/hosts에 추가 (macOS/Linux)
sudo echo "127.0.0.1 host.containers.internal" >> /etc/hosts
```

### 권한 오류

```bash
# 스크립트 실행 권한 부여
chmod +x scripts/*.sh
```

### 포트 충돌

기본 포트가 사용 중인 경우 `.env` 파일에서 변경:

```env
DB_MASTER_PORT=5432
DB_SLAVE1_PORT=5433
DB_SLAVE2_PORT=5434
REDIS_PORT=6379
```
