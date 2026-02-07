package com.petpro.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지오코딩 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResult {

    private String formattedAddress;
    private double latitude;
    private double longitude;
    private String placeId;
    private AddressComponents components;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressComponents {
        private String country;
        private String province;
        private String city;
        private String district;
        private String street;
        private String streetNumber;
        private String postalCode;
    }
}
