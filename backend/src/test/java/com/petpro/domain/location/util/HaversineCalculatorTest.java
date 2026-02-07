package com.petpro.domain.location.util;

import com.petpro.domain.location.dto.Coordinates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HaversineCalculator 테스트")
class HaversineCalculatorTest {

    @Test
    @DisplayName("서울-부산 간 직선 거리 계산")
    void calculateSeoulToBusan() {
        // 서울 시청
        double seoulLat = 37.5666805;
        double seoulLng = 126.9784147;

        // 부산 시청
        double busanLat = 35.1795543;
        double busanLng = 129.0756416;

        double distance = HaversineCalculator.calculate(seoulLat, seoulLng, busanLat, busanLng);

        // 서울-부산 직선거리 약 325km
        assertThat(distance).isBetween(320.0, 330.0);
    }

    @Test
    @DisplayName("같은 좌표 간 거리는 0")
    void calculateSameLocation() {
        double lat = 37.5666805;
        double lng = 126.9784147;

        double distance = HaversineCalculator.calculate(lat, lng, lat, lng);

        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Coordinates 객체로 거리 계산")
    void calculateWithCoordinates() {
        Coordinates seoul = Coordinates.of(37.5666805, 126.9784147);
        Coordinates busan = Coordinates.of(35.1795543, 129.0756416);

        double distance = HaversineCalculator.calculate(seoul, busan);

        assertThat(distance).isBetween(320.0, 330.0);
    }

    @Test
    @DisplayName("미터 단위로 거리 계산")
    void calculateMeters() {
        // 강남역 - 선릉역 (약 1.5km)
        Coordinates gangnam = Coordinates.of(37.4979, 127.0276);
        Coordinates seolleung = Coordinates.of(37.5044, 127.0490);

        double distanceMeters = HaversineCalculator.calculateMeters(gangnam, seolleung);

        // 약 1.9km
        assertThat(distanceMeters).isBetween(1800.0, 2100.0);
    }
}
