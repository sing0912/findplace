# 재고 (Inventory)

## 개요

상품 재고를 관리하는 도메인입니다.
공급사별/창고별 재고 관리 및 이력 추적이 가능합니다.

---

## 엔티티

### Inventory

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| supplierId | Long | 공급사 ID (FK) | Not Null |
| productId | Long | 상품 ID (FK) | Not Null |
| warehouseId | Long | 창고 ID (FK) | Nullable (확장용) |
| quantity | Integer | 현재 재고 | Not Null, >= 0 |
| safetyStock | Integer | 안전 재고 | Not Null, Default 0 |
| reservedQuantity | Integer | 예약 수량 | Not Null, Default 0 |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

**가용 재고 = quantity - reservedQuantity**

### InventoryHistory

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| inventoryId | Long | 재고 ID (FK) | Not Null |
| type | Enum | 변동 유형 | Not Null |
| quantity | Integer | 변동 수량 (±) | Not Null |
| beforeQuantity | Integer | 변동 전 수량 | Not Null |
| afterQuantity | Integer | 변동 후 수량 | Not Null |
| reason | String | 변동 사유 | Nullable |
| referenceType | String | 참조 타입 | Nullable |
| referenceId | Long | 참조 ID | Nullable |
| createdBy | Long | 처리자 ID | Nullable |
| createdAt | DateTime | 생성일시 | Not Null |

### InventoryHistoryType

| 값 | 설명 |
|----|------|
| IN | 입고 |
| OUT | 출고 |
| ADJUST | 조정 |
| RESERVE | 예약 (주문 시) |
| RELEASE | 예약 해제 (취소 시) |
| TRANSFER | 이동 (창고 간) |

### Warehouse (확장용)

| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | Long | PK | Auto Increment |
| code | String | 창고 코드 | Unique, Not Null |
| name | String | 창고명 | Not Null |
| address | String | 주소 | Not Null |
| type | Enum | 창고 유형 | Not Null |
| status | Enum | 상태 | Not Null |
| createdAt | DateTime | 생성일시 | Not Null |
| updatedAt | DateTime | 수정일시 | Not Null |

### WarehouseType

| 값 | 설명 |
|----|------|
| SUPPLIER | 공급사 자체 창고 |
| PLATFORM | 플랫폼 창고 |
| FULFILLMENT | 풀필먼트 센터 (3PL) |

---

## API 목록

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | /api/v1/inventory | 목록 조회 | SUPPLIER_ADMIN, ADMIN |
| POST | /api/v1/inventory | 재고 생성 | SUPPLIER_ADMIN |
| GET | /api/v1/inventory/{id} | 상세 조회 | SUPPLIER_ADMIN (본인) |
| PUT | /api/v1/inventory/{id} | 수정 | SUPPLIER_ADMIN (본인) |
| DELETE | /api/v1/inventory/{id} | 삭제 | PLATFORM_ADMIN |
| GET | /api/v1/inventory/history | 재고 이력 | SUPPLIER_ADMIN (본인) |
| POST | /api/v1/inventory/adjust | 재고 조정 | SUPPLIER_ADMIN (본인) |
| GET | /api/v1/inventory/low-stock | 부족 재고 알림 | SUPPLIER_ADMIN |
| POST | /api/v1/inventory/transfer | 재고 이동 | SUPPLIER_ADMIN |

---

## 재고 처리 플로우

### 주문 시 (예약)

```
1. 가용 재고 확인 (quantity - reservedQuantity >= 주문수량)
2. reservedQuantity 증가
3. InventoryHistory 기록 (type: RESERVE)
```

### 출고 시

```
1. reservedQuantity 감소
2. quantity 감소
3. InventoryHistory 기록 (type: OUT)
```

### 주문 취소 시

```
1. reservedQuantity 감소
2. InventoryHistory 기록 (type: RELEASE)
```

### 입고 시

```
1. quantity 증가
2. InventoryHistory 기록 (type: IN)
```

---

## 비즈니스 규칙

1. quantity는 음수가 될 수 없음
2. 가용 재고 부족 시 주문 불가
3. 안전 재고(safetyStock) 이하로 떨어지면 알림 발송
4. 모든 재고 변동은 이력 기록 필수
5. 상품당 공급사당 1개의 재고 레코드 (창고 미사용 시)
6. 창고 사용 시 상품-공급사-창고 조합으로 관리

---

## 확장 계획

### Phase 1 (현재)
- 공급사별 단일 재고 관리
- 기본 입/출고 처리

### Phase 2 (예정)
- 창고별 재고 관리
- 창고 간 이동

### Phase 3 (예정)
- 선입선출(FIFO) 관리
- 유통기한 관리
- 로트 관리

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [crud.md](./crud.md) | 재고 CRUD |
| [adjustment.md](./adjustment.md) | 재고 조정 |
| [history.md](./history.md) | 재고 이력 |
| [warehouse/](./warehouse/) | 창고 관리 (확장) |
