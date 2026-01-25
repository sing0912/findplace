# AGENTS.md - 반려동물 장례 토탈 플랫폼 (FindPlace)

## 프로젝트 개요

반려동물 장례 서비스를 위한 토탈 플랫폼입니다.
- 사용자(B2C): 장례업체 검색, 예약, 결제, 추모관
- 장례업체(B2B): 예약/일정/봉안당 관리
- 공급사: 상품/재고/주문/정산 관리
- 플랫폼 관리자: 통합 관리

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

- 영구지침 업데이트 없이 구현 진행
- 구현 후 영구지침 일치 검증 생략
- "나중에 문서화" 방식의 개발

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
| Infrastructure | Docker Compose |
| File Storage | MinIO (S3 호환) |

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

| 도메인 | 설명 | 지침 경로 |
|--------|------|-----------|
| _common | 공통 규칙 (TDD 포함) | `docs/develop/_common/` |
| test-scenario | 테스트 시나리오 미들웨어 | `docs/develop/test-scenario/` |
| auth | 인증/인가 | `docs/develop/auth/` |
| user | 사용자 관리 | `docs/develop/user/` |
| company | 장례업체 | `docs/develop/company/` |
| supplier | 공급사 | `docs/develop/supplier/` |
| pet | 반려동물 | `docs/develop/pet/` |
| product | 상품 | `docs/develop/product/` |
| inventory | 재고 | `docs/develop/inventory/` |
| reservation | 예약 (장례) | `docs/develop/reservation/` |
| order | 주문 (굿즈) | `docs/develop/order/` |
| delivery | 배송 | `docs/develop/delivery/` |
| payment | 결제 | `docs/develop/payment/` |
| settlement | 정산 | `docs/develop/settlement/` |
| columbarium | 봉안당 | `docs/develop/columbarium/` |
| memorial | 추모관 | `docs/develop/memorial/` |
| board | 게시판 | `docs/develop/board/` |
| notification | 알림/SMS/이메일 | `docs/develop/notification/` |
| schedule | 일정 | `docs/develop/schedule/` |
| file | 파일/미디어 | `docs/develop/file/` |
| dashboard | 대시보드/통계 | `docs/develop/dashboard/` |

---

## 사용자 역할

| 역할 | 코드 | 설명 |
|------|------|------|
| 일반 사용자 | CUSTOMER | 예약, 주문, 추모관 이용 |
| 장례업체 관리자 | COMPANY_ADMIN | 업체 관리, 예약/일정 관리 |
| 공급사 관리자 | SUPPLIER_ADMIN | 상품/재고/주문/정산 관리 |
| 플랫폼 관리자 | PLATFORM_ADMIN | 전체 시스템 관리 |

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

## 배송 타입

| 타입 | 코드 | 설명 |
|------|------|------|
| 공급사 직배송 | DIRECT | 공급사가 직접 배송 |
| 사입배송 | PURCHASE | 플랫폼이 매입 후 배송 |
| 물류창고 배송 | WAREHOUSE | 3PL 위탁 배송 |

---

## 참조 문서

- 초기 설계: `docs/plan/2026-01-24-initial-design.md`
- 개발 지침: `docs/develop/README.md`
