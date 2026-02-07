# 인증/인가 (Auth)

## 개요

사용자 인증 및 권한 관리를 담당하는 도메인입니다.

---

## 기능 목록

| 기능 | 설명 |
|------|------|
| 로그인 | 이메일/비밀번호 인증 |
| 로그아웃 | 토큰 무효화 |
| 토큰 갱신 | Access Token 재발급 |
| 회원가입 | 신규 사용자 등록 |
| 비밀번호 재설정 | 이메일 인증 후 재설정 |

---

## API 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/v1/auth/login | 로그인 | X |
| POST | /api/v1/auth/logout | 로그아웃 | O |
| POST | /api/v1/auth/refresh | 토큰 갱신 | X (Refresh Token) |
| POST | /api/v1/auth/register | 회원가입 | X |
| POST | /api/v1/auth/password/reset | 비밀번호 재설정 요청 | X |
| POST | /api/v1/auth/password/reset/confirm | 비밀번호 재설정 확인 | X |

---

## 사용자 역할

| 역할 | 코드 | 설명 |
|------|------|------|
| 반려인 | CUSTOMER | 시터 검색, 예약, 결제, 돌봄 조회, 채팅, 커뮤니티 |
| 펫시터 | PARTNER | 프로필/자격 관리, 예약 수락/거절, 돌봄 일지, 정산 |
| 관리자 | ADMIN | 일반 관리 기능 |
| 최고 관리자 | SUPER_ADMIN | 전체 시스템 관리 |

---

## 토큰 정책

| 토큰 | 만료 시간 | 저장 위치 |
|------|-----------|-----------|
| Access Token | 1시간 | Memory |
| Refresh Token | 14일 | HttpOnly Cookie |

---

## 비즈니스 규칙

1. 로그인 5회 실패 시 30분 계정 잠금
2. 비밀번호는 최소 8자, 영문/숫자/특수문자 포함
3. Refresh Token은 1회 사용 후 재발급 (Rotation)
4. 비밀번호 재설정 링크는 1시간 유효

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [login.md](./login.md) | 로그인 상세 |
| [token.md](./token.md) | 토큰 관리 |
| [register.md](./register.md) | 회원가입 |
| [password.md](./password.md) | 비밀번호 관리 |
