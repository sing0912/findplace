# 시터 프론트엔드 UI 지침

---

## 1. 파일 구조

```
frontend/src/
├── types/sitter.ts              # 타입 정의
├── api/sitter.ts                # API 서비스
├── hooks/
│   ├── useSitterSearch.ts       # 시터 검색 훅
│   └── useSitterDetail.ts       # 시터 상세 훅
├── components/sitter/
│   ├── SitterCard.tsx           # 시터 카드 (검색 결과 아이템)
│   ├── SitterFilter.tsx         # 필터/정렬 컴포넌트
│   ├── SitterServiceBadge.tsx   # 서비스 유형 뱃지
│   ├── SitterRating.tsx         # 평점 표시
│   ├── SitterIntroTab.tsx       # 상세 - 소개 탭
│   ├── SitterEnvTab.tsx         # 상세 - 환경 탭
│   ├── SitterPriceTab.tsx       # 상세 - 요금 탭
│   └── SitterReviewTab.tsx      # 상세 - 후기 탭
└── pages/search/
    ├── SitterSearchPage.tsx     # 시터 검색 페이지
    └── SitterDetailPage.tsx     # 시터 상세 페이지
```

---

## 2. 라우팅

| 경로 | 컴포넌트 | 설명 |
|------|----------|------|
| /search | SitterSearchPage | 시터 검색 결과 (리스트뷰) |
| /sitter/:id | SitterDetailPage | 시터 상세 (4개 탭) |

---

## 3. 시터 검색 페이지 (SitterSearchPage)

### 3.1 레이아웃

```
┌─────────────────────────────────┐
│ 검색바 (지역/날짜/서비스유형)    │
├─────────────────────────────────┤
│ 필터/정렬 바                     │
│ [서비스유형] [크기] [가격] [정렬] │
├─────────────────────────────────┤
│ 검색 결과 ({총 N}명의 시터)      │
│                                  │
│ ┌──────────────────────────┐    │
│ │ SitterCard               │    │
│ │ [사진] 해피독시터  ★4.8  │    │
│ │       서울 강남구 | 1.2km│    │
│ │       데이케어 35,000원~ │    │
│ │       후기 45 | 완료 120 │    │
│ └──────────────────────────┘    │
│                                  │
│ ┌──────────────────────────┐    │
│ │ SitterCard               │    │
│ │ ...                      │    │
│ └──────────────────────────┘    │
│                                  │
│ [더 보기] 또는 무한 스크롤       │
└─────────────────────────────────┘
```

### 3.2 SitterCard 정보

| 항목 | 표시 |
|------|------|
| 프로필 이미지 | 60x60 원형 |
| 닉네임 | 텍스트 |
| 평점 | 별 + 숫자 (4.8) |
| 지역 | 주소 요약 |
| 거리 | km (위치 검색 시) |
| 서비스/요금 | 가장 저렴한 서비스 표시 |
| 후기 수 | 후기 N개 |
| 수용 동물 | 아이콘 (강아지/고양이) |

### 3.3 필터 옵션

| 필터 | 옵션 |
|------|------|
| 서비스 유형 | 데이케어, 위탁 돌봄, 산책, 방문 돌봄, 미용 |
| 반려동물 크기 | 소형, 중형, 대형 |
| 가격 범위 | 슬라이더 (0 ~ 100,000원) |
| 정렬 | 거리순, 평점순, 가격 낮은순, 가격 높은순, 후기 많은순 |

### 3.4 상태 처리

| 상태 | UI |
|------|-----|
| 로딩 | SitterCard 스켈레톤 (3~4개) |
| 빈 결과 | "조건에 맞는 시터가 없습니다" + 필터 초기화 버튼 |
| 에러 | "검색 중 오류가 발생했습니다" + 다시 시도 버튼 |

---

## 4. 시터 상세 페이지 (SitterDetailPage)

### 4.1 레이아웃

```
┌─────────────────────────────────┐
│ ← 뒤로가기          [채팅] [♡]  │
├─────────────────────────────────┤
│ [프로필 이미지]                   │
│ 해피독시터            ★4.8 (45) │
│ 서울 강남구 역삼동               │
│ 완료 120건                       │
├─────────────────────────────────┤
│ [소개] [환경] [요금] [후기]      │
├─────────────────────────────────┤
│                                  │
│ {선택된 탭 콘텐츠}               │
│                                  │
├─────────────────────────────────┤
│        [예약하기] 버튼           │
└─────────────────────────────────┘
```

### 4.2 소개 탭 (SitterIntroTab)

| 항목 | 설명 |
|------|------|
| 자기소개 | introduction 텍스트 |
| 경력 | experience |
| 수용 가능 동물 | 아이콘 + 텍스트 (강아지, 고양이) |
| 수용 가능 크기 | 뱃지 (소형, 중형, 대형) |
| 동시 돌봄 수 | 최대 N마리 |
| 마당 유무 | 있음/없음 |
| 본인 반려동물 | 있음/없음 + 설명 |
| 흡연 여부 | 비흡연/흡연 |

### 4.3 환경 탭 (SitterEnvTab)

| 항목 | 설명 |
|------|------|
| 환경 사진 그리드 | 2열 그리드, 탭하면 풀스크린 뷰어 |
| 사진 캡션 | 각 사진 하단에 표시 |

### 4.4 요금 탭 (SitterPriceTab)

| 항목 | 설명 |
|------|------|
| 서비스 목록 | 카드 형태로 각 서비스 표시 |
| 서비스별 정보 | 유형명, 기본 요금, 추가 요금, 설명 |
| 요금 단위 | "1일 35,000원~", "1박 50,000원~" |

### 4.5 후기 탭 (SitterReviewTab)

| 항목 | 설명 |
|------|------|
| 평균 평점 | 큰 별 + 숫자 |
| 후기 목록 | 작성자명(마스킹), 별점, 내용, 펫 이름, 서비스 유형, 날짜 |
| 더 보기 | 페이지네이션 (10개씩) |

---

## 5. 타입 정의 (types/sitter.ts)

```typescript
interface SitterSummary {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
  address: string;
  distance: number | null;
  averageRating: number;
  reviewCount: number;
  completedBookingCount: number;
  services: SitterServiceSummary[];
  acceptablePetTypes: string[];
  environmentPhotoUrl: string | null;
  isActive: boolean;
}

interface SitterServiceSummary {
  serviceType: string;
  serviceTypeName: string;
  basePrice: number;
}

interface SitterDetail {
  id: number;
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  introduction: string | null;
  experience: string | null;
  address: string;
  latitude: number;
  longitude: number;
  averageRating: number;
  reviewCount: number;
  completedBookingCount: number;
  verificationStatus: string;
  profile: SitterProfileDetail;
  services: SitterServiceDetail[];
  environmentPhotos: EnvironmentPhoto[];
  recentReviews: SitterReview[];
  createdAt: string;
}

interface SitterProfileDetail {
  acceptablePetTypes: string[];
  acceptablePetSizes: string[];
  maxPetCount: number;
  hasYard: boolean;
  hasOwnPets: boolean;
  ownPetsDescription: string | null;
  smokingStatus: boolean;
}

interface SitterServiceDetail {
  id: number;
  serviceType: string;
  serviceTypeName: string;
  basePrice: number;
  additionalPetPrice: number;
  description: string | null;
  isActive: boolean;
}

interface EnvironmentPhoto {
  id: number;
  imageUrl: string;
  caption: string | null;
}

interface SitterReview {
  id: number;
  customerName: string;
  rating: number;
  content: string;
  petName: string;
  serviceType: string;
  createdAt: string;
}

interface SitterSearchParams {
  region?: string;
  latitude?: number;
  longitude?: number;
  radius?: number;
  serviceType?: string;
  petType?: string;
  petSize?: string;
  startDate?: string;
  endDate?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: 'DISTANCE' | 'RATING' | 'PRICE_LOW' | 'PRICE_HIGH' | 'REVIEW_COUNT';
  page?: number;
  size?: number;
}
```

---

## 6. API 서비스 (api/sitter.ts)

```typescript
const sitterApi = {
  search: (params: SitterSearchParams) =>
    client.get<PageResponse<SitterSummary>>('/api/v1/sitters', { params }),

  getDetail: (id: number) =>
    client.get<SitterDetail>(`/api/v1/sitters/${id}`),
};
```

---

## 7. 커스텀 훅

```typescript
// useSitterSearch.ts
const { sitters, totalElements, loading, error, refetch } = useSitterSearch(params);

// useSitterDetail.ts
const { sitter, loading, error } = useSitterDetail(id);
```
