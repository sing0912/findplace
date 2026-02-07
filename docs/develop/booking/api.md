# Booking 도메인 - API 지침

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. API 목록

### 1.1 반려인 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | /api/v1/bookings | 예약 요청 | CUSTOMER |
| 2 | GET | /api/v1/bookings | 내 예약 목록 | CUSTOMER |
| 3 | GET | /api/v1/bookings/{id} | 예약 상세 | CUSTOMER |
| 4 | POST | /api/v1/bookings/{id}/cancel | 예약 취소 | CUSTOMER |

### 1.2 시터 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 5 | GET | /api/v1/partner/bookings | 내 예약관리 목록 | PARTNER |
| 6 | PUT | /api/v1/partner/bookings/{id}/accept | 예약 수락 | PARTNER |
| 7 | PUT | /api/v1/partner/bookings/{id}/reject | 예약 거절 | PARTNER |
| 8 | PUT | /api/v1/partner/bookings/{id}/start | 돌봄 시작 | PARTNER |
| 9 | PUT | /api/v1/partner/bookings/{id}/complete | 돌봄 완료 | PARTNER |

---

## 2. 예약 요청 (반려인)

```
POST /api/v1/bookings

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "partnerId": 5,
  "serviceType": "DAY_CARE",
  "startDate": "2026-02-10T09:00:00",
  "endDate": "2026-02-10T18:00:00",
  "petIds": [1, 3],
  "requestNote": "산책은 오후에 한 번 부탁드립니다."
}

Response 201:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "customerId": 2,
  "partnerId": 5,
  "serviceType": "DAY_CARE",
  "startDate": "2026-02-10T09:00:00",
  "endDate": "2026-02-10T18:00:00",
  "status": "REQUESTED",
  "pets": [
    { "petId": 1, "petName": "초코", "petBreed": "말티즈" },
    { "petId": 3, "petName": "콩이", "petBreed": "포메라니안" }
  ],
  "quote": {
    "basePrice": 40000,
    "additionalPetPrice": 15000,
    "totalAmount": 55000,
    "description": "데이케어 기본 40,000원 + 추가 반려동물 1마리 15,000원"
  },
  "totalAmount": 55000,
  "requestNote": "산책은 오후에 한 번 부탁드립니다.",
  "createdAt": "2026-02-07T14:30:00"
}

Error 400:
{ "code": "INVALID_BOOKING_DATE", "message": "종료일은 시작일 이후여야 합니다." }

Error 409:
{ "code": "SITTER_NOT_AVAILABLE", "message": "시터의 해당 일정이 차단되어 있습니다." }
{ "code": "PET_ALREADY_BOOKED", "message": "해당 반려동물은 동일 시간대에 이미 예약되어 있습니다." }
```

---

## 3. 내 예약 목록 (반려인)

```
GET /api/v1/bookings?status=REQUESTED&page=0&size=10

Headers:
  Authorization: Bearer {accessToken}

Query Parameters:
  - status (선택): REQUESTED, ACCEPTED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED
  - page (선택): 페이지 번호 (기본 0)
  - size (선택): 페이지 크기 (기본 10)

Response 200:
{
  "content": [
    {
      "id": 1,
      "bookingNumber": "BK-20260207-00001",
      "partnerName": "김시터",
      "partnerProfileImageUrl": "https://...",
      "serviceType": "DAY_CARE",
      "startDate": "2026-02-10T09:00:00",
      "endDate": "2026-02-10T18:00:00",
      "status": "REQUESTED",
      "totalAmount": 55000,
      "petNames": ["초코", "콩이"],
      "createdAt": "2026-02-07T14:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0
}
```

---

## 4. 예약 상세 (반려인)

```
GET /api/v1/bookings/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "customerId": 2,
  "customerName": "박반려",
  "partnerId": 5,
  "partnerName": "김시터",
  "partnerProfileImageUrl": "https://...",
  "serviceType": "DAY_CARE",
  "startDate": "2026-02-10T09:00:00",
  "endDate": "2026-02-10T18:00:00",
  "status": "REQUESTED",
  "pets": [
    { "petId": 1, "petName": "초코", "petBreed": "말티즈", "petAge": 3 },
    { "petId": 3, "petName": "콩이", "petBreed": "포메라니안", "petAge": 2 }
  ],
  "quote": {
    "basePrice": 40000,
    "additionalPetPrice": 15000,
    "totalAmount": 55000,
    "description": "데이케어 기본 40,000원 + 추가 반려동물 1마리 15,000원"
  },
  "totalAmount": 55000,
  "requestNote": "산책은 오후에 한 번 부탁드립니다.",
  "statusHistory": [
    {
      "fromStatus": null,
      "toStatus": "REQUESTED",
      "changedBy": "박반려",
      "reason": null,
      "createdAt": "2026-02-07T14:30:00"
    }
  ],
  "createdAt": "2026-02-07T14:30:00",
  "updatedAt": "2026-02-07T14:30:00"
}

Error 404:
{ "code": "BOOKING_NOT_FOUND", "message": "예약을 찾을 수 없습니다." }

Error 403:
{ "code": "UNAUTHORIZED_BOOKING_ACCESS", "message": "해당 예약에 대한 접근 권한이 없습니다." }
```

---

## 5. 예약 취소 (반려인)

```
POST /api/v1/bookings/{id}/cancel

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "reason": "일정이 변경되었습니다."
}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "status": "CANCELLED",
  "cancelledAt": "2026-02-07T16:00:00",
  "refundAmount": 55000,
  "refundRate": 100,
  "message": "예약이 취소되었습니다. 전액 환불됩니다."
}

Error 400:
{ "code": "BOOKING_CANNOT_CANCEL", "message": "취소할 수 없는 예약 상태입니다." }
{ "code": "BOOKING_ALREADY_CANCELLED", "message": "이미 취소된 예약입니다." }
```

---

## 6. 시터 예약관리 목록 (시터)

```
GET /api/v1/partner/bookings?status=REQUESTED&page=0&size=10

Headers:
  Authorization: Bearer {accessToken}

Query Parameters:
  - status (선택): REQUESTED, ACCEPTED, CONFIRMED, IN_PROGRESS, COMPLETED, REJECTED
  - page (선택): 페이지 번호 (기본 0)
  - size (선택): 페이지 크기 (기본 10)

Response 200:
{
  "content": [
    {
      "id": 1,
      "bookingNumber": "BK-20260207-00001",
      "customerName": "박반려",
      "customerProfileImageUrl": "https://...",
      "serviceType": "DAY_CARE",
      "startDate": "2026-02-10T09:00:00",
      "endDate": "2026-02-10T18:00:00",
      "status": "REQUESTED",
      "totalAmount": 55000,
      "petNames": ["초코", "콩이"],
      "petCount": 2,
      "requestNote": "산책은 오후에 한 번 부탁드립니다.",
      "createdAt": "2026-02-07T14:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0
}
```

---

## 7. 예약 수락 (시터)

```
PUT /api/v1/partner/bookings/{id}/accept

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "status": "ACCEPTED",
  "message": "예약을 수락했습니다. 반려인의 결제를 기다립니다."
}

Error 400:
{ "code": "INVALID_BOOKING_STATUS", "message": "유효하지 않은 예약 상태 변경입니다." }
```

---

## 8. 예약 거절 (시터)

```
PUT /api/v1/partner/bookings/{id}/reject

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "reason": "해당 일정에 다른 예약이 있습니다."
}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "status": "REJECTED",
  "reason": "해당 일정에 다른 예약이 있습니다.",
  "message": "예약을 거절했습니다."
}

Error 400:
{ "code": "REJECT_REASON_REQUIRED", "message": "거절 사유를 입력해주세요." }
{ "code": "INVALID_BOOKING_STATUS", "message": "유효하지 않은 예약 상태 변경입니다." }
```

---

## 9. 돌봄 시작 (시터)

```
PUT /api/v1/partner/bookings/{id}/start

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "status": "IN_PROGRESS",
  "startedAt": "2026-02-10T09:00:00",
  "message": "돌봄이 시작되었습니다."
}

Error 400:
{ "code": "INVALID_BOOKING_STATUS", "message": "유효하지 않은 예약 상태 변경입니다." }
{ "code": "PAYMENT_REQUIRED", "message": "결제가 필요합니다." }
```

---

## 10. 돌봄 완료 (시터)

```
PUT /api/v1/partner/bookings/{id}/complete

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "id": 1,
  "bookingNumber": "BK-20260207-00001",
  "status": "COMPLETED",
  "completedAt": "2026-02-10T18:00:00",
  "message": "돌봄이 완료되었습니다."
}

Error 400:
{ "code": "INVALID_BOOKING_STATUS", "message": "유효하지 않은 예약 상태 변경입니다." }
```

---

## 11. SecurityConfig 규칙

```java
// BookingController - 반려인 전용
.requestMatchers("/api/v1/bookings/**").hasRole("CUSTOMER")

// PartnerBookingController - 시터 전용
.requestMatchers("/api/v1/partner/bookings/**").hasRole("PARTNER")
```

### 접근 제어 상세

| Endpoint 패턴 | 허용 역할 | 추가 검증 |
|---------------|----------|-----------|
| POST /api/v1/bookings | CUSTOMER | - |
| GET /api/v1/bookings | CUSTOMER | 본인 예약만 조회 |
| GET /api/v1/bookings/{id} | CUSTOMER | 본인 예약만 조회 |
| POST /api/v1/bookings/{id}/cancel | CUSTOMER | 본인 예약만 취소 |
| GET /api/v1/partner/bookings | PARTNER | 본인에게 요청된 예약만 |
| PUT /api/v1/partner/bookings/{id}/accept | PARTNER | 본인에게 요청된 예약만 |
| PUT /api/v1/partner/bookings/{id}/reject | PARTNER | 본인에게 요청된 예약만 |
| PUT /api/v1/partner/bookings/{id}/start | PARTNER | 본인에게 할당된 예약만 |
| PUT /api/v1/partner/bookings/{id}/complete | PARTNER | 본인에게 할당된 예약만 |

---

## 12. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 Jackson 역직렬화를 위해 다음 어노테이션 조합을 사용한다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```

---

## 13. Request/Response DTO 정의

### 13.1 BookingRequest

```java
public class BookingRequest {

    @Getter @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        @NotNull private Long partnerId;
        @NotNull private ServiceType serviceType;
        @NotNull private LocalDateTime startDate;
        @NotNull private LocalDateTime endDate;
        @NotEmpty private List<Long> petIds;
        @Size(max = 500) private String requestNote;
    }

    @Getter @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Cancel {
        @Size(max = 300) private String reason;
    }

    @Getter @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Reject {
        @NotBlank @Size(max = 300) private String reason;
    }
}
```

### 13.2 BookingResponse

```java
public class BookingResponse {

    @Getter @Builder
    public static class Detail {
        private Long id;
        private String bookingNumber;
        private Long customerId;
        private String customerName;
        private Long partnerId;
        private String partnerName;
        private String partnerProfileImageUrl;
        private ServiceType serviceType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BookingStatus status;
        private List<PetInfo> pets;
        private QuoteInfo quote;
        private BigDecimal totalAmount;
        private String requestNote;
        private List<StatusHistoryInfo> statusHistory;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter @Builder
    public static class ListItem {
        private Long id;
        private String bookingNumber;
        private String partnerName;
        private String partnerProfileImageUrl;
        private ServiceType serviceType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BookingStatus status;
        private BigDecimal totalAmount;
        private List<String> petNames;
        private LocalDateTime createdAt;
    }

    @Getter @Builder
    public static class CancelResult {
        private Long id;
        private String bookingNumber;
        private BookingStatus status;
        private LocalDateTime cancelledAt;
        private BigDecimal refundAmount;
        private Integer refundRate;
        private String message;
    }

    @Getter @Builder
    public static class StatusChangeResult {
        private Long id;
        private String bookingNumber;
        private BookingStatus status;
        private String reason;
        private String message;
    }

    @Getter @Builder
    public static class PetInfo {
        private Long petId;
        private String petName;
        private String petBreed;
        private Integer petAge;
    }

    @Getter @Builder
    public static class QuoteInfo {
        private BigDecimal basePrice;
        private BigDecimal additionalPetPrice;
        private BigDecimal totalAmount;
        private String description;
    }

    @Getter @Builder
    public static class StatusHistoryInfo {
        private BookingStatus fromStatus;
        private BookingStatus toStatus;
        private String changedBy;
        private String reason;
        private LocalDateTime createdAt;
    }
}
```
