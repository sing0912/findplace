package com.petpro.global.exception;

import lombok.Getter;

/**
 * BusinessException
 *
 * 비즈니스 로직에서 발생하는 예외의 기본 클래스입니다.
 * 모든 커스텀 비즈니스 예외는 이 클래스를 상속받아야 합니다.
 *
 * 특징:
 * - ErrorCode를 통한 일관된 에러 정보 관리
 * - 추가 상세 정보(details) 전달 가능
 * - GlobalExceptionHandler에서 통합 처리
 *
 * 사용 예시:
 * - throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * - throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일 형식이 올바르지 않습니다.");
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 에러 코드 정보 */
    private final ErrorCode errorCode;

    /** 추가 상세 정보 (선택적) */
    private final Object details;

    /**
     * 에러 코드만으로 예외를 생성합니다.
     * 에러 코드에 정의된 기본 메시지를 사용합니다.
     *
     * @param errorCode 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * 에러 코드와 커스텀 메시지로 예외를 생성합니다.
     * 기본 메시지 대신 지정된 메시지를 사용합니다.
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * 에러 코드와 상세 정보로 예외를 생성합니다.
     * 기본 메시지를 사용하며 추가 정보를 전달합니다.
     *
     * @param errorCode 에러 코드
     * @param details 추가 상세 정보 (예: 실패한 필드 목록)
     */
    public BusinessException(ErrorCode errorCode, Object details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * 에러 코드, 커스텀 메시지, 상세 정보로 예외를 생성합니다.
     * 가장 상세한 예외 정보를 제공합니다.
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     * @param details 추가 상세 정보
     */
    public BusinessException(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
