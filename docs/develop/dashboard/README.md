# 대시보드 (Dashboard)

## 개요

각 역할별 대시보드 및 통계를 제공하는 도메인입니다.

---

## 대시보드 유형

### 1. 플랫폼 관리자 대시보드

- 전체 예약/주문 현황
- 신규 업체/공급사 등록 요청
- 만료 예정 봉안당
- 만료 예정 광고
- 매출 통계

### 2. 장례업체 대시보드

- 오늘의 예약
- 신규 접수
- 이번달 만료 예정 봉안당
- 화장로별 일정
- 매출 통계

### 3. 공급사 대시보드

- 오늘의 신규 주문
- 배송 대기 건수
- 미정산 금액
- 재고 부족 알림
- 매출 통계

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/dashboard/summary | 요약 정보 | 역할별 |
| GET | /api/v1/dashboard/today | 오늘의 할일 | 역할별 |
| GET | /api/v1/dashboard/reservations | 예약 통계 | COMPANY, ADMIN |
| GET | /api/v1/dashboard/orders | 주문 통계 | SUPPLIER, ADMIN |
| GET | /api/v1/dashboard/sales | 매출 통계 | 역할별 |
| GET | /api/v1/dashboard/expiring | 만료 예정 | 역할별 |

---

## API 응답 예시

### 요약 정보 (플랫폼 관리자)

```json
{
  "today": {
    "newReservations": 15,
    "newOrders": 42,
    "newUsers": 8,
    "pendingApprovals": 3
  },
  "thisMonth": {
    "totalReservations": 320,
    "totalOrders": 1250,
    "totalRevenue": 45000000
  },
  "alerts": [
    { "type": "COLUMBARIUM_EXPIRING", "count": 12 },
    { "type": "PENDING_COMPANY", "count": 2 },
    { "type": "LOW_STOCK", "count": 5 }
  ]
}
```

### 오늘의 할일 (장례업체)

```json
{
  "date": "2026-01-24",
  "reservations": [
    {
      "id": 1,
      "time": "09:00",
      "petName": "코코",
      "packageName": "기본 예식",
      "crematoriumName": "1호기"
    }
  ],
  "newInquiries": 3,
  "expiringColumbariums": [
    {
      "id": 1,
      "location": "A구역 1열 3번",
      "petName": "초코",
      "expiryDate": "2026-02-15"
    }
  ]
}
```

### 매출 통계

```json
{
  "period": {
    "start": "2026-01-01",
    "end": "2026-01-24"
  },
  "summary": {
    "totalRevenue": 15000000,
    "totalOrders": 45,
    "averageOrderValue": 333333
  },
  "daily": [
    { "date": "2026-01-24", "revenue": 1200000, "orders": 4 },
    { "date": "2026-01-23", "revenue": 800000, "orders": 3 }
  ],
  "byCategory": [
    { "category": "유골함", "revenue": 5000000, "percentage": 33.3 },
    { "category": "수의", "revenue": 3000000, "percentage": 20.0 }
  ]
}
```

---

## 통계 기간

| 파라미터 | 설명 | 기본값 |
|----------|------|--------|
| period | 기간 유형 | MONTH |
| startDate | 시작일 | - |
| endDate | 종료일 | - |

### Period 값

| 값 | 설명 |
|----|------|
| TODAY | 오늘 |
| WEEK | 최근 7일 |
| MONTH | 최근 30일 |
| QUARTER | 최근 90일 |
| YEAR | 최근 365일 |
| CUSTOM | 직접 지정 |

---

## 비즈니스 규칙

1. 대시보드 데이터는 역할에 따라 필터링
2. 통계는 캐싱하여 성능 최적화
3. 실시간 데이터는 WebSocket으로 push (선택)
4. 민감한 통계는 권한 확인 필수

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [platform.md](./platform.md) | 플랫폼 관리자 대시보드 |
| [company.md](./company.md) | 장례업체 대시보드 |
| [supplier.md](./supplier.md) | 공급사 대시보드 |
| [statistics.md](./statistics.md) | 통계 계산 |
