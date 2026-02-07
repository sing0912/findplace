# 정산 (Payout) - API 명세

**최종 수정일:** 2026-02-07
**상태:** 확정

---

## API 목록 요약

### 시터(PARTNER) API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/partner/payouts | 정산 내역 조회 | PARTNER |
| GET | /api/v1/partner/payouts/summary | 수익 현황 요약 | PARTNER |
| GET | /api/v1/partner/bank-accounts | 계좌 목록 조회 | PARTNER |
| POST | /api/v1/partner/bank-accounts | 계좌 등록 | PARTNER |
| PUT | /api/v1/partner/bank-accounts/{id} | 계좌 수정 | PARTNER |
| DELETE | /api/v1/partner/bank-accounts/{id} | 계좌 삭제 | PARTNER |

### 관리자(ADMIN) API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/admin/payouts | 정산 대기 조회 | ADMIN, SUPER_ADMIN |
| POST | /api/v1/admin/payouts/process | 일괄 정산 처리 | SUPER_ADMIN |
| GET | /api/v1/admin/payouts/export | CSV/Excel 다운로드 | ADMIN, SUPER_ADMIN |
| PUT | /api/v1/admin/fee-rate | 수수료율 설정 | SUPER_ADMIN |

---

## 시터 API 상세

### 1. 정산 내역 조회

```
GET /api/v1/partner/payouts
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| status | String | N | 정산 상태 필터 (PENDING/PROCESSING/COMPLETED/FAILED) | 전체 |
| startDate | LocalDate | N | 조회 시작일 | - |
| endDate | LocalDate | N | 조회 종료일 | - |
| page | Integer | N | 페이지 번호 | 0 |
| size | Integer | N | 페이지 크기 | 20 |

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "payoutNumber": "PO-2026020701-001",
      "bookingId": 100,
      "bookingAmount": 50000,
      "platformFeeRate": 0.15,
      "platformFeeAmount": 7500,
      "payoutAmount": 42500,
      "status": "COMPLETED",
      "processedAt": "2026-02-07T10:30:00",
      "createdAt": "2026-02-03T00:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

---

### 2. 수익 현황 요약

```
GET /api/v1/partner/payouts/summary
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| period | String | N | 기간 (WEEK/MONTH/YEAR) | MONTH |

**Response (200 OK):**

```json
{
  "totalEarnings": 1250000,
  "totalFees": 187500,
  "totalPayout": 1062500,
  "pendingAmount": 42500,
  "completedCount": 25,
  "pendingCount": 1,
  "period": "MONTH",
  "periodStart": "2026-01-01",
  "periodEnd": "2026-01-31"
}
```

---

### 3. 계좌 목록 조회

```
GET /api/v1/partner/bank-accounts
```

**Response (200 OK):**

```json
{
  "accounts": [
    {
      "id": 1,
      "bankName": "국민은행",
      "accountNumber": "****-**-****-123",
      "accountHolder": "홍길동",
      "isDefault": true,
      "isVerified": true,
      "createdAt": "2026-01-15T10:00:00"
    }
  ]
}
```

> 계좌번호는 마스킹 처리하여 응답합니다.

---

### 4. 계좌 등록

```
POST /api/v1/partner/bank-accounts
```

**Request Body:**

```json
{
  "bankName": "국민은행",
  "accountNumber": "123-45-6789-123",
  "accountHolder": "홍길동",
  "isDefault": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| bankName | String | Y | 은행명 |
| accountNumber | String | Y | 계좌번호 |
| accountHolder | String | Y | 예금주 |
| isDefault | Boolean | N | 기본 계좌 여부 (기본값: false) |

**Response (201 Created):**

```json
{
  "id": 1,
  "bankName": "국민은행",
  "accountNumber": "****-**-****-123",
  "accountHolder": "홍길동",
  "isDefault": true,
  "isVerified": false,
  "createdAt": "2026-02-07T10:00:00"
}
```

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| BANK_ACCOUNT_LIMIT_EXCEEDED | 400 | 계좌 등록 한도 초과 (최대 5개) |

---

### 5. 계좌 수정

```
PUT /api/v1/partner/bank-accounts/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | 계좌 ID |

**Request Body:**

```json
{
  "bankName": "신한은행",
  "accountNumber": "110-123-456789",
  "accountHolder": "홍길동",
  "isDefault": true
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "bankName": "신한은행",
  "accountNumber": "***-***-***789",
  "accountHolder": "홍길동",
  "isDefault": true,
  "isVerified": false,
  "updatedAt": "2026-02-07T11:00:00"
}
```

> 계좌번호 변경 시 isVerified가 false로 초기화됩니다.

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| BANK_ACCOUNT_NOT_FOUND | 404 | 계좌를 찾을 수 없음 |
| BANK_ACCOUNT_ACCESS_DENIED | 403 | 본인 계좌가 아님 |

---

### 6. 계좌 삭제

```
DELETE /api/v1/partner/bank-accounts/{id}
```

**Path Parameters:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | Long | 계좌 ID |

**Response (204 No Content)**

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| BANK_ACCOUNT_NOT_FOUND | 404 | 계좌를 찾을 수 없음 |
| BANK_ACCOUNT_ACCESS_DENIED | 403 | 본인 계좌가 아님 |
| BANK_ACCOUNT_DELETE_DENIED | 400 | 정산 대기 중인 건이 있어 기본 계좌 삭제 불가 |

---

## 관리자 API 상세

### 1. 정산 대기 조회

```
GET /api/v1/admin/payouts
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| status | String | N | 정산 상태 필터 | 전체 |
| partnerId | Long | N | 시터 ID 필터 | - |
| startDate | LocalDate | N | 조회 시작일 | - |
| endDate | LocalDate | N | 조회 종료일 | - |
| page | Integer | N | 페이지 번호 | 0 |
| size | Integer | N | 페이지 크기 | 20 |

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 1,
      "payoutNumber": "PO-2026020701-001",
      "partnerId": 10,
      "partnerName": "김시터",
      "bookingId": 100,
      "bookingAmount": 50000,
      "platformFeeRate": 0.15,
      "platformFeeAmount": 7500,
      "payoutAmount": 42500,
      "status": "PENDING",
      "bankName": "국민은행",
      "accountNumber": "****-**-****-123",
      "createdAt": "2026-02-03T00:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 15,
  "totalPages": 1
}
```

---

### 2. 일괄 정산 처리

```
POST /api/v1/admin/payouts/process
```

**Request Body:**

```json
{
  "payoutIds": [1, 2, 3, 5, 8]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| payoutIds | List\<Long\> | Y | 처리할 정산 ID 목록 |

**Response (200 OK):**

```json
{
  "processedCount": 5,
  "totalAmount": 212500,
  "processedAt": "2026-02-07T14:00:00"
}
```

> PENDING 상태의 정산 건만 처리 가능합니다.

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| PAYOUT_NOT_PENDING | 400 | PENDING 상태가 아닌 정산 건 포함 |
| PAYOUT_UNVERIFIED_ACCOUNT | 400 | 미인증 계좌를 가진 시터 포함 |

---

### 3. CSV/Excel 다운로드

```
GET /api/v1/admin/payouts/export
```

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|----------|------|------|------|--------|
| format | String | N | 다운로드 형식 (CSV/EXCEL) | CSV |
| status | String | N | 정산 상태 필터 | 전체 |
| startDate | LocalDate | N | 조회 시작일 | - |
| endDate | LocalDate | N | 조회 종료일 | - |

**Response:** 파일 다운로드 (Content-Type: application/octet-stream)

**파일 컬럼:**

| 컬럼 | 설명 |
|------|------|
| 정산번호 | payoutNumber |
| 시터명 | partnerName |
| 예약번호 | bookingId |
| 예약금액 | bookingAmount |
| 수수료율 | platformFeeRate |
| 수수료금액 | platformFeeAmount |
| 정산금액 | payoutAmount |
| 상태 | status |
| 은행명 | bankName |
| 계좌번호 | accountNumber (마스킹) |
| 처리일시 | processedAt |
| 생성일시 | createdAt |

---

### 4. 수수료율 설정

```
PUT /api/v1/admin/fee-rate
```

**Request Body:**

```json
{
  "feeRate": 0.15
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| feeRate | BigDecimal | Y | 수수료율 (0.01~0.50) |

**Response (200 OK):**

```json
{
  "feeRate": 0.15,
  "updatedAt": "2026-02-07T14:00:00",
  "updatedBy": "admin@petpro.com"
}
```

> 변경된 수수료율은 이후 새로 생성되는 Payout에만 적용됩니다.

**에러:**

| 코드 | 상태 | 설명 |
|------|------|------|
| INVALID_FEE_RATE | 400 | 수수료율 범위 초과 (0.01~0.50) |
