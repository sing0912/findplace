package com.petpro.domain.user.dto;

import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO 클래스
 *
 * 사용자 관련 API 응답 데이터를 담는 DTO 모음
 * - Info: 상세 정보 (마이페이지, 사용자 상세 조회)
 * - Simple: 간략 정보 (목록 조회)
 */
public class UserResponse {

    /**
     * 사용자 상세 정보 응답 DTO
     *
     * 사용자의 모든 정보를 포함 (비밀번호 제외)
     */
    @Getter
    @Builder
    public static class Info {
        /** 사용자 ID */
        private Long id;

        /** 이메일 */
        private String email;

        /** 이름 */
        private String name;

        /** 닉네임 */
        private String nickname;

        /** 전화번호 */
        private String phone;

        /** 역할 */
        private UserRole role;

        /** 계정 상태 */
        private UserStatus status;

        /** 프로필 이미지 URL */
        private String profileImageUrl;

        /** 인증 제공자 */
        private AuthProvider provider;

        /** 마지막 로그인 일시 */
        private LocalDateTime lastLoginAt;

        /** 생성일시 */
        private LocalDateTime createdAt;

        /** 수정일시 */
        private LocalDateTime updatedAt;

        /**
         * User 엔티티를 Info DTO로 변환
         *
         * @param user 사용자 엔티티
         * @return Info DTO
         */
        public static Info from(User user) {
            return Info.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .profileImageUrl(user.getProfileImageUrl())
                    .provider(user.getProvider())
                    .lastLoginAt(user.getLastLoginAt())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 사용자 간략 정보 응답 DTO
     *
     * 목록 조회 시 사용하는 간략한 정보만 포함
     */
    @Getter
    @Builder
    public static class Simple {
        /** 사용자 ID */
        private Long id;

        /** 이메일 */
        private String email;

        /** 이름 */
        private String name;

        /** 역할 */
        private UserRole role;

        /** 계정 상태 */
        private UserStatus status;

        /**
         * User 엔티티를 Simple DTO로 변환
         *
         * @param user 사용자 엔티티
         * @return Simple DTO
         */
        public static Simple from(User user) {
            return Simple.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .build();
        }
    }
}
