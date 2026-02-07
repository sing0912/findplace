# Admin 도메인 - 관리자 시스템

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. 개요

PetPro 플랫폼 관리자를 위한 독립적인 관리 시스템입니다.
사용자(B2C) 사이트와 완전히 분리된 별도의 레이아웃, 로그인, 사이드바를 사용합니다.

### 1.1 핵심 원칙

- 관리자 사이트는 `/admin` 하위에 완전히 분리
- 사용자 사이트와 레이아웃/사이드바/로그인을 공유하지 않음
- 관리자 로그인은 이메일/비밀번호만 지원 (소셜 로그인 없음)

### 1.2 접근 가능 역할

| 역할 | 코드 | 설명 |
|------|------|------|
| 시스템 관리자 | ADMIN | 일반 관리 기능 |
| 최고 관리자 | SUPER_ADMIN | 모든 관리 기능 |

---

## 2. 라우트 구조

### 2.1 공개 라우트

| 경로 | 컴포넌트 | 설명 |
|------|----------|------|
| /admin/login | AdminLoginPage | 관리자 로그인 |

### 2.2 보호 라우트 (AdminProtectedRoute + AdminLayout)

| 경로 | 컴포넌트 | 설명 |
|------|----------|------|
| /admin | - | /admin/dashboard로 리다이렉트 |
| /admin/dashboard | DashboardPage | 관리자 대시보드 |
| /admin/members/users | UserManagementPage | 반려인 관리 |
| /admin/members/partners | PartnerManagementPage | 펫시터 관리 |
| /admin/members/review | PartnerReviewPage | 시터 심사 |
| /admin/reservations | ReservationManagementPage | 예약 현황 |
| /admin/reservations/disputes | DisputeManagementPage | 분쟁 관리 |
| /admin/settlement | SettlementPage | 정산 리스트 |
| /admin/settlement/fee | FeeSettingPage | 수수료 설정 |
| /admin/cs/inquiries | InquiryManagementPage | 1:1 문의 관리 |
| /admin/cs/faq | FaqManagementPage | FAQ 관리 |
| /admin/contents/notices | NoticeManagementPage | 공지사항 |
| /admin/contents/events | EventManagementPage | 이벤트/배너 |
| /admin/contents/community | CommunityManagementPage | 커뮤니티 |
| /admin/contents/campaigns | CampaignManagementPage | 캠페인 |
| /admin/statistics | StatisticsPage | 통계 |
| /admin/settings | SettingsPage | 설정 (알림 템플릿) |
| /admin/settings/policies | PolicyManagementPage | 약관/정책 |
| /admin/settings/profile | AdminProfilePage | 관리자 계정 |
| /admin/settings/audit-log | AuditLogPage | 감사 로그 |
| /admin/settings/app-version | AppVersionPage | 앱 버전 |

---

## 3. 레이아웃 컴포넌트

| 컴포넌트 | 파일 | 설명 |
|----------|------|------|
| AdminLayout | `components/admin/layout/AdminLayout.tsx` | 관리자 메인 레이아웃 |
| AdminHeader | `components/admin/layout/AdminHeader.tsx` | 관리자 헤더 (타이틀: "PetPro 관리자") |
| AdminSidebar | `components/admin/layout/AdminSidebar.tsx` | 관리자 사이드바 |

---

## 4. 인증/인가

- **AdminProtectedRoute**: 미인증 시 `/admin/login`으로 리다이렉트
- **useAdminAuth**: 관리자 전용 인증 훅
  - 로그인 성공 → `/admin/dashboard`로 이동
  - 로그아웃 → `/admin/login`으로 이동
  - ADMIN/SUPER_ADMIN 역할이 아닌 경우 로그인 거부
- **401 에러**: `/admin/*` 경로에서 발생 시 → `/admin/login`으로 리다이렉트

---

## 5. 관리자 웹 기능 영역

| 영역 | 주요 기능 | 비고 |
|------|-----------|------|
| 대시보드 | KPI 카드(예약/완료/취소), 예약 추이 차트, 긴급 알림(미처리 심사/분쟁), 정산 현황 | - |
| 회원 관리 | 반려인 리스트/상세, 펫시터 리스트/상세, 시터 심사(승인/반려/보류, 이력 로그) | - |
| 예약 관리 | 예약 현황(리스트/상세/상태이력), 분쟁 관리(리스트/상세/중재) | - |
| 정산 관리 | 정산 리스트(대기/일괄처리/완료/다운로드), 수수료 설정 | - |
| 고객센터 | 1:1 문의 관리(접수/처리중/완료), FAQ 관리(카테고리/CRUD) | - |
| 콘텐츠 관리 | 공지사항, 이벤트/배너, 커뮤니티, 캠페인 | - |
| 통계 | 통계/리포트 | - |
| 설정 | 알림 템플릿, 약관/정책, 관리자 계정(프로필/비밀번호), 감사 로그, 앱 버전 | - |

---

## 6. 상세 지침

- 프론트엔드: `docs/develop/admin/frontend.md`
