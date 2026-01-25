# FindPlace - 반려동물 장례 토탈 플랫폼

반려동물 장례 서비스를 위한 통합 플랫폼입니다.

- **사용자(B2C)**: 장례업체 검색, 예약, 결제, 추모관
- **장례업체(B2B)**: 예약/일정/봉안당 관리
- **공급사**: 상품/재고/주문/정산 관리
- **플랫폼 관리자**: 통합 관리

---

## 기술 스택

| 구분 | 기술 | 버전 |
| --- | --- | --- |
| Backend | Java, Spring Boot | Java 21, Spring Boot 3.2.5 |
| Frontend | React, TypeScript | React 19, TypeScript 4.9 |
| Database | PostgreSQL (Master 1 + Slave 2) | PostgreSQL 16 |
| Cache | Redis | Redis 7 |
| Storage | MinIO (S3 호환) | MinIO |
| Container | Docker, Docker Compose | - |

---

## 프로젝트 구조

```text
findplace/
├── AGENTS.md                           # 프로젝트 메인 지침
├── README.md                           # 이 문서
├── Makefile                            # 빌드/실행 명령어
├── setup.sh                            # 서버 설치/실행 스크립트
├── docker-compose.yml                  # Docker 구성
├── .env.example                        # 환경변수 예시
├── .gitignore                          # Git 제외 파일
│
├── backend/                            # Spring Boot 백엔드
│   ├── build.gradle.kts                # Gradle 빌드 설정
│   ├── settings.gradle.kts             # Gradle 설정
│   ├── gradlew                         # Gradle Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/findplace/
│       │   │   ├── FindPlaceApplication.java
│       │   │   ├── domain/             # 도메인 계층
│       │   │   │   ├── auth/           # 인증 도메인
│       │   │   │   │   ├── controller/AuthController.java
│       │   │   │   │   ├── service/AuthService.java
│       │   │   │   │   └── dto/
│       │   │   │   └── user/           # 사용자 도메인
│       │   │   │       ├── controller/UserController.java
│       │   │   │       ├── service/UserService.java
│       │   │   │       ├── repository/UserRepository.java
│       │   │   │       ├── entity/
│       │   │   │       └── dto/
│       │   │   └── global/             # 전역 설정
│       │   │       ├── config/         # 설정 클래스
│       │   │       │   ├── SecurityConfig.java
│       │   │       │   ├── RedisConfig.java
│       │   │       │   ├── MinioConfig.java
│       │   │       │   ├── WebConfig.java
│       │   │       │   ├── JpaConfig.java
│       │   │       │   ├── AuditingConfig.java
│       │   │       │   └── datasource/ # Master-Slave 라우팅
│       │   │       │       ├── DataSourceConfig.java
│       │   │       │       ├── RoutingDataSource.java
│       │   │       │       └── DataSourceAspect.java
│       │   │       ├── security/jwt/   # JWT 인증
│       │   │       │   ├── JwtTokenProvider.java
│       │   │       │   └── JwtAuthenticationFilter.java
│       │   │       ├── exception/      # 예외 처리
│       │   │       │   ├── GlobalExceptionHandler.java
│       │   │       │   ├── BusinessException.java
│       │   │       │   └── ErrorCode.java
│       │   │       ├── common/         # 공통 클래스
│       │   │       │   ├── response/ApiResponse.java
│       │   │       │   └── entity/BaseEntity.java
│       │   │       └── controller/HealthController.java
│       │   └── resources/
│       │       ├── application.yml     # 기본 설정
│       │       ├── application-local.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/       # Flyway 마이그레이션
│       │           ├── V1__create_users_table.sql
│       │           ├── V2__create_companies_table.sql
│       │           └── V3__create_suppliers_table.sql
│       └── test/                       # 테스트 코드
│           ├── java/com/findplace/
│           │   ├── FindPlaceApplicationTests.java
│           │   └── domain/user/service/UserServiceTest.java
│           └── resources/application-test.yml
│
├── frontend/                           # React 프론트엔드
│   ├── package.json                    # NPM 설정
│   ├── tsconfig.json                   # TypeScript 설정
│   ├── public/
│   │   └── manifest.json
│   └── src/
│       ├── index.tsx                   # 엔트리 포인트
│       ├── App.tsx                     # 메인 앱 컴포넌트
│       ├── api/                        # API 클라이언트
│       │   ├── client.ts               # Axios 인스턴스
│       │   ├── auth.ts                 # 인증 API
│       │   ├── user.ts                 # 사용자 API
│       │   └── index.ts
│       ├── components/                 # 컴포넌트
│       │   ├── common/
│       │   │   ├── ProtectedRoute.tsx  # 인증 라우트 가드
│       │   │   └── Notification.tsx    # 알림 컴포넌트
│       │   └── layout/
│       │       ├── MainLayout.tsx      # 메인 레이아웃
│       │       ├── Header.tsx          # 헤더
│       │       └── Sidebar.tsx         # 사이드바
│       ├── pages/                      # 페이지 컴포넌트
│       │   ├── HomePage.tsx            # 홈 페이지
│       │   └── auth/
│       │       ├── LoginPage.tsx       # 로그인
│       │       └── RegisterPage.tsx    # 회원가입
│       ├── hooks/                      # 커스텀 훅
│       │   └── useAuth.ts              # 인증 훅
│       ├── stores/                     # 상태 관리 (Zustand)
│       │   ├── authStore.ts            # 인증 상태
│       │   └── uiStore.ts              # UI 상태
│       └── types/                      # TypeScript 타입
│           ├── api.ts
│           ├── auth.ts
│           ├── user.ts
│           └── index.ts
│
├── docker/                             # Docker 설정
│   ├── postgres/                       # PostgreSQL 설정
│   │   ├── init-master.sh              # Master 초기화 스크립트
│   │   ├── init-slave.sh               # Slave 초기화 스크립트
│   │   └── pg_hba.conf                 # 접근 권한 설정
│   └── nginx/                          # Nginx 설정
│       ├── nginx.conf                  # 메인 설정
│       └── conf.d/default.conf         # 가상 호스트 설정
│
├── scripts/                            # 유틸리티 스크립트
│   ├── README.md                       # 스크립트 설명
│   ├── detect-runtime.sh               # Docker/Podman 감지
│   ├── run.sh                          # 실행 스크립트
│   ├── start-all.sh                    # 전체 시작
│   └── stop-all.sh                     # 전체 중지
│
└── docs/                               # 문서
    ├── plan/                           # 설계 문서
    │   ├── README.md
    │   └── 2026-01-24-initial-design.md
    ├── develop/                        # 개발 지침
    │   ├── README.md
    │   ├── _common/                    # 공통 규칙
    │   │   ├── README.md
    │   │   ├── api-convention.md       # API 규칙
    │   │   ├── db-convention.md        # DB 규칙
    │   │   ├── error-handling.md       # 에러 처리
    │   │   ├── security.md             # 보안
    │   │   └── tdd.md                  # TDD 가이드
    │   ├── test-scenario/              # 테스트 시나리오
    │   │   ├── README.md
    │   │   ├── scenario-definition.md
    │   │   └── domains/
    │   ├── auth/README.md              # 인증 도메인
    │   ├── user/README.md              # 사용자 도메인
    │   ├── company/README.md           # 장례업체 도메인
    │   ├── supplier/README.md          # 공급사 도메인
    │   ├── product/README.md           # 상품 도메인
    │   ├── reservation/README.md       # 예약 도메인
    │   ├── order/README.md             # 주문 도메인
    │   ├── payment/README.md           # 결제 도메인
    │   ├── memorial/README.md          # 추모관 도메인
    │   └── ...                         # 기타 도메인
    └── troubleshooting/                # 트러블슈팅
        └── production-setup-guide.md   # 실서버 설정 가이드
```

---

## 구현 현황

### Backend 도메인

| 도메인 | 상태 | 기능 |
| --- | --- | --- |
| auth | ✅ 완료 | 회원가입, 로그인, JWT 발급/갱신 |
| user | ✅ 완료 | 사용자 조회, 프로필 수정 |
| company | ⬜ 미구현 | 장례업체 CRUD |
| supplier | ⬜ 미구현 | 공급사 CRUD |
| product | ⬜ 미구현 | 상품 CRUD |
| inventory | ⬜ 미구현 | 재고 관리 |
| reservation | ⬜ 미구현 | 예약 관리 |
| order | ⬜ 미구현 | 주문 관리 |
| payment | ⬜ 미구현 | 결제 관리 |
| delivery | ⬜ 미구현 | 배송 관리 |
| settlement | ⬜ 미구현 | 정산 관리 |
| memorial | ⬜ 미구현 | 추모관 |
| columbarium | ⬜ 미구현 | 봉안당 |
| notification | ⬜ 미구현 | 알림/SMS/이메일 |

### Frontend 페이지

| 페이지 | 상태 | 경로 |
| --- | --- | --- |
| 로그인 | ✅ 완료 | /login |
| 회원가입 | ✅ 완료 | /register |
| 홈 (대시보드) | ✅ 완료 | / |
| 장례업체 목록 | ⬜ 미구현 | /companies |
| 상품 목록 | ⬜ 미구현 | /products |
| 예약 관리 | ⬜ 미구현 | /reservations |
| 추모관 | ⬜ 미구현 | /memorial |

### Infrastructure

| 서비스 | 상태 | 포트 | 설명 |
| --- | --- | --- | --- |
| PostgreSQL Master | ✅ | 5432 | 쓰기 DB |
| PostgreSQL Slave 1 | ✅ | 5433 | 읽기 DB |
| PostgreSQL Slave 2 | ✅ | 5434 | 읽기 DB |
| Redis | ✅ | 6379 | 캐시/세션 |
| MinIO | ✅ | 9000, 9001 | 파일 저장소 |
| Nginx | ✅ | 80 | 리버스 프록시 |

---

## 빠른 시작

### 방법 1: setup.sh 사용 (권장)

```bash
# 저장소 클론
git clone https://github.com/sing0912/findplace.git
cd findplace

# 전체 설치 및 실행
chmod +x setup.sh
./setup.sh
```

### 방법 2: Makefile 사용

```bash
# 환경변수 설정
cp .env.example .env

# 전체 시작
make up
```

### 방법 3: 수동 실행

```bash
# 1. Docker 인프라 시작
docker-compose up -d

# 2. 백엔드 실행
cd backend
./gradlew bootRun

# 3. 프론트엔드 실행
cd frontend
npm install
npm start
```

---

## 스크립트 사용법

### setup.sh (서버 배포용)

```bash
./setup.sh              # 전체 설치 및 실행
./setup.sh install      # 필수 소프트웨어 설치
./setup.sh start        # 서비스 시작
./setup.sh stop         # 서비스 중지
./setup.sh status       # 상태 확인
./setup.sh restart      # 재시작
./setup.sh clean        # 전체 초기화 (데이터 삭제)
```

### Makefile (개발용)

```bash
make up                 # Docker 컨테이너 시작
make down               # Docker 컨테이너 중지
make logs               # 로그 확인
make ps                 # 상태 확인
make db-shell           # PostgreSQL 접속
make redis-shell        # Redis 접속
make test               # 테스트 실행
make coverage           # 테스트 커버리지
make clean              # 빌드 파일 정리
```

### scripts/ 디렉토리

```bash
./scripts/start-all.sh  # 전체 서비스 시작 (Docker/Podman 자동 감지)
./scripts/stop-all.sh   # 전체 서비스 중지
./scripts/run.sh        # 백엔드 실행
```

---

## 서비스 포트

| 서비스 | 포트 | URL |
| --- | --- | --- |
| Frontend | 3000 | http://localhost:3000 |
| Backend API | 8080 | http://localhost:8080/api |
| Swagger UI | 8080 | http://localhost:8080/api/swagger-ui.html |
| PostgreSQL Master | 5432 | - |
| PostgreSQL Slave 1 | 5433 | - |
| PostgreSQL Slave 2 | 5434 | - |
| Redis | 6379 | - |
| MinIO API | 9000 | http://localhost:9000 |
| MinIO Console | 9001 | http://localhost:9001 |
| Nginx | 80 | http://localhost |

---

## 데이터베이스 구조

### Master-Slave Replication

```text
┌─────────────────────┐
│  PostgreSQL Master  │  ← Write (INSERT, UPDATE, DELETE)
└──────────┬──────────┘
           │ Streaming Replication
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐ ┌─────────┐
│ Slave 1 │ │ Slave 2 │  ← Read Only (SELECT)
└─────────┘ └─────────┘
```

### 라우팅 규칙

- `@Transactional(readOnly = false)` → Master
- `@Transactional(readOnly = true)` → Slave (Round Robin)

### Flyway 마이그레이션

```text
backend/src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_companies_table.sql
└── V3__create_suppliers_table.sql
```

---

## API 구조

### 인증 API

```text
POST /api/auth/register     # 회원가입
POST /api/auth/login        # 로그인
POST /api/auth/refresh      # 토큰 갱신
POST /api/auth/logout       # 로그아웃
```

### 사용자 API

```text
GET  /api/users/me          # 내 정보 조회
PUT  /api/users/me          # 프로필 수정
```

### JWT 인증

```bash
# 로그인 요청
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

# 응답
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}

# API 호출 시 헤더
Authorization: Bearer {accessToken}
```

---

## 환경 변수

### .env 파일

```env
# Database
DB_HOST=localhost
DB_NAME=findplace
DB_USERNAME=findplace
DB_PASSWORD=findplace123!
DB_MASTER_PORT=5432
DB_SLAVE1_PORT=5433
DB_SLAVE2_PORT=5434

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis123!

# JWT
JWT_SECRET=your-jwt-secret-key-must-be-at-least-256-bits-long
JWT_ACCESS_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=1209600

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123!
MINIO_BUCKET=findplace

# Application
APP_PORT=8080
APP_ENV=local
FRONTEND_URL=http://localhost:3000
```

---

## 로그 확인

```bash
# 백엔드 로그
tail -f /tmp/findplace-backend.log

# 프론트엔드 로그
tail -f /tmp/findplace-frontend.log

# Docker 컨테이너 로그
docker logs findplace-postgres-master
docker logs findplace-redis
docker logs findplace-minio

# 에러만 확인
grep -i "error\|exception" /tmp/findplace-backend.log
```

---

## 사용자 역할

| 역할 | 코드 | 설명 |
| --- | --- | --- |
| 일반 사용자 | ROLE_USER | 예약, 주문, 추모관 이용 |
| 장례업체 관리자 | ROLE_COMPANY_ADMIN | 업체 관리, 예약/일정 관리 |
| 공급사 관리자 | ROLE_SUPPLIER_ADMIN | 상품/재고/주문/정산 관리 |
| 플랫폼 관리자 | ROLE_ADMIN | 전체 시스템 관리 |

---

## 문서

| 문서 | 설명 |
| --- | --- |
| [AGENTS.md](./AGENTS.md) | 프로젝트 메인 지침 |
| [docs/plan/](./docs/plan/) | 설계 문서 |
| [docs/develop/](./docs/develop/) | 개발 지침 |
| [docs/develop/_common/](./docs/develop/_common/) | 공통 규칙 (API, DB, TDD 등) |
| [docs/troubleshooting/](./docs/troubleshooting/) | 트러블슈팅 가이드 |

---

## 트러블슈팅

서버 배포 시 발생할 수 있는 문제들:

- [실서비스 환경 세팅 가이드](./docs/troubleshooting/production-setup-guide.md)

주요 이슈:
- Git/Docker/docker-compose 설치
- 포트 충돌 (httpd/nginx)
- MinIO CPU 호환성
- Gradle Wrapper JAR 누락
- 프론트엔드 외부 접속 설정

---

## 라이선스

Private - All Rights Reserved
