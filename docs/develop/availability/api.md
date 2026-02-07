# Availability 도메인 - API 지침

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. API 목록

### 1.1 시터 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/partner/calendar | 월간 일정 조회 (차단일 + 예약일) | PARTNER |
| 2 | POST | /api/v1/partner/calendar/block | 날짜 차단 | PARTNER |
| 3 | DELETE | /api/v1/partner/calendar/block/{id} | 차단 해제 | PARTNER |

### 1.2 Public API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 4 | GET | /api/v1/sitters/{id}/availability | 시터 가용 날짜 조회 | 불필요 |

---

## 2. 월간 일정 조회 (시터)

```
GET /api/v1/partner/calendar?year=2026&month=2

Headers:
  Authorization: Bearer {accessToken}

Query Parameters:
  - year (필수): 연도
  - month (필수): 월 (1~12)

Response 200:
{
  "year": 2026,
  "month": 2,
  "blocks": [
    {
      "id": 1,
      "startDate": "2026-02-10",
      "endDate": "2026-02-12",
      "reason": "개인 일정"
    },
    {
      "id": 2,
      "startDate": "2026-02-20",
      "endDate": "2026-02-20",
      "reason": null
    }
  ],
  "bookedDates": [
    {
      "date": "2026-02-15",
      "bookingId": 10,
      "bookingNumber": "BK-20260210-00003",
      "customerName": "박반려",
      "serviceType": "DAY_CARE",
      "status": "CONFIRMED"
    },
    {
      "date": "2026-02-16",
      "bookingId": 10,
      "bookingNumber": "BK-20260210-00003",
      "customerName": "박반려",
      "serviceType": "DAY_CARE",
      "status": "CONFIRMED"
    }
  ]
}
```

---

## 3. 날짜 차단 (시터)

```
POST /api/v1/partner/calendar/block

Headers:
  Authorization: Bearer {accessToken}

Request:
{
  "startDate": "2026-02-10",
  "endDate": "2026-02-12",
  "reason": "개인 일정"
}

Response 201:
{
  "id": 1,
  "partnerId": 5,
  "startDate": "2026-02-10",
  "endDate": "2026-02-12",
  "reason": "개인 일정",
  "createdAt": "2026-02-07T10:00:00"
}

Error 400:
{ "code": "INVALID_DATE_RANGE", "message": "종료일은 시작일 이후여야 합니다." }
{ "code": "PAST_DATE_BLOCK", "message": "과거 날짜는 차단할 수 없습니다." }

Error 409:
{ "code": "DATE_HAS_CONFIRMED_BOOKING", "message": "예약 확정된 날짜는 차단할 수 없습니다." }
{ "code": "DUPLICATE_BLOCK_DATE", "message": "이미 차단된 날짜 범위와 겹칩니다." }
```

---

## 4. 차단 해제 (시터)

```
DELETE /api/v1/partner/calendar/block/{id}

Headers:
  Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "message": "일정 차단이 해제되었습니다."
}

Error 404:
{ "code": "CALENDAR_BLOCK_NOT_FOUND", "message": "일정 차단을 찾을 수 없습니다." }

Error 403:
{ "code": "UNAUTHORIZED_CALENDAR_ACCESS", "message": "해당 캘린더에 대한 접근 권한이 없습니다." }
```

---

## 5. 시터 가용 날짜 조회 (Public)

```
GET /api/v1/sitters/{id}/availability?year=2026&month=2

Query Parameters:
  - year (필수): 연도
  - month (필수): 월 (1~12)

Response 200:
{
  "sitterId": 5,
  "sitterName": "김시터",
  "year": 2026,
  "month": 2,
  "unavailableDates": [
    "2026-02-10",
    "2026-02-11",
    "2026-02-12",
    "2026-02-15",
    "2026-02-16",
    "2026-02-20"
  ],
  "availableDates": [
    "2026-02-07",
    "2026-02-08",
    "2026-02-09",
    "2026-02-13",
    "2026-02-14",
    "2026-02-17",
    "2026-02-18",
    "2026-02-19",
    "2026-02-21",
    "2026-02-22",
    "2026-02-23",
    "2026-02-24",
    "2026-02-25",
    "2026-02-26",
    "2026-02-27",
    "2026-02-28"
  ]
}

Error 404:
{ "code": "SITTER_NOT_FOUND", "message": "시터를 찾을 수 없습니다." }
```

> **참고**: `unavailableDates`는 차단 날짜 + 예약 확정(CONFIRMED/IN_PROGRESS) 날짜를 합산한 결과입니다.

---

## 6. SecurityConfig 규칙

```java
// PartnerCalendarController - 시터 전용
.requestMatchers("/api/v1/partner/calendar/**").hasRole("PARTNER")

// SitterAvailabilityController - Public
.requestMatchers(HttpMethod.GET, "/api/v1/sitters/*/availability").permitAll()
```

### 접근 제어 상세

| Endpoint 패턴 | 허용 역할 | 추가 검증 |
|---------------|----------|-----------|
| GET /api/v1/partner/calendar | PARTNER | 본인 캘린더만 조회 |
| POST /api/v1/partner/calendar/block | PARTNER | 본인 캘린더만 차단 |
| DELETE /api/v1/partner/calendar/block/{id} | PARTNER | 본인 차단만 해제 |
| GET /api/v1/sitters/{id}/availability | 모든 사용자 | 인증 불필요 |

---

## 7. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 Jackson 역직렬화를 위해 다음 어노테이션 조합을 사용한다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```

---

## 8. Request/Response DTO 정의

### 8.1 CalendarBlockRequest

```java
public class CalendarBlockRequest {

    @Getter @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        @NotNull private LocalDate startDate;
        @NotNull private LocalDate endDate;
        @Size(max = 200) private String reason;
    }
}
```

### 8.2 CalendarBlockResponse

```java
public class CalendarBlockResponse {

    @Getter @Builder
    public static class BlockInfo {
        private Long id;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
    }

    @Getter @Builder
    public static class BookedDateInfo {
        private LocalDate date;
        private Long bookingId;
        private String bookingNumber;
        private String customerName;
        private String serviceType;
        private String status;
    }

    @Getter @Builder
    public static class MonthlyCalendar {
        private Integer year;
        private Integer month;
        private List<BlockInfo> blocks;
        private List<BookedDateInfo> bookedDates;
    }
}
```

### 8.3 SitterAvailabilityResponse

```java
public class SitterAvailabilityResponse {

    @Getter @Builder
    public static class Monthly {
        private Long sitterId;
        private String sitterName;
        private Integer year;
        private Integer month;
        private List<LocalDate> unavailableDates;
        private List<LocalDate> availableDates;
    }
}
```
