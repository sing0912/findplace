package com.findplace.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 계정 상태 열거형
 *
 * 사용자 계정의 현재 상태를 나타내는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {
    /** 활성 - 정상적으로 서비스 이용 가능 */
    ACTIVE("활성"),

    /** 비활성 - 이메일 인증 대기 등 */
    INACTIVE("비활성"),

    /** 정지 - 관리자에 의해 이용 정지됨 */
    SUSPENDED("정지"),

    /** 삭제 - 탈퇴 또는 관리자에 의해 삭제됨 */
    DELETED("삭제");

    /** 상태 설명 */
    private final String description;
}
