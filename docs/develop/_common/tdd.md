# TDD (Test-Driven Development) 지침

## 개요

모든 기능은 TDD 방식으로 개발하며, **테스트 커버리지 100%**를 목표로 합니다.

---

## TDD 사이클

```
┌─────────────────────────────────────────────────────────────┐
│                      TDD Cycle                              │
│                                                             │
│    ┌─────────┐      ┌─────────┐      ┌─────────┐           │
│    │  RED    │─────▶│  GREEN  │─────▶│ REFACTOR│           │
│    │(테스트 실패)│      │(테스트 통과)│      │ (리팩토링) │           │
│    └─────────┘      └─────────┘      └─────────┘           │
│         ▲                                   │               │
│         └───────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### 1. RED (실패하는 테스트 작성)
- 구현 전에 테스트를 먼저 작성
- 테스트가 실패하는지 확인

### 2. GREEN (테스트 통과하는 최소 코드 작성)
- 테스트를 통과하는 최소한의 코드 작성
- 완벽한 코드가 아니어도 됨

### 3. REFACTOR (리팩토링)
- 테스트가 통과하는 상태에서 코드 개선
- 중복 제거, 가독성 향상

---

## 테스트 계층

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Pyramid                             │
│                                                             │
│                      ┌─────┐                                │
│                     /  E2E  \         (10%)                 │
│                    /─────────\                              │
│                   / Integration\      (20%)                 │
│                  /───────────────\                          │
│                 /      Unit       \   (70%)                 │
│                /───────────────────\                        │
└─────────────────────────────────────────────────────────────┘
```

### Unit Test (단위 테스트) - 70%
- Service, Repository, Entity 테스트
- 외부 의존성 Mock 처리
- 빠른 실행 속도

### Integration Test (통합 테스트) - 20%
- Controller + Service + Repository
- 실제 DB 연동 (TestContainers)
- API 엔드포인트 테스트

### E2E Test (End-to-End) - 10%
- 전체 시나리오 테스트
- 시나리오 미들웨어 활용
- 실제 사용 흐름 검증

---

## 테스트 커버리지

### 목표

| 항목 | 최소 | 목표 |
|------|------|------|
| Line Coverage | 80% | 100% |
| Branch Coverage | 80% | 100% |
| Method Coverage | 90% | 100% |
| Class Coverage | 100% | 100% |

### 측정 도구

- **JaCoCo** (Java)
- **Istanbul/nyc** (TypeScript)

### 커버리지 검증

```bash
# 빌드 시 커버리지 검증
./gradlew test jacocoTestCoverageVerification

# 커버리지 미달 시 빌드 실패
```

---

## 테스트 작성 규칙

### 네이밍 컨벤션

```java
// 메소드명: should_ExpectedBehavior_When_Condition
@Test
void should_ReturnUser_When_ValidIdProvided() { }

@Test
void should_ThrowException_When_UserNotFound() { }

@Test
void should_CreateOrder_When_StockAvailable() { }
```

### Given-When-Then 패턴

```java
@Test
void should_CreateOrder_When_StockAvailable() {
    // Given (준비)
    Product product = createProduct(10); // 재고 10개
    OrderCreateRequest request = new OrderCreateRequest(product.getId(), 5);

    // When (실행)
    Order order = orderService.createOrder(request);

    // Then (검증)
    assertThat(order).isNotNull();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(inventoryService.getStock(product.getId())).isEqualTo(5);
}
```

### 테스트 격리

```java
@BeforeEach
void setUp() {
    // 테스트 데이터 초기화
}

@AfterEach
void tearDown() {
    // 테스트 데이터 정리
}
```

---

## 테스트 종류별 가이드

### 1. Entity 테스트

```java
class UserTest {

    @Test
    void should_CreateUser_WithValidData() {
        User user = User.builder()
            .email("test@example.com")
            .name("홍길동")
            .build();

        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void should_ThrowException_When_InvalidEmail() {
        assertThatThrownBy(() -> User.builder()
            .email("invalid-email")
            .build())
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

### 2. Service 테스트

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void should_ReturnUser_When_UserExists() {
        // Given
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUser(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }
}
```

### 3. Repository 테스트

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_FindByEmail_When_UserExists() {
        // Given
        User user = userRepository.save(createUser());

        // When
        Optional<User> result = userRepository.findByEmail(user.getEmail());

        // Then
        assertThat(result).isPresent();
    }
}
```

### 4. Controller 테스트

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void should_Return200_When_GetUser() throws Exception {
        // Given
        when(userService.getUser(1L)).thenReturn(createUser(1L));

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

### 5. Integration 테스트

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_CreateAndRetrieveUser() {
        // Create
        UserCreateRequest request = new UserCreateRequest("test@example.com", "홍길동");
        ResponseEntity<ApiResponse<User>> createResponse = restTemplate.postForEntity(
            "/api/v1/users", request, new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long userId = createResponse.getBody().getData().getId();

        // Retrieve
        ResponseEntity<ApiResponse<User>> getResponse = restTemplate.getForEntity(
            "/api/v1/users/" + userId, new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getBody().getData().getEmail()).isEqualTo("test@example.com");
    }
}
```

---

## 테스트 데이터 관리

### Test Fixture

```java
public class UserFixture {

    public static User createUser() {
        return createUser(null);
    }

    public static User createUser(Long id) {
        return User.builder()
            .id(id)
            .email("test@example.com")
            .name("홍길동")
            .role(Role.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
    }

    public static UserCreateRequest createUserRequest() {
        return new UserCreateRequest(
            "test@example.com",
            "password123!",
            "홍길동",
            "010-1234-5678"
        );
    }
}
```

### Test Data Builder

```java
public class UserTestBuilder {
    private String email = "test@example.com";
    private String name = "홍길동";
    private Role role = Role.CUSTOMER;

    public UserTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    public User build() {
        return User.builder()
            .email(email)
            .name(name)
            .role(role)
            .build();
    }
}

// 사용
User admin = new UserTestBuilder().withRole(Role.ADMIN).build();
```

---

## CI/CD 연동

### GitHub Actions

```yaml
- name: Run Tests
  run: ./gradlew test

- name: Check Coverage
  run: ./gradlew jacocoTestCoverageVerification

- name: Upload Coverage Report
  uses: codecov/codecov-action@v3
  with:
    files: ./build/reports/jacoco/test/jacocoTestReport.xml
```

### PR 머지 조건

- 모든 테스트 통과
- 커버리지 80% 이상 (목표: 100%)
- 새 코드 커버리지 100%

---

## 도메인별 테스트 체크리스트

각 도메인은 최소 다음 테스트를 포함해야 합니다:

- [ ] Entity 생성/수정/검증 테스트
- [ ] Service 비즈니스 로직 테스트
- [ ] Repository 쿼리 테스트
- [ ] Controller API 테스트
- [ ] 예외 케이스 테스트
- [ ] 경계값 테스트
- [ ] 통합 테스트
