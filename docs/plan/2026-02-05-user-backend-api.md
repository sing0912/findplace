# User 도메인 백엔드 API 구현 계획

**작성일:** 2026-02-05
**상태:** 승인됨
**도메인:** user, auth
**유형:** 백엔드 API 구현

---

## 1. 개요

프론트엔드에서 필요한 User 도메인 관련 백엔드 API를 구현합니다.

### 1.1 연관 지침
- 프론트엔드: `docs/develop/user/frontend.md`
- 백엔드: `docs/develop/user/README.md`, `docs/develop/auth/`

---

## 2. API 목록

### 2.1 인증 (Auth)

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | POST | /api/v1/auth/register | 이메일 회원가입 |
| 2 | POST | /api/v1/auth/login | 로그인 (이메일) |
| 3 | POST | /api/v1/auth/logout | 로그아웃 |
| 4 | GET | /api/v1/auth/check-email | 이메일 중복확인 |
| 5 | GET | /api/v1/auth/check-nickname | 닉네임 중복확인 |
| 6 | POST | /api/v1/auth/oauth/{provider} | 소셜 로그인 |
| 7 | POST | /api/v1/auth/find-id/request | 아이디 찾기 - 인증요청 |
| 8 | POST | /api/v1/auth/find-id/verify | 아이디 찾기 - 인증확인 |
| 9 | POST | /api/v1/auth/find-id/resend | 아이디 찾기 - 재전송 |
| 10 | POST | /api/v1/auth/reset-password/request | 비밀번호 재설정 - 인증요청 |
| 11 | POST | /api/v1/auth/reset-password/verify | 비밀번호 재설정 - 인증확인 |
| 12 | POST | /api/v1/auth/reset-password/confirm | 비밀번호 재설정 - 변경 |

### 2.2 사용자 (User)

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | GET | /api/v1/users/me | 내 정보 조회 |
| 2 | PUT | /api/v1/users/me | 내 정보 수정 |
| 3 | DELETE | /api/v1/users/me | 회원 탈퇴 |
| 4 | POST | /api/v1/users/me/profile-image | 프로필 이미지 업로드 |
| 5 | PUT | /api/v1/users/me/password | 비밀번호 변경 |

### 2.3 문의 (Inquiry)

| # | Method | Endpoint | 설명 |
|---|--------|----------|------|
| 1 | GET | /api/v1/inquiries | 내 문의 목록 |
| 2 | POST | /api/v1/inquiries | 문의 작성 |
| 3 | GET | /api/v1/inquiries/{id} | 문의 상세 |
| 4 | PUT | /api/v1/inquiries/{id} | 문의 수정 |
| 5 | DELETE | /api/v1/inquiries/{id} | 문의 삭제 |

---

## 3. API 상세 스펙

### 3.1 회원가입

```
POST /api/v1/auth/register

Request:
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "홍길동",
  "agreeTerms": true,
  "agreePrivacy": true,
  "agreeMarketing": false
}

Response (201):
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error (400):
{
  "code": "DUPLICATE_EMAIL",
  "message": "이미 사용 중인 이메일입니다."
}
```

### 3.2 이메일 중복확인

```
GET /api/v1/auth/check-email?email=user@example.com

Response (200):
{
  "available": true
}

Response (200):
{
  "available": false,
  "message": "이미 사용 중인 이메일입니다."
}
```

### 3.3 닉네임 중복확인

```
GET /api/v1/auth/check-nickname?nickname=홍길동

Response (200):
{
  "available": true
}

Response (200):
{
  "available": false,
  "message": "이미 사용 중인 닉네임입니다."
}
```

### 3.4 소셜 로그인

```
POST /api/v1/auth/oauth/{provider}

Provider: kakao, naver, google

Request:
{
  "code": "authorization_code_from_oauth"
}

Response (200) - 기존 회원:
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "홍길동",
    "profileImageUrl": "https://..."
  },
  "isNewUser": false
}

Response (200) - 신규 회원:
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token",
  "user": {
    "id": 2,
    "email": "newuser@example.com",
    "nickname": "카카오유저123",
    "profileImageUrl": null
  },
  "isNewUser": true
}
```

### 3.5 아이디 찾기 - 인증요청

```
POST /api/v1/auth/find-id/request

Request:
{
  "name": "홍길동",
  "phone": "01012345678"
}

Response (200):
{
  "requestId": "uuid-request-id",
  "expireAt": "2026-02-05T10:03:00Z"
}

Error (404):
{
  "code": "USER_NOT_FOUND",
  "message": "일치하는 계정을 찾을 수 없습니다."
}
```

### 3.6 아이디 찾기 - 인증확인

```
POST /api/v1/auth/find-id/verify

Request:
{
  "requestId": "uuid-request-id",
  "code": "123456"
}

Response (200):
{
  "email": "use***@exa***.com"
}

Error (400):
{
  "code": "INVALID_CODE",
  "message": "인증번호가 일치하지 않습니다."
}

Error (400):
{
  "code": "CODE_EXPIRED",
  "message": "인증번호가 만료되었습니다."
}
```

### 3.7 아이디 찾기 - 재전송

```
POST /api/v1/auth/find-id/resend

Request:
{
  "requestId": "uuid-request-id"
}

Response (200):
{
  "requestId": "uuid-request-id",
  "expireAt": "2026-02-05T10:06:00Z"
}
```

### 3.8 비밀번호 재설정 - 인증요청

```
POST /api/v1/auth/reset-password/request

Request:
{
  "email": "user@example.com",
  "phone": "01012345678"
}

Response (200):
{
  "requestId": "uuid-request-id",
  "expireAt": "2026-02-05T10:03:00Z"
}
```

### 3.9 비밀번호 재설정 - 인증확인

```
POST /api/v1/auth/reset-password/verify

Request:
{
  "requestId": "uuid-request-id",
  "code": "123456"
}

Response (200):
{
  "token": "password_reset_token"
}
```

### 3.10 비밀번호 재설정 - 변경

```
POST /api/v1/auth/reset-password/confirm

Request:
{
  "token": "password_reset_token",
  "newPassword": "newpassword123"
}

Response (200):
{
  "success": true,
  "message": "비밀번호가 변경되었습니다."
}
```

### 3.11 내 정보 조회

```
GET /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Response (200):
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

### 3.12 내 정보 수정

```
PUT /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "nickname": "새닉네임"
}

Response (200):
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "새닉네임",
  "phone": "01012345678",
  "profileImageUrl": "https://...",
  "provider": "EMAIL",
  "createdAt": "2026-02-05T10:00:00Z"
}
```

### 3.13 프로필 이미지 업로드

```
POST /api/v1/users/me/profile-image

Headers:
  Authorization: Bearer {accessToken}
  Content-Type: multipart/form-data

Request:
  file: (이미지 파일)

Response (200):
{
  "profileImageUrl": "https://..."
}
```

### 3.14 비밀번호 변경

```
PUT /api/v1/users/me/password

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword123"
}

Response (200):
{
  "success": true,
  "message": "비밀번호가 변경되었습니다."
}

Error (400):
{
  "code": "INVALID_PASSWORD",
  "message": "현재 비밀번호가 일치하지 않습니다."
}
```

### 3.15 회원 탈퇴

```
DELETE /api/v1/users/me

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "password": "password123"
}

Response (200):
{
  "success": true,
  "message": "회원 탈퇴가 완료되었습니다."
}
```

### 3.16 문의 목록

```
GET /api/v1/inquiries?page=0&size=10

Headers:
  Authorization: Bearer {accessToken}

Response (200):
{
  "content": [
    {
      "id": 1,
      "title": "상품 문의",
      "status": "WAITING",
      "createdAt": "2026-02-05T10:00:00Z"
    },
    {
      "id": 2,
      "title": "배송 문의",
      "status": "ANSWERED",
      "createdAt": "2026-02-04T10:00:00Z"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0
}
```

### 3.17 문의 작성

```
POST /api/v1/inquiries

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?"
}

Response (201):
{
  "id": 1,
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?",
  "status": "WAITING",
  "createdAt": "2026-02-05T10:00:00Z"
}
```

### 3.18 문의 상세

```
GET /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Response (200):
{
  "id": 1,
  "title": "상품 문의",
  "content": "상품 배송이 언제 되나요?",
  "status": "ANSWERED",
  "createdAt": "2026-02-05T10:00:00Z",
  "answer": {
    "content": "안녕하세요. 배송은 2-3일 소요됩니다.",
    "createdAt": "2026-02-05T11:00:00Z"
  }
}
```

### 3.19 문의 수정

```
PUT /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "title": "상품 문의 (수정)",
  "content": "수정된 내용"
}

Response (200):
{
  "id": 1,
  "title": "상품 문의 (수정)",
  "content": "수정된 내용",
  "status": "WAITING",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error (400):
{
  "code": "ALREADY_ANSWERED",
  "message": "답변이 완료된 문의는 수정할 수 없습니다."
}
```

### 3.20 문의 삭제

```
DELETE /api/v1/inquiries/{id}

Headers:
  Authorization: Bearer {accessToken}

Response (200):
{
  "success": true
}
```

---

## 4. 데이터 모델

### 4.1 User (기존 확장)

```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String phone;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;  // EMAIL, KAKAO, NAVER, GOOGLE

    private String providerId;  // 소셜 로그인 ID

    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeMarketing;

    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 4.2 VerificationRequest (신규)

```java
@Entity
public class VerificationRequest {
    @Id
    private String id;  // UUID

    @Enumerated(EnumType.STRING)
    private VerificationType type;  // FIND_ID, RESET_PASSWORD

    private String phone;
    private String code;  // 6자리 인증번호
    private Long userId;  // 대상 사용자

    private LocalDateTime expireAt;
    private boolean verified;

    private LocalDateTime createdAt;
}
```

### 4.3 Inquiry (신규)

```java
@Entity
public class Inquiry {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;  // WAITING, ANSWERED

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL)
    private InquiryAnswer answer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 4.4 InquiryAnswer (신규)

```java
@Entity
public class InquiryAnswer {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    private User admin;  // 답변 작성 관리자

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
}
```

---

## 5. 파일 구조

```
backend/src/main/java/com/petpro/
├── domain/
│   ├── user/
│   │   ├── entity/
│   │   │   ├── User.java (수정)
│   │   │   └── AuthProvider.java (신규)
│   │   ├── repository/
│   │   │   └── UserRepository.java (수정)
│   │   ├── service/
│   │   │   └── UserService.java (수정)
│   │   └── controller/
│   │       └── UserController.java (수정)
│   │
│   ├── auth/
│   │   ├── entity/
│   │   │   ├── VerificationRequest.java (신규)
│   │   │   └── VerificationType.java (신규)
│   │   ├── repository/
│   │   │   └── VerificationRequestRepository.java (신규)
│   │   ├── service/
│   │   │   ├── AuthService.java (수정)
│   │   │   ├── OAuthService.java (신규)
│   │   │   └── SmsService.java (신규)
│   │   ├── controller/
│   │   │   └── AuthController.java (수정)
│   │   └── dto/
│   │       ├── RegisterRequest.java
│   │       ├── LoginRequest.java
│   │       ├── OAuthRequest.java
│   │       ├── FindIdRequest.java
│   │       ├── ResetPasswordRequest.java
│   │       └── ...
│   │
│   └── inquiry/
│       ├── entity/
│       │   ├── Inquiry.java (신규)
│       │   ├── InquiryAnswer.java (신규)
│       │   └── InquiryStatus.java (신규)
│       ├── repository/
│       │   └── InquiryRepository.java (신규)
│       ├── service/
│       │   └── InquiryService.java (신규)
│       ├── controller/
│       │   └── InquiryController.java (신규)
│       └── dto/
│           ├── InquiryRequest.java
│           └── InquiryResponse.java
```

---

## 6. 구현 순서

| 순서 | 기능 | 파일 |
|------|------|------|
| 1 | Entity 추가/수정 | User, VerificationRequest, Inquiry |
| 2 | Repository 추가 | VerificationRequestRepository, InquiryRepository |
| 3 | 이메일/닉네임 중복확인 | AuthController |
| 4 | 회원가입 API | AuthService, AuthController |
| 5 | 소셜 로그인 API | OAuthService, AuthController |
| 6 | 아이디 찾기 API | AuthService, SmsService |
| 7 | 비밀번호 재설정 API | AuthService |
| 8 | 사용자 정보 API | UserService, UserController |
| 9 | 문의 게시판 API | InquiryService, InquiryController |

---

## 7. 환경 설정

### 7.1 소셜 로그인 환경변수

```properties
# Kakao
spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID}
spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET}

# Naver
spring.security.oauth2.client.registration.naver.client-id=${NAVER_CLIENT_ID}
spring.security.oauth2.client.registration.naver.client-secret=${NAVER_CLIENT_SECRET}

# Google
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

### 7.2 SMS 서비스 환경변수

```properties
# SMS (예: NHN Cloud, AWS SNS 등)
sms.provider=${SMS_PROVIDER}
sms.api-key=${SMS_API_KEY}
sms.sender=${SMS_SENDER_NUMBER}
```

---

## 8. 테스트

각 API에 대해 단위 테스트 및 통합 테스트 작성 필요.

---

## 9. 승인

위 설계 내용은 프론트엔드 지침과 함께 승인되었습니다.
