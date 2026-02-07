package com.petpro.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주소 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AddressUpdateRequest {

    @NotBlank(message = "주소를 입력해주세요")
    private String address;

    private String addressDetail;

    private String zipCode;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
