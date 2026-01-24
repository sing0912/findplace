package com.findplace.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 *
 * 애플리케이션에서 발생하는 모든 에러 코드를 정의하는 열거형입니다.
 * 각 에러 코드는 HTTP 상태, 에러 코드 문자열, 에러 메시지를 포함합니다.
 *
 * 코드 체계:
 * - C: Common (공통)
 * - A: Auth (인증)
 * - U: User (사용자)
 * - CO: Company (업체)
 * - S: Supplier (공급사)
 * - P: Product (상품)
 * - O: Order (주문)
 * - R: Reservation (예약)
 * - PY: Payment (결제)
 * - F: File (파일)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==================== Common (공통) ====================
    /** 잘못된 입력값 */
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    /** 요청한 리소스를 찾을 수 없음 */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "리소스를 찾을 수 없습니다."),
    /** 서버 내부 오류 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    /** 지원하지 않는 HTTP 메서드 */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않은 메서드입니다."),
    /** 접근 권한 없음 */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근이 거부되었습니다."),

    // ==================== Auth (인증) ====================
    /** 인증되지 않은 요청 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    /** 유효하지 않은 토큰 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    /** 만료된 토큰 */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    /** 잘못된 로그인 정보 */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A004", "잘못된 인증 정보입니다."),

    // ==================== User (사용자) ====================
    /** 사용자를 찾을 수 없음 */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    /** 이미 존재하는 이메일 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    /** 이미 존재하는 전화번호 */
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "U003", "이미 사용 중인 전화번호입니다."),

    // ==================== Company (업체) ====================
    /** 업체를 찾을 수 없음 */
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "CO001", "업체를 찾을 수 없습니다."),

    // ==================== Supplier (공급사) ====================
    /** 공급사를 찾을 수 없음 */
    SUPPLIER_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "공급사를 찾을 수 없습니다."),
    /** 재고 부족 */
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "S002", "재고가 부족합니다."),

    // ==================== Product (상품) ====================
    /** 상품을 찾을 수 없음 */
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),

    // ==================== Order (주문) ====================
    /** 주문을 찾을 수 없음 */
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    /** 잘못된 주문 상태 전이 */
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O002", "잘못된 주문 상태입니다."),

    // ==================== Reservation (예약) ====================
    /** 예약을 찾을 수 없음 */
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예약을 찾을 수 없습니다."),
    /** 예약 불가능한 시간대 */
    SLOT_NOT_AVAILABLE(HttpStatus.CONFLICT, "R002", "해당 시간은 예약이 불가능합니다."),

    // ==================== Payment (결제) ====================
    /** 결제 처리 실패 */
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PY001", "결제에 실패했습니다."),
    /** 환불 처리 실패 */
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "PY002", "환불에 실패했습니다."),

    // ==================== File (파일) ====================
    /** 파일 업로드 실패 */
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    /** 파일을 찾을 수 없음 */
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "파일을 찾을 수 없습니다."),
    /** 허용되지 않은 파일 형식 */
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않은 파일 형식입니다.");

    /** HTTP 상태 코드 */
    private final HttpStatus httpStatus;

    /** 에러 코드 문자열 (예: U001, P002) */
    private final String code;

    /** 사용자에게 표시할 에러 메시지 */
    private final String message;
}
