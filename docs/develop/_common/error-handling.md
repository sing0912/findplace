# 에러 처리 규칙

## 개요

모든 도메인에서 일관된 에러 처리를 위한 규칙입니다.

---

## 에러 응답 형식

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자에게 보여줄 메시지",
    "details": [
      {
        "field": "fieldName",
        "message": "필드별 상세 메시지"
      }
    ]
  },
  "timestamp": "2026-01-24T10:30:00Z"
}
```

---

## 에러 코드 체계

### 형식

```
{DOMAIN}_{ERROR_TYPE}
```

### 공통 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| INVALID_REQUEST | 400 | 잘못된 요청 |
| UNAUTHORIZED | 401 | 인증 필요 |
| ACCESS_DENIED | 403 | 권한 없음 |
| NOT_FOUND | 404 | 리소스 없음 |
| CONFLICT | 409 | 충돌 |
| VALIDATION_ERROR | 422 | 유효성 검증 실패 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |
| EXTERNAL_SERVICE_ERROR | 502 | 외부 서비스 오류 |

### 도메인별 에러 코드 예시

#### 인증 (AUTH)

| 코드 | HTTP | 설명 |
|------|------|------|
| AUTH_INVALID_CREDENTIALS | 401 | 잘못된 인증 정보 |
| AUTH_TOKEN_EXPIRED | 401 | 토큰 만료 |
| AUTH_TOKEN_INVALID | 401 | 유효하지 않은 토큰 |
| AUTH_REFRESH_TOKEN_EXPIRED | 401 | 리프레시 토큰 만료 |

#### 사용자 (USER)

| 코드 | HTTP | 설명 |
|------|------|------|
| USER_NOT_FOUND | 404 | 사용자 없음 |
| USER_EMAIL_DUPLICATE | 409 | 이메일 중복 |
| USER_PHONE_DUPLICATE | 409 | 전화번호 중복 |
| USER_SUSPENDED | 403 | 정지된 계정 |

#### 공급사 (SUPPLIER)

| 코드 | HTTP | 설명 |
|------|------|------|
| SUPPLIER_NOT_FOUND | 404 | 공급사 없음 |
| SUPPLIER_NOT_APPROVED | 403 | 미승인 공급사 |
| SUPPLIER_BUSINESS_NUMBER_DUPLICATE | 409 | 사업자번호 중복 |

#### 상품 (PRODUCT)

| 코드 | HTTP | 설명 |
|------|------|------|
| PRODUCT_NOT_FOUND | 404 | 상품 없음 |
| PRODUCT_OUT_OF_STOCK | 400 | 재고 부족 |
| PRODUCT_NOT_AVAILABLE | 400 | 판매 불가 상품 |

#### 주문 (ORDER)

| 코드 | HTTP | 설명 |
|------|------|------|
| ORDER_NOT_FOUND | 404 | 주문 없음 |
| ORDER_ALREADY_CANCELLED | 400 | 이미 취소된 주문 |
| ORDER_CANNOT_CANCEL | 400 | 취소 불가 상태 |
| ORDER_AMOUNT_MISMATCH | 400 | 금액 불일치 |

#### 결제 (PAYMENT)

| 코드 | HTTP | 설명 |
|------|------|------|
| PAYMENT_NOT_FOUND | 404 | 결제 정보 없음 |
| PAYMENT_FAILED | 400 | 결제 실패 |
| PAYMENT_ALREADY_REFUNDED | 400 | 이미 환불됨 |
| PAYMENT_REFUND_EXCEED | 400 | 환불 금액 초과 |

#### 재고 (INVENTORY)

| 코드 | HTTP | 설명 |
|------|------|------|
| INVENTORY_NOT_FOUND | 404 | 재고 정보 없음 |
| INVENTORY_INSUFFICIENT | 400 | 재고 부족 |
| INVENTORY_NEGATIVE_NOT_ALLOWED | 400 | 음수 재고 불가 |

---

## 예외 클래스 구조

### 기본 예외 클래스

```java
// 기본 비즈니스 예외
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<ErrorDetail> details;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = Collections.emptyList();
    }

    public BusinessException(ErrorCode errorCode, List<ErrorDetail> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}
```

### 도메인별 예외

```java
// 사용자 도메인
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

public class UserEmailDuplicateException extends BusinessException {
    public UserEmailDuplicateException(String email) {
        super(ErrorCode.USER_EMAIL_DUPLICATE,
            List.of(new ErrorDetail("email", "이미 사용 중인 이메일입니다: " + email)));
    }
}
```

---

## Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(errorCode, e.getDetails()));
    }

    // 유효성 검증 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {
        List<ErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
            .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
            .toList();
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, details));
    }

    // 기타 예외 (예상치 못한 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
```

---

## 유효성 검증

### Bean Validation 사용

```java
public class UserCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$",
             message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;
}
```

### 커스텀 Validator

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BusinessNumberValidator.class)
public @interface ValidBusinessNumber {
    String message() default "올바른 사업자번호 형식이 아닙니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class BusinessNumberValidator
        implements ConstraintValidator<ValidBusinessNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        // 사업자번호 검증 로직
        return value.matches("^[0-9]{3}-[0-9]{2}-[0-9]{5}$");
    }
}
```

---

## 에러 로깅

### 로깅 레벨

| 상황 | 레벨 | 설명 |
|------|------|------|
| 4xx 에러 | WARN | 클라이언트 오류 |
| 5xx 에러 | ERROR | 서버 오류 (스택트레이스 포함) |
| 외부 서비스 오류 | ERROR | 외부 연동 실패 |

### 로깅 내용

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<?>> handleBusinessException(
        BusinessException e, HttpServletRequest request) {

    log.warn("Business exception: code={}, message={}, path={}, method={}",
        e.getErrorCode().getCode(),
        e.getMessage(),
        request.getRequestURI(),
        request.getMethod());

    // ...
}
```

---

## 프론트엔드 에러 처리

### API 클라이언트

```typescript
interface ApiError {
  code: string;
  message: string;
  details?: Array<{
    field: string;
    message: string;
  }>;
}

// Axios interceptor
api.interceptors.response.use(
  response => response,
  error => {
    const apiError: ApiError = error.response?.data?.error;

    if (apiError) {
      // 비즈니스 에러 처리
      handleApiError(apiError);
    } else {
      // 네트워크 에러 등
      handleNetworkError(error);
    }

    return Promise.reject(error);
  }
);
```

### 에러 표시

```typescript
function handleApiError(error: ApiError) {
  switch (error.code) {
    case 'AUTH_TOKEN_EXPIRED':
      // 토큰 갱신 시도 또는 로그인 페이지로
      break;
    case 'VALIDATION_ERROR':
      // 폼 필드에 에러 표시
      showFieldErrors(error.details);
      break;
    default:
      // 토스트 메시지
      showToast(error.message);
  }
}
```
