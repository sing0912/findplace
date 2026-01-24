# 사용자 (User)

## 개요

사용자 정보 관리를 담당하는 도메인입니다.

---

## 엔티티

### User

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| email | String | 이메일 | Unique, Not Null |
| password | String | 비밀번호 (암호화) | Not Null |
| name | String | 이름 | Not Null |
| phone | String | 전화번호 | Not Null |
| role | Enum | 역할 | Not Null |
| status | Enum | 상태 | Not Null |
| profileImageUrl | String | 프로필 이미지 | Nullable |
| lastLoginAt | DateTime | 마지막 로그인 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### UserStatus

| 값 | 설명 |
|----|------|
| ACTIVE | 활성 |
| INACTIVE | 비활성 |
| SUSPENDED | 정지 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/users | 목록 조회 | PLATFORM_ADMIN |
| POST | /api/v1/users | 생성 | PLATFORM_ADMIN |
| GET | /api/v1/users/{id} | 상세 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/users/{id} | 수정 | 본인 또는 ADMIN |
| DELETE | /api/v1/users/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/users/me | 내 정보 조회 | 인증된 사용자 |
| PUT | /api/v1/users/me | 내 정보 수정 | 인증된 사용자 |

---

## 비즈니스 규칙

1. 이메일은 시스템 전체에서 고유해야 함
2. 전화번호 형식 검증: 010-XXXX-XXXX
3. 삭제는 Soft Delete (status = INACTIVE)
4. 본인 정보는 본인만 수정 가능 (ADMIN 제외)

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | CRUD 상세 |
| [profile.md](./profile.md) | 프로필 관리 |
