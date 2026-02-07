# 결제 (Payment)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. 개요

돌봄 예약에 대한 결제와 사용자 카드 관리를 담당하는 도메인입니다. 반려인(CUSTOMER)이 시터의 예약 수락 후 결제를 진행하며, 등록된 카드로 간편 결제할 수 있습니다.

> **마이그레이션 참고**: 기존 PetPro의 RESERVATION/ORDER/COLUMBARIUM 결제 유형은 모두 제거되고, PetPro에서는 BOOKING 유형만 사용합니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| booking | 1:1 | 예약 결제 (referenceId = bookingId) |
| user | N:1 | 결제자 (반려인) |

---

## 2. 엔티티

### 2.1 Payment

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| paymentNumber | String | 결제번호 (PAY-YYYYMMDD-XXXXX) | Unique, Not Null |
| userId | Long | 결제자 ID (FK → users) | Not Null |
| paymentType | Enum | 결제 유형 | Not Null |
| referenceType | Enum | 참조 유형 | Not Null |
| referenceId | Long | 참조 ID (bookingId) | Not Null |
| amount | Decimal | 결제 금액 | Not Null |
| method | Enum | 결제 수단 | Not Null |
| status | Enum | 상태 | Not Null |
| pgProvider | String | PG사 | Nullable |
| pgTransactionId | String | PG 거래번호 | Nullable |
| userPaymentCardId | Long | 결제 카드 ID (FK) | Nullable |
| paidAt | DateTime | 결제일시 | Nullable |
| cancelledAt | DateTime | 취소일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### 2.2 PaymentType

| 값 | 설명 |
|----|------|
| BOOKING | 돌봄 예약 결제 |

### 2.3 ReferenceType

| 값 | 설명 |
|----|------|
| BOOKING | 예약 |

### 2.4 PaymentMethod

| 값 | 설명 |
|----|------|
| CARD | 신용/체크카드 |
| KAKAO_PAY | 카카오페이 |
| NAVER_PAY | 네이버페이 |
| TOSS_PAY | 토스페이 |

### 2.5 PaymentStatus

| 값 | 설명 |
|----|------|
| PENDING | 결제 대기 |
| COMPLETED | 결제 완료 |
| FAILED | 결제 실패 |
| CANCELLED | 전체 취소 |
| PARTIAL_CANCELLED | 부분 취소 |

### 2.6 UserPaymentCard (카드 관리)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 소유자 ID (FK → users) | Not Null |
| cardCompany | String | 카드사 (삼성/신한/현대 등) | Not Null |
| cardNumber | String | 카드번호 (마스킹: ****-****-****-1234) | Not Null |
| cardAlias | String | 카드 별명 | Nullable, Max 50자 |
| isDefault | Boolean | 기본 카드 여부 | Not Null, Default: false |
| createdAt | DateTime | 생성일시 | Not Null |

### 2.7 Refund

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| paymentId | Long | 결제 ID (FK → payments) | Not Null |
| refundNumber | String | 환불번호 | Unique, Not Null |
| amount | Decimal | 환불 금액 | Not Null |
| reason | String | 환불 사유 | Not Null |
| status | Enum | 상태 | Not Null |
| refundedAt | DateTime | 환불일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

---

## 3. API 목록

### 3.1 결제 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | /api/v1/payments | 결제 요청 (bookingId 기반) | CUSTOMER |
| 2 | GET | /api/v1/payments | 결제 목록 조회 | CUSTOMER |
| 3 | GET | /api/v1/payments/{id} | 결제 상세 조회 | 본인 또는 ADMIN |
| 4 | POST | /api/v1/payments/{id}/refund | 환불 요청 | 본인 또는 ADMIN |
| 5 | GET | /api/v1/payments/{id}/receipt | 영수증 조회 | 본인 또는 ADMIN |
| 6 | POST | /api/v1/payments/webhook | PG 웹훅 수신 | Internal |

### 3.2 카드 관리 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 7 | GET | /api/v1/payment-cards | 내 카드 목록 | CUSTOMER |
| 8 | POST | /api/v1/payment-cards | 카드 등록 | CUSTOMER |
| 9 | PUT | /api/v1/payment-cards/{id} | 카드 수정 (별명, 기본카드) | CUSTOMER |
| 10 | DELETE | /api/v1/payment-cards/{id} | 카드 삭제 | CUSTOMER |

---

## 4. 결제 요청

```
POST /api/v1/payments

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "bookingId": 1,
  "method": "CARD",
  "userPaymentCardId": 3
}

Response 201:
{
  "id": 1,
  "paymentNumber": "PAY-20260207-00001",
  "bookingId": 1,
  "bookingNumber": "BK-20260207-00001",
  "amount": 55000,
  "method": "CARD",
  "status": "COMPLETED",
  "paidAt": "2026-02-07T15:00:00",
  "createdAt": "2026-02-07T15:00:00"
}

Error 400:
{ "code": "INVALID_PAYMENT_AMOUNT", "message": "결제 금액이 일치하지 않습니다." }
{ "code": "BOOKING_NOT_PAYABLE", "message": "결제 가능한 예약 상태가 아닙니다." }

Error 402:
{ "code": "PAYMENT_FAILED", "message": "결제에 실패했습니다." }
```

---

## 5. 카드 관리 API

### 5.1 카드 목록

```
GET /api/v1/payment-cards

Headers:
  Authorization: Bearer {accessToken}

Response 200:
[
  {
    "id": 1,
    "cardCompany": "삼성카드",
    "cardNumber": "****-****-****-1234",
    "cardAlias": "내 삼성카드",
    "isDefault": true,
    "createdAt": "2026-01-15T10:00:00"
  },
  {
    "id": 2,
    "cardCompany": "현대카드",
    "cardNumber": "****-****-****-5678",
    "cardAlias": null,
    "isDefault": false,
    "createdAt": "2026-02-01T14:30:00"
  }
]
```

### 5.2 카드 등록

```
POST /api/v1/payment-cards

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "cardCompany": "신한카드",
  "cardNumber": "4321-1234-5678-9012",
  "cardAlias": "생활비 카드",
  "isDefault": false
}

Response 201:
{
  "id": 3,
  "cardCompany": "신한카드",
  "cardNumber": "****-****-****-9012",
  "cardAlias": "생활비 카드",
  "isDefault": false,
  "createdAt": "2026-02-07T16:00:00"
}

Error 400:
{ "code": "INVALID_CARD_NUMBER", "message": "유효하지 않은 카드 번호입니다." }
{ "code": "MAX_CARDS_EXCEEDED", "message": "최대 5장까지 등록할 수 있습니다." }
```

### 5.3 카드 수정

```
PUT /api/v1/payment-cards/{id}

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "cardAlias": "주 결제 카드",
  "isDefault": true
}

Response 200:
{
  "id": 3,
  "cardCompany": "신한카드",
  "cardNumber": "****-****-****-9012",
  "cardAlias": "주 결제 카드",
  "isDefault": true,
  "createdAt": "2026-02-07T16:00:00"
}
```

> **참고**: `isDefault`를 true로 변경하면 기존 기본 카드의 isDefault는 자동으로 false로 변경됩니다.

### 5.4 카드 삭제

```
DELETE /api/v1/payment-cards/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{ "success": true, "message": "카드가 삭제되었습니다." }

Error 400:
{ "code": "DEFAULT_CARD_CANNOT_DELETE", "message": "기본 카드는 삭제할 수 없습니다. 다른 카드를 기본으로 설정한 후 삭제해주세요." }
```

---

## 6. 결제 플로우

```
[클라이언트]                    [서버]                     [PG]
     │                           │                          │
     │  1. 결제 요청              │                          │
     │  (bookingId, method,      │                          │
     │   userPaymentCardId)      │                          │
     │──────────────────────────▶│                          │
     │                           │  2. 예약 상태 검증        │
     │                           │  (ACCEPTED 상태 확인)     │
     │                           │                          │
     │                           │  3. PG 결제 요청         │
     │                           │─────────────────────────▶│
     │                           │                          │
     │  4. PG 결제창 (간편결제)   │                          │
     │◀─────────────────────────────────────────────────────│
     │                           │                          │
     │  5. 결제 승인             │                          │
     │─────────────────────────────────────────────────────▶│
     │                           │  6. 결제 승인 결과       │
     │                           │◀─────────────────────────│
     │                           │                          │
     │                           │  7. 결제 완료 처리       │
     │                           │  (Payment COMPLETED,     │
     │                           │   Booking → CONFIRMED)   │
     │                           │                          │
     │  8. 결제 완료 응답        │                          │
     │◀──────────────────────────│                          │
```

### 결제 완료 시 자동 처리

1. Payment 상태 → `COMPLETED`
2. Booking 상태 → `CONFIRMED` (ACCEPTED에서 전이)
3. BookingStatusHistory 이력 추가
4. 반려인/시터에게 알림 발송

---

## 7. PG 연동

### 지원 PG사

- 토스페이먼츠 (기본)
- KG이니시스

### 웹훅 처리

```java
@PostMapping("/api/v1/payments/webhook")
public ResponseEntity<Void> handleWebhook(
    @RequestHeader("X-PG-Signature") String signature,
    @RequestBody String payload) {

    // 1. 서명 검증
    // 2. 결제 상태 업데이트
    // 3. Booking 상태 자동 변경

    return ResponseEntity.ok().build();
}
```

---

## 8. 비즈니스 규칙

1. 결제 금액은 Booking의 totalAmount와 일치해야 함
2. ACCEPTED 상태의 Booking만 결제 가능
3. 부분 환불 지원 (취소 정책에 따른 환불율 적용)
4. 환불 금액은 결제 금액을 초과할 수 없음
5. 결제 완료 후 Booking 상태 자동 CONFIRMED 전이
6. 결제 실패 시 재시도 가능
7. 카드는 최대 5장까지 등록 가능
8. 기본 카드 삭제 시 다른 카드를 먼저 기본으로 설정해야 함
9. 결제 취소 시 환불 정책에 따라 Refund 생성

---

## 9. DDL

### 9.1 payments

```sql
CREATE TABLE payments (
    id                      BIGSERIAL       PRIMARY KEY,
    payment_number          VARCHAR(25)     NOT NULL UNIQUE,
    user_id                 BIGINT          NOT NULL REFERENCES users(id),
    payment_type            VARCHAR(20)     NOT NULL DEFAULT 'BOOKING',
    reference_type          VARCHAR(20)     NOT NULL DEFAULT 'BOOKING',
    reference_id            BIGINT          NOT NULL,
    amount                  DECIMAL(12,2)   NOT NULL,
    method                  VARCHAR(20)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    pg_provider             VARCHAR(50),
    pg_transaction_id       VARCHAR(100),
    user_payment_card_id    BIGINT          REFERENCES user_payment_cards(id),
    paid_at                 TIMESTAMP,
    cancelled_at            TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 9.2 user_payment_cards

```sql
CREATE TABLE user_payment_cards (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    card_company    VARCHAR(50)     NOT NULL,
    card_number     VARCHAR(30)     NOT NULL,
    card_alias      VARCHAR(50),
    is_default      BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 9.3 refunds

```sql
CREATE TABLE refunds (
    id              BIGSERIAL       PRIMARY KEY,
    payment_id      BIGINT          NOT NULL REFERENCES payments(id),
    refund_number   VARCHAR(25)     NOT NULL UNIQUE,
    amount          DECIMAL(12,2)   NOT NULL,
    reason          VARCHAR(300)    NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    refunded_at     TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 10. 인덱스

```sql
-- payments
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_reference ON payments(reference_type, reference_id);
CREATE INDEX idx_payments_status ON payments(status);

-- user_payment_cards
CREATE INDEX idx_user_payment_cards_user_id ON user_payment_cards(user_id);

-- refunds
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
```

---

## 11. 패키지 구조

```
backend/src/main/java/com/petpro/domain/payment/
├── entity/
│   ├── Payment.java
│   ├── PaymentType.java
│   ├── ReferenceType.java
│   ├── PaymentMethod.java
│   ├── PaymentStatus.java
│   ├── UserPaymentCard.java
│   └── Refund.java
├── repository/
│   ├── PaymentRepository.java
│   ├── UserPaymentCardRepository.java
│   └── RefundRepository.java
├── service/
│   ├── PaymentService.java
│   ├── UserPaymentCardService.java
│   └── RefundService.java
├── controller/
│   ├── PaymentController.java
│   ├── UserPaymentCardController.java
│   └── PaymentWebhookController.java
└── dto/
    ├── PaymentRequest.java
    ├── PaymentResponse.java
    ├── UserPaymentCardRequest.java
    ├── UserPaymentCardResponse.java
    └── RefundResponse.java
```

---

## 12. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| PAYMENT_NOT_FOUND | 404 | 결제를 찾을 수 없습니다. |
| INVALID_PAYMENT_AMOUNT | 400 | 결제 금액이 일치하지 않습니다. |
| BOOKING_NOT_PAYABLE | 400 | 결제 가능한 예약 상태가 아닙니다. |
| PAYMENT_FAILED | 402 | 결제에 실패했습니다. |
| PAYMENT_ALREADY_COMPLETED | 400 | 이미 완료된 결제입니다. |
| REFUND_EXCEEDS_AMOUNT | 400 | 환불 금액이 결제 금액을 초과합니다. |
| CARD_NOT_FOUND | 404 | 카드를 찾을 수 없습니다. |
| INVALID_CARD_NUMBER | 400 | 유효하지 않은 카드 번호입니다. |
| MAX_CARDS_EXCEEDED | 400 | 최대 5장까지 등록할 수 있습니다. |
| DEFAULT_CARD_CANNOT_DELETE | 400 | 기본 카드는 삭제할 수 없습니다. |
| UNAUTHORIZED_PAYMENT_ACCESS | 403 | 해당 결제에 대한 접근 권한이 없습니다. |

---

## 13. 프론트엔드

### 13.1 파일 구조

```
frontend/src/
├── components/
│   └── payment/
│       ├── CardSelector.tsx              # 카드 선택 컴포넌트
│       ├── PaymentMethodSelector.tsx     # 결제 수단 선택 (카드/간편결제)
│       ├── PaymentSummary.tsx            # 결제 금액 요약
│       ├── PaymentCardItem.tsx           # 카드 목록 아이템
│       ├── AddCardModal.tsx              # 카드 등록 모달
│       └── index.ts
│
├── pages/
│   └── payment/
│       ├── PaymentPage.tsx               # 결제 화면
│       ├── PaymentCompletePage.tsx        # 결제 완료
│       └── PaymentCardsPage.tsx          # 카드 관리 (마이페이지)
│
├── hooks/
│   └── usePayment.ts
│
└── api/
    └── payment.ts
```

### 13.2 라우팅

| 경로 | 컴포넌트 | 인증 | 설명 |
|------|----------|------|------|
| /payment/:bookingId | PaymentPage | CUSTOMER | 결제 화면 |
| /payment/complete | PaymentCompletePage | CUSTOMER | 결제 완료 |
| /mypage/cards | PaymentCardsPage | CUSTOMER | 카드 관리 |

### 13.3 결제 화면 (PaymentPage) 레이아웃

```
┌─────────────────────────────────────┐
│  결제                                │
├─────────────────────────────────────┤
│  예약 정보                           │
│  ┌─────────────────────────────┐   │
│  │ 시터: 김시터                  │   │
│  │ 서비스: 데이케어              │   │
│  │ 날짜: 2/10(월) 09:00~18:00  │   │
│  │ 반려동물: 초코, 콩이          │   │
│  └─────────────────────────────┘   │
│                                      │
│  결제 수단                           │
│  ┌─────────────────────────────┐   │
│  │ ○ 등록된 카드                 │   │
│  │   [삼성 ****1234] (기본)     │   │
│  │   [현대 ****5678]            │   │
│  │   [+ 새 카드 등록]            │   │
│  │ ○ 카카오페이                  │   │
│  │ ○ 네이버페이                  │   │
│  │ ○ 토스페이                    │   │
│  └─────────────────────────────┘   │
│                                      │
│  결제 금액                           │
│  ┌─────────────────────────────┐   │
│  │ 기본가          40,000원     │   │
│  │ 추가 동물        15,000원    │   │
│  │ ──────────────────────      │   │
│  │ 총 결제 금액     55,000원    │   │
│  └─────────────────────────────┘   │
│                                      │
│  [55,000원 결제하기]                  │
└─────────────────────────────────────┘
```

### 13.4 카드 관리 화면 (PaymentCardsPage) 레이아웃

```
┌─────────────────────────────────────┐
│  결제 수단 관리                       │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │ 삼성카드  ****1234  (기본)   │   │
│  │ 내 삼성카드          [편집]  │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ 현대카드  ****5678           │   │
│  │ -                   [편집]  │   │
│  └─────────────────────────────┘   │
│                                      │
│  [+ 카드 추가]                        │
└─────────────────────────────────────┘
```

---

## 14. 서브 지침

| 파일 | 설명 |
|------|------|
| [process.md](./process.md) | 결제 처리 |
| [refund.md](./refund.md) | 환불 처리 |
| [webhook.md](./webhook.md) | 웹훅 처리 |
| [pg-integration.md](./pg-integration.md) | PG 연동 |
