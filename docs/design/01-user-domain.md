# 회원 도메인 설계

## 1. 개요

회원(User) 도메인은 PetPro 플랫폼의 핵심 도메인으로, 사용자 인증/인가, 프로필 관리, 주소 기반 서비스를 담당합니다.

---

## 2. 엔티티 설계

### 2.1 User 엔티티

```
┌─────────────────────────────────────────────────────────────┐
│                          User                                │
├─────────────────────────────────────────────────────────────┤
│  id                  BIGINT PK AUTO_INCREMENT               │
│  email               VARCHAR(255) NOT NULL UNIQUE           │
│  password            VARCHAR(255) NOT NULL                  │
│  name                VARCHAR(100) NOT NULL                  │
│  phone               VARCHAR(20)                            │
│  birthDate           DATE                                   │
│                                                              │
│  [주소 정보]                                                  │
│  address             VARCHAR(500)                           │
│  addressDetail       VARCHAR(200)                           │
│  zipCode             VARCHAR(10)                            │
│  latitude            DECIMAL(10,7)                          │
│  longitude           DECIMAL(10,7)                          │
│                                                              │
│  [역할 및 상태]                                               │
│  role                VARCHAR(20) DEFAULT 'USER'             │
│  status              VARCHAR(20) DEFAULT 'ACTIVE'           │
│                                                              │
│  [프로필]                                                     │
│  profileImageUrl     VARCHAR(500)                           │
│                                                              │
│  [로그인 정보]                                                │
│  lastLoginAt         TIMESTAMP                              │
│                                                              │
│  [Soft Delete]                                               │
│  deletedAt           TIMESTAMP                              │
│  deletedBy           BIGINT                                 │
│                                                              │
│  [Audit]                                                     │
│  createdAt           TIMESTAMP NOT NULL                     │
│  updatedAt           TIMESTAMP NOT NULL                     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 UserRole 열거형

| 값 | 설명 | 권한 |
|----|------|------|
| USER | 일반 사용자 | 기본 서비스 이용 |
| COMPANY_ADMIN | 업체 관리자 | 업체 정보 관리 |
| SUPPLIER_ADMIN | 공급사 관리자 | 공급사 정보 관리 |
| ADMIN | 관리자 | 시스템 관리 (회원 상태 변경) |
| SUPER_ADMIN | 최고 관리자 | 모든 권한 (역할 변경 포함) |

### 2.3 UserStatus 열거형

| 값 | 설명 | 서비스 이용 |
|----|------|------------|
| ACTIVE | 활성 | 모든 서비스 이용 가능 |
| INACTIVE | 비활성 | 로그인 가능, 일부 기능 제한 |
| SUSPENDED | 정지 | 로그인 불가 |
| DELETED | 삭제됨 | Soft Delete 처리됨 |

---

## 3. API 설계

### 3.1 인증 API (공개)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /auth/register | 회원가입 |
| POST | /auth/login | 로그인 |
| POST | /auth/refresh | 토큰 갱신 |

### 3.2 회원 API (인증 필요)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /users/me | 내 정보 조회 | 인증 |
| PUT | /users/{id} | 회원 정보 수정 | 본인 또는 ADMIN |
| PUT | /users/{id}/password | 비밀번호 변경 | 본인만 |
| DELETE | /users/{id} | 회원 탈퇴 | 본인 또는 ADMIN |

### 3.3 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /users | 회원 목록 조회 | ADMIN |
| GET | /users/{id} | 회원 상세 조회 | ADMIN |
| GET | /users/search | 회원 검색 | ADMIN |
| POST | /users | 회원 생성 | ADMIN |
| PATCH | /users/{id}/status | 상태 변경 | ADMIN |
| PATCH | /users/{id}/role | 역할 변경 | SUPER_ADMIN |

---

## 4. 요청/응답 DTO

### 4.1 회원가입 요청

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "홍길동",
  "phone": "010-1234-5678"
}
```

### 4.2 로그인 요청

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### 4.3 토큰 응답

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

### 4.4 회원 정보 응답

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birthDate": "1990-05-15",
  "address": "서울특별시 강남구 테헤란로 123",
  "addressDetail": "101동 202호",
  "zipCode": "06141",
  "latitude": 37.5065,
  "longitude": 127.0536,
  "role": "USER",
  "status": "ACTIVE",
  "profileImageUrl": "https://storage.petpro.com/profiles/1.jpg",
  "lastLoginAt": "2025-01-25T09:30:00",
  "createdAt": "2025-01-20T14:30:00",
  "updatedAt": "2025-01-25T10:00:00"
}
```

### 4.5 프로필 수정 요청

```json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birthDate": "1990-05-15",
  "address": "서울특별시 강남구 테헤란로 123",
  "addressDetail": "101동 202호",
  "zipCode": "06141",
  "profileImageUrl": "https://storage.petpro.com/profiles/1.jpg"
}
```

### 4.6 비밀번호 변경 요청

```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!"
}
```

---

## 5. 비즈니스 로직

### 5.1 회원가입 프로세스

```
1. 이메일 중복 확인
2. 비밀번호 정책 검증 (8자 이상, 영문/숫자/특수문자 포함)
3. 비밀번호 BCrypt 암호화
4. User 엔티티 생성 (role: USER, status: ACTIVE)
5. DB 저장 (Master)
6. JWT 토큰 발급
7. 회원가입 완료 이벤트 발행 (쿠폰 발급용)
```

### 5.2 로그인 프로세스

```
1. 이메일로 회원 조회
2. 계정 상태 확인 (ACTIVE만 로그인 가능)
3. 비밀번호 검증
4. 마지막 로그인 시간 갱신
5. JWT 토큰 발급 (Access + Refresh)
```

### 5.3 주소 변경 시 좌표 변환

```
1. 새 주소 입력
2. Google Geocoding API로 좌표 변환
3. latitude, longitude 저장
4. 근처 장례식장 검색에 활용
```

### 5.4 회원 탈퇴 (Soft Delete)

```
1. 비밀번호 확인
2. deletedAt = 현재 시간
3. deletedBy = 처리 관리자 ID (본인 탈퇴시 본인 ID)
4. status = DELETED
5. 개인정보 마스킹 (이메일, 이름, 전화번호)
```

---

## 6. 보안

### 6.1 비밀번호 정책

- 최소 8자 이상
- 영문 대소문자, 숫자, 특수문자 중 3가지 이상 포함
- BCrypt 암호화 (Strength: 10)

### 6.2 JWT 토큰

- Access Token: 1시간 유효
- Refresh Token: 7일 유효
- Claims: userId, email, roles

### 6.3 권한 체크

```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
```

---

## 7. 인덱스

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_birth_month_day ON users(
  EXTRACT(MONTH FROM birth_date),
  EXTRACT(DAY FROM birth_date)
);
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
```

---

## 8. 이벤트

### 8.1 발행 이벤트

| 이벤트 | 발생 시점 | 용도 |
|--------|----------|------|
| UserSignupEvent | 회원가입 완료 | 쿠폰 자동 발급 |
| UserLoginEvent | 로그인 완료 | 로그, 통계 |
| UserDeleteEvent | 회원 탈퇴 | 관련 데이터 정리 |

### 8.2 이벤트 구조

```java
public record UserSignupEvent(
    Long userId,
    String email,
    LocalDateTime signupAt
) {}
```

---

## 9. 연관 관계

```
User (1) ──────────── (N) Pet           : 회원은 여러 반려동물 보유
User (1) ──────────── (N) UserCoupon    : 회원은 여러 쿠폰 보유 (Coupon DB)
User (1) ──────────── (N) Order         : 회원은 여러 주문 가능
User (1) ──────────── (N) Reservation   : 회원은 여러 예약 가능
```

---

## 10. 프론트엔드 연동

### 10.1 인증 상태 관리 (Zustand)

```typescript
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  login(user: User, accessToken: string, refreshToken: string): void;
  logout(): void;
}
```

### 10.2 토큰 자동 갱신

```typescript
// Axios 인터셉터
// 401 에러 발생 시 Refresh Token으로 자동 갱신
// 갱신 실패 시 로그아웃 처리
```

---

## 11. 참고 사항

- 이메일은 변경 불가 (로그인 ID로 사용)
- 비밀번호 변경 시 현재 비밀번호 확인 필수
- 관리자가 생성한 회원은 초기 비밀번호 변경 필요 플래그 추가 고려
- GDPR 대응을 위한 개인정보 내보내기 기능 추후 추가 고려
