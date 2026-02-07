package com.petpro.domain.auth.dto;

import com.petpro.domain.auth.entity.AuthProvider;
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

    /**
     * 아이디 찾기 결과 응답 DTO
     */
    @Getter
    @Builder
    public static class FindIdResult {
        /** 찾은 이메일 (마스킹 처리) */
        private String email;

        /** 가입 제공자 */
        private AuthProvider provider;

        public static FindIdResult of(String email, AuthProvider provider) {
            return FindIdResult.builder()
                    .email(maskEmail(email))
                    .provider(provider)
                    .build();
        }

        private static String maskEmail(String email) {
            if (email == null || !email.contains("@")) {
                return email;
            }
            String[] parts = email.split("@");
            String localPart = parts[0];
            String domain = parts[1];

            if (localPart.length() <= 3) {
                return localPart.charAt(0) + "**@" + domain;
            }
            return localPart.substring(0, 3) + "***@" + domain;
        }
    }

    /**
     * 성공 메시지 응답 DTO
     */
    @Getter
    @Builder
    public static class SuccessMessage {
        private boolean success;
        private String message;

        public static SuccessMessage of(String message) {
            return SuccessMessage.builder()
                    .success(true)
                    .message(message)
                    .build();
        }
    }

    /**
     * 회원가입 결과 응답 DTO
     */
    @Getter
    @Builder
    public static class RegisterResult {
        private Long id;
        private String email;
        private String nickname;
        private String createdAt;
    }

    /**
     * 중복 확인 응답 DTO
     */
    @Getter
    @Builder
    public static class AvailabilityCheck {
        private boolean available;
        private String message;

        public static AvailabilityCheck available() {
            return AvailabilityCheck.builder()
                    .available(true)
                    .build();
        }

        public static AvailabilityCheck unavailable(String message) {
            return AvailabilityCheck.builder()
                    .available(false)
                    .message(message)
                    .build();
        }
    }

    /**
     * 인증 요청 응답 DTO
     */
    @Getter
    @Builder
    public static class VerificationRequestResult {
        private String requestId;
        private String expireAt;
    }

    /**
     * 비밀번호 재설정 토큰 응답 DTO
     */
    @Getter
    @Builder
    public static class PasswordResetToken {
        private String token;
    }

    /**
     * 소셜 로그인 응답 DTO
     */
    @Getter
    @Builder
    public static class OAuthResult {
        private String accessToken;
        private String refreshToken;
        private UserInfo user;
        private boolean isNewUser;
    }

    /**
     * 사용자 정보 응답 DTO (소셜 로그인용)
     */
    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImageUrl;
    }
}
