# 주문 (Order)

## 개요

굿즈/용품 주문을 관리하는 도메인입니다.

---

## 엔티티

### Order

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 주문자 ID (FK) | Not Null |
| orderNumber | String | 주문번호 | Unique, Not Null |
| totalProductAmount | Decimal | 상품 합계 | Not Null |
| totalDeliveryFee | Decimal | 배송비 합계 | Not Null |
| totalDiscountAmount | Decimal | 할인 합계 | Not Null, Default 0 |
| totalAmount | Decimal | 최종 결제금액 | Not Null |
| status | Enum | 주문 상태 | Not Null |
| orderedAt | DateTime | 주문일시 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### OrderItem

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| orderId | Long | 주문 ID (FK) | Not Null |
| productId | Long | 상품 ID (FK) | Not Null |
| supplierId | Long | 공급사 ID (FK) | Not Null |
| productOptionId | Long | 옵션 ID (FK) | Nullable |
| productName | String | 상품명 (스냅샷) | Not Null |
| optionName | String | 옵션명 (스냅샷) | Nullable |
| price | Decimal | 단가 | Not Null |
| quantity | Integer | 수량 | Not Null |
| amount | Decimal | 금액 (단가 × 수량) | Not Null |
| deliveryType | Enum | 배송 유형 | Not Null |
| status | Enum | 아이템 상태 | Not Null |

### OrderStatus

| 값 | 설명 |
|----|------|
| PENDING | 결제 대기 |
| PAID | 결제 완료 |
| PREPARING | 상품 준비중 |
| SHIPPED | 배송중 |
| DELIVERED | 배송 완료 |
| CANCELLED | 취소됨 |
| REFUNDED | 환불됨 |

### OrderItemStatus

| 값 | 설명 |
|----|------|
| PENDING | 대기 |
| CONFIRMED | 확인됨 |
| PREPARING | 준비중 |
| SHIPPED | 배송중 |
| DELIVERED | 배송완료 |
| CANCELLED | 취소됨 |
| REFUNDED | 환불됨 |

### ShippingAddress

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| orderId | Long | 주문 ID (FK) | Not Null |
| recipientName | String | 수령인 | Not Null |
| phone | String | 연락처 | Not Null |
| zipCode | String | 우편번호 | Not Null |
| address | String | 주소 | Not Null |
| addressDetail | String | 상세주소 | Nullable |
| memo | String | 배송 메모 | Nullable |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/orders | 목록 조회 | 인증된 사용자 |
| POST | /api/v1/orders | 주문 생성 | 인증된 사용자 |
| GET | /api/v1/orders/{id} | 상세 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/orders/{id} | 수정 | ADMIN |
| DELETE | /api/v1/orders/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/orders/{id}/delivery | 배송 조회 | 본인 또는 ADMIN |
| PUT | /api/v1/orders/{id}/status | 상태 변경 | SUPPLIER_ADMIN, ADMIN |
| PUT | /api/v1/orders/{id}/cancel | 취소 | 본인 (조건부) |

---

## 주문 플로우

```
[장바구니]
     ↓
[주문서 작성]
     ↓
[결제 요청] → PENDING
     ↓
[결제 완료] → PAID
     ↓
[상품 준비] → PREPARING (공급사별)
     ↓
[배송 시작] → SHIPPED
     ↓
[배송 완료] → DELIVERED
```

### 취소 플로우

```
[취소 요청]
     ↓
상태 확인
├── PENDING/PAID → 즉시 취소 가능
├── PREPARING → 공급사 승인 필요
└── SHIPPED 이후 → 취소 불가 (반품으로 처리)
```

---

## 비즈니스 규칙

1. 주문번호 형식: YYYYMMDD + 8자리 랜덤
2. 주문 시 재고 예약 (reservedQuantity 증가)
3. 결제 완료 시 재고 차감
4. 취소 시 재고 복구
5. 배송중(SHIPPED) 이후 취소 불가
6. 공급사별로 배송 분리 처리

---

## 주문번호 생성

```java
// 형식: ORD + YYYYMMDD + 랜덤 8자리
// 예: ORD20260124A1B2C3D4
public String generateOrderNumber() {
    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String random = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    return "ORD" + date + random;
}
```

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [create.md](./create.md) | 주문 생성 |
| [cancel.md](./cancel.md) | 주문 취소 |
| [status.md](./status.md) | 상태 관리 |
| [delivery/](./delivery/) | 배송 관리 |
