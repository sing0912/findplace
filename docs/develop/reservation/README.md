# 예약 (Reservation)

## 개요

반려동물 장례 서비스 예약을 관리하는 도메인입니다.

---

## 엔티티

### Reservation

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 예약자 ID (FK) | Not Null |
| companyId | Long | 장례업체 ID (FK) | Not Null |
| petId | Long | 반려동물 ID (FK) | Not Null |
| reservationNumber | String | 예약번호 | Unique, Not Null |
| reservationDate | Date | 예약 날짜 | Not Null |
| reservationTime | Time | 예약 시간 | Not Null |
| crematoriumId | Long | 화장로 ID | Nullable |
| packageId | Long | 예식 패키지 ID | Nullable |
| totalAmount | Decimal | 총 금액 | Not Null |
| status | Enum | 상태 | Not Null |
| memo | Text | 메모 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ReservationStatus

| 값 | 설명 |
|----|------|
| PENDING | 예약 대기 |
| CONFIRMED | 확정됨 |
| PICKED_UP | 픽업 완료 |
| IN_PROGRESS | 진행중 (염습/화장) |
| COMPLETED | 완료 |
| CANCELLED | 취소됨 |

### ReservationOption

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| reservationId | Long | 예약 ID (FK) | Not Null |
| optionId | Long | 옵션 ID (FK) | Not Null |
| optionName | String | 옵션명 (스냅샷) | Not Null |
| price | Decimal | 가격 | Not Null |
| quantity | Integer | 수량 | Not Null |

### ReservationProgress

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| reservationId | Long | 예약 ID (FK) | Not Null |
| step | Enum | 진행 단계 | Not Null |
| description | String | 설명 | Nullable |
| imageUrl | String | 이미지 URL | Nullable |
| recordedAt | DateTime | 기록 시간 | Not Null |

### ProgressStep

| 값 | 설명 |
|----|------|
| PICKUP_COMPLETE | 픽업 완료 |
| PREPARATION_START | 염습 시작 |
| PREPARATION_COMPLETE | 염습 완료 |
| CREMATION_START | 화장 시작 |
| CREMATION_COMPLETE | 화장 완료 |
| REMAINS_READY | 유골 준비 완료 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/reservations | 목록 조회 | 인증된 사용자 |
| POST | /api/v1/reservations | 예약 생성 | 인증된 사용자 |
| GET | /api/v1/reservations/{id} | 상세 조회 | 본인 또는 관련자 |
| PUT | /api/v1/reservations/{id} | 수정 | 본인 (조건부) |
| DELETE | /api/v1/reservations/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/reservations/{id}/status | 진행 상황 | 본인 또는 관련자 |
| PUT | /api/v1/reservations/{id}/status | 상태 변경 | COMPANY_ADMIN |
| PUT | /api/v1/reservations/{id}/cancel | 취소 | 본인 (조건부) |
| POST | /api/v1/reservations/{id}/progress | 진행 기록 추가 | COMPANY_ADMIN |

---

## 예약 플로우

```
[예약 요청] → PENDING
     ↓
[업체 확인] → CONFIRMED
     ↓
[운구차 픽업] → PICKED_UP
     ↓
[염습/화장 진행] → IN_PROGRESS
     ↓
[완료] → COMPLETED
```

### 실시간 알림

```
픽업 완료 → 푸시 알림 + 사진
염습 시작 → 푸시 알림
화장 시작 → 푸시 알림
화장 완료 → 푸시 알림 + 사진
유골 준비 완료 → 푸시 알림
```

---

## 비즈니스 규칙

1. 예약은 업체 영업시간 내에서만 가능
2. 화장로 수용량 초과 시 예약 불가
3. CONFIRMED 이후 취소 시 위약금 발생 가능
4. IN_PROGRESS 이후 취소 불가
5. 진행 상황은 실시간 푸시 알림

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [create.md](./create.md) | 예약 생성 |
| [cancel.md](./cancel.md) | 예약 취소 |
| [progress.md](./progress.md) | 진행 상황 관리 |
| [pricing.md](./pricing.md) | 가격 계산 |
