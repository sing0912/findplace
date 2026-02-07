# 후기 프론트엔드 UI 지침

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## 1. 파일 구조

```
frontend/src/
├── types/review.ts                  # 타입 정의
├── api/review.ts                    # API 서비스
├── hooks/
│   ├── useReviewWrite.ts            # 후기 작성 훅
│   └── useSitterReviews.ts          # 시터 후기 목록 훅
├── components/review/
│   ├── StarRating.tsx               # 별점 입력/표시 컴포넌트
│   ├── ReviewTagSelector.tsx        # 태그 선택 컴포넌트
│   ├── ReviewCard.tsx               # 후기 카드 (목록 아이템)
│   ├── ReviewSummary.tsx            # 후기 요약 (평균 평점, 분포, 태그)
│   ├── RatingDistribution.tsx       # 평점 분포 막대 그래프
│   └── ReviewEmpty.tsx              # 후기 없음 안내
└── pages/review/
    ├── ReviewWritePage.tsx           # 후기 작성 페이지
    ├── ReviewEditPage.tsx           # 후기 수정 페이지
    └── index.ts
```

---

## 2. 라우팅

| 경로 | 컴포넌트 | 설명 | 권한 |
|------|----------|------|------|
| /bookings/:bookingId/review | ReviewWritePage | 후기 작성 | CUSTOMER |
| /reviews/:id/edit | ReviewEditPage | 후기 수정 | CUSTOMER (본인) |

> **참고**: 시터 후기 목록은 별도 페이지가 아닌, 시터 상세 페이지의 후기 탭(`SitterReviewTab`)에서 `ReviewSummary` + `ReviewCard` 컴포넌트를 사용하여 표시합니다.

---

## 3. 후기 작성 (ReviewWritePage)

### 3.1 레이아웃

```
┌─────────────────────────────────────┐
│ < 후기 작성                  [등록] │
├─────────────────────────────────────┤
│                                     │
│ 예약 정보                           │
│ ┌─────────────────────────────────┐│
│ │ [시터 프로필]                    ││
│ │ 해피독시터 | 데이케어            ││
│ │ 2026-02-07 | 콩이 (말티즈)      ││
│ └─────────────────────────────────┘│
│                                     │
│ 별점                                │
│ ┌─────────────────────────────────┐│
│ │        ★ ★ ★ ★ ☆              ││
│ │         (4점 선택됨)            ││
│ └─────────────────────────────────┘│
│                                     │
│ 이런 점이 좋았어요 (최대 5개)       │
│ ┌─────────────────────────────────┐│
│ │ [친절해요]  [깨끗해요]          ││
│ │ [응답이 빨라요]  [전문적이에요] ││
│ │ [일지가 꼼꼼해요]              ││
│ │ [산책을 잘해요]  [애정이 넘쳐요]││
│ └─────────────────────────────────┘│
│                                     │
│ 후기 내용                           │
│ ┌─────────────────────────────────┐│
│ │                                 ││
│ │ (텍스트 입력 영역)              ││
│ │ 최소 10자, 최대 1000자          ││
│ │                                 ││
│ │                    (25 / 1000)  ││
│ └─────────────────────────────────┘│
│                                     │
│           [후기 등록하기]           │
│                                     │
└─────────────────────────────────────┘
```

### 3.2 별점 입력 (StarRating)

| 항목 | 설명 |
|------|------|
| 크기 | 별 40x40 (작성 모드), 16x16 (표시 모드) |
| 색상 | 선택됨: #FFD700 (금색), 미선택: #D0D0D0 (회색) |
| 동작 | 탭으로 별점 선택 (1~5) |
| 필수 | 필수 입력 (미선택 시 등록 버튼 비활성) |

### 3.3 태그 선택 (ReviewTagSelector)

| 항목 | 설명 |
|------|------|
| 표시 | 칩(Chip) 형태, 가로 나열 + 줄바꿈 |
| 선택 스타일 | 선택됨: Primary 배경 + 흰색 텍스트, 미선택: 흰색 배경 + 테두리 |
| 최대 선택 | 5개 (초과 시 토스트 알림) |
| 필수 | 선택사항 |

### 3.4 후기 내용 입력

| 항목 | 설명 |
|------|------|
| 최소 글자 | 10자 (미달 시 등록 버튼 비활성) |
| 최대 글자 | 1000자 |
| 글자 수 표시 | 우하단 "(현재/최대)" 형식 |
| placeholder | "돌봄 서비스에 대한 솔직한 후기를 작성해주세요." |

### 3.5 등록 버튼 활성화 조건

- 별점 1개 이상 선택
- 후기 내용 10자 이상 입력

### 3.6 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | 등록 버튼 로딩 스피너 |
| 성공 | "후기가 등록되었습니다" 토스트 + 이전 화면으로 이동 |
| 에러 | 에러 메시지 토스트 |

---

## 4. 후기 수정 (ReviewEditPage)

- ReviewWritePage와 동일한 레이아웃
- 기존 후기 데이터를 미리 채움 (별점, 태그, 내용)
- 제목: "후기 수정"
- 버튼: "후기 수정하기"
- 하단에 "후기 삭제" 텍스트 버튼 (빨간색)
- 삭제 시 확인 다이얼로그: "후기를 삭제하시겠습니까? 삭제된 후기는 복구할 수 없습니다."

---

## 5. 시터 후기 탭 컴포넌트 (SitterReviewTab 구성)

시터 상세 페이지(`SitterDetailPage`)의 후기 탭에서 사용되는 컴포넌트입니다.

### 5.1 레이아웃

```
┌─────────────────────────────────────┐
│                                     │
│ ReviewSummary                       │
│ ┌─────────────────────────────────┐│
│ │ ★ 4.8  (45개)                   ││
│ │                                 ││
│ │ 5★ ████████████████████ 35     ││
│ │ 4★ ██████             7        ││
│ │ 3★ ██                 2        ││
│ │ 2★ █                  1        ││
│ │ 1★                    0        ││
│ │                                 ││
│ │ 태그                            ││
│ │ [친절해요 38] [일지가 꼼꼼 30]  ││
│ │ [애정이 넘쳐요 25]             ││
│ └─────────────────────────────────┘│
│                                     │
│ ReviewCard 목록                     │
│ ┌─────────────────────────────────┐│
│ │ [프로필] 김**       ★★★★★     ││
│ │ 데이케어 | 콩이     2026-02-07  ││
│ │ 콩이를 정말 잘 돌봐주셨어요!    ││
│ │ 돌봄 일지도 꼼꼼하게...         ││
│ │ [친절해요] [일지가 꼼꼼해요]    ││
│ └─────────────────────────────────┘│
│                                     │
│ ┌─────────────────────────────────┐│
│ │ [프로필] 박**       ★★★★☆     ││
│ │ 위탁 돌봄 | 두부    2026-02-05  ││
│ │ 전반적으로 만족스러운 돌봄...    ││
│ │ [전문적이에요] [응답이 빨라요]  ││
│ └─────────────────────────────────┘│
│                                     │
│ [더 보기]                           │
│                                     │
└─────────────────────────────────────┘
```

### 5.2 ReviewSummary 컴포넌트

| 항목 | 설명 |
|------|------|
| 평균 평점 | 큰 별 + 숫자 (4.8) + 총 후기 수 |
| 평점 분포 | 5단계 수평 막대 그래프 (RatingDistribution) |
| 인기 태그 | 칩 형태, 태그별 횟수 표시 (상위 5개) |

### 5.3 ReviewCard 컴포넌트

| 항목 | 설명 |
|------|------|
| 프로필 | 반려인 프로필 이미지 (36x36 원형) |
| 닉네임 | 마스킹 처리 (예: "김**") |
| 별점 | 별 아이콘 (16x16) |
| 서비스 유형 | 뱃지 형태 (데이케어, 위탁 돌봄 등) |
| 펫 이름 | 텍스트 |
| 작성일 | YYYY-MM-DD 형식 |
| 내용 | 최대 3줄, 초과 시 "더 보기" 링크 |
| 태그 | 칩 형태 (작은 사이즈) |
| 수정/삭제 | 본인 후기인 경우 메뉴 버튼 (...) 표시 |

### 5.4 필터/정렬

| 항목 | 옵션 |
|------|------|
| 정렬 | 최신순 (기본), 평점 높은순, 평점 낮은순 |
| 평점 필터 | 전체, 5점, 4점, 3점, 2점, 1점 |

### 5.5 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | ReviewCard 스켈레톤 (3개) |
| 빈 목록 | "아직 후기가 없습니다" (ReviewEmpty) |
| 에러 | "후기를 불러올 수 없습니다" + 다시 시도 버튼 |

---

## 6. 타입 정의 (types/review.ts)

```typescript
type ReviewTag =
  | 'FRIENDLY'
  | 'CLEAN'
  | 'RESPONSIVE'
  | 'PROFESSIONAL'
  | 'DETAILED_JOURNAL'
  | 'GOOD_WALKING'
  | 'CARING';

interface SitterReview {
  id: number;
  bookingId: number;
  customerId?: number;
  customerNickname: string;
  customerProfileImageUrl: string | null;
  partnerId: number;
  rating: number;
  content: string;
  tags: ReviewTag[];
  tagNames: string[];
  serviceType: string;
  serviceTypeName: string;
  petName: string;
  createdAt: string;
  updatedAt?: string;
}

interface ReviewSummary {
  averageRating: number;
  totalCount: number;
  ratingDistribution: Record<string, number>;
  topTags: ReviewTagCount[];
}

interface ReviewTagCount {
  tag: ReviewTag;
  tagName: string;
  count: number;
}

interface CreateReviewRequest {
  rating: number;
  content: string;
  tags?: ReviewTag[];
}

interface UpdateReviewRequest {
  rating: number;
  content: string;
  tags?: ReviewTag[];
}

interface ReviewListResponse {
  summary: ReviewSummary;
  content: SitterReview[];
  page: PageInfo;
}

interface PageInfo {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ReviewTag 한글 라벨 매핑
const REVIEW_TAG_LABELS: Record<ReviewTag, string> = {
  FRIENDLY: '친절해요',
  CLEAN: '깨끗해요',
  RESPONSIVE: '응답이 빨라요',
  PROFESSIONAL: '전문적이에요',
  DETAILED_JOURNAL: '일지가 꼼꼼해요',
  GOOD_WALKING: '산책을 잘해요',
  CARING: '애정이 넘쳐요',
};
```

---

## 7. API 서비스 (api/review.ts)

```typescript
interface ReviewSearchParams {
  page?: number;
  size?: number;
  sort?: string;
  rating?: number;
}

const reviewApi = {
  create: (bookingId: number, data: CreateReviewRequest) =>
    client.post<SitterReview>(`/api/v1/bookings/${bookingId}/review`, data),

  getSitterReviews: (sitterId: number, params?: ReviewSearchParams) =>
    client.get<ReviewListResponse>(`/api/v1/sitters/${sitterId}/reviews`, { params }),

  update: (id: number, data: UpdateReviewRequest) =>
    client.put<SitterReview>(`/api/v1/reviews/${id}`, data),

  delete: (id: number) =>
    client.delete(`/api/v1/reviews/${id}`),
};
```

---

## 8. 커스텀 훅

```typescript
// useReviewWrite.ts - 후기 작성/수정/삭제
const {
  loading,          // boolean
  error,            // Error | null
  createReview,     // (bookingId: number, data: CreateReviewRequest) => Promise<void>
  updateReview,     // (id: number, data: UpdateReviewRequest) => Promise<void>
  deleteReview,     // (id: number) => Promise<void>
} = useReviewWrite();

// useSitterReviews.ts - 시터 후기 목록
const {
  summary,          // ReviewSummary | null
  reviews,          // SitterReview[]
  loading,          // boolean
  error,            // Error | null
  hasMore,          // boolean
  loadMore,         // () => void
  filterByRating,   // (rating?: number) => void
  changeSort,       // (sort: string) => void
  refetch,          // () => void
} = useSitterReviews(sitterId);
```
