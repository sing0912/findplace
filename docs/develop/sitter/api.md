# 시터 API 스펙

---

## 1. 시터 검색 API

### 1.1 시터 목록 검색

```
GET /api/v1/sitters
```

**권한**: Public (비인증 접근 가능)

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| region | String | N | 지역 키워드 (예: "서울 강남") |
| latitude | Decimal | N | 검색 기준 위도 |
| longitude | Decimal | N | 검색 기준 경도 |
| radius | Integer | N | 검색 반경 (km, 기본값 5) |
| serviceType | String | N | 서비스 유형 (DAY_CARE, BOARDING, WALKING, HOME_VISIT, GROOMING) |
| petType | String | N | 반려동물 종류 (DOG, CAT 등) |
| petSize | String | N | 크기 (SMALL, MEDIUM, LARGE) |
| startDate | Date | N | 이용 시작일 |
| endDate | Date | N | 이용 종료일 |
| minPrice | Integer | N | 최소 요금 |
| maxPrice | Integer | N | 최대 요금 |
| sort | String | N | 정렬 (DISTANCE, RATING, PRICE_LOW, PRICE_HIGH, REVIEW_COUNT) |
| page | Integer | N | 페이지 번호 (기본값 0) |
| size | Integer | N | 페이지 크기 (기본값 20) |

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "content": [
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
          },
          {
            "serviceType": "WALKING",
            "serviceTypeName": "산책",
            "basePrice": 15000
          }
        ],
        "acceptablePetTypes": ["DOG", "CAT"],
        "environmentPhotoUrl": "...",
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**거리 계산**: Haversine 공식 사용 (위도/경도 기반)

### 1.2 정렬 기준

| 값 | 설명 | 기본값 |
|----|------|--------|
| DISTANCE | 거리순 (가까운 순) | latitude/longitude 있을 때 기본 |
| RATING | 평점순 (높은 순) | 위치 없을 때 기본 |
| PRICE_LOW | 가격순 (낮은 순) | |
| PRICE_HIGH | 가격순 (높은 순) | |
| REVIEW_COUNT | 후기 많은 순 | |

---

## 2. 시터 상세 API

### 2.1 시터 상세 조회

```
GET /api/v1/sitters/{id}
```

**권한**: Public

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 10,
    "nickname": "해피독시터",
    "profileImageUrl": "...",
    "introduction": "안녕하세요! 5년차 펫시터입니다...",
    "experience": "반려동물관리사 자격증 보유, 5년 경력",
    "address": "서울 강남구 역삼동",
    "latitude": 37.5001,
    "longitude": 127.0365,
    "averageRating": 4.8,
    "reviewCount": 45,
    "completedBookingCount": 120,
    "verificationStatus": "APPROVED",
    "profile": {
      "acceptablePetTypes": ["DOG", "CAT"],
      "acceptablePetSizes": ["SMALL", "MEDIUM"],
      "maxPetCount": 3,
      "hasYard": true,
      "hasOwnPets": true,
      "ownPetsDescription": "말티즈 1마리 (3살, 수컷)",
      "smokingStatus": false
    },
    "services": [
      {
        "id": 1,
        "serviceType": "DAY_CARE",
        "serviceTypeName": "데이케어",
        "basePrice": 35000,
        "additionalPetPrice": 15000,
        "description": "오전 9시~오후 7시, 산책 2회 포함",
        "isActive": true
      },
      {
        "id": 2,
        "serviceType": "BOARDING",
        "serviceTypeName": "위탁 돌봄",
        "basePrice": 50000,
        "additionalPetPrice": 20000,
        "description": "1박 기준, 24시간 케어",
        "isActive": true
      }
    ],
    "environmentPhotos": [
      {
        "id": 1,
        "imageUrl": "...",
        "caption": "넓은 거실 공간"
      },
      {
        "id": 2,
        "imageUrl": "...",
        "caption": "마당 놀이 공간"
      }
    ],
    "recentReviews": [
      {
        "id": 1,
        "customerName": "김**",
        "rating": 5,
        "content": "정말 꼼꼼하게 돌봐주셨어요!",
        "petName": "콩이",
        "serviceType": "DAY_CARE",
        "createdAt": "2026-02-01T10:00:00"
      }
    ],
    "createdAt": "2025-06-15T10:00:00"
  }
}
```

**에러 코드**:

| 코드 | 상태 | 설명 |
|------|------|------|
| SITTER_NOT_FOUND | 404 | 시터를 찾을 수 없음 |
| SITTER_NOT_ACTIVE | 400 | 활동 중이 아닌 시터 |

---

## 3. 시터 프로필 관리 API (PARTNER 전용)

### 3.1 내 시터 프로필 조회

```
GET /api/v1/partner/profile
```

**권한**: PARTNER

**Response** (200 OK): 시터 상세와 동일한 구조 + 비공개 필드 포함

### 3.2 기본 정보 등록/수정 (Step 1)

```
POST /api/v1/partner/profile/basic
PUT /api/v1/partner/profile/basic
```

**권한**: PARTNER

**Request Body**:
```json
{
  "nickname": "해피독시터",
  "introduction": "안녕하세요!",
  "experience": "5년 경력",
  "address": "서울 강남구 역삼동 123-4",
  "addressDetail": "101호",
  "latitude": 37.5001,
  "longitude": 127.0365
}
```

### 3.3 활동 정보 등록/수정 (Step 2)

```
POST /api/v1/partner/profile/detail
PUT /api/v1/partner/profile/detail
```

**권한**: PARTNER

**Request Body**:
```json
{
  "acceptablePetTypes": ["DOG", "CAT"],
  "acceptablePetSizes": ["SMALL", "MEDIUM", "LARGE"],
  "maxPetCount": 3,
  "hasYard": true,
  "hasOwnPets": true,
  "ownPetsDescription": "말티즈 1마리",
  "smokingStatus": false,
  "emergencyContact": "010-1234-5678"
}
```

### 3.4 환경 사진 관리 (Step 3)

```
POST /api/v1/partner/profile/photos
DELETE /api/v1/partner/profile/photos/{photoId}
PUT /api/v1/partner/profile/photos/order
```

**권한**: PARTNER

**사진 업로드 (multipart/form-data)**:
```
POST /api/v1/partner/profile/photos
Content-Type: multipart/form-data

file: (binary)
caption: "넓은 거실"
```

**순서 변경**:
```json
{
  "photoOrders": [
    { "photoId": 1, "sortOrder": 0 },
    { "photoId": 3, "sortOrder": 1 },
    { "photoId": 2, "sortOrder": 2 }
  ]
}
```

### 3.5 서비스 & 요금 관리 (Step 4)

```
POST /api/v1/partner/services
PUT /api/v1/partner/services/{serviceId}
DELETE /api/v1/partner/services/{serviceId}
```

**권한**: PARTNER

**Request Body** (등록/수정):
```json
{
  "serviceType": "DAY_CARE",
  "basePrice": 35000,
  "additionalPetPrice": 15000,
  "description": "오전 9시~오후 7시, 산책 2회 포함"
}
```

### 3.6 자격 증빙 관리

```
POST /api/v1/partner/documents
GET /api/v1/partner/documents
DELETE /api/v1/partner/documents/{documentId}
```

**권한**: PARTNER

**서류 업로드 (multipart/form-data)**:
```
POST /api/v1/partner/documents
Content-Type: multipart/form-data

file: (binary)
documentType: "ID_CARD"
```

### 3.7 심사 요청

```
POST /api/v1/partner/verification/request
```

**권한**: PARTNER

**검증 조건** (모두 충족해야 요청 가능):
- 기본 정보(Step 1) 완료
- 활동 정보(Step 2) 완료
- 환경 사진 최소 3장(Step 3)
- 서비스 최소 1개(Step 4)
- 필수 서류(신분증, 범죄경력회보서) 제출

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "verificationStatus": "PENDING",
    "message": "심사 요청이 접수되었습니다."
  }
}
```

**에러 코드**:

| 코드 | 상태 | 설명 |
|------|------|------|
| PROFILE_INCOMPLETE | 400 | 프로필이 완성되지 않음 |
| REQUIRED_DOCUMENT_MISSING | 400 | 필수 서류 미제출 |
| ALREADY_UNDER_REVIEW | 400 | 이미 심사 중 |
| ALREADY_APPROVED | 400 | 이미 승인됨 |

---

## 4. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/sitters` | GET | Public |
| `/api/v1/sitters/{id}` | GET | Public |
| `/api/v1/partner/**` | ALL | PARTNER |
| `/api/v1/admin/sitters/**` | ALL | ADMIN, SUPER_ADMIN |
