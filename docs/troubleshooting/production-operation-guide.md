# 프로덕션 운영 가이드

프로덕션 환경에서 실제 발생한 이슈와 개선 사항을 정리한 문서입니다.
새로운 프로덕션 이슈를 해결할 때마다 이 문서에 추가합니다.

> **초기 서버 설정**은 [production-setup-guide.md](./production-setup-guide.md)를 참조하세요.

---

## 목차

1. [배포 파이프라인](#1-배포-파이프라인)
2. [배포 명령어 정리](#2-배포-명령어-정리)
3. [저사양 서버 최적화](#3-저사양-서버-최적화)
4. [보안 설정](#4-보안-설정)
5. [프로덕션 이슈 해결 기록](#5-프로덕션-이슈-해결-기록)
6. [점검 체크리스트](#6-점검-체크리스트)

---

## 1. 배포 파이프라인

### 아키텍처

서버 성능이 낮아(1.9GB RAM) 빌드를 GitHub Actions에서 수행하고, 서버는 실행만 합니다.

```
변경 전: git pull → gradlew build → npm install → npm start (서버 빌드, 느림)
변경 후: git push → Actions 자동 빌드 → 서버에서 deploy.sh (서버는 실행만)
```

```
로컬 개발자
  │
  ├── git push origin main
  │
  ▼
GitHub Actions (ubuntu-latest)
  ├── Java 21 + Gradle → backend.jar
  ├── Node.js 20 + npm → frontend.tar.gz
  └── Release 생성 (softprops/action-gh-release)
  │
  ▼
서버 (deploy.sh)
  ├── GitHub Release에서 아티팩트 다운로드
  ├── frontend/build/에 정적 파일 배포
  ├── Docker 인프라 시작 (필수 서비스만)
  ├── 쿠폰 DB 자동 생성 (없는 경우)
  ├── MinIO 버킷 자동 생성 (없는 경우)
  ├── java -jar backend.jar 실행
  ├── nginx 재시작
  └── 방화벽 보안 설정 적용
```

### GitHub Actions 설정

- **워크플로 파일**: `.github/workflows/deploy.yml`
- **트리거**: `main` push + `workflow_dispatch` (수동)
- **빌드 환경**: Java 21 (temurin) + Node.js 20
- **프론트엔드 빌드**: `CI=false`로 ESLint 경고를 에러로 처리하지 않음
- **npm**: `npm install` 사용 (`npm ci`는 lock 파일 동기화 문제로 실패)

### GitHub 초기 설정 (1회)

1. **GitHub Settings > Actions > Variables**에 `REACT_APP_GOOGLE_CLIENT_ID` 등록
2. 서버 `.env`에 추가:
   ```
   GITHUB_REPO=sing0912/findplace
   MINIO_PUBLIC_URL=https://dev.findplace.co.kr/files
   ```

---

## 2. 배포 명령어 정리

### 코드 수정 후 배포

```bash
# 1. 로컬에서 푸시 (공통)
git add .
git commit -m "[수정] 변경사항 설명"
git push origin main
# → GitHub Actions 탭에서 빌드 완료 확인 (~2분)
```

#### 백엔드만 수정했을 때

```bash
# 서버에서
./deploy.sh
./scripts/back_end_restart.sh
```

#### 프론트엔드만 수정했을 때

```bash
# 서버에서
./deploy.sh
./scripts/front_end_restart.sh
```

#### 둘 다 수정했을 때

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

### 로그 확인

```bash
# 백엔드 실시간 로그
tail -f logs/backend.log

# 에러만 확인
tail -500 logs/backend.log | grep -i error

# nginx 로그
docker logs petpro-nginx --tail 100
```

---

## 3. 저사양 서버 최적화

### 환경

- **RAM**: 1.9GB
- **문제**: 전체 컨테이너(13개) + JVM 실행 시 OOM 발생

### 해결: 필수 서비스만 실행

`deploy.sh`에서 필수 6개 서비스만 시작하고, 나머지 8개를 중지합니다.

**필수 서비스 (6개)**:
| 서비스 | 용도 |
|--------|------|
| postgres-master | 메인 DB |
| postgres-coupon | 쿠폰 DB |
| mysql-log-master | 로그 DB |
| redis | 캐시/세션 |
| minio | 파일 저장소 |
| nginx | 리버스 프록시 |

**제외 서비스 (8개)**:
| 서비스 | 이유 |
|--------|------|
| postgres-slave1, slave2 | Slave → Master 포트 우회로 대체 |
| mysql-log-slave | Slave → Master 포트 우회로 대체 |
| grafana, prometheus, loki, promtail, tempo | 모니터링 스택 (메모리 부족) |

### Slave → Master 포트 우회

`.env`에서 Slave 포트를 Master 포트로 설정하면 Slave 컨테이너 없이 동작합니다:

```env
DB_SLAVE1_PORT=5432
DB_SLAVE2_PORT=5432
LOG_DB_SLAVE_PORT=3306
```

### JVM 메모리 제한

```bash
java -Xms128m -Xmx384m -jar backend.jar
```

---

## 4. 보안 설정

### 4.1 nginx 보안 (ssl.conf)

#### Swagger/API Docs 차단

프로덕션에서 Swagger UI를 열어두면 API 전체 명세가 외부에 노출됩니다.
(엔드포인트 목록, 파라미터 구조, 인증 방식, 내부 DTO 등)

```nginx
location /api/api-docs { return 403; }
location /api/swagger-ui { return 403; }
```

> 로컬 개발(`localhost:8080`)에서는 nginx를 거치지 않으므로 Swagger 사용 가능.
> 프로덕션에서 임시 확인 필요 시: 해당 블록 주석 처리 → `docker restart petpro-nginx`

#### 파일 업로드 Rate Limit

업로드 API에 요청 제한을 적용하여 DoS 공격을 방지합니다.

```nginx
location ~ ^/api/v1/(pets/\d+/image|users/me/profile-image) {
    limit_req zone=upload_limit burst=5 nodelay;
    client_max_body_size 10M;
    ...
}
```

#### API Rate Limit

전체 API에 요청 제한을 적용합니다.

```nginx
location /api {
    limit_req zone=api_limit burst=50 nodelay;
    ...
}
```

#### MinIO 프록시 접근 제어

MinIO 전체를 노출하지 않고, 허용 경로만 읽기 전용(GET/HEAD)으로 공개합니다.

```
/files/petpro/pets/     → 허용 (펫 이미지, GET/HEAD만)
/files/petpro/public/   → 허용 (공개 파일, GET/HEAD만)
/files/petpro/sitters/  → 차단 (민감 정보)
/files/petpro/chat/     → 차단 (민감 정보)
/files/petpro/care/     → 차단 (민감 정보)
/files/                 → 차단 (그 외 모든 경로)
```

### 4.2 파일 업로드 보안 (PetImageService)

| 검증 항목 | 설명 |
|-----------|------|
| 파일 크기 | 5MB 제한 (Spring + 서비스 레이어 이중 검증) |
| 확장자 검증 | jpg, jpeg, png, gif, webp만 허용 |
| 매직바이트 검증 | 파일 헤더를 읽어 실제 이미지인지 확인 (.exe를 .jpg로 위조해도 차단) |
| Path Traversal 방지 | 파일명에서 `../` 등 경로 조작 제거 |
| Content-Type | 확장자 기반으로 검증 (클라이언트 Content-Type은 신뢰하지 않음) |
| 파일명 | UUID 기반 랜덤 생성 (원본 파일명 미사용) |

**Spring 설정** (`application.yml`):
```yaml
servlet:
  multipart:
    max-file-size: 5MB      # 100MB → 5MB로 축소
    max-request-size: 5MB
```

### 4.3 MinIO 내부/외부 URL 분리

MinIO 연결(S3Client)은 내부 주소, 이미지 URL은 외부 주소를 사용합니다.

| 설정 | 용도 | 값 (예시) |
|------|------|-----------|
| `MINIO_ENDPOINT` | S3Client 내부 연결 | `http://localhost:9000` |
| `MINIO_PUBLIC_URL` | 이미지 URL 저장용 | `https://dev.findplace.co.kr/files` |

> `MINIO_PUBLIC_URL` 미설정 시 `MINIO_ENDPOINT` 값을 사용 (로컬 개발 호환)

### 4.4 방화벽 보안

`scripts/secure-firewall.sh`가 배포 시 자동 실행됩니다.

**원칙**: 외부에는 80(HTTP), 443(HTTPS)만 노출

```
허용: 80/tcp (HTTP), 443/tcp (HTTPS)
차단: 5432-5435 (PostgreSQL), 3306-3307 (MySQL),
      6379 (Redis), 9000-9001 (MinIO),
      8080 (Backend), 3000 (Frontend Dev),
      9090 (Prometheus), 3001 (Grafana), 3100 (Loki) 등
```

docker-compose.yml에서 내부 서비스는 `127.0.0.1`에 바인딩하여 이중 방어합니다.

### 4.5 HSTS

모든 HTTPS 응답에 HSTS 헤더를 추가하여 브라우저가 HTTP를 사용하지 않도록 강제합니다.

```nginx
add_header Strict-Transport-Security "max-age=63072000" always;
```

### 4.6 SSL/TLS

- TLS 1.2, 1.3만 허용 (1.0, 1.1 비활성)
- 안전한 Cipher Suite만 사용
- Let's Encrypt 인증서 자동 갱신 (crontab)

---

## 5. 프로덕션 이슈 해결 기록

실제 프로덕션 배포 과정에서 발생한 이슈와 해결 방법입니다.

### 5.1 npm ci 동기화 실패 (GitHub Actions)

**날짜**: 2026-02-08
**증상**: Actions에서 `npm ci` 실행 시 package-lock.json 동기화 에러
**원인**: 로컬에서 `npm install` 후 lock 파일이 최신 상태가 아님
**해결**: `npm ci` → `npm install`로 변경

### 5.2 ESLint 경고가 CI에서 에러로 처리됨

**날짜**: 2026-02-08
**증상**: 미사용 import(`Alert`)가 CI에서 빌드 실패 유발
**원인**: React의 `CI=true` (기본값) 환경에서 ESLint 경고를 에러로 취급
**해결**:
- `CI: false` 환경변수 추가 (워크플로)
- 미사용 import 제거 (`PetChecklistPage.tsx`)

### 5.3 쿠폰 DB 미존재

**날짜**: 2026-02-08
**증상**: 백엔드 시작 시 `petpro_coupon` DB 연결 실패
**원인**: PostgreSQL 컨테이너는 실행되지만 DB가 자동 생성되지 않음
**해결**: `deploy.sh`에 쿠폰 DB 자동 생성 로직 추가

```bash
docker exec petpro-postgres-coupon psql -U coupon -d postgres \
    -c "CREATE DATABASE petpro_coupon OWNER coupon;"
```

### 5.4 PostgreSQL 인증 실패

**날짜**: 2026-02-08
**증상**: `password authentication failed for user "petpro"`
**원인**: 기존 볼륨에 다른 비밀번호로 초기화된 데이터 존재
**해결**: 볼륨 삭제 후 재생성

```bash
docker compose down -v
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

> 주의: `-v` 옵션은 모든 데이터를 삭제합니다. 초기 세팅 시에만 사용.

### 5.5 서버 OOM (메모리 부족)

**날짜**: 2026-02-08
**증상**: 백엔드 시작 후 응답 없음, JVM 행(hang)
**원인**: RAM 1.9GB에서 컨테이너 13개 + JVM 실행 시 메모리 부족
**해결**:
- 불필요 컨테이너 8개 중지 (모니터링 + Slave DB)
- JVM 메모리 제한: `-Xms128m -Xmx384m`
- Slave 포트를 Master로 우회

### 5.6 MinIO 버킷 미존재

**날짜**: 2026-02-09
**증상**: 이미지 업로드 시 500 에러 (`NoSuchBucket`)
**원인**: MinIO 컨테이너는 실행되지만 `petpro` 버킷이 없음
**해결**:
- `deploy.sh`에 버킷 자동 생성 로직 추가
- `setup.sh` 헬스체크에도 버킷 확인 추가

```bash
docker exec petpro-minio mkdir -p /data/petpro
```

### 5.7 이미지 URL이 localhost로 저장됨

**날짜**: 2026-02-09
**증상**: 이미지 업로드 성공 후 브라우저에서 `localhost:9000` 접근 시도 → ERR_CONNECTION_REFUSED
**원인**: `MINIO_ENDPOINT`가 S3 연결과 URL 생성에 동시 사용됨
**해결**:
- `MINIO_ENDPOINT` (내부 연결용)과 `MINIO_PUBLIC_URL` (외부 URL용) 분리
- 서버 `.env`에 `MINIO_PUBLIC_URL=https://dev.findplace.co.kr/files` 추가

### 5.8 Google OAuth redirect_uri 불일치

**날짜**: 2026-02-09
**증상**: Google 로그인 시 `redirect_uri_mismatch` 에러
**원인**: Google Cloud Console에 프로덕션 도메인 미등록
**해결**:
1. Google Cloud Console > OAuth 2.0 > 승인된 리디렉션 URI에 추가:
   `https://dev.findplace.co.kr/oauth/google/callback`
2. 서버 `.env`에 추가:
   `GOOGLE_REDIRECT_URI=https://dev.findplace.co.kr/oauth/google/callback`

### 5.9 Google OAuth 401 에러

**날짜**: 2026-02-09
**증상**: OAuth 콜백에서 401 응답
**원인**: 백엔드 `GOOGLE_REDIRECT_URI`가 localhost로 설정됨
**해결**: 서버 `.env`에 프로덕션 URL 설정

```env
GOOGLE_REDIRECT_URI=https://dev.findplace.co.kr/oauth/google/callback
```

---

## 6. 점검 체크리스트

### 배포 전 확인

- [ ] GitHub Actions 빌드 성공 (녹색 체크)
- [ ] Releases에 `backend.jar` + `frontend.tar.gz` 존재

### 서버 배포 후 확인

- [ ] `curl -sf http://localhost:8080/api/health` 응답 확인
- [ ] `https://dev.findplace.co.kr` 프론트엔드 정상 로드
- [ ] `https://dev.findplace.co.kr/api/health` API 응답 확인
- [ ] `https://dev.findplace.co.kr/api/swagger-ui` → 403 차단 확인
- [ ] 이미지 업로드 테스트 (5MB 이하 jpg/png)
- [ ] 이미지 URL이 `https://dev.findplace.co.kr/files/petpro/...` 형태인지 확인

### 보안 점검

- [ ] `./scripts/secure-firewall.sh --check` 실행하여 포트 보안 확인
- [ ] Swagger UI 외부 접근 차단 확인
- [ ] MinIO `/files/` 루트 접근 차단 확인 (403)
- [ ] MinIO 민감 경로 차단 확인 (`/files/petpro/sitters/` 등 → 403)
- [ ] 이미지 경로 PUT/POST 차단 확인 (GET/HEAD만 허용)

### .env 필수 항목 (서버)

```env
# DB
DB_PASSWORD=...
COUPON_DB_PASSWORD=...
LOG_DB_PASSWORD=...
LOG_DB_ROOT_PASSWORD=...
REPLICATOR_PASSWORD=...

# Redis
REDIS_PASSWORD=...

# JWT
JWT_SECRET=...

# MinIO
MINIO_ACCESS_KEY=...
MINIO_SECRET_KEY=...
MINIO_PUBLIC_URL=https://dev.findplace.co.kr/files

# 배포
GITHUB_REPO=sing0912/findplace
APP_ENV=prod

# OAuth (사용 시)
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GOOGLE_REDIRECT_URI=https://dev.findplace.co.kr/oauth/google/callback

# 저사양 서버 Slave 우회
DB_SLAVE1_PORT=5432
DB_SLAVE2_PORT=5432
LOG_DB_SLAVE_PORT=3306
```
