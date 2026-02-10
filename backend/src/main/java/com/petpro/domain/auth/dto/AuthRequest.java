package com.petpro.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
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
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Register {
        /** 이메일 (필수, 이메일 형식, 중복 불가) */
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        /** 비밀번호 (필수, 8~50자, 영문+숫자+특수문자) */
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.")
        private String password;

        /** 이름 (필수, 최대 100자) */
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        private String name;

        /** 닉네임 (필수, 최대 50자) */
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        private String nickname;

        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;

        /** 이용약관 동의 (필수) */
        private boolean agreeTerms;

        /** 개인정보처리방침 동의 (필수) */
        private boolean agreePrivacy;

        /** 마케팅 정보 수신 동의 (선택) */
        private boolean agreeMarketing;
    }

    /**
     * 인증번호 발송 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class SendVerification {
        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;
    }

    /**
     * 인증번호 확인 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class VerifyCode {
        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;

        /** 인증번호 (필수, 6자리) */
        @NotBlank(message = "인증번호는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        private String code;
    }

    /**
     * 비밀번호 재설정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ResetPassword {
        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;

        /** 새 비밀번호 (필수, 8~50자, 영문+숫자+특수문자) */
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.")
        private String newPassword;
    }

    /**
     * 토큰 갱신 요청 DTO
     *
     * Refresh Token으로 새로운 Access Token 발급 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RefreshToken {
        /** Refresh Token (필수) */
        @NotBlank(message = "Refresh Token은 필수입니다.")
        private String refreshToken;
    }

    /**
     * 소셜 로그인 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class OAuthLogin {
        /** Authorization Code (필수) */
        @NotBlank(message = "인가 코드는 필수입니다.")
        private String code;
    }

    /**
     * 아이디 찾기 요청 DTO (이름 + 전화번호)
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class FindIdRequest {
        /** 이름 (필수) */
        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;
    }

    /**
     * 인증번호 확인 요청 DTO (requestId 기반)
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class VerifyRequest {
        /** 요청 ID (필수) */
        @NotBlank(message = "요청 ID는 필수입니다.")
        private String requestId;

        /** 인증번호 (필수, 6자리) */
        @NotBlank(message = "인증번호는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        private String code;
    }

    /**
     * 재전송 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ResendRequest {
        /** 요청 ID (필수) */
        @NotBlank(message = "요청 ID는 필수입니다.")
        private String requestId;
    }

    /**
     * 비밀번호 재설정 요청 DTO (이메일 + 전화번호)
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        /** 이메일 (필수) */
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        /** 전화번호 (필수, 한국 휴대전화 형식) */
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대전화 번호 형식이 아닙니다.")
        private String phone;
    }

    /**
     * 비밀번호 재설정 확정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ResetPasswordConfirm {
        /** 비밀번호 재설정 토큰 (필수) */
        @NotBlank(message = "토큰은 필수입니다.")
        private String token;

        /** 새 비밀번호 (필수, 8~50자, 영문+숫자+특수문자) */
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.")
        private String newPassword;
    }
}
