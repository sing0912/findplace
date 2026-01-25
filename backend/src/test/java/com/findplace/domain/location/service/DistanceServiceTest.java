package com.findplace.domain.location.service;

import com.findplace.domain.location.dto.Coordinates;
import com.findplace.domain.location.dto.DistanceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DistanceService 테스트")
class DistanceServiceTest {

    private DistanceService distanceService;

    @BeforeEach
    void setUp() {
        distanceService = new DistanceService();
    }

    @Test
    @DisplayName("두 지점 간 직선 거리 계산")
    void calculateHaversineDistance() {
        Coordinates seoul = Coordinates.of(37.5666805, 126.9784147);
        Coordinates busan = Coordinates.of(35.1795543, 129.0756416);

        double distance = distanceService.calculateHaversineDistance(seoul, busan);

        assertThat(distance).isBetween(320.0, 330.0);
    }

    @Test
    @DisplayName("거리 결과 객체 반환")
    void calculateDistance() {
        Coordinates gangnam = Coordinates.of(37.4979, 127.0276);
        Coordinates seolleung = Coordinates.of(37.5044, 127.0490);

        DistanceResult result = distanceService.calculateDistance(gangnam, seolleung);

        assertThat(result.getDistanceMeters()).isBetween(1800L, 2100L);
        assertThat(result.getDistanceText()).isNotBlank();
    }

    @Test
    @DisplayName("여러 목적지 거리 계산 및 정렬")
    void calculateDistances() {
        Coordinates origin = Coordinates.of(37.5666805, 126.9784147); // 서울 시청
        List<Coordinates> destinations = List.of(
                Coordinates.of(35.1795543, 129.0756416), // 부산 (약 325km)
                Coordinates.of(37.4563, 126.7052),       // 인천 (약 27km)
                Coordinates.of(35.8714, 128.6014)        // 대구 (약 240km)
        );

        List<DistanceResult> results = distanceService.calculateDistances(origin, destinations);

        assertThat(results).hasSize(3);
        // 거리순 정렬 확인 (인천 < 대구 < 부산)
        assertThat(results.get(0).getDistanceMeters()).isLessThan(results.get(1).getDistanceMeters());
        assertThat(results.get(1).getDistanceMeters()).isLessThan(results.get(2).getDistanceMeters());
    }

    @Test
    @DisplayName("반경 내 여부 확인 - 반경 내")
    void isWithinRadius_True() {
        Coordinates gangnam = Coordinates.of(37.4979, 127.0276);
        Coordinates seolleung = Coordinates.of(37.5044, 127.0490);

        boolean result = distanceService.isWithinRadius(gangnam, seolleung, 5.0);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("반경 내 여부 확인 - 반경 외")
    void isWithinRadius_False() {
        Coordinates seoul = Coordinates.of(37.5666805, 126.9784147);
        Coordinates busan = Coordinates.of(35.1795543, 129.0756416);

        boolean result = distanceService.isWithinRadius(seoul, busan, 100.0);

        assertThat(result).isFalse();
    }
}
