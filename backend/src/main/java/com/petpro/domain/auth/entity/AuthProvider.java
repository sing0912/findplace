package com.petpro.domain.auth.entity;

/**
 * 인증 제공자 유형
 *
 * 사용자가 가입한 인증 방식을 구분
 * - EMAIL: 이메일/비밀번호 직접 가입
 * - KAKAO, NAVER, GOOGLE: 소셜 로그인
 */
public enum AuthProvider {
    EMAIL("이메일"),
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글");

    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
