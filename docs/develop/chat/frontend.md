# 채팅 프론트엔드 UI 지침

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. 파일 구조

```
frontend/src/
├── types/chat.ts                    # 타입 정의
├── api/chat.ts                      # REST API 서비스
├── hooks/
│   ├── useChatRooms.ts              # 채팅방 목록 훅
│   ├── useChatMessages.ts           # 메시지 이력 훅
│   └── useChatWebSocket.ts          # WebSocket 연결/메시지 관리 훅
├── components/chat/
│   ├── ChatRoomItem.tsx             # 채팅방 목록 아이템
│   ├── ChatBubble.tsx               # 메시지 버블 (내 메시지/상대 메시지)
│   ├── ChatImageMessage.tsx         # 이미지 메시지
│   ├── ChatVideoMessage.tsx         # 동영상 메시지
│   ├── BookingInfoCard.tsx          # 예약 정보 카드
│   ├── ChatInput.tsx                # 메시지 입력창 + 전송 버튼
│   ├── ChatMediaPicker.tsx          # 이미지/동영상 선택기
│   ├── ChatDateSeparator.tsx        # 날짜 구분선
│   ├── ChatReadReceipt.tsx          # 읽음 표시
│   └── ChatEmpty.tsx                # 빈 채팅 안내
└── pages/chat/
    ├── ChatListPage.tsx             # 채팅 목록 페이지
    ├── ChatRoomPage.tsx             # 채팅방 페이지
    └── index.ts
```

---

## 2. 라우팅

| 경로 | 컴포넌트 | 설명 | 권한 |
|------|----------|------|------|
| /chat | ChatListPage | 채팅방 목록 | CUSTOMER, PARTNER |
| /chat/:roomId | ChatRoomPage | 채팅방 | CUSTOMER, PARTNER |

---

## 3. 채팅 목록 (ChatListPage)

### 3.1 레이아웃

```
┌─────────────────────────────────────┐
│ 채팅                                │
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐│
│ │ [프로필] 해피독시터        15:30 ││
│ │         네 알겠습니다! 콩...  3 ││
│ └─────────────────────────────────┘│
│                                     │
│ ┌─────────────────────────────────┐│
│ │ [프로필] 러브펫시터        어제  ││
│ │         사진을 보냈습니다.      ││
│ └─────────────────────────────────┘│
│                                     │
│ ┌─────────────────────────────────┐│
│ │ [프로필] 댕댕시터          02/01 ││
│ │         돌봄이 완료되었습니다.  ││
│ └─────────────────────────────────┘│
│                                     │
└─────────────────────────────────────┘
```

### 3.2 ChatRoomItem 정보

| 항목 | 설명 |
|------|------|
| 프로필 이미지 | 상대방 프로필 (48x48 원형) |
| 닉네임 | 상대방 닉네임 |
| 마지막 메시지 | 미리보기 (1줄, 말줄임) |
| 시간 | 상대 시간 표기 (오늘: 시:분, 어제: "어제", 그 외: MM/DD) |
| 안읽은 수 | 뱃지 (빨간색 원형) |

### 3.3 마지막 메시지 표시 규칙

| 메시지 유형 | 표시 |
|-------------|------|
| TEXT | 메시지 내용 (최대 30자 + "...") |
| IMAGE | "사진을 보냈습니다." |
| VIDEO | "동영상을 보냈습니다." |
| BOOKING_INFO | 예약 상태 메시지 (예: "돌봄이 시작되었습니다") |

### 3.4 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | ChatRoomItem 스켈레톤 (3~5개) |
| 빈 목록 | "아직 채팅이 없습니다" + 안내 텍스트 |
| 에러 | "채팅 목록을 불러올 수 없습니다" + 다시 시도 버튼 |

---

## 4. 채팅방 (ChatRoomPage)

### 4.1 레이아웃

```
┌─────────────────────────────────────┐
│ < 해피독시터               [메뉴]   │
├─────────────────────────────────────┤
│                                     │
│      ── 2026년 2월 7일 ──          │
│                                     │
│ ┌──────────────────────────────┐   │
│ │ [시스템] 예약이 확정되었습니다 │   │
│ │ ┌─────────────────────────┐  │   │
│ │ │ 데이케어 | 2/7          │  │   │
│ │ │ 콩이 (말티즈)           │  │   │
│ │ │         [예약 상세 보기] │  │   │
│ │ └─────────────────────────┘  │   │
│ └──────────────────────────────┘   │
│                                     │
│   ┌──────────────────────┐         │
│   │ 안녕하세요!           │  10:00  │
│   │ 콩이 잘 부탁드려요.   │  ✓✓    │
│   └──────────────────────┘         │
│                                     │
│ [프로필]                            │
│ ┌────────────────────────┐         │
│ │ 네 걱정 마세요~        │  10:01  │
│ │ 잘 돌볼게요!           │         │
│ └────────────────────────┘         │
│                                     │
│ [프로필]                            │
│ ┌─────────────────────┐            │
│ │  [이미지 썸네일]      │  14:00   │
│ │  콩이 간식 먹는 중!   │          │
│ └─────────────────────┘            │
│                                     │
├─────────────────────────────────────┤
│ [+] [메시지 입력...        ] [전송] │
└─────────────────────────────────────┘
```

### 4.2 메시지 버블 (ChatBubble)

| 항목 | 내 메시지 | 상대 메시지 |
|------|-----------|-------------|
| 위치 | 오른쪽 정렬 | 왼쪽 정렬 |
| 배경색 | Primary (#76BCA2) | #F0F0F0 |
| 텍스트 색 | 흰색 (#FFFFFF) | 검정 (#000000) |
| 프로필 | 표시 안함 | 프로필 이미지 (36x36) |
| 시간 | 버블 왼쪽 아래 | 버블 오른쪽 아래 |
| 읽음 표시 | 체크 아이콘 (읽음: 이중체크) | 표시 안함 |

### 4.3 이미지 메시지 (ChatImageMessage)

| 항목 | 설명 |
|------|------|
| 썸네일 | 최대 240x240, border-radius 12px |
| 탭 동작 | 풀스크린 이미지 뷰어 |
| 캡션 | 이미지 아래 텍스트 (있는 경우) |

### 4.4 동영상 메시지 (ChatVideoMessage)

| 항목 | 설명 |
|------|------|
| 썸네일 | 최대 240x180, 중앙에 재생 아이콘 |
| 탭 동작 | 동영상 플레이어 |
| 캡션 | 동영상 아래 텍스트 (있는 경우) |

### 4.5 예약 정보 카드 (BookingInfoCard)

```
┌─────────────────────────────────────┐
│ [아이콘] 예약이 확정되었습니다        │
│ ┌─────────────────────────────────┐│
│ │ 데이케어 | 2026-02-07           ││
│ │ 콩이 (말티즈)                   ││
│ │                   [예약 상세] → ││
│ └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

### 4.6 메시지 입력창 (ChatInput)

| 항목 | 설명 |
|------|------|
| [+] 버튼 | 미디어 선택기 열기 (이미지/동영상) |
| 텍스트 입력 | 멀티라인 (최대 5줄), placeholder: "메시지 입력..." |
| 전송 버튼 | 텍스트 입력 시 활성화 (Primary 색상) |

### 4.7 날짜 구분선 (ChatDateSeparator)

- 메시지 날짜가 바뀔 때 표시
- 형식: "2026년 2월 7일" 또는 "오늘", "어제"

### 4.8 스크롤 동작

| 동작 | 설명 |
|------|------|
| 초기 진입 | 최하단으로 스크롤 (최신 메시지) |
| 새 메시지 수신 | 하단에 있으면 자동 스크롤, 상단이면 "새 메시지" 배너 |
| 과거 메시지 로드 | 상단 스크롤 시 이전 메시지 로드 (무한 스크롤) |

### 4.9 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | 메시지 스켈레톤 |
| 빈 채팅방 | "대화를 시작해보세요!" |
| WebSocket 연결 중 | 상단 "연결 중..." 배너 |
| WebSocket 끊김 | 상단 "연결이 끊어졌습니다. 재연결 중..." 배너 |
| 전송 실패 | 메시지 옆 "!" 아이콘 + 재전송 버튼 |

---

## 5. 타입 정의 (types/chat.ts)

```typescript
type SenderRole = 'CUSTOMER' | 'PARTNER' | 'SYSTEM';

type ChatMessageType = 'TEXT' | 'IMAGE' | 'VIDEO' | 'BOOKING_INFO';

interface ChatRoom {
  id: number;
  bookingId: number;
  otherUser: ChatUser;
  lastMessage: ChatLastMessage | null;
  unreadCount: number;
  createdAt: string;
}

interface ChatUser {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
  role: SenderRole;
}

interface ChatLastMessage {
  content: string | null;
  messageType: ChatMessageType;
  createdAt: string;
}

interface ChatMessage {
  id: number;
  chatRoomId: number;
  senderId: number;
  senderRole: SenderRole;
  senderNickname: string;
  senderProfileImageUrl: string | null;
  messageType: ChatMessageType;
  content: string | null;
  mediaUrl: string | null;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
}

interface SendMessageRequest {
  messageType: ChatMessageType;
  content: string;
}

interface BookingInfoContent {
  bookingId: number;
  serviceType: string;
  serviceTypeName: string;
  date: string;
  status: string;
  statusName: string;
  petName: string;
}

interface ReadReceiptEvent {
  type: 'READ_RECEIPT';
  userId: number;
  lastReadAt: string;
}

interface ChatReadResponse {
  chatRoomId: number;
  lastReadAt: string;
  readCount: number;
}
```

---

## 6. API 서비스 (api/chat.ts)

```typescript
const chatApi = {
  getRooms: (params?: { page?: number; size?: number }) =>
    client.get<PageResponse<ChatRoom>>('/api/v1/chat/rooms', { params }),

  getMessages: (roomId: number, params?: { page?: number; size?: number; before?: string }) =>
    client.get<PageResponse<ChatMessage>>(`/api/v1/chat/rooms/${roomId}/messages`, { params }),

  sendMessage: (roomId: number, data: SendMessageRequest) =>
    client.post<ChatMessage>(`/api/v1/chat/rooms/${roomId}/messages`, data),

  uploadMedia: (roomId: number, file: File, caption?: string) => {
    const formData = new FormData();
    formData.append('file', file);
    if (caption) formData.append('caption', caption);
    return client.post<ChatMessage>(
      `/api/v1/chat/rooms/${roomId}/media`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },

  markAsRead: (roomId: number) =>
    client.put<ChatReadResponse>(`/api/v1/chat/rooms/${roomId}/read`),
};
```

---

## 7. WebSocket 연결 관리 훅 (useChatWebSocket)

```typescript
interface UseChatWebSocketOptions {
  chatRoomId: number;
  onMessage: (message: ChatMessage) => void;
  onReadReceipt?: (event: ReadReceiptEvent) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

const {
  connected,      // boolean - 연결 상태
  sendMessage,    // (content: string, type?: ChatMessageType) => void
  disconnect,     // () => void
  reconnect,      // () => void
} = useChatWebSocket({
  chatRoomId: 1,
  onMessage: (msg) => { /* 새 메시지 처리 */ },
  onReadReceipt: (evt) => { /* 읽음 확인 처리 */ },
  onConnect: () => { /* 연결 성공 */ },
  onDisconnect: () => { /* 연결 끊김 */ },
});
```

### 연결 관리 로직

```typescript
// useChatWebSocket.ts 내부 로직 (참고용)
//
// 1. 컴포넌트 마운트 시 WebSocket 연결
// 2. JWT 토큰으로 CONNECT
// 3. /topic/chat/{chatRoomId} 구독
// 4. 연결 끊김 시 자동 재연결 (3초 간격, 최대 5회)
// 5. 컴포넌트 언마운트 시 구독 해제 + 연결 종료
// 6. WebSocket 불안정 시 REST fallback으로 메시지 전송
```

---

## 8. 커스텀 훅

```typescript
// useChatRooms.ts - 채팅방 목록
const {
  rooms,          // ChatRoom[]
  loading,        // boolean
  error,          // Error | null
  refetch,        // () => void
  hasMore,        // boolean
  loadMore,       // () => void
} = useChatRooms();

// useChatMessages.ts - 메시지 이력
const {
  messages,       // ChatMessage[]
  loading,        // boolean
  error,          // Error | null
  hasMore,        // boolean
  loadMore,       // () => void (이전 메시지 로드)
  addMessage,     // (msg: ChatMessage) => void (새 메시지 추가)
  markAsRead,     // () => void (읽음 처리)
} = useChatMessages(roomId);
```
