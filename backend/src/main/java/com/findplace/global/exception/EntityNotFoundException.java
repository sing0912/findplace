package com.findplace.global.exception;

/**
 * EntityNotFoundException
 *
 * 데이터베이스에서 엔티티를 찾을 수 없을 때 발생하는 예외입니다.
 * BusinessException을 상속받아 일관된 예외 처리를 지원합니다.
 *
 * 사용 예시:
 * - 특정 ID로 조회 시 해당 엔티티가 없는 경우
 * - 특정 조건으로 검색 시 결과가 없는 경우
 */
public class EntityNotFoundException extends BusinessException {

    /**
     * 에러 코드만으로 예외를 생성합니다.
     *
     * @param errorCode 에러 코드
     */
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 에러 코드와 엔티티 ID로 예외를 생성합니다.
     * 메시지에 ID 정보가 포함됩니다.
     *
     * @param errorCode 에러 코드
     * @param id 찾을 수 없는 엔티티의 ID
     */
    public EntityNotFoundException(ErrorCode errorCode, Long id) {
        super(errorCode, "ID: " + id);
    }

    /**
     * 에러 코드와 식별자 문자열로 예외를 생성합니다.
     * ID 외의 다른 식별자(이메일, 코드 등)로 조회할 때 사용합니다.
     *
     * @param errorCode 에러 코드
     * @param identifier 식별자 문자열 (예: 이메일, 사용자명 등)
     */
    public EntityNotFoundException(ErrorCode errorCode, String identifier) {
        super(errorCode, identifier);
    }
}
