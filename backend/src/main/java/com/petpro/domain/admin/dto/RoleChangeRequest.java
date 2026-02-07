package com.petpro.domain.admin.dto;

import com.petpro.domain.user.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RoleChangeRequest {

    @NotNull(message = "변경할 역할을 선택해주세요")
    private UserRole role;

    @NotBlank(message = "변경 사유를 입력해주세요")
    private String reason;
}
