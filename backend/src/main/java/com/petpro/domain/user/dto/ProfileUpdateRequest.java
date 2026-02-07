package com.petpro.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    private String phone;

    private String profileImageUrl;

    private LocalDate birthDate;
}
