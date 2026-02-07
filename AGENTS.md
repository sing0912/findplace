# AGENTS.md - AI 기반 반려동물 돌봄 플랫폼 (PetPro)

## 프로젝트 개요

AI 기반 반려동물 돌봄 서비스를 위한 토탈 플랫폼입니다.
- 반려인(CUSTOMER): 시터 검색, 예약, 결제, 돌봄 일지 조회, 채팅, 커뮤니티
- 펫시터(PARTNER): 프로필/자격 관리, 예약 수락/거절, 돌봄 일지 작성, 정산
- 플랫폼 관리자(ADMIN): 회원/시터심사/예약/정산/문의/FAQ/콘텐츠 관리

---

## ⛔⛔⛔ 절대 규정: 모든 Git 메시지는 한글로 작성 (ABSOLUTE MANDATORY) ⛔⛔⛔

> **이 규정은 어떠한 경우에도 예외 없이 반드시 준수해야 합니다.**
> **영문 커밋 메시지, 영문 PR 제목/본문은 절대 금지합니다.**

```
┌─────────────────────────────────────────────────────────────────────┐
│  ⛔ 절대 금지: 영문 커밋 메시지 (feat:, fix:, chore: 등 금지)      │
│  ⛔ 절대 금지: 영문 PR 제목/본문                                   │
│  ✅ 반드시 준수: 커밋 메시지 한글 작성 (형식: [타입] 제목)          │
│  ✅ 반드시 준수: PR 제목/본문 한글 작성                             │
└─────────────────────────────────────────────────────────────────────┘
```

### 커밋 메시지 형식

```
[타입] 한글 제목

한글 본문 (선택)
```

- **타입**: 기능, 수정, 리팩토링, 문서, 테스트, 설정
- **절대 금지**: `feat:`, `fix:`, `chore:`, `refactor:`, `docs:` 등 영문 접두사

### 올바른 예시 vs 금지 예시

| ✅ 올바른 예시 | ⛔ 금지 예시 |
|---------------|-------------|
| `[기능] 마이페이지 약관/정책 목록 구현` | `feat: add policy list page` |
| `[수정] 예약 날짜 유효성 검사 오류 해결` | `fix: reservation date validation` |
| `[문서] API 명세서 업데이트` | `docs: update API spec` |
| `[리팩토링] 결제 서비스 코드 구조 개선` | `refactor: payment service` |
| `[설정] Docker 환경 변수 추가` | `chore: add docker env vars` |

### PR (Pull Request)

- **제목**: `[타입] 한글 제목`
- **본문**: 한글로 작성 (섹션 제목도 한글: 요약, 테스트 등)

---

## ⛔⛔⛔ 절대 규정: 선지침 작성 후 구현 (ABSOLUTE MANDATORY) ⛔⛔⛔

> **이 규정은 어떠한 상황에서도 예외 없이 반드시 준수해야 합니다.**
> **위반 시 즉시 작업을 중단하고 지침부터 작성해야 합니다.**
> **"급해서", "간단해서", "나중에" 등의 이유는 인정되지 않습니다.**

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│   ⛔ 절대 금지: 코드를 먼저 작성하고 나중에 문서화                  │
│   ✅ 반드시 준수: 지침을 먼저 작성하고 그 다음에 구현              │
│                                                                     │
│   ⛔ 절대 금지: 구현 후 지침 일치 검증 생략                        │
│   ✅ 반드시 준수: 구현 완료 후 지침과 100% 일치 검증               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 필수 순서 (위반 절대 불가)

```
┌─────────────────────────────────────────────────────────┐
│  1️⃣ 지침 작성   docs/develop/{domain}/ 영구지침 먼저!  │
│        ↓                                                │
│  2️⃣ 구현       지침에 따라 코드 작성                   │
│        ↓                                                │
│  3️⃣ 검증       지침 vs 구현 100% 일치 확인 (필수!)    │
│        ↓                                                │
│  4️⃣ 불일치 시  지침 또는 코드 수정하여 일치시킴        │
└─────────────────────────────────────────────────────────┘
```

### 절대 위반 사례 (금지)

| 위반 유형 | 예시 | 판정 |
|-----------|------|------|
| 코드 선 구현 | "일단 수정하고 문서는 나중에" | ⛔ 절대 금지 |
| 검증 생략 | "간단한 수정이라 검증 생략" | ⛔ 절대 금지 |
| 급한 작업 | "급해서 지침 없이 구현" | ⛔ 절대 금지 |
| 부분 검증 | "API만 검증하고 로직은 생략" | ⛔ 절대 금지 |

### 100% 일치 검증 체크리스트 (필수 수행)

구현 완료 후 **반드시** 아래 항목을 **모두** 확인:

| # | 검증 항목 | 확인 |
|---|----------|------|
| 1 | 해당 도메인의 영구지침이 최신 상태인가? | ☐ |
| 2 | API 엔드포인트가 지침과 100% 일치하는가? | ☐ |
| 3 | Request/Response 스펙이 지침과 100% 일치하는가? | ☐ |
| 4 | 데이터 모델(Entity/DTO)이 지침과 100% 일치하는가? | ☐ |
| 5 | 비즈니스 로직이 지침과 100% 일치하는가? | ☐ |
| 6 | 예외 처리/에러 코드가 지침과 100% 일치하는가? | ☐ |
| 7 | 프론트엔드 컴포넌트가 지침과 100% 일치하는가? (해당 시) | ☐ |

**⚠️ 하나라도 불일치하면 지침 또는 코드를 수정하여 일치시켜야 함**

---

## 절대 필수지침 (MANDATORY RULES)

> **이 규칙은 모든 개발 작업에서 예외 없이 반드시 준수해야 합니다.**

### 1. 영구지침 우선 원칙

```
[설계/지침 변경 발생]
       ↓
[docs/develop/{domain}/ 영구지침 먼저 업데이트]  ← 반드시 먼저!
       ↓
[구현 진행]
       ↓
[영구지침 vs 구현 100% 일치 검증]  ← 반드시 마지막에!
```

### 2. 필수 준수 사항

| 순서 | 단계 | 설명 | 필수 여부 |
|------|------|------|-----------|
| 1 | 영구지침 업데이트 | 설계 변경 시 `docs/develop/` 먼저 수정 | **필수** |
| 2 | 구현 진행 | 영구지침 기반으로 코드 작성 | **필수** |
| 3 | 100% 일치 검증 | 구현 완료 후 영구지침과 코드 비교 검토 | **필수** |
| 4 | 불일치 시 수정 | 지침 또는 코드 중 하나를 수정하여 일치시킴 | **필수** |

### 3. 금지 사항

- ⛔ 영구지침 업데이트 없이 구현 진행
- ⛔ 구현 후 영구지침 일치 검증 생략
- ⛔ "나중에 문서화" 방식의 개발

### 4. 검증 체크리스트

구현 완료 시 아래 항목을 반드시 확인:

- [ ] 해당 도메인의 영구지침이 최신 상태인가?
- [ ] API 엔드포인트가 지침과 100% 일치하는가?
- [ ] 데이터 모델(Entity/DTO)이 지침과 100% 일치하는가?
- [ ] 비즈니스 로직이 지침과 100% 일치하는가?
- [ ] 예외 처리가 지침과 100% 일치하는가?
- [ ] 프론트엔드 컴포넌트가 지침과 100% 일치하는가?

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java, Spring Boot (최신) |
| Frontend | React, TypeScript |
| Database | PostgreSQL (Master 1 + Slave 2) |
| Cache | Redis |
| Container | Podman + Podman Compose (Docker 호환) |
| File Storage | MinIO (S3 호환) |
| Realtime | WebSocket (STOMP) |

---

## 지침 구조

```
/
├── AGENTS.md                    # 이 파일 (전체 프로젝트 메인 지침)
│
└── docs/
    ├── plan/                    # 일회성 설계/개선 지침
    │   ├── README.md
    │   ├── {YYYY-MM-DD}-{topic}.md
    │   └── archive/             # 완료된 플랜
    │
    └── develop/                 # 영구 지침 (도메인별)
        ├── README.md            # 개발 지침 개요
        ├── _common/             # 공통 지침
        └── {domain}/            # 도메인별 지침
            ├── README.md        # 메인 지침
            ├── {feature}.md     # 서브 지침 (단일)
            └── {feature}/       # 서브 지침 (분할)
                ├── README.md
                └── {section}.md
```

---

## 지침 관리 규칙

### 1. 분할 기준
- 단일 파일 **1,500줄 이상** → 디렉토리로 분할
- 서브 기능 **5개 이상** → 디렉토리로 분할 고려

### 2. 워크플로우
```
[기능 기획/개선]
       ↓
[docs/plan/{date}-{topic}.md 작성]  ← 일회성 설계 지침
       ↓
[설계 검토 & 승인]
       ↓
[docs/develop/{domain}/ 영구 지침화]
       ↓
[구현]
       ↓
[지침 vs 구현 검증]
       ↓
[docs/plan/{date}-{topic}.md → archive/ 이동]
```

### 3. 지침 작성 원칙
- 모든 지침은 **구현 가능한 수준**으로 상세히 작성
- API 스펙, 데이터 모델, 비즈니스 로직 포함
- 예외 케이스와 에러 처리 명시
- 테스트 케이스 포함

### 4. 지침 vs 구현 검증
- 구현 전: 해당 도메인 지침 읽기 필수
- 구현 후: 지침과 코드 일치 여부 검증
- 불일치 시: 지침 업데이트 또는 코드 수정

---

## TDD 및 테스트 커버리지

### 필수 요구사항
- **모든 기능은 TDD 방식으로 개발**
- **테스트 커버리지 100% 달성**
- **테스트 시나리오 미들웨어 내장**

### 테스트 계층

| 계층 | 비율 | 설명 |
|------|------|------|
| Unit Test | 70% | Service, Repository, Entity |
| Integration Test | 20% | API 엔드포인트 |
| E2E Test | 10% | 시나리오 미들웨어 |

### 테스트 시나리오 미들웨어

API 스펙에 맞게 **등록 → 조회 → 수정 → 삭제**를 자동 순환 테스트:
- SDD(Spec-Driven Development) 방식
- 의존성 기반 자동 실행 순서 결정
- 지침: `docs/develop/test-scenario/`

---

## 도메인 목록

### 핵심 도메인 (Phase 1)

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| _common | 공통 규칙 (TDD 포함) | `docs/develop/_common/` |
| test-scenario | 테스트 시나리오 미들웨어 | `docs/develop/test-scenario/` |
| auth | 인증/인가 (OAuth 포함) | `docs/develop/auth/` |
| user | 사용자 관리 | `docs/develop/user/` |
| pet | 반려동물 + 성향 체크리스트 | `docs/develop/pet/` |
| sitter | 시터 프로필/검색/서비스/자격증빙 | `docs/develop/sitter/` |
| inquiry | 1:1 문의 | `docs/develop/inquiry/` |

### 예약/결제 도메인 (Phase 2)

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| booking | 예약 (4단계 프로세스) | `docs/develop/booking/` |
| payment | 결제/카드관리 | `docs/develop/payment/` |
| availability | 시터 일정/캘린더 차단 | `docs/develop/availability/` |

### 돌봄/소통 도메인 (Phase 3)

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| care | 돌봄 일지/GPS 트래킹 | `docs/develop/care/` |
| chat | 실시간 채팅 (WebSocket) | `docs/develop/chat/` |
| review | 시터 후기/평점 | `docs/develop/review/` |
| notification | 알림/FCM 푸시 | `docs/develop/notification/` |

### 운영 도메인 (Phase 4)

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| payout | 시터 정산/수수료 | `docs/develop/payout/` |
| admin | 관리자 시스템 | `docs/develop/admin/` |
| faq | FAQ 관리 | `docs/develop/faq/` |

### 부가 도메인 (Phase 5)

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| community | 커뮤니티/게시판 | `docs/develop/community/` |
| policy | 약관 버전관리 | `docs/develop/policy/` |
| coupon | 이벤트/쿠폰 | `docs/develop/coupon/` |
| file | 파일/미디어 | `docs/develop/file/` |
| dashboard | 대시보드/통계 | `docs/develop/dashboard/` |

### Deprecated 도메인 (FindPlace 전용, PetPro에서 불필요)

| 도메인 | 설명 | 상태 |
|--------|------|------|
| ~~company~~ | 장례업체 | deprecated |
| ~~supplier~~ | 공급사 | deprecated |
| ~~product~~ | 상품 | deprecated |
| ~~inventory~~ | 재고 | deprecated |
| ~~reservation~~ | 장례 예약 | deprecated → booking |
| ~~order~~ | 굿즈 주문 | deprecated |
| ~~delivery~~ | 배송 | deprecated |
| ~~settlement~~ | 정산 | deprecated → payout |
| ~~columbarium~~ | 봉안당 | deprecated |
| ~~memorial~~ | 추모관 | deprecated |
| ~~schedule~~ | 일정 | deprecated → availability |

---

## 사용자 역할

| 역할 | 코드 | 설명 |
|------|------|------|
| 반려인 | CUSTOMER | 시터 검색, 예약, 결제, 돌봄 조회, 채팅, 커뮤니티 |
| 펫시터 | PARTNER | 프로필/자격 관리, 예약 수락/거절, 돌봄 일지, 정산 |
| 관리자 | ADMIN | 일반 관리 기능 |
| 최고 관리자 | SUPER_ADMIN | 전체 시스템 관리 |

---

## 데이터베이스 구조

### Master-Slave 구성
```
┌─────────────────────┐
│  PostgreSQL Master  │  ← CUD (Create, Update, Delete)
└──────────┬──────────┘
           │ Streaming Replication
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐ ┌─────────┐
│ Slave 1 │ │ Slave 2 │  ← R (Read Only)
└─────────┘ └─────────┘
```

### 라우팅 규칙
- `@Transactional(readOnly = false)` → Master
- `@Transactional(readOnly = true)` → Slave (Round Robin)

---

## 참조 우선순위

1. **AGENTS.md** - 프로젝트 전체 규칙 및 필수 지침 (이 파일)
2. **docs/develop/{domain}/README.md** - 작업 도메인별 영구 지침
3. **docs/plan/** - 진행 중인 설계/개선 지침

## 참조 문서

- IA 설계: `docs/plan/2026-02-06-petpro-ia-final.md`
- 개발 지침: `docs/develop/README.md`
