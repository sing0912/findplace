# 채팅 (Chat)

## 개요

실시간 채팅을 담당하는 도메인입니다.
반려인(CUSTOMER)과 펫시터(PARTNER) 간의 1:1 채팅을 지원하며, WebSocket(STOMP) 기반으로 실시간 메시지를 전달합니다.
예약 생성 시 자동으로 채팅방이 생성되며, 텍스트/이미지/동영상/예약 정보 카드 전송을 지원합니다.

---

## 엔티티

### ChatRoom (채팅방)

예약 1건당 1개의 채팅방이 자동 생성됩니다 (Booking:ChatRoom = 1:1).

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| bookingId | Long | 예약 ID (FK) | Unique, Not Null |
| customerId | Long | 반려인 ID (FK) | Not Null |
| partnerId | Long | 시터 ID (FK) | Not Null |
| lastMessageContent | String | 마지막 메시지 내용 (미리보기) | Nullable |
| lastMessageAt | DateTime | 마지막 메시지 시각 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### ChatMessage (채팅 메시지)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| chatRoomId | Long | 채팅방 ID (FK) | Not Null |
| senderId | Long | 발신자 ID (FK) | Not Null |
| senderRole | Enum | 발신자 역할 | Not Null |
| messageType | Enum | 메시지 유형 | Not Null |
| content | Text | 메시지 내용 | Nullable (미디어 시) |
| mediaUrl | String | 미디어 URL | Nullable |
| isRead | Boolean | 읽음 여부 | Default false |
| readAt | DateTime | 읽은 시각 | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

#### SenderRole (발신자 역할)

| 값 | 설명 |
|----|------|
| CUSTOMER | 반려인 |
| PARTNER | 펫시터 |

#### MessageType (메시지 유형)

| 값 | 설명 | content | mediaUrl |
|----|------|---------|----------|
| TEXT | 텍스트 메시지 | 메시지 내용 | null |
| IMAGE | 이미지 | 캡션 (선택) | 이미지 URL |
| VIDEO | 동영상 | 캡션 (선택) | 동영상 URL |
| BOOKING_INFO | 예약 정보 카드 | JSON (예약 요약 정보) | null |

### ChatParticipant (채팅 참여자)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| chatRoomId | Long | 채팅방 ID (FK) | Not Null |
| userId | Long | 사용자 ID (FK) | Not Null |
| role | Enum | 역할 (CUSTOMER/PARTNER) | Not Null |
| lastReadAt | DateTime | 마지막 읽은 시각 | Nullable |

---

## 엔티티 관계

```
Booking (1) ──── (1) ChatRoom : 예약당 1개 채팅방
ChatRoom (1) ─── (N) ChatMessage : 채팅방당 N개 메시지
ChatRoom (1) ─── (2) ChatParticipant : 채팅방당 2명 참여자 (반려인 + 시터)
```

---

## WebSocket 설정

### STOMP over WebSocket

| 항목 | 설정 |
|------|------|
| 프로토콜 | STOMP over WebSocket |
| 엔드포인트 | `/ws` |
| 메시지 브로커 | `/topic` (구독), `/app` (발행) |
| 인증 | JWT 토큰 (CONNECT 프레임 헤더) |

### 구독/발행 경로

| 용도 | 경로 | 설명 |
|------|------|------|
| 채팅방 메시지 구독 | `/topic/chat/{chatRoomId}` | 채팅방 메시지 수신 |
| 메시지 발행 | `/app/chat/{chatRoomId}` | 채팅방 메시지 전송 |
| 읽음 확인 | `/app/chat/{chatRoomId}/read` | 읽음 상태 전송 |

### Spring WebSocket Config

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

### JWT 인증 인터셉터

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            // JWT 토큰 검증 후 사용자 정보 설정
        }
        return message;
    }
}
```

---

## 비즈니스 규칙

### 채팅방 생성

1. 예약(Booking) 생성 시 **자동으로 채팅방 생성**
2. 예약 1건당 채팅방 1개 (1:1 관계)
3. 참여자는 반려인(CUSTOMER) + 시터(PARTNER) 2명

### 메시지

1. 텍스트 메시지 최대 **2000자**
2. 이미지: 최대 **10MB**, jpg/jpeg/png/webp
3. 동영상: 최대 **50MB**, mp4/mov
4. 메시지 삭제/수정 기능 없음 (기록 보존)
5. BOOKING_INFO 메시지는 시스템이 자동 전송 (예약 상태 변경 시)

### 읽음 처리

1. 채팅방 입장 시 모든 미읽음 메시지 읽음 처리
2. `lastReadAt` 갱신으로 안읽은 메시지 수 계산
3. 안읽은 메시지 수 = `createdAt > lastReadAt` 카운트

### 미디어 저장

- 저장 경로: `petpro/chat/{chatRoomId}/{uuid}.{ext}`

---

## DDL

### chat_rooms 테이블

```sql
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    partner_id BIGINT NOT NULL REFERENCES partners(id),
    last_message_content VARCHAR(200),
    last_message_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### chat_messages 테이블

```sql
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL REFERENCES chat_rooms(id),
    sender_id BIGINT NOT NULL,
    sender_role VARCHAR(20) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    content TEXT,
    media_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### chat_participants 테이블

```sql
CREATE TABLE chat_participants (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL REFERENCES chat_rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(20) NOT NULL,
    last_read_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(chat_room_id, user_id)
);
```

### 인덱스

```sql
-- chat_rooms
CREATE UNIQUE INDEX idx_chat_rooms_booking_id ON chat_rooms(booking_id);
CREATE INDEX idx_chat_rooms_customer_id ON chat_rooms(customer_id);
CREATE INDEX idx_chat_rooms_partner_id ON chat_rooms(partner_id);
CREATE INDEX idx_chat_rooms_last_message_at ON chat_rooms(last_message_at DESC);

-- chat_messages
CREATE INDEX idx_chat_messages_chat_room_id ON chat_messages(chat_room_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_is_read ON chat_messages(is_read) WHERE is_read = FALSE;

-- chat_participants
CREATE INDEX idx_chat_participants_user_id ON chat_participants(user_id);
CREATE INDEX idx_chat_participants_chat_room_id ON chat_participants(chat_room_id);
```

---

## 패키지 구조

```
domain/chat/
├── entity/
│   ├── ChatRoom.java
│   ├── ChatMessage.java
│   ├── ChatParticipant.java
│   ├── SenderRole.java
│   └── MessageType.java
├── repository/
│   ├── ChatRoomRepository.java
│   ├── ChatMessageRepository.java
│   └── ChatParticipantRepository.java
├── service/
│   ├── ChatRoomService.java
│   ├── ChatMessageService.java
│   └── ChatReadService.java
├── controller/
│   ├── ChatRestController.java
│   └── ChatWebSocketController.java
├── config/
│   ├── WebSocketConfig.java
│   └── WebSocketAuthInterceptor.java
└── dto/
    ├── ChatRoomResponse.java
    ├── ChatMessageRequest.java
    ├── ChatMessageResponse.java
    └── ChatReadRequest.java
```

---

## 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| CHAT_ROOM_NOT_FOUND | 404 | 채팅방 없음 |
| CHAT_ROOM_ACCESS_DENIED | 403 | 채팅방 접근 권한 없음 |
| CHAT_MESSAGE_TOO_LONG | 400 | 메시지 길이 초과 (2000자) |
| CHAT_INVALID_FILE_TYPE | 400 | 지원하지 않는 파일 형식 |
| CHAT_FILE_TOO_LARGE | 400 | 파일 크기 초과 |
| CHAT_WEBSOCKET_AUTH_FAILED | 401 | WebSocket 인증 실패 |

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [api.md](./api.md) | 채팅 REST/WebSocket API 상세 스펙 |
| [frontend.md](./frontend.md) | 채팅 프론트엔드 UI 지침 |

---

## 관련 도메인

- **Booking**: 예약 생성 시 채팅방 자동 생성
- **User**: 반려인 참여자 정보
- **Sitter**: 시터 참여자 정보
- **Notification**: 새 메시지 수신 시 푸시 알림
- **Care**: 돌봄 중 채팅으로 소통
