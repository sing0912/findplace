# 후기 (Review)

## 개요

시터 후기 및 평점 관리를 담당하는 도메인입니다.
반려인(CUSTOMER)이 완료된 예약에 대해 시터 후기를 작성하고, 시터의 평균 평점과 후기 수를 관리합니다.
시터 상세 페이지의 후기 탭에서 조회됩니다.

---

## 엔티티

### SitterReview (시터 후기)

예약 1건당 0~1개의 후기를 작성할 수 있습니다 (Booking:SitterReview = 1:0..1).

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK) | Unique, Not Null |
| customerId | Long | 반려인 ID (FK) | Not Null |
| partnerId | Long | 시터 ID (FK) | Not Null |
| rating | Integer | 평점 (1~5) | Not Null |
| content | Text | 후기 내용 | Not Null |
| tags | JSONB | 후기 태그 배열 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ReviewTag (후기 태그)

후기 작성 시 선택할 수 있는 태그 목록입니다. `tags` 필드에 JSON 배열로 저장됩니다.

| 값 | 한글명 | 설명 |
|----|--------|------|
| FRIENDLY | 친절해요 | 소통이 원활하고 친절한 시터 |
| CLEAN | 깨끗해요 | 위생 관리가 잘 되어 있음 |
| RESPONSIVE | 응답이 빨라요 | 메시지 응답이 빠름 |
| PROFESSIONAL | 전문적이에요 | 전문적인 돌봄 서비스 |
| DETAILED_JOURNAL | 일지가 꼼꼼해요 | 돌봄 일지를 상세하게 작성 |
| GOOD_WALKING | 산책을 잘해요 | 산책 서비스가 우수 |
| CARING | 애정이 넘쳐요 | 반려동물에 대한 애정이 느껴짐 |

---

## 엔티티 관계

```
Booking (1) ──── (0..1) SitterReview : 예약당 최대 1개 후기
User(CUSTOMER) (1) ── (N) SitterReview : 반려인이 작성한 후기
Partner (1) ──── (N) SitterReview : 시터가 받은 후기
```

---

## 비즈니스 규칙

### 후기 작성

1. **완료(COMPLETED) 상태의 예약에만** 후기 작성 가능
2. 예약당 **1개의 후기만** 작성 가능 (bookingId UNIQUE)
3. 해당 예약의 **반려인 본인만** 후기 작성 가능
4. 평점은 **1~5 사이 정수**
5. 내용은 **최소 10자, 최대 1000자**
6. 태그는 **0~5개** 선택 가능

### 후기 수정/삭제

1. 본인이 작성한 후기만 수정/삭제 가능
2. 작성 후 **7일 이내**에만 수정/삭제 가능
3. 수정 시 평점, 내용, 태그 모두 변경 가능

### 시터 평점 갱신

후기 작성/수정/삭제 시 해당 시터의 통계를 갱신합니다:
- `Partner.averageRating`: 전체 후기의 평균 평점 (소수점 첫째 자리까지)
- `Partner.reviewCount`: 전체 후기 수

**갱신 로직:**
```
averageRating = ROUND(SUM(rating) / COUNT(*), 1)
reviewCount = COUNT(*)
```

---

## DDL

### sitter_reviews 테이블

```sql
CREATE TABLE sitter_reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT NOT NULL,
    tags JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 인덱스

```sql
CREATE UNIQUE INDEX idx_sitter_reviews_booking_id ON sitter_reviews(booking_id);
CREATE INDEX idx_sitter_reviews_customer_id ON sitter_reviews(customer_id);
CREATE INDEX idx_sitter_reviews_partner_id ON sitter_reviews(partner_id);
CREATE INDEX idx_sitter_reviews_rating ON sitter_reviews(rating);
CREATE INDEX idx_sitter_reviews_created_at ON sitter_reviews(created_at DESC);
CREATE INDEX idx_sitter_reviews_tags ON sitter_reviews USING GIN(tags);
```

---

## 패키지 구조

```
domain/review/
├── entity/
│   ├── SitterReview.java
│   └── ReviewTag.java
├── repository/
│   └── SitterReviewRepository.java
├── service/
│   └── SitterReviewService.java
├── controller/
│   └── ReviewController.java
└── dto/
    ├── ReviewRequest.java
    └── ReviewResponse.java
```

---

## 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| REVIEW_NOT_FOUND | 404 | 후기 없음 |
| REVIEW_ALREADY_EXISTS | 409 | 해당 예약에 이미 후기 존재 |
| REVIEW_BOOKING_NOT_COMPLETED | 400 | 완료되지 않은 예약 |
| REVIEW_NOT_OWNER | 403 | 본인 후기가 아님 |
| REVIEW_EDIT_PERIOD_EXPIRED | 400 | 수정/삭제 가능 기간 (7일) 초과 |
| REVIEW_INVALID_RATING | 400 | 유효하지 않은 평점 (1~5 범위 벗어남) |
| REVIEW_CONTENT_TOO_SHORT | 400 | 후기 내용이 너무 짧음 (최소 10자) |
| REVIEW_TOO_MANY_TAGS | 400 | 태그 수 초과 (최대 5개) |

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 후기 API 상세 스펙 |
| [frontend.md](./frontend.md) | 후기 프론트엔드 UI 지침 |

---

## 관련 도메인

- **Booking**: 완료된 예약에 대해서만 후기 작성 가능
- **Sitter**: 시터 averageRating/reviewCount 갱신
- **Care**: 돌봄 일지 기반의 후기 작성 (DETAILED_JOURNAL, GOOD_WALKING 태그)
- **User**: 반려인(CUSTOMER) 정보
