package com.petpro.domain.funeralhome.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FuneralHome 엔티티 테스트")
class FuneralHomeTest {

    @Test
    @DisplayName("API 응답으로부터 엔티티 생성")
    void fromApiResponse_Success() {
        // when
        FuneralHome home = FuneralHome.fromApiResponse(
                "펫메모리얼",
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 역삼동 123-45",
                "02-1234-5678",
                "6110000",
                "서울특별시",
                true, false, true
        );

        // then
        assertThat(home.getName()).isEqualTo("펫메모리얼");
        assertThat(home.getRoadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(home.getLotAddress()).isEqualTo("서울시 강남구 역삼동 123-45");
        assertThat(home.getPhone()).isEqualTo("02-1234-5678");
        assertThat(home.getLocCode()).isEqualTo("6110000");
        assertThat(home.getLocName()).isEqualTo("서울특별시");
        assertThat(home.getHasCrematorium()).isTrue();
        assertThat(home.getHasColumbarium()).isFalse();
        assertThat(home.getHasFuneral()).isTrue();
        assertThat(home.getIsActive()).isTrue();
        assertThat(home.getSyncedAt()).isNotNull();
    }

    @Test
    @DisplayName("API 응답으로 정보 업데이트")
    void updateFromApi_Success() {
        // given
        FuneralHome home = FuneralHome.fromApiResponse(
                "펫메모리얼", "주소1", "주소2", "010-0000-0000",
                "code", "지역", false, false, false
        );

        // when
        home.updateFromApi(
                "새주소1", "새주소2", "02-9999-8888",
                true, true, true
        );

        // then
        assertThat(home.getRoadAddress()).isEqualTo("새주소1");
        assertThat(home.getLotAddress()).isEqualTo("새주소2");
        assertThat(home.getPhone()).isEqualTo("02-9999-8888");
        assertThat(home.getHasCrematorium()).isTrue();
        assertThat(home.getHasColumbarium()).isTrue();
        assertThat(home.getHasFuneral()).isTrue();
    }

    @Test
    @DisplayName("좌표 설정")
    void setCoordinates_Success() {
        // given
        FuneralHome home = FuneralHome.builder()
                .name("테스트")
                .build();

        // when
        home.setCoordinates(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        // then
        assertThat(home.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(37.5665));
        assertThat(home.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(126.9780));
        assertThat(home.getGeocodedAt()).isNotNull();
        assertThat(home.hasCoordinates()).isTrue();
    }

    @Test
    @DisplayName("좌표 없음 확인")
    void hasCoordinates_NoCoordinates() {
        // given
        FuneralHome home = FuneralHome.builder()
                .name("테스트")
                .build();

        // then
        assertThat(home.hasCoordinates()).isFalse();
    }

    @Test
    @DisplayName("활성화 상태 변경")
    void activateAndDeactivate() {
        // given
        FuneralHome home = FuneralHome.builder()
                .name("테스트")
                .isActive(true)
                .build();

        // when
        home.deactivate();

        // then
        assertThat(home.getIsActive()).isFalse();

        // when
        home.activate();

        // then
        assertThat(home.getIsActive()).isTrue();
    }
}
