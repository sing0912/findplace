# 채팅 API 스펙

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. API 목록

### REST API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/chat/rooms | 채팅방 목록 | CUSTOMER, PARTNER |
| 2 | GET | /api/v1/chat/rooms/{roomId}/messages | 메시지 이력 | CUSTOMER, PARTNER |
| 3 | POST | /api/v1/chat/rooms/{roomId}/messages | 메시지 전송 (REST fallback) | CUSTOMER, PARTNER |
| 4 | POST | /api/v1/chat/rooms/{roomId}/media | 미디어 업로드 | CUSTOMER, PARTNER |
| 5 | PUT | /api/v1/chat/rooms/{roomId}/read | 읽음 처리 | CUSTOMER, PARTNER |

### WebSocket API

| # | 명령 | 경로 | 설명 |
|---|------|------|------|
| 6 | CONNECT | /ws | WebSocket 연결 (JWT 인증) |
| 7 | SUBSCRIBE | /topic/chat/{chatRoomId} | 채팅방 메시지 구독 |
| 8 | SEND | /app/chat/{chatRoomId} | 채팅방 메시지 전송 |

---

## 2. 채팅방 목록

```
GET /api/v1/chat/rooms

Headers:
  Authorization: Bearer {accessToken}

Query Parameters:
  page: 0 (기본값)
  size: 20 (기본값)

Response 200:
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "bookingId": 100,
        "otherUser": {
          "id": 10,
          "nickname": "해피독시터",
          "profileImageUrl": "https://...",
          "role": "PARTNER"
        },
        "lastMessage": {
          "content": "네 알겠습니다! 콩이 잘 돌볼게요~",
          "messageType": "TEXT",
          "createdAt": "2026-02-07T15:30:00Z"
        },
        "unreadCount": 3,
        "createdAt": "2026-02-05T10:00:00Z"
      },
      {
        "id": 2,
        "bookingId": 101,
        "otherUser": {
          "id": 20,
          "nickname": "러브펫시터",
          "profileImageUrl": "https://...",
          "role": "PARTNER"
        },
        "lastMessage": {
          "content": null,
          "messageType": "IMAGE",
          "createdAt": "2026-02-06T09:00:00Z"
        },
        "unreadCount": 0,
        "createdAt": "2026-02-04T14:00:00Z"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 5,
      "totalPages": 1
    }
  }
}
```

**정렬**: `lastMessageAt` 내림차순 (최신 메시지가 있는 채팅방이 상위)

**unreadCount 계산**: 상대방이 보낸 메시지 중 `createdAt > lastReadAt`인 메시지 수

---

## 3. 메시지 이력

```
GET /api/v1/chat/rooms/{roomId}/messages

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  roomId: 채팅방 ID (Long)

Query Parameters:
  page: 0 (기본값)
  size: 50 (기본값)
  before: (선택) 특정 시각 이전 메시지 (커서 기반 페이지네이션)

Response 200:
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "chatRoomId": 1,
        "senderId": 5,
        "senderRole": "CUSTOMER",
        "senderNickname": "김반려",
        "senderProfileImageUrl": "https://...",
        "messageType": "TEXT",
        "content": "안녕하세요! 콩이 잘 부탁드립니다.",
        "mediaUrl": null,
        "isRead": true,
        "readAt": "2026-02-07T10:01:00Z",
        "createdAt": "2026-02-07T10:00:00Z"
      },
      {
        "id": 2,
        "chatRoomId": 1,
        "senderId": 10,
        "senderRole": "PARTNER",
        "senderNickname": "해피독시터",
        "senderProfileImageUrl": "https://...",
        "messageType": "TEXT",
        "content": "네 안녕하세요! 걱정 마세요~",
        "mediaUrl": null,
        "isRead": true,
        "readAt": "2026-02-07T10:02:00Z",
        "createdAt": "2026-02-07T10:01:30Z"
      },
      {
        "id": 3,
        "chatRoomId": 1,
        "senderId": 10,
        "senderRole": "PARTNER",
        "senderNickname": "해피독시터",
        "senderProfileImageUrl": "https://...",
        "messageType": "IMAGE",
        "content": "콩이 간식 먹는 중이에요!",
        "mediaUrl": "https://storage.petpro.kr/petpro/chat/1/abc123.jpg",
        "isRead": false,
        "readAt": null,
        "createdAt": "2026-02-07T14:00:00Z"
      },
      {
        "id": 4,
        "chatRoomId": 1,
        "senderId": 0,
        "senderRole": "SYSTEM",
        "senderNickname": "시스템",
        "senderProfileImageUrl": null,
        "messageType": "BOOKING_INFO",
        "content": "{\"bookingId\":100,\"serviceType\":\"DAY_CARE\",\"date\":\"2026-02-07\",\"status\":\"IN_PROGRESS\"}",
        "mediaUrl": null,
        "isRead": true,
        "readAt": null,
        "createdAt": "2026-02-07T09:00:00Z"
      }
    ],
    "page": {
      "number": 0,
      "size": 50,
      "totalElements": 4,
      "totalPages": 1
    }
  }
}
```

**정렬**: `createdAt` 내림차순 (최신 메시지가 먼저, 프론트에서 역순 렌더링)

**입장 시 자동 읽음**: 이 API 호출 시 해당 채팅방의 `lastReadAt` 갱신 (자동 읽음 처리)

---

## 4. 메시지 전송 (REST fallback)

WebSocket 연결이 불안정할 때 REST API로 메시지를 전송합니다.

```
POST /api/v1/chat/rooms/{roomId}/messages

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  roomId: 채팅방 ID (Long)

Request:
{
  "messageType": "TEXT",
  "content": "안녕하세요! 콩이 잘 부탁드립니다."
}

Response 201:
{
  "success": true,
  "data": {
    "id": 5,
    "chatRoomId": 1,
    "senderId": 5,
    "senderRole": "CUSTOMER",
    "messageType": "TEXT",
    "content": "안녕하세요! 콩이 잘 부탁드립니다.",
    "mediaUrl": null,
    "isRead": false,
    "createdAt": "2026-02-07T15:30:00Z"
  }
}

Error 400:
{ "code": "CHAT_MESSAGE_TOO_LONG", "message": "메시지는 2000자 이하여야 합니다." }

Error 403:
{ "code": "CHAT_ROOM_ACCESS_DENIED", "message": "채팅방에 접근할 수 없습니다." }

Error 404:
{ "code": "CHAT_ROOM_NOT_FOUND", "message": "채팅방을 찾을 수 없습니다." }
```

**Request 필드:**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| messageType | String | Y | 메시지 유형 (TEXT) |
| content | String | Y | 메시지 내용 (최대 2000자) |

---

## 5. 미디어 업로드

```
POST /api/v1/chat/rooms/{roomId}/media

Headers:
  Authorization: Bearer {accessToken}
  Content-Type: multipart/form-data

Path Parameters:
  roomId: 채팅방 ID (Long)

Request:
  file: (바이너리 파일)
  caption: "콩이 간식 먹는 중이에요!" (선택)

Response 201:
{
  "success": true,
  "data": {
    "id": 6,
    "chatRoomId": 1,
    "senderId": 10,
    "senderRole": "PARTNER",
    "messageType": "IMAGE",
    "content": "콩이 간식 먹는 중이에요!",
    "mediaUrl": "https://storage.petpro.kr/petpro/chat/1/def456.jpg",
    "isRead": false,
    "createdAt": "2026-02-07T15:35:00Z"
  }
}

Error 400:
{ "code": "CHAT_INVALID_FILE_TYPE", "message": "지원하지 않는 파일 형식입니다. (허용: jpg, jpeg, png, webp, mp4, mov)" }
{ "code": "CHAT_FILE_TOO_LARGE", "message": "파일 크기가 제한을 초과했습니다. (이미지: 10MB, 동영상: 50MB)" }
```

**파일 제한:**

| 미디어 유형 | 허용 확장자 | 최대 크기 |
|-------------|-------------|-----------|
| IMAGE | jpg, jpeg, png, webp | 10MB |
| VIDEO | mp4, mov | 50MB |

---

## 6. 읽음 처리

```
PUT /api/v1/chat/rooms/{roomId}/read

Headers:
  Authorization: Bearer {accessToken}

Path Parameters:
  roomId: 채팅방 ID (Long)

Response 200:
{
  "success": true,
  "data": {
    "chatRoomId": 1,
    "lastReadAt": "2026-02-07T15:40:00Z",
    "readCount": 3
  }
}
```

**처리:**
- `ChatParticipant.lastReadAt`을 현재 시각으로 갱신
- 해당 채팅방에서 상대방이 보낸 미읽음 메시지를 읽음 처리 (isRead = true, readAt 설정)
- WebSocket으로 상대방에게 읽음 확인 이벤트 전송

---

## 7. WebSocket 연결

### CONNECT (연결)

```
CONNECT
Authorization: Bearer {accessToken}

CONNECTED
version: 1.2
heart-beat: 10000,10000
```

### SUBSCRIBE (메시지 구독)

```
SUBSCRIBE
destination: /topic/chat/{chatRoomId}
id: sub-{chatRoomId}
```

### SEND (메시지 전송)

```
SEND
destination: /app/chat/{chatRoomId}
content-type: application/json

{
  "messageType": "TEXT",
  "content": "안녕하세요!"
}
```

### 수신 메시지 형식

```
MESSAGE
destination: /topic/chat/{chatRoomId}
content-type: application/json

{
  "id": 7,
  "chatRoomId": 1,
  "senderId": 5,
  "senderRole": "CUSTOMER",
  "senderNickname": "김반려",
  "senderProfileImageUrl": "https://...",
  "messageType": "TEXT",
  "content": "안녕하세요!",
  "mediaUrl": null,
  "createdAt": "2026-02-07T16:00:00Z"
}
```

### 읽음 확인 이벤트

```
MESSAGE
destination: /topic/chat/{chatRoomId}
content-type: application/json

{
  "type": "READ_RECEIPT",
  "userId": 5,
  "lastReadAt": "2026-02-07T16:00:30Z"
}
```

### Heart-beat 설정

| 항목 | 값 | 설명 |
|------|-----|------|
| 클라이언트 → 서버 | 10초 | 클라이언트가 10초마다 하트비트 전송 |
| 서버 → 클라이언트 | 10초 | 서버가 10초마다 하트비트 전송 |
| 연결 끊김 판단 | 30초 | 3번 하트비트 미수신 시 연결 끊김 |

---

## 8. 시스템 메시지 (BOOKING_INFO)

예약 상태 변경 시 시스템이 자동으로 채팅방에 예약 정보 카드를 전송합니다.

### 전송 시점

| 이벤트 | 메시지 내용 |
|--------|-------------|
| 예약 요청 | "예약이 요청되었습니다" |
| 예약 수락 | "예약이 확정되었습니다" |
| 예약 거절 | "예약이 거절되었습니다" |
| 돌봄 시작 | "돌봄이 시작되었습니다" |
| 돌봄 완료 | "돌봄이 완료되었습니다" |
| 예약 취소 | "예약이 취소되었습니다" |

### content JSON 형식

```json
{
  "bookingId": 100,
  "serviceType": "DAY_CARE",
  "serviceTypeName": "데이케어",
  "date": "2026-02-07",
  "status": "IN_PROGRESS",
  "statusName": "진행 중",
  "petName": "콩이"
}
```

---

## 9. SecurityConfig 규칙

### REST API

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/chat/rooms` | GET | CUSTOMER, PARTNER |
| `/api/v1/chat/rooms/*/messages` | GET, POST | CUSTOMER, PARTNER |
| `/api/v1/chat/rooms/*/media` | POST | CUSTOMER, PARTNER |
| `/api/v1/chat/rooms/*/read` | PUT | CUSTOMER, PARTNER |

### WebSocket

| 경로 | 권한 |
|------|------|
| `/ws` | 인증된 사용자 (JWT) |
| `/topic/chat/*` | 해당 채팅방 참여자만 |
| `/app/chat/*` | 해당 채팅방 참여자만 |

---

## 10. DTO 어노테이션 규칙

모든 `@RequestBody`로 수신하는 DTO 내부 클래스에는 다음 어노테이션 조합을 사용합니다:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
```
