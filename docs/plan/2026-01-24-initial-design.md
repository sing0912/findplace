# 초기 시스템 설계 - 반려동물 장례 토탈 플랫폼

## 배경

반려동물 장례 서비스 시장에서 사용자와 장례업체, 용품 공급사를 연결하는 통합 플랫폼이 필요합니다.

---

## 목표

1. 사용자가 쉽게 장례업체를 검색하고 예약할 수 있는 서비스 제공
2. 장례업체의 예약/일정/봉안당 관리 효율화
3. 공급사의 상품/재고/주문/정산 통합 관리
4. 사이버 추모관을 통한 지속적인 사용자 engagement

---

## 범위

### 포함 (이번 설계)
- 전체 시스템 아키텍처
- 모든 도메인 설계 (API, 데이터 모델)
- 기술 스택 확정

### 제외 (향후)
- 상세 UI/UX 설계
- 외부 연동 (PG, SMS, 정부24 등) 상세 스펙
- 성능 최적화

---

## 시스템 아키텍처

### 전체 구조

```
┌─────────────────────────────────────────────────────────────┐
│                      Nginx (Reverse Proxy)                  │
└──────────────┬──────────────────────┬───────────────────────┘
               │                      │
    ┌──────────▼──────────┐  ┌───────▼────────┐
    │   Frontend (React)   │  │  Backend API   │
    │   - 사용자 웹        │  │  (Spring Boot) │
    │   - B2B 어드민       │  │                │
    │   - 공급사 어드민    │  │                │
    │   - 통합 어드민      │  └───────┬────────┘
    └─────────────────────┘          │
                              ┌──────▼──────┐
                              │  DataSource │
                              │   Router    │
                              └──────┬──────┘
               ┌─────────────────────┼─────────────────────┐
               │                     │                     │
    ┌──────────▼──────────┐         │          ┌──────────▼──────────┐
    │  PostgreSQL Master  │         │          │   Load Balancer     │
    │  (CUD - Write)      │         │          │   (Round Robin)     │
    └──────────┬──────────┘         │          └──────────┬──────────┘
               │                    │                     │
               │    Streaming Replication    ┌────────────┼────────────┐
               │                             │            │            │
               ├─────────────────────────────▶  Slave 1   │  Slave 2   │
               │                             │  (Read)    │  (Read)    │
               └─────────────────────────────▶            │            │
                                             └────────────┴────────────┘
```

### 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Backend | Java | 21+ |
| Backend | Spring Boot | 3.2+ |
| Frontend | React | 18+ |
| Frontend | TypeScript | 5+ |
| Database | PostgreSQL | 16+ |
| Cache | Redis | 7+ |
| Container | Docker | 24+ |
| Container | Docker Compose | 2.20+ |
| File Storage | MinIO | Latest |
| Reverse Proxy | Nginx | Latest |

---

## 도메인 모델

### 핵심 엔티티 관계

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              User (사용자)                               │
│  - CUSTOMER (일반 사용자)                                                │
│  - COMPANY_ADMIN (장례업체 관리자)                                        │
│  - SUPPLIER_ADMIN (공급사 관리자)                                         │
│  - PLATFORM_ADMIN (통합 관리자)                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Company   │     │  Supplier   │     │     Pet     │
│  (장례업체)  │     │   (공급사)   │     │  (반려동물)  │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                 ▼
┌─────────────┐     ┌─────────────┐   ┌─────────────┐
│   Product   │────▶│  Inventory  │   │ Settlement  │
│   (상품)    │     │   (재고)    │   │   (정산)    │
└──────┬──────┘     └─────────────┘   └─────────────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐   ┌─────────────┐
│    Order    │────▶│  Delivery   │   │   Shipment  │
│   (주문)    │     │   (배송)    │   │  (출고정보)  │
└─────────────┘     └─────────────┘   └─────────────┘
```

### 엔티티 상세

#### User (사용자)
```
- id: Long (PK)
- email: String (Unique)
- password: String (Encrypted)
- name: String
- phone: String
- role: Enum (CUSTOMER, COMPANY_ADMIN, SUPPLIER_ADMIN, PLATFORM_ADMIN)
- status: Enum (ACTIVE, INACTIVE, SUSPENDED)
- createdAt: DateTime
- updatedAt: DateTime
```

#### Company (장례업체)
```
- id: Long (PK)
- name: String
- businessNumber: String (Unique)
- address: String
- latitude: Decimal
- longitude: Decimal
- phone: String
- operatingHours: JSON
- isObservationAllowed: Boolean (참관 여부)
- maxWeight: Decimal (최대 몸무게 제한)
- status: Enum (PENDING, APPROVED, REJECTED, SUSPENDED)
- createdAt: DateTime
- updatedAt: DateTime
```

#### Supplier (공급사)
```
- id: Long (PK)
- name: String
- businessNumber: String (Unique)
- address: String
- phone: String
- bankAccount: JSON
- settlementCycle: Enum (WEEKLY, BIWEEKLY, MONTHLY)
- status: Enum (PENDING, APPROVED, REJECTED, SUSPENDED)
- createdAt: DateTime
- updatedAt: DateTime
```

#### Product (상품)
```
- id: Long (PK)
- supplierId: Long (FK)
- categoryId: Long (FK)
- name: String
- description: Text
- price: Decimal
- costPrice: Decimal
- deliveryType: Enum (DIRECT, PURCHASE, WAREHOUSE)
- status: Enum (DRAFT, ACTIVE, INACTIVE, SOLDOUT)
- createdAt: DateTime
- updatedAt: DateTime
```

#### Inventory (재고)
```
- id: Long (PK)
- supplierId: Long (FK)
- productId: Long (FK)
- warehouseId: Long (FK, nullable) - 확장용
- quantity: Integer
- safetyStock: Integer
- reservedQuantity: Integer
- createdAt: DateTime
- updatedAt: DateTime
```

#### Order (주문)
```
- id: Long (PK)
- userId: Long (FK)
- orderNumber: String (Unique)
- totalAmount: Decimal
- status: Enum (PENDING, PAID, PREPARING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)
- createdAt: DateTime
- updatedAt: DateTime
```

#### Delivery (배송)
```
- id: Long (PK)
- orderId: Long (FK)
- supplierId: Long (FK)
- deliveryType: Enum (DIRECT, PURCHASE, WAREHOUSE)
- trackingNumber: String
- carrier: String
- status: Enum (PENDING, PICKED_UP, IN_TRANSIT, DELIVERED)
- shippedAt: DateTime
- deliveredAt: DateTime
- createdAt: DateTime
- updatedAt: DateTime
```

#### Settlement (정산)
```
- id: Long (PK)
- supplierId: Long (FK)
- periodStart: Date
- periodEnd: Date
- totalSales: Decimal
- commission: Decimal
- netAmount: Decimal
- status: Enum (PENDING, CONFIRMED, PAID)
- paidAt: DateTime
- createdAt: DateTime
- updatedAt: DateTime
```

---

## API 구조

### 엔드포인트 전체 목록

```
/api/v1
├── /auth                    # 인증/인가
│   ├── POST   /login
│   ├── POST   /logout
│   ├── POST   /refresh
│   ├── POST   /register
│   └── POST   /password/reset
│
├── /users                   # 사용자
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /companies               # 장례업체
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /nearby
│   ├── GET    /{id}/products
│   ├── GET    /{id}/schedules
│   └── PUT    /{id}/status
│
├── /suppliers               # 공급사
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /{id}/products
│   ├── GET    /{id}/orders
│   ├── GET    /{id}/inventory
│   └── GET    /{id}/settlements
│
├── /pets                    # 반려동물
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /products                # 상품
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /categories
│   └── PUT    /{id}/status
│
├── /inventory               # 재고
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /history
│   ├── POST   /adjust
│   └── GET    /low-stock
│
├── /reservations            # 예약 (장례)
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /{id}/status
│   ├── PUT    /{id}/status
│   └── PUT    /{id}/cancel
│
├── /orders                  # 주문 (굿즈)
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /{id}/delivery
│   ├── PUT    /{id}/status
│   └── PUT    /{id}/cancel
│
├── /deliveries              # 배송
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── PUT    /{id}/status
│   └── GET    /{id}/tracking
│
├── /payments                # 결제
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── POST   /webhook
│   ├── POST   /{id}/refund
│   └── GET    /{id}/receipt
│
├── /settlements             # 정산
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── PUT    /{id}/complete
│   └── GET    /summary
│
├── /columbariums            # 봉안당
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /grid
│   ├── GET    /expiring
│   └── POST   /{id}/renew
│
├── /memorials               # 추모관
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /{id}/guestbook
│   ├── POST   /{id}/guestbook
│   ├── POST   /{id}/media
│   └── GET    /{id}/anniversaries
│
├── /boards                  # 게시판
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   └── DELETE /{id}
│
├── /posts                   # 게시글
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   ├── GET    /{id}/comments
│   └── POST   /{id}/comments
│
├── /notifications           # 알림
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   └── PUT    /{id}/read
│
├── /sms                     # SMS
│   ├── GET    /
│   ├── POST   /send
│   ├── POST   /bulk
│   ├── GET    /{id}
│   └── GET    /templates
│
├── /emails                  # 이메일
│   ├── GET    /
│   ├── POST   /send
│   ├── POST   /bulk
│   ├── GET    /{id}
│   └── GET    /templates
│
├── /schedules               # 일정
│   ├── GET    /
│   ├── POST   /
│   ├── GET    /{id}
│   ├── PUT    /{id}
│   ├── DELETE /{id}
│   └── GET    /calendar
│
├── /files                   # 파일
│   ├── GET    /
│   ├── POST   /upload
│   ├── GET    /{id}
│   ├── DELETE /{id}
│   └── POST   /bulk-upload
│
└── /dashboard               # 대시보드
    ├── GET    /summary
    ├── GET    /today
    ├── GET    /reservations
    ├── GET    /sales
    └── GET    /expiring
```

---

## 배송 타입

| 타입 | 코드 | 설명 | 재고 관리 |
|------|------|------|-----------|
| 공급사 직배송 | DIRECT | 공급사가 직접 배송 | 공급사 재고 |
| 사입배송 | PURCHASE | 플랫폼이 매입 후 배송 | 플랫폼 재고 |
| 물류창고 배송 | WAREHOUSE | 3PL 위탁 배송 | 창고 재고 |

---

## 다음 단계

1. [ ] docs/develop/ 영구 지침 작성
2. [ ] Docker Compose 설정
3. [ ] Spring Boot 프로젝트 초기화
4. [ ] React 프로젝트 초기화
5. [ ] 공통 모듈 구현 (인증, 에러 처리 등)

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-01-24 | 초기 설계 작성 |
