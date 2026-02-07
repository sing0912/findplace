# 시터 일정/캘린더 차단 (Availability)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 2 (예약/결제)

---

## 1. 개요

펫시터(PARTNER)의 일정 및 캘린더 차단을 관리하는 도메인입니다. 시터가 특정 날짜 또는 기간을 차단하여 예약 요청을 받지 않도록 설정하며, 반려인이 시터의 가용 일정을 조회할 수 있습니다.

> **마이그레이션 참고**: 기존 `schedule` 도메인은 `availability`로 대체됩니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| user (Partner) | N:1 | 일정 차단 소유자 (시터) |
| booking | 참조 | 예약 확정 일정과 중복 검증 |

---

## 2. 엔티티

### 2.1 CalendarBlock

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| partnerId | Long | 시터 ID (FK → users) | Not Null |
| startDate | LocalDate | 차단 시작일 | Not Null |
| endDate | LocalDate | 차단 종료일 | Not Null |
| reason | String | 차단 사유 | Nullable, Max 200자 |
| createdAt | LocalDateTime | 생성일시 | Not Null |

> **참고**: 단일 날짜 차단 시 startDate = endDate 동일 값

---

## 3. 비즈니스 규칙

### 3.1 날짜 차단 규칙

1. 시터만 본인의 캘린더를 차단/해제할 수 있음
2. 과거 날짜는 차단할 수 없음
3. 이미 예약 확정(CONFIRMED/IN_PROGRESS)된 날짜는 차단 불가
4. 기간 선택 차단 지원 (startDate ~ endDate 범위 차단)
5. 기존 차단과 중복되는 기간은 병합하거나 에러 반환

### 3.2 가용 일정 조회 규칙

1. 반려인은 시터의 가용 일정을 월 단위로 조회 가능
2. 가용하지 않은 날짜 = 차단된 날짜 + 예약 확정된 날짜
3. 비로그인 사용자도 시터 가용 날짜 조회 가능 (Public API)

### 3.3 차단 해제 규칙

1. 차단 ID로 개별 해제
2. 예약 확정된 일정은 이 도메인에서 관리하지 않음 (booking 도메인 담당)

---

## 4. DDL

### 4.1 calendar_blocks

```sql
CREATE TABLE calendar_blocks (
    id          BIGSERIAL       PRIMARY KEY,
    partner_id  BIGINT          NOT NULL REFERENCES users(id),
    start_date  DATE            NOT NULL,
    end_date    DATE            NOT NULL,
    reason      VARCHAR(200),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_date_range CHECK (end_date >= start_date)
);
```

---

## 5. 인덱스

```sql
CREATE INDEX idx_calendar_blocks_partner_id ON calendar_blocks(partner_id);
CREATE INDEX idx_calendar_blocks_date_range ON calendar_blocks(partner_id, start_date, end_date);
CREATE INDEX idx_calendar_blocks_start_date ON calendar_blocks(start_date);
```

---

## 6. 패키지 구조

```
backend/src/main/java/com/petpro/domain/availability/
├── entity/
│   └── CalendarBlock.java
├── repository/
│   └── CalendarBlockRepository.java
├── service/
│   └── CalendarBlockService.java
├── controller/
│   ├── PartnerCalendarController.java   (시터용)
│   └── SitterAvailabilityController.java (반려인/Public용)
└── dto/
    ├── CalendarBlockRequest.java
    ├── CalendarBlockResponse.java
    └── SitterAvailabilityResponse.java
```

---

## 7. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| CALENDAR_BLOCK_NOT_FOUND | 404 | 일정 차단을 찾을 수 없습니다. |
| INVALID_DATE_RANGE | 400 | 종료일은 시작일 이후여야 합니다. |
| PAST_DATE_BLOCK | 400 | 과거 날짜는 차단할 수 없습니다. |
| DATE_HAS_CONFIRMED_BOOKING | 409 | 예약 확정된 날짜는 차단할 수 없습니다. |
| DUPLICATE_BLOCK_DATE | 409 | 이미 차단된 날짜 범위와 겹칩니다. |
| UNAUTHORIZED_CALENDAR_ACCESS | 403 | 해당 캘린더에 대한 접근 권한이 없습니다. |
| SITTER_NOT_FOUND | 404 | 시터를 찾을 수 없습니다. |

---

## 8. 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | **시터 일정 API 지침** |
| [frontend.md](./frontend.md) | **프론트엔드 UI 지침** |
