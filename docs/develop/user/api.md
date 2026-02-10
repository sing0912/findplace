# User 도메인 - API 지침

**최종 수정일:** 2026-02-05
**상태:** 확정

---

## 1. API 목록

### 1.1 사용자 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/users/me | 내 정보 조회 | ✅ |
| 2 | PUT | /api/v1/users/me | 내 정보 수정 | ✅ |
| 3 | DELETE | /api/v1/users/me | 회원 탈퇴 | ✅ |
| 4 | POST | /api/v1/users/me/profile-image | 프로필 이미지 업로드 | ✅ |
| 5 | PUT | /api/v1/users/me/password | 비밀번호 변경 | ✅ |

### 1.2 관리자 API

| # | Method | Endpoint | 설명 | 권한 |
|---|--------|----------|------|------|
| 6 | POST | /api/v1/users | 사용자 생성 | ADMIN |
| 7 | GET | /api/v1/users | 사용자 목록 조회 | ADMIN |
| 8 | GET | /api/v1/users/search | 사용자 검색 | ADMIN |
| 9 | GET | /api/v1/users/{id} | 사용자 조회 | ADMIN 또는 본인 |
| 10 | PUT | /api/v1/users/{id} | 사용자 수정 | ADMIN 또는 본인 |
| 11 | PUT | /api/v1/users/{id}/password | 비밀번호 변경 | 본인만 |
| 12 | DELETE | /api/v1/users/{id} | 사용자 삭제 (Soft Delete) | ADMIN |
| 13 | PATCH | /api/v1/users/{id}/status | 사용자 상태 변경 | ADMIN |
| 14 | PATCH | /api/v1/users/{id}/role | 사용자 역할 변경 | SUPER_ADMIN |

---

## 2. 내 정보 조회

```
GET /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "phone": "01012345678",
  "profileImageUrl": "https://...",
  "provider": "EMAIL",
  "createdAt": "2026-02-05T10:00:00Z"
}
```

---

## 3. 내 정보 수정

```
PUT /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Request:
{ "nickname": "새닉네임" }

Response 200:
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "새닉네임",
  "phone": "01012345678",
  "profileImageUrl": "https://...",
  "provider": "EMAIL",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error 400:
{ "code": "DUPLICATE_NICKNAME", "message": "이미 사용 중인 닉네임입니다." }
```

---

## 4. 프로필 이미지 업로드

```
POST /api/v1/users/me/profile-image

Headers:
  Authorization: Bearer {accessToken}
  Content-Type: multipart/form-data

Request:
  file: (이미지 파일, max 5MB, jpg/png/gif)

Response 200:
{ "profileImageUrl": "https://..." }

Error 400:
{ "code": "INVALID_FILE_TYPE", "message": "지원하지 않는 파일 형식입니다." }
{ "code": "FILE_TOO_LARGE", "message": "파일 크기는 5MB 이하여야 합니다." }
```

---

## 5. 비밀번호 변경

```
PUT /api/v1/users/me/password

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}

Response 200:
{ "success": true, "message": "비밀번호가 변경되었습니다." }

Error 400:
{ "code": "INVALID_PASSWORD", "message": "현재 비밀번호가 일치하지 않습니다." }
{ "code": "WEAK_PASSWORD", "message": "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다." }
```

---

## 6. 회원 탈퇴

```
DELETE /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Request (선택적, 소셜 로그인 사용자는 생략 가능):
{ "password": "password123" }

Response 204: (응답 본문 없음)

Error 400:
{ "code": "INVALID_PASSWORD", "message": "비밀번호가 일치하지 않습니다." }
```

---

## 7. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 Jackson 역직렬화를 위해 다음 어노테이션 조합을 사용한다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```

- `@NoArgsConstructor`: Jackson이 기본 생성자로 객체를 생성하고 setter/field 접근으로 값 주입
- `@AllArgsConstructor`: `@Builder`가 요구하는 전체 필드 생성자 제공
- **특히 단일 필드 DTO**에서 `@NoArgsConstructor` 없이 `@Builder`만 사용하면 Jackson이 단일 파라미터 생성자를 "delegating creator"로 해석하여 역직렬화 실패 (500 에러)

---

## 8. User Entity 확장

```java
@Entity
public class User {
    // 기존 필드
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;

    // 추가 필드
    private String nickname;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;  // EMAIL, KAKAO, NAVER, GOOGLE

    private String providerId;  // 소셜 로그인 ID

    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeMarketing;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 8.1 UserStatus

```java
public enum UserStatus {
    ACTIVE,     // 활성
    INACTIVE,   // 휴면 (1년 미접속)
    SUSPENDED,  // 정지 (관리자에 의한 정지)
    DELETED     // 삭제 (회원 탈퇴 - Soft Delete)
}
```

---

## 9. 관리자 API 상세

### 9.1 사용자 생성

```
POST /api/v1/users

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "email": "user@example.com",
  "password": "password123!",
  "name": "홍길동",
  "nickname": "길동이",
  "phone": "01012345678"
}

Response 201:
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "길동이",
  ...
}
```

**권한**: ADMIN

### 9.2 사용자 목록 조회

```
GET /api/v1/users?page=0&size=20&sort=createdAt,desc

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "email": "user@example.com", "nickname": "길동이", "status": "ACTIVE", ... }
    ],
    "page": { "number": 0, "size": 20, "totalElements": 100, "totalPages": 5 }
  }
}
```

**권한**: ADMIN

### 9.3 사용자 검색

```
GET /api/v1/users/search?keyword=홍길동&page=0&size=20

Headers:
  Authorization: Bearer {accessToken}

Response 200: (목록 조회와 동일 형식)
```

**권한**: ADMIN

### 9.4 사용자 조회

```
GET /api/v1/users/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{ "id": 1, "email": "user@example.com", "nickname": "길동이", ... }
```

**권한**: ADMIN 또는 본인 (`@PreAuthorize`)

### 9.5 사용자 수정

```
PUT /api/v1/users/{id}

Headers:
  Authorization: Bearer {accessToken}

Request:
{ "name": "새이름", "phone": "01099998888" }

Response 200:
{ "id": 1, "email": "user@example.com", "name": "새이름", ... }
```

**권한**: ADMIN 또는 본인

### 9.6 비밀번호 변경 (관리자 경로)

```
PUT /api/v1/users/{id}/password

Headers:
  Authorization: Bearer {accessToken}

Request:
{ "currentPassword": "old123!", "newPassword": "new456!" }

Response 200:
{ "success": true, "message": "비밀번호가 변경되었습니다." }
```

**권한**: 본인만

### 9.7 사용자 삭제 (Soft Delete)

```
DELETE /api/v1/users/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 204: (응답 본문 없음)
```

**권한**: ADMIN

### 9.8 사용자 상태 변경

```
PATCH /api/v1/users/{id}/status?status=SUSPENDED

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{ "id": 1, "status": "SUSPENDED", ... }
```

**권한**: ADMIN

### 9.9 사용자 역할 변경

```
PATCH /api/v1/users/{id}/role?role=PARTNER

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{ "id": 1, "role": "PARTNER", ... }
```

**권한**: SUPER_ADMIN
