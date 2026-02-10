# Playwright E2E 테스트 지침

## 개요

백엔드 미완성 상태에서 프론트엔드 6개 도메인(Auth, Home, MyPage, Pet, Inquiry, Admin)의 사용자 시나리오를 검증하기 위한 Playwright E2E 테스트이다. 모든 API는 `page.route()`로 모킹하여 백엔드 독립적으로 실행한다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| 테스트 프레임워크 | Playwright Test |
| 브라우저 | Chromium (Desktop), Mobile Chrome (Pixel 5) |
| API 모킹 | page.route() |
| 상태 관리 모킹 | localStorage + Zustand persist 형식 |

## 파일 구조

```
frontend/
├── playwright.config.ts
└── e2e/
    ├── fixtures/
    │   ├── data/
    │   │   ├── auth.json
    │   │   ├── home.json
    │   │   ├── pet.json
    │   │   ├── inquiry.json
    │   │   └── admin.json
    │   └── index.ts
    ├── helpers/
    │   ├── api-mock.ts
    │   ├── auth-setup.ts
    │   └── index.ts
    └── specs/
        ├── auth.spec.ts
        ├── home.spec.ts
        ├── mypage.spec.ts
        ├── pet.spec.ts
        ├── inquiry.spec.ts
        └── admin.spec.ts
```

## 헬퍼 API

### api-mock.ts

| 함수 | 설명 |
|------|------|
| `mockApi(page, routes[])` | 여러 API 라우트를 한번에 모킹. URL 패턴: `**/api/v1/{url}` |
| `mockApiError(page, method, url, status, code, message)` | 에러 응답 모킹 |
| `wrapResponse(data)` | `{ success, data, error, timestamp }` 엔벨로프 래핑 |
| `wrapPageResponse(content[], page?, totalElements?)` | 페이지네이션 응답 래핑 |

### auth-setup.ts

| 함수 | 설명 |
|------|------|
| `removeOverlay(page)` | webpack-dev-server 오버레이 iframe 제거 (MutationObserver) |
| `setupAuth(page, role)` | localStorage에 토큰 + Zustand `auth-storage` 설정 |
| `setupAdminUI(page)` | 관리자 UI 스토어 (`ui-storage`) 설정 |
| `gotoAuthenticated(page, url, role)` | `removeOverlay` + `setupAuth` + `goto` 통합 (ADMIN이면 `setupAdminUI` 자동 포함) |
| `clearAuth(page)` | 인증 상태 제거 |

역할: `CUSTOMER`, `PARTNER`, `ADMIN`, `SUPER_ADMIN`

## Fixture 데이터

| 파일 | 키 | 설명 |
|------|------|------|
| auth.json | `customerUser` | 반려인 유저 (id:1, CUSTOMER) |
| | `partnerUser` | 펫시터 유저 (id:2, PARTNER) |
| | `adminUser` | 관리자 유저 (id:3, ADMIN) |
| | `tokenResponse` | accessToken, refreshToken |
| | `registerResult` | 회원가입 결과 (id:4) |
| home.json | `customerHome` | 추천시터 2명, 이벤트배너 1개, 커뮤니티피드 1개 |
| | `partnerHome` | 오늘일정 2건, 새요청 5건, 수익 1,250,000원, 공지 1건 |
| | `emptyCustomerHome` | 빈 반려인 홈 |
| | `emptyPartnerHome` | 빈 펫시터 홈 |
| pet.json | `petList` | 펫 2마리 (초코, 나비) |
| | `petDetail` | 초코 상세 (id:1) |
| | `emptyPetList` | 빈 펫 목록 |
| | `petChecklist` | 체크리스트 데이터 |
| inquiry.json | `inquiryList` | 문의 3건 (답변완료 1, 답변대기 2) |
| | `inquiryDetail` | 답변완료 문의 상세 (id:1) |
| | `inquiryDetailWaiting` | 답변대기 문의 상세 (id:2) |
| | `emptyInquiryList` | 빈 문의 목록 |
| | `createdInquiry` | 새 문의 생성 결과 (id:4) |
| admin.json | `adminTokenResponse` | 관리자 토큰 |

---

## 테스트 시나리오 상세 (총 88개 케이스)

> 각 케이스는 Chromium + Mobile Chrome 2개 프로젝트에서 실행됨 (사이드바 테스트 제외)

### 1. auth.spec.ts — 인증 (19개 케이스)

**커버 페이지**: `/login`, `/register`, `/register/info`, `/find-id`, `/reset-password`

#### 로그인 페이지 (`/login`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 앱 이름과 슬로건을 표시한다 | `heading:펫프로`, `돌봄이 필요한 순간, 펫프로` |
| 2 | 소셜 로그인 버튼 3개를 표시한다 | `카카오로 시작하기`, `네이버로 시작하기`, `Google로 시작하기` |
| 3 | 이메일로 회원가입 링크 → `/register` 이동 | 클릭 후 URL 확인 |
| 4 | 아이디 찾기 링크 → `/find-id` 이동 | `{ exact: true }` 사용 |
| 5 | 비밀번호 찾기 링크 → `/reset-password` 이동 | `{ exact: true }` 사용 |

#### 회원가입 Step1 — 약관동의 (`/register`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 6 | 회원가입 헤더와 약관 동의 체크박스를 표시한다 | `회원가입`, `약관에 동의해 주세요`, 전체동의/이용약관/개인정보/마케팅 체크박스 |
| 7 | 필수 약관 미동의 시 다음 버튼이 비활성화된다 | `button:다음` disabled |
| 8 | 전체 동의 체크 시 모든 체크박스가 체크된다 | `#agree-terms`, `#agree-privacy`, `#agree-marketing` 모두 checked |
| 9 | 필수 약관(이용약관, 개인정보) 동의 시 다음 버튼이 활성화된다 | 개별 클릭 후 enabled 확인 |
| 10 | 다음 버튼 클릭 시 Step2 페이지로 이동한다 | → `/register/info` |

#### 회원가입 Step2 — 정보 입력 (`/register/info`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 11 | 회원가입 폼 필드들을 표시한다 | `input[name=email/password/passwordConfirm/name/nickname/phone]` 6개 필드 |
| 12 | 입력값이 없으면 가입하기 버튼이 비활성화된다 | `button:가입하기` disabled |
| 13 | 유효한 정보 입력 후 가입에 성공하면 완료 페이지로 이동한다 | 이메일/닉네임 중복체크 API 모킹 + 전체 입력 + → `/register/complete` |

#### 아이디 찾기 (`/find-id`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 14 | 아이디 찾기 페이지를 표시한다 | 제목, 안내문, `input[name=name/phone]` |
| 15 | 입력값이 없으면 인증번호 받기 버튼이 비활성화된다 | `button:인증번호 받기` disabled |
| 16 | 유효한 정보 입력 후 제출 시 인증번호 화면으로 이동한다 | POST `/auth/find-id/request` 모킹 → `/find-id/verify` |

#### 비밀번호 재설정 (`/reset-password`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 17 | 비밀번호 재설정 페이지를 표시한다 | 제목, 안내문, `input[name=email/phone]` |
| 18 | 입력값이 없으면 인증번호 받기 버튼이 비활성화된다 | `button:인증번호 받기` disabled |
| 19 | 유효한 정보 입력 후 제출 시 인증번호 화면으로 이동한다 | POST `/auth/reset-password/request` 모킹 → `/reset-password/verify` |

---

### 2. home.spec.ts — 홈 (12개 케이스)

**커버 페이지**: `/` (역할에 따라 CustomerHomePage / PartnerHomePage 분기)

#### 반려인(CUSTOMER) 홈

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 추천 시터 섹션을 표시한다 | `추천 시터`, `김시터`, `이시터` |
| 2 | 이벤트 배너를 표시한다 | `첫 예약 30% 할인` |
| 3 | 커뮤니티 피드를 표시한다 | `커뮤니티`, `강아지 산책 팁 공유` |

#### 반려인 홈 — 빈 상태

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 4 | 데이터가 없을 때 빈 상태를 처리한다 | `주변에 추천 시터가 없습니다` |

#### 반려인 홈 — 에러 처리

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 5 | API 실패 시 에러 메시지와 재시도 버튼을 표시한다 | `홈 화면을 불러올 수 없습니다` + `button:재시도` |
| 6 | 재시도 버튼 클릭 시 데이터를 다시 요청한다 | 첫 번째 500 → 두 번째 200, `김시터` 표시 확인 |

#### 펫시터(PARTNER) 홈

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 7 | 오늘 일정 카드를 표시한다 | `오늘의 일정`, `초코`, `나비` |
| 8 | 새 요청 배지를 표시한다 | `5` 표시 |
| 9 | 수익 요약 카드를 표시한다 | `이번 달 수익`, `1,250,000` |
| 10 | 공지사항을 표시한다 | `2월 정산 안내` |

#### 펫시터 홈 — 에러/빈 상태

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 11 | API 실패 시 에러 메시지와 재시도 버튼을 표시한다 | `홈 화면을 불러올 수 없습니다` + `button:재시도` |
| 12 | 데이터가 없을 때 빈 상태를 처리한다 | `오늘 예정된 일정이 없습니다` |

---

### 3. mypage.spec.ts — 마이페이지 (17개 케이스)

**커버 페이지**: `/mypage`, `/mypage/edit`, `/mypage/password`

#### 마이페이지 메인 (`/mypage`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 마이페이지 헤더를 표시한다 | `마이페이지 { exact: true }` |
| 2 | 프로필 카드에 닉네임과 이메일을 표시한다 | `테스트반려인`, `customer@test.com` |
| 3 | 내 정보 메뉴 항목을 표시한다 | `내 프로필`, `펫 관리` |
| 4 | 서비스 메뉴 항목을 표시한다 | `결제 수단 관리`, `알림 설정`, `회원등급 안내`, `친구 초대` |
| 5 | 고객센터 메뉴 항목을 표시한다 | `FAQ`, `1:1 문의` |
| 6 | 설정 메뉴 항목을 표시한다 | `비밀번호 변경`, `약관/정책` |
| 7 | 내 프로필 클릭 → `/mypage/edit` 이동 | 네비게이션 확인 |
| 8 | 펫 관리 클릭 → `/mypage/pets` 이동 | 네비게이션 확인 |
| 9 | 1:1 문의 클릭 → `/mypage/inquiry` 이동 | 네비게이션 확인 |
| 10 | 비밀번호 변경 클릭 → `/mypage/password` 이동 | 네비게이션 확인 |
| 11 | 약관/정책 클릭 → `/mypage/settings/policies` 이동 | 네비게이션 확인 |

#### 마이페이지 — 로그아웃

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 12 | 로그아웃 버튼 클릭 시 확인 다이얼로그를 표시한다 | `로그아웃 하시겠습니까?` |
| 13 | 로그아웃 확인 시 로그인 페이지로 이동한다 | POST `/auth/logout` 모킹 → `/login` |
| 14 | 로그아웃 취소 시 다이얼로그를 닫는다 | `button:취소` 클릭 후 다이얼로그 사라짐 확인 |

#### 마이페이지 — 회원탈퇴

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 15 | 회원탈퇴 버튼 클릭 시 확인 다이얼로그를 표시한다 | `정말 탈퇴하시겠습니까?` |
| 16 | 회원탈퇴 확인 시 로그인 페이지로 이동한다 | DELETE `/users/me` 모킹 → `/login` |

#### 프로필 수정 페이지 (`/mypage/edit`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 17 | 프로필 정보를 표시한다 | `회원정보 수정`, `input[name=email]` |

#### 비밀번호 변경 페이지 (`/mypage/password`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 18 | 비밀번호 변경 폼을 표시한다 | `input[name=currentPassword/newPassword/confirmPassword]` 3개 필드 |
| 19 | 유효한 비밀번호 입력 후 변경에 성공한다 | PUT `/users/*/password` 모킹 + 입력 + 클릭 |

#### 미인증 접근

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 20 | 미인증 상태로 접근 시 로그인 페이지로 리디렉트된다 | `/mypage` → `/login` |

---

### 4. pet.spec.ts — 반려동물 (9개 케이스)

**커버 페이지**: `/mypage/pets`, `/mypage/pets/register`, `/mypage/pets/:id/edit`, `/mypage/pets/:id/checklist`

#### 펫 목록 페이지 (`/mypage/pets`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 펫 카드들을 표시한다 | `초코`, `나비` |
| 2 | 펫 수 카운트를 표시한다 | `/전체.*2마리/` |
| 3 | 등록 FAB 버튼을 표시한다 | svg 포함 버튼 |

#### 펫 목록 — 빈 상태

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 4 | 펫이 없으면 빈 상태 메시지를 표시한다 | `반려동물 등록` |
| 5 | 빈 상태에서 등록 버튼 클릭 시 등록 페이지로 이동한다 | → `/mypage/pets/register` |

#### 펫 등록/수정/체크리스트

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 6 | 등록 폼을 표시한다 (`/mypage/pets/register`) | `반려동물 등록` 텍스트 |
| 7 | 기존 데이터가 프리필된 수정 폼을 표시한다 (`/mypage/pets/1/edit`) | `input.first()` 값 = `초코` |
| 8 | 체크리스트 폼을 표시한다 (`/mypage/pets/1/checklist`) | `body` visible (페이지 렌더링 확인) |

#### 미인증 접근

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 9 | 미인증 상태로 접근 시 로그인 페이지로 리디렉트된다 | `/mypage/pets` → `/login` |

---

### 5. inquiry.spec.ts — 1:1 문의 (13개 케이스)

**커버 페이지**: `/mypage/inquiry`, `/mypage/inquiry/write`, `/mypage/inquiry/:id`

#### 문의 목록 페이지 (`/mypage/inquiry`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 문의 게시판 헤더와 문의하기 버튼을 표시한다 | `문의 게시판`, `문의하기` |
| 2 | 문의 카드 목록을 표시한다 | `예약 변경 문의`, `결제 오류 문의`, `시터 자격 관련 문의` |
| 3 | 답변 상태 배지를 표시한다 | `답변완료`, `답변대기` |
| 4 | 문의하기 버튼 클릭 시 작성 페이지로 이동한다 | → `/mypage/inquiry/write` |
| 5 | 문의 카드 클릭 시 상세 페이지로 이동한다 | → `/mypage/inquiry/1` |

#### 문의 목록 — 빈 상태

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 6 | 문의가 없으면 빈 상태 메시지를 표시한다 | `등록된 문의가 없습니다.`, `첫 번째 문의 작성하기` |
| 7 | 빈 상태에서 작성 버튼 클릭 시 작성 페이지로 이동한다 | → `/mypage/inquiry/write` |

#### 문의 작성 페이지 (`/mypage/inquiry/write`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 8 | 문의 작성 폼을 표시한다 | `문의하기`, `#inquiry-title`, `#inquiry-content` |
| 9 | 입력값이 없으면 등록 버튼이 비활성화된다 | `button:등록` disabled |
| 10 | 제목과 내용 입력 후 등록에 성공한다 | POST `/inquiries` 모킹 → `/mypage/inquiry/4` |

#### 문의 상세 — 답변 있는 문의 (`/mypage/inquiry/1`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 11 | 문의 제목과 내용을 표시한다 | `문의 상세`, `예약 변경 문의`, `예약 날짜를 변경하고 싶습니다` |
| 12 | 답변완료 상태 배지를 표시한다 | `답변완료` |
| 13 | 답변 내용을 표시한다 | `답변 { exact: true }`, `예약 변경은 마이페이지에서 가능합니다` |
| 14 | 답변 완료된 문의는 수정/삭제 버튼이 없다 | `button:수정`, `button:삭제` not.toBeVisible |

#### 문의 상세 — 대기중 문의 (`/mypage/inquiry/2`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 15 | 답변대기 상태 배지를 표시한다 | `답변대기` |
| 16 | 수정/삭제 버튼을 표시한다 | `button:수정`, `button:삭제` visible |
| 17 | 삭제 버튼 클릭 시 확인 다이얼로그를 표시한다 | `이 문의를 삭제하시겠습니까?` |

---

### 6. admin.spec.ts — 관리자 (15개 케이스)

**커버 페이지**: `/admin/login`, `/admin/dashboard`, `/admin/members/users`

#### 관리자 로그인 (`/admin/login`)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 1 | 관리자 로그인 폼을 표시한다 | `PetPro 관리자`, `label:이메일`, `label:비밀번호`, `button:로그인` |
| 2 | 이메일/비밀번호 입력 후 로그인에 성공하면 대시보드로 이동한다 | POST `/auth/login` + GET `/users/me` 모킹 → `/admin/dashboard` |
| 3 | 로그인 실패 시 로그인 페이지에 남아있다 | 401 응답 모킹, URL 변경 없음 확인 |

#### 관리자 보호 라우트

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 4 | 미인증 상태로 대시보드 접근 시 로그인 페이지로 리디렉트 | `/admin/dashboard` → `/admin/login` |
| 5 | 미인증 상태로 회원관리 접근 시 로그인 페이지로 리디렉트 | `/admin/members/users` → `/admin/login` |
| 6 | 미인증 상태로 설정 접근 시 로그인 페이지로 리디렉트 | `/admin/settings` → `/admin/login` |

#### 관리자 역할 검증

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 7 | CUSTOMER 역할로 관리자 페이지 접근 시 리디렉트 | → `/admin/login` |
| 8 | PARTNER 역할로 관리자 페이지 접근 시 리디렉트 | → `/admin/login` |
| 9 | ADMIN 역할로 관리자 페이지 접근 시 정상 표시 | `/admin/dashboard` 유지 |

#### 관리자 사이드바 (Desktop Chromium만 실행, Mobile Chrome은 skip)

| # | 테스트 케이스 | 검증 내용 |
|---|------------|---------|
| 10 | 8개 메인 메뉴를 표시한다 | 대시보드/회원관리/예약관리/정산관리/고객센터/콘텐츠관리/통계/설정 |
| 11 | 회원 관리 메뉴 클릭 시 하위 메뉴 표시 | `반려인 관리`, `펫시터 관리`, `시터 심사` |
| 12 | 고객센터 메뉴 클릭 시 하위 메뉴 표시 | `1:1 문의`, `FAQ 관리` |
| 13 | 콘텐츠 관리 메뉴 클릭 시 하위 메뉴 표시 | `공지사항`, `이벤트`, `커뮤니티`, `캠페인` |
| 14 | 설정 메뉴 클릭 시 하위 메뉴 표시 | `알림 설정`, `약관 관리`, `계정 관리`, `감사 로그`, `앱 버전` |
| 15 | 하위 메뉴 클릭 시 해당 페이지로 이동한다 | `반려인 관리` → `/admin/members/users` |

---

## 테스트 커버리지 현황

### 커버된 페이지 (17개)

| 도메인 | 페이지 | 테스트 수 |
|--------|--------|----------|
| Auth | `/login` | 5 |
| Auth | `/register` | 5 |
| Auth | `/register/info` | 3 |
| Auth | `/find-id` | 3 |
| Auth | `/reset-password` | 3 |
| Home | `/` (CUSTOMER) | 6 |
| Home | `/` (PARTNER) | 6 |
| MyPage | `/mypage` | 16 |
| MyPage | `/mypage/edit` | 1 |
| MyPage | `/mypage/password` | 2 |
| Pet | `/mypage/pets` | 5 |
| Pet | `/mypage/pets/register` | 1 |
| Pet | `/mypage/pets/:id/edit` | 1 |
| Pet | `/mypage/pets/:id/checklist` | 1 |
| Inquiry | `/mypage/inquiry` | 7 |
| Inquiry | `/mypage/inquiry/write` | 3 |
| Inquiry | `/mypage/inquiry/:id` | 7 |
| Admin | `/admin/login` | 3 |
| Admin | `/admin/dashboard` | 9 (보호라우트/역할/사이드바 포함) |

### 미커버 페이지 (32개)

#### 인증 후속 페이지 (4개) — 네비게이션만 확인, 페이지 내용 미검증

| 페이지 | 상태 | 사유 |
|--------|------|------|
| `/register/complete` | 네비게이션만 | 가입 성공 후 이동만 확인 |
| `/find-id/verify` | 네비게이션만 | 인증번호 입력 → 백엔드 의존 |
| `/find-id/result` | 미커버 | 아이디 찾기 결과 → 백엔드 의존 |
| `/reset-password/verify` | 네비게이션만 | 인증번호 입력 → 백엔드 의존 |
| `/reset-password/confirm` | 미커버 | 새 비밀번호 설정 → 백엔드 의존 |

#### OAuth 콜백 (1개)

| 페이지 | 상태 | 사유 |
|--------|------|------|
| `/oauth/:provider/callback` | 미커버 | 외부 OAuth 리다이렉트 흐름이라 E2E 모킹 어려움 |

#### 법적 페이지 (2개)

| 페이지 | 상태 | 사유 |
|--------|------|------|
| `/privacy-policy` (`/privacy`) | 미커버 | 정적 콘텐츠 |
| `/terms` | 미커버 | 정적 콘텐츠 |

#### 마이페이지 하위 (1개)

| 페이지 | 상태 | 사유 |
|--------|------|------|
| `/mypage/settings/policies` | 네비게이션만 | 약관/정책 → 네비게이션만 확인 |

#### 관리자 하위 페이지 (20개) — 사이드바 네비게이션만 확인, 페이지 내용 미검증

| 페이지 | 상태 |
|--------|------|
| `/admin/members/users` | 네비게이션만 |
| `/admin/members/partners` | 미커버 |
| `/admin/members/partner-review` | 미커버 |
| `/admin/reservations` | 미커버 |
| `/admin/reservations/disputes` | 미커버 |
| `/admin/settlement` | 미커버 |
| `/admin/settlement/fees` | 미커버 |
| `/admin/cs/inquiries` | 미커버 |
| `/admin/cs/faq` | 미커버 |
| `/admin/contents/notices` | 미커버 |
| `/admin/contents/events` | 미커버 |
| `/admin/contents/community` | 미커버 |
| `/admin/contents/campaigns` | 미커버 |
| `/admin/statistics` | 미커버 |
| `/admin/settings` | 미커버 |
| `/admin/settings/policies` | 미커버 |
| `/admin/settings/profile` | 미커버 |
| `/admin/settings/audit-log` | 미커버 |
| `/admin/settings/app-version` | 미커버 |
| `/admin/dashboard` (내용) | 미커버 (라우트 보호만 확인) |

#### 준비 중 페이지 (3개)

| 페이지 | 상태 |
|--------|------|
| `/search` | 미구현 |
| `/reservations` | 미구현 |
| `/chat` | 미구현 |

---

## 실행 명령어

```bash
npm run test:e2e              # 전체 테스트 (Chromium + Mobile Chrome)
npm run test:e2e:ui           # UI 모드 (디버깅)
npm run test:e2e:headed       # 브라우저 표시 모드
npm run test:e2e:report       # HTML 리포트 확인
npx playwright test e2e/specs/auth.spec.ts   # 특정 파일만 실행
```

---

## 주의사항

### webpack-dev-server 오버레이

개발 서버 `<iframe id="webpack-dev-server-client-overlay">`가 모든 클릭 이벤트를 차단한다. `removeOverlay()` 헬퍼가 MutationObserver로 자동 제거한다. `gotoAuthenticated()`에 포함되어 있으며, 미인증 테스트에서는 직접 호출해야 한다.

### Zustand 리하이드레이션

`ProtectedRoute`가 `isLoading` → `isAuthenticated` 순서로 체크한다. `auth-storage` persist 형식을 정확히 맞춰야 리디렉트 방지된다.

```json
{
  "state": { "user": {...}, "isAuthenticated": true },
  "version": 0
}
```

### 직접 fetch 사용 페이지

MyPage, EditProfilePage, FindIdPage, ResetPasswordPage, RegisterStep2Page에서 `fetch()` 직접 사용. `page.route()`가 fetch도 인터셉트하므로 별도 처리 불필요.

### Mobile Chrome에서 사이드바 테스트

관리자 사이드바는 `variant="persistent"` Drawer로 구현되어 있어 모바일 뷰포트에서 정상 표시되지 않는다. `admin.spec.ts` 사이드바 테스트는 Desktop Chromium에서만 실행되도록 `test.skip`으로 설정되어 있다.
