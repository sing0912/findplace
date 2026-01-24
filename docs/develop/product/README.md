# 상품 (Product)

## 개요

공급사가 판매하는 상품 관리 도메인입니다.

---

## 엔티티

### Product

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| supplierId | Long | 공급사 ID (FK) | Not Null |
| categoryId | Long | 카테고리 ID (FK) | Not Null |
| name | String | 상품명 | Not Null |
| description | Text | 상품 설명 | Nullable |
| price | Decimal | 판매가 | Not Null |
| costPrice | Decimal | 원가 | Not Null |
| deliveryType | Enum | 배송 유형 | Not Null |
| deliveryFee | Decimal | 배송비 | Not Null, Default 0 |
| freeShippingThreshold | Decimal | 무료배송 기준금액 | Nullable |
| status | Enum | 상태 | Not Null |
| sortOrder | Integer | 정렬 순서 | Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### ProductStatus

| 값 | 설명 |
|----|------|
| DRAFT | 임시저장 |
| ACTIVE | 판매중 |
| INACTIVE | 판매중지 |
| SOLDOUT | 품절 |

### DeliveryType

| 값 | 설명 |
|----|------|
| DIRECT | 공급사 직배송 |
| PURCHASE | 사입배송 |
| WAREHOUSE | 물류창고 배송 |

### ProductImage

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| productId | Long | 상품 ID (FK) | Not Null |
| imageUrl | String | 이미지 URL | Not Null |
| sortOrder | Integer | 정렬 순서 | Default 0 |
| isMain | Boolean | 대표 이미지 여부 | Default false |

### ProductOption

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| productId | Long | 상품 ID (FK) | Not Null |
| name | String | 옵션명 | Not Null |
| additionalPrice | Decimal | 추가 가격 | Default 0 |
| status | Enum | 상태 | Not Null |

### Category

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| parentId | Long | 상위 카테고리 ID | Nullable |
| name | String | 카테고리명 | Not Null |
| depth | Integer | 깊이 | Not Null |
| sortOrder | Integer | 정렬 순서 | Default 0 |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/products | 목록 조회 | Public |
| POST | /api/v1/products | 등록 | SUPPLIER_ADMIN |
| GET | /api/v1/products/{id} | 상세 조회 | Public |
| PUT | /api/v1/products/{id} | 수정 | SUPPLIER_ADMIN (본인 상품) |
| DELETE | /api/v1/products/{id} | 삭제 | SUPPLIER_ADMIN (본인 상품) |
| PUT | /api/v1/products/{id}/status | 상태 변경 | SUPPLIER_ADMIN (본인 상품) |
| GET | /api/v1/products/categories | 카테고리 목록 | Public |
| POST | /api/v1/products/categories | 카테고리 생성 | PLATFORM_ADMIN |

---

## 배송 유형별 처리

### DIRECT (공급사 직배송)
- 공급사가 직접 배송
- 재고: 공급사 재고 차감
- 배송: 공급사 책임

### PURCHASE (사입배송)
- 플랫폼이 매입 후 배송
- 재고: 플랫폼 재고 차감
- 배송: 플랫폼 책임

### WAREHOUSE (물류창고 배송)
- 3PL 물류창고 위탁
- 재고: 창고 재고 차감
- 배송: 물류사 책임

---

## 비즈니스 규칙

1. APPROVED 상태의 공급사만 상품 등록 가능
2. 판매가 >= 원가 검증
3. 대표 이미지는 상품당 1개만 지정 가능
4. 재고가 0이 되면 자동으로 SOLDOUT 상태로 변경
5. 삭제는 Soft Delete (status = INACTIVE)

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | 상품 CRUD |
| [option.md](./option.md) | 옵션 관리 |
| [image.md](./image.md) | 이미지 관리 |
| [category.md](./category.md) | 카테고리 관리 |
| [pricing.md](./pricing.md) | 가격 정책 |
