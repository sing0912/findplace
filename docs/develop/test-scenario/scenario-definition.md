# 시나리오 정의 방법

## 개요

각 도메인의 테스트 시나리오를 정의하는 방법을 설명합니다.
SDD(Spec-Driven Development) 방식으로 지침을 먼저 정의하고 구현합니다.

---

## 시나리오 구조

### ScenarioStep

```java
@Builder
public class ScenarioStep {

    private String name;                    // 단계 이름
    private HttpMethod method;              // HTTP 메소드
    private String endpoint;                // API 엔드포인트
    private Map<String, Function<ScenarioContext, Object>> pathVariables;  // 경로 변수
    private Map<String, Object> queryParams;    // 쿼리 파라미터
    private Object requestBody;             // 요청 본문 (정적)
    private Function<ScenarioContext, Object> requestBodyBuilder;  // 요청 본문 (동적)
    private Function<ScenarioContext, String> authTokenProvider;   // 인증 토큰
    private HttpStatus expectedStatus;      // 예상 상태 코드
    private Consumer<ApiResponse<?>> validator;  // 응답 검증
    private String contextKey;              // 컨텍스트 저장 키
    private boolean skipOnFailure;          // 실패 시 스킵
    private int retryCount;                 // 재시도 횟수
    private long delayMs;                   // 실행 전 대기 시간
}
```

---

## 시나리오 정의 단계

### 1단계: 도메인 지침 분석

도메인 README.md에서 다음을 확인:
- 엔티티 구조
- API 목록
- 비즈니스 규칙
- 의존성

### 2단계: 의존성 정의

```java
@Override
public List<String> getDependencies() {
    // 이 도메인이 의존하는 다른 도메인
    // 의존 도메인의 데이터가 먼저 생성됨
    return List.of("user", "product");
}
```

**의존성 그래프:**

```
user (Level 0)
  │
  ▼
product (Level 1) ← supplier (Level 0)
  │
  ▼
inventory (Level 2)
  │
  ▼
order (Level 3) ← user
  │
  ▼
delivery (Level 4)
```

### 3단계: CRUD 시나리오 정의

#### Create (생성)

```java
@Override
public ScenarioStep createStep() {
    return ScenarioStep.builder()
        .name("상품 생성")
        .method(HttpMethod.POST)
        .endpoint("/api/v1/products")
        // 동적 요청 본문 (컨텍스트 참조)
        .requestBodyBuilder(ctx -> ProductCreateRequest.builder()
            .supplierId(ctx.get("supplier", Supplier.class).getId())
            .categoryId(1L)
            .name("테스트 상품")
            .price(new BigDecimal("50000"))
            .costPrice(new BigDecimal("30000"))
            .deliveryType(DeliveryType.DIRECT)
            .build())
        // 공급사 관리자 권한
        .authTokenProvider(ctx -> ctx.getToken("SUPPLIER_ADMIN"))
        .expectedStatus(HttpStatus.CREATED)
        // 응답 검증
        .validator(response -> {
            ProductResponse product = (ProductResponse) response.getData();
            assertThat(product.getId()).isNotNull();
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        })
        // 컨텍스트에 저장 (다음 단계에서 사용)
        .contextKey("product")
        .build();
}
```

#### Read (조회)

```java
@Override
public ScenarioStep readStep() {
    return ScenarioStep.builder()
        .name("상품 조회")
        .method(HttpMethod.GET)
        .endpoint("/api/v1/products/{id}")
        // 경로 변수 (컨텍스트에서 가져옴)
        .pathVariable("id", ctx -> ctx.get("product", Product.class).getId())
        .expectedStatus(HttpStatus.OK)
        .validator(response -> {
            ProductResponse product = (ProductResponse) response.getData();
            assertThat(product.getName()).isEqualTo("테스트 상품");
        })
        .build();
}
```

#### Update (수정)

```java
@Override
public ScenarioStep updateStep() {
    return ScenarioStep.builder()
        .name("상품 수정")
        .method(HttpMethod.PUT)
        .endpoint("/api/v1/products/{id}")
        .pathVariable("id", ctx -> ctx.get("product", Product.class).getId())
        .requestBodyBuilder(ctx -> ProductUpdateRequest.builder()
            .name("수정된 상품명")
            .price(new BigDecimal("55000"))
            .build())
        .authTokenProvider(ctx -> ctx.getToken("SUPPLIER_ADMIN"))
        .expectedStatus(HttpStatus.OK)
        .validator(response -> {
            ProductResponse product = (ProductResponse) response.getData();
            assertThat(product.getName()).isEqualTo("수정된 상품명");
            assertThat(product.getPrice()).isEqualByComparingTo("55000");
        })
        .build();
}
```

#### Delete (삭제)

```java
@Override
public ScenarioStep deleteStep() {
    return ScenarioStep.builder()
        .name("상품 삭제")
        .method(HttpMethod.DELETE)
        .endpoint("/api/v1/products/{id}")
        .pathVariable("id", ctx -> ctx.get("product", Product.class).getId())
        .authTokenProvider(ctx -> ctx.getToken("SUPPLIER_ADMIN"))
        .expectedStatus(HttpStatus.NO_CONTENT)
        .build();
}
```

### 4단계: 커스텀 시나리오 정의

비즈니스 로직을 검증하는 추가 시나리오:

```java
@Override
public List<ScenarioStep> customSteps() {
    return List.of(
        // 상품 상태 변경
        ScenarioStep.builder()
            .name("상품 활성화")
            .method(HttpMethod.PUT)
            .endpoint("/api/v1/products/{id}/status")
            .pathVariable("id", ctx -> ctx.get("product", Product.class).getId())
            .requestBody(Map.of("status", "ACTIVE"))
            .authTokenProvider(ctx -> ctx.getToken("SUPPLIER_ADMIN"))
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                assertThat(response.getData().getStatus()).isEqualTo("ACTIVE");
            })
            .build(),

        // 목록 조회 (필터링)
        ScenarioStep.builder()
            .name("공급사별 상품 목록 조회")
            .method(HttpMethod.GET)
            .endpoint("/api/v1/suppliers/{supplierId}/products")
            .pathVariable("supplierId", ctx -> ctx.get("supplier", Supplier.class).getId())
            .queryParam("status", "ACTIVE")
            .authTokenProvider(ctx -> ctx.getToken("SUPPLIER_ADMIN"))
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                List<?> products = (List<?>) response.getData().getContent();
                assertThat(products).isNotEmpty();
            })
            .build()
    );
}
```

---

## 테스트 데이터 (Fixture)

### 정적 데이터

```java
public class ProductFixture {

    public static ProductCreateRequest createRequest() {
        return ProductCreateRequest.builder()
            .name("TEST_테스트상품")
            .price(new BigDecimal("50000"))
            .costPrice(new BigDecimal("30000"))
            .deliveryType(DeliveryType.DIRECT)
            .description("테스트용 상품입니다.")
            .build();
    }

    public static ProductUpdateRequest updateRequest() {
        return ProductUpdateRequest.builder()
            .name("TEST_수정된상품")
            .price(new BigDecimal("55000"))
            .build();
    }
}
```

### 동적 데이터 (컨텍스트 활용)

```java
.requestBodyBuilder(ctx -> {
    Supplier supplier = ctx.get("supplier", Supplier.class);
    Category category = ctx.get("category", Category.class);

    return ProductCreateRequest.builder()
        .supplierId(supplier.getId())
        .categoryId(category.getId())
        .name("TEST_" + UUID.randomUUID().toString().substring(0, 8))
        .price(randomPrice())
        .build();
})
```

---

## 검증 패턴

### 기본 검증

```java
.validator(response -> {
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).isNotNull();
})
```

### 상세 검증

```java
.validator(response -> {
    OrderResponse order = (OrderResponse) response.getData();

    // 기본 필드
    assertThat(order.getId()).isNotNull();
    assertThat(order.getOrderNumber()).startsWith("ORD");

    // 상태
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

    // 금액
    assertThat(order.getTotalAmount())
        .isGreaterThan(BigDecimal.ZERO);

    // 관계
    assertThat(order.getItems()).hasSize(1);
})
```

### 에러 검증

```java
ScenarioStep.builder()
    .name("중복 이메일로 사용자 생성 시 에러")
    .method(HttpMethod.POST)
    .endpoint("/api/v1/users")
    .requestBody(UserFixture.createRequest())  // 이미 존재하는 이메일
    .expectedStatus(HttpStatus.CONFLICT)
    .validator(response -> {
        assertThat(response.getError().getCode())
            .isEqualTo("USER_EMAIL_DUPLICATE");
    })
    .build()
```

---

## 시나리오 등록

### 자동 등록 (Component Scan)

```java
@Component
public class ProductScenario implements DomainScenario {
    // @Component로 자동 등록
}
```

### 수동 등록 (Configuration)

```java
@Configuration
public class ScenarioConfig {

    @Bean
    public ScenarioRegistry scenarioRegistry(List<DomainScenario> scenarios) {
        ScenarioRegistry registry = new ScenarioRegistry();
        scenarios.forEach(s -> registry.register(s.getDomain(), s));
        return registry;
    }
}
```

---

## 체크리스트

시나리오 정의 시 확인 사항:

- [ ] 도메인 지침(README.md) 분석 완료
- [ ] 의존성 정의
- [ ] Create 단계 정의 (생성 + 검증)
- [ ] Read 단계 정의 (조회 + 검증)
- [ ] Update 단계 정의 (수정 + 검증)
- [ ] Delete 단계 정의 (삭제)
- [ ] 커스텀 시나리오 정의 (비즈니스 로직)
- [ ] 에러 케이스 시나리오 정의
- [ ] Fixture 클래스 작성
- [ ] 권한별 테스트 포함
