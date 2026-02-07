package com.petpro.domain.location.service;

import com.petpro.domain.location.dto.Coordinates;
import com.petpro.domain.location.dto.DistanceResult;
import com.petpro.domain.location.dto.GeocodingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 통합 위치 서비스
 *
 * 지오코딩, 거리 계산 등 위치 관련 기능을 통합 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class LocationService {

    private final GeocodingService geocodingService;
    private final DistanceService distanceService;

    /**
     * 주소로 좌표 조회
     */
    public Coordinates getCoordinates(String address) {
        return geocodingService.getCoordinates(address);
    }

    /**
     * 좌표로 주소 조회
     */
    public GeocodingResult getAddress(double latitude, double longitude) {
        return geocodingService.reverseGeocode(latitude, longitude);
    }

    /**
     * 두 지점 간 직선 거리 계산 (km)
     */
    public double calculateDistance(Coordinates from, Coordinates to) {
        return distanceService.calculateHaversineDistance(from, to);
    }

    /**
     * 두 지점 간 거리 계산 결과
     */
    public DistanceResult calculateDistanceResult(Coordinates from, Coordinates to) {
        return distanceService.calculateDistance(from, to);
    }

    /**
     * 두 주소 간 직선 거리 계산 (km)
     */
    public double calculateDistanceByAddress(String fromAddress, String toAddress) {
        Coordinates from = getCoordinates(fromAddress);
        Coordinates to = getCoordinates(toAddress);
        return calculateDistance(from, to);
    }

    /**
     * 반경 내에 있는지 확인
     */
    public boolean isWithinRadius(Coordinates from, Coordinates to, double radiusKm) {
        return distanceService.isWithinRadius(from, to, radiusKm);
    }
}
