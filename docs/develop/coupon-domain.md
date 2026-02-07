# 쿠폰 (Coupon)

**최종 수정일:** 2026-02-07
**상태:** 확정
**Phase:** 5 (부가)

---

## 1. 개요

이벤트 쿠폰, 친구 초대, 첫이용 할인 등 쿠폰의 생성, 발급, 사용, 만료를 관리하는 도메인입니다.

> **마이그레이션 참고**: 기존 PetPro의 주문(Order) 기반 쿠폰 시스템에서 PetPro의 예약(Booking) 기반 쿠폰 시스템으로 전환되었습니다.

### 1.1 핵심 특징

- **예약 기반**: 쿠폰은 돌봄 예약(Booking)에 적용
- **4가지 쿠폰 유형**: 이벤트, 친구 초대, 첫이용 할인, 가입 축하
- **코드 등록**: 사용자가 쿠폰 코드를 직접 입력하여 등록
- **관리자 발급**: 관리자가 쿠폰을 생성하고 발급 조건을 설정

### 1.2 관련 도메인

| 도메인 | 관계 | 설명 |
|--------|------|------|
| booking | 참조 | 쿠폰 적용 대상 (bookingId) |
| user | N:1 | 쿠폰 보유/사용 사용자 |
| payment | 참조 | 결제 시 쿠폰 할인 적용 |
| admin | 참조 | 쿠폰 생성/관리 |

---

## 2. 엔티티

### 2.1 CouponType (쿠폰 유형)

| 값 | 코드 | 설명 |
|----|------|------|
| EVENT | EVENT | 이벤트 쿠폰 (기간 한정 프로모션) |
| INVITE | INVITE | 친구 초대 쿠폰 (추천인/피추천인) |
| FIRST_USE | FIRST_USE | 첫이용 할인 쿠폰 |
| WELCOME | WELCOME | 가입 축하 쿠폰 |

### 2.2 DiscountType (할인 방식)

| 값 | 설명 |
|----|------|
| FIXED | 정액 할인 (예: 5,000원 할인) |
| PERCENT | 정률 할인 (예: 20% 할인) |

### 2.3 Coupon (쿠폰 마스터)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| code | String | 쿠폰 코드 (자동 생성 또는 관리자 지정) | Unique, Not Null, Max 20자 |
| name | String | 쿠폰명 | Not Null, Max 100자 |
| description | String | 쿠폰 설명 | Nullable, Max 500자 |
| discountType | Enum | 할인 방식 (FIXED/PERCENT) | Not Null |
| discountValue | Integer | 할인 값 (금액 또는 퍼센트) | Not Null |
| minBookingAmount | Integer | 최소 예약 금액 (이상일 때 사용 가능) | Nullable |
| maxDiscountAmount | Integer | 최대 할인 금액 (PERCENT일 때 상한) | Nullable |
| usageLimit | Integer | 전체 사용 제한 수 (null이면 무제한) | Nullable |
| usedCount | Integer | 사용된 횟수 | Not Null, Default 0 |
| couponType | Enum | 쿠폰 유형 (EVENT/INVITE/FIRST_USE/WELCOME) | Not Null |
| validFrom | LocalDateTime | 사용 시작일시 | Not Null |
| validTo | LocalDateTime | 사용 종료일시 | Not Null |
| isActive | Boolean | 활성 여부 | Not Null, Default true |
| createdAt | LocalDateTime | 생성일시 | Not Null |
| updatedAt | LocalDateTime | 수정일시 | Not Null |

### 2.4 UserCoupon (사용자 보유 쿠폰)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| userId | Long | 사용자 ID (FK -> users) | Not Null |
| couponId | Long | 쿠폰 ID (FK -> coupons) | Not Null |
| isUsed | Boolean | 사용 여부 | Not Null, Default false |
| usedAt | LocalDateTime | 사용일시 | Nullable |
| bookingId | Long | 사용된 예약 ID (FK -> bookings) | Nullable |
| createdAt | LocalDateTime | 발급일시 | Not Null |

> **Unique 제약**: (userId, couponId) 조합은 유일해야 함 (같은 쿠폰 중복 발급 방지)

---

## 3. 엔티티 관계도

```
User (1) ────────── (N) UserCoupon : 사용자 보유 쿠폰
Coupon (1) ─────── (N) UserCoupon : 쿠폰별 발급 내역
UserCoupon (N) ─── (0..1) Booking : 쿠폰 사용 예약
```

---

## 4. DDL

### 4.1 coupons

```sql
CREATE TABLE coupons (
    id                  BIGSERIAL       PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL UNIQUE,
    name                VARCHAR(100)    NOT NULL,
    description         VARCHAR(500),
    discount_type       VARCHAR(10)     NOT NULL,
    discount_value      INTEGER         NOT NULL,
    min_booking_amount  INTEGER,
    max_discount_amount INTEGER,
    usage_limit         INTEGER,
    used_count          INTEGER         NOT NULL DEFAULT 0,
    coupon_type         VARCHAR(20)     NOT NULL,
    valid_from          TIMESTAMP       NOT NULL,
    valid_to            TIMESTAMP       NOT NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 user_coupons

```sql
CREATE TABLE user_coupons (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id),
    coupon_id   BIGINT          NOT NULL REFERENCES coupons(id),
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMP,
    booking_id  BIGINT          REFERENCES bookings(id),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, coupon_id)
);
```

---

## 5. 인덱스

```sql
-- coupons
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_coupon_type ON coupons(coupon_type);
CREATE INDEX idx_coupons_valid_period ON coupons(valid_from, valid_to);
CREATE INDEX idx_coupons_is_active ON coupons(is_active);

-- user_coupons
CREATE INDEX idx_user_coupons_user_id ON user_coupons(user_id);
CREATE INDEX idx_user_coupons_coupon_id ON user_coupons(coupon_id);
CREATE INDEX idx_user_coupons_user_not_used ON user_coupons(user_id, is_used) WHERE is_used = FALSE;
CREATE INDEX idx_user_coupons_booking_id ON user_coupons(booking_id);
```

---

## 6. 비즈니스 규칙

### 6.1 쿠폰 코드 형식

| 유형 | 형식 | 예시 |
|------|------|------|
| EVENT | EVT-{영문숫자8자} | EVT-A1B2C3D4 |
| INVITE | INV-{영문숫자8자} | INV-X7Y8Z9W0 |
| FIRST_USE | FST-{영문숫자8자} | FST-P3Q4R5S6 |
| WELCOME | WLC-{영문숫자8자} | WLC-M1N2O3P4 |

### 6.2 쿠폰 등록 (코드 입력)

```
1. 쿠폰 코드 유효성 확인 (존재하는 코드인지)
2. 쿠폰 활성 상태 확인 (isActive = true)
3. 쿠폰 유효기간 확인 (validFrom <= now <= validTo)
4. 전체 사용 제한 수 확인 (usageLimit이 있으면 usedCount < usageLimit)
5. 중복 발급 확인 (userId + couponId 유니크)
6. UserCoupon 생성 (isUsed = false)
```

### 6.3 쿠폰 사용 (예약 결제 시)

```
1. UserCoupon 존재 확인
2. 사용 가능 상태 확인 (isUsed = false)
3. 쿠폰 유효기간 확인 (validFrom <= now <= validTo)
4. 최소 예약 금액 확인 (bookingAmount >= minBookingAmount)
5. 할인 금액 계산
6. UserCoupon 업데이트 (isUsed = true, usedAt = now, bookingId)
7. Coupon.usedCount 증가
```

### 6.4 할인 금액 계산

```
FIXED:
  discountAmount = discountValue

PERCENT:
  discountAmount = bookingAmount * discountValue / 100
  if (maxDiscountAmount != null && discountAmount > maxDiscountAmount):
    discountAmount = maxDiscountAmount
```

### 6.5 쿠폰 적용 조건

| 조건 | 설명 |
|------|------|
| 활성 상태 | isActive = true |
| 유효 기간 내 | validFrom <= 현재시각 <= validTo |
| 미사용 상태 | UserCoupon.isUsed = false |
| 최소 금액 | 예약 금액 >= minBookingAmount (설정된 경우) |
| 사용 한도 | usedCount < usageLimit (설정된 경우) |

### 6.6 쿠폰 유형별 발급 규칙

| 유형 | 발급 방법 | 설명 |
|------|-----------|------|
| EVENT | 코드 입력 또는 관리자 발급 | 이벤트 페이지에서 코드 입력 |
| INVITE | 자동 발급 | 친구 초대 코드로 가입 시 양쪽에 자동 발급 |
| FIRST_USE | 자동 발급 | 첫 예약 완료 시 자동 발급 |
| WELCOME | 자동 발급 | 회원가입 완료 시 자동 발급 |

---

## 7. API 명세

### 7.1 사용자 API

| # | Method | Endpoint | 설명 | 인증 |
|---|--------|----------|------|------|
| 1 | GET | /api/v1/coupons/my | 내 쿠폰 목록 | 필수 |
| 2 | GET | /api/v1/coupons/my/available | 사용 가능한 쿠폰 (예약 적용용) | 필수 |
| 3 | POST | /api/v1/coupons/redeem | 쿠폰 코드 등록 | 필수 |

### 7.2 관리자 API

| # | Method | Endpoint | 설명 | 권한 |
|---|--------|----------|------|------|
| 4 | GET | /api/v1/admin/coupons | 쿠폰 목록 | ADMIN |
| 5 | GET | /api/v1/admin/coupons/{id} | 쿠폰 상세 | ADMIN |
| 6 | POST | /api/v1/admin/coupons | 쿠폰 생성 | ADMIN |
| 7 | PUT | /api/v1/admin/coupons/{id} | 쿠폰 수정 | ADMIN |
| 8 | PUT | /api/v1/admin/coupons/{id}/deactivate | 쿠폰 비활성화 | ADMIN |

---

## 8. 사용자 API 상세

### 8.1 내 쿠폰 목록

```
GET /api/v1/coupons/my?status=AVAILABLE&page=0&size=20
```

**권한**: 인증 필수

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| status | String | N | 필터 (AVAILABLE: 사용 가능, USED: 사용 완료, EXPIRED: 만료), 기본값: 전체 |
| page | Integer | N | 페이지 번호 (기본값 0) |
| size | Integer | N | 페이지 크기 (기본값 20) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userCouponId": 1,
        "couponId": 10,
        "code": "EVT-A1B2C3D4",
        "name": "첫 이용 30% 할인",
        "description": "첫 돌봄 예약 시 30% 할인",
        "discountType": "PERCENT",
        "discountValue": 30,
        "minBookingAmount": 30000,
        "maxDiscountAmount": 15000,
        "couponType": "FIRST_USE",
        "couponTypeName": "첫이용 할인",
        "validFrom": "2026-02-01T00:00:00",
        "validTo": "2026-03-31T23:59:59",
        "isUsed": false,
        "usedAt": null,
        "status": "AVAILABLE",
        "daysLeft": 52,
        "createdAt": "2026-02-05T10:00:00"
      },
      {
        "userCouponId": 2,
        "couponId": 11,
        "code": "WLC-M1N2O3P4",
        "name": "가입 축하 5,000원 할인",
        "description": "가입을 축하합니다! 첫 예약에 사용하세요.",
        "discountType": "FIXED",
        "discountValue": 5000,
        "minBookingAmount": 20000,
        "maxDiscountAmount": null,
        "couponType": "WELCOME",
        "couponTypeName": "가입 축하",
        "validFrom": "2026-02-05T00:00:00",
        "validTo": "2026-05-05T23:59:59",
        "isUsed": true,
        "usedAt": "2026-02-06T14:30:00",
        "status": "USED",
        "daysLeft": null,
        "createdAt": "2026-02-05T09:00:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 2,
      "totalPages": 1
    }
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **status 판정 로직**:
> - `AVAILABLE`: isUsed=false AND validTo >= now
> - `USED`: isUsed=true
> - `EXPIRED`: isUsed=false AND validTo < now

### 8.2 사용 가능한 쿠폰 (예약 적용용)

```
GET /api/v1/coupons/my/available?bookingAmount=50000
```

**권한**: 인증 필수

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| bookingAmount | Integer | N | 예약 금액 (입력 시 minBookingAmount 필터 적용) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": [
    {
      "userCouponId": 1,
      "couponId": 10,
      "name": "첫 이용 30% 할인",
      "discountType": "PERCENT",
      "discountValue": 30,
      "minBookingAmount": 30000,
      "maxDiscountAmount": 15000,
      "couponType": "FIRST_USE",
      "couponTypeName": "첫이용 할인",
      "validTo": "2026-03-31T23:59:59",
      "daysLeft": 52,
      "expectedDiscount": 15000
    }
  ],
  "timestamp": "2026-02-07T10:00:00Z"
}
```

> **참고**: `expectedDiscount`는 bookingAmount를 기반으로 계산된 예상 할인 금액입니다. bookingAmount가 없으면 이 필드는 `null`입니다.

### 8.3 쿠폰 코드 등록

```
POST /api/v1/coupons/redeem
```

**권한**: 인증 필수

**Request Body**:

```json
{
  "code": "EVT-A1B2C3D4"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| code | String | 필수 | 쿠폰 코드 |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "userCouponId": 3,
    "couponId": 12,
    "name": "2월 특별 이벤트 10,000원 할인",
    "discountType": "FIXED",
    "discountValue": 10000,
    "couponType": "EVENT",
    "couponTypeName": "이벤트 쿠폰",
    "validTo": "2026-02-28T23:59:59",
    "message": "쿠폰이 등록되었습니다."
  },
  "timestamp": "2026-02-07T10:30:00Z"
}
```

**에러 응답**:

```json
{ "code": "COUPON_NOT_FOUND", "message": "유효하지 않은 쿠폰 코드입니다." }
{ "code": "COUPON_EXPIRED", "message": "사용 기간이 만료된 쿠폰입니다." }
{ "code": "COUPON_INACTIVE", "message": "현재 사용할 수 없는 쿠폰입니다." }
{ "code": "COUPON_USAGE_LIMIT_EXCEEDED", "message": "쿠폰 발급 수량이 모두 소진되었습니다." }
{ "code": "COUPON_ALREADY_OWNED", "message": "이미 보유한 쿠폰입니다." }
```

---

## 9. 관리자 API 상세

### 9.1 쿠폰 목록

```
GET /api/v1/admin/coupons?couponType=EVENT&isActive=true&page=0&size=20
```

**권한**: ADMIN, SUPER_ADMIN

**Query Parameters**:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| couponType | String | N | 쿠폰 유형 필터 |
| isActive | Boolean | N | 활성 상태 필터 |
| search | String | N | 검색어 (이름/코드) |
| page | Integer | N | 페이지 번호 (기본값 0) |
| size | Integer | N | 페이지 크기 (기본값 20) |

**Response** (200 OK):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 10,
        "code": "EVT-A1B2C3D4",
        "name": "첫 이용 30% 할인",
        "discountType": "PERCENT",
        "discountValue": 30,
        "couponType": "FIRST_USE",
        "couponTypeName": "첫이용 할인",
        "usageLimit": 1000,
        "usedCount": 234,
        "validFrom": "2026-02-01T00:00:00",
        "validTo": "2026-03-31T23:59:59",
        "isActive": true,
        "createdAt": "2026-01-30T10:00:00"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 15,
      "totalPages": 1
    }
  },
  "timestamp": "2026-02-07T10:00:00Z"
}
```

### 9.2 쿠폰 생성

```
POST /api/v1/admin/coupons
```

**권한**: ADMIN, SUPER_ADMIN

**Request Body**:

```json
{
  "code": "EVT-SPRING2026",
  "name": "봄맞이 특별 할인",
  "description": "봄맞이 돌봄 예약 20% 할인 쿠폰",
  "discountType": "PERCENT",
  "discountValue": 20,
  "minBookingAmount": 30000,
  "maxDiscountAmount": 20000,
  "usageLimit": 500,
  "couponType": "EVENT",
  "validFrom": "2026-03-01T00:00:00",
  "validTo": "2026-03-31T23:59:59"
}
```

| 필드 | 타입 | 필수 | 제약조건 |
|------|------|------|----------|
| code | String | 선택 | 미입력 시 자동 생성, Max 20자 |
| name | String | 필수 | 1~100자 |
| description | String | 선택 | Max 500자 |
| discountType | String | 필수 | FIXED 또는 PERCENT |
| discountValue | Integer | 필수 | 양수 |
| minBookingAmount | Integer | 선택 | 양수 |
| maxDiscountAmount | Integer | 선택 | 양수 (PERCENT일 때 권장) |
| usageLimit | Integer | 선택 | 양수 (null이면 무제한) |
| couponType | String | 필수 | EVENT, INVITE, FIRST_USE, WELCOME |
| validFrom | String | 필수 | ISO 8601 형식 |
| validTo | String | 필수 | ISO 8601 형식, validFrom 이후 |

**Response** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 15,
    "code": "EVT-SPRING2026",
    "name": "봄맞이 특별 할인",
    "discountType": "PERCENT",
    "discountValue": 20,
    "couponType": "EVENT",
    "isActive": true,
    "createdAt": "2026-02-07T11:00:00"
  },
  "timestamp": "2026-02-07T11:00:00Z"
}
```

**에러 응답**:

```json
{ "code": "COUPON_CODE_DUPLICATE", "message": "이미 존재하는 쿠폰 코드입니다." }
{ "code": "INVALID_COUPON_PERIOD", "message": "종료일은 시작일 이후여야 합니다." }
{ "code": "INVALID_DISCOUNT_VALUE", "message": "할인 값은 양수여야 합니다." }
```

---

## 10. 에러 코드 전체

| 코드 | HTTP | 메시지 |
|------|------|--------|
| COUPON_NOT_FOUND | 404 | 유효하지 않은 쿠폰 코드입니다. |
| COUPON_EXPIRED | 400 | 사용 기간이 만료된 쿠폰입니다. |
| COUPON_INACTIVE | 400 | 현재 사용할 수 없는 쿠폰입니다. |
| COUPON_USAGE_LIMIT_EXCEEDED | 400 | 쿠폰 발급 수량이 모두 소진되었습니다. |
| COUPON_ALREADY_OWNED | 409 | 이미 보유한 쿠폰입니다. |
| COUPON_ALREADY_USED | 400 | 이미 사용된 쿠폰입니다. |
| COUPON_MIN_AMOUNT_NOT_MET | 400 | 최소 예약 금액 조건을 충족하지 않습니다. |
| COUPON_CODE_DUPLICATE | 409 | 이미 존재하는 쿠폰 코드입니다. |
| INVALID_COUPON_PERIOD | 400 | 종료일은 시작일 이후여야 합니다. |
| INVALID_DISCOUNT_VALUE | 400 | 할인 값은 양수여야 합니다. |
| USER_COUPON_NOT_FOUND | 404 | 보유하지 않은 쿠폰입니다. |

---

## 11. 패키지 구조

```
backend/src/main/java/com/petpro/domain/coupon/
├── entity/
│   ├── Coupon.java
│   ├── UserCoupon.java
│   ├── CouponType.java             # Enum
│   └── DiscountType.java           # Enum
├── repository/
│   ├── CouponRepository.java
│   └── UserCouponRepository.java
├── service/
│   ├── CouponService.java          # 사용자 서비스
│   ├── CouponRedeemService.java    # 코드 등록 서비스
│   └── AdminCouponService.java     # 관리자 서비스
├── controller/
│   ├── CouponController.java       # 사용자 API
│   └── AdminCouponController.java  # 관리자 API
└── dto/
    ├── CouponRequest.java
    ├── CouponResponse.java
    ├── UserCouponResponse.java
    ├── CouponRedeemRequest.java
    └── AvailableCouponResponse.java
```

---

## 12. 프론트엔드

### 12.1 쿠폰함 UI

**접근 경로**: 마이 > 쿠폰함

```
┌─────────────────────────────────┐
│ ← 뒤로가기        쿠폰함        │
├─────────────────────────────────┤
│                                  │
│ 쿠폰 코드 입력                   │
│ ┌──────────────────────┐ [등록] │
│ │ 쿠폰 코드를 입력하세요│        │
│ └──────────────────────┘        │
│                                  │
│ [사용 가능(2)] [사용 완료] [만료]│
├─────────────────────────────────┤
│                                  │
│ ┌──────────────────────────────┐│
│ │ [이벤트]                      ││
│ │ 첫 이용 30% 할인              ││
│ │ 30,000원 이상 예약 시 | 최대15,000원│
│ │ ~2026.03.31           52일 남음│
│ └──────────────────────────────┘│
│                                  │
│ ┌──────────────────────────────┐│
│ │ [가입 축하]                   ││
│ │ 가입 축하 5,000원 할인        ││
│ │ 20,000원 이상 예약 시          ││
│ │ ~2026.05.05           87일 남음│
│ └──────────────────────────────┘│
│                                  │
└─────────────────────────────────┘
```

### 12.2 쿠폰 적용 UI (예약 결제 시)

**접근 경로**: 예약 > 결제 화면 > 쿠폰 적용

```
┌─────────────────────────────────┐
│ ← 뒤로가기    쿠폰 선택         │
├─────────────────────────────────┤
│ 예약 금액: 50,000원              │
├─────────────────────────────────┤
│                                  │
│ ┌──────────────────────────────┐│
│ │ ○ 첫 이용 30% 할인           ││
│ │   -15,000원 할인              ││
│ │   ~2026.03.31                 ││
│ └──────────────────────────────┘│
│                                  │
│ ┌──────────────────────────────┐│
│ │ ○ 가입 축하 5,000원 할인     ││
│ │   -5,000원 할인               ││
│ │   ~2026.05.05                 ││
│ └──────────────────────────────┘│
│                                  │
│ ┌──────────────────────────────┐│
│ │ ✕ 10,000원 할인 (사용 불가)  ││
│ │   최소 예약 금액: 80,000원    ││
│ └──────────────────────────────┘│
│                                  │
│          [적용하기]              │
└─────────────────────────────────┘
```

### 12.3 파일 구조

```
frontend/src/
├── types/coupon.ts                  # 쿠폰 타입 정의
├── api/coupon.ts                    # 쿠폰 API 서비스
├── hooks/
│   ├── useMyCoupons.ts             # 내 쿠폰 목록 훅
│   └── useAvailableCoupons.ts      # 사용 가능한 쿠폰 훅
├── components/coupon/
│   ├── CouponCard.tsx              # 쿠폰 카드
│   ├── CouponCodeInput.tsx         # 쿠폰 코드 입력
│   ├── CouponStatusTabs.tsx        # 상태 탭 (사용가능/완료/만료)
│   └── CouponSelectModal.tsx       # 쿠폰 선택 모달 (결제 시)
└── pages/coupon/
    └── CouponListPage.tsx           # 쿠폰함 페이지
```

### 12.4 타입 정의 (types/coupon.ts)

```typescript
type CouponType = 'EVENT' | 'INVITE' | 'FIRST_USE' | 'WELCOME';
type DiscountType = 'FIXED' | 'PERCENT';
type CouponStatus = 'AVAILABLE' | 'USED' | 'EXPIRED';

interface UserCouponItem {
  userCouponId: number;
  couponId: number;
  code: string;
  name: string;
  description: string | null;
  discountType: DiscountType;
  discountValue: number;
  minBookingAmount: number | null;
  maxDiscountAmount: number | null;
  couponType: CouponType;
  couponTypeName: string;
  validFrom: string;
  validTo: string;
  isUsed: boolean;
  usedAt: string | null;
  status: CouponStatus;
  daysLeft: number | null;
  createdAt: string;
}

interface AvailableCouponItem {
  userCouponId: number;
  couponId: number;
  name: string;
  discountType: DiscountType;
  discountValue: number;
  minBookingAmount: number | null;
  maxDiscountAmount: number | null;
  couponType: CouponType;
  couponTypeName: string;
  validTo: string;
  daysLeft: number;
  expectedDiscount: number | null;
}

interface CouponRedeemRequest {
  code: string;
}
```

---

## 13. SecurityConfig 규칙

| URL 패턴 | Method | 권한 |
|----------|--------|------|
| `/api/v1/coupons/my` | GET | Authenticated |
| `/api/v1/coupons/my/available` | GET | Authenticated |
| `/api/v1/coupons/redeem` | POST | Authenticated |
| `/api/v1/admin/coupons/**` | ALL | ADMIN, SUPER_ADMIN |

---

## 14. 관련 도메인

- **Booking**: 쿠폰 적용 대상 (bookingId 참조)
- **Payment**: 결제 시 쿠폰 할인 금액 반영
- **User**: 쿠폰 보유/사용 사용자
- **Admin**: 쿠폰 생성/관리
- **MyPage**: 쿠폰함 UI
