package com.petpro.domain.user.dto;

import com.petpro.domain.user.entity.UserRole;
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
 * 사용자 요청 DTO 클래스
 *
 * 사용자 관련 API 요청 데이터를 담는 DTO 모음
 * - Create: 사용자 생성 요청
 * - Update: 사용자 정보 수정 요청
 * - UpdatePassword: 비밀번호 변경 요청
 * - Search: 사용자 검색 요청
 */
public class UserRequest {

    /**
     * 사용자 생성 요청 DTO
     *
     * 회원가입 또는 관리자가 사용자를 생성할 때 사용
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Create {
        /** 이메일 (필수, 이메일 형식) */
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

        /** 전화번호 (선택, 010-XXXX-XXXX 형식) */
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        private String phone;

        /** 사용자 역할 (선택, 미입력 시 USER) */
        private UserRole role;
    }

    /**
     * 사용자 정보 수정 요청 DTO
     *
     * 프로필 정보 수정 시 사용
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Update {
        /** 이름 (선택, 최대 100자) */
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        private String name;

        /** 전화번호 (선택) */
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        private String phone;

        /** 프로필 이미지 URL (선택) */
        private String profileImageUrl;
    }

    /**
     * 비밀번호 변경 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class UpdatePassword {
        /** 현재 비밀번호 (필수) */
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        private String currentPassword;

        /** 새 비밀번호 (필수, 8~50자) */
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        private String newPassword;
    }

    /**
     * 사용자 검색 요청 DTO
     *
     * 관리자 페이지에서 사용자 검색 시 사용
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Search {
        /** 검색 키워드 (이름, 이메일 검색) */
        private String keyword;

        /** 계정 상태 필터 */
        private String status;

        /** 역할 필터 */
        private String role;
    }

    /**
     * 닉네임 수정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class UpdateNickname {
        /** 새 닉네임 (필수, 최대 50자) */
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        private String nickname;
    }

    /**
     * 회원 탈퇴 요청 DTO
     * 소셜 로그인 사용자는 비밀번호 없이 탈퇴 가능
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DeleteAccount {
        /** 비밀번호 확인 (일반 회원만 필수) */
        private String password;
    }

    /**
     * 내 비밀번호 변경 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class ChangeMyPassword {
        /** 현재 비밀번호 (필수) */
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        private String currentPassword;

        /** 새 비밀번호 (필수, 8~50자) */
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        private String newPassword;
    }
}
