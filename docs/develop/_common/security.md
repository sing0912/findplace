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
public enum UserRole {
    CUSTOMER,       // 반려인 - 시터 검색, 예약, 결제, 돌봄 조회
    PARTNER,        // 펫시터 - 프로필/자격 관리, 예약 수락/거절, 돌봄 일지
    ADMIN,          // 관리자 - 일반 관리 기능
    SUPER_ADMIN     // 최고 관리자 - 전체 시스템 관리
}
```

### 권한 매트릭스

| 리소스 | CUSTOMER | PARTNER | ADMIN | SUPER_ADMIN |
|--------|----------|---------|-------|-------------|
| 사용자 (본인) | RU | RU | RU | CRUD |
| 사용자 (전체) | - | - | CRUD | CRUD |
| 예약 | CRU (본인) | RU (본인) | CRUD | CRUD |
| 돌봄 일지 | R (본인) | CRU (본인) | R | CRUD |
| 정산 | - | R (본인) | CRUD | CRUD |
| 후기 | CRU (본인) | R | R | CRUD |

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

                // Admin only
                .requestMatchers("/api/v1/admin/**")
                    .hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Partner only
                .requestMatchers("/api/v1/partner/**")
                    .hasAnyRole("PARTNER", "ADMIN", "SUPER_ADMIN")

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
public class BookingService {

    @PreAuthorize("hasRole('CUSTOMER') and #userId == authentication.principal.id")
    public Booking createBooking(Long userId, BookingCreateRequest request) {
        // 반려인만 예약 생성 가능
    }

    @PreAuthorize("@bookingSecurity.canAccess(#bookingId)")
    public Booking getBooking(Long bookingId) {
        // 커스텀 보안 로직
    }
}

@Component
public class BookingSecurity {
    public boolean canAccess(Long bookingId) {
        // 본인 예약이거나 관리자인지 확인
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
            "https://petpro.com",
            "https://admin.petpro.com"
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

## 시크릿 관리 (필수)

### 환경변수 필수 사용 규칙

**모든 시크릿(비밀키, 크레덴셜)은 반드시 환경변수로 관리해야 합니다.**

| 항목 | 환경변수 | 기본값 허용 |
|------|---------|------------|
| JWT Secret | `JWT_SECRET` | ❌ 기본값 없음 (필수 설정) |
| OAuth Client ID | `GOOGLE_CLIENT_ID`, `KAKAO_CLIENT_ID`, `NAVER_CLIENT_ID` | ❌ 빈 문자열만 허용 |
| OAuth Client Secret | `GOOGLE_CLIENT_SECRET`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_SECRET` | ❌ 빈 문자열만 허용 |
| DB Password | `DB_PASSWORD` | ❌ 운영 환경에서 금지 |

```yaml
# Bad - 하드코딩 (절대 금지)
app:
  jwt:
    secret: your-jwt-secret-key-must-be-at-least-256-bits
  oauth:
    google:
      client-id: 141536358954-xxxxx.apps.googleusercontent.com
      client-secret: GOCSPX-xxxxx

# Good - 환경변수 참조
app:
  jwt:
    secret: ${JWT_SECRET}  # 기본값 없음 → 미설정 시 앱 시작 실패
  oauth:
    google:
      client-id: ${GOOGLE_CLIENT_ID:}
      client-secret: ${GOOGLE_CLIENT_SECRET:}
```

### 프로필별 설정 원칙

- `application.yml`: 환경변수 참조만 사용 (기본값은 빈 문자열 또는 없음)
- `application-local.yml`: 환경변수 참조 사용 (개발 편의를 위한 기본값 허용하되, 실제 시크릿 하드코딩 금지)
- `.env` 파일: `.gitignore`에 포함, 실제 시크릿 저장

---

## IDOR 방지 (필수)

### 리소스 접근 제어 규칙

**모든 사용자 ID 기반 조회 엔드포인트는 접근 제어가 필수입니다.**

```java
// Bad - IDOR 취약 (누구나 다른 사용자 정보 조회 가능)
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse.Info>> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
}

// Good - 본인 또는 관리자만 접근 가능
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or #id == T(Long).parseLong(authentication.name)")
public ResponseEntity<ApiResponse<UserResponse.Info>> getUser(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
}
```

### 관리자 컨트롤러 userId 추출

```java
// Bad - 하드코딩
private Long extractUserId(UserDetails userDetails) {
    return 1L; // 임시 값
}

// Good - UserDetails에서 추출
private Long extractUserId(UserDetails userDetails) {
    return Long.parseLong(userDetails.getUsername());
}
```

---

## 파일 업로드 보안 (필수)

### 파일 업로드 검증 규칙

| 검증 항목 | 규칙 |
|-----------|------|
| 파일 크기 | 최대 5MB |
| 허용 타입 | `image/jpeg`, `image/png`, `image/gif`, `image/webp` |
| 파일명 | UUID로 재생성 (원본 파일명 사용 금지) |
| 빈 파일 | 거부 |

```java
// 필수 검증 로직
private void validateProfileImage(MultipartFile file) {
    if (file.isEmpty()) {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    if (file.getSize() > 5 * 1024 * 1024) {
        throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
        throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
    }
}

// 파일명 새니타이징 (UUID 사용)
String extension = getExtension(file.getOriginalFilename());
String safeFileName = UUID.randomUUID() + "." + extension;
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
- [ ] 시크릿 하드코딩 금지 (환경변수 사용)
- [ ] IDOR 방지 (리소스 접근 제어)
- [ ] 파일 업로드 타입/크기 검증

### 배포 단계

- [ ] HTTPS 적용
- [ ] 보안 헤더 설정
- [ ] CORS 설정
- [ ] Rate Limiting 설정
- [ ] 로그에서 민감정보 제외
- [ ] 환경변수로 시크릿 관리
