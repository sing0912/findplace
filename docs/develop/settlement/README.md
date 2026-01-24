# 정산 (Settlement)

## 개요

공급사 판매 대금 정산을 관리하는 도메인입니다.

---

## 엔티티

### Settlement

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| supplierId | Long | 공급사 ID (FK) | Not Null |
| settlementNumber | String | 정산번호 | Unique, Not Null |
| periodStart | Date | 정산 시작일 | Not Null |
| periodEnd | Date | 정산 종료일 | Not Null |
| totalSalesAmount | Decimal | 총 판매금액 | Not Null |
| totalCommission | Decimal | 총 수수료 | Not Null |
| totalDeliveryFee | Decimal | 총 배송비 | Not Null |
| totalRefundAmount | Decimal | 총 환불금액 | Not Null |
| netAmount | Decimal | 정산금액 | Not Null |
| status | Enum | 상태 | Not Null |
| confirmedAt | DateTime | 확정일시 | Nullable |
| paidAt | DateTime | 지급일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

**정산금액 = 총판매금액 - 총수수료 - 총환불금액**

### SettlementItem

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| settlementId | Long | 정산 ID (FK) | Not Null |
| orderId | Long | 주문 ID (FK) | Not Null |
| orderItemId | Long | 주문상품 ID (FK) | Not Null |
| productName | String | 상품명 | Not Null |
| salesAmount | Decimal | 판매금액 | Not Null |
| commission | Decimal | 수수료 | Not Null |
| commissionRate | Decimal | 수수료율 | Not Null |
| netAmount | Decimal | 정산금액 | Not Null |
| deliveredAt | DateTime | 배송완료일시 | Not Null |

### SettlementStatus

| 값 | 설명 |
|----|------|
| PENDING | 정산 대기 |
| CONFIRMED | 확정됨 |
| PAID | 지급 완료 |
| CANCELLED | 취소됨 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/settlements | 목록 조회 | SUPPLIER_ADMIN, ADMIN |
| POST | /api/v1/settlements | 정산 생성 | PLATFORM_ADMIN |
| GET | /api/v1/settlements/{id} | 상세 조회 | 관련자 |
| PUT | /api/v1/settlements/{id} | 수정 | PLATFORM_ADMIN |
| DELETE | /api/v1/settlements/{id} | 삭제 | PLATFORM_ADMIN |
| PUT | /api/v1/settlements/{id}/confirm | 확정 | PLATFORM_ADMIN |
| PUT | /api/v1/settlements/{id}/pay | 지급 완료 | PLATFORM_ADMIN |
| GET | /api/v1/settlements/summary | 정산 요약 | SUPPLIER_ADMIN, ADMIN |
| GET | /api/v1/settlements/{id}/export | 명세서 다운로드 | 관련자 |

---

## 정산 플로우

```
[배송 완료]
     ↓
[구매 확정] (배송완료 후 7일 자동 또는 수동)
     ↓
[정산 대상 포함]
     ↓
[정산 주기 도래]
     ↓
[정산서 자동 생성] → PENDING
     ↓
[플랫폼 검토/확정] → CONFIRMED
     ↓
[공급사 계좌로 송금] → PAID
```

---

## 정산 주기

### WEEKLY (주간)

- 매주 월요일 ~ 일요일 배송완료 건
- 익주 수요일 정산서 생성
- 익주 금요일 지급

### BIWEEKLY (격주)

- 1일 ~ 15일 / 16일 ~ 말일 배송완료 건
- 익월 5일 / 20일 정산서 생성
- 익월 10일 / 25일 지급

### MONTHLY (월간)

- 매월 1일 ~ 말일 배송완료 건
- 익월 5일 정산서 생성
- 익월 15일 지급

---

## 정산 계산

```java
// 주문 아이템별 정산금액
BigDecimal salesAmount = orderItem.getAmount();
BigDecimal commissionRate = supplier.getCommissionRate(); // 예: 10%
BigDecimal commission = salesAmount.multiply(commissionRate).divide(100);
BigDecimal netAmount = salesAmount.subtract(commission);

// 환불 발생 시
if (isRefunded) {
    netAmount = netAmount.negate(); // 마이너스 정산
}
```

---

## 비즈니스 규칙

1. 구매 확정된 주문만 정산 대상
2. 환불 발생 시 해당 금액 차감
3. 최소 정산금액 미만 시 이월
4. 정산서 확정 전 공급사 검토 기간 제공
5. 세금계산서 발행 연동 (선택)

---

## 정산 명세서

### 포함 항목

- 정산 기간
- 공급사 정보
- 주문별 상세 내역
- 수수료 내역
- 환불 내역
- 최종 정산금액

### 출력 형식

- PDF
- Excel

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [calculation.md](./calculation.md) | 정산 계산 |
| [cycle.md](./cycle.md) | 정산 주기 |
| [payout.md](./payout.md) | 지급 처리 |
| [export.md](./export.md) | 명세서 출력 |
