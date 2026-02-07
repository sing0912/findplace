package com.petpro.global.exception;

import com.petpro.global.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 핸들러입니다.
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리합니다.
 *
 * 처리하는 예외 유형:
 * - BusinessException: 비즈니스 로직 예외
 * - MethodArgumentNotValidException: @Valid 검증 실패
 * - ConstraintViolationException: 제약 조건 위반
 * - AuthenticationException: 인증 실패
 * - AccessDeniedException: 접근 권한 없음
 * - HttpRequestMethodNotSupportedException: 지원하지 않는 HTTP 메서드
 * - NoHandlerFoundException: 엔드포인트 없음 (404)
 * - Exception: 기타 모든 예외
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 예외를 처리합니다.
     * 애플리케이션에서 의도적으로 발생시킨 예외를 처리합니다.
     *
     * @param e BusinessException 인스턴스
     * @return 에러 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), e.getMessage(), e.getDetails()));
    }

    /**
     * @Valid 어노테이션에 의한 검증 실패 예외를 처리합니다.
     * 요청 본문의 필드 검증 실패 시 발생합니다.
     *
     * @param e MethodArgumentNotValidException 인스턴스
     * @return 에러 응답 (필드별 검증 오류 포함)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation exception: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT.getCode(), "입력값 검증에 실패했습니다.", errors));
    }

    /**
     * 제약 조건 위반 예외를 처리합니다.
     * @Validated 어노테이션에 의한 메서드 파라미터 검증 실패 시 발생합니다.
     *
     * @param e ConstraintViolationException 인스턴스
     * @return 에러 응답 (필드별 검증 오류 포함)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Constraint violation: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT.getCode(), "입력값 검증에 실패했습니다.", errors));
    }

    /**
     * 인증 예외를 처리합니다.
     * 인증되지 않은 사용자의 요청 시 발생합니다.
     *
     * @param e AuthenticationException 인스턴스
     * @return 401 Unauthorized 응답
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
    }

    /**
     * 접근 거부 예외를 처리합니다.
     * 인증된 사용자이지만 해당 리소스에 대한 권한이 없을 때 발생합니다.
     *
     * @param e AccessDeniedException 인스턴스
     * @return 403 Forbidden 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage()));
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외를 처리합니다.
     * 허용되지 않은 HTTP 메서드로 요청 시 발생합니다.
     *
     * @param e HttpRequestMethodNotSupportedException 인스턴스
     * @return 405 Method Not Allowed 응답
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED.getCode(), ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    /**
     * 핸들러를 찾을 수 없는 예외를 처리합니다.
     * 존재하지 않는 엔드포인트로 요청 시 발생합니다.
     *
     * @param e NoHandlerFoundException 인스턴스
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NoHandlerFoundException e) {
        log.warn("Handler not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    /**
     * 기타 모든 예외를 처리합니다.
     * 예상하지 못한 예외가 발생했을 때 호출됩니다.
     *
     * @param e Exception 인스턴스
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
