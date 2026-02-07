# 커뮤니티 프론트엔드 UI 지침

---

## 1. 파일 구조

```
frontend/src/
├── types/community.ts                 # 커뮤니티 타입 정의
├── api/community.ts                   # API 서비스
├── hooks/
│   ├── usePosts.ts                    # 게시글 목록 훅
│   ├── usePostDetail.ts              # 게시글 상세 훅
│   ├── useComments.ts                # 댓글 목록 훅
│   └── usePostLike.ts                # 좋아요 토글 훅
├── components/community/
│   ├── PostCard.tsx                   # 게시글 카드 (리스트 아이템)
│   ├── PostCategoryTabs.tsx          # 카테고리 탭 바
│   ├── PostSortSelect.tsx            # 정렬 선택
│   ├── PostContent.tsx               # 게시글 본문 (이미지 포함)
│   ├── PostActions.tsx               # 좋아요/댓글/신고 액션 바
│   ├── CommentItem.tsx               # 댓글 항목 (대댓글 포함)
│   ├── CommentInput.tsx              # 댓글 입력 폼
│   ├── PostEditor.tsx                # 게시글 작성/수정 에디터
│   ├── ImageUploader.tsx             # 이미지 업로드 컴포넌트
│   └── ReportModal.tsx               # 신고 모달
└── pages/community/
    ├── CommunityMainPage.tsx          # 커뮤니티 메인 (게시글 리스트)
    ├── PostDetailPage.tsx             # 게시글 상세
    └── PostWritePage.tsx              # 게시글 작성/수정
```

---

## 2. 라우팅

| 경로 | 컴포넌트 | 설명 |
|------|----------|------|
| /community | CommunityMainPage | 커뮤니티 메인 (게시글 리스트) |
| /community/posts/:id | PostDetailPage | 게시글 상세 |
| /community/write | PostWritePage | 게시글 작성 |
| /community/edit/:id | PostWritePage | 게시글 수정 (동일 컴포넌트) |

---

## 3. 커뮤니티 메인 페이지 (CommunityMainPage)

### 3.1 레이아웃

```
┌─────────────────────────────────┐
│ ← 뒤로가기      커뮤니티        │
├─────────────────────────────────┤
│ [전체] [팁] [자랑] [Q&A] [자유] │
├─────────────────────────────────┤
│ [최신순 v]              [검색🔍]│
├─────────────────────────────────┤
│                                  │
│ ┌──────────────────────────────┐│
│ │ PostCard                     ││
│ │ [콩이맘]         팁  2시간전 ││
│ │ 소형견 데이케어 이용 꿀팁     ││
│ │ 소형견 데이케어를 처음 이용... ││
│ │ [썸네일]                     ││
│ │ ♡42  💬12  👁234            ││
│ └──────────────────────────────┘│
│                                  │
│ ┌──────────────────────────────┐│
│ │ PostCard                     ││
│ │ [두부맘]       자랑  5시간전  ││
│ │ 우리 두부 첫 산책!            ││
│ │ 오늘 두부가 처음으로 산책을... ││
│ │ [썸네일1] [썸네일2]          ││
│ │ ♡38  💬8  👁156              ││
│ └──────────────────────────────┘│
│                                  │
│ ... (무한 스크롤)                │
│                                  │
│ [✏ 글쓰기] (FAB 버튼)           │
├─────────────────────────────────┤
│ [홈] [검색] [예약] [채팅] [마이] │
└─────────────────────────────────┘
```

### 3.2 카테고리 탭 (PostCategoryTabs)

| 탭 | 값 | 설명 |
|----|----|------|
| 전체 | null | 전체 카테고리 (필터 없음) |
| 팁 | TIP | 돌봄/육아 꿀팁 |
| 자랑 | BOAST | 내 반려동물 자랑 |
| Q&A | QA | 질문/답변 |
| 자유 | FREE | 자유 게시판 |

- 가로 스크롤 가능한 탭 바
- 활성 탭: 하단 밑줄 + 프라이머리 색상
- 탭 전환 시 게시글 목록 갱신

### 3.3 PostCard 정보

| 항목 | 표시 |
|------|------|
| 작성자 | 프로필 이미지(24x24) + 닉네임 |
| 카테고리 | 뱃지 (팁: 파랑, 자랑: 분홍, Q&A: 초록, 자유: 회색) |
| 시간 | 상대 시간 (방금, N분전, N시간전, N일전) |
| 제목 | 볼드, 1줄 (말줄임) |
| 미리보기 | 본문 2줄 (말줄임) |
| 썸네일 | imageUrls의 첫 번째 이미지 (있는 경우만, 60x60) |
| 좋아요 | 하트 아이콘 + 숫자 |
| 댓글 | 말풍선 아이콘 + 숫자 |
| 조회수 | 눈 아이콘 + 숫자 |

### 3.4 글쓰기 버튼

- FAB (Floating Action Button) 형태
- 우측 하단 고정
- 인증된 사용자만 표시 (비인증 시 숨김)
- 탭하면 PostWritePage로 이동

### 3.5 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | PostCard 스켈레톤 (3개) |
| 빈 결과 | "게시글이 없습니다" (카테고리별 메시지) |
| 에러 | "게시글을 불러올 수 없습니다" + 재시도 버튼 |

---

## 4. 게시글 상세 페이지 (PostDetailPage)

### 4.1 레이아웃

```
┌─────────────────────────────────┐
│ ← 뒤로가기            [⋮ 더보기]│
├─────────────────────────────────┤
│ [프로필] 콩이맘                  │
│          팁 | 2026.02.06 15:30  │
├─────────────────────────────────┤
│                                  │
│ 소형견 데이케어 이용 꿀팁        │
│                                  │
│ 소형견 데이케어를 처음 이용하시는│
│ 분들을 위해 몇 가지 팁을         │
│ 공유합니다.                      │
│                                  │
│ 1. 첫 방문 전 시터와 사전 채팅을│
│    꼭 해보세요...                │
│                                  │
│ ┌──────────────────────────────┐│
│ │ [이미지 1]                    ││
│ └──────────────────────────────┘│
│ ┌──────────────────────────────┐│
│ │ [이미지 2]                    ││
│ └──────────────────────────────┘│
│                                  │
│ 👁 235                           │
├─────────────────────────────────┤
│ [♡ 42]      [💬 12]    [🚨신고]│
├─────────────────────────────────┤
│ 댓글 12개                        │
│                                  │
│ [두부맘] 정말 유용한 정보네요!   │
│          2/6 16:00    [답글]     │
│   ├─ [콩이맘] 도움이 되셨다니!  │
│   │           2/6 16:30          │
│                                  │
│ [삭제된 댓글입니다]              │
│                                  │
│ [초코아빠] 저도 경험담 공유!    │
│            2/6 17:00    [답글]   │
│                                  │
│ ... (더 보기)                    │
├─────────────────────────────────┤
│ [댓글 입력...]           [전송] │
└─────────────────────────────────┘
```

### 4.2 더보기 메뉴 (본인 게시글)

| 메뉴 | 동작 |
|------|------|
| 수정 | PostWritePage로 이동 (수정 모드) |
| 삭제 | 삭제 확인 모달 표시 |

### 4.3 더보기 메뉴 (타인 게시글)

| 메뉴 | 동작 |
|------|------|
| 신고 | 신고 모달 표시 |

### 4.4 좋아요 액션 (PostActions)

| 항목 | 설명 |
|------|------|
| 비활성 상태 | 빈 하트 아이콘 + 숫자 |
| 활성 상태 | 채워진 빨간 하트 아이콘 + 숫자 |
| 탭 동작 | 토글 API 호출, 즉시 UI 반영 (Optimistic Update) |

### 4.5 댓글 영역 (CommentItem)

| 항목 | 설명 |
|------|------|
| 1단계 댓글 | 프로필(24x24) + 닉네임 + 내용 + 시간 + [답글] 버튼 |
| 2단계 대댓글 | 들여쓰기 + 연결선 + 동일 구조 (답글 버튼 없음) |
| 삭제된 댓글 | 회색 텍스트 "삭제된 댓글입니다" (대댓글 있으면 유지) |
| 본인 댓글 | 길게 누르면 수정/삭제 옵션 |

### 4.6 댓글 입력 (CommentInput)

| 항목 | 설명 |
|------|------|
| 기본 상태 | 하단 고정 입력 바 |
| 답글 모드 | "@닉네임에게 답글" 플레이스홀더, 취소(X) 버튼 |
| 전송 버튼 | 내용이 있을 때만 활성화 (프라이머리 색상) |

---

## 5. 게시글 작성 페이지 (PostWritePage)

### 5.1 레이아웃

```
┌─────────────────────────────────┐
│ ← 취소         게시글 작성  [등록]│
├─────────────────────────────────┤
│                                  │
│ 카테고리 선택                    │
│ [팁] [자랑] [Q&A] [자유]        │
│                                  │
│ ┌──────────────────────────────┐│
│ │ 제목을 입력해주세요           ││
│ └──────────────────────────────┘│
│                                  │
│ ┌──────────────────────────────┐│
│ │                               ││
│ │ 내용을 입력해주세요           ││
│ │                               ││
│ │                               ││
│ │                               ││
│ └──────────────────────────────┘│
│                                  │
│ 이미지 (0/10)                    │
│ ┌────┐ ┌────┐ ┌────┐           │
│ │ +  │ │img1│ │img2│           │
│ │추가│ │  x │ │  x │           │
│ └────┘ └────┘ └────┘           │
│                                  │
└─────────────────────────────────┘
```

### 5.2 이미지 업로드 (ImageUploader)

| 항목 | 설명 |
|------|------|
| 최대 장수 | 10장 |
| 허용 형식 | jpg, jpeg, png, webp |
| 최대 크기 | 10MB/장 |
| 업로드 방식 | 파일 선택 시 즉시 파일 서버에 업로드, URL을 imageUrls에 추가 |
| 삭제 | 각 이미지 우상단 X 버튼 |
| 순서 변경 | 드래그 앤 드롭으로 순서 변경 |

### 5.3 수정 모드

- URL 파라미터의 id로 기존 게시글 데이터를 로드
- 카테고리, 제목, 내용, 이미지를 기존 데이터로 채움
- 헤더: "게시글 수정", 버튼: "수정"

### 5.4 유효성 검증

| 필드 | 조건 | 에러 메시지 |
|------|------|-------------|
| 카테고리 | 필수 선택 | "카테고리를 선택해주세요" |
| 제목 | 1~100자 | "제목을 입력해주세요 (100자 이내)" |
| 내용 | 1~5000자 | "내용을 입력해주세요 (5000자 이내)" |

### 5.5 신고 모달 (ReportModal)

```
┌─────────────────────────────────┐
│           게시글 신고             │
├─────────────────────────────────┤
│                                  │
│ 신고 사유를 입력해주세요         │
│ ┌──────────────────────────────┐│
│ │                               ││
│ │                               ││
│ └──────────────────────────────┘│
│                                  │
│ [취소]              [신고하기]   │
└─────────────────────────────────┘
```

---

## 6. 타입 정의 (types/community.ts)

```typescript
// 카테고리
type PostCategory = 'TIP' | 'BOAST' | 'QA' | 'FREE';

const CATEGORY_LABELS: Record<PostCategory, string> = {
  TIP: '팁',
  BOAST: '자랑',
  QA: 'Q&A',
  FREE: '자유',
};

// 작성자 정보
interface Author {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
}

// 게시글 목록 아이템
interface PostListItem {
  id: number;
  category: PostCategory;
  categoryName: string;
  title: string;
  contentPreview: string;
  thumbnailUrl: string | null;
  author: Author;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  createdAt: string;
}

// 게시글 상세
interface PostDetail {
  id: number;
  category: PostCategory;
  categoryName: string;
  title: string;
  content: string;
  imageUrls: string[];
  author: Author;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  isOwner: boolean;
  createdAt: string;
  updatedAt: string;
}

// 게시글 작성/수정 요청
interface PostCreateRequest {
  category: PostCategory;
  title: string;
  content: string;
  imageUrls?: string[];
}

// 좋아요 토글 응답
interface LikeToggleResponse {
  postId: number;
  isLiked: boolean;
  likeCount: number;
}

// 댓글
interface CommentItem {
  id: number;
  author: Author;
  content: string | null;
  isDeleted: boolean;
  isOwner: boolean;
  createdAt: string;
  updatedAt: string;
  replies: ReplyItem[];
}

interface ReplyItem {
  id: number;
  parentId: number;
  author: Author;
  content: string | null;
  isDeleted: boolean;
  isOwner: boolean;
  createdAt: string;
  updatedAt: string;
}

// 댓글 작성 요청
interface CommentCreateRequest {
  content: string;
  parentId?: number;
}

// 댓글 수정 요청
interface CommentUpdateRequest {
  content: string;
}

// 신고 요청
interface ReportRequest {
  reason: string;
}

// 신고 응답
interface ReportResponse {
  id: number;
  postId: number;
  status: string;
  message: string;
}
```

---

## 7. API 서비스 (api/community.ts)

```typescript
const communityApi = {
  // 게시글
  getPosts: (params: {
    category?: PostCategory;
    sort?: 'LATEST' | 'POPULAR' | 'MOST_COMMENTED';
    search?: string;
    page?: number;
    size?: number;
  }) => client.get<PageResponse<PostListItem>>('/api/v1/community/posts', { params }),

  getPost: (id: number) =>
    client.get<PostDetail>(`/api/v1/community/posts/${id}`),

  createPost: (data: PostCreateRequest) =>
    client.post<PostDetail>('/api/v1/community/posts', data),

  updatePost: (id: number, data: PostCreateRequest) =>
    client.put<PostDetail>(`/api/v1/community/posts/${id}`, data),

  deletePost: (id: number) =>
    client.delete(`/api/v1/community/posts/${id}`),

  // 좋아요
  toggleLike: (postId: number) =>
    client.post<LikeToggleResponse>(`/api/v1/community/posts/${postId}/like`),

  // 댓글
  getComments: (postId: number, params?: { page?: number; size?: number }) =>
    client.get<PageResponse<CommentItem>>(`/api/v1/community/posts/${postId}/comments`, { params }),

  createComment: (postId: number, data: CommentCreateRequest) =>
    client.post<CommentItem>(`/api/v1/community/posts/${postId}/comments`, data),

  updateComment: (commentId: number, data: CommentUpdateRequest) =>
    client.put<CommentItem>(`/api/v1/community/comments/${commentId}`, data),

  deleteComment: (commentId: number) =>
    client.delete(`/api/v1/community/comments/${commentId}`),

  // 신고
  reportPost: (postId: number, data: ReportRequest) =>
    client.post<ReportResponse>(`/api/v1/community/posts/${postId}/report`, data),
};
```

---

## 8. 커스텀 훅

```typescript
// usePosts.ts - 게시글 목록 (무한 스크롤)
const { posts, totalElements, loading, error, hasMore, loadMore, refetch } =
  usePosts({ category, sort, search });

// usePostDetail.ts - 게시글 상세
const { post, loading, error, refetch } = usePostDetail(postId);

// useComments.ts - 댓글 목록
const { comments, totalElements, loading, hasMore, loadMore, refetch } =
  useComments(postId);

// usePostLike.ts - 좋아요 토글
const { toggleLike, isLiked, likeCount } = usePostLike(postId, initialIsLiked, initialLikeCount);
```

---

## 9. 상태 처리

| 페이지 | 로딩 | 빈 상태 | 에러 |
|--------|------|---------|------|
| 메인 | PostCard 스켈레톤 (3개) | "아직 게시글이 없습니다. 첫 글을 작성해보세요!" | "게시글을 불러올 수 없습니다" + 재시도 |
| 상세 | 전체 스켈레톤 | - | "게시글을 불러올 수 없습니다" + 뒤로가기 |
| 댓글 | CommentItem 스켈레톤 (3개) | "첫 댓글을 남겨보세요!" | "댓글을 불러올 수 없습니다" + 재시도 |
