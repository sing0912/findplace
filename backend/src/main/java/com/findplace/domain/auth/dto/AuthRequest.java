package com.findplace.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * 인증 요청 DTO 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 요청 데이터를 담는 DTO 모음
 */
public class AuthRequest {

    /**
     * 로그인 요청 DTO
     */
    @Getter
    @Builder
    public static class Login {
        /** 이메일 (필수, 이메일 형식) */
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        /** 비밀번호 (필수) */
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    /**
     * 회원가입 요청 DTO
     */
    @Getter
    @Builder
    public static class Register {
        /** 이메일 (필수, 이메일 형식, 중복 불가) */
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        /** 비밀번호 (필수, 8~50자) */
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        private String password;

        /** 이름 (필수, 최대 100자) */
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        private String name;

        /** 전화번호 (선택) */
        private String phone;
    }

    /**
     * 토큰 갱신 요청 DTO
     *
     * Refresh Token으로 새로운 Access Token 발급 요청
     */
    @Getter
    @Builder
    public static class RefreshToken {
        /** Refresh Token (필수) */
        @NotBlank(message = "Refresh Token은 필수입니다.")
        private String refreshToken;
    }
}
