# 배송 (Delivery)

## 개요

주문 상품의 배송을 관리하는 도메인입니다.
배송 유형별로 다른 처리 로직을 적용합니다.

---

## 엔티티

### Delivery

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| orderId | Long | 주문 ID (FK) | Not Null |
| supplierId | Long | 공급사 ID (FK) | Not Null |
| deliveryNumber | String | 배송번호 | Unique, Not Null |
| deliveryType | Enum | 배송 유형 | Not Null |
| trackingNumber | String | 운송장 번호 | Nullable |
| carrier | String | 택배사 | Nullable |
| status | Enum | 배송 상태 | Not Null |
| shippedAt | DateTime | 발송일시 | Nullable |
| deliveredAt | DateTime | 배송완료일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### DeliveryItem

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| deliveryId | Long | 배송 ID (FK) | Not Null |
| orderItemId | Long | 주문상품 ID (FK) | Not Null |
| quantity | Integer | 수량 | Not Null |

### DeliveryType

| 값 | 설명 | 처리 주체 |
|----|------|-----------|
| DIRECT | 공급사 직배송 | 공급사 |
| PURCHASE | 사입배송 | 플랫폼 |
| WAREHOUSE | 물류창고 배송 | 3PL |

### DeliveryStatus

| 값 | 설명 |
|----|------|
| PENDING | 배송 대기 |
| PICKED_UP | 집하 완료 |
| IN_TRANSIT | 배송중 |
| OUT_FOR_DELIVERY | 배송 출발 |
| DELIVERED | 배송 완료 |
| FAILED | 배송 실패 |

### Carrier (택배사)

| 코드 | 이름 |
|------|------|
| CJLOGISTICS | CJ대한통운 |
| HANJIN | 한진택배 |
| LOTTE | 롯데택배 |
| LOGEN | 로젠택배 |
| POST | 우체국택배 |
| ETC | 기타 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/deliveries | 목록 조회 | SUPPLIER_ADMIN, ADMIN |
| POST | /api/v1/deliveries | 배송 생성 | SUPPLIER_ADMIN |
| GET | /api/v1/deliveries/{id} | 상세 조회 | 관련자 |
| PUT | /api/v1/deliveries/{id} | 수정 | SUPPLIER_ADMIN (본인) |
| DELETE | /api/v1/deliveries/{id} | 삭제 | PLATFORM_ADMIN |
| PUT | /api/v1/deliveries/{id}/status | 상태 변경 | SUPPLIER_ADMIN (본인) |
| GET | /api/v1/deliveries/{id}/tracking | 배송 추적 | 관련자 |
| POST | /api/v1/deliveries/{id}/ship | 발송 처리 | SUPPLIER_ADMIN (본인) |

---

## 배송 유형별 처리

### DIRECT (공급사 직배송)

```
1. 주문 확인 → 공급사에 알림
2. 공급사가 직접 포장/발송
3. 공급사가 운송장 번호 입력
4. 배송 추적 (택배사 API 연동)
5. 배송 완료
```

**책임:**
- 포장: 공급사
- 발송: 공급사
- 배송: 택배사
- 반품: 공급사

### PURCHASE (사입배송)

```
1. 주문 확인
2. 플랫폼 창고에서 출고
3. 플랫폼이 운송장 번호 입력
4. 배송 추적
5. 배송 완료
```

**책임:**
- 포장: 플랫폼
- 발송: 플랫폼
- 배송: 택배사
- 반품: 플랫폼

### WAREHOUSE (물류창고 배송)

```
1. 주문 확인 → 물류사에 출고 요청
2. 물류사가 포장/발송
3. 물류사가 운송장 번호 전송 (API)
4. 배송 추적
5. 배송 완료
```

**책임:**
- 포장: 물류사
- 발송: 물류사
- 배송: 택배사
- 반품: 물류사 → 공급사

---

## 배송 추적

### 택배사 API 연동

```java
public interface CarrierTrackingService {
    TrackingInfo track(String carrier, String trackingNumber);
}

public record TrackingInfo(
    String trackingNumber,
    DeliveryStatus status,
    List<TrackingEvent> events,
    LocalDateTime estimatedDelivery
) {}

public record TrackingEvent(
    LocalDateTime time,
    String location,
    String description,
    String status
) {}
```

### 배송 상태 동기화

- 주기적 폴링 (10분 간격)
- 또는 택배사 웹훅 (지원 시)

---

## 비즈니스 규칙

1. 하나의 주문은 공급사별로 배송 분리
2. 운송장 번호 입력 시 자동으로 SHIPPED 상태 변경
3. 배송 완료 후 7일 이내 구매 확정
4. 구매 확정 시 정산 대상에 포함
5. 배송 실패 시 재배송 또는 환불 처리

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [direct.md](./direct.md) | 공급사 직배송 |
| [purchase.md](./purchase.md) | 사입배송 |
| [warehouse.md](./warehouse.md) | 물류창고 배송 |
| [tracking.md](./tracking.md) | 배송 추적 |
