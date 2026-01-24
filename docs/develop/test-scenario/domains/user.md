# User 도메인 테스트 시나리오

## 의존성

없음 (Level 0)

---

## CRUD 시나리오

### 1. Create - 사용자 생성

**엔드포인트:** `POST /api/v1/users`

**요청:**
```json
{
  "email": "test-user@example.com",
  "password": "Test1234!",
  "name": "테스트사용자",
  "phone": "010-1234-5678"
}
```

**예상 응답:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test-user@example.com",
    "name": "테스트사용자",
    "role": "CUSTOMER",
    "status": "ACTIVE"
  }
}
```

**검증:**
- id가 null이 아님
- email이 요청과 일치
- role이 CUSTOMER (기본값)
- status가 ACTIVE

---

### 2. Read - 사용자 조회

**엔드포인트:** `GET /api/v1/users/{id}`

**예상 응답:** `200 OK`

**검증:**
- 생성된 사용자 정보와 일치

---

### 3. Update - 사용자 수정

**엔드포인트:** `PUT /api/v1/users/{id}`

**요청:**
```json
{
  "name": "수정된이름",
  "phone": "010-9999-8888"
}
```

**예상 응답:** `200 OK`

**검증:**
- name이 "수정된이름"으로 변경
- phone이 "010-9999-8888"로 변경
- email은 변경되지 않음

---

### 4. Delete - 사용자 삭제

**엔드포인트:** `DELETE /api/v1/users/{id}`

**예상 응답:** `204 No Content`

**검증:**
- 이후 조회 시 404 반환

---

## 커스텀 시나리오

### 5. 이메일 중복 검증

**엔드포인트:** `POST /api/v1/users`

**요청:** 이미 존재하는 이메일

**예상 응답:** `409 Conflict`
```json
{
  "success": false,
  "error": {
    "code": "USER_EMAIL_DUPLICATE",
    "message": "이미 사용 중인 이메일입니다."
  }
}
```

---

### 6. 내 정보 조회

**엔드포인트:** `GET /api/v1/users/me`

**인증:** Bearer Token (CUSTOMER)

**예상 응답:** `200 OK`

**검증:**
- 토큰의 사용자 정보와 일치

---

### 7. 목록 조회 (관리자)

**엔드포인트:** `GET /api/v1/users?page=0&size=10`

**인증:** Bearer Token (PLATFORM_ADMIN)

**예상 응답:** `200 OK`

**검증:**
- content 배열 존재
- page 정보 존재

---

## 권한별 테스트

| 시나리오 | CUSTOMER | COMPANY_ADMIN | SUPPLIER_ADMIN | PLATFORM_ADMIN |
|----------|----------|---------------|----------------|----------------|
| 본인 조회 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| 타인 조회 | ❌ 403 | ❌ 403 | ❌ 403 | ✅ 200 |
| 본인 수정 | ✅ 200 | ✅ 200 | ✅ 200 | ✅ 200 |
| 타인 수정 | ❌ 403 | ❌ 403 | ❌ 403 | ✅ 200 |
| 목록 조회 | ❌ 403 | ❌ 403 | ❌ 403 | ✅ 200 |
| 삭제 | ❌ 403 | ❌ 403 | ❌ 403 | ✅ 204 |

---

## 테스트 데이터

### UserFixture

```java
public class UserFixture {

    public static final String TEST_EMAIL = "test-user@example.com";
    public static final String TEST_PASSWORD = "Test1234!";
    public static final String TEST_NAME = "테스트사용자";
    public static final String TEST_PHONE = "010-1234-5678";

    public static UserCreateRequest createRequest() {
        return UserCreateRequest.builder()
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .name(TEST_NAME)
            .phone(TEST_PHONE)
            .build();
    }

    public static UserCreateRequest createRequestWithEmail(String email) {
        return UserCreateRequest.builder()
            .email(email)
            .password(TEST_PASSWORD)
            .name(TEST_NAME)
            .phone(TEST_PHONE)
            .build();
    }

    public static UserUpdateRequest updateRequest() {
        return UserUpdateRequest.builder()
            .name("수정된이름")
            .phone("010-9999-8888")
            .build();
    }
}
```

---

## 구현 코드

```java
@Component
public class UserScenario implements DomainScenario {

    @Override
    public String getDomain() {
        return "user";
    }

    @Override
    public List<String> getDependencies() {
        return List.of();
    }

    @Override
    public ScenarioStep createStep() {
        return ScenarioStep.builder()
            .name("사용자 생성")
            .method(HttpMethod.POST)
            .endpoint("/api/v1/users")
            .requestBody(UserFixture.createRequest())
            .expectedStatus(HttpStatus.CREATED)
            .validator(response -> {
                UserResponse user = (UserResponse) response.getData();
                assertThat(user.getId()).isNotNull();
                assertThat(user.getEmail()).isEqualTo(UserFixture.TEST_EMAIL);
                assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
                assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
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
            .pathVariable("id", ctx -> ctx.get("user", UserResponse.class).getId())
            .authTokenProvider(ctx -> ctx.getToken("PLATFORM_ADMIN"))
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                UserResponse user = (UserResponse) response.getData();
                assertThat(user.getEmail()).isEqualTo(UserFixture.TEST_EMAIL);
            })
            .build();
    }

    @Override
    public ScenarioStep updateStep() {
        return ScenarioStep.builder()
            .name("사용자 수정")
            .method(HttpMethod.PUT)
            .endpoint("/api/v1/users/{id}")
            .pathVariable("id", ctx -> ctx.get("user", UserResponse.class).getId())
            .requestBody(UserFixture.updateRequest())
            .authTokenProvider(ctx -> ctx.getToken("PLATFORM_ADMIN"))
            .expectedStatus(HttpStatus.OK)
            .validator(response -> {
                UserResponse user = (UserResponse) response.getData();
                assertThat(user.getName()).isEqualTo("수정된이름");
                assertThat(user.getPhone()).isEqualTo("010-9999-8888");
            })
            .build();
    }

    @Override
    public ScenarioStep deleteStep() {
        return ScenarioStep.builder()
            .name("사용자 삭제")
            .method(HttpMethod.DELETE)
            .endpoint("/api/v1/users/{id}")
            .pathVariable("id", ctx -> ctx.get("user", UserResponse.class).getId())
            .authTokenProvider(ctx -> ctx.getToken("PLATFORM_ADMIN"))
            .expectedStatus(HttpStatus.NO_CONTENT)
            .build();
    }

    @Override
    public List<ScenarioStep> customSteps() {
        return List.of(
            // 이메일 중복 테스트
            ScenarioStep.builder()
                .name("이메일 중복 시 에러")
                .method(HttpMethod.POST)
                .endpoint("/api/v1/users")
                .requestBody(UserFixture.createRequest())
                .expectedStatus(HttpStatus.CONFLICT)
                .validator(response -> {
                    assertThat(response.getError().getCode())
                        .isEqualTo("USER_EMAIL_DUPLICATE");
                })
                .build(),

            // 내 정보 조회
            ScenarioStep.builder()
                .name("내 정보 조회")
                .method(HttpMethod.GET)
                .endpoint("/api/v1/users/me")
                .authTokenProvider(ctx -> ctx.getToken("CUSTOMER"))
                .expectedStatus(HttpStatus.OK)
                .build()
        );
    }
}
```
