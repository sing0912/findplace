# 돌봄 API 스펙

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. API 목록

### 시터(PARTNER) API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | POST | /api/v1/partner/bookings/{bookingId}/journal | 돌봄 일지 시작 | PARTNER |
| 2 | POST | /api/v1/partner/journals/{journalId}/entries | 일지 항목 추가 | PARTNER |
| 3 | POST | /api/v1/partner/entries/{entryId}/media | 미디어 업로드 | PARTNER |
| 4 | PUT | /api/v1/partner/journals/{journalId}/complete | 돌봄 완료 | PARTNER |
| 5 | POST | /api/v1/partner/bookings/{bookingId}/walk/start | 산책 시작 | PARTNER |
| 6 | POST | /api/v1/partner/walks/{walkId}/gps | GPS 포인트 전송 (배치) | PARTNER |
| 7 | PUT | /api/v1/partner/walks/{walkId}/end | 산책 종료 | PARTNER |

### 반려인(CUSTOMER) API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 8 | GET | /api/v1/bookings/{bookingId}/journal | 돌봄 일지 조회 | CUSTOMER |
| 9 | GET | /api/v1/bookings/{bookingId}/walk | 산책 경로 조회 | CUSTOMER |
| 10 | GET | /api/v1/bookings/{bookingId}/walk/live | 실시간 GPS (SSE) | CUSTOMER |

---

## 2. 돌봄 일지 시작

```
POST /api/v1/partner/bookings/{bookingId}/journal

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  bookingId: 예약 ID (Long)

Response 201:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "partnerId": 10,
    "status": "IN_PROGRESS",
    "startedAt": "2026-02-07T09:00:00Z",
    "entries": [],
    "createdAt": "2026-02-07T09:00:00Z"
  }
}

Error 400:
{ "code": "CARE_BOOKING_NOT_IN_PROGRESS", "message": "진행 중인 예약에만 돌봄 일지를 작성할 수 있습니다." }

Error 409:
{ "code": "CARE_JOURNAL_ALREADY_EXISTS", "message": "해당 예약에 이미 돌봄 일지가 존재합니다." }
```

**검증 조건:**
- 예약 상태가 IN_PROGRESS인지 확인
- 해당 예약에 기존 일지가 없는지 확인
- 요청한 시터가 해당 예약의 담당 시터인지 확인

---

## 3. 일지 항목 추가

```
POST /api/v1/partner/journals/{journalId}/entries

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  journalId: 돌봄 일지 ID (Long)

Request:
{
  "entryType": "MEAL",
  "content": "오전 10시 사료 1컵 급여 완료. 식욕 양호.",
  "recordedAt": "2026-02-07T10:00:00Z"
}

Response 201:
{
  "success": true,
  "data": {
    "id": 1,
    "journalId": 1,
    "entryType": "MEAL",
    "entryTypeName": "식사 기록",
    "content": "오전 10시 사료 1컵 급여 완료. 식욕 양호.",
    "recordedAt": "2026-02-07T10:00:00Z",
    "media": [],
    "createdAt": "2026-02-07T10:00:30Z"
  }
}

Error 400:
{ "code": "CARE_JOURNAL_ALREADY_COMPLETED", "message": "완료된 돌봄 일지에는 항목을 추가할 수 없습니다." }

Error 404:
{ "code": "CARE_JOURNAL_NOT_FOUND", "message": "돌봄 일지를 찾을 수 없습니다." }
```

**Request 필드:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| entryType | String | Y | 항목 유형 (MEAL, BOWEL, PLAY, SNACK, CONDITION, FREE) |
| content | String | Y | 기록 내용 (최대 1000자) |
| recordedAt | DateTime | N | 기록 시각 (미입력 시 현재 시각, 미래 불가) |

---

## 4. 미디어 업로드

```
POST /api/v1/partner/entries/{entryId}/media

Headers:
  Authorization: Bearer {accessToken}
  Content-Type: multipart/form-data

Path Parameters:
  entryId: 일지 항목 ID (Long)

Request:
  file: (바이너리 파일)

Response 201:
{
  "success": true,
  "data": {
    "id": 1,
    "entryId": 1,
    "mediaType": "IMAGE",
    "mediaUrl": "https://storage.petpro.kr/petpro/care/100/entry_1/abc123.jpg",
    "thumbnailUrl": "https://storage.petpro.kr/petpro/care/100/entry_1/abc123_thumb.jpg",
    "createdAt": "2026-02-07T10:01:00Z"
  }
}

Error 400:
{ "code": "CARE_INVALID_FILE_TYPE", "message": "지원하지 않는 파일 형식입니다. (허용: jpg, jpeg, png, webp, mp4, mov)" }
{ "code": "CARE_FILE_TOO_LARGE", "message": "파일 크기가 제한을 초과했습니다. (이미지: 10MB, 동영상: 100MB)" }
{ "code": "CARE_MEDIA_LIMIT_EXCEEDED", "message": "항목당 미디어는 최대 10개까지 첨부할 수 있습니다." }
```

**파일 제한:**

| 미디어 유형 | 허용 확장자 | 최대 크기 |
|-------------|-------------|-----------|
| IMAGE | jpg, jpeg, png, webp | 10MB |
| VIDEO | mp4, mov | 100MB |

---

## 5. 돌봄 완료

```
PUT /api/v1/partner/journals/{journalId}/complete

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  journalId: 돌봄 일지 ID (Long)

Request:
{
  "summary": "하루 종일 건강하게 잘 놀았습니다. 사료 2회 급여, 산책 1회 진행했고 컨디션 매우 좋았습니다."
}

Response 200:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "partnerId": 10,
    "summary": "하루 종일 건강하게 잘 놀았습니다...",
    "status": "COMPLETED",
    "startedAt": "2026-02-07T09:00:00Z",
    "completedAt": "2026-02-07T18:00:00Z",
    "entries": [ ... ],
    "createdAt": "2026-02-07T09:00:00Z",
    "updatedAt": "2026-02-07T18:00:00Z"
  }
}

Error 400:
{ "code": "CARE_JOURNAL_ALREADY_COMPLETED", "message": "이미 완료된 돌봄 일지입니다." }
```

**Request 필드:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| summary | String | Y | 돌봄 완료 요약 코멘트 (최대 2000자) |

**처리:**
- 일지 상태를 COMPLETED로 변경
- completedAt을 현재 시각으로 설정
- 진행 중인 산책이 있으면 자동 종료

---

## 6. 산책 시작

```
POST /api/v1/partner/bookings/{bookingId}/walk/start

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  bookingId: 예약 ID (Long)

Request:
{
  "latitude": 37.5001,
  "longitude": 127.0365
}

Response 201:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "partnerId": 10,
    "status": "TRACKING",
    "startedAt": "2026-02-07T14:00:00Z",
    "distanceMeters": 0,
    "durationMinutes": 0,
    "gpsPoints": [
      {
        "latitude": 37.5001,
        "longitude": 127.0365,
        "recordedAt": "2026-02-07T14:00:00Z"
      }
    ],
    "createdAt": "2026-02-07T14:00:00Z"
  }
}

Error 400:
{ "code": "CARE_BOOKING_NOT_IN_PROGRESS", "message": "진행 중인 예약에만 산책을 시작할 수 있습니다." }

Error 409:
{ "code": "WALK_ALREADY_IN_PROGRESS", "message": "이미 진행 중인 산책이 있습니다." }
```

**Request 필드:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| latitude | Decimal | Y | 시작 위치 위도 |
| longitude | Decimal | Y | 시작 위치 경도 |

---

## 7. GPS 포인트 전송 (배치)

```
POST /api/v1/partner/walks/{walkId}/gps

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  walkId: 산책 트래킹 ID (Long)

Request:
{
  "points": [
    {
      "latitude": 37.5002,
      "longitude": 127.0366,
      "altitude": 15.5,
      "speed": 1.2,
      "recordedAt": "2026-02-07T14:00:05Z"
    },
    {
      "latitude": 37.5003,
      "longitude": 127.0367,
      "altitude": 15.8,
      "speed": 1.3,
      "recordedAt": "2026-02-07T14:00:10Z"
    },
    {
      "latitude": 37.5004,
      "longitude": 127.0368,
      "altitude": 16.0,
      "speed": 1.1,
      "recordedAt": "2026-02-07T14:00:15Z"
    }
  ]
}

Response 200:
{
  "success": true,
  "data": {
    "walkId": 1,
    "savedCount": 3,
    "totalPoints": 4,
    "currentDistanceMeters": 35,
    "currentDurationMinutes": 0
  }
}

Error 400:
{ "code": "WALK_ALREADY_COMPLETED", "message": "이미 종료된 산책입니다." }

Error 404:
{ "code": "WALK_NOT_FOUND", "message": "산책 트래킹을 찾을 수 없습니다." }
```

**Request 필드 (points 배열 내):**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| latitude | Decimal | Y | 위도 |
| longitude | Decimal | Y | 경도 |
| altitude | Decimal | N | 고도 (미터) |
| speed | Decimal | N | 속도 (m/s) |
| recordedAt | DateTime | Y | 기록 시각 |

**배치 전송 규칙:**
- 5초 간격으로 GPS 포인트 수집
- 10~20개씩 묶어서 전송
- 전송 실패 시 로컬 저장 후 재전송

---

## 8. 산책 종료

```
PUT /api/v1/partner/walks/{walkId}/end

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  walkId: 산책 트래킹 ID (Long)

Response 200:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "partnerId": 10,
    "status": "COMPLETED",
    "startedAt": "2026-02-07T14:00:00Z",
    "endedAt": "2026-02-07T14:35:00Z",
    "distanceMeters": 2150,
    "durationMinutes": 35,
    "totalGpsPoints": 420,
    "createdAt": "2026-02-07T14:00:00Z"
  }
}

Error 400:
{ "code": "WALK_ALREADY_COMPLETED", "message": "이미 종료된 산책입니다." }
```

**처리:**
- 상태를 COMPLETED로 변경
- endedAt을 현재 시각으로 설정
- GPS 포인트 기반으로 총 이동 거리(distanceMeters) 계산 (Haversine 공식)
- 시작~종료 시간 차이로 durationMinutes 계산

---

## 9. 돌봄 일지 조회 (반려인)

```
GET /api/v1/bookings/{bookingId}/journal

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  bookingId: 예약 ID (Long)

Response 200:
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 100,
    "partnerId": 10,
    "partnerNickname": "해피독시터",
    "partnerProfileImageUrl": "...",
    "summary": null,
    "status": "IN_PROGRESS",
    "startedAt": "2026-02-07T09:00:00Z",
    "completedAt": null,
    "entries": [
      {
        "id": 1,
        "entryType": "MEAL",
        "entryTypeName": "식사 기록",
        "content": "오전 10시 사료 1컵 급여 완료. 식욕 양호.",
        "recordedAt": "2026-02-07T10:00:00Z",
        "media": [
          {
            "id": 1,
            "mediaType": "IMAGE",
            "mediaUrl": "https://...",
            "thumbnailUrl": "https://..."
          }
        ]
      },
      {
        "id": 2,
        "entryType": "PLAY",
        "entryTypeName": "놀이/활동",
        "content": "공놀이 30분 진행. 매우 즐거워함.",
        "recordedAt": "2026-02-07T11:00:00Z",
        "media": [
          {
            "id": 2,
            "mediaType": "VIDEO",
            "mediaUrl": "https://...",
            "thumbnailUrl": "https://..."
          }
        ]
      },
      {
        "id": 3,
        "entryType": "CONDITION",
        "entryTypeName": "컨디션 체크",
        "content": "활력 좋음, 식욕 정상, 배변 정상",
        "recordedAt": "2026-02-07T14:00:00Z",
        "media": []
      }
    ],
    "createdAt": "2026-02-07T09:00:00Z",
    "updatedAt": "2026-02-07T14:00:30Z"
  }
}

Error 404:
{ "code": "CARE_JOURNAL_NOT_FOUND", "message": "돌봄 일지를 찾을 수 없습니다." }
```

**검증 조건:**
- 요청한 사용자가 해당 예약의 반려인인지 확인
- entries는 recordedAt 기준 오름차순 정렬 (타임라인)

---

## 10. 산책 경로 조회 (반려인)

```
GET /api/v1/bookings/{bookingId}/walk

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  bookingId: 예약 ID (Long)

Query Parameters:
  walkId (선택): 특정 산책 ID (미지정 시 최신 산책)

Response 200:
{
  "success": true,
  "data": {
    "walks": [
      {
        "id": 1,
        "status": "COMPLETED",
        "startedAt": "2026-02-07T10:00:00Z",
        "endedAt": "2026-02-07T10:35:00Z",
        "distanceMeters": 2150,
        "durationMinutes": 35,
        "gpsPoints": [
          {
            "latitude": 37.5001,
            "longitude": 127.0365,
            "speed": 1.2,
            "recordedAt": "2026-02-07T10:00:00Z"
          },
          {
            "latitude": 37.5002,
            "longitude": 127.0366,
            "speed": 1.3,
            "recordedAt": "2026-02-07T10:00:05Z"
          }
        ]
      },
      {
        "id": 2,
        "status": "TRACKING",
        "startedAt": "2026-02-07T15:00:00Z",
        "endedAt": null,
        "distanceMeters": 800,
        "durationMinutes": 12,
        "gpsPoints": [ ... ]
      }
    ]
  }
}
```

---

## 11. 실시간 GPS (SSE)

```
GET /api/v1/bookings/{bookingId}/walk/live

Headers:
  Authorization: Bearer {accessToken}
  Accept: text/event-stream

Path Parameters:
  bookingId: 예약 ID (Long)

Response (SSE Stream):
event: gps-update
data: {
  "walkId": 2,
  "latitude": 37.5010,
  "longitude": 127.0370,
  "speed": 1.5,
  "recordedAt": "2026-02-07T15:12:05Z",
  "currentDistanceMeters": 850,
  "currentDurationMinutes": 12
}

event: walk-ended
data: {
  "walkId": 2,
  "endedAt": "2026-02-07T15:30:00Z",
  "totalDistanceMeters": 2300,
  "totalDurationMinutes": 30
}
```

**SSE 이벤트 유형:**

| 이벤트 | 설명 |
|--------|------|
| gps-update | 새 GPS 포인트 수신 |
| walk-ended | 산책 종료 |
| walk-started | 새 산책 시작 |

**대안: WebSocket**

SSE 대신 WebSocket을 사용할 경우:

```
WebSocket CONNECT: /ws
SUBSCRIBE: /topic/walk/{bookingId}
```

---

## 12. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/partner/bookings/*/journal` | POST | PARTNER |
| `/api/v1/partner/journals/**` | ALL | PARTNER |
| `/api/v1/partner/entries/**` | ALL | PARTNER |
| `/api/v1/partner/bookings/*/walk/**` | ALL | PARTNER |
| `/api/v1/partner/walks/**` | ALL | PARTNER |
| `/api/v1/bookings/*/journal` | GET | CUSTOMER |
| `/api/v1/bookings/*/walk` | GET | CUSTOMER |
| `/api/v1/bookings/*/walk/live` | GET | CUSTOMER |

---

## 13. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 다음 어노테이션 조합을 사용합니다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```
