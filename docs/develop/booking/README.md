# 예약 (Booking)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. 개요

돌봄 예약을 관리하는 도메인입니다. 반려인(CUSTOMER)이 펫시터(PARTNER)에게 돌봄 서비스를 요청하고, 시터가 수락/거절하며, 돌봄 진행부터 완료까지의 전체 예약 라이프사이클을 관리합니다.

> **마이그레이션 참고**: 기존 `reservation` 도메인은 `booking`으로 대체됩니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| user | N:1 | 반려인(예약 요청자) |
| sitter | N:1 | 펫시터(예약 수행자) |
| pet | N:M | 돌봄 대상 반려동물 (BookingPet 중간 테이블) |
| payment | 1:1 | 예약 결제 |
| care | 1:N | 돌봄 일지 |
| review | 1:0..1 | 돌봄 후기 |
| chat | 1:1 | 예약별 채팅방 |
| availability | 참조 | 시터 일정 차단 확인 |

---

## 2. 엔티티

### 2.1 Booking

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingNumber | String | 예약번호 (BK-YYYYMMDD-XXXXX) | Unique, Not Null |
| customerId | Long | 반려인 ID (FK → users) | Not Null |
| partnerId | Long | 펫시터 ID (FK → users) | Not Null |
| serviceType | Enum | 서비스 유형 | Not Null |
| startDate | LocalDateTime | 돌봄 시작일시 | Not Null |
| endDate | LocalDateTime | 돌봄 종료일시 | Not Null |
| status | Enum | 예약 상태 | Not Null, Default: REQUESTED |
| totalAmount | BigDecimal | 총 결제 금액 | Not Null |
| requestNote | String | 요청사항 | Nullable, Max 500자 |
| createdAt | LocalDateTime | 생성일시 | Not Null |
| updatedAt | LocalDateTime | 수정일시 | Not Null |

### 2.2 BookingStatus (Enum)

```java
public enum BookingStatus {
    REQUESTED,      // 요청됨 (반려인이 예약 요청)
    ACCEPTED,       // 수락됨 (시터가 수락)
    CONFIRMED,      // 확정됨 (결제 완료)
    IN_PROGRESS,    // 진행중 (돌봄 시작)
    COMPLETED,      // 완료됨 (돌봄 종료)
    CANCELLED,      // 취소됨 (반려인 또는 시스템 취소)
    REJECTED        // 거절됨 (시터가 거절)
}
```

#### 상태 전이 다이어그램

```
                    ┌──────────────┐
                    │  REQUESTED   │
                    └──────┬───────┘
                           │
                ┌──────────┼──────────┐
                ▼          │          ▼
         ┌──────────┐     │   ┌──────────┐
         │ ACCEPTED │     │   │ REJECTED │
         └────┬─────┘     │   └──────────┘
              │           │
              ▼           │
         ┌──────────┐    │
         │CONFIRMED │    │
         └────┬─────┘    │
              │           │
              ▼           ▼
        ┌───────────┐  ┌──────────┐
        │IN_PROGRESS│  │CANCELLED │
        └─────┬─────┘  └──────────┘
              │
              ▼
        ┌──────────┐
        │COMPLETED │
        └──────────┘
```

#### 상태 전이 규칙

| 현재 상태 | 가능한 전이 | 트리거 |
|-----------|------------|--------|
| REQUESTED | ACCEPTED | 시터가 수락 |
| REQUESTED | REJECTED | 시터가 거절 |
| REQUESTED | CANCELLED | 반려인이 취소 |
| ACCEPTED | CONFIRMED | 결제 완료 |
| ACCEPTED | CANCELLED | 반려인이 취소 |
| CONFIRMED | IN_PROGRESS | 시터가 돌봄 시작 |
| CONFIRMED | CANCELLED | 반려인이 취소 (취소 수수료 발생 가능) |
| IN_PROGRESS | COMPLETED | 시터가 돌봄 완료 |

### 2.3 ServiceType (Enum)

```java
public enum ServiceType {
    DAY_CARE,       // 데이케어 (일일 돌봄)
    BOARDING,       // 위탁 돌봄 (숙박)
    WALKING,        // 산책
    GROOMING,       // 목욕/미용
    TRAINING        // 훈련
}
```

### 2.4 BookingPet (N:M 중간 테이블)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK → bookings) | Not Null |
| petId | Long | 반려동물 ID (FK → pets) | Not Null |

> **Unique 제약**: (bookingId, petId) 조합은 유일해야 함

### 2.5 BookingQuote (견적)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK → bookings) | Not Null, Unique |
| basePrice | BigDecimal | 기본 가격 | Not Null |
| additionalPetPrice | BigDecimal | 추가 반려동물 가격 | Not Null, Default: 0 |
| totalAmount | BigDecimal | 합계 금액 | Not Null |
| description | String | 견적 설명 | Nullable, Max 300자 |

### 2.6 BookingStatusHistory (상태 변경 이력)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK → bookings) | Not Null |
| fromStatus | Enum | 이전 상태 | Nullable (최초 생성 시) |
| toStatus | Enum | 변경 상태 | Not Null |
| changedBy | Long | 변경자 ID (FK → users) | Not Null |
| reason | String | 변경 사유 | Nullable |
| createdAt | LocalDateTime | 변경일시 | Not Null |

---

## 3. 엔티티 관계도

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│   User   │──1:N──│   Booking    │──N:1──│  Partner  │
│(Customer)│       │              │       │ (Sitter)  │
└──────────┘       └──────┬───────┘       └──────────┘
                          │
           ┌──────────────┼──────────────────┐
           │              │                  │
     ┌─────┴─────┐  ┌────┴─────┐   ┌────────┴────────┐
     │BookingPet │  │Booking   │   │BookingStatus    │
     │(N:M 중간) │  │Quote     │   │History          │
     └─────┬─────┘  └──────────┘   └─────────────────┘
           │
     ┌─────┴─────┐
     │    Pet    │
     └──────────┘

Booking 1:1 → Payment
Booking 1:N → CareJournal
Booking 1:0..1 → Review
Booking 1:1 → ChatRoom
```

---

## 4. 비즈니스 규칙

### 4.1 예약 4단계 프로세스

| 단계 | 화면 | 설명 |
|------|------|------|
| Step 1 | 일정 선택 | 서비스 유형, 시작/종료 날짜 선택 |
| Step 2 | 펫 선택 | 돌봄 대상 반려동물 선택 (1마리 이상) |
| Step 3 | 견적 확인 | 자동 계산된 견적 확인 (기본가 + 추가동물) |
| Step 4 | 요청사항 입력 | 특이사항/요청사항 작성 후 예약 요청 발송 |

### 4.2 예약 번호 생성 규칙

- 형식: `BK-YYYYMMDD-XXXXX` (예: BK-20260207-00001)
- XXXXX: 당일 순번 (5자리, 0-패딩)

### 4.3 취소 정책

| 취소 시점 | 환불율 | 설명 |
|-----------|--------|------|
| REQUESTED 상태 | 100% | 시터 수락 전, 전액 환불 |
| ACCEPTED 상태 | 100% | 결제 전, 전액 환불 |
| CONFIRMED + 시작 3일 전 | 100% | 전액 환불 |
| CONFIRMED + 시작 1~2일 전 | 50% | 50% 환불 |
| CONFIRMED + 당일 | 0% | 환불 불가 |
| IN_PROGRESS | 0% | 진행 중 취소 불가 (분쟁 접수) |

### 4.4 기타 규칙

1. 시터의 차단된 일정에는 예약 요청 불가
2. 시터가 24시간 이내 미응답 시 자동 취소
3. 한 반려동물은 동일 시간대에 중복 예약 불가
4. 예약 수락 후 72시간 이내 결제 미완료 시 자동 취소
5. 돌봄 완료 후 7일 이내 후기 작성 가능

---

## 5. DDL

### 5.1 bookings

```sql
CREATE TABLE bookings (
    id              BIGSERIAL       PRIMARY KEY,
    booking_number  VARCHAR(20)     NOT NULL UNIQUE,
    customer_id     BIGINT          NOT NULL REFERENCES users(id),
    partner_id      BIGINT          NOT NULL REFERENCES users(id),
    service_type    VARCHAR(20)     NOT NULL,
    start_date      TIMESTAMP       NOT NULL,
    end_date        TIMESTAMP       NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED',
    total_amount    DECIMAL(12,2)   NOT NULL,
    request_note    VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 5.2 booking_pets

```sql
CREATE TABLE booking_pets (
    id          BIGSERIAL   PRIMARY KEY,
    booking_id  BIGINT      NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    pet_id      BIGINT      NOT NULL REFERENCES pets(id),
    UNIQUE (booking_id, pet_id)
);
```

### 5.3 booking_quotes

```sql
CREATE TABLE booking_quotes (
    id                      BIGSERIAL       PRIMARY KEY,
    booking_id              BIGINT          NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    base_price              DECIMAL(12,2)   NOT NULL,
    additional_pet_price    DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_amount            DECIMAL(12,2)   NOT NULL,
    description             VARCHAR(300)
);
```

### 5.4 booking_status_histories

```sql
CREATE TABLE booking_status_histories (
    id          BIGSERIAL       PRIMARY KEY,
    booking_id  BIGINT          NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status   VARCHAR(20)     NOT NULL,
    changed_by  BIGINT          NOT NULL REFERENCES users(id),
    reason      VARCHAR(300),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. 인덱스

```sql
-- bookings
CREATE INDEX idx_bookings_customer_id ON bookings(customer_id);
CREATE INDEX idx_bookings_partner_id ON bookings(partner_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_start_date ON bookings(start_date);
CREATE INDEX idx_bookings_customer_status ON bookings(customer_id, status);
CREATE INDEX idx_bookings_partner_status ON bookings(partner_id, status);

-- booking_pets
CREATE INDEX idx_booking_pets_booking_id ON booking_pets(booking_id);
CREATE INDEX idx_booking_pets_pet_id ON booking_pets(pet_id);

-- booking_status_histories
CREATE INDEX idx_booking_status_histories_booking_id ON booking_status_histories(booking_id);
CREATE INDEX idx_booking_status_histories_created_at ON booking_status_histories(created_at);
```

---

## 7. 패키지 구조

```
backend/src/main/java/com/petpro/domain/booking/
├── entity/
│   ├── Booking.java
│   ├── BookingPet.java
│   ├── BookingQuote.java
│   ├── BookingStatusHistory.java
│   ├── BookingStatus.java
│   └── ServiceType.java
├── repository/
│   ├── BookingRepository.java
│   ├── BookingPetRepository.java
│   ├── BookingQuoteRepository.java
│   └── BookingStatusHistoryRepository.java
├── service/
│   ├── BookingService.java
│   ├── BookingQuoteService.java
│   └── BookingStatusService.java
├── controller/
│   ├── BookingController.java          (반려인용)
│   └── PartnerBookingController.java   (시터용)
└── dto/
    ├── BookingRequest.java
    ├── BookingResponse.java
    ├── BookingListResponse.java
    ├── BookingQuoteResponse.java
    └── BookingStatusHistoryResponse.java
```

---

## 8. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| BOOKING_NOT_FOUND | 404 | 예약을 찾을 수 없습니다. |
| INVALID_BOOKING_STATUS | 400 | 유효하지 않은 예약 상태 변경입니다. |
| BOOKING_ALREADY_CANCELLED | 400 | 이미 취소된 예약입니다. |
| BOOKING_CANNOT_CANCEL | 400 | 취소할 수 없는 예약 상태입니다. |
| BOOKING_DATE_CONFLICT | 409 | 해당 일정에 이미 예약이 존재합니다. |
| SITTER_NOT_AVAILABLE | 409 | 시터의 해당 일정이 차단되어 있습니다. |
| PET_NOT_FOUND | 404 | 반려동물을 찾을 수 없습니다. |
| PET_ALREADY_BOOKED | 409 | 해당 반려동물은 동일 시간대에 이미 예약되어 있습니다. |
| BOOKING_EXPIRED | 400 | 예약 요청이 만료되었습니다. |
| PAYMENT_REQUIRED | 402 | 결제가 필요합니다. |
| REJECT_REASON_REQUIRED | 400 | 거절 사유를 입력해주세요. |
| UNAUTHORIZED_BOOKING_ACCESS | 403 | 해당 예약에 대한 접근 권한이 없습니다. |

---

## 9. 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | **예약 API 지침** |
| [frontend.md](./frontend.md) | **프론트엔드 UI 지침** |
