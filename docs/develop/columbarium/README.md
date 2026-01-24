# 봉안당 (Columbarium)

## 개요

반려동물 유골을 보관하는 봉안당(납골당) 관리 도메인입니다.

---

## 엔티티

### Columbarium

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| companyId | Long | 장례업체 ID (FK) | Not Null |
| name | String | 봉안당 이름 | Not Null |
| totalCapacity | Integer | 총 수용량 | Not Null |
| description | Text | 설명 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ColumbariumSlot

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| columbariumId | Long | 봉안당 ID (FK) | Not Null |
| section | String | 구역 (A, B, C...) | Not Null |
| row | Integer | 열 | Not Null |
| column | Integer | 번 | Not Null |
| status | Enum | 상태 | Not Null |
| pricePerYear | Decimal | 연간 사용료 | Not Null |

### SlotStatus

| 값 | 설명 |
|----|------|
| AVAILABLE | 사용 가능 |
| OCCUPIED | 사용중 |
| RESERVED | 예약됨 |
| MAINTENANCE | 점검중 |

### ColumbariumContract

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| slotId | Long | 슬롯 ID (FK) | Not Null |
| userId | Long | 계약자 ID (FK) | Not Null |
| petId | Long | 반려동물 ID (FK) | Nullable |
| contractNumber | String | 계약번호 | Unique, Not Null |
| startDate | Date | 시작일 | Not Null |
| endDate | Date | 종료일 | Not Null |
| amount | Decimal | 계약 금액 | Not Null |
| status | Enum | 상태 | Not Null |
| renewedAt | DateTime | 갱신일시 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ContractStatus

| 값 | 설명 |
|----|------|
| ACTIVE | 활성 |
| EXPIRED | 만료 |
| RENEWED | 갱신됨 |
| TERMINATED | 해지됨 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/columbariums | 목록 조회 | COMPANY_ADMIN, ADMIN |
| POST | /api/v1/columbariums | 봉안당 생성 | COMPANY_ADMIN |
| GET | /api/v1/columbariums/{id} | 상세 조회 | COMPANY_ADMIN |
| PUT | /api/v1/columbariums/{id} | 수정 | COMPANY_ADMIN |
| DELETE | /api/v1/columbariums/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/columbariums/grid | 그리드 뷰 | COMPANY_ADMIN |
| GET | /api/v1/columbariums/expiring | 만료 예정 목록 | COMPANY_ADMIN |
| POST | /api/v1/columbariums/{id}/renew | 계약 갱신 | 본인 또는 ADMIN |

---

## 그리드 뷰

### 시각적 위치 매핑

```
       1열    2열    3열    4열    5열
     ┌─────┬─────┬─────┬─────┬─────┐
A구역│ 사용 │     │ 만료 │ 사용 │     │
     │ 중  │     │ 예정 │ 중  │     │
     ├─────┼─────┼─────┼─────┼─────┤
B구역│     │ 사용 │     │     │ 사용 │
     │     │ 중  │     │     │ 중  │
     └─────┴─────┴─────┴─────┴─────┘
```

### API 응답

```json
{
  "sections": [
    {
      "name": "A",
      "slots": [
        { "row": 1, "column": 1, "status": "OCCUPIED", "contractEndDate": "2026-12-31" },
        { "row": 1, "column": 2, "status": "AVAILABLE" },
        { "row": 1, "column": 3, "status": "OCCUPIED", "contractEndDate": "2026-02-15" }
      ]
    }
  ]
}
```

---

## 자동 알림

### 만료 예정 알림

```
만료 30일 전 → 보호자 문자/카카오 알림
만료 7일 전 → 보호자 문자/카카오 알림
만료 당일 → 보호자 문자/카카오 알림
```

### 운영자 알림

- 매일 만료 예정 목록 대시보드 표시
- 만료 후 미갱신 시 별도 관리 필요

---

## 비즈니스 규칙

1. 계약은 연 단위 (최소 1년)
2. 만료 전 갱신 시 연장
3. 만료 후 일정 기간(30일) 내 미갱신 시 유골 처리 안내
4. 슬롯 위치는 구역-열-번 형식으로 고유
5. 같은 슬롯에 중복 계약 불가

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [slot.md](./slot.md) | 슬롯 관리 |
| [contract.md](./contract.md) | 계약 관리 |
| [renewal.md](./renewal.md) | 갱신 처리 |
| [grid.md](./grid.md) | 그리드 뷰 |
