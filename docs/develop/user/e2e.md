# User 도메인 E2E 테스트 지침

## 개요

User 도메인 14개 API 엔드포인트에 대한 End-to-End 테스트.
실제 HTTP 서버를 기동하고 JWT 인증을 포함한 전체 요청/응답 흐름을 검증한다.

---

## 기술 스택

| 항목 | 기술 | 설명 |
|------|------|------|
| 서버 기동 | `@SpringBootTest(RANDOM_PORT)` | 실제 HTTP 서버 전체 스택 |
| HTTP 클라이언트 | `TestRestTemplate` | 실제 HTTP 호출 |
| DB | H2 인메모리 (PostgreSQL 모드) | Docker 불필요 |
| 인증 | Auth API로 JWT 발급 | `POST /v1/auth/register` + `login` |
| 관리자 생성 | DB 직접 삽입 + login API | 닭-달걀 문제 해결 |

---

## 테스트 대상 API (14개)

### /v1/users/me (5개 엔드포인트)

| # | API | 메서드 | 설명 |
|---|-----|--------|------|
| 1 | `/v1/users/me` | GET | 내 정보 조회 |
| 2 | `/v1/users/me` | PUT | 닉네임 수정 |
| 3 | `/v1/users/me/profile-image` | POST | 프로필 이미지 업로드 |
| 4 | `/v1/users/me/password` | PUT | 비밀번호 변경 |
| 5 | `/v1/users/me` | DELETE | 회원 탈퇴 |

### /v1/users/{id} (3개 엔드포인트)

| # | API | 메서드 | 설명 |
|---|-----|--------|------|
| 6 | `/v1/users/{id}` | GET | 사용자 조회 |
| 7 | `/v1/users/{id}` | PUT | 사용자 정보 수정 |
| 8 | `/v1/users/{id}/password` | PUT | 비밀번호 변경 |

### 관리자 전용 (6개 엔드포인트)

| # | API | 메서드 | 권한 | 설명 |
|---|-----|--------|------|------|
| 9 | `/v1/users` | POST | ADMIN | 사용자 생성 |
| 10 | `/v1/users` | GET | ADMIN | 사용자 목록 조회 |
| 11 | `/v1/users/search` | GET | ADMIN | 사용자 검색 |
| 12 | `/v1/users/{id}/status` | PATCH | ADMIN | 상태 변경 |
| 13 | `/v1/users/{id}/role` | PATCH | SUPER_ADMIN | 역할 변경 |
| 14 | `/v1/users/{id}` | DELETE | ADMIN | 사용자 삭제 |

---

## 테스트 시나리오

### UserMeE2ETest (11개)

| # | 테스트 | API | 기대 결과 |
|---|--------|-----|-----------|
| 1 | 내 정보 조회 성공 | GET /me | 200, email/role 일치 |
| 2 | 내 정보 조회 미인증 | GET /me (no token) | 401/403 |
| 3 | 닉네임 수정 성공 | PUT /me | 200, 변경된 닉네임 |
| 4 | 닉네임 빈값 실패 | PUT /me | 400 |
| 5 | 닉네임 중복 실패 | PUT /me | 409, U007 |
| 6 | 프로필 이미지 업로드 성공 | POST /me/profile-image | 200, multipart |
| 7 | 비밀번호 변경 성공 | PUT /me/password | 200 |
| 8 | 비밀번호 변경 실패 (현재PW 불일치) | PUT /me/password | 400, U004 |
| 9 | 비밀번호 변경 실패 (취약PW) | PUT /me/password | 400 |
| 10 | 회원 탈퇴 성공 | DELETE /me | 200 |
| 11 | 회원 탈퇴 실패 (PW 불일치) | DELETE /me | 400, U004 |

### UserByIdE2ETest (9개)

| # | 테스트 | API | 기대 결과 |
|---|--------|-----|-----------|
| 1 | 사용자 조회 성공 | GET /{id} | 200, id 일치 |
| 2 | 존재하지 않는 사용자 | GET /999999 | 404, U001 |
| 3 | 미인증 접근 | GET /{id} (no token) | 401/403 |
| 4 | 본인 정보 수정 성공 | PUT /{id} | 200, name 변경 |
| 5 | 관리자가 타인 수정 | PUT /{id} (admin) | 200 |
| 6 | 타인이 수정 시도 | PUT /{id} (other) | 403 |
| 7 | 본인 비밀번호 변경 성공 | PUT /{id}/password | 200 |
| 8 | 타인 비밀번호 변경 시도 | PUT /{id}/password | 403 |
| 9 | 비밀번호 변경 실패 (PW 불일치) | PUT /{id}/password | 400/401 |

### UserAdminE2ETest (16개)

| # | 테스트 | API | 기대 결과 |
|---|--------|-----|-----------|
| 1 | 관리자가 사용자 생성 | POST /users | 201 |
| 2 | 일반 사용자가 생성 시도 | POST /users | 403 |
| 3 | 미인증 생성 시도 | POST /users (no token) | 401/403 |
| 4 | 이메일 중복 생성 | POST /users | 409, U002 |
| 5 | 관리자 목록 조회 | GET /users | 200, content 존재 |
| 6 | 일반 사용자 목록 조회 | GET /users | 403 |
| 7 | 관리자 검색 | GET /users/search | 200 |
| 8 | 일반 사용자 검색 시도 | GET /users/search | 403 |
| 9 | 사용자 정지 | PATCH /{id}/status?status=SUSPENDED | 200 |
| 10 | 사용자 재활성화 | PATCH /{id}/status?status=ACTIVE | 200 |
| 11 | 일반 사용자 상태 변경 시도 | PATCH /{id}/status | 403 |
| 12 | SUPER_ADMIN 역할 변경 | PATCH /{id}/role?role=ADMIN | 200 |
| 13 | ADMIN 역할 변경 시도 | PATCH /{id}/role | 403 |
| 14 | 일반 사용자 역할 변경 시도 | PATCH /{id}/role | 403 |
| 15 | 관리자 소프트 삭제 | DELETE /{id} | 200 |
| 16 | 일반 사용자 삭제 시도 | DELETE /{id} | 403 |

---

## 파일 구조

```
backend/src/test/
  resources/
    application-test.yml                          ← 수정
  java/com/petpro/e2e/
    config/
      E2ETestConfig.java                          ← 신규
    support/
      BaseE2ETest.java                            ← 신규
      AuthTestHelper.java                         ← 신규
      AdminTestHelper.java                        ← 신규
    user/
      UserMeE2ETest.java                          ← 신규
      UserByIdE2ETest.java                        ← 신규
      UserAdminE2ETest.java                       ← 신규
```

---

## 실행 방법

### H2 모드 (기본, CI/CD)

```bash
cd backend
./gradlew clean test --tests "com.petpro.e2e.user.*" --no-build-cache
```

### PostgreSQL 모드 (실제 DB 확인)

```bash
cd backend
./gradlew clean test --tests "com.petpro.e2e.user.*" --no-build-cache -De2e.profiles=test,test-pg
```

### 전체 테스트 실행

```bash
cd backend
./gradlew test
```

### 프로필 전환 구조

| 항목 | H2 모드 (기본) | PostgreSQL 모드 |
|------|---------------|----------------|
| 프로필 | `test` | `test,test-pg` |
| 설정 파일 | `application-test.yml` | `application-test.yml` + `application-test-pg.yml` |
| DB | H2 인메모리 | 로컬 PostgreSQL (`petpro`) |
| 로그/쿠폰 DB | H2 인메모리 | H2 인메모리 (오버라이드 안 함) |
| 실행 후 데이터 | 자동 삭제 | **DB에 남음** (사용자가 확인 가능) |

### 주의사항
- PostgreSQL 모드는 로컬 petpro DB에 실제 데이터를 삽입함
- 테스트 후 데이터가 DB에 남으므로 필요시 수동 정리
- 로컬 PostgreSQL이 실행 중이어야 함 (localhost:5432)

---

## 주의사항

- `SUPER_ADMIN`은 `ROLE_SUPER_ADMIN` 권한만 가짐 (`ROLE_ADMIN` 아님)
- ADMIN 전용 API 테스트에는 ADMIN 사용자를, SUPER_ADMIN 전용 API에는 SUPER_ADMIN 사용자를 별도 생성
- H2의 `MODE=PostgreSQL` 호환 모드에서 `PostgreSQLDialect` DDL이 동작함
- `context-path: /api`가 설정되어 있으므로 모든 URL에 `/api` 접두사 포함
- `@RequestBody` DTO에는 반드시 `@NoArgsConstructor @AllArgsConstructor`를 `@Builder`와 함께 사용해야 함 (단일 필드 DTO에서 Jackson 역직렬화 실패 방지)
