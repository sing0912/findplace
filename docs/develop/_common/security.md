# 보안 규칙

## 개요

시스템 보안을 위한 규칙과 가이드라인입니다.

---

## 인증 (Authentication)

### JWT 기반 인증

```
┌─────────┐       ┌─────────┐       ┌─────────┐
│  Client │──────▶│   API   │──────▶│   DB    │
└─────────┘       └─────────┘       └─────────┘
     │                 │
     │  1. Login       │
     │  (email/pw)     │
     │────────────────▶│
     │                 │ 2. Verify credentials
     │                 │
     │  3. JWT Token   │
     │◀────────────────│
     │                 │
     │  4. API Request │
     │  (Bearer Token) │
     │────────────────▶│
     │                 │ 5. Verify token
     │  6. Response    │
     │◀────────────────│
```

### 토큰 구조

#### Access Token

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "1",           // User ID
    "email": "user@example.com",
    "role": "CUSTOMER",
    "iat": 1706090400,    // Issued At
    "exp": 1706094000     // Expiration (1시간)
  }
}
```

#### Refresh Token

```json
{
  "payload": {
    "sub": "1",
    "type": "refresh",
    "iat": 1706090400,
    "exp": 1707300000     // Expiration (14일)
  }
}
```

### 토큰 저장

| 토큰 | 저장 위치 | 비고 |
|------|-----------|------|
| Access Token | Memory (변수) | XSS 방지 |
| Refresh Token | HttpOnly Cookie | CSRF 방지 |

### 토큰 갱신 플로우

```
1. Access Token 만료 감지
2. Refresh Token으로 /api/v1/auth/refresh 호출
3. 새 Access Token 발급
4. (선택) 새 Refresh Token 발급 (Rotation)
```

---

## 인가 (Authorization)

### 역할 기반 접근 제어 (RBAC)

```java
public enum Role {
    CUSTOMER,           // 일반 사용자
    COMPANY_ADMIN,      // 장례업체 관리자
    SUPPLIER_ADMIN,     // 공급사 관리자
    PLATFORM_ADMIN      // 플랫폼 관리자
}
```

### 권한 매트릭스

| 리소스 | CUSTOMER | COMPANY_ADMIN | SUPPLIER_ADMIN | PLATFORM_ADMIN |
|--------|----------|---------------|----------------|----------------|
| 사용자 (본인) | RU | RU | RU | CRUD |
| 사용자 (전체) | - | - | - | CRUD |
| 장례업체 | R | RU (본인) | R | CRUD |
| 공급사 | R | R | RU (본인) | CRUD |
| 상품 | R | R | CRU (본인) | CRUD |
| 예약 | CRU (본인) | RU | - | CRUD |
| 주문 | CRU (본인) | R | R (본인) | CRUD |
| 정산 | - | R (본인) | R (본인) | CRUD |

### Spring Security 설정

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()

                // Admin only
                .requestMatchers("/api/v1/admin/**").hasRole("PLATFORM_ADMIN")

                // Supplier only
                .requestMatchers("/api/v1/suppliers/*/products/**")
                    .hasAnyRole("SUPPLIER_ADMIN", "PLATFORM_ADMIN")

                // Authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### 메소드 레벨 보안

```java
@Service
public class OrderService {

    @PreAuthorize("hasRole('CUSTOMER') and #userId == authentication.principal.id")
    public Order createOrder(Long userId, OrderCreateRequest request) {
        // 본인만 주문 생성 가능
    }

    @PreAuthorize("@orderSecurity.canAccess(#orderId)")
    public Order getOrder(Long orderId) {
        // 커스텀 보안 로직
    }
}

@Component
public class OrderSecurity {
    public boolean canAccess(Long orderId) {
        // 본인 주문이거나 관리자인지 확인
    }
}
```

---

## 입력 검증

### SQL Injection 방지

```java
// Bad - SQL Injection 취약
String query = "SELECT * FROM users WHERE email = '" + email + "'";

// Good - Parameterized Query
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);
```

### XSS 방지

```java
// 입력값 이스케이프
import org.apache.commons.text.StringEscapeUtils;

String safeHtml = StringEscapeUtils.escapeHtml4(userInput);
```

### CSRF 방지

- SameSite Cookie 설정
- CSRF Token (필요 시)

```java
// Cookie 설정
ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
    .path("/api/v1/auth")
    .maxAge(Duration.ofDays(14))
    .build();
```

---

## 민감 정보 처리

### 비밀번호

```java
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### 개인정보 마스킹

```java
public class MaskingUtils {

    // 이메일: ho***@example.com
    public static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    // 전화번호: 010-****-5678
    public static String maskPhone(String phone) {
        return phone.replaceAll("(\\d{3})-?(\\d{4})-?(\\d{4})", "$1-****-$3");
    }

    // 이름: 홍*동
    public static String maskName(String name) {
        if (name.length() <= 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
```

### 로깅 시 민감정보 제외

```java
@Slf4j
public class UserService {

    public User createUser(UserCreateRequest request) {
        // Bad
        log.info("Creating user: {}", request);

        // Good
        log.info("Creating user: email={}", maskEmail(request.getEmail()));
    }
}
```

---

## API 보안

### Rate Limiting

```java
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.builder()
            .limitForPeriod(100)           // 요청 수
            .limitRefreshPeriod(Duration.ofMinutes(1))  // 기간
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
    }
}
```

### API Key (외부 연동용)

```
X-API-Key: {api_key}
```

### CORS 설정

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "https://findplace.com",
            "https://admin.findplace.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

---

## 데이터 암호화

### 저장 데이터 암호화

```java
@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {

    private final AesEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptor.decrypt(dbData);
    }
}

// Entity에서 사용
@Entity
public class User {

    @Convert(converter = EncryptionConverter.class)
    private String personalId;  // 주민등록번호 등 민감정보
}
```

### 전송 데이터 암호화

- HTTPS 필수 (TLS 1.2 이상)
- HSTS 헤더 적용

```java
@Configuration
public class SecurityHeaderConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request,
                    HttpServletResponse response, Object handler, ModelAndView modelAndView) {
                response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
            }
        });
    }
}
```

---

## 감사 로그 (Audit Log)

### 기록 대상

- 로그인/로그아웃
- 중요 데이터 CRUD
- 권한 변경
- 설정 변경

### 감사 로그 구조

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;          // CREATE, READ, UPDATE, DELETE
    private String resourceType;    // User, Order, Payment, etc.
    private String resourceId;
    private Long userId;
    private String userIp;
    private String userAgent;
    private String oldValue;        // JSON
    private String newValue;        // JSON
    private LocalDateTime createdAt;
}
```

### AOP를 통한 자동 기록

```java
@Aspect
@Component
public class AuditAspect {

    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result")
    public void audit(JoinPoint joinPoint, Auditable auditable, Object result) {
        // 감사 로그 저장
    }
}

// 사용
@Auditable(action = "CREATE", resourceType = "ORDER")
public Order createOrder(OrderCreateRequest request) {
    // ...
}
```

---

## 보안 체크리스트

### 개발 단계

- [ ] 모든 입력값 검증
- [ ] SQL Injection 방지 (Parameterized Query)
- [ ] XSS 방지 (출력 이스케이프)
- [ ] CSRF 방지
- [ ] 민감정보 암호화/마스킹
- [ ] 적절한 에러 메시지 (정보 노출 방지)

### 배포 단계

- [ ] HTTPS 적용
- [ ] 보안 헤더 설정
- [ ] CORS 설정
- [ ] Rate Limiting 설정
- [ ] 로그에서 민감정보 제외
- [ ] 환경변수로 시크릿 관리
