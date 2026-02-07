# 후기 API 스펙

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | /api/v1/bookings/{bookingId}/review | 후기 작성 | CUSTOMER |
| 2 | GET | /api/v1/sitters/{sitterId}/reviews | 시터 후기 목록 | Public |
| 3 | PUT | /api/v1/reviews/{id} | 후기 수정 | CUSTOMER (본인) |
| 4 | DELETE | /api/v1/reviews/{id} | 후기 삭제 | CUSTOMER (본인) |

---

## 2. 후기 작성

```
POST /api/v1/bookings/{bookingId}/review

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  bookingId: 예약 ID (Long)

Request:
{
  "rating": 5,
  "content": "콩이를 정말 잘 돌봐주셨어요! 돌봄 일지도 꼼꼼하게 작성해주시고, 산책도 즐겁게 다녀왔더라고요. 다음에도 꼭 부탁드리고 싶습니다.",
  "tags": ["FRIENDLY", "DETAILED_JOURNAL", "GOOD_WALKING"]
}

Response 201:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "customerId": 5,
    "customerNickname": "김반려",
    "customerProfileImageUrl": "https://...",
    "partnerId": 10,
    "rating": 5,
    "content": "콩이를 정말 잘 돌봐주셨어요! 돌봄 일지도 꼼꼼하게 작성해주시고, 산책도 즐겁게 다녀왔더라고요. 다음에도 꼭 부탁드리고 싶습니다.",
    "tags": ["FRIENDLY", "DETAILED_JOURNAL", "GOOD_WALKING"],
    "tagNames": ["친절해요", "일지가 꼼꼼해요", "산책을 잘해요"],
    "serviceType": "DAY_CARE",
    "serviceTypeName": "데이케어",
    "petName": "콩이",
    "createdAt": "2026-02-07T18:00:00Z",
    "updatedAt": "2026-02-07T18:00:00Z"
  }
}

Error 400:
{ "code": "REVIEW_BOOKING_NOT_COMPLETED", "message": "완료된 예약에만 후기를 작성할 수 있습니다." }
{ "code": "REVIEW_INVALID_RATING", "message": "평점은 1~5 사이의 정수여야 합니다." }
{ "code": "REVIEW_CONTENT_TOO_SHORT", "message": "후기 내용은 최소 10자 이상이어야 합니다." }
{ "code": "REVIEW_TOO_MANY_TAGS", "message": "태그는 최대 5개까지 선택할 수 있습니다." }

Error 409:
{ "code": "REVIEW_ALREADY_EXISTS", "message": "해당 예약에 이미 후기가 작성되었습니다." }
```

**Request 필드:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| rating | Integer | Y | 평점 (1~5) |
| content | String | Y | 후기 내용 (10~1000자) |
| tags | String[] | N | 후기 태그 (0~5개, ReviewTag enum 값) |

**처리:**
- 후기 저장
- `Partner.averageRating` 재계산 및 갱신
- `Partner.reviewCount` 증가

---

## 3. 시터 후기 목록

```
GET /api/v1/sitters/{sitterId}/reviews

Path Parameters:
  sitterId: 시터(Partner) ID (Long)

Query Parameters:
  page: 0 (기본값)
  size: 10 (기본값)
  sort: createdAt,desc (기본값)
  rating: (선택) 특정 평점 필터 (1~5)

Response 200:
{
  "success": true,
  "data": {
    "summary": {
      "averageRating": 4.8,
      "totalCount": 45,
      "ratingDistribution": {
        "5": 35,
        "4": 7,
        "3": 2,
        "2": 1,
        "1": 0
      },
      "topTags": [
        { "tag": "FRIENDLY", "tagName": "친절해요", "count": 38 },
        { "tag": "DETAILED_JOURNAL", "tagName": "일지가 꼼꼼해요", "count": 30 },
        { "tag": "CARING", "tagName": "애정이 넘쳐요", "count": 25 },
        { "tag": "GOOD_WALKING", "tagName": "산책을 잘해요", "count": 20 },
        { "tag": "PROFESSIONAL", "tagName": "전문적이에요", "count": 15 }
      ]
    },
    "content": [
      {
        "id": 1,
        "bookingId": 100,
        "customerNickname": "김**",
        "customerProfileImageUrl": "https://...",
        "rating": 5,
        "content": "콩이를 정말 잘 돌봐주셨어요! 돌봄 일지도 꼼꼼하게 작성해주시고...",
        "tags": ["FRIENDLY", "DETAILED_JOURNAL", "GOOD_WALKING"],
        "tagNames": ["친절해요", "일지가 꼼꼼해요", "산책을 잘해요"],
        "serviceType": "DAY_CARE",
        "serviceTypeName": "데이케어",
        "petName": "콩이",
        "createdAt": "2026-02-07T18:00:00Z"
      },
      {
        "id": 2,
        "bookingId": 98,
        "customerNickname": "박**",
        "customerProfileImageUrl": "https://...",
        "rating": 4,
        "content": "전반적으로 만족스러운 돌봄이었습니다. 다만 사진이 좀 더 많았으면...",
        "tags": ["PROFESSIONAL", "RESPONSIVE"],
        "tagNames": ["전문적이에요", "응답이 빨라요"],
        "serviceType": "BOARDING",
        "serviceTypeName": "위탁 돌봄",
        "petName": "두부",
        "createdAt": "2026-02-05T12:00:00Z"
      }
    ],
    "page": {
      "number": 0,
      "size": 10,
      "totalElements": 45,
      "totalPages": 5
    }
  }
}
```

**권한**: Public (비인증 접근 가능)

**반려인 닉네임 마스킹 규칙:**
- 2자: 첫 글자 + "*" (예: "김*")
- 3자 이상: 첫 글자 + "*" 반복 + 마지막 글자 (예: "김**동")

**summary 필드:**
- `averageRating`: 해당 시터의 전체 평균 평점
- `totalCount`: 전체 후기 수
- `ratingDistribution`: 평점별 후기 수 분포
- `topTags`: 가장 많이 선택된 태그 상위 5개

---

## 4. 후기 수정

```
PUT /api/v1/reviews/{id}

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  id: 후기 ID (Long)

Request:
{
  "rating": 4,
  "content": "수정된 후기 내용입니다. 전반적으로 만족스러웠으나 한 가지 아쉬운 점이 있었습니다.",
  "tags": ["FRIENDLY", "PROFESSIONAL"]
}

Response 200:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "customerId": 5,
    "customerNickname": "김반려",
    "customerProfileImageUrl": "https://...",
    "partnerId": 10,
    "rating": 4,
    "content": "수정된 후기 내용입니다...",
    "tags": ["FRIENDLY", "PROFESSIONAL"],
    "tagNames": ["친절해요", "전문적이에요"],
    "serviceType": "DAY_CARE",
    "serviceTypeName": "데이케어",
    "petName": "콩이",
    "createdAt": "2026-02-07T18:00:00Z",
    "updatedAt": "2026-02-08T10:00:00Z"
  }
}

Error 400:
{ "code": "REVIEW_EDIT_PERIOD_EXPIRED", "message": "후기 수정은 작성 후 7일 이내에만 가능합니다." }
{ "code": "REVIEW_INVALID_RATING", "message": "평점은 1~5 사이의 정수여야 합니다." }
{ "code": "REVIEW_CONTENT_TOO_SHORT", "message": "후기 내용은 최소 10자 이상이어야 합니다." }

Error 403:
{ "code": "REVIEW_NOT_OWNER", "message": "본인이 작성한 후기만 수정할 수 있습니다." }

Error 404:
{ "code": "REVIEW_NOT_FOUND", "message": "후기를 찾을 수 없습니다." }
```

**처리:**
- 후기 내용 수정
- 평점 변경 시 `Partner.averageRating` 재계산

---

## 5. 후기 삭제

```
DELETE /api/v1/reviews/{id}

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  id: 후기 ID (Long)

Response 204:
(응답 본문 없음)

Error 400:
{ "code": "REVIEW_EDIT_PERIOD_EXPIRED", "message": "후기 삭제는 작성 후 7일 이내에만 가능합니다." }

Error 403:
{ "code": "REVIEW_NOT_OWNER", "message": "본인이 작성한 후기만 삭제할 수 있습니다." }

Error 404:
{ "code": "REVIEW_NOT_FOUND", "message": "후기를 찾을 수 없습니다." }
```

**처리:**
- 후기 삭제 (물리 삭제)
- `Partner.averageRating` 재계산
- `Partner.reviewCount` 감소

---

## 6. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/bookings/*/review` | POST | CUSTOMER |
| `/api/v1/sitters/*/reviews` | GET | Public |
| `/api/v1/reviews/*` | PUT | CUSTOMER (본인) |
| `/api/v1/reviews/*` | DELETE | CUSTOMER (본인) |

---

## 7. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 다음 어노테이션 조합을 사용합니다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```
