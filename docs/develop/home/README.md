# 홈 (Home)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 5 (부가)

---

## 1. 개요

반려인(CUSTOMER)과 펫시터(PARTNER) 각각의 홈 화면을 구성하는 도메인입니다. 사용자의 역할에 따라 서로 다른 홈 화면 데이터를 하나의 API로 묶어서 응답합니다.

### 1.1 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| sitter | 참조 | 추천 시터 리스트 (반려인 홈) |
| booking | 참조 | 진행중 돌봄 배너, 오늘의 일정, 새 예약 요청 |
| review | 참조 | 최근 이용 시터 평점 |
| payout | 참조 | 수익 요약 (시터 홈) |
| notification | 참조 | 공지사항 |
| community | 참조 | 커뮤니티 피드 요약 (반려인 홈) |

---

## 2. 반려인 홈 구성

반려인(CUSTOMER)이 앱에 접속했을 때 보는 메인 화면입니다.

### 2.1 섹션 구성

| 순서 | 섹션 | 설명 | 데이터 소스 |
|------|------|------|-------------|
| 1 | 진행중 돌봄 배너 | 현재 IN_PROGRESS 상태의 활성 예약 | bookings (status=IN_PROGRESS) |
| 2 | 추천 시터 리스트 | 알고리즘 기반 추천 시터 5명 | partners + 추천 알고리즘 |
| 3 | 최근 이용 시터 | 최근 돌봄 완료한 시터 3명 | bookings (status=COMPLETED) |
| 4 | 이벤트/공지 배너 | 활성 이벤트 및 공지사항 | notices/events (isActive=true) |
| 5 | 커뮤니티 피드 요약 | 최신 인기 게시글 3개 | community posts (인기순) |

### 2.2 추천 시터 알고리즘

추천 시터는 다음 가중치 기반으로 점수를 계산하여 상위 5명을 선별합니다.

| 요소 | 가중치 | 설명 |
|------|--------|------|
| 거리 | 40% | 사용자 위치 기준 가까울수록 높은 점수 |
| 평점 | 35% | averageRating이 높을수록 높은 점수 |
| 완료 수 | 25% | completedBookingCount가 많을수록 높은 점수 |

#### 점수 계산 공식

```
score = (distanceScore * 0.4) + (ratingScore * 0.35) + (completionScore * 0.25)

distanceScore = max(0, 100 - (distance_km / max_radius * 100))
ratingScore = (averageRating / 5.0) * 100
completionScore = min(100, (completedBookingCount / 100.0) * 100)
```

#### 필터 조건

- `verificationStatus = APPROVED`
- `isActive = true`
- 사용자 위치 기준 반경 10km 이내
- 최소 1개 이상 활성 서비스 보유

---

## 3. 펫시터 홈 구성

펫시터(PARTNER)가 앱에 접속했을 때 보는 메인 화면입니다.

### 3.1 섹션 구성

| 순서 | 섹션 | 설명 | 데이터 소스 |
|------|------|------|-------------|
| 1 | 오늘의 일정 요약 | 오늘 날짜의 확정/진행중 예약 건수 및 목록 | bookings (날짜=오늘, status IN (CONFIRMED, IN_PROGRESS)) |
| 2 | 새 예약 요청 | REQUESTED 상태의 미응답 예약 건수 | bookings (status=REQUESTED) |
| 3 | 수익 요약 | 이번 달 정산 금액 요약 | payouts (월별 합산) |
| 4 | 공지사항 | 최신 공지사항 3개 | notices (최신순 3개) |

---

## 4. API 명세

### 4.1 반려인 홈 조회

```
GET /api/v1/home/customer
```

**권한**: CUSTOMER

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| latitude | Decimal | N | 사용자 위치 위도 (추천 시터 거리 계산용) |
| longitude | Decimal | N | 사용자 위치 경도 (추천 시터 거리 계산용) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "activeBooking": {
      "bookingId": 1,
      "bookingNumber": "BK-20260207-00001",
      "partnerNickname": "해피독시터",
      "partnerProfileImageUrl": "...",
      "serviceType": "DAY_CARE",
      "serviceTypeName": "데이케어",
      "startDate": "2026-02-07T09:00:00",
      "endDate": "2026-02-07T19:00:00",
      "status": "IN_PROGRESS",
      "petNames": ["콩이", "두부"]
    },
    "recommendedSitters": [
      {
        "id": 1,
        "nickname": "해피독시터",
        "profileImageUrl": "...",
        "address": "서울 강남구 역삼동",
        "distance": 1.2,
        "averageRating": 4.8,
        "reviewCount": 45,
        "completedBookingCount": 120,
        "services": [
          {
            "serviceType": "DAY_CARE",
            "serviceTypeName": "데이케어",
            "basePrice": 35000
          }
        ],
        "acceptablePetTypes": ["DOG", "CAT"]
      }
    ],
    "recentSitters": [
      {
        "id": 2,
        "nickname": "러블리캣시터",
        "profileImageUrl": "...",
        "averageRating": 4.9,
        "lastBookingDate": "2026-02-05",
        "serviceType": "BOARDING",
        "serviceTypeName": "위탁 돌봄"
      }
    ],
    "eventBanners": [
      {
        "id": 1,
        "title": "첫 이용 30% 할인",
        "imageUrl": "...",
        "linkUrl": "/event/1",
        "type": "EVENT"
      },
      {
        "id": 2,
        "title": "서비스 점검 안내",
        "imageUrl": null,
        "linkUrl": "/notice/5",
        "type": "NOTICE"
      }
    ],
    "communityFeeds": [
      {
        "id": 10,
        "title": "소형견 데이케어 후기",
        "category": "TIP",
        "categoryName": "팁",
        "likeCount": 42,
        "commentCount": 12,
        "thumbnailUrl": "...",
        "createdAt": "2026-02-06T15:30:00"
      }
    ]
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**: `activeBooking`은 진행중 돌봄이 없으면 `null`을 반환합니다. 여러 건이 있을 경우 가장 최근 시작된 1건만 표시합니다.

### 4.2 펫시터 홈 조회

```
GET /api/v1/home/partner
```

**권한**: PARTNER

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "todaySchedule": {
      "date": "2026-02-07",
      "totalCount": 3,
      "bookings": [
        {
          "bookingId": 10,
          "bookingNumber": "BK-20260207-00010",
          "customerName": "김**",
          "serviceType": "DAY_CARE",
          "serviceTypeName": "데이케어",
          "startDate": "2026-02-07T09:00:00",
          "endDate": "2026-02-07T19:00:00",
          "status": "CONFIRMED",
          "petNames": ["콩이"],
          "petCount": 1
        }
      ]
    },
    "newRequestCount": 2,
    "revenueSummary": {
      "month": "2026-02",
      "totalAmount": 1250000,
      "completedBookingCount": 15,
      "pendingAmount": 350000
    },
    "notices": [
      {
        "id": 5,
        "title": "2월 정산 일정 안내",
        "createdAt": "2026-02-06T09:00:00"
      },
      {
        "id": 4,
        "title": "시터 가이드라인 업데이트",
        "createdAt": "2026-02-03T14:00:00"
      },
      {
        "id": 3,
        "title": "앱 업데이트 안내 (v2.1)",
        "createdAt": "2026-02-01T10:00:00"
      }
    ]
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

---

## 5. 캐시 전략

홈 화면은 다수의 도메인 데이터를 조합하므로 Redis 캐시를 적용하여 응답 성능을 최적화합니다.

### 5.1 캐시 설정

| 캐시 키 | TTL | 설명 |
|---------|-----|------|
| `home:customer:{userId}` | 5분 | 반려인 홈 전체 응답 |
| `home:partner:{userId}` | 5분 | 펫시터 홈 전체 응답 |
| `home:recommended:{lat}:{lng}` | 10분 | 추천 시터 (위치 기반, 소수점 2자리 그룹핑) |
| `home:community:popular` | 10분 | 커뮤니티 인기 피드 (전체 공통) |
| `home:events:active` | 30분 | 활성 이벤트/공지 배너 (전체 공통) |

### 5.2 캐시 무효화 규칙

| 이벤트 | 무효화 대상 |
|--------|-------------|
| 예약 상태 변경 | `home:customer:{customerId}`, `home:partner:{partnerId}` |
| 새 예약 요청 | `home:partner:{partnerId}` |
| 정산 완료 | `home:partner:{partnerId}` |
| 공지사항 등록/수정 | `home:events:active` |
| 커뮤니티 게시글 작성 | `home:community:popular` |

---

## 6. DDL

홈 도메인은 자체 테이블 없이 다른 도메인의 데이터를 조합합니다.

> **참고**: 추천 시터 알고리즘의 캐시된 점수 테이블이 필요할 경우 추후 추가합니다.

---

## 7. 에러 코드

| 코드 | HTTP | 메시지 |
|------|------|--------|
| HOME_LOCATION_REQUIRED | 400 | 추천 시터 조회를 위해 위치 정보가 필요합니다. |
| HOME_UNAUTHORIZED_ROLE | 403 | 해당 역할로는 이 홈 화면에 접근할 수 없습니다. |

---

## 8. 패키지 구조

```
backend/src/main/java/com/petpro/domain/home/
├── controller/
│   └── HomeController.java
├── service/
│   ├── CustomerHomeService.java
│   ├── PartnerHomeService.java
│   └── SitterRecommendationService.java
└── dto/
    ├── CustomerHomeResponse.java
    ├── PartnerHomeResponse.java
    ├── RecommendedSitterResponse.java
    ├── ActiveBookingResponse.java
    ├── RecentSitterResponse.java
    ├── EventBannerResponse.java
    ├── CommunityFeedResponse.java
    ├── TodayScheduleResponse.java
    ├── RevenueSummaryResponse.java
    └── NoticeResponse.java
```

---

## 9. 비즈니스 규칙

### 9.1 반려인 홈

1. 진행중 돌봄 배너는 `IN_PROGRESS` 상태의 예약이 있을 때만 표시
2. 추천 시터는 사용자 위치가 없으면 평점순으로 대체 (전국 기준)
3. 최근 이용 시터는 최근 30일 이내 완료된 예약 기준, 최대 3명
4. 이벤트/공지 배너는 노출 기간(displayFrom ~ displayTo)이 현재 시각 범위에 포함된 것만 표시
5. 커뮤니티 피드 요약은 최근 7일 이내 게시글 중 좋아요 수 기준 상위 3개

### 9.2 펫시터 홈

1. 오늘의 일정은 오늘 날짜에 해당하는 CONFIRMED, IN_PROGRESS 상태의 예약
2. 새 예약 요청은 REQUESTED 상태의 전체 미응답 예약 건수
3. 수익 요약은 이번 달(1일~오늘) 기준의 정산 합계
4. 공지사항은 전체 공지 중 최신순 3개

### 9.3 SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/home/customer` | GET | CUSTOMER |
| `/api/v1/home/partner` | GET | PARTNER |

---

## 10. 서브 지침

| 파일 | 설명 |
|------|------|
| [frontend.md](./frontend.md) | 홈 화면 프론트엔드 UI 지침 |
