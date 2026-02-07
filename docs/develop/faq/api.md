# FAQ 관리 - API 명세

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## API 목록 요약

### 사용자 API (Public)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/faq/categories | 카테고리 목록 조회 | 없음 (공개) |
| GET | /api/v1/faq | FAQ 목록 조회 | 없음 (공개) |
| GET | /api/v1/faq/{id} | FAQ 상세 조회 | 없음 (공개) |

### 관리자 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | /api/v1/admin/faq/categories | 카테고리 추가 | ADMIN, SUPER_ADMIN |
| PUT | /api/v1/admin/faq/categories/{id} | 카테고리 수정 | ADMIN, SUPER_ADMIN |
| DELETE | /api/v1/admin/faq/categories/{id} | 카테고리 삭제 | ADMIN, SUPER_ADMIN |
| POST | /api/v1/admin/faq | FAQ 등록 | ADMIN, SUPER_ADMIN |
| PUT | /api/v1/admin/faq/{id} | FAQ 수정 | ADMIN, SUPER_ADMIN |
| DELETE | /api/v1/admin/faq/{id} | FAQ 삭제 | ADMIN, SUPER_ADMIN |

---

## 사용자 API 상세

### 1. 카테고리 목록 조회

```
GET /api/v1/faq/categories
```

> 활성(isActive=true) 카테고리만 반환합니다.

**Response (200 OK):**

```json
{
  "categories": [
    {
      "id": 1,
      "name": "예약/결제",
      "sortOrder": 1,
      "faqCount": 5
    },
    {
      "id": 2,
      "name": "돌봄 서비스",
      "sortOrder": 2,
      "faqCount": 3
    },
    {
      "id": 3,
      "name": "시터 등록",
      "sortOrder": 3,
      "faqCount": 4
    }
  ]
}
```

---

### 2. FAQ 목록 조회

```
GET /api/v1/faq
```

> 공개(isPublished=true) FAQ만 반환합니다.

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| categoryId | Long | N | 카테고리 ID 필터 | 전체 |
| keyword | String | N | 검색어 (질문/답변 검색) | - |
| page | Integer | N | 페이지 번호 | 0 |
| size | Integer | N | 페이지 크기 | 20 |

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "예약/결제",
      "question": "예약 취소는 어떻게 하나요?",
      "sortOrder": 1,
      "viewCount": 120,
      "createdAt": "2026-01-15T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1
}
```

---

### 3. FAQ 상세 조회

```
GET /api/v1/faq/{id}
```

> 조회 시 viewCount가 1 증가합니다.

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | FAQ ID |

**Response (200 OK):**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "예약/결제",
  "question": "예약 취소는 어떻게 하나요?",
  "answer": "마이페이지 > 예약 내역에서 해당 예약을 선택 후 '예약 취소' 버튼을 눌러주세요.\n\n취소 수수료는 돌봄 시작 48시간 전까지 무료이며, 이후 취소 시 수수료가 발생합니다.",
  "viewCount": 121,
  "createdAt": "2026-01-15T10:00:00",
  "updatedAt": "2026-01-20T15:30:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_NOT_FOUND | 404 | FAQ를 찾을 수 없음 |

---

## 관리자 API 상세

### 1. 카테고리 추가

```
POST /api/v1/admin/faq/categories
```

**Request Body:**

```json
{
  "name": "예약/결제",
  "sortOrder": 1,
  "isActive": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | Y | 카테고리명 (최대 100자) |
| sortOrder | Integer | N | 정렬 순서 (기본값: 0) |
| isActive | Boolean | N | 활성 여부 (기본값: true) |

**Response (201 Created):**

```json
{
  "id": 1,
  "name": "예약/결제",
  "sortOrder": 1,
  "isActive": true,
  "createdAt": "2026-02-07T10:00:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_CATEGORY_DUPLICATE_NAME | 400 | 동일한 이름의 카테고리가 이미 존재 |

---

### 2. 카테고리 수정

```
PUT /api/v1/admin/faq/categories/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | 카테고리 ID |

**Request Body:**

```json
{
  "name": "예약/결제/환불",
  "sortOrder": 1,
  "isActive": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | Y | 카테고리명 (최대 100자) |
| sortOrder | Integer | N | 정렬 순서 |
| isActive | Boolean | N | 활성 여부 |

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "예약/결제/환불",
  "sortOrder": 1,
  "isActive": true,
  "updatedAt": "2026-02-07T11:00:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_CATEGORY_NOT_FOUND | 404 | 카테고리를 찾을 수 없음 |
| FAQ_CATEGORY_DUPLICATE_NAME | 400 | 동일한 이름의 카테고리가 이미 존재 |

---

### 3. 카테고리 삭제

```
DELETE /api/v1/admin/faq/categories/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | 카테고리 ID |

**Response (204 No Content)**

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_CATEGORY_NOT_FOUND | 404 | 카테고리를 찾을 수 없음 |
| FAQ_CATEGORY_HAS_ITEMS | 400 | 하위 FAQ가 존재하여 삭제 불가 |

---

### 4. FAQ 등록

```
POST /api/v1/admin/faq
```

**Request Body:**

```json
{
  "categoryId": 1,
  "question": "예약 취소는 어떻게 하나요?",
  "answer": "마이페이지 > 예약 내역에서 해당 예약을 선택 후 '예약 취소' 버튼을 눌러주세요.",
  "sortOrder": 1,
  "isPublished": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| categoryId | Long | Y | 카테고리 ID |
| question | String | Y | 질문 (최대 500자) |
| answer | String | Y | 답변 |
| sortOrder | Integer | N | 정렬 순서 (기본값: 0) |
| isPublished | Boolean | N | 공개 여부 (기본값: false) |

**Response (201 Created):**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "예약/결제",
  "question": "예약 취소는 어떻게 하나요?",
  "answer": "마이페이지 > 예약 내역에서 해당 예약을 선택 후 '예약 취소' 버튼을 눌러주세요.",
  "sortOrder": 1,
  "isPublished": true,
  "viewCount": 0,
  "createdAt": "2026-02-07T10:00:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_CATEGORY_NOT_FOUND | 404 | 카테고리를 찾을 수 없음 |

---

### 5. FAQ 수정

```
PUT /api/v1/admin/faq/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | FAQ ID |

**Request Body:**

```json
{
  "categoryId": 1,
  "question": "예약 취소/변경은 어떻게 하나요?",
  "answer": "마이페이지 > 예약 내역에서 해당 예약을 선택 후 '예약 취소' 또는 '예약 변경' 버튼을 눌러주세요.",
  "sortOrder": 1,
  "isPublished": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| categoryId | Long | Y | 카테고리 ID |
| question | String | Y | 질문 (최대 500자) |
| answer | String | Y | 답변 |
| sortOrder | Integer | N | 정렬 순서 |
| isPublished | Boolean | N | 공개 여부 |

**Response (200 OK):**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "예약/결제",
  "question": "예약 취소/변경은 어떻게 하나요?",
  "answer": "마이페이지 > 예약 내역에서 해당 예약을 선택 후 '예약 취소' 또는 '예약 변경' 버튼을 눌러주세요.",
  "sortOrder": 1,
  "isPublished": true,
  "viewCount": 121,
  "updatedAt": "2026-02-07T11:00:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_NOT_FOUND | 404 | FAQ를 찾을 수 없음 |
| FAQ_CATEGORY_NOT_FOUND | 404 | 카테고리를 찾을 수 없음 |

---

### 6. FAQ 삭제

```
DELETE /api/v1/admin/faq/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | FAQ ID |

**Response (204 No Content)**

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| FAQ_NOT_FOUND | 404 | FAQ를 찾을 수 없음 |
