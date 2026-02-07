# Admin 도메인 - 프론트엔드 지침

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. 개요

PetPro 관리자 프론트엔드 화면 구현 영구 지침입니다.
사용자 사이트와 완전히 분리된 독립 레이아웃을 사용합니다.

---

## 2. 파일 구조

```
frontend/src/
├── components/
│   └── admin/
│       ├── common/
│       │   └── AdminProtectedRoute.tsx
│       └── layout/
│           ├── AdminLayout.tsx
│           ├── AdminHeader.tsx
│           ├── AdminSidebar.tsx
│           └── index.ts
│
├── pages/
│   └── admin/
│       ├── AdminLoginPage.tsx
│       ├── DashboardPage.tsx
│       ├── members/
│       │   ├── UserManagementPage.tsx
│       │   ├── PartnerManagementPage.tsx
│       │   └── PartnerReviewPage.tsx
│       ├── reservations/
│       │   ├── ReservationManagementPage.tsx
│       │   └── DisputeManagementPage.tsx
│       ├── settlement/
│       │   ├── SettlementPage.tsx
│       │   └── FeeSettingPage.tsx
│       ├── cs/
│       │   ├── InquiryManagementPage.tsx
│       │   └── FaqManagementPage.tsx
│       ├── contents/
│       │   ├── NoticeManagementPage.tsx
│       │   ├── EventManagementPage.tsx
│       │   ├── CommunityManagementPage.tsx
│       │   └── CampaignManagementPage.tsx
│       ├── statistics/
│       │   └── StatisticsPage.tsx
│       ├── settings/
│       │   ├── SettingsPage.tsx
│       │   ├── PolicyManagementPage.tsx
│       │   ├── AdminProfilePage.tsx
│       │   ├── AuditLogPage.tsx
│       │   └── AppVersionPage.tsx
│       └── index.ts
│
└── hooks/
    └── useAdminAuth.ts
```

---

## 3. AdminLoginPage

**경로:** `/admin/login`

**구성:**
- "PetPro 관리자" 타이틀
- 이메일 입력 (필수)
- 비밀번호 입력 (필수)
- 로그인 버튼
- 소셜 로그인 **없음** (관리자는 이메일/비밀번호만)

**동작:**
- 로그인 성공 (ADMIN/SUPER_ADMIN) → `/admin/dashboard`로 이동
- 로그인 실패 → 에러 메시지 표시
- 권한 부족 (USER 등) → "관리자 권한이 없습니다." 에러 표시

**API:** `POST /api/auth/login` (기존 로그인 API 공유)

---

## 4. AdminProtectedRoute

**파일:** `frontend/src/components/admin/common/AdminProtectedRoute.tsx`

**Props:**
- `children`: React.ReactNode (보호할 컴포넌트)
- `roles?`: string[] (기본값: `['ADMIN', 'SUPER_ADMIN']`)

**동작:**
1. 로딩 중 → 스피너 표시
2. 미인증 → `/admin/login` 리다이렉트 (현재 위치 저장)
3. 역할 불일치 → `/admin/login` 리다이렉트
4. 인증 + 권한 확인 → children 렌더링

---

## 5. AdminLayout

**파일:** `frontend/src/components/admin/layout/AdminLayout.tsx`

**구성:**
- AdminHeader (상단 고정)
- AdminSidebar (좌측 고정)
- 메인 콘텐츠 영역 (Outlet)

**참조:** `MainLayout.tsx` 패턴과 동일 구조

---

## 6. AdminHeader

**파일:** `frontend/src/components/admin/layout/AdminHeader.tsx`

**구성:**
- 사이드바 토글 버튼
- 타이틀: "PetPro 관리자"
- 사용자 프로필 메뉴 (이름, 이메일, 구분선, 로그아웃)

---

## 7. AdminSidebar

**파일:** `frontend/src/components/admin/layout/AdminSidebar.tsx`

**메뉴 항목:**

| # | 메뉴 | 아이콘 | 경로 | 하위 메뉴 |
|---|------|--------|------|-----------|
| 1 | 대시보드 | Dashboard | /admin/dashboard | - |
| 2 | 회원 관리 | People | - | 반려인 관리, 펫시터 관리, 시터 심사 |
| 3 | 예약 관리 | CalendarMonth | - | 예약 현황, 분쟁 관리 |
| 4 | 정산 관리 | AccountBalance | - | 정산 리스트, 수수료 설정 |
| 5 | 고객센터 | Support | - | 1:1 문의, FAQ 관리 |
| 6 | 콘텐츠 관리 | Article | - | 공지사항, 이벤트/배너, 커뮤니티, 캠페인 |
| 7 | 통계 | BarChart | /admin/statistics | - |
| 8 | 설정 | Settings | - | 알림 템플릿, 약관/정책, 관리자 계정, 감사 로그, 앱 버전 |

### 하위 메뉴 상세

**회원 관리:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 반려인 관리 | /admin/members/users |
| 펫시터 관리 | /admin/members/partners |
| 시터 심사 | /admin/members/review |

**예약 관리:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 예약 현황 | /admin/reservations |
| 분쟁 관리 | /admin/reservations/disputes |

**정산 관리:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 정산 리스트 | /admin/settlement |
| 수수료 설정 | /admin/settlement/fee |

**고객센터:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 1:1 문의 | /admin/cs/inquiries |
| FAQ 관리 | /admin/cs/faq |

**콘텐츠 관리:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 공지사항 | /admin/contents/notices |
| 이벤트/배너 | /admin/contents/events |
| 커뮤니티 | /admin/contents/community |
| 캠페인 | /admin/contents/campaigns |

**설정:**

| 하위 메뉴 | 경로 |
|-----------|------|
| 알림 템플릿 | /admin/settings |
| 약관/정책 | /admin/settings/policies |
| 관리자 계정 | /admin/settings/profile |
| 감사 로그 | /admin/settings/audit-log |
| 앱 버전 | /admin/settings/app-version |

---

## 8. useAdminAuth

**파일:** `frontend/src/hooks/useAdminAuth.ts`

**기능:**
- `login(email, password)`: 로그인 실행
  - 성공 시 역할 검증 (ADMIN/SUPER_ADMIN만 허용)
  - 성공 → `/admin/dashboard`로 이동
  - 실패 → 에러 알림
- `logout()`: 로그아웃 실행
  - 토큰 제거 → `/admin/login`으로 이동
- `isLoginLoading`: 로그인 처리 중 여부

---

## 9. Placeholder 페이지

**각 관리자 페이지는 placeholder로 구현합니다.**

| 페이지 | 타이틀 |
|--------|--------|
| DashboardPage | 관리자 대시보드 |
| UserManagementPage | 반려인 관리 |
| PartnerManagementPage | 펫시터 관리 |
| PartnerReviewPage | 시터 심사 |
| ReservationManagementPage | 예약 현황 |
| DisputeManagementPage | 분쟁 관리 |
| SettlementPage | 정산 리스트 |
| FeeSettingPage | 수수료 설정 |
| InquiryManagementPage | 1:1 문의 관리 |
| FaqManagementPage | FAQ 관리 |
| NoticeManagementPage | 공지사항 |
| EventManagementPage | 이벤트/배너 |
| CommunityManagementPage | 커뮤니티 |
| CampaignManagementPage | 캠페인 |
| StatisticsPage | 통계 |
| SettingsPage | 알림 템플릿 |
| PolicyManagementPage | 약관/정책 |
| AdminProfilePage | 관리자 계정 |
| AuditLogPage | 감사 로그 |
| AppVersionPage | 앱 버전 |

각 페이지는 `<Typography variant="h4">` 타이틀과 "준비 중입니다." 안내 메시지를 표시합니다.

---

## 10. 라우팅 (App.tsx)

### 10.1 관리자 공개 라우트

```
/admin/login → AdminLoginPage
```

### 10.2 관리자 보호 라우트

```
AdminProtectedRoute + AdminLayout 래핑:
  /admin                       → /admin/dashboard 리다이렉트
  /admin/dashboard             → DashboardPage
  /admin/members/users         → UserManagementPage
  /admin/members/partners      → PartnerManagementPage
  /admin/members/review        → PartnerReviewPage
  /admin/reservations          → ReservationManagementPage
  /admin/reservations/disputes → DisputeManagementPage
  /admin/settlement            → SettlementPage
  /admin/settlement/fee        → FeeSettingPage
  /admin/cs/inquiries          → InquiryManagementPage
  /admin/cs/faq                → FaqManagementPage
  /admin/contents/notices      → NoticeManagementPage
  /admin/contents/events       → EventManagementPage
  /admin/contents/community    → CommunityManagementPage
  /admin/contents/campaigns    → CampaignManagementPage
  /admin/statistics            → StatisticsPage
  /admin/settings              → SettingsPage
  /admin/settings/policies     → PolicyManagementPage
  /admin/settings/profile      → AdminProfilePage
  /admin/settings/audit-log    → AuditLogPage
  /admin/settings/app-version  → AppVersionPage
```

---

## 11. 401 에러 처리 (client.ts)

**현재 경로 기반 분기:**
- `/admin/*` 경로 → `/admin/login`으로 리다이렉트
- 그 외 → `/login`으로 리다이렉트

---

## 12. 사용자 Sidebar 변경

**제거 항목:**
- `adminMenuItems` 배열
- `hasAccess()` 함수
- 관리자 메뉴 렌더링 블록 (Divider 포함)
- 불필요한 import: `People`, `Inventory`, `LocalShipping`, `Dashboard`, `Divider`, `useAuthStore`

---

## 13. 사용자 Header 변경

**추가 항목:**
- 프로필 드롭다운에 "마이페이지" 메뉴 항목 추가
- `useNavigate` import 추가
- `Person` 아이콘 import 추가

**메뉴 순서:**
1. 이름 (disabled)
2. 이메일 (disabled)
3. Divider
4. 마이페이지 (Person 아이콘, /mypage로 이동)
5. 설정 (Settings 아이콘)
6. 로그아웃 (Logout 아이콘)
