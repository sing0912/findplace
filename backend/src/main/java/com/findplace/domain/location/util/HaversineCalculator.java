package com.findplace.domain.location.util;

import com.findplace.domain.location.dto.Coordinates;

/**
 * Haversine 공식을 이용한 두 좌표 간 직선 거리 계산 유틸리티
 */
public final class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineCalculator() {
        // 유틸리티 클래스
    }

    /**
     * 두 좌표 간 직선 거리 계산 (km)
     *
     * @param lat1 시작점 위도
     * @param lon1 시작점 경도
     * @param lat2 끝점 위도
     * @param lon2 끝점 경도
     * @return 거리 (km)
     */
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 두 좌표 간 직선 거리 계산 (km)
     */
    public static double calculate(Coordinates from, Coordinates to) {
        return calculate(
            from.getLatitude(), from.getLongitude(),
            to.getLatitude(), to.getLongitude()
        );
    }

    /**
     * 두 좌표 간 직선 거리 계산 (m)
     */
    public static double calculateMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculate(lat1, lon1, lat2, lon2) * 1000;
    }

    /**
     * 두 좌표 간 직선 거리 계산 (m)
     */
    public static double calculateMeters(Coordinates from, Coordinates to) {
        return calculate(from, to) * 1000;
    }
}
