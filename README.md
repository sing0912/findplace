# FindPlace - 반려동물 장례 토탈 플랫폼

반려동물 장례 서비스를 위한 통합 플랫폼입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 21, Spring Boot 3.2+ |
| Frontend | React 18, TypeScript 5 |
| Database | PostgreSQL 16 (Master 1 + Slave 2) |
| Cache | Redis 7 |
| Storage | MinIO (S3 호환) |
| Container | Docker, Docker Compose |

## 시작하기

### 사전 요구사항

- Docker & Docker Compose
- Java 21+
- Node.js 20+

### 설치

```bash
# 저장소 클론
git clone https://github.com/your-org/findplace.git
cd findplace

# 초기 설정
make init

# 환경변수 설정
cp .env.example .env
# .env 파일을 편집하여 설정 변경

# Docker 컨테이너 시작
make up
```

### 서비스 포트

| 서비스 | 포트 | 설명 |
|--------|------|------|
| PostgreSQL Master | 5432 | 쓰기 DB |
| PostgreSQL Slave 1 | 5433 | 읽기 DB |
| PostgreSQL Slave 2 | 5434 | 읽기 DB |
| Redis | 6379 | 캐시/세션 |
| MinIO API | 9000 | 파일 저장소 |
| MinIO Console | 9001 | 관리 콘솔 |
| Nginx | 80 | 리버스 프록시 |
| Backend | 8080 | API 서버 |
| Frontend | 3000 | 웹 클라이언트 |

## 프로젝트 구조

```
findplace/
├── AGENTS.md                    # 프로젝트 메인 지침
├── docker-compose.yml           # Docker 구성
├── Makefile                     # 빌드/실행 명령어
│
├── docs/
│   ├── plan/                    # 설계 문서
│   └── develop/                 # 개발 지침
│       ├── _common/             # 공통 규칙
│       ├── test-scenario/       # 테스트 시나리오
│       └── {domain}/            # 도메인별 지침
│
├── docker/                      # Docker 설정
│   ├── postgres/
│   ├── nginx/
│   └── redis/
│
├── backend/                     # Spring Boot (예정)
└── frontend/                    # React (예정)
```

## 주요 명령어

```bash
# 컨테이너 시작
make up

# 컨테이너 중지
make down

# 로그 확인
make logs

# 상태 확인
make ps

# PostgreSQL 접속
make db-shell

# Redis 접속
make redis-shell

# Replication 상태 확인
make replication-status

# 테스트 실행
make test

# 테스트 커버리지
make coverage
```

## 문서

- [AGENTS.md](./AGENTS.md) - 프로젝트 메인 지침
- [docs/plan/](./docs/plan/) - 설계 문서
- [docs/develop/](./docs/develop/) - 개발 지침

## 라이선스

Private - All Rights Reserved
