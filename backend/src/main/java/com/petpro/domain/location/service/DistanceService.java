package com.petpro.domain.location.service;

import com.petpro.domain.location.dto.Coordinates;
import com.petpro.domain.location.dto.DistanceResult;
import com.petpro.domain.location.util.HaversineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 거리 계산 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistanceService {

    /**
     * 두 지점 간 직선 거리 계산 (Haversine 공식 사용, 무료)
     *
     * @param from 시작 좌표
     * @param to 끝 좌표
     * @return 거리 (km)
     */
    public double calculateHaversineDistance(Coordinates from, Coordinates to) {
        return HaversineCalculator.calculate(from, to);
    }

    /**
     * 두 지점 간 직선 거리 계산 및 결과 반환
     */
    public DistanceResult calculateDistance(Coordinates from, Coordinates to) {
        double distanceKm = HaversineCalculator.calculate(from, to);
        return DistanceResult.ofHaversine(distanceKm);
    }

    /**
     * 여러 목적지까지의 거리 일괄 계산 (거리순 정렬)
     */
    public List<DistanceResult> calculateDistances(Coordinates from, List<Coordinates> destinations) {
        return destinations.stream()
                .map(to -> calculateDistance(from, to))
                .sorted((a, b) -> Long.compare(a.getDistanceMeters(), b.getDistanceMeters()))
                .toList();
    }

    /**
     * 반경 내에 있는지 확인
     *
     * @param from 시작 좌표
     * @param to 끝 좌표
     * @param radiusKm 반경 (km)
     * @return 반경 내이면 true
     */
    public boolean isWithinRadius(Coordinates from, Coordinates to, double radiusKm) {
        double distance = calculateHaversineDistance(from, to);
        return distance <= radiusKm;
    }
}
