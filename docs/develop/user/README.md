# 사용자 (User)

## 개요

사용자 정보 관리를 담당하는 도메인입니다.
PetPro 플랫폼에서 반려인(CUSTOMER)과 펫시터(PARTNER)를 모두 포함합니다.

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

### UserRole

| 값 | 설명 |
|----|------|
| CUSTOMER | 반려인 - 시터 검색, 예약, 결제, 돌봄 조회 |
| PARTNER | 펫시터 - 프로필/자격 관리, 예약 수락/거절, 돌봄 일지 |
| ADMIN | 관리자 - 일반 관리 기능 |
| SUPER_ADMIN | 최고 관리자 - 전체 시스템 관리 |

> **마이그레이션 참고**: 기존 `USER` 역할은 `CUSTOMER`로 전환됩니다.

### UserStatus

| 값 | 설명 |
|----|------|
| ACTIVE | 활성 |
| INACTIVE | 비활성 |
| SUSPENDED | 정지 |

---

## 온보딩 (사용자 유형 선택)

회원가입 후 최초 로그인 시 사용자 유형을 선택합니다.

### 플로우
1. 소셜/이메일 로그인 완료
2. **사용자 유형 선택 화면** 표시 (CUSTOMER / PARTNER)
3. 선택한 유형에 따라 role 설정
4. CUSTOMER → 초기 프로필 설정 → 펫 등록 안내
5. PARTNER → 본인 인증 → 프로필 4단계 설정 → 자격 증빙 → 심사 대기

### 비즈니스 규칙
- 최초 가입 시 role은 반드시 CUSTOMER 또는 PARTNER 중 하나 선택
- 한 번 선택한 역할은 직접 변경 불가 (관리자 처리 필요)
- ADMIN/SUPER_ADMIN은 관리자가 직접 부여

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/users | 목록 조회 | ADMIN, SUPER_ADMIN |
| POST | /api/v1/users | 생성 | ADMIN, SUPER_ADMIN |
| GET | /api/v1/users/{id} | 상세 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/users/{id} | 수정 | 본인 또는 ADMIN |
| DELETE | /api/v1/users/{id} | 삭제 | ADMIN, SUPER_ADMIN |
| GET | /api/v1/users/me | 내 정보 조회 | 인증된 사용자 |
| PUT | /api/v1/users/me | 내 정보 수정 | 인증된 사용자 |

---

## 비즈니스 규칙

1. 이메일은 시스템 전체에서 고유해야 함
2. 전화번호 형식 검증: 010-XXXX-XXXX
3. 삭제는 Soft Delete (status = INACTIVE)
4. 본인 정보는 본인만 수정 가능 (ADMIN 제외)
5. CUSTOMER/PARTNER 역할 전환은 관리자만 가능

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [frontend.md](./frontend.md) | **프론트엔드 UI 지침** |
| [api.md](./api.md) | **사용자 API 지침** |
| [e2e.md](./e2e.md) | **E2E 테스트 지침** |
