package com.petpro.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ApiResponse
 *
 * REST API의 표준 응답 포맷을 정의하는 제네릭 클래스입니다.
 * 모든 API 응답은 이 클래스를 통해 일관된 형식으로 반환됩니다.
 *
 * 응답 구조:
 * - success: 요청 성공 여부
 * - message: 응답 메시지 (선택적)
 * - data: 실제 응답 데이터 (성공 시)
 * - error: 에러 상세 정보 (실패 시)
 * - timestamp: 응답 시간
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 요청 성공 여부 */
    private final boolean success;

    /** 응답 메시지 */
    private final String message;

    /** 응답 데이터 */
    private final T data;

    /** 에러 상세 정보 */
    private final ErrorDetail error;

    /** 응답 생성 시간 */
    private final LocalDateTime timestamp;

    /**
     * 데이터만 포함하는 성공 응답을 생성합니다.
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 데이터와 메시지를 포함하는 성공 응답을 생성합니다.
     *
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @param <T> 데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 에러 코드와 메시지를 포함하는 실패 응답을 생성합니다.
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 에러 코드, 메시지, 상세 정보를 포함하는 실패 응답을 생성합니다.
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param details 에러 상세 정보 (예: 유효성 검증 실패 필드 목록)
     * @param <T> 데이터 타입
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ErrorDetail
     *
     * 에러 응답의 상세 정보를 담는 내부 클래스입니다.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        /** 에러 코드 (예: U001, P002 등) */
        private final String code;

        /** 에러 메시지 */
        private final String message;

        /** 추가 상세 정보 (유효성 검증 오류 등) */
        private final Object details;
    }
}
