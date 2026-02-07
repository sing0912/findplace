# 약관/정책 프론트엔드 지침

> **도메인**: policy (Phase 5)
> **범위**: 프론트엔드 전용 (백엔드 Policy API는 별도 구현 예정, 현재는 하드코딩 콘텐츠)

---

## 1. 약관 종류

| # | 약관명 | 컴포넌트 | 라우트 | 상태 |
|---|--------|----------|--------|------|
| 1 | 이용약관 | `TermsOfUseContent` | `/terms` | ✅ 구현 |
| 2 | 개인정보처리방침 | `PrivacyPolicyContent` | `/privacy`, `/privacy-policy` | ✅ 구현 |
| 3 | 위치기반서비스 이용약관 | - | - | 🔜 준비 중 |
| 4 | 마케팅 수신 동의 | - | - | 🔜 준비 중 |

---

## 2. 라우트 구성

### 공개 라우트 (인증 불필요)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/terms` | `TermsOfUsePage` | 이용약관 |
| `/privacy` | `PrivacyPolicyPage` | 개인정보처리방침 |
| `/privacy-policy` | `PrivacyPolicyPage` | 개인정보처리방침 (레거시 URL) |

### 보호 라우트 (인증 필요)

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/mypage/policy` | `PolicyListPage` | 약관/정책 목록 |

---

## 3. 컴포넌트 구조

### 3.1 콘텐츠 컴포넌트 (`components/legal/`)

| 파일 | 설명 | Props |
|------|------|-------|
| `PrivacyPolicyContent.tsx` | 개인정보처리방침 콘텐츠 | `showContainer?: boolean` (default: true) |
| `TermsOfUseContent.tsx` | 이용약관 콘텐츠 | `showContainer?: boolean` (default: true) |
| `index.ts` | barrel export | - |

- `showContainer=true`: Paper 래퍼 포함 (페이지용)
- `showContainer=false`: Box 래퍼만 (다이얼로그/임베드용)

### 3.2 페이지 컴포넌트

| 파일 | 설명 |
|------|------|
| `pages/legal/PrivacyPolicyPage.tsx` | 개인정보처리방침 페이지 |
| `pages/legal/TermsOfUsePage.tsx` | 이용약관 페이지 |
| `pages/mypage/PolicyListPage.tsx` | 약관/정책 목록 (마이페이지) |

---

## 4. PolicyListPage 명세

### UI 구성
- **헤더**: "약관/정책" 타이틀 + 뒤로가기 버튼(ArrowBackIosNew)
- **메뉴 목록**: `MenuItem` 컴포넌트 재사용 (4개)

### 메뉴 항목

| # | label | icon | 동작 |
|---|-------|------|------|
| 1 | 이용약관 | `Gavel` | `window.open('/terms', '_blank')` |
| 2 | 개인정보처리방침 | `Security` | `window.open('/privacy', '_blank')` |
| 3 | 위치기반서비스 이용약관 | `LocationOn` | 스낵바 "준비 중입니다" |
| 4 | 마케팅 수신 동의 | `Campaign` | 스낵바 "준비 중입니다" |

---

## 5. 콘텐츠 스펙

### 5.1 개인정보처리방침 (PetPro)
- **서비스명**: 펫프로(PETPRO)
- **회사명**: 베뉴네트웍스
- **서비스 설명**: AI 기반 반려동물 돌봄 서비스
- **수집항목**: 회원가입, 간편가입, 본인인증, 돌봄 예약 및 상담, 결제 정보 처리, 위치기반 서비스, 환불
- **시행일**: 2026년 2월 7일

### 5.2 이용약관 (PetPro)
- **서비스명**: 펫프로(PETPRO)
- **12조 구성**: 목적, 정의, 약관 효력, 서비스 이용 신청, 서비스 내용, 이용자 의무, 회사 의무, 예약 및 결제, 취소/환불, 면책사항, 분쟁해결, 기타
- **시행일**: 2026년 2월 7일

---

## 6. 링크 연결 맵

| 출발점 | 대상 | 방식 |
|--------|------|------|
| RegisterStep1Page "이용약관 동의" > 보기 | `/terms` | `window.open('_blank')` |
| RegisterStep1Page "개인정보처리방침 동의" > 보기 | `/privacy` | `window.open('_blank')` |
| MyPage > 약관/정책 | `/mypage/policy` | `navigate()` |
| PolicyListPage > 이용약관 | `/terms` | `window.open('_blank')` |
| PolicyListPage > 개인정보처리방침 | `/privacy` | `window.open('_blank')` |
| PolicyListPage > 위치기반서비스 | - | 스낵바 (준비 중) |
| PolicyListPage > 마케팅 수신 동의 | - | 스낵바 (준비 중) |
