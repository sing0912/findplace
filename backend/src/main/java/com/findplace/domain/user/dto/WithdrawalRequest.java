package com.findplace.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@NoArgsConstructor
public class WithdrawalRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
