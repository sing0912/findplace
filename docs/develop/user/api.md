# User 도메인 - API 지침

**최종 수정일:** 2026-02-05
**상태:** 확정

---

## 1. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/users/me | 내 정보 조회 | ✅ |
| 2 | PUT | /api/v1/users/me | 내 정보 수정 | ✅ |
| 3 | DELETE | /api/v1/users/me | 회원 탈퇴 | ✅ |
| 4 | POST | /api/v1/users/me/profile-image | 프로필 이미지 업로드 | ✅ |
| 5 | PUT | /api/v1/users/me/password | 비밀번호 변경 | ✅ |

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
{ "code": "WEAK_PASSWORD", "message": "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다." }
```

---

## 6. 회원 탈퇴

```
DELETE /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Request:
{ "password": "password123" }

Response 200:
{ "success": true, "message": "회원 탈퇴가 완료되었습니다." }

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
