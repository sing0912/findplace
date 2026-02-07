# User 도메인 - 프론트엔드 지침

**최종 수정일:** 2026-02-05
**상태:** 확정
**플랜 참조:** `docs/plan/2026-02-05-user-frontend.md`

---

## 1. 개요

User 도메인의 프론트엔드 화면 구현 영구 지침입니다.

### 1.1 Figma 참조
- **URL:** https://www.figma.com/design/mXrXb73tJYn0qzE9jKgEUv/펫프로-와이어프레임

### 1.2 기능 목록

| # | 기능 | Figma |
|---|------|-------|
| 1 | 로그인 (카카오/네이버/구글) | ✅ |
| 2 | 아이디 찾기 (휴대폰 인증) | ✅ |
| 3 | 비밀번호 재설정 | ✅ |
| 4 | 회원가입 | ✅ |
| 5 | 마이페이지 | 구현 후 추가 |
| 6 | 회원정보 수정 | 구현 후 추가 |
| 7 | 문의 게시판 | 구현 후 추가 |

---

## 2. 디자인 시스템

### 2.1 색상

| 용도 | HEX | 사용처 |
|------|-----|--------|
| Primary | #76BCA2 | 메인 버튼, 강조, 체크 아이콘 |
| Kakao | #FEE500 | 카카오 로그인 버튼 |
| Naver | #03C75A | 네이버 로그인 버튼 |
| Google | #FFFFFF | 구글 로그인 버튼 (테두리 #DADCE0) |
| Text Primary | #000000 | 제목, 레이블 |
| Text Secondary | #404040 | 입력 텍스트, 본문 |
| Border | #AEAEAE | 입력창 테두리 |
| Background | #FFFFFF | 배경 |
| Error | #FF0000 | 에러 메시지 |
| Success | #76BCA2 | 성공 메시지, 체크 아이콘 |

### 2.2 타이포그래피

| 용도 | 폰트 | 크기 | 굵기 |
|------|------|------|------|
| 페이지 제목 | Noto Sans | 16px | Bold (700) |
| 필드 레이블 | Noto Sans | 14px | Regular (400) |
| 입력 텍스트 | Noto Sans KR | 14px | Regular (400) |
| 버튼 텍스트 | Noto Sans | 14px | Regular (400) |
| 본문 | Noto Sans KR | 14px | Regular (400) |

### 2.3 컴포넌트 스펙

#### 입력창 (AuthInput)
```
width: 100%
height: 50px
background: #FFFFFF
border: 1px solid #AEAEAE
border-radius: 5px
padding: 0 10px
font-size: 14px
```

#### Primary 버튼 (AuthButton)
```
width: 230px (또는 100%)
height: 45px
background: #76BCA2
border-radius: 10px
color: #FFFFFF
font-size: 14px
```

#### 소셜 로그인 버튼 (SocialLoginButton)
```
width: 100%
height: 50px
border-radius: 8px
font-size: 14px

Kakao: background #FEE500, color #000000
Naver: background #03C75A, color #FFFFFF
Google: background #FFFFFF, border 1px solid #DADCE0, color #000000
```

---

## 3. 로그인

### 3.1 화면: 로그인 시작 (LoginStartPage)

**경로:** `/login`

**구성:**
- 로고 및 슬로건
- 카카오 로그인 버튼
- 네이버 로그인 버튼
- 구글 로그인 버튼
- 이메일 회원가입 링크
- 아이디 찾기 / 비밀번호 찾기 링크

### 3.2 소셜 로그인 설정

| Provider | 환경변수 | Redirect URI |
|----------|---------|--------------|
| Kakao | REACT_APP_KAKAO_CLIENT_ID | /oauth/kakao/callback |
| Naver | REACT_APP_NAVER_CLIENT_ID | /oauth/naver/callback |
| Google | REACT_APP_GOOGLE_CLIENT_ID | /oauth/google/callback |

### 3.3 OAuth 콜백 (OAuthCallbackPage)

**경로:** `/oauth/:provider/callback`

**처리:**
1. Authorization Code 수신
2. 백엔드 API 호출하여 토큰 교환
3. 기존 회원 → 홈으로 이동
4. 신규 회원 → 추가정보 입력 또는 회원가입 완료

---

## 4. 아이디 찾기

### 4.1 화면: 정보 입력 (FindIdPage)

**경로:** `/find-id`

**필드:**
- 이름 (필수)
- 휴대폰 번호 (필수)

**동작:**
- 확인 버튼 클릭 → SMS 인증번호 발송 → 인증 화면 이동

### 4.2 화면: SMS 인증 (FindIdVerifyPage)

**경로:** `/find-id/verify`

**필드:**
- 인증번호 6자리 (필수)

**동작:**
- 타이머 3분 표시
- 재전송 버튼
- 확인 → 결과 화면 이동

### 4.3 화면: 결과 (FindIdResultPage)

**경로:** `/find-id/result`

**표시:**
- 마스킹된 이메일 (예: find***@na***.com)
- 로그인 버튼
- 비밀번호 찾기 링크

**실패 시:**
- 일치하는 계정 없음 메시지
- 회원가입 버튼
- 다시 찾기 버튼

### 4.4 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 인증요청 | POST | /api/v1/auth/find-id/request |
| 인증확인 | POST | /api/v1/auth/find-id/verify |
| 재전송 | POST | /api/v1/auth/find-id/resend |

---

## 5. 비밀번호 재설정

### 5.1 화면: 정보 입력 (ResetPasswordPage)

**경로:** `/reset-password`

**필드:**
- 이메일 (필수)
- 휴대폰 번호 (필수)

### 5.2 화면: SMS 인증 (ResetPasswordVerifyPage)

**경로:** `/reset-password/verify`

**필드:**
- 인증번호 6자리

### 5.3 화면: 새 비밀번호 (ResetPasswordConfirmPage)

**경로:** `/reset-password/confirm`

**필드:**
- 새 비밀번호 (8자 이상, 영문+숫자)
- 비밀번호 확인

### 5.4 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 인증요청 | POST | /api/v1/auth/reset-password/request |
| 인증확인 | POST | /api/v1/auth/reset-password/verify |
| 비밀번호 변경 | POST | /api/v1/auth/reset-password/confirm |

---

## 6. 회원가입

### 6.1 화면: 약관 동의 (RegisterStep1Page)

**경로:** `/register`

**체크박스:**
- 전체 동의
- [필수] 이용약관 동의 (보기 링크)
- [필수] 개인정보처리방침 동의 (보기 링크)
- [선택] 마케팅 정보 수신 동의

**동작:**
- 필수 항목 모두 체크 시 다음 버튼 활성화

### 6.2 화면: 정보 입력 (RegisterStep2Page)

**경로:** `/register/info`

**필드:**

| 필드 | 규칙 | 에러 메시지 |
|------|------|-------------|
| 이메일 | 이메일 형식, 중복확인 | "올바른 이메일 형식이 아닙니다." / "이미 사용 중인 이메일입니다." |
| 비밀번호 | 8자 이상, 영문+숫자 | "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다." |
| 비밀번호 확인 | 일치 | "비밀번호가 일치하지 않습니다." |
| 닉네임 | 2-20자, 중복확인 | "닉네임은 2-20자여야 합니다." / "이미 사용 중인 닉네임입니다." |

**아이콘:**
- 유효성 통과: 체크 아이콘 (✓)
- 비밀번호: 보기/숨기기 토글 (👁)

### 6.3 화면: 완료 (RegisterCompletePage)

**경로:** `/register/complete`

**표시:**
- 축하 메시지
- 시작하기 버튼 → 홈으로 이동

### 6.4 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 이메일 중복확인 | GET | /api/v1/auth/check-email?email={email} |
| 닉네임 중복확인 | GET | /api/v1/auth/check-nickname?nickname={nickname} |
| 회원가입 | POST | /api/v1/auth/register |

---

## 7. 마이페이지

### 7.1 화면: 메인 (MyPage)

**경로:** `/mypage`
**인증:** 필수

**구성:**
- 프로필 영역 (이미지, 닉네임, 이메일)
- 메뉴 그룹 (구분선으로 섹션 분리):

#### 내 정보
| 메뉴 | 아이콘 | 경로 | 구현 상태 |
|------|--------|------|-----------|
| 내 프로필 | Person | /mypage/edit | ✅ 구현 |
| 펫 관리 | Pets | /mypage/pets | placeholder |

#### 서비스
| 메뉴 | 아이콘 | 경로 | 구현 상태 |
|------|--------|------|-----------|
| 결제 수단 관리 | CreditCard | /mypage/payment | placeholder |
| 알림 설정 | Notifications | /mypage/notifications | placeholder |
| 회원등급 안내 | Star | /mypage/membership | placeholder |
| 친구 초대 | PersonAdd | /mypage/referral | placeholder |

#### 고객센터
| 메뉴 | 아이콘 | 경로 | 구현 상태 |
|------|--------|------|-----------|
| FAQ | HelpOutline | /mypage/cs/faq | placeholder |
| 1:1 문의 | QuestionAnswer | /mypage/inquiry | ✅ 구현 |

#### 설정
| 메뉴 | 아이콘 | 경로 | 구현 상태 |
|------|--------|------|-----------|
| 계정 관리 | ManageAccounts | /mypage/settings | placeholder |
| 비밀번호 변경 | Lock | /mypage/password | ✅ 구현 |
| 약관/정책 | Description | /mypage/settings/policies | placeholder |
| 앱 정보 | Info | /mypage/settings/app-info | placeholder |

- 하단: 로그아웃 버튼 + 회원탈퇴 링크
- 미구현 메뉴(placeholder)는 "준비 중입니다" Snackbar 표시

### 7.2 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 내 정보 조회 | GET | /api/v1/users/me |
| 로그아웃 | POST | /api/v1/auth/logout |
| 회원탈퇴 | DELETE | /api/v1/users/me |

---

## 8. 회원정보 수정

### 8.1 화면: 프로필 수정 (EditProfilePage)

**경로:** `/mypage/edit`
**인증:** 필수

**필드:**
- 프로필 이미지 (변경 가능)
- 이메일 (읽기 전용)
- 닉네임 (수정 가능, 중복확인)
- 휴대폰 번호 (변경 버튼 → 재인증)
- 비밀번호 변경 링크

### 8.2 화면: 비밀번호 변경 (ChangePasswordPage)

**경로:** `/mypage/password`
**인증:** 필수

**필드:**

| 필드 | 규칙 | 에러 메시지 |
|------|------|-------------|
| 현재 비밀번호 | 필수 | "현재 비밀번호를 입력해주세요" |
| 새 비밀번호 | 8자 이상, 영문+숫자 | "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다" |
| 새 비밀번호 확인 | 새 비밀번호와 일치 | "비밀번호가 일치하지 않습니다" |

**서버 에러 처리:**

| 에러 코드 | 메시지 |
|----------|--------|
| A009 | 소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다 |
| U004 | 현재 비밀번호가 일치하지 않습니다 |
| U009 | 비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다 |

**성공 시:** 알림 표시 → `/mypage`로 이동

### 8.3 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 정보 수정 | PUT | /api/v1/users/me |
| 프로필 이미지 업로드 | POST | /api/v1/users/me/profile-image |
| 비밀번호 변경 | PUT | /api/v1/users/me/password |

---

## 9. 문의 게시판

### 9.1 화면: 목록 (InquiryListPage)

**경로:** `/mypage/inquiry`
**인증:** 필수

**구성:**
- 문의하기 버튼
- 문의 목록 (제목, 날짜, 상태)
- 상태: 답변대기 / 답변완료

### 9.2 화면: 작성 (InquiryWritePage)

**경로:** `/mypage/inquiry/write`

**필드:**
- 제목 (필수)
- 내용 (필수)

### 9.3 화면: 상세 (InquiryDetailPage)

**경로:** `/mypage/inquiry/:id`

**구성:**
- 문의 내용
- 답변 (있는 경우)
- 수정/삭제 버튼 (답변 전만)

### 9.4 API

| 기능 | Method | Endpoint |
|------|--------|----------|
| 목록 | GET | /api/v1/inquiries |
| 작성 | POST | /api/v1/inquiries |
| 상세 | GET | /api/v1/inquiries/{id} |
| 수정 | PUT | /api/v1/inquiries/{id} |
| 삭제 | DELETE | /api/v1/inquiries/{id} |

---

## 10. 파일 구조

```
frontend/src/
├── components/
│   ├── auth/
│   │   ├── AuthInput.tsx
│   │   ├── AuthButton.tsx
│   │   ├── SocialLoginButton.tsx
│   │   ├── AgreementCheckbox.tsx
│   │   └── index.ts
│   ├── mypage/
│   │   ├── ProfileCard.tsx
│   │   ├── MenuItem.tsx
│   │   └── index.ts
│   └── inquiry/
│       ├── InquiryCard.tsx
│       ├── InquiryForm.tsx
│       └── index.ts
│
├── pages/
│   ├── auth/
│   │   ├── LoginStartPage.tsx
│   │   ├── OAuthCallbackPage.tsx
│   │   ├── RegisterStep1Page.tsx
│   │   ├── RegisterStep2Page.tsx
│   │   ├── RegisterCompletePage.tsx
│   │   ├── FindIdPage.tsx
│   │   ├── FindIdVerifyPage.tsx
│   │   ├── FindIdResultPage.tsx
│   │   ├── ResetPasswordPage.tsx
│   │   ├── ResetPasswordVerifyPage.tsx
│   │   ├── ResetPasswordConfirmPage.tsx
│   │   └── index.ts
│   ├── mypage/
│   │   ├── MyPage.tsx
│   │   ├── EditProfilePage.tsx
│   │   ├── ChangePasswordPage.tsx
│   │   └── index.ts
│   └── inquiry/
│       ├── InquiryListPage.tsx
│       ├── InquiryWritePage.tsx
│       ├── InquiryDetailPage.tsx
│       └── index.ts
│
└── hooks/
    ├── useAuth.ts
    └── useInquiry.ts
```

> **참고:** 관리자(admin) 관련 컴포넌트/페이지는 `docs/develop/admin/frontend.md` 참조

---

## 11. 라우팅

> **참고:** 관리자 라우트(`/admin/*`)는 사용자 라우트와 완전 분리됨. `docs/develop/admin/frontend.md` 참조

| 경로 | 컴포넌트 | 인증 |
|------|----------|------|
| /login | LoginStartPage | X |
| /oauth/:provider/callback | OAuthCallbackPage | X |
| /register | RegisterStep1Page | X |
| /register/info | RegisterStep2Page | X |
| /register/complete | RegisterCompletePage | X |
| /find-id | FindIdPage | X |
| /find-id/verify | FindIdVerifyPage | X |
| /find-id/result | FindIdResultPage | X |
| /reset-password | ResetPasswordPage | X |
| /reset-password/verify | ResetPasswordVerifyPage | X |
| /reset-password/confirm | ResetPasswordConfirmPage | X |
| / | HomePage | ✅ |
| /search | 시터 검색 (placeholder) | ✅ |
| /reservations | 예약 (placeholder) | ✅ |
| /chat | 채팅 (placeholder) | ✅ |
| /mypage | MyPage | ✅ |
| /mypage/edit | EditProfilePage | ✅ |
| /mypage/pets | 펫 관리 (placeholder) | ✅ |
| /mypage/payment | 결제 수단 (placeholder) | ✅ |
| /mypage/notifications | 알림 설정 (placeholder) | ✅ |
| /mypage/membership | 회원등급 (placeholder) | ✅ |
| /mypage/referral | 친구 초대 (placeholder) | ✅ |
| /mypage/cs/faq | FAQ (placeholder) | ✅ |
| /mypage/inquiry | InquiryListPage | ✅ |
| /mypage/inquiry/write | InquiryWritePage | ✅ |
| /mypage/inquiry/:id | InquiryDetailPage | ✅ |
| /mypage/settings | 계정 관리 (placeholder) | ✅ |
| /mypage/password | ChangePasswordPage | ✅ |
| /mypage/settings/policies | 약관/정책 (placeholder) | ✅ |
| /mypage/settings/app-info | 앱 정보 (placeholder) | ✅ |

### 11.1 사용자 사이드바 메뉴

PetPro IA 기준 메뉴입니다.

| # | 메뉴 | 아이콘 | 경로 |
|---|------|--------|------|
| 1 | 홈 | Home | / |
| 2 | 시터 검색 | Search | /search |
| 3 | 예약 | CalendarMonth | /reservations |
| 4 | 채팅 | ChatBubble | /chat |
| 5 | 마이 | Person | /mypage |

### 11.2 Header 프로필 메뉴

프로필 드롭다운 메뉴에서 마이페이지 접근 가능:

| 순서 | 항목 | 아이콘 | 동작 |
|------|------|--------|------|
| 1 | 이름 | - | 표시만 (disabled) |
| 2 | 이메일 | - | 표시만 (disabled) |
| 3 | 구분선 | - | - |
| 4 | 마이페이지 | Person | /mypage로 이동 |
| 5 | 설정 | Settings | (미구현) |
| 6 | 로그아웃 | Logout | 로그아웃 실행 |

---

## 12. Figma 추가 예정

구현 완료 후 Figma에 추가:
- 마이페이지
- 회원정보 수정
- 문의 게시판 (목록, 작성, 상세)
