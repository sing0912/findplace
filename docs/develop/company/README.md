# 장례업체 (Company)

## 개요

반려동물 장례 서비스를 제공하는 업체 관리 도메인입니다.

---

## 엔티티

### Company

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| name | String | 업체명 | Not Null |
| businessNumber | String | 사업자번호 | Unique, Not Null |
| representativeName | String | 대표자명 | Not Null |
| address | String | 주소 | Not Null |
| latitude | Decimal | 위도 | Not Null |
| longitude | Decimal | 경도 | Not Null |
| phone | String | 대표 전화번호 | Not Null |
| email | String | 이메일 | Not Null |
| description | Text | 업체 소개 | Nullable |
| operatingHours | JSON | 영업 시간 | Not Null |
| isObservationAllowed | Boolean | 참관 가능 여부 | Not Null |
| maxWeight | Decimal | 최대 몸무게 (kg) | Nullable |
| crematoriumCount | Integer | 화장로 수 | Default 1 |
| status | Enum | 상태 | Not Null |
| approvedAt | DateTime | 승인일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### CompanyStatus

| 값 | 설명 |
|----|------|
| PENDING | 승인 대기 |
| APPROVED | 승인됨 |
| REJECTED | 거절됨 |
| SUSPENDED | 정지됨 |

### OperatingHours (JSON)

```json
{
  "monday": { "open": "09:00", "close": "18:00", "isOpen": true },
  "tuesday": { "open": "09:00", "close": "18:00", "isOpen": true },
  "wednesday": { "open": "09:00", "close": "18:00", "isOpen": true },
  "thursday": { "open": "09:00", "close": "18:00", "isOpen": true },
  "friday": { "open": "09:00", "close": "18:00", "isOpen": true },
  "saturday": { "open": "10:00", "close": "16:00", "isOpen": true },
  "sunday": { "open": null, "close": null, "isOpen": false },
  "is24Hours": false,
  "holidayNotice": "공휴일 정상 운영"
}
```

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/companies | 목록 조회 | Public |
| POST | /api/v1/companies | 등록 신청 | 인증된 사용자 |
| GET | /api/v1/companies/{id} | 상세 조회 | Public |
| PUT | /api/v1/companies/{id} | 수정 | COMPANY_ADMIN (본인) |
| DELETE | /api/v1/companies/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/companies/nearby | 주변 업체 검색 | Public |
| GET | /api/v1/companies/{id}/products | 업체 상품/서비스 | Public |
| GET | /api/v1/companies/{id}/schedules | 업체 일정 | COMPANY_ADMIN (본인) |
| PUT | /api/v1/companies/{id}/status | 상태 변경 | PLATFORM_ADMIN |

---

## 주변 업체 검색

### 파라미터

| 파라미터 | 설명 | 필수 |
|----------|------|------|
| latitude | 위도 | O |
| longitude | 경도 | O |
| radius | 반경 (km) | X (기본: 10) |
| isObservationAllowed | 참관 가능 | X |
| maxWeight | 최소 몸무게 제한 | X |
| isOpenNow | 현재 영업중 | X |

### 응답

- 거리순 정렬
- 예상 견적 포함 (몸무게 입력 시)

---

## 비즈니스 규칙

1. 업체 등록 시 PENDING 상태로 생성
2. PLATFORM_ADMIN 승인 후 APPROVED 상태로 변경
3. APPROVED 상태에서만 예약 가능
4. 사업자번호는 시스템 전체에서 고유
5. 위치 기반 검색을 위해 좌표 필수

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [registration.md](./registration.md) | 업체 등록 |
| [search.md](./search.md) | 업체 검색 |
| [pricing.md](./pricing.md) | 가격 정책 |
| [schedule.md](./schedule.md) | 일정 관리 |
