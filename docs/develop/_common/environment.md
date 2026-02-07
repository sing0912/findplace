# 개발 환경 구성 가이드

## 컨테이너 런타임: Podman

이 프로젝트는 **Podman**(Docker 호환)을 컨테이너 런타임으로 사용합니다.

### 설치

```bash
# macOS (Homebrew)
brew install podman podman-compose

# Podman 머신 초기화 및 시작
podman machine init
podman machine start
```

### 컨테이너 목록

| 컨테이너 이름 | 이미지 | 포트 | 용도 |
|---------------|--------|------|------|
| `petpro-postgres-master` | `postgres:16-alpine` | 5432 | PostgreSQL Master (CUD) |
| `petpro-postgres-slave1` | `postgres:16-alpine` | 5433 | PostgreSQL Slave 1 (Read) |
| `petpro-postgres-slave2` | `postgres:16-alpine` | 5434 | PostgreSQL Slave 2 (Read) |
| `petpro-postgres-coupon` | `postgres:16-alpine` | 5435 | 쿠폰 전용 DB |
| `petpro-mysql-log-master` | `mysql:8.0` | 3306 | 로그 DB Master |
| `petpro-mysql-log-slave` | `mysql:8.0` | 3307 | 로그 DB Slave |
| `findplace-redis` | `redis:7-alpine` | 6379 | 캐시/세션 |

### 자주 사용하는 명령어

```bash
# 전체 컨테이너 상태 확인
podman ps

# 컨테이너 시작/중지
podman-compose up -d
podman-compose down

# PostgreSQL 접속
podman exec -it petpro-postgres-master psql -U petpro -d petpro

# MySQL 로그 DB 접속
podman exec -it petpro-mysql-log-master mysql -u loguser -plogpass123!

# Redis 접속
podman exec -it findplace-redis redis-cli -a redis123!

# 컨테이너 로그 확인
podman logs -f petpro-postgres-master
```

---

## 환경 변수 (.env)

### 로딩 방법

백엔드(Spring Boot)는 `.env` 파일을 자동으로 읽지 않습니다.

#### CLI 실행 시

```bash
# .env를 환경변수로 export 후 실행
set -a && source .env && set +a
cd backend && ./gradlew bootRun
```

#### IntelliJ IDEA

1. **EnvFile 플러그인** 설치 (Marketplace 검색)
2. Run Configuration → EnvFile 탭 → `.env` 파일 추가
3. 또는 Run Configuration → Environment Variables에 직접 입력

### 주요 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `DB_PASSWORD` | (필수) | PostgreSQL 비밀번호 |
| `LOG_DB_PASSWORD` | (필수) | MySQL 로그 DB 비밀번호 |
| `REDIS_PASSWORD` | (필수) | Redis 비밀번호 |
| `JWT_SECRET` | (필수) | JWT 서명 시크릿 |
| `MINIO_SECRET_KEY` | (필수) | MinIO 시크릿 키 |
| `APP_ENV` | `local` | 활성 프로필 (local/prod) |
| `APP_PORT` | `8080` | 백엔드 포트 |

> **참고**: 전체 변수 목록은 `.env.example` 참조

---

## 개발 서버 실행 순서

```
1. Podman 컨테이너 시작    →  podman-compose up -d
2. 환경변수 로드            →  set -a && source .env && set +a
3. 백엔드 실행             →  cd backend && ./gradlew bootRun
4. 프론트엔드 실행 (별도)   →  cd frontend && npm start
```

### 서비스 접속 URL

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:3000 |
| 백엔드 API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| MinIO Console | http://localhost:9001 |

---

## 관리자 계정

### 초기 관리자 생성

프로젝트에 기본 관리자 계정이 없으므로 수동 생성이 필요합니다.

```bash
# 1. 회원가입 API로 계정 생성
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  --data-raw '{"email":"admin@petpro.com","password":"Admin1234@","name":"관리자","nickname":"admin","phone":"010-0000-0000","agreeTerms":true,"agreePrivacy":true,"agreeMarketing":false}'

# 2. DB에서 role 변경
podman exec petpro-postgres-master \
  psql -U petpro -d petpro \
  -c "UPDATE users SET role = 'SUPER_ADMIN' WHERE email = 'admin@petpro.com';"
```

### 로그인 정보

| 항목 | 값 |
|------|------|
| 관리자 로그인 URL | http://localhost:3000/admin/login |
| 이메일 | `admin@petpro.com` |
| 비밀번호 | `Admin1234@` |
| 역할 | `SUPER_ADMIN` |

---

## 데이터베이스

### PostgreSQL (메인 DB)

- **Master**: 5432 (CUD 작업)
- **Slave 1**: 5433 (읽기 전용, Round Robin)
- **Slave 2**: 5434 (읽기 전용, Round Robin)
- **Coupon**: 5435 (쿠폰 전용, 별도 DB)

Flyway 마이그레이션: `backend/src/main/resources/db/migration/`

### MySQL (로그 DB)

- **Master**: 3306 (로그 쓰기)
- **Slave**: 3307 (로그 읽기)

Flyway 마이그레이션: `backend/src/main/resources/db/log-migration/`
