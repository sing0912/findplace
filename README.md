# PetPro - 반려동물 장례 토탈 플랫폼

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
| Container | Podman, Podman Compose (Docker 호환) | Podman 5.x |

---

## 프로젝트 구조

```text
petpro/
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
│       │   ├── java/com/petpro/
│       │   │   ├── PetProApplication.java
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
│           ├── java/com/petpro/
│           │   ├── PetProApplicationTests.java
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
| region | ✅ 완료 | 지역/시도 관리, 행정구역 코드 |
| pet | ✅ 완료 | 반려동물 CRUD |
| funeralhome | ✅ 완료 | 장례식장 조회, 근처 장례식장 검색 |
| coupon | ✅ 완료 | 쿠폰 관리 |
| admin | ✅ 완료 | 관리자 사용자 관리 |
| batch | ✅ 완료 | 배치 작업 (데이터 동기화) |
| location | ✅ 완료 | 위치 기반 서비스, 거리 계산 |
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
| 근처 장례식장 | ✅ 완료 | /nearby-funeral-homes |
| 반려동물 목록 | ✅ 완료 | /pets |
| 개인정보처리방침 | ✅ 완료 | /privacy-policy |
| 장례업체 목록 | ⬜ 미구현 | /companies |
| 상품 목록 | ⬜ 미구현 | /products |
| 예약 관리 | ⬜ 미구현 | /reservations |
| 추모관 | ⬜ 미구현 | /memorial |

### Infrastructure (Podman Compose)

| 서비스 | 상태 | 포트 | 설명 |
| --- | --- | --- | --- |
| PostgreSQL Master | ✅ | 5432 | 쓰기 DB |
| PostgreSQL Slave 1 | ✅ | 5433 | 읽기 DB |
| PostgreSQL Slave 2 | ✅ | 5434 | 읽기 DB |
| PostgreSQL Coupon | ✅ | 5435 | 쿠폰 DB (별도) |
| MySQL Log Master | ✅ | 3306 | 로그 DB (쓰기) |
| MySQL Log Slave | ✅ | 3307 | 로그 DB (읽기) |
| Redis | ✅ | 6379 | 캐시/세션 |
| MinIO | ✅ | 9000, 9001 | 파일 저장소 |
| Nginx | ✅ | 80, 443 | 리버스 프록시 (SSL) |

---

## 컨테이너 런타임

이 프로젝트는 **Podman**을 컨테이너 런타임으로 사용합니다 (Docker 호환).

### Podman 설치 (macOS)

```bash
brew install podman podman-compose
podman machine init
podman machine start
```

### 주요 차이점 (Docker vs Podman)

| 항목 | Docker | Podman |
|------|--------|--------|
| 데몬 | dockerd 데몬 필요 | 데몬 없음 (rootless) |
| 명령어 | `docker`, `docker-compose` | `podman`, `podman-compose` |
| 소켓 | `/var/run/docker.sock` | `podman machine` 기반 |
| 네트워크 | `bridge` 기본 | `slirp4netns` 기본 |

> **참고**: `scripts/detect-runtime.sh`가 Docker/Podman을 자동 감지합니다.

---

## 빠른 시작

### 방법 1: setup.sh 사용 (권장)

```bash
# 저장소 클론
git clone https://github.com/sing0912/petpro.git
cd petpro

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
# 1. 인프라 시작 (Podman)
podman-compose up -d

# 2. 환경변수 로드 후 백엔드 실행
set -a && source .env && set +a
cd backend
./gradlew bootRun

# 3. 프론트엔드 실행 (별도 터미널)
cd frontend
npm install
npm start
```

> **중요**: 백엔드 실행 시 `.env` 파일을 반드시 source해야 합니다.
> IDE(IntelliJ)에서 실행할 경우 EnvFile 플러그인으로 자동 로드됩니다.

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

## 배포 (GitHub Actions + Release)

서버 성능이 낮아 빌드는 GitHub Actions에서, 서버는 실행만 합니다.

```
로컬: git push origin main
  → GitHub Actions 자동 빌드 (~2분) → Release 생성

서버: ./deploy.sh
  → 최신 Release 다운로드 → 배포 → 시작 (~30초)
```

### 배포 순서

#### 1. 코드 수정 후 푸시 (공통)

```bash
git add .
git commit -m "[수정] 변경사항 설명"
git push origin main
# → GitHub Actions 탭에서 빌드 완료 확인 (~2분)
```

#### 2-A. 백엔드만 수정했을 때

```bash
# 서버에서
./deploy.sh
./scripts/back_end_restart.sh
```

#### 2-B. 프론트엔드만 수정했을 때

```bash
# 서버에서
./deploy.sh
./scripts/front_end_restart.sh
```

#### 2-C. 둘 다 수정했을 때

```bash
# 서버에서
./deploy.sh
```

> `deploy.sh`가 백엔드 + 프론트엔드 + nginx 전부 재시작합니다.

### 코드 변경 없이 재시작만 할 때

```bash
./scripts/restart-app.sh              # 전체 재시작 (백엔드 + nginx)
./scripts/restart-app.sh backend      # 백엔드만 재시작
./scripts/restart-app.sh frontend     # 프론트엔드(nginx)만 재시작
./scripts/restart-app.sh stop         # 백엔드 중지
./scripts/restart-app.sh status       # 상태 확인
```

### 초기 설정 (1회)

1. **GitHub Settings > Actions > Variables**에 `REACT_APP_GOOGLE_CLIENT_ID` 등록
2. 서버 `.env`에 추가:
   ```
   GITHUB_REPO=sing0912/findplace
   MINIO_PUBLIC_URL=https://dev.findplace.co.kr/files
   ```
3. 서버에 Java 21 JRE 설치 확인

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
| Nginx | 80, 443 | http://localhost, https://dev.findplace.co.kr |

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
  "password": "your-password"
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

```bash
# .env.example을 복사하여 실제 값을 설정하세요.
cp .env.example .env
# 반드시 모든 your-* 값을 실제 비밀번호로 변경해야 합니다.
```

> **주의**: 비밀번호는 절대 소스코드에 하드코딩하지 마세요. `.env` 파일은 `.gitignore`에 포함되어 있습니다.

필수 환경변수 목록은 `.env.example` 파일을 참고하세요.

---

## 로그 확인

```bash
# 백엔드 로그
tail -f backend/logs/petpro-backend.log

# Podman 컨테이너 로그
podman logs petpro-postgres-master
podman logs petpro-redis
podman logs petpro-mysql-log-master

# 에러만 확인
grep -i "error\|exception" backend/logs/petpro-backend.log
```

---

## 사용자 역할

| 역할 | 코드 | 설명 |
| --- | --- | --- |
| 반려인 | CUSTOMER | 시터 검색, 예약, 결제, 돌봄 조회 |
| 펫시터 | PARTNER | 프로필/자격 관리, 예약 수락/거절, 정산 |
| 관리자 | ADMIN | 일반 관리 기능 |
| 최고 관리자 | SUPER_ADMIN | 전체 시스템 관리 |

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

## 프로덕션 SSL/HTTPS 설정

프로덕션 환경에서 HTTPS를 사용하려면 SSL 인증서 설정이 필요합니다.

```bash
# 1. SSL 인증서 발급
docker stop petpro-nginx
certbot certonly --standalone -d dev.findplace.co.kr -m your@email.com --agree-tos
docker start petpro-nginx

# 2. SSL 설정 적용
cp docker/nginx/conf.d/ssl.conf.prod docker/nginx/conf.d/ssl.conf

# 3. 프로덕션 모드로 실행
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

자세한 내용은 [실서비스 환경 세팅 가이드](./docs/troubleshooting/production-setup-guide.md#6-sslhttps-설정)를 참조하세요.

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
