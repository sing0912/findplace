package com.findplace.domain.user.dto;

import com.findplace.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 프로필 응답 DTO
 */
@Getter
@Builder
public class ProfileResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String profileImageUrl;
    private LocalDate birthDate;
    private String address;
    private String addressDetail;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .birthDate(user.getBirthDate())
                .address(user.getAddress())
                .addressDetail(user.getAddressDetail())
                .zipCode(user.getZipCode())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .build();
    }
}
