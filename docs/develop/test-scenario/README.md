# 테스트 시나리오 미들웨어 (Test Scenario Middleware)

## 개요

API 스펙에 맞게 데이터를 순환하며 **등록 → 조회 → 수정 → 삭제**를 자동으로 수행하는 테스트 시나리오 미들웨어입니다.

SDD(Spec-Driven Development) 방식으로 지침을 기반으로 테스트 시나리오를 자동 생성하고 실행합니다.

---

## 아키텍처

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Test Scenario Middleware                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐             │
│  │  Scenario   │───▶│  Executor   │───▶│  Reporter   │             │
│  │  Registry   │    │             │    │             │             │
│  └─────────────┘    └──────┬──────┘    └─────────────┘             │
│                            │                                        │
│                     ┌──────▼──────┐                                 │
│                     │ API Client  │                                 │
│                     └──────┬──────┘                                 │
│                            │                                        │
└────────────────────────────┼────────────────────────────────────────┘
                             │
                      ┌──────▼──────┐
                      │  REST API   │
                      └─────────────┘
```

---

## 핵심 컴포넌트

### 1. Scenario Registry (시나리오 등록소)

도메인별 테스트 시나리오를 등록하고 관리합니다.

```java
@Component
public class ScenarioRegistry {

    private final Map<String, DomainScenario> scenarios = new LinkedHashMap<>();

    public void register(String domain, DomainScenario scenario) {
        scenarios.put(domain, scenario);
    }

    public List<DomainScenario> getAllScenarios() {
        return new ArrayList<>(scenarios.values());
    }

    public DomainScenario getScenario(String domain) {
        return scenarios.get(domain);
    }
}
```

### 2. Domain Scenario (도메인 시나리오)

각 도메인의 CRUD 시나리오를 정의합니다.

```java
public interface DomainScenario {

    String getDomain();

    List<String> getDependencies();  // 선행 도메인

    ScenarioStep createStep();       // 생성 단계

    ScenarioStep readStep();         // 조회 단계

    ScenarioStep updateStep();       // 수정 단계

    ScenarioStep deleteStep();       // 삭제 단계

    List<ScenarioStep> customSteps(); // 커스텀 단계
}
```

### 3. Scenario Executor (시나리오 실행기)

시나리오를 순서대로 실행합니다.

```java
@Component
public class ScenarioExecutor {

    private final ScenarioRegistry registry;
    private final ApiClient apiClient;
    private final ScenarioContext context;

    public ScenarioResult execute(ExecutionConfig config) {
        ScenarioResult result = new ScenarioResult();

        for (DomainScenario scenario : registry.getAllScenarios()) {
            if (config.isIncluded(scenario.getDomain())) {
                DomainResult domainResult = executeScenario(scenario);
                result.add(domainResult);
            }
        }

        return result;
    }

    private DomainResult executeScenario(DomainScenario scenario) {
        // 의존성 확인
        checkDependencies(scenario);

        // CRUD 순환 실행
        StepResult createResult = executeStep(scenario.createStep());
        StepResult readResult = executeStep(scenario.readStep());
        StepResult updateResult = executeStep(scenario.updateStep());
        StepResult deleteResult = executeStep(scenario.deleteStep());

        // 커스텀 단계 실행
        for (ScenarioStep step : scenario.customSteps()) {
            executeStep(step);
        }

        return new DomainResult(scenario.getDomain(), ...);
    }
}
```

### 4. Scenario Context (시나리오 컨텍스트)

시나리오 실행 중 생성된 데이터를 관리합니다.

```java
@Component
@Scope("prototype")
public class ScenarioContext {

    private final Map<String, Object> createdEntities = new HashMap<>();
    private final Map<String, String> tokens = new HashMap<>();

    public void store(String key, Object entity) {
        createdEntities.put(key, entity);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(createdEntities.get(key));
    }

    public void storeToken(String role, String token) {
        tokens.put(role, token);
    }

    public String getToken(String role) {
        return tokens.get(role);
    }
}
```

---

## 시나리오 정의 예시

### User 도메인

```java
@Component
public class UserScenario implements DomainScenario {

    @Override
    public String getDomain() {
        return "user";
    }

    @Override
    public List<String> getDependencies() {
        return List.of(); // 의존성 없음
    }

    @Override
    public ScenarioStep createStep() {
        return ScenarioStep.builder()
            .name("사용자 생성")
            .method(HttpMethod.POST)
            .endpoint("/api/v1/users")
            .requestBody(UserFixture.createUserRequest())
            .expectedStatus(HttpStatus.CREATED)
            .validator(response -> {
                assertThat(response.getData().getId()).isNotNull();
            })
            .contextKey("user")
            .build();
    }

    @Override
    public ScenarioStep readStep() {
        return ScenarioStep.builder()
            .name("사용자 조회")
            .method(HttpMethod.GET)
            .endpoint("/api/v1/users/{id}")
            .pathVariable("id", ctx -> ctx.get("user", User.class).getId())
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                assertThat(response.getData().getEmail()).isNotNull();
            })
            .build();
    }

    @Override
    public ScenarioStep updateStep() {
        return ScenarioStep.builder()
            .name("사용자 수정")
            .method(HttpMethod.PUT)
            .endpoint("/api/v1/users/{id}")
            .pathVariable("id", ctx -> ctx.get("user", User.class).getId())
            .requestBody(UserFixture.updateUserRequest())
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                assertThat(response.getData().getName()).isEqualTo("수정된이름");
            })
            .build();
    }

    @Override
    public ScenarioStep deleteStep() {
        return ScenarioStep.builder()
            .name("사용자 삭제")
            .method(HttpMethod.DELETE)
            .endpoint("/api/v1/users/{id}")
            .pathVariable("id", ctx -> ctx.get("user", User.class).getId())
            .expectedStatus(HttpStatus.NO_CONTENT)
            .build();
    }

    @Override
    public List<ScenarioStep> customSteps() {
        return List.of();
    }
}
```

### Order 도메인 (의존성 있음)

```java
@Component
public class OrderScenario implements DomainScenario {

    @Override
    public String getDomain() {
        return "order";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("user", "product", "inventory");
    }

    @Override
    public ScenarioStep createStep() {
        return ScenarioStep.builder()
            .name("주문 생성")
            .method(HttpMethod.POST)
            .endpoint("/api/v1/orders")
            .requestBody(ctx -> OrderCreateRequest.builder()
                .productId(ctx.get("product", Product.class).getId())
                .quantity(2)
                .build())
            .auth(ctx -> ctx.getToken("CUSTOMER"))
            .expectedStatus(HttpStatus.CREATED)
            .contextKey("order")
            .build();
    }

    @Override
    public List<ScenarioStep> customSteps() {
        return List.of(
            // 주문 취소 시나리오
            ScenarioStep.builder()
                .name("주문 취소")
                .method(HttpMethod.PUT)
                .endpoint("/api/v1/orders/{id}/cancel")
                .pathVariable("id", ctx -> ctx.get("order", Order.class).getId())
                .expectedStatus(HttpStatus.OK)
                .build()
        );
    }
}
```

---

## 실행 순서 (의존성 그래프)

```
Level 0: user, supplier, company (의존성 없음)
    │
    ▼
Level 1: product, pet, board (user/supplier/company 의존)
    │
    ▼
Level 2: inventory, reservation, memorial (product/pet 의존)
    │
    ▼
Level 3: order, schedule (inventory/reservation 의존)
    │
    ▼
Level 4: delivery, payment (order 의존)
    │
    ▼
Level 5: settlement (delivery/payment 의존)
```

---

## API 인터페이스

### 시나리오 실행 엔드포인트

```
POST /api/v1/test-scenario/execute
```

**Request:**

```json
{
  "domains": ["user", "product", "order"],  // null이면 전체
  "mode": "FULL",  // FULL, CREATE_ONLY, CRUD_ONLY
  "cleanupAfter": true,
  "stopOnFailure": false
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "totalDomains": 3,
    "passedDomains": 3,
    "failedDomains": 0,
    "totalSteps": 12,
    "passedSteps": 12,
    "failedSteps": 0,
    "executionTime": 5432,
    "results": [
      {
        "domain": "user",
        "status": "PASSED",
        "steps": [
          { "name": "사용자 생성", "status": "PASSED", "time": 120 },
          { "name": "사용자 조회", "status": "PASSED", "time": 45 },
          { "name": "사용자 수정", "status": "PASSED", "time": 89 },
          { "name": "사용자 삭제", "status": "PASSED", "time": 67 }
        ]
      }
    ]
  }
}
```

### 단일 도메인 실행

```
POST /api/v1/test-scenario/execute/{domain}
```

### 시나리오 목록 조회

```
GET /api/v1/test-scenario/scenarios
```

### 실행 이력 조회

```
GET /api/v1/test-scenario/history
```

---

## 실행 모드

| 모드 | 설명 |
|------|------|
| FULL | 모든 단계 실행 (Create → Read → Update → Delete → Custom) |
| CREATE_ONLY | 생성만 실행 (데이터 시딩용) |
| CRUD_ONLY | CRUD만 실행 (커스텀 제외) |
| CLEANUP_ONLY | 기존 테스트 데이터 정리만 |

---

## 환경 설정

### application.yml

```yaml
test-scenario:
  enabled: true  # 운영환경에서는 false
  base-url: http://localhost:8080
  default-timeout: 30000
  retry-count: 3
  cleanup-on-startup: true
  profiles:
    - name: default
      users:
        - role: CUSTOMER
          email: test-customer@example.com
          password: test1234!
        - role: SUPPLIER_ADMIN
          email: test-supplier@example.com
          password: test1234!
        - role: PLATFORM_ADMIN
          email: test-admin@example.com
          password: test1234!
```

### 프로파일별 실행

```bash
# 개발 환경
./gradlew test -Pprofile=dev -PtestScenario=true

# 스테이징 환경
./gradlew test -Pprofile=staging -PtestScenario=true
```

---

## 비즈니스 규칙

1. 운영 환경에서는 비활성화 필수
2. 테스트 데이터는 명확한 prefix 사용 (예: TEST_)
3. 시나리오 실행 후 정리(cleanup) 옵션 제공
4. 의존성 순서에 따라 자동 실행
5. 실패 시 롤백 및 상세 로그 제공

---

## 서브 지침

| 파일 | 설명 |
|------|------|
| [scenario-definition.md](./scenario-definition.md) | 시나리오 정의 방법 |
| [execution.md](./execution.md) | 실행 상세 |
| [context.md](./context.md) | 컨텍스트 관리 |
| [reporting.md](./reporting.md) | 리포팅 |
| [domains/](./domains/) | 도메인별 시나리오 |
