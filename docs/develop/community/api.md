# 커뮤니티 API 스펙

---

## 1. API 목록

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/community/posts | 게시글 목록 | 선택 |
| 2 | POST | /api/v1/community/posts | 게시글 작성 | 필수 |
| 3 | GET | /api/v1/community/posts/{id} | 게시글 상세 | 선택 |
| 4 | PUT | /api/v1/community/posts/{id} | 게시글 수정 | 필수 |
| 5 | DELETE | /api/v1/community/posts/{id} | 게시글 삭제 | 필수 |
| 6 | POST | /api/v1/community/posts/{id}/like | 좋아요 토글 | 필수 |
| 7 | GET | /api/v1/community/posts/{id}/comments | 댓글 목록 | 선택 |
| 8 | POST | /api/v1/community/posts/{id}/comments | 댓글 작성 | 필수 |
| 9 | PUT | /api/v1/community/comments/{id} | 댓글 수정 | 필수 |
| 10 | DELETE | /api/v1/community/comments/{id} | 댓글 삭제 | 필수 |
| 11 | POST | /api/v1/community/posts/{id}/report | 게시글 신고 | 필수 |

---

## 2. 게시글 목록

```
GET /api/v1/community/posts?category=TIP&sort=LATEST&page=0&size=20
```

**권한**: Public (비인증 접근 가능, 인증 시 좋아요 여부 포함)

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| category | String | N | 카테고리 필터 (TIP, BOAST, QA, FREE) |
| sort | String | N | 정렬 (LATEST, POPULAR, MOST_COMMENTED), 기본값: LATEST |
| search | String | N | 검색어 (제목+내용) |
| page | Integer | N | 페이지 번호 (기본값 0) |
| size | Integer | N | 페이지 크기 (기본값 20) |

**정렬 기준**:

| 값 | 설명 |
|----|------|
| LATEST | 최신순 (created_at DESC) |
| POPULAR | 인기순 (like_count DESC, created_at DESC) |
| MOST_COMMENTED | 댓글 많은순 (comment_count DESC, created_at DESC) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "category": "TIP",
        "categoryName": "팁",
        "title": "소형견 데이케어 이용 꿀팁",
        "contentPreview": "소형견 데이케어를 처음 이용하시는 분들을 위해...",
        "thumbnailUrl": "https://storage.example.com/community/1/thumb.jpg",
        "author": {
          "id": 10,
          "nickname": "콩이맘",
          "profileImageUrl": "..."
        },
        "viewCount": 234,
        "likeCount": 42,
        "commentCount": 12,
        "isLiked": false,
        "createdAt": "2026-02-06T15:30:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8
    }
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**: `contentPreview`는 본문의 처음 100자를 잘라서 반환합니다. `isLiked`는 인증된 사용자의 경우에만 포함되며, 비인증 시 항상 `false`입니다.

---

## 3. 게시글 작성

```
POST /api/v1/community/posts
```

**권한**: 인증 필수 (CUSTOMER, PARTNER)

**Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body**:

```json
{
  "category": "TIP",
  "title": "소형견 데이케어 이용 꿀팁",
  "content": "소형견 데이케어를 처음 이용하시는 분들을 위해 몇 가지 팁을 공유합니다...",
  "imageUrls": [
    "https://storage.example.com/community/upload/img1.jpg",
    "https://storage.example.com/community/upload/img2.jpg"
  ]
}
```

| 필드 | 타입 | 필수 | 제약조건 |
|------|------|------|----------|
| category | String | 필수 | TIP, BOAST, QA, FREE 중 하나 |
| title | String | 필수 | 1~100자 |
| content | String | 필수 | 1~5000자 |
| imageUrls | String[] | 선택 | 최대 10개, 각 URL은 유효한 파일 URL |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "TIP",
    "categoryName": "팁",
    "title": "소형견 데이케어 이용 꿀팁",
    "content": "소형견 데이케어를 처음 이용하시는 분들을 위해...",
    "imageUrls": [
      "https://storage.example.com/community/upload/img1.jpg",
      "https://storage.example.com/community/upload/img2.jpg"
    ],
    "author": {
      "id": 10,
      "nickname": "콩이맘",
      "profileImageUrl": "..."
    },
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "createdAt": "2026-02-07T10:00:00"
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

**에러 응답**:

```json
{ "code": "INVALID_POST_TITLE", "message": "제목을 입력해주세요. (1~100자)" }
{ "code": "INVALID_POST_CONTENT", "message": "내용을 입력해주세요. (1~5000자)" }
{ "code": "INVALID_POST_CATEGORY", "message": "유효하지 않은 카테고리입니다." }
{ "code": "POST_IMAGE_LIMIT_EXCEEDED", "message": "이미지는 최대 10장까지 첨부할 수 있습니다." }
```

---

## 4. 게시글 상세

```
GET /api/v1/community/posts/{id}
```

**권한**: Public (인증 시 좋아요 여부 포함)

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "TIP",
    "categoryName": "팁",
    "title": "소형견 데이케어 이용 꿀팁",
    "content": "소형견 데이케어를 처음 이용하시는 분들을 위해 몇 가지 팁을 공유합니다.\n\n1. 첫 방문 전 시터와 사전 채팅을 꼭 해보세요...",
    "imageUrls": [
      "https://storage.example.com/community/upload/img1.jpg",
      "https://storage.example.com/community/upload/img2.jpg"
    ],
    "author": {
      "id": 10,
      "nickname": "콩이맘",
      "profileImageUrl": "..."
    },
    "viewCount": 235,
    "likeCount": 42,
    "commentCount": 12,
    "isLiked": true,
    "isOwner": false,
    "createdAt": "2026-02-06T15:30:00",
    "updatedAt": "2026-02-06T15:30:00"
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**: `isOwner`는 인증된 사용자가 게시글 작성자인지 여부입니다. 비인증 시 `false`입니다.

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
```

---

## 5. 게시글 수정

```
PUT /api/v1/community/posts/{id}
```

**권한**: 인증 필수 (본인 게시글만)

**Request Body**:

```json
{
  "category": "TIP",
  "title": "소형견 데이케어 이용 꿀팁 (수정)",
  "content": "수정된 내용입니다...",
  "imageUrls": [
    "https://storage.example.com/community/upload/img1.jpg"
  ]
}
```

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "category": "TIP",
    "categoryName": "팁",
    "title": "소형견 데이케어 이용 꿀팁 (수정)",
    "content": "수정된 내용입니다...",
    "imageUrls": [
      "https://storage.example.com/community/upload/img1.jpg"
    ],
    "author": {
      "id": 10,
      "nickname": "콩이맘",
      "profileImageUrl": "..."
    },
    "viewCount": 235,
    "likeCount": 42,
    "commentCount": 12,
    "createdAt": "2026-02-06T15:30:00",
    "updatedAt": "2026-02-07T10:05:00"
  },
  "timestamp": "2026-02-07T10:05:00Z"
}
```

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
{ "code": "UNAUTHORIZED_POST_ACCESS", "message": "해당 게시글에 대한 권한이 없습니다." }
```

---

## 6. 게시글 삭제

```
DELETE /api/v1/community/posts/{id}
```

**권한**: 인증 필수 (본인 게시글만)

**Response** (204 No Content): 응답 본문 없음

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
{ "code": "UNAUTHORIZED_POST_ACCESS", "message": "해당 게시글에 대한 권한이 없습니다." }
```

---

## 7. 좋아요 토글

```
POST /api/v1/community/posts/{id}/like
```

**권한**: 인증 필수

**Request Body**: 없음

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "postId": 1,
    "isLiked": true,
    "likeCount": 43
  },
  "timestamp": "2026-02-07T10:10:00Z"
}
```

> **동작**: 좋아요가 없으면 추가(isLiked=true, likeCount+1), 이미 있으면 삭제(isLiked=false, likeCount-1)

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
```

---

## 8. 댓글 목록

```
GET /api/v1/community/posts/{id}/comments?page=0&size=20
```

**권한**: Public

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
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
        "author": {
          "id": 11,
          "nickname": "두부맘",
          "profileImageUrl": "..."
        },
        "content": "정말 유용한 정보네요! 감사합니다.",
        "isDeleted": false,
        "isOwner": false,
        "createdAt": "2026-02-06T16:00:00",
        "updatedAt": "2026-02-06T16:00:00",
        "replies": [
          {
            "id": 3,
            "parentId": 1,
            "author": {
              "id": 10,
              "nickname": "콩이맘",
              "profileImageUrl": "..."
            },
            "content": "도움이 되셨다니 기쁩니다!",
            "isDeleted": false,
            "isOwner": false,
            "createdAt": "2026-02-06T16:30:00",
            "updatedAt": "2026-02-06T16:30:00"
          }
        ]
      },
      {
        "id": 2,
        "author": {
          "id": 12,
          "nickname": "초코아빠",
          "profileImageUrl": "..."
        },
        "content": null,
        "isDeleted": true,
        "isOwner": false,
        "createdAt": "2026-02-06T16:10:00",
        "updatedAt": "2026-02-06T17:00:00",
        "replies": []
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 12,
      "totalPages": 1
    }
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**:
> - 댓글 목록은 1단계 댓글만 페이지네이션하며, 각 댓글의 `replies`에 대댓글을 포함합니다.
> - 삭제된 댓글(isDeleted=true)의 content는 `null`이며, 프론트엔드에서 "삭제된 댓글입니다"로 표시합니다.
> - 삭제된 댓글의 대댓글이 존재하는 경우 댓글 자체는 유지하되 content만 null로 반환합니다.

---

## 9. 댓글 작성

```
POST /api/v1/community/posts/{id}/comments
```

**권한**: 인증 필수

**Request Body**:

```json
{
  "content": "정말 유용한 정보네요! 감사합니다.",
  "parentId": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| content | String | 필수 | 댓글 내용 (1~500자) |
| parentId | Long | 선택 | 대댓글인 경우 상위 댓글 ID |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 4,
    "postId": 1,
    "parentId": null,
    "author": {
      "id": 11,
      "nickname": "두부맘",
      "profileImageUrl": "..."
    },
    "content": "정말 유용한 정보네요! 감사합니다.",
    "createdAt": "2026-02-07T10:15:00"
  },
  "timestamp": "2026-02-07T10:15:00Z"
}
```

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
{ "code": "INVALID_COMMENT_CONTENT", "message": "댓글 내용을 입력해주세요. (1~500자)" }
{ "code": "COMMENT_NOT_FOUND", "message": "댓글을 찾을 수 없습니다." }
{ "code": "INVALID_REPLY_DEPTH", "message": "대댓글에는 답글을 작성할 수 없습니다." }
```

---

## 10. 댓글 수정

```
PUT /api/v1/community/comments/{id}
```

**권한**: 인증 필수 (본인 댓글만)

**Request Body**:

```json
{
  "content": "수정된 댓글 내용입니다."
}
```

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 4,
    "postId": 1,
    "parentId": null,
    "author": {
      "id": 11,
      "nickname": "두부맘",
      "profileImageUrl": "..."
    },
    "content": "수정된 댓글 내용입니다.",
    "createdAt": "2026-02-07T10:15:00",
    "updatedAt": "2026-02-07T10:20:00"
  },
  "timestamp": "2026-02-07T10:20:00Z"
}
```

**에러 응답**:

```json
{ "code": "COMMENT_NOT_FOUND", "message": "댓글을 찾을 수 없습니다." }
{ "code": "UNAUTHORIZED_COMMENT_ACCESS", "message": "해당 댓글에 대한 권한이 없습니다." }
```

---

## 11. 댓글 삭제

```
DELETE /api/v1/community/comments/{id}
```

**권한**: 인증 필수 (본인 댓글만)

**Response** (204 No Content): 응답 본문 없음

> **동작**: Soft Delete (isDeleted = true, content는 DB에 유지). 삭제 후 post의 commentCount 감소.

**에러 응답**:

```json
{ "code": "COMMENT_NOT_FOUND", "message": "댓글을 찾을 수 없습니다." }
{ "code": "UNAUTHORIZED_COMMENT_ACCESS", "message": "해당 댓글에 대한 권한이 없습니다." }
```

---

## 12. 게시글 신고

```
POST /api/v1/community/posts/{id}/report
```

**권한**: 인증 필수

**Request Body**:

```json
{
  "reason": "부적절한 내용이 포함되어 있습니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| reason | String | 필수 | 신고 사유 (1~500자) |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "postId": 1,
    "status": "PENDING",
    "message": "신고가 접수되었습니다."
  },
  "timestamp": "2026-02-07T10:25:00Z"
}
```

**에러 응답**:

```json
{ "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." }
{ "code": "CANNOT_REPORT_OWN_POST", "message": "본인의 게시글은 신고할 수 없습니다." }
{ "code": "ALREADY_REPORTED", "message": "이미 신고한 게시글입니다." }
```

---

## 13. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/community/posts` | GET | Public |
| `/api/v1/community/posts/{id}` | GET | Public |
| `/api/v1/community/posts/{id}/comments` | GET | Public |
| `/api/v1/community/posts` | POST | Authenticated |
| `/api/v1/community/posts/{id}` | PUT, DELETE | Authenticated |
| `/api/v1/community/posts/{id}/like` | POST | Authenticated |
| `/api/v1/community/posts/{id}/comments` | POST | Authenticated |
| `/api/v1/community/comments/{id}` | PUT, DELETE | Authenticated |
| `/api/v1/community/posts/{id}/report` | POST | Authenticated |
