package com.petpro.global.exception;

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
    /** 인증 요청을 찾을 수 없음 */
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "A005", "인증 요청을 찾을 수 없습니다."),
    /** 인증번호 만료 */
    VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "A006", "인증번호가 만료되었습니다."),
    /** 인증번호 불일치 */
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "A007", "인증번호가 일치하지 않습니다."),
    /** 인증되지 않은 요청 (인증번호 미확인) */
    VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "A008", "인증번호 확인이 필요합니다."),
    /** 인증번호 시도 횟수 초과 */
    VERIFICATION_MAX_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "A010", "인증번호 시도 횟수를 초과했습니다. 새로운 인증번호를 요청해주세요."),
    /** 소셜 로그인 사용자 */
    SOCIAL_LOGIN_USER(HttpStatus.BAD_REQUEST, "A009", "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."),
    /** 필수 약관 미동의 */
    TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "A010", "필수 약관에 동의해주세요."),
    /** OAuth 인증 실패 */
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A011", "소셜 로그인 인증에 실패했습니다."),
    /** 계정 잠금 (로그인 5회 실패) */
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "A012", "로그인 5회 실패로 계정이 잠겼습니다. 30분 후 다시 시도해주세요."),

    // ==================== User (사용자) ====================
    /** 사용자를 찾을 수 없음 */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    /** 이미 존재하는 이메일 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    /** 이미 존재하는 전화번호 */
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "U003", "이미 사용 중인 전화번호입니다."),
    /** 잘못된 비밀번호 */
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U004", "비밀번호가 일치하지 않습니다."),
    /** 새 비밀번호 불일치 */
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U005", "새 비밀번호가 일치하지 않습니다."),
    /** 잘못된 입력값 */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "U006", "잘못된 입력값입니다."),
    /** 이미 존재하는 닉네임 */
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U007", "이미 사용 중인 닉네임입니다."),
    /** 파일 크기 초과 */
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "U008", "파일 크기는 5MB 이하여야 합니다."),
    /** 취약한 비밀번호 */
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "U009", "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다."),

    // ==================== Inquiry (문의) ====================
    /** 문의를 찾을 수 없음 */
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "IQ001", "문의를 찾을 수 없습니다."),
    /** 이미 답변된 문의 */
    INQUIRY_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "IQ002", "답변이 완료된 문의는 수정/삭제할 수 없습니다."),

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
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않은 파일 형식입니다."),

    // ==================== Region (지역) ====================
    /** 지역을 찾을 수 없음 */
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "RG001", "지역을 찾을 수 없습니다."),

    // ==================== Pet (반려동물) ====================
    /** 반려동물을 찾을 수 없음 */
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PT001", "반려동물을 찾을 수 없습니다."),
    /** 반려동물 등록 한도 초과 */
    PET_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PT002", "반려동물 등록 한도를 초과했습니다."),
    /** 성향 체크리스트를 찾을 수 없음 */
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PT003", "성향 체크리스트를 찾을 수 없습니다."),
    /** 이미 성향 체크리스트가 존재 */
    CHECKLIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "PT004", "이미 성향 체크리스트가 존재합니다."),

    // ==================== FuneralHome (장례식장) ====================
    /** 장례식장을 찾을 수 없음 */
    FUNERAL_HOME_NOT_FOUND(HttpStatus.NOT_FOUND, "FH001", "장례식장을 찾을 수 없습니다."),
    /** 외부 API 호출 실패 */
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "FH002", "외부 API 호출에 실패했습니다."),
    /** 동기화 진행 중 */
    SYNC_ALREADY_RUNNING(HttpStatus.CONFLICT, "FH003", "이미 동기화가 진행 중입니다."),

    // ==================== Coupon (쿠폰) ====================
    /** 쿠폰을 찾을 수 없음 */
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "CP001", "쿠폰을 찾을 수 없습니다."),
    /** 쿠폰 사용 불가 */
    COUPON_NOT_USABLE(HttpStatus.BAD_REQUEST, "CP002", "사용할 수 없는 쿠폰입니다."),
    /** 쿠폰 만료 */
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "CP003", "만료된 쿠폰입니다."),
    /** 쿠폰 수량 소진 */
    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "CP004", "쿠폰이 모두 소진되었습니다."),
    /** 이미 발급받은 쿠폰 */
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "CP005", "이미 발급받은 쿠폰입니다."),

    // ==================== Location (위치) ====================
    /** 지오코딩 실패 */
    GEOCODING_FAILED(HttpStatus.BAD_REQUEST, "LC001", "주소를 좌표로 변환하는데 실패했습니다."),

    // ==================== Log (로그) ====================
    /** 로그 저장 실패 */
    LOG_WRITE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L001", "로그 저장에 실패했습니다."),
    /** 로그 통계 조회 실패 */
    LOG_STATISTICS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L002", "로그 통계 조회에 실패했습니다.");

    /** HTTP 상태 코드 */
    private final HttpStatus httpStatus;

    /** 에러 코드 문자열 (예: U001, P002) */
    private final String code;

    /** 사용자에게 표시할 에러 메시지 */
    private final String message;
}
