package com.petpro.domain.auth.entity;

/**
 * SMS 인증 유형
 *
 * - FIND_ID: 아이디(이메일) 찾기
 * - RESET_PASSWORD: 비밀번호 재설정
 */
public enum VerificationType {
    FIND_ID("아이디 찾기"),
    RESET_PASSWORD("비밀번호 재설정");

    private final String description;

    VerificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
