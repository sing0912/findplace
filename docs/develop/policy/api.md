# 약관/정책 API 스펙

---

## 1. API 목록

### 1.1 사용자 API (Public)

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/policies/{type} | 최신 활성 약관 조회 | 불필요 |
| 2 | GET | /api/v1/policies/{type}/{date} | 특정 날짜 약관 조회 | 불필요 |
| 3 | GET | /api/v1/policies/{type}/versions | 약관 버전 히스토리 | 불필요 |

### 1.2 관리자 API

| # | Method | Endpoint | 설명 | 권한 |
|---|--------|----------|------|------|
| 4 | POST | /api/v1/admin/policies | 약관 등록 | ADMIN |
| 5 | PUT | /api/v1/admin/policies/{id} | 약관 수정 | ADMIN |
| 6 | PUT | /api/v1/admin/policies/{id}/activate | 약관 활성화 | ADMIN |

---

## 2. 최신 활성 약관 조회

```
GET /api/v1/policies/{type}
```

**권한**: Public

**Path Parameters**:

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| type | String | 약관 유형 (PRIVACY, TERMS_OF_USE, MARKETING, LOCATION) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 3,
    "policyType": "PRIVACY",
    "policyTypeName": "개인정보처리방침",
    "version": "2026-02-06",
    "title": "개인정보처리방침 (2026.02.06 시행)",
    "content": "<h1>개인정보처리방침</h1><p>PetPro(이하 '회사')는 개인정보보호법에 따라...</p>...",
    "effectiveDate": "2026-02-06",
    "createdAt": "2026-02-01T10:00:00"
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

**에러 응답**:

```json
{ "code": "INVALID_POLICY_TYPE", "message": "유효하지 않은 약관 유형입니다." }
{ "code": "POLICY_NOT_FOUND", "message": "약관을 찾을 수 없습니다." }
```

---

## 3. 특정 날짜 약관 조회

```
GET /api/v1/policies/{type}/{date}
```

**권한**: Public

**Path Parameters**:

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| type | String | 약관 유형 |
| date | String | 버전 날짜 (YYYY-MM-DD) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "policyType": "PRIVACY",
    "policyTypeName": "개인정보처리방침",
    "version": "2025-12-01",
    "title": "개인정보처리방침 (2025.12.01 시행)",
    "content": "<h1>개인정보처리방침</h1><p>이전 버전 내용...</p>...",
    "effectiveDate": "2025-12-01",
    "isActive": false,
    "createdAt": "2025-11-25T10:00:00"
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

**에러 응답**:

```json
{ "code": "INVALID_POLICY_TYPE", "message": "유효하지 않은 약관 유형입니다." }
{ "code": "INVALID_POLICY_VERSION", "message": "유효하지 않은 버전 형식입니다. (YYYY-MM-DD)" }
{ "code": "POLICY_VERSION_NOT_FOUND", "message": "해당 버전의 약관을 찾을 수 없습니다." }
```

---

## 4. 약관 버전 히스토리

```
GET /api/v1/policies/{type}/versions
```

**권한**: Public

**Path Parameters**:

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| type | String | 약관 유형 |

**Response** (200 OK):

```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "version": "2026-02-06",
      "title": "개인정보처리방침 (2026.02.06 시행)",
      "effectiveDate": "2026-02-06",
      "isActive": true,
      "createdAt": "2026-02-01T10:00:00"
    },
    {
      "id": 2,
      "version": "2026-01-15",
      "title": "개인정보처리방침 (2026.01.15 시행)",
      "effectiveDate": "2026-01-15",
      "isActive": false,
      "createdAt": "2026-01-10T10:00:00"
    },
    {
      "id": 1,
      "version": "2025-12-01",
      "title": "개인정보처리방침 (2025.12.01 시행)",
      "effectiveDate": "2025-12-01",
      "isActive": false,
      "createdAt": "2025-11-25T10:00:00"
    }
  ],
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**: 시행일(effectiveDate) 역순으로 정렬됩니다. 활성 버전은 `isActive: true`로 표시됩니다.

---

## 5. 약관 등록 (관리자)

```
POST /api/v1/admin/policies
```

**권한**: ADMIN, SUPER_ADMIN

**Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body**:

```json
{
  "policyType": "PRIVACY",
  "version": "2026-03-01",
  "title": "개인정보처리방침 (2026.03.01 시행)",
  "content": "<h1>개인정보처리방침</h1><p>신규 버전 내용...</p>...",
  "effectiveDate": "2026-03-01"
}
```

| 필드 | 타입 | 필수 | 제약조건 |
|------|------|------|----------|
| policyType | String | 필수 | PRIVACY, TERMS_OF_USE, MARKETING, LOCATION 중 하나 |
| version | String | 필수 | YYYY-MM-DD 형식 |
| title | String | 필수 | 1~200자 |
| content | String | 필수 | HTML 형식 |
| effectiveDate | String | 필수 | YYYY-MM-DD 형식 |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 4,
    "policyType": "PRIVACY",
    "policyTypeName": "개인정보처리방침",
    "version": "2026-03-01",
    "title": "개인정보처리방침 (2026.03.01 시행)",
    "effectiveDate": "2026-03-01",
    "isActive": false,
    "createdAt": "2026-02-07T10:30:00"
  },
  "timestamp": "2026-02-07T10:30:00Z"
}
```

> **참고**: 등록 시 `isActive`는 항상 `false`입니다. 별도의 활성화 API를 호출해야 합니다.

**에러 응답**:

```json
{ "code": "INVALID_POLICY_TYPE", "message": "유효하지 않은 약관 유형입니다." }
{ "code": "INVALID_POLICY_VERSION", "message": "유효하지 않은 버전 형식입니다. (YYYY-MM-DD)" }
{ "code": "POLICY_VERSION_DUPLICATE", "message": "동일 유형의 해당 날짜 약관이 이미 존재합니다." }
```

---

## 6. 약관 수정 (관리자)

```
PUT /api/v1/admin/policies/{id}
```

**권한**: ADMIN, SUPER_ADMIN

**Request Body**:

```json
{
  "title": "개인정보처리방침 (2026.03.01 시행) - 수정",
  "content": "<h1>개인정보처리방침</h1><p>수정된 내용...</p>...",
  "effectiveDate": "2026-03-01"
}
```

| 필드 | 타입 | 필수 | 제약조건 |
|------|------|------|----------|
| title | String | 선택 | 1~200자 |
| content | String | 선택 | HTML 형식 |
| effectiveDate | String | 선택 | YYYY-MM-DD 형식 |

> **참고**: `policyType`과 `version`은 수정 불가합니다. 유형/버전을 변경하려면 새 약관을 등록해야 합니다.

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 4,
    "policyType": "PRIVACY",
    "policyTypeName": "개인정보처리방침",
    "version": "2026-03-01",
    "title": "개인정보처리방침 (2026.03.01 시행) - 수정",
    "effectiveDate": "2026-03-01",
    "isActive": false,
    "updatedAt": "2026-02-07T11:00:00"
  },
  "timestamp": "2026-02-07T11:00:00Z"
}
```

**에러 응답**:

```json
{ "code": "POLICY_NOT_FOUND", "message": "약관을 찾을 수 없습니다." }
{ "code": "POLICY_ACTIVE_CANNOT_EDIT", "message": "활성화된 약관은 수정할 수 없습니다. 새 버전을 등록해주세요." }
```

---

## 7. 약관 활성화 (관리자)

```
PUT /api/v1/admin/policies/{id}/activate
```

**권한**: ADMIN, SUPER_ADMIN

**Request Body**: 없음

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 4,
    "policyType": "PRIVACY",
    "policyTypeName": "개인정보처리방침",
    "version": "2026-03-01",
    "title": "개인정보처리방침 (2026.03.01 시행)",
    "isActive": true,
    "effectiveDate": "2026-03-01",
    "message": "약관이 활성화되었습니다. 기존 활성 약관(2026-02-06)은 비활성화됩니다."
  },
  "timestamp": "2026-02-07T11:10:00Z"
}
```

> **동작**: 같은 policyType의 기존 활성 약관을 자동으로 비활성화(isActive = false)하고, 해당 약관을 활성화(isActive = true)합니다.

**에러 응답**:

```json
{ "code": "POLICY_NOT_FOUND", "message": "약관을 찾을 수 없습니다." }
{ "code": "POLICY_ALREADY_ACTIVE", "message": "이미 활성화된 약관입니다." }
```

---

## 8. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/policies/**` | GET | Public |
| `/api/v1/admin/policies` | POST | ADMIN, SUPER_ADMIN |
| `/api/v1/admin/policies/{id}` | PUT | ADMIN, SUPER_ADMIN |
| `/api/v1/admin/policies/{id}/activate` | PUT | ADMIN, SUPER_ADMIN |
