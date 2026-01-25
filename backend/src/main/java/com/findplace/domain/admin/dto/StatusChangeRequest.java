package com.findplace.domain.admin.dto;

import com.findplace.domain.user.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class StatusChangeRequest {

    @NotNull(message = "변경할 상태를 선택해주세요")
    private UserStatus status;

    @NotBlank(message = "변경 사유를 입력해주세요")
    private String reason;
}
