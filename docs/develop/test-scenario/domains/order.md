# Order 도메인 테스트 시나리오

## 의존성

```
user → product → inventory → order
         ↑
      supplier
```

**Dependencies:** `["user", "supplier", "product", "inventory"]`

---

## 사전 조건

시나리오 실행 전 컨텍스트에 다음이 존재해야 함:

| 키 | 타입 | 설명 |
|----|------|------|
| user | UserResponse | 주문자 |
| supplier | SupplierResponse | 공급사 |
| product | ProductResponse | 상품 (ACTIVE 상태) |
| inventory | InventoryResponse | 재고 (quantity > 0) |

---

## CRUD 시나리오

### 1. Create - 주문 생성

**엔드포인트:** `POST /api/v1/orders`

**인증:** CUSTOMER

**요청:**
```json
{
  "items": [
    {
      "productId": "{product.id}",
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "recipientName": "홍길동",
    "phone": "010-1234-5678",
    "zipCode": "12345",
    "address": "서울시 강남구",
    "addressDetail": "101호"
  }
}
```

**예상 응답:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD20260124XXXXXXXX",
    "status": "PENDING",
    "totalAmount": 100000,
    "items": [
      {
        "productId": 1,
        "productName": "테스트 상품",
        "quantity": 2,
        "price": 50000,
        "amount": 100000
      }
    ]
  }
}
```

**검증:**
- orderNumber가 ORD + 날짜로 시작
- status가 PENDING
- totalAmount가 상품가격 × 수량
- 재고 reservedQuantity 증가 확인

---

### 2. Read - 주문 조회

**엔드포인트:** `GET /api/v1/orders/{id}`

**인증:** CUSTOMER (본인) 또는 ADMIN

**예상 응답:** `200 OK`

**검증:**
- 생성된 주문 정보와 일치
- items 포함

---

### 3. Update - 주문 수정

**엔드포인트:** `PUT /api/v1/orders/{id}`

**인증:** PLATFORM_ADMIN

**요청:**
```json
{
  "memo": "빠른 배송 부탁드립니다."
}
```

**예상 응답:** `200 OK`

---

### 4. Delete - 주문 삭제

**엔드포인트:** `DELETE /api/v1/orders/{id}`

**인증:** PLATFORM_ADMIN

**예상 응답:** `204 No Content`

---

## 커스텀 시나리오

### 5. 주문 취소 (PENDING 상태)

**엔드포인트:** `PUT /api/v1/orders/{id}/cancel`

**인증:** CUSTOMER (본인)

**예상 응답:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CANCELLED"
  }
}
```

**검증:**
- status가 CANCELLED
- 재고 reservedQuantity 복구 확인

---

### 6. 재고 부족 시 주문 실패

**엔드포인트:** `POST /api/v1/orders`

**요청:** quantity가 재고보다 큰 경우

**예상 응답:** `400 Bad Request`
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_OUT_OF_STOCK",
    "message": "재고가 부족합니다."
  }
}
```

---

### 7. 배송중 상태에서 취소 불가

**사전 조건:** status가 SHIPPED

**엔드포인트:** `PUT /api/v1/orders/{id}/cancel`

**예상 응답:** `400 Bad Request`
```json
{
  "success": false,
  "error": {
    "code": "ORDER_CANNOT_CANCEL",
    "message": "배송중인 주문은 취소할 수 없습니다."
  }
}
```

---

### 8. 주문 상태 변경 플로우

```
PENDING → PAID → PREPARING → SHIPPED → DELIVERED
```

**시나리오:**

1. 결제 완료 (PENDING → PAID)
2. 상품 준비 (PAID → PREPARING)
3. 배송 시작 (PREPARING → SHIPPED)
4. 배송 완료 (SHIPPED → DELIVERED)

---

### 9. 내 주문 목록 조회

**엔드포인트:** `GET /api/v1/orders?page=0&size=10`

**인증:** CUSTOMER

**예상 응답:** `200 OK`

**검증:**
- 본인 주문만 조회됨
- 페이지네이션 정보 포함

---

## 권한별 테스트

| 시나리오 | CUSTOMER | SUPPLIER_ADMIN | PLATFORM_ADMIN |
|----------|----------|----------------|----------------|
| 주문 생성 | ✅ 201 | ❌ 403 | ✅ 201 |
| 본인 주문 조회 | ✅ 200 | ❌ 403 | ✅ 200 |
| 타인 주문 조회 | ❌ 403 | ❌ 403 | ✅ 200 |
| 본인 주문 취소 | ✅ 200 | ❌ 403 | ✅ 200 |
| 상태 변경 | ❌ 403 | ✅ 200 (배송) | ✅ 200 |
| 전체 목록 | ❌ 403 | ❌ 403 | ✅ 200 |

---

## 테스트 데이터

### OrderFixture

```java
public class OrderFixture {

    public static OrderCreateRequest createRequest(Long productId) {
        return OrderCreateRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(productId)
                    .quantity(2)
                    .build()
            ))
            .shippingAddress(ShippingAddressRequest.builder()
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 강남구")
                .addressDetail("101호")
                .build())
            .build();
    }

    public static OrderCreateRequest createRequestWithQuantity(Long productId, int quantity) {
        return OrderCreateRequest.builder()
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build()
            ))
            .shippingAddress(createShippingAddress())
            .build();
    }

    private static ShippingAddressRequest createShippingAddress() {
        return ShippingAddressRequest.builder()
            .recipientName("홍길동")
            .phone("010-1234-5678")
            .zipCode("12345")
            .address("서울시 강남구")
            .addressDetail("101호")
            .build();
    }
}
```

---

## 구현 코드

```java
@Component
public class OrderScenario implements DomainScenario {

    @Override
    public String getDomain() {
        return "order";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("user", "supplier", "product", "inventory");
    }

    @Override
    public ScenarioStep createStep() {
        return ScenarioStep.builder()
            .name("주문 생성")
            .method(HttpMethod.POST)
            .endpoint("/api/v1/orders")
            .requestBodyBuilder(ctx -> {
                ProductResponse product = ctx.get("product", ProductResponse.class);
                return OrderFixture.createRequest(product.getId());
            })
            .authTokenProvider(ctx -> ctx.getToken("CUSTOMER"))
            .expectedStatus(HttpStatus.CREATED)
            .validator(response -> {
                OrderResponse order = (OrderResponse) response.getData();
                assertThat(order.getId()).isNotNull();
                assertThat(order.getOrderNumber()).startsWith("ORD");
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
                assertThat(order.getItems()).hasSize(1);
            })
            .contextKey("order")
            .build();
    }

    @Override
    public ScenarioStep readStep() {
        return ScenarioStep.builder()
            .name("주문 조회")
            .method(HttpMethod.GET)
            .endpoint("/api/v1/orders/{id}")
            .pathVariable("id", ctx -> ctx.get("order", OrderResponse.class).getId())
            .authTokenProvider(ctx -> ctx.getToken("CUSTOMER"))
            .expectedStatus(HttpStatus.OK)
            .build();
    }

    @Override
    public ScenarioStep updateStep() {
        return ScenarioStep.builder()
            .name("주문 수정")
            .method(HttpMethod.PUT)
            .endpoint("/api/v1/orders/{id}")
            .pathVariable("id", ctx -> ctx.get("order", OrderResponse.class).getId())
            .requestBody(Map.of("memo", "빠른 배송 부탁"))
            .authTokenProvider(ctx -> ctx.getToken("PLATFORM_ADMIN"))
            .expectedStatus(HttpStatus.OK)
            .build();
    }

    @Override
    public ScenarioStep deleteStep() {
        return ScenarioStep.builder()
            .name("주문 삭제")
            .method(HttpMethod.DELETE)
            .endpoint("/api/v1/orders/{id}")
            .pathVariable("id", ctx -> ctx.get("order", OrderResponse.class).getId())
            .authTokenProvider(ctx -> ctx.getToken("PLATFORM_ADMIN"))
            .expectedStatus(HttpStatus.NO_CONTENT)
            .build();
    }

    @Override
    public List<ScenarioStep> customSteps() {
        return List.of(
            // 주문 취소
            ScenarioStep.builder()
                .name("주문 취소")
                .method(HttpMethod.PUT)
                .endpoint("/api/v1/orders/{id}/cancel")
                .pathVariable("id", ctx -> ctx.get("order", OrderResponse.class).getId())
                .authTokenProvider(ctx -> ctx.getToken("CUSTOMER"))
                .expectedStatus(HttpStatus.OK)
                .validator(response -> {
                    OrderResponse order = (OrderResponse) response.getData();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                })
                .build(),

            // 재고 부족 테스트
            ScenarioStep.builder()
                .name("재고 부족 시 주문 실패")
                .method(HttpMethod.POST)
                .endpoint("/api/v1/orders")
                .requestBodyBuilder(ctx -> {
                    ProductResponse product = ctx.get("product", ProductResponse.class);
                    return OrderFixture.createRequestWithQuantity(product.getId(), 99999);
                })
                .authTokenProvider(ctx -> ctx.getToken("CUSTOMER"))
                .expectedStatus(HttpStatus.BAD_REQUEST)
                .validator(response -> {
                    assertThat(response.getError().getCode())
                        .isEqualTo("PRODUCT_OUT_OF_STOCK");
                })
                .build()
        );
    }
}
```

---

## 상태 전이 테스트

```java
// 별도 시나리오 클래스
@Component
public class OrderStatusScenario implements DomainScenario {

    @Override
    public String getDomain() {
        return "order-status";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("order");
    }

    @Override
    public List<ScenarioStep> customSteps() {
        return List.of(
            // PENDING → PAID
            statusChangeStep("결제 완료", OrderStatus.PAID, "CUSTOMER"),
            // PAID → PREPARING
            statusChangeStep("상품 준비", OrderStatus.PREPARING, "SUPPLIER_ADMIN"),
            // PREPARING → SHIPPED
            statusChangeStep("배송 시작", OrderStatus.SHIPPED, "SUPPLIER_ADMIN"),
            // SHIPPED → DELIVERED
            statusChangeStep("배송 완료", OrderStatus.DELIVERED, "SUPPLIER_ADMIN")
        );
    }

    private ScenarioStep statusChangeStep(String name, OrderStatus status, String role) {
        return ScenarioStep.builder()
            .name(name)
            .method(HttpMethod.PUT)
            .endpoint("/api/v1/orders/{id}/status")
            .pathVariable("id", ctx -> ctx.get("order", OrderResponse.class).getId())
            .requestBody(Map.of("status", status.name()))
            .authTokenProvider(ctx -> ctx.getToken(role))
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                assertThat(response.getData().getStatus()).isEqualTo(status);
            })
            .build();
    }
}
```
