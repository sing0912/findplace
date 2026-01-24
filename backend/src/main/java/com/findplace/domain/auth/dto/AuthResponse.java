package com.findplace.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 인증 응답 DTO 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 응답 데이터를 담는 DTO 모음
 */
public class AuthResponse {

    /**
     * 토큰 응답 DTO
     *
     * JWT 토큰 정보를 담는 DTO
     */
    @Getter
    @Builder
    public static class Token {
        /** Access Token (API 요청 시 사용) */
        private String accessToken;

        /** Refresh Token (Access Token 갱신 시 사용) */
        private String refreshToken;

        /** Access Token 만료 시간 (초 단위) */
        private Long expiresIn;

        /** 토큰 타입 (항상 "Bearer") */
        private String tokenType;

        /**
         * Token DTO 생성 팩토리 메서드
         *
         * @param accessToken Access Token
         * @param refreshToken Refresh Token
         * @param expiresIn 만료 시간 (초)
         * @return Token DTO
         */
        public static Token of(String accessToken, String refreshToken, Long expiresIn) {
            return Token.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .tokenType("Bearer")
                    .build();
        }
    }
}
