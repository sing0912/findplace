# API 컨벤션

## 개요

모든 REST API는 이 컨벤션을 따라야 합니다.

---

## URL 구조

### 기본 형식

```
/api/v{version}/{resource}
```

### Context Path 설정

**중요**: `/api` 접두사는 `application.yml`의 `server.servlet.context-path`에서 설정됩니다.

```yaml
# application.yml
server:
  servlet:
    context-path: /api
```

따라서 **컨트롤러에서는 `/api` 없이 `/v1/...`만 사용**합니다:

```java
// 올바른 예
@RequestMapping("/v1/funeral-homes")
public class FuneralHomeController { }

// 잘못된 예 (context-path와 중복됨)
@RequestMapping("/api/v1/funeral-homes")  // ❌ 실제 경로가 /api/api/v1/... 이 됨
```

### 규칙

1. **소문자 사용**: `/api/v1/users` (O), `/api/v1/Users` (X)
2. **복수형 사용**: `/api/v1/users` (O), `/api/v1/user` (X)
3. **하이픈 사용**: `/api/v1/user-profiles` (O), `/api/v1/userProfiles` (X)
4. **동사 지양**: `/api/v1/users` (O), `/api/v1/getUsers` (X)

### 중첩 리소스

```
/api/v1/{parent}/{parentId}/{child}
```

예시:
- `/api/v1/suppliers/1/products` - 공급사 1의 상품 목록
- `/api/v1/orders/1/deliveries` - 주문 1의 배송 목록

**깊이 제한**: 최대 2단계까지만 중첩

```
# Good
/api/v1/suppliers/1/products

# Bad (3단계 이상)
/api/v1/suppliers/1/products/1/inventory
→ /api/v1/inventory?productId=1 로 변경
```

---

## HTTP 메소드

| 메소드 | 용도 | 멱등성 |
|--------|------|--------|
| GET | 조회 | O |
| POST | 생성 | X |
| PUT | 전체 수정 | O |
| PATCH | 부분 수정 | O |
| DELETE | 삭제 | O |

### 예시

```
GET    /api/v1/users          # 목록 조회
GET    /api/v1/users/1        # 단건 조회
POST   /api/v1/users          # 생성
PUT    /api/v1/users/1        # 전체 수정
PATCH  /api/v1/users/1        # 부분 수정
DELETE /api/v1/users/1        # 삭제
```

---

## 요청 (Request)

### 헤더

```
Content-Type: application/json
Authorization: Bearer {token}
Accept-Language: ko-KR
```

### 쿼리 파라미터 (목록 조회)

| 파라미터 | 설명 | 기본값 |
|----------|------|--------|
| page | 페이지 번호 (0부터) | 0 |
| size | 페이지 크기 | 20 |
| sort | 정렬 (field,direction) | createdAt,desc |
| search | 검색어 | - |

예시:
```
GET /api/v1/users?page=0&size=20&sort=createdAt,desc&search=홍길동
```

### Request Body

```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678"
}
```

**규칙:**
- camelCase 사용
- null 대신 필드 생략
- 빈 배열은 `[]`로 명시

---

## 응답 (Response)

### 공통 응답 형식

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-01-24T10:30:00Z"
}
```

### 성공 응답

#### 단건 조회 (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "error": null,
  "timestamp": "2026-01-24T10:30:00Z"
}
```

#### 목록 조회 (200 OK)

```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "홍길동" },
      { "id": 2, "name": "김철수" }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5
    }
  },
  "error": null,
  "timestamp": "2026-01-24T10:30:00Z"
}
```

#### 생성 (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "error": null,
  "timestamp": "2026-01-24T10:30:00Z"
}
```

#### 삭제 (204 No Content)

```
(응답 본문 없음)
```

### 에러 응답

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다.",
    "details": [
      {
        "field": "userId",
        "message": "존재하지 않는 사용자 ID입니다."
      }
    ]
  },
  "timestamp": "2026-01-24T10:30:00Z"
}
```

---

## HTTP 상태 코드

### 성공 (2xx)

| 코드 | 설명 | 용도 |
|------|------|------|
| 200 | OK | 조회, 수정 성공 |
| 201 | Created | 생성 성공 |
| 204 | No Content | 삭제 성공 |

### 클라이언트 에러 (4xx)

| 코드 | 설명 | 용도 |
|------|------|------|
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 필요 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 충돌 (중복 등) |
| 422 | Unprocessable Entity | 유효성 검증 실패 |

### 서버 에러 (5xx)

| 코드 | 설명 | 용도 |
|------|------|------|
| 500 | Internal Server Error | 서버 오류 |
| 502 | Bad Gateway | 외부 서비스 오류 |
| 503 | Service Unavailable | 서비스 이용 불가 |

---

## 버전 관리

### URL 버전

```
/api/v1/users
/api/v2/users
```

### 버전 정책

- Major 변경 (하위 호환 X): v1 → v2
- Minor 변경 (하위 호환 O): 버전 유지
- 구 버전 최소 6개월 유지 후 deprecate

---

## 필터링 & 검색

### 필터링

```
GET /api/v1/products?status=ACTIVE&supplierId=1
```

### 범위 검색

```
GET /api/v1/orders?createdAtFrom=2026-01-01&createdAtTo=2026-01-31
GET /api/v1/products?priceMin=10000&priceMax=50000
```

### 다중 값

```
GET /api/v1/products?status=ACTIVE,DRAFT
GET /api/v1/products?categoryId=1,2,3
```

---

## 액션 API

리소스에 대한 특정 액션은 동사를 허용합니다.

```
POST /api/v1/orders/1/cancel        # 주문 취소
POST /api/v1/payments/1/refund      # 환불
PUT  /api/v1/users/1/activate       # 활성화
PUT  /api/v1/users/1/deactivate     # 비활성화
```

---

## 파일 업로드

### Multipart 요청

```
POST /api/v1/files/upload
Content-Type: multipart/form-data

file: (binary)
```

### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "url": "https://storage.example.com/files/abc123.jpg",
    "filename": "product.jpg",
    "size": 102400,
    "contentType": "image/jpeg"
  }
}
```

---

## Rate Limiting

### 헤더

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1706090400
```

### 초과 시

```
HTTP 429 Too Many Requests

{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
  }
}
```
