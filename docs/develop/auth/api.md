# Auth 도메인 - API 지침

**최종 수정일:** 2026-02-05
**상태:** 확정

---

## 1. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | /v1/auth/register | 이메일 회원가입 | X |
| 2 | POST | /v1/auth/login | 로그인 | X |
| 3 | POST | /v1/auth/logout | 로그아웃 | ✅ |
| 4 | POST | /v1/auth/refresh | 토큰 갱신 | X (Refresh Token) |
| 5 | GET | /v1/auth/check-email | 이메일 중복확인 | X |
| 6 | GET | /v1/auth/check-nickname | 닉네임 중복확인 | X |
| 7 | POST | /v1/auth/oauth/{provider}/callback | 소셜 로그인 | X |
| 8 | POST | /v1/auth/find-id/request | 아이디 찾기 - 인증요청 | X |
| 9 | POST | /v1/auth/find-id/verify | 아이디 찾기 - 인증확인 | X |
| 10 | POST | /v1/auth/find-id/resend | 아이디 찾기 - 재전송 | X |
| 11 | POST | /v1/auth/reset-password/request | 비밀번호 재설정 - 인증요청 | X |
| 12 | POST | /v1/auth/reset-password/verify | 비밀번호 재설정 - 인증확인 | X |
| 13 | POST | /v1/auth/reset-password/confirm | 비밀번호 재설정 - 변경 | X |

---

## 2. 회원가입

```
POST /v1/auth/register

Request:
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "홍길동",
  "nickname": "길동이",
  "phone": "01012345678",
  "agreeTerms": true,
  "agreePrivacy": true,
  "agreeMarketing": false
}

Response 201:
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "홍길동",
  "createdAt": "2026-02-05T10:00:00Z"
}

Error 400:
{ "code": "DUPLICATE_EMAIL", "message": "이미 사용 중인 이메일입니다." }
{ "code": "DUPLICATE_NICKNAME", "message": "이미 사용 중인 닉네임입니다." }
{ "code": "INVALID_PASSWORD", "message": "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다." }
```

---

## 3. 이메일/닉네임 중복확인

```
GET /v1/auth/check-email?email=user@example.com

Response 200:
{ "available": true }
{ "available": false, "message": "이미 사용 중인 이메일입니다." }
```

```
GET /v1/auth/check-nickname?nickname=홍길동

Response 200:
{ "available": true }
{ "available": false, "message": "이미 사용 중인 닉네임입니다." }
```

---

## 4. 소셜 로그인

```
POST /v1/auth/oauth/{provider}/callback

Provider: kakao, naver, google

Request:
{ "code": "authorization_code" }

Response 200:
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

Error 400:
{ "code": "INVALID_PROVIDER", "message": "지원하지 않는 OAuth 제공자입니다." }
```

### 4.1 OAuth 로그인 비즈니스 로직

```
1. email로 기존 사용자 검색 (email이 unique이므로 먼저 검색)
   └─ 있으면: OAuth 정보(provider, providerId) 업데이트 후 로그인

2. 없으면 provider + providerId로 기존 사용자 검색
   └─ 있으면: 해당 사용자로 로그인

3. 둘 다 없으면: 신규 사용자 생성
   - email: OAuth 제공자에서 받은 이메일
   - provider: OAuth 제공자 (KAKAO, NAVER, GOOGLE)
   - providerId: OAuth 제공자에서 받은 고유 ID
   - 기본 역할: CUSTOMER
   - 약관 동의: 자동 true (OAuth 동의 화면에서 처리)
```

### 4.2 User.updateOAuthInfo()

```java
/**
 * OAuth 정보 업데이트 (이메일 사용자가 소셜 로그인 연동 시)
 */
public void updateOAuthInfo(AuthProvider provider, String providerId) {
    this.provider = provider;
    this.providerId = providerId;
}
```

---

## 5. 아이디 찾기

### 5.1 인증요청
```
POST /v1/auth/find-id/request

Request:
{ "name": "홍길동", "phone": "01012345678" }

Response 200:
{ "requestId": "uuid", "expireAt": "2026-02-05T10:03:00Z" }

Error 404:
{ "code": "USER_NOT_FOUND", "message": "일치하는 계정을 찾을 수 없습니다." }
```

### 5.2 인증확인
```
POST /v1/auth/find-id/verify

Request:
{ "requestId": "uuid", "code": "123456" }

Response 200:
{ "email": "use***@exa***.com" }

Error 400:
{ "code": "INVALID_CODE", "message": "인증번호가 일치하지 않습니다." }
{ "code": "CODE_EXPIRED", "message": "인증번호가 만료되었습니다." }
```

### 5.3 재전송
```
POST /v1/auth/find-id/resend

Request:
{ "requestId": "uuid" }

Response 200:
{ "requestId": "uuid", "expireAt": "2026-02-05T10:06:00Z" }
```

---

## 6. 비밀번호 재설정

### 6.1 인증요청
```
POST /v1/auth/reset-password/request

Request:
{ "email": "user@example.com", "phone": "01012345678" }

Response 200:
{ "requestId": "uuid", "expireAt": "2026-02-05T10:03:00Z" }
```

### 6.2 인증확인
```
POST /v1/auth/reset-password/verify

Request:
{ "requestId": "uuid", "code": "123456" }

Response 200:
{ "token": "password_reset_token" }
```

### 6.3 변경
```
POST /v1/auth/reset-password/confirm

Request:
{ "token": "password_reset_token", "newPassword": "newpassword123" }

Response 200:
{ "success": true, "message": "비밀번호가 변경되었습니다." }
```

---

## 7. Entity

### 7.1 AuthProvider (Enum)
```java
public enum AuthProvider {
    EMAIL, KAKAO, NAVER, GOOGLE
}
```

### 7.2 VerificationType (Enum)
```java
public enum VerificationType {
    FIND_ID, RESET_PASSWORD
}
```

### 7.3 VerificationRequest
```java
@Entity
public class VerificationRequest {
    @Id
    private String id;  // UUID

    @Enumerated(EnumType.STRING)
    private VerificationType type;

    private String phone;
    private String code;  // 6자리
    private Long userId;

    private LocalDateTime expireAt;
    private boolean verified;
    private LocalDateTime createdAt;
}
```

---

## 8. SMS 서비스

### 8.1 구조

```
SmsService (인터페이스)
├── MockSmsService (local/test 프로필) - 콘솔 로그 출력
└── CoolSmsService (prod 프로필)     - 실제 SMS 발송 (미구현)
```

### 8.2 SmsService 인터페이스

```java
public interface SmsService {
    void sendVerificationCode(String phone, String code);
}
```

### 8.3 MockSmsService (local, test 프로필)

- `@Profile({"local", "test"})` 로 활성화
- 실제 SMS 발송하지 않음
- 콘솔에 `[SMS MOCK] 010xxxx -> 인증번호: 123456` 형식으로 출력
- 개발/테스트 시 로그에서 인증번호 확인 가능

### 8.4 CoolSmsService (prod 프로필, 미구현)

- `@Profile("prod")` 로 활성화 예정
- CoolSMS (SOLAPI) SDK 사용: `net.nurigo:sdk:4.3.0`
- 사업자등록증 불필요 (개인 가입 가능)
- 건당 약 18원

### 8.5 AuthService 연동

`findIdRequest()`, `findIdResend()`, `resetPasswordRequest()` 에서 `smsService.sendVerificationCode()` 호출.

---

## 9. 환경변수

```properties
# OAuth
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# SMS (prod 환경)
SMS_API_KEY=
SMS_API_SECRET=
SMS_SENDER_NUMBER=
```
