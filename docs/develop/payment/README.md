# 결제 (Payment)

## 개요

예약/주문에 대한 결제를 관리하는 도메인입니다.

---

## 엔티티

### Payment

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| paymentNumber | String | 결제번호 | Unique, Not Null |
| userId | Long | 결제자 ID (FK) | Not Null |
| paymentType | Enum | 결제 유형 | Not Null |
| referenceType | Enum | 참조 유형 | Not Null |
| referenceId | Long | 참조 ID | Not Null |
| amount | Decimal | 결제 금액 | Not Null |
| method | Enum | 결제 수단 | Not Null |
| status | Enum | 상태 | Not Null |
| pgProvider | String | PG사 | Nullable |
| pgTransactionId | String | PG 거래번호 | Nullable |
| paidAt | DateTime | 결제일시 | Nullable |
| cancelledAt | DateTime | 취소일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### PaymentType

| 값 | 설명 |
|----|------|
| RESERVATION | 예약 결제 |
| ORDER | 주문 결제 |
| COLUMBARIUM | 봉안당 결제 |

### ReferenceType

| 값 | 설명 |
|----|------|
| RESERVATION | 예약 |
| ORDER | 주문 |
| COLUMBARIUM_CONTRACT | 봉안당 계약 |

### PaymentMethod

| 값 | 설명 |
|----|------|
| CARD | 신용카드 |
| BANK_TRANSFER | 계좌이체 |
| VIRTUAL_ACCOUNT | 가상계좌 |
| KAKAO_PAY | 카카오페이 |
| NAVER_PAY | 네이버페이 |
| TOSS_PAY | 토스페이 |

### PaymentStatus

| 값 | 설명 |
|----|------|
| PENDING | 결제 대기 |
| COMPLETED | 결제 완료 |
| FAILED | 결제 실패 |
| CANCELLED | 전체 취소 |
| PARTIAL_CANCELLED | 부분 취소 |

### Refund

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| paymentId | Long | 결제 ID (FK) | Not Null |
| refundNumber | String | 환불번호 | Unique, Not Null |
| amount | Decimal | 환불 금액 | Not Null |
| reason | String | 환불 사유 | Not Null |
| status | Enum | 상태 | Not Null |
| refundedAt | DateTime | 환불일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/payments | 목록 조회 | 인증된 사용자 |
| POST | /api/v1/payments | 결제 요청 | 인증된 사용자 |
| GET | /api/v1/payments/{id} | 상세 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/payments/{id} | 수정 | PLATFORM_ADMIN |
| DELETE | /api/v1/payments/{id} | 삭제 | PLATFORM_ADMIN |
| POST | /api/v1/payments/webhook | PG 웹훅 | Internal |
| POST | /api/v1/payments/{id}/refund | 환불 요청 | 본인 또는 ADMIN |
| GET | /api/v1/payments/{id}/receipt | 영수증 조회 | 본인 또는 ADMIN |

---

## 결제 플로우

```
[클라이언트]                    [서버]                     [PG]
     │                           │                          │
     │  1. 결제 요청              │                          │
     │──────────────────────────▶│                          │
     │                           │  2. 결제 준비            │
     │                           │─────────────────────────▶│
     │  3. PG 결제창             │                          │
     │◀─────────────────────────────────────────────────────│
     │                           │                          │
     │  4. 결제 완료             │                          │
     │─────────────────────────────────────────────────────▶│
     │                           │  5. 결제 승인 요청       │
     │                           │◀─────────────────────────│
     │                           │                          │
     │                           │  6. 결제 검증            │
     │                           │─────────────────────────▶│
     │                           │                          │
     │  7. 결제 완료 응답        │                          │
     │◀──────────────────────────│                          │
```

---

## PG 연동

### 지원 PG사

- 토스페이먼츠 (기본)
- KG이니시스
- NHN KCP

### 웹훅 처리

```java
@PostMapping("/api/v1/payments/webhook")
public ResponseEntity<Void> handleWebhook(
    @RequestHeader("X-PG-Signature") String signature,
    @RequestBody String payload) {

    // 1. 서명 검증
    // 2. 결제 상태 업데이트
    // 3. 관련 도메인 업데이트 (주문 상태 등)

    return ResponseEntity.ok().build();
}
```

---

## 비즈니스 규칙

1. 결제 금액은 참조 대상과 일치해야 함
2. 부분 환불 지원
3. 환불 금액은 결제 금액을 초과할 수 없음
4. 결제 완료 후 관련 도메인 상태 자동 변경
5. 결제 실패 시 재시도 가능

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [process.md](./process.md) | 결제 처리 |
| [refund.md](./refund.md) | 환불 처리 |
| [webhook.md](./webhook.md) | 웹훅 처리 |
| [pg-integration.md](./pg-integration.md) | PG 연동 |
