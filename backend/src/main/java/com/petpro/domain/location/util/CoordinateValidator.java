package com.petpro.domain.location.util;

import com.petpro.domain.location.dto.Coordinates;

/**
 * 좌표 유효성 검증 유틸리티
 */
public final class CoordinateValidator {

    // 대한민국 좌표 범위
    private static final double KOREA_MIN_LAT = 33.0;
    private static final double KOREA_MAX_LAT = 39.0;
    private static final double KOREA_MIN_LNG = 124.0;
    private static final double KOREA_MAX_LNG = 132.0;

    private CoordinateValidator() {
        // 유틸리티 클래스
    }

    /**
     * 유효한 좌표인지 확인
     */
    public static boolean isValid(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90
            && longitude >= -180 && longitude <= 180;
    }

    /**
     * 유효한 좌표인지 확인
     */
    public static boolean isValid(Coordinates coordinates) {
        if (coordinates == null) {
            return false;
        }
        return isValid(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /**
     * 대한민국 범위 내 좌표인지 확인
     */
    public static boolean isInKorea(double latitude, double longitude) {
        return latitude >= KOREA_MIN_LAT && latitude <= KOREA_MAX_LAT
            && longitude >= KOREA_MIN_LNG && longitude <= KOREA_MAX_LNG;
    }

    /**
     * 대한민국 범위 내 좌표인지 확인
     */
    public static boolean isInKorea(Coordinates coordinates) {
        if (coordinates == null) {
            return false;
        }
        return isInKorea(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /**
     * 좌표 유효성 검증 및 예외 발생
     */
    public static void validate(Coordinates coordinates) {
        if (!isValid(coordinates)) {
            throw new IllegalArgumentException("Invalid coordinates: " + coordinates);
        }
    }
}
