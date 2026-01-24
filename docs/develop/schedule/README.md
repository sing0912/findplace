# 일정 (Schedule)

## 개요

장례업체의 예약 일정 및 화장로 스케줄을 관리하는 도메인입니다.

---

## 엔티티

### Schedule

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| companyId | Long | 업체 ID (FK) | Not Null |
| crematoriumId | Long | 화장로 ID (FK) | Nullable |
| reservationId | Long | 예약 ID (FK) | Nullable |
| title | String | 제목 | Not Null |
| startDateTime | DateTime | 시작 일시 | Not Null |
| endDateTime | DateTime | 종료 일시 | Not Null |
| scheduleType | Enum | 일정 유형 | Not Null |
| status | Enum | 상태 | Not Null |
| memo | Text | 메모 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ScheduleType

| 값 | 설명 |
|----|------|
| RESERVATION | 예약 |
| MAINTENANCE | 점검 |
| HOLIDAY | 휴무 |
| OTHER | 기타 |

### ScheduleStatus

| 값 | 설명 |
|----|------|
| SCHEDULED | 예정 |
| IN_PROGRESS | 진행중 |
| COMPLETED | 완료 |
| CANCELLED | 취소 |

### Crematorium (화장로)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| companyId | Long | 업체 ID (FK) | Not Null |
| name | String | 화장로 이름 | Not Null |
| capacity | Decimal | 수용 가능 무게 (kg) | Not Null |
| isVip | Boolean | VIP 전용 여부 | Default false |
| status | Enum | 상태 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### CrematoriumStatus

| 값 | 설명 |
|----|------|
| AVAILABLE | 사용 가능 |
| IN_USE | 사용중 |
| MAINTENANCE | 점검중 |
| UNAVAILABLE | 사용 불가 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/schedules | 목록 조회 | COMPANY_ADMIN |
| POST | /api/v1/schedules | 일정 생성 | COMPANY_ADMIN |
| GET | /api/v1/schedules/{id} | 상세 조회 | COMPANY_ADMIN |
| PUT | /api/v1/schedules/{id} | 수정 | COMPANY_ADMIN |
| DELETE | /api/v1/schedules/{id} | 삭제 | COMPANY_ADMIN |
| GET | /api/v1/schedules/calendar | 캘린더 뷰 | COMPANY_ADMIN |
| GET | /api/v1/schedules/available | 예약 가능 슬롯 | Public |

---

## 캘린더 뷰

### 화장로별 스케줄

```
           09:00  10:00  11:00  12:00  13:00  14:00  15:00
화장로 1호 [예약1---------]      [예약2---------------]
화장로 2호        [예약3-----]         [점검-----------]
VIP실                   [예약4---------------------]
```

### API 응답

```json
{
  "date": "2026-01-24",
  "crematoriums": [
    {
      "id": 1,
      "name": "1호기",
      "schedules": [
        {
          "id": 1,
          "startTime": "09:00",
          "endTime": "11:00",
          "type": "RESERVATION",
          "title": "코코 장례"
        }
      ]
    }
  ]
}
```

---

## 예약 가능 시간 조회

### 파라미터

| 파라미터 | 설명 | 필수 |
|----------|------|------|
| companyId | 업체 ID | O |
| date | 날짜 | O |
| weight | 반려동물 무게 | X |
| duration | 예상 소요시간 | X |

### 로직

1. 해당 날짜의 모든 화장로 조회
2. 기존 일정 제외
3. 운영 시간 내 가능 시간대 반환
4. (선택) 무게 제한에 맞는 화장로만 필터

---

## 비즈니스 규칙

1. 일정 중복 불가 (같은 화장로, 같은 시간)
2. 예약 일정은 예약과 연동 (예약 취소 시 일정 취소)
3. 점검 일정 등록 시 해당 시간 예약 불가
4. 화장로 수용량 초과 반려동물 예약 불가
5. VIP실은 VIP 패키지 선택 시에만 예약 가능

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [calendar.md](./calendar.md) | 캘린더 뷰 |
| [crematorium.md](./crematorium.md) | 화장로 관리 |
| [availability.md](./availability.md) | 가용성 조회 |
