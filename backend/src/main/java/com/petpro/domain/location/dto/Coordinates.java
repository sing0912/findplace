package com.petpro.domain.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌표 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {

    private double latitude;
    private double longitude;

    @Override
    public String toString() {
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    public static Coordinates of(double latitude, double longitude) {
        return new Coordinates(latitude, longitude);
    }
}
