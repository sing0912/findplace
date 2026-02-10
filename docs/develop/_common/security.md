# 보안 규칙

## 개요

시스템 보안을 위한 규칙과 가이드라인입니다.

---

## 보안 표준 프레임워크

본 보안 지침은 아래 국제 보안 표준을 기반으로 작성되었습니다.

### 참조 표준

| 표준 | 용도 | 링크 |
|------|------|------|
| **OWASP ASVS** (Application Security Verification Standard) | 애플리케이션 보안 요구사항 정의 및 검증 기준 | https://owasp.org/www-project-application-security-verification-standard/ |
| **OWASP WSTG** (Web Security Testing Guide) | 웹 애플리케이션 보안 테스트 절차 및 기법 | https://owasp.org/www-project-web-security-testing-guide/ |
| **SANS SWAT** (Securing Web Application Technologies) Checklist | 웹 애플리케이션 보안 설정 빠른 점검 체크리스트 | https://www.sans.org/cloud-security/securing-web-application-technologies/ |

> 대규모 조직이나 정보보안 인증이 필요한 경우 **NIST SP 800-53** (미국 연방 보안 통제), **ISO/IEC 27001** (국제 정보보안 관리체계) 표준의 애플리케이션 보안 섹션도 참고합니다.

### 본 지침과 OWASP ASVS 매핑

| ASVS 영역 | 본 지침 섹션 |
|-----------|-------------|
| V1: Architecture | 인프라/포트 보안, 컨테이너 보안 |
| V2: Authentication | 인증 (JWT, 토큰 갱신, 로그아웃) |
| V3: Session Management | 토큰 저장, Refresh Token Rotation |
| V4: Access Control | 인가 (RBAC), IDOR 방지 |
| V5: Validation | 입력 검증, SQL Injection, XSS 방지 |
| V6: Cryptography | 데이터 암호화, 비밀번호 해싱, SecureRandom |
| V7: Error Handling & Logging | 에러 처리 보안, 감사 로그 |
| V8: Data Protection | 민감 정보 처리, 개인정보 마스킹 |
| V9: Communication | 전송 보안 (HTTPS/TLS), HSTS |
| V10: Malicious Code | 의존성(오픈소스) 보안 |
| V12: Files & Resources | 파일 업로드 보안, 파일 스토리지 보안 |
| V13: API & Web Services | API 보안, Rate Limiting, CORS |
| V14: Configuration | 시크릿 관리, Swagger 차단, Actuator 제한 |

---

## 핵심 보안 점검 5대 영역

전체 보안 표준을 검토하기 어려운 경우, 아래 **5가지 핵심 영역**을 우선 점검합니다.

### 1. 전송 보안

> 모든 페이지에 HTTPS(TLS 1.2 이상)가 적용되었는가?

- 모든 HTTP 요청은 HTTPS로 리다이렉트 (Nginx `return 301 https://`)
- TLS 1.2/1.3만 허용 (`ssl_protocols TLSv1.2 TLSv1.3`)
- HSTS 헤더 적용 (`max-age=63072000; includeSubDomains`)
- 관련 섹션: [데이터 암호화 > 전송 데이터 암호화](#전송-데이터-암호화)

### 2. 인증 및 인가

> MFA(2단계 인증)를 지원하며, URL 파라미터 조작만으로 타인의 정보가 보이지 않는가?

- JWT 기반 Stateless 인증 + Refresh Token Rotation
- RBAC(역할 기반 접근 제어)로 엔드포인트별 권한 분리
- IDOR(Insecure Direct Object Reference) 방지: 모든 리소스 접근에 소유권 검증
- 로그인 실패 5회 시 계정 잠금 (30분)
- 인증번호 시도 5회 제한
- 관련 섹션: [인증](#인증-authentication), [인가](#인가-authorization), [IDOR 방지](#idor-방지-필수)

### 3. 입력값 검증

> 사용자 입력이 DB 쿼리나 스크립트(XSS)로 실행되지 않도록 차단했는가?

- JPA Parameterized Query로 SQL Injection 방지
- 출력 이스케이프로 XSS 방지
- CSP(Content-Security-Policy) 헤더로 스크립트 실행 제한
- DTO 레벨 `@Valid` + `@Pattern` 검증 (전화번호, 비밀번호, 이메일 등)
- 파일 업로드: 확장자 화이트리스트 + 매직바이트 검증
- 관련 섹션: [입력 검증](#입력-검증), [CSP](#csp-content-security-policy-필수)

### 4. 자산 관리 (의존성 보안)

> 현재 사용 중인 오픈소스 라이브러리 중 취약점 보고가 된 것이 있는가?

- 정기적인 의존성 취약점 스캔 필수
- Backend: `./gradlew dependencyCheckAnalyze` (OWASP Dependency-Check)
- Frontend: `npm audit`
- Docker 이미지: `docker scout cves` 또는 `trivy image`
- 관련 섹션: [의존성(오픈소스) 보안](#의존성오픈소스-보안-필수)

### 5. 보안 설정

> 관리자 페이지가 외부 노출되지 않고, 서버 에러 시 내부 경로가 노출되지 않는가?

- `/admin/**` 엔드포인트: ADMIN 이상 역할만 접근 가능
- Actuator: `/health`만 공개, 나머지 인증 필요
- Swagger UI: 프로덕션에서 비활성화 + Nginx 403 차단
- 에러 응답에 스택트레이스, 내부 경로, SQL 쿼리 등 노출 금지
- 서버 버전 정보 헤더 숨김 (`server_tokens off`)
- 관련 섹션: [Actuator 보안](#actuator-엔드포인트-보안-필수), [에러 처리 보안](#에러-처리-보안-필수)

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

### 토큰 저장 (필수)

| 토큰 | 저장 위치 | 비고 |
|------|-----------|------|
| Access Token | Memory (Zustand 상태) | XSS 방지 — 페이지 새로고침 시 refresh로 재발급 |
| Refresh Token | HttpOnly Cookie | CSRF 방지 — 서버에서 Set-Cookie로 설정 |

> **⛔ 절대 금지: localStorage/sessionStorage에 토큰 저장**
>
> localStorage는 JavaScript로 자유롭게 접근 가능하므로 XSS 공격 시 토큰 탈취가 가능합니다.
> ```typescript
> // Bad — 절대 금지
> localStorage.setItem('accessToken', token);
>
> // Good — 메모리에만 보관
> const useAuthStore = create((set) => ({
>   accessToken: null, // 메모리 상태로만 관리
> }));
> ```

### 토큰 갱신 플로우

```
1. Access Token 만료 감지
2. Refresh Token으로 /api/v1/auth/refresh 호출
3. 새 Access Token 발급
4. (선택) 새 Refresh Token 발급 (Rotation)
```

### 로그아웃 보안 (필수)

**로그아웃 시 반드시 서버측에서 Refresh Token을 무효화해야 합니다.**

```java
// Bad — 클라이언트에만 의존 (탈취된 토큰이 만료까지 유효)
@PostMapping("/logout")
public ResponseEntity<?> logout() {
    return ResponseEntity.ok(ApiResponse.success("로그아웃"));
}

// Good — 서버측 토큰 무효화
@PostMapping("/logout")
public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
    Long userId = Long.parseLong(userDetails.getUsername());
    User user = userRepository.findById(userId).orElseThrow();
    user.invalidateRefreshToken();
    return ResponseEntity.ok(ApiResponse.success("로그아웃"));
}
```

### 인증번호 보안 (필수)

#### SecureRandom 사용

```java
// Bad — 예측 가능한 난수 (java.util.Random)
Random random = new Random();
int code = 100000 + random.nextInt(900000);

// Good — 암호학적으로 안전한 난수
private static final SecureRandom SECURE_RANDOM = new SecureRandom();
int code = 100000 + SECURE_RANDOM.nextInt(900000);
```

#### 시도 횟수 제한

인증번호 검증 시 **최대 5회까지만 시도 허용**합니다. 초과 시 해당 인증 요청을 만료 처리합니다.

```java
if (verification.getAttemptCount() >= 5) {
    verification.expire();
    throw new BusinessException(ErrorCode.VERIFICATION_ATTEMPT_EXCEEDED);
}
verification.incrementAttemptCount();
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

### Actuator 엔드포인트 보안 (필수)

**`/actuator/**` 전체를 permitAll로 열면 안 됩니다.** `/actuator/env`, `/actuator/heapdump` 등으로 시스템 내부 정보가 유출됩니다.

```java
// Bad — Actuator 전체 공개 (시스템 정보 유출)
.requestMatchers("/health", "/actuator/**").permitAll()

// Good — health만 공개, 나머지는 인증 필요
.requestMatchers("/health", "/actuator/health").permitAll()
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

프로덕션 `application-prod.yml`에서 노출 엔드포인트 최소화:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

### Swagger UI 프로덕션 차단 (필수)

프로덕션에서 API 문서가 인증 없이 노출되면 안 됩니다.
Nginx에서 차단하거나, `application-prod.yml`에서 비활성화합니다:

```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
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

### @Valid 필수 사용 규칙

**모든 `@RequestBody`에는 반드시 `@Valid`를 붙여야 합니다.** 누락 시 DTO의 검증 어노테이션(`@NotBlank`, `@Size`, `@Pattern` 등)이 작동하지 않습니다.

```java
// Bad — @Valid 누락 (DTO 검증 미작동)
@PostMapping
public ResponseEntity<?> create(@RequestBody CreateRequest request) { ... }

// Good — @Valid 필수
@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody CreateRequest request) { ... }
```

### 전화번호 형식 검증 (필수)

전화번호 필드는 `@NotBlank`만으로 부족합니다. 반드시 `@Pattern`으로 형식을 검증합니다:

```java
@NotBlank(message = "전화번호는 필수입니다.")
@Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 전화번호 형식이 아닙니다.")
private String phone;
```

### 비밀번호 복잡도 일관성 (필수)

모든 비밀번호 입력 DTO에 동일한 복잡도 검증을 적용합니다. 일부 DTO만 `@Size(min=8)`이고 다른 곳은 `@Pattern`인 불일치를 허용하지 않습니다:

```java
// 모든 비밀번호 필드에 동일한 규칙 적용
@NotBlank(message = "비밀번호는 필수입니다.")
@Size(min = 8, max = 50, message = "비밀번호는 8~50자여야 합니다.")
@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
         message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
private String password;
```

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

### OAuth state 파라미터 보안 (필수)

OAuth 로그인에서 **CSRF 공격 방지를 위해 state 파라미터를 반드시 구현**합니다.

```
1. 프론트엔드: crypto.randomUUID()로 state 생성 → sessionStorage에 저장
2. 프론트엔드: OAuth 인가 URL에 state 포함
3. 콜백: sessionStorage의 state와 응답의 state 비교 검증
4. 불일치 시 요청 거부
```

```typescript
// Good — 암호학적으로 안전한 state 생성 + 검증
const state = crypto.randomUUID();
sessionStorage.setItem('oauth_state', state);
const authUrl = `https://kauth.kakao.com/oauth/authorize?state=${state}&...`;

// 콜백에서 검증
const savedState = sessionStorage.getItem('oauth_state');
if (savedState !== searchParams.get('state')) {
  throw new Error('OAuth state 불일치 — CSRF 의심');
}
```

```typescript
// Bad — Math.random() 사용 (예측 가능)
const state = Math.random().toString(36).substring(7);

// Bad — state 생성만 하고 저장/검증 안 함
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

## 의존성(오픈소스) 보안 (필수)

### 취약점 스캔

사용 중인 오픈소스 라이브러리에 알려진 취약점(CVE)이 없는지 정기적으로 점검합니다.

#### Backend (Gradle)

```bash
# OWASP Dependency-Check 플러그인 사용
./gradlew dependencyCheckAnalyze

# 또는 Gradle 플러그인 추가 후
# build.gradle
plugins {
    id 'org.owasp.dependencycheck' version '10.0.0'
}

dependencyCheck {
    failBuildOnCVSS = 7  // CVSS 7점 이상이면 빌드 실패
}
```

#### Frontend (npm)

```bash
# 취약점 확인
npm audit

# 자동 수정 가능한 취약점 패치
npm audit fix

# 상세 보고서
npm audit --json
```

#### Docker 이미지

```bash
# Docker Scout (Docker Desktop 내장)
docker scout cves petpro-nginx:latest

# Trivy (CI/CD용)
trivy image postgres:16-alpine
```

### 점검 주기

| 점검 항목 | 주기 | 도구 |
|-----------|------|------|
| npm audit | PR마다 (CI) + 주 1회 | npm audit |
| Gradle dependency check | 주 1회 | OWASP Dependency-Check |
| Docker 이미지 스캔 | 배포 전 + 월 1회 | Docker Scout / Trivy |
| 주요 CVE 모니터링 | 상시 | GitHub Dependabot / Snyk |

### 대응 기준

| CVSS 점수 | 심각도 | 대응 기한 |
|-----------|--------|-----------|
| 9.0 ~ 10.0 | Critical | 24시간 이내 패치 |
| 7.0 ~ 8.9 | High | 72시간 이내 패치 |
| 4.0 ~ 6.9 | Medium | 2주 이내 패치 |
| 0.1 ~ 3.9 | Low | 다음 정기 업데이트 시 |

---

## 에러 처리 보안 (필수)

### 에러 응답 원칙

프로덕션 환경에서 에러 응답에 시스템 내부 정보가 노출되면 안 됩니다.

```java
// Bad — 스택트레이스/내부 경로 노출
{
  "error": "NullPointerException at com.petpro.service.UserService.getUser(UserService.java:42)",
  "path": "/Users/deploy/app/src/main/java/..."
}

// Good — 일반적인 메시지만 반환
{
  "success": false,
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 오류가 발생했습니다."
  }
}
```

### 프로덕션 설정

```yaml
# application-prod.yml
server:
  error:
    include-stacktrace: never
    include-message: never
    include-binding-errors: never
    include-exception: false
```

### Nginx 서버 정보 숨김

```nginx
# nginx.conf (http 블록)
server_tokens off;
```

### 에러별 노출 허용 범위

| 에러 유형 | 개발 환경 | 프로덕션 환경 |
|-----------|-----------|-------------|
| 스택트레이스 | 허용 | **금지** |
| SQL 쿼리 | 허용 | **금지** |
| 내부 파일 경로 | 허용 | **금지** |
| 서버 버전 (Nginx, Tomcat) | 허용 | **금지** |
| 비즈니스 에러 메시지 | 허용 | 허용 (사용자 친화적) |

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
    secret: hardcoded-secret-here  # 절대 금지!
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

## 인프라/포트 보안 (필수)

### 포트 바인딩 원칙

**외부에 노출하는 포트는 Nginx(80/443)만 허용합니다.**

`docker-compose.yml`에서 모든 내부 서비스 포트는 `127.0.0.1`로 바인딩:

```yaml
# Good - localhost만 접근 가능
ports:
  - "127.0.0.1:5432:5432"

# Bad - 외부에서 직접 접근 가능 (절대 금지)
ports:
  - "5432:5432"
```

### 포트 분류

| 분류 | 포트 | 바인딩 | 비고 |
|------|------|--------|------|
| **외부 허용** | 80, 443 | `0.0.0.0` | Nginx (유일한 진입점) |
| **내부 전용** | 5432-5435 | `127.0.0.1` | PostgreSQL (Master/Slave/Coupon) |
| **내부 전용** | 3306-3307 | `127.0.0.1` | MySQL Log (Master/Slave) |
| **내부 전용** | 6379 | `127.0.0.1` | Redis |
| **내부 전용** | 9000-9001 | `127.0.0.1` | MinIO (API/Console) |
| **내부 전용** | 9090, 3001, 3100 | `127.0.0.1` | Prometheus, Grafana, Loki |
| **내부 전용** | 3200, 4317, 4318, 9411 | `127.0.0.1` | Tempo |

### 방화벽 규칙

```bash
# 허용: HTTP/HTTPS만
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload

# 절대 금지: DB, Redis, MinIO, 모니터링 포트 개방
# systemctl stop firewalld 도 절대 금지
```

내부 서비스 접근 시 SSH 터널링 사용:
```bash
ssh -L 3001:localhost:3001 user@server  # Grafana
ssh -L 9001:localhost:9001 user@server  # MinIO Console
```

### 자동 보안 적용

배포/시작 시 방화벽 보안이 자동 적용됩니다:
- `./setup.sh start --prod` → `scripts/secure-firewall.sh` 자동 호출
- `./deploy.sh` → 배포 완료 시 자동 호출
- `./scripts/secure-firewall.sh --check` → 수동 점검

---

## 파일 스토리지 보안 (필수)

### MinIO 버킷 정책

**경로별 접근 정책을 분리합니다. 버킷 전체를 public으로 설정하지 않습니다.**

```bash
# Good - 공개 필요한 경로만 download 허용
mc anonymous set download myminio/petpro/pets
mc anonymous set download myminio/petpro/public

# Bad - 버킷 전체 공개 (민감 파일 노출 위험)
mc anonymous set download myminio/petpro
mc anonymous set public myminio/petpro
```

### 경로별 접근 정책

| 경로 | 정책 | 저장 데이터 | 접근 방법 |
|------|------|------------|-----------|
| `pets/` | anonymous download | 펫 프로필 이미지 | Nginx 프록시 (공개) |
| `public/` | anonymous download | 공개 리소스 | Nginx 프록시 (공개) |
| `sitters/{id}/docs/` | 인증 필수 | 신분증, 범죄경력증명서 | Presigned URL |
| `chat/{roomId}/` | 인증 필수 | 채팅 첨부파일 | Presigned URL |
| `care/{bookingId}/` | 인증 필수 | 돌봄 일지 미디어 | Presigned URL |

### Nginx 파일 접근 제어

```nginx
# 민감 경로 차단 (반드시 공개 경로보다 먼저 선언)
location /files/petpro/sitters/ { return 403; }
location /files/petpro/chat/    { return 403; }
location /files/petpro/care/    { return 403; }

# 공개 경로만 허용 (GET/HEAD만)
location /files/petpro/pets/ {
    limit_except GET HEAD { deny all; }
    # ...
}

# 나머지 전부 차단
location /files/ { return 403; }
```

### 파일 업로드 검증 (강화)

| 검증 항목 | 규칙 | 비고 |
|-----------|------|------|
| 파일 크기 | 최대 5MB (이미지) | Spring + Nginx 이중 제한 |
| 허용 확장자 | jpg, jpeg, png, gif, webp | 화이트리스트 방식 |
| 매직바이트 | 파일 헤더 검증 필수 | 확장자 위조 방지 |
| Content-Type | 서버 측 확장자 기반 설정 | 클라이언트 헤더 불신 |
| 파일명 | UUID로 재생성 | Path Traversal 방지 |
| 경로 구분자 | 파일명에서 제거 후 확장자 추출 | `../../etc/passwd` 방어 |

```java
// Content-Type은 클라이언트가 아닌 서버가 결정
private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
    "jpg", "image/jpeg",
    "jpeg", "image/jpeg",
    "png", "image/png",
    "gif", "image/gif",
    "webp", "image/webp"
);
String safeContentType = CONTENT_TYPE_MAP.getOrDefault(extension, "application/octet-stream");
```

### Nginx Host 헤더

MinIO 프록시 시 Host 헤더를 MinIO 내부 주소로 고정:

```nginx
# Good - MinIO가 올바른 Host 수신
proxy_set_header Host minio:9000;

# Bad - 외부 도메인이 전달되어 MinIO 혼란
proxy_set_header Host $host;
```

---

## CSP (Content Security Policy) (필수)

XSS 공격의 가장 효과적인 방어 수단입니다. Nginx에서 CSP 헤더를 설정합니다:

```nginx
# nginx.conf 또는 ssl.conf 서버 블록
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:; connect-src 'self' https:" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;
```

---

## 소스맵 보안 (필수)

프로덕션 빌드에서 소스맵(.map 파일)이 포함되면 전체 소스코드가 역공학 가능합니다.

### 빌드 시 소스맵 비활성화

```json
// package.json
{
  "scripts": {
    "build": "GENERATE_SOURCEMAP=false react-scripts build"
  }
}
```

### Nginx에서 .map 파일 차단 (이중 방어)

```nginx
location ~* \.map$ {
    return 404;
}
```

---

## 컨테이너 보안 하드닝

### 필수 보안 옵션

```yaml
# docker-compose.yml 또는 docker-compose.prod.yml
services:
  postgres-master:
    security_opt:
      - no-new-privileges:true
    cap_drop:
      - ALL
    deploy:
      resources:
        limits:
          memory: 512M
```

| 옵션 | 설명 | 필수 |
|------|------|------|
| `security_opt: [no-new-privileges:true]` | 권한 상승 방지 | **필수** |
| `cap_drop: [ALL]` | 불필요한 Linux capability 제거 | **권장** |
| `deploy.resources.limits` | CPU/메모리 제한 | **권장** |
| `read_only: true` | 읽기 전용 파일시스템 (tmpfs와 함께) | 선택 |

---

## 데이터베이스 네트워크 접근 제어

### PostgreSQL pg_hba.conf

**`0.0.0.0/0`으로 모든 IP를 허용하면 안 됩니다.** Docker 내부 서브넷만 허용합니다:

```
# Good — Docker 내부만 허용
local   all             all                                     trust
host    all             all             127.0.0.1/32            scram-sha-256
host    all             all             172.16.0.0/12           scram-sha-256
host    replication     replicator      172.16.0.0/12           scram-sha-256

# Bad — 전체 네트워크 허용 (절대 금지)
host    all             all             0.0.0.0/0               scram-sha-256
host    replication     all             0.0.0.0/0               scram-sha-256
```

### MySQL 초기화 스크립트

복제 비밀번호를 하드코딩하지 않고 환경변수를 사용합니다:

```sql
-- Bad — 하드코딩
CREATE USER 'repl_user'@'%' IDENTIFIED BY 'repl_pass';

-- Good — 환경변수 (entrypoint 스크립트에서 envsubst 처리)
CREATE USER 'repl_user'@'mysql-log-slave' IDENTIFIED BY '${MYSQL_REPL_PASSWORD}';
```

---

## Swagger/API 문서 접근 제어

프로덕션 환경에서 Swagger UI와 API 문서는 차단:

```nginx
# 프로덕션 Nginx에서 차단
location /api/api-docs  { return 403; }
location /api/swagger-ui { return 403; }
```

---

## Rate Limiting

### Nginx Rate Limit 설정

```nginx
# nginx.conf (http 블록)
limit_req_zone $binary_remote_addr zone=upload_limit:10m rate=10r/m;
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s;

# 파일 업로드 API
location ~ ^/api/v1/(pets/\d+/image|users/me/profile-image) {
    limit_req zone=upload_limit burst=5 nodelay;
    client_max_body_size 10M;
}

# 일반 API
location /api {
    limit_req zone=api_limit burst=50 nodelay;
}
```

### Nginx body size 정합성

| 위치 | 설정 | 값 |
|------|------|------|
| `nginx.conf` (기본값) | `client_max_body_size` | 10M |
| 업로드 location (override) | `client_max_body_size` | 10M |
| Spring Boot | `max-file-size` | 5MB |
| Spring Boot | `max-request-size` | 5MB |

---

## 보안 체크리스트

### 보안 표준 준수

- [ ] OWASP ASVS Level 1 요구사항 충족 확인
- [ ] OWASP Top 10 취약점 점검 (Injection, Broken Auth, XSS, IDOR 등)
- [ ] 의존성 취약점 스캔: `npm audit` + `./gradlew dependencyCheckAnalyze`
- [ ] Docker 이미지 취약점 스캔: `docker scout cves` 또는 `trivy image`
- [ ] CVSS 7.0 이상 취약점 없음 확인

### 개발 단계

- [ ] 모든 `@RequestBody`에 `@Valid` 적용
- [ ] 전화번호 필드에 `@Pattern` 검증
- [ ] 비밀번호 필드에 동일한 복잡도 `@Pattern` 적용 (모든 DTO 일관성)
- [ ] 인증번호 생성: `SecureRandom` 사용 (`Random` 금지)
- [ ] 인증번호 검증: 시도 횟수 제한 (최대 5회)
- [ ] 로그아웃: 서버측 Refresh Token 무효화
- [ ] SQL Injection 방지 (Parameterized Query / JPA)
- [ ] LIKE 쿼리: 와일드카드(`%`, `_`) 이스케이프 처리
- [ ] XSS 방지 (출력 이스케이프)
- [ ] CSRF 방지 (SameSite Cookie)
- [ ] OAuth: state 파라미터 생성(crypto.randomUUID) + 검증
- [ ] 민감정보 암호화/마스킹
- [ ] 에러 응답: 스택트레이스/내부 경로/SQL 쿼리 노출 금지
- [ ] 프로덕션 `include-stacktrace: never` 설정
- [ ] 시크릿 하드코딩 금지 (환경변수 사용)
- [ ] IDOR 방지 (리소스 접근 제어 / 소유권 검증)
- [ ] 파일 업로드: 확장자 화이트리스트 + 매직바이트 검증 + UUID 파일명
- [ ] 파일 Content-Type: 서버 측 확장자 기반 설정 (클라이언트 불신)
- [ ] 파일 경로: Path Traversal 방어 (경로 구분자 제거)
- [ ] 토큰 저장: localStorage 사용 금지 (메모리 + httpOnly Cookie)

### 인프라/배포 단계

- [ ] HTTPS 적용 (TLS 1.2 이상)
- [ ] 보안 헤더 설정 (HSTS, X-Content-Type-Options, X-Frame-Options, **CSP**, Referrer-Policy)
- [ ] CSP(Content-Security-Policy) 헤더 설정
- [ ] 소스맵: `GENERATE_SOURCEMAP=false` + Nginx `.map` 차단
- [ ] CORS 허용 출처 최소화 (프로덕션에서 localhost 제거)
- [ ] Rate Limiting 설정 (업로드: 10r/m, API: 30r/s, 로그인: 5r/m)
- [ ] Nginx body size 제한 (기본 10M)
- [ ] 로그에서 민감정보 제외 (OAuth 이메일 마스킹)
- [ ] 환경변수로 시크릿 관리 (.env는 .gitignore 포함)
- [ ] 방화벽: 80/443만 외부 허용 (DB/Redis/모니터링 차단)
- [ ] Docker 포트: Nginx 외 모든 서비스 `127.0.0.1` 바인딩
- [ ] Actuator: `/health`만 permitAll (나머지는 ADMIN 인증 필요)
- [ ] Swagger UI: 프로덕션에서 비활성화
- [ ] MinIO: 경로별 anonymous 정책 (pets/, public/만 download)
- [ ] MinIO: 민감 경로 Nginx에서 403 차단 (sitters/docs/, chat/, care/)
- [ ] 방화벽 보안 자동 적용 확인 (`./scripts/secure-firewall.sh --check`)
- [ ] pg_hba.conf: `0.0.0.0/0` 금지, Docker 서브넷만 허용
- [ ] MySQL init SQL: 복제 비밀번호 환경변수화
- [ ] 컨테이너: `security_opt: [no-new-privileges:true]` 설정
- [ ] Content-Disposition 헤더 설정 (이미지: inline)
- [ ] Nginx `server_tokens off` 설정 (서버 버전 숨김)
- [ ] 프로덕션 에러 페이지: 내부 정보 미노출 커스텀 페이지
